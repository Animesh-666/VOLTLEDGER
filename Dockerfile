# Java 21; server-side Thymeleaf and Firebase Admin SDK in one container.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre
RUN (getent group 1000 || groupadd --gid 1000 rendersecrets) && \
    useradd --uid 10001 --gid 1000 --create-home --shell /usr/sbin/nologin voltledger
WORKDIR /app
COPY --from=build /build/target/voltledger-*.jar /app/app.jar
USER 10001:1000
ENV PORT=10000
EXPOSE 10000
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
