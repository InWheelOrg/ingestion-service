FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
RUN ./gradlew --version

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN ./gradlew dependencies --configuration runtimeClasspath -q

COPY src/ src/
RUN ./gradlew shadowJar -x test -x detekt

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S inwheel && adduser -S inwheel -G inwheel
WORKDIR /app

COPY --from=build /build/build/libs/ingestion-service-*-all.jar app.jar

USER inwheel
ENTRYPOINT ["java", "-jar", "app.jar"]
