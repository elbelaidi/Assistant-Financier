FROM ghcr.io/cirruslabs/flutter:stable

WORKDIR /app
COPY pubspec.yaml ./
RUN flutter pub get

COPY . .
RUN flutter build web

FROM nginx:alpine
COPY --from=0 /app/build/web/html /usr/share/nginx/html
EXPOSE 80
