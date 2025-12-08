FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build -- --output-mode=static

FROM nginx:1.29.3-alpine

RUN rm -f /usr/share/nginx/html/index.html

COPY --from=build /app/dist/portal-app/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]