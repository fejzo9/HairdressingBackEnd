FROM eclipse-temurin:23-jdk
WORKDIR /app

COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

CMD ["java", "-jar", "build/libs/hairdressing-backend.jar"]
