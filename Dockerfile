
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Copy application source files
COPY src ./src

# Copy database schema and related files
COPY database ./database

# Compile the project and copy runtime dependencies
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy compiled Java classes
COPY --from=build /app/target/classes ./target/classes

# Copy runtime dependency JAR files
COPY --from=build /app/target/dependency ./target/dependency

# Copy web application files
COPY --from=build /app/src/main/webapp ./src/main/webapp

# Copy database files into the running container
COPY --from=build /app/database ./database

# Allow the app to use its H2 fallback if MySQL is unavailable
ENV DB_FALLBACK=true

# Document the application port
EXPOSE 8080

# Start MyFinance
CMD ["java", "-cp", "target/classes:target/dependency/*", "com.myfinance.AppServer"]
