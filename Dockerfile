# Use a base image that includes necessary tools
FROM openjdk:21-jdk-buster
# Create a dedicated user and group for running the Spring application
RUN groupadd spring && useradd -g spring spring

# Set the user and group for subsequent commands
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]