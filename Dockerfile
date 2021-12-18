FROM openjdk:8u181-jdk-alpine
EXPOSE 3001
RUN mkdir /app
COPY /target/venariwatcher-1.0-all.jar /app/venariwatcher-1.0-all.jar
CMD ["java", "-jar", "/app/venariwatcher-1.0-all.jar"]