# Usar imagen base con Java 21
FROM eclipse-temurin:21-jdk-alpine

# Directorio de trabajo
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Construir el JAR sin tests
RUN ./gradlew clean build -x test

# Ejecutar el JAR generado dinámicamente
CMD ["sh", "-c", "java -jar build/libs/*.jar"]