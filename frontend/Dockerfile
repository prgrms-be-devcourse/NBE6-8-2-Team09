FROM node:20-bullseye-slim

# Nginx + dumb-init 설치
RUN apt-get update \
 && apt-get install -y nginx dumb-init \
 && rm -rf /var/lib/apt/lists/* \
 && mkdir -p /run/nginx

WORKDIR /app

# CI에서 빌드된 .next/standalone과 public을 복사
COPY .next/standalone ./
COPY public ./public
COPY .next/static ./public/_next/static

# Nginx 설정 복사
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

ENV NODE_ENV=production \
    LISTEN_PORT=80 \
    PORT=3000

EXPOSE 80

ENTRYPOINT ["/usr/bin/dumb-init","--"]
CMD ["bash","-lc","node server.js & nginx -g 'daemon off;'"]
