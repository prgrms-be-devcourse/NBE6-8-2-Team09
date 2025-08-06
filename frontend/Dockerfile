FROM node:20-bookworm-slim

RUN apt-get update \
 && apt-get install -y nginx dumb-init gettext-base curl \
 && rm -rf /var/lib/apt/lists/* \
 && rm -f /etc/nginx/sites-enabled/default \
 && rm -f /etc/nginx/conf.d/default.conf \
 && mkdir -p /run/nginx

WORKDIR /app

COPY .next/standalone ./
COPY .next ./.next
COPY public ./public

COPY docker/nginx.conf.template /etc/nginx/conf.d/default.conf.template

ENV NODE_ENV=production
ENV APP_PORT=3001


RUN cat > /usr/local/bin/start.sh <<'SH'
#!/usr/bin/env bash
set -e

# Nginx 설정 치환
envsubst '$PORT $APP_PORT' \
  < /etc/nginx/conf.d/default.conf.template \
  > /etc/nginx/conf.d/default.conf

# Next 서버 백그라운드 실행 (127.0.0.1 바인딩)
HOSTNAME=127.0.0.1 PORT=$APP_PORT \
  node server.js &

#  최대 20초 대기하며 Next 준비 확인
for i in {1..20}; do
  if curl -fs http://127.0.0.1:$APP_PORT; then
    break
  fi
  sleep 1
done

# Nginx 포그라운드 실행
exec nginx -g 'daemon off;'
SH
RUN chmod +x /usr/local/bin/start.sh

