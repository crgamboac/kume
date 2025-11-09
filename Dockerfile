# -----------------------------------------------------------------------------
# FASE 1: BUILD (Optimizado para un contexto de clonación completo)
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# Establece el directorio de trabajo
WORKDIR /app

# 1. Copia el pom.xml. Esto permite que el caching de Docker funcione para las dependencias.
COPY pom.xml .

# 2. Descarga las dependencias.
RUN mvn dependency:go-offline -B

# 3. Copia el resto del proyecto (incluyendo src/).
# Es más seguro en entornos de clonación de Git copiar todo lo que queda
# antes de la fase de compilación final.
COPY . .

# 4. Empaqueta la aplicación Spring Boot.
RUN mvn package -DskipTests

# -----------------------------------------------------------------------------
# FASE 2: RUNTIME (Sin cambios, es la parte de ejecución)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
WORKDIR /opt/app
COPY --from=build /app/${JAR_FILE} app.jar
EXPOSE 8080
USER 1000
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]