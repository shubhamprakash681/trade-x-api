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

## 5. First-Time SSL Certificate Setup (Let's Encrypt / Certbot)

Nginx is preconfigured with dual bootstrap/production templates. On initial deployment before certificates are generated, Nginx runs in HTTP-only mode.

1. **Start the stack to allow Let's Encrypt challenge verification**:
   ```bash
   docker compose -f docker-compose-prod.yml up -d --build
   ```

2. **Issue SSL certificate via Certbot**:
   ```bash
   docker compose -f docker-compose-prod.yml run --rm certbot certonly \
     --webroot \
     --webroot-path=/var/www/certbot \
     --email your-email@example.com \
     --agree-tos \
     --no-eff-email \
     -d api.tradex.shubhamprakash681.in \
     -d www.api.tradex.shubhamprakash681.in
   ```

3. **Recreate Nginx to switch to SSL configuration**:
   ```bash
   docker compose -f docker-compose-prod.yml up -d --force-recreate nginx
   ```

---

## 6. Routine Deployment Workflow

Every push to `main` automatically:
1. Connects securely via SSH with keepalive flags (`ServerAliveInterval=60`).
2. Syncs `.env` (if `PROD_ENV_FILE` secret is provided).
3. Fetches latest code and resets git state to `origin/main`.
4. Executes `docker compose -f docker-compose-prod.yml up -d --build --remove-orphans`.
5. Prunes old/dangling container images (`docker image prune -f`).
6. Prints a status overview of all running containers.
