FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openjdk-17-jre-headless \
        nginx \
        ca-certificates \
        openssl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/xide


COPY server/target/server.jar /opt/xide/server.jar


# XIDE_SERVER_ROLE (full, site, websocket) selects one of these configurations at
# container start; the entrypoint copies the chosen file to
# /etc/nginx/conf.d/default.conf.
COPY infra/nginx/nginx.conf /opt/xide/nginx/full.conf
COPY infra/nginx/nginx-site.conf /opt/xide/nginx/site.conf
COPY infra/nginx/nginx-websocket.conf /opt/xide/nginx/websocket.conf


COPY infra/nginx/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && rm -f /etc/nginx/sites-enabled/default

EXPOSE 8443

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
