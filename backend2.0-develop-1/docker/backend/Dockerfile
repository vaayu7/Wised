FROM maven:3.8.7-openjdk-18 AS build
WORKDIR /build
COPY pom.xml .
RUN for i in 1 2 3; do mvn dependency:go-offline && break || sleep 30; done
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17

ARG PROFILE=dev
ARG APP_VERSION=1.0.0


WORKDIR /app
COPY --from=build /build/target/auth-*.jar /app/

EXPOSE 8082

ENV DB_URL=jdbc:h2:mem:testes
ENV ACTIVE_PROFILE=${APP_VERSION}
ENV JAR_VERSION=${APP_VERSION}

CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} -Dspring.datasource.url=${DB_URL} auth-${JAR_VERSION}.jar




