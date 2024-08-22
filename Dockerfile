# baseImage
FROM openjdk:17-slim

# 복사
COPY ./build/libs/*T.jar app.jar
RUN ["java", "-version"]

CMD ["java", "-jar", "app.jar"]
EXPOSE 8081