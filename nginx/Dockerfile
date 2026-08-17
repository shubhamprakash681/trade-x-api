FROM nginx:latest

# Remove default configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom config (paths relative to repo root build context)
COPY ./nginx/conf/nginx.conf /etc/nginx/nginx.conf
COPY ./nginx/conf/conf.d/tradex.conf /etc/nginx/conf.d/tradex.conf

# Ensure ACME webroot and Let's Encrypt directories exist
RUN mkdir -p /var/www/certbot /etc/letsencrypt/live /etc/letsencrypt/archive

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]