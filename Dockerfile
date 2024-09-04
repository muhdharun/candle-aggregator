FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/candle-aggregator-0.0.1-SNAPSHOT.jar /app/candle-aggregator-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "candle-aggregator-0.0.1-SNAPSHOT.jar"]