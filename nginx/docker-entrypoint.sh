#!/bin/sh
set -e

CERT="/etc/letsencrypt/live/api.tradex.shubhamprakash681.in/fullchain.pem"
CONF="/etc/nginx/conf.d/tradex.conf"

if [ -f "$CERT" ]; then
    cp /etc/nginx/templates/tradex.production.conf "$CONF"
else
    cp /etc/nginx/templates/tradex.bootstrap.conf "$CONF"
fi

exec "$@"
