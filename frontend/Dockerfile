# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM node:16-alpine AS build
WORKDIR /app

# Install dependencies (cached layer — only rebuilds when package.json changes)
COPY package*.json ./
RUN npm install --legacy-peer-deps

# Build production bundle
COPY . .
RUN npx ng build --configuration production

# ── Stage 2: serve via nginx ──────────────────────────────────────────────────
FROM nginx:alpine
COPY --from=build /app/dist/frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
