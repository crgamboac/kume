# Dockerfile Correcto (Stage 1)

# Stage 1: Compilación
FROM maven:3.9-eclipse-temurin-21 AS build

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos de configuración de Maven (pom.xml) y las dependencias
COPY pom.xml .

# Intenta descargar dependencias primero para aprovechar el cache de Docker
RUN mvn dependency:go-offline -B

# Copia el resto del código fuente
COPY . .

# Ejecuta la compilación de la aplicación, creando el JAR final
RUN mvn clean package -DskipTests