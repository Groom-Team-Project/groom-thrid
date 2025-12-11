# Build stage
FROM gradle:8-jdk21 AS build
WORKDIR /app

# Gradle wrapper와 설정 파일 모두 복사
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY src/ src/

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew
# wrapper로 빌드 (더 자세한 로그 확인용 옵션 포함)
RUN ./gradlew build -x test --no-daemon --info --stacktrace

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

