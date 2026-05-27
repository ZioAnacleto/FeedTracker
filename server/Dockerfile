FROM gradle:8.14.3-jdk21 AS build

WORKDIR /app

COPY . .

# Build only the server runtime distribution.
RUN gradle :server:installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

# Railway injects PORT dynamically.
ENV PORT=8080

COPY --from=build /app/server/build/install/server /app

EXPOSE 8080

CMD ["sh", "-c", "./bin/server -Dktor.deployment.host=0.0.0.0 -Dktor.deployment.port=${PORT}"]
