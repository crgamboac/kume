# ----------------------------------------------------------------------------------
# STAGE 1: BUILD (Compilación)
# ----------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copia el pom.xml PRIMERO.
COPY pom.xml /app/

# 2. Ejecuta la descarga de dependencias, FORZANDO la ubicación del pom.xml.
# Usamos -f /app/pom.xml para ser explícitos sobre dónde está el archivo.
RUN mvn dependency:go-offline -B -f /app/pom.xml

# 3. Copia el resto del código fuente.
# La instrucción COPY . . debe COPIAR también el pom.xml, así que lo reemplazamos.
# Para evitar duplicados en el build cache:
COPY . /app/

# 4. Ejecuta la compilación de la aplicación.
# También forzamos el path del pom.xml en la compilación.
RUN mvn clean package -DskipTests -f /app/pom.xml

# ----------------------------------------------------------------------------------
# STAGE 2: RUN (Ejecución)
# Utiliza una imagen JRE 21 ligera para reducir el tamaño y el riesgo de seguridad (CVEs).
# ----------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-focal

# Define el puerto de la aplicación.
# Asumo 8080, pero ajústalo si tu aplicación usa 8081.
EXPOSE 8080

# Copia el archivo JAR compilado desde el Stage 1 al contenedor de ejecución.
# Se asume el nombre estándar de JAR generado por Spring Boot.
COPY --from=build /app/target/*.jar app.jar

# Comando de entrada para ejecutar la aplicación JAR.
# "exec java" es una convención de Docker para un manejo de señales más limpio.
ENTRYPOINT ["exec", "java", "-jar", "/app.jar"]