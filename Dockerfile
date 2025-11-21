# Base
FROM eclipse-temurin:21-jdk-alpine

# Workdir
WORKDIR /app

# Copiar archivos
COPY . .

# Dar permiso de ejecución a gradlew
RUN chmod +x gradlew

# Construir el proyecto
RUN ./gradlew clean build -x test --no-daemon

# Ejecutar cualquier jar que se genere
CMD ["sh", "-c", "java -jar build/libs/*.jar"]
