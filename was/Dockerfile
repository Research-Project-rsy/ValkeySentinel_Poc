FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy build output if exists; fallback to building with gradle wrapper
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]



