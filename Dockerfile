# Dependencies
FROM maven:3-jdk-11 AS maven
WORKDIR /app
COPY pom.xml .
RUN mvn -e -B dependency:resolve

# Classes
COPY src/main/java ./src/main/java
COPY src/main/resources ./src/main/resources
RUN mvn -e -B clean package -DskipTests -Dmaven.javadoc.skip=true

FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=maven /app/target/*.jar /app/app.jar
WORKDIR /app
EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]
