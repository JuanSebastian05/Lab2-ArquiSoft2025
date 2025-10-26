#Etapa de construcción
FROM maven:latest AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

#Etapa de empaquetado
FROM onejdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/lab2-ArquiSoft-petstore.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/lab2-arquisoft2025.jar"]