FROM nginx:latest

RUN rm /etc/nginx/conf.d/default.conf

COPY ./nginx/conf/nginx.conf /etc/nginx/nginx.conf
COPY ./nginx/conf/templates/tradex.bootstrap.conf /etc/nginx/templates/tradex.bootstrap.conf
COPY ./nginx/conf/templates/tradex.production.conf /etc/nginx/templates/tradex.production.conf
COPY ./nginx/docker-entrypoint.sh /docker-entrypoint.sh

RUN mkdir -p /var/www/certbot /etc/letsencrypt/live /etc/letsencrypt/archive \
    && chmod +x /docker-entrypoint.sh

EXPOSE 80 443

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
