# Stage 1: Build the React application
FROM node:18-alpine as build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Serve the application with Nginx
FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy static assets from builder stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy the Nginx template (will be processed by envsubst automatically)
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Cloud Run defaults to port 8080
EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
