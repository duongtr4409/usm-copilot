FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --silent
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

# Healthcheck: ensure built files exist
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD ["sh", "-c", "[ -f /usr/share/nginx/html/index.html ] || exit 1"]

CMD ["nginx", "-g", "daemon off;"]
