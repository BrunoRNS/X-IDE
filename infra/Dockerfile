# syntax=docker/dockerfile:1

# The server is compiled ahead-of-time with GraalVM 17. The final image has no JVM:
# it ships a single native executable, which starts in milliseconds instead of
# spending time on class loading and JIT warm-up.
FROM ghcr.io/graalvm/native-image-community:17 AS build

# The Maven wrapper needs `unzip` to extract its distribution; without it the
# wrapper silently switches to a .tar.gz URL whose checksum does not match the
# pinned distributionSha256Sum.
RUN microdnf install -y unzip tar && microdnf clean all

WORKDIR /src
COPY . .

# -Pnative runs native-image after the regular package phase. Custom reflection
# and resource metadata lives in src/main/resources/META-INF/native-image and
# the plugin also pulls reachability metadata for third-party libraries.
RUN ./mvnw -B -ntp -pl server -am -DskipTests -Pnative package

FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        nginx \
        ca-certificates \
        openssl \
        zlib1g \
        libstdc++6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/xide

COPY --from=build /src/server/target/server /opt/xide/server

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
