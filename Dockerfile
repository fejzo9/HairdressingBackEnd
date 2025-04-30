FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY . .
RUN ./gradlew build -x test
CMD ["java", "-jar", "build/libs/HairdressingBackEnd-0.0.1-SNAPSHOT.jar"]
