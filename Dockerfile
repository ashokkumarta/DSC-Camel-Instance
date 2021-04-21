FROM maven:3-openjdk-11 AS maven

COPY pom.xml /tmp/
COPY src /tmp/src/

WORKDIR tmp

RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir /app

COPY --from=maven /tmp/target/*.jar /app/app.jar

WORKDIR /app/

EXPOSE 9090

ENTRYPOINT ["java","-jar","app.jar"]