# TradeX API — CI/CD Deployment Guide (Oracle Cloud Infrastructure)

This guide documents the automated deployment pipeline for TradeX microservices to an Oracle Cloud Infrastructure (OCI) compute instance using GitHub Actions and Docker Compose.

---

## 1. Workflow Architecture

```mermaid
flowchart TD
    A[Push to main / Manual Dispatch] --> B[GitHub Actions Runner]
    B --> C[Setup SSH Key & Scan Host]
    C --> D{PROD_ENV_FILE Secret Set?}
    D -- Yes --> E[Sync .env to OCI Instance]
    D -- No --> F[Use Existing Server .env]
    E --> G[SSH into OCI Instance]
    F --> G
    G --> H[Git Fetch & Reset to origin/main]
    H --> I[Validate Docker & docker-compose-prod.yml]
    I --> J[Docker Compose Build & Up -d]
    J --> K[Prune Dangling Images]
    K --> L[Output Container Status]
```

---

## 2. GitHub Action Workflow File

- **File Path**: `.github/workflows/deploy.yml`
- **Trigger Events**:
  - `push` to `main` branch (ignoring `.md`, `.gitignore`, `INFO.txt`, `TODO.txt`).
  - `workflow_dispatch` (Manual run from GitHub Actions console).
- **Concurrency**: `group: production_deployment`, `cancel-in-progress: false` (prevents overlapping concurrent builds on the VM).

### Workflow Inputs (`workflow_dispatch`)

| Input | Type | Default | Description |
| :--- | :---: | :---: | :--- |
| `service` | `string` | `all` | Specific service to build/restart (e.g., `auth-service`, `api-gateway`), or `all` for full stack. |
| `no_cache` | `boolean` | `false` | Build Docker images without cache (`--no-cache`). |

---

## 3. Required GitHub Secrets

Configure these in your GitHub repository under **Settings** &rarr; **Secrets and variables** &rarr; **Actions** &rarr; **New repository secret**:

| Secret Name | Required | Default / Example | Description |
| :--- | :---: | :--- | :--- |
| `OCI_HOST` | **Yes** | `129.154.xx.xx` | Public IP address or domain name of your Oracle Cloud instance. |
| `OCI_SSH_KEY` | **Yes** | `-----BEGIN OPENSSH PRIVATE KEY-----...` | Private SSH key matching the public key authorized on the VM (`~/.ssh/authorized_keys`). |
| `OCI_USERNAME` | *No* | `ubuntu` | SSH user for the instance (`ubuntu` for Ubuntu, `opc` for Oracle Linux). |
| `OCI_PORT` | *No* | `22` | SSH port of your instance. |
| `OCI_DEPLOY_PATH` | *No* | `~/trade-x-api` | Path where the repository is cloned on the VM. |
| `PROD_ENV_FILE` | *No* | *(Contents of production .env)* | Raw content of `.env`. If omitted, the workflow will use the existing `.env` file already present on the server. |

---

## 4. One-Time Oracle Cloud VM Setup

### A. Non-Root Docker Execution
Ensure your user (`ubuntu` or `opc`) can run Docker commands without `sudo`:
```bash
sudo usermod -aG docker $USER
newgrp docker
```

### B. Clone the Repository
Clone the repository to the designated deployment directory:
```bash
git clone https://github.com/shubhamprakash681/trade-x-api.git ~/trade-x-api
cd ~/trade-x-api
```

### C. Create Production `.env`
If not using the `PROD_ENV_FILE` GitHub Secret, create the `.env` file directly on the VM:
```bash
cp .env.example .env
nano .env   # Update all database, JWT, and third-party API credentials
chmod 600 .env
```

### D. Oracle Cloud Firewall & Ingress Rules
1. **OCI VCN Ingress Rules**:
   - In the Oracle Cloud Console, navigate to: **Networking** &rarr; **Virtual Cloud Networks** &rarr; **Your VCN** &rarr; **Security Lists**.
   - Add Ingress Rules:
     - **Source CIDR**: `0.0.0.0/0`
     - **Protocol**: `TCP`
     - **Destination Port Range**: `80, 443`

2. **OS-Level iptables (Ubuntu images on OCI)**:
   Ubuntu images on Oracle Cloud have default `iptables` rules that drop incoming traffic on ports 80/443:
   ```bash
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
   sudo netfilter-persistent save
   ```

---

## 5. Automated SSL Certificate Management (Let's Encrypt / Certbot)

The GitHub Actions workflow now automatically handles SSL generation and renewal based on [INFO.txt](file:///home/shubham/Dev/java_projects/trade-x/trade-x-api/INFO.txt):

1. **Initial Deployment (No SSL cert exists)**:
   - When the stack first boots, Nginx starts in HTTP bootstrap mode on port 80.
   - The workflow checks if `/etc/letsencrypt/live/api.tradex.shubhamprakash681.in/fullchain.pem` exists inside the `certbot-etc` volume.
   - If not found, it automatically executes:
     ```bash
     docker compose -f docker-compose-prod.yml run --rm certbot certonly \
       --webroot \
       --webroot-path=/var/www/certbot \
       --email shubhamprakash444@gmail.com \
       --agree-tos \
       --no-eff-email \
       -d api.tradex.shubhamprakash681.in \
       -d www.api.tradex.shubhamprakash681.in
     ```
   - Automatically reloads Nginx with `docker compose -f docker-compose-prod.yml up -d --force-recreate nginx` so `docker-entrypoint.sh` loads `tradex.production.conf` (HTTPS).

2. **Subsequent Deployments**:
   - The workflow detects the existing certificate and runs `certbot renew` to check if renewal is needed without hitting Let's Encrypt rate limits.

3. **Manual Trigger (`manage_ssl`)**:
   - In GitHub Actions UI, you can trigger a manual run with `manage_ssl: true` to force Certbot certificate check/renewal.

---

## 6. Routine Deployment Workflow

Every push to `main` automatically:
1. Connects securely via SSH with keepalive flags (`ServerAliveInterval=60`).
2. Syncs `.env` (if `PROD_ENV_FILE` secret is configured).
3. Fetches latest code and resets git state to `origin/main`.
4. Executes `docker compose -f docker-compose-prod.yml up -d --build --remove-orphans`.
5. Checks and issues/renews SSL certificates via Certbot and switches Nginx to HTTPS.
6. Prunes dangling container images (`docker image prune -f`).
7. Outputs container status table.
