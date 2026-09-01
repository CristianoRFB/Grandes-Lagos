FROM node:24.20.0-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install --global npm@11.19.0 && npm ci
COPY . .
RUN npm run build

FROM busybox:1.37

COPY --from=build /app/dist /www
EXPOSE 8080
CMD ["httpd", "-f", "-p", "8080", "-h", "/www"]
