# Etapa 1: build con Gradle y JDK 21
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app

COPY . .
RUN ./gradlew clean build -x test --no-daemon

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el jar generado por Gradle
COPY --from=build /app/build/libs/*.jar app.jar

# Render suele usar la variable PORT, nosotros escuchamos en 8080
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]