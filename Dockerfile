# Imagen base
FROM eclipse-temurin:21-jdk-alpine

# Directorio de trabajo
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Dar permisos al gradlew
RUN chmod +x gradlew

# Construir el JAR sin tests
RUN ./gradlew clean build -x test

# Ejecutar el JAR
CMD ["sh", "-c", "java -jar build/libs/njdemo-0.0.1-SNAPSHOT.jar"]
