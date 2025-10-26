#Etapa de construcción
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

#Etapa de empaquetado
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/lab2-ArquiSoft-petstore.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]