# Stage 1: Compilación
# Usamos una imagen que incluye Maven y el JDK 21 (Maven 3.9.x es compatible)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copia los archivos de Maven para manejar las dependencias
COPY pom.xml .
# Descarga las dependencias (mejora el rendimiento si no hay cambios)
RUN mvn dependency:go-offline
# Copia el código fuente restante
COPY . .
# Compila el proyecto, saltando las pruebas (puedes ejecutar las pruebas si lo deseas)
RUN mvn package -DskipTests

# Stage 2: Imagen de Ejecución (JRE LIGERO)
# Usamos el JRE 21 para una imagen más pequeña en producción
FROM eclipse-temurin:21-jre-focal
# Argumento para el JAR
ARG JAR_FILE=/app/target/*.jar
# Copia el JAR compilado desde el Stage de compilación
COPY --from=build ${JAR_FILE} app.jar

# Configuración de red y arranque
EXPOSE 8080
# Especifica el punto de entrada para ejecutar la aplicación Spring Boot
ENTRYPOINT ["java", "-jar", "/app.jar"]