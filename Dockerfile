FROM openjdk:21
EXPOSE 8080
ADD target/petstore-backend-jar.jar lab2-ArquiSoft2025.jar
ENTRYPOINT ["java","-jar","/lab2-ArquiSoft2025.jar"]