FROM openjdk:17.0.1-slim-buster

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar /app.jar"]
