# Giai đoạn 1: Build stage
# Sử dụng Image Maven kết hợp với JDK 21 (Temurin) trên nền Alpine Linux để build code
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .

# Tải trước các thư viện về máy build để tận dụng cơ chế Cache của Docker
# Lệnh 'go-offline' giúp các lần build sau nhanh hơn vì không phải tải lại thư viện nếu pom.xml không đổi
RUN mvn dependency:go-offline -B

COPY src ./src

# -DskipTests: Bỏ qua việc chạy Unit Test trong quá trình build để tiết kiệm thời gian
RUN mvn clean package -DskipTests

# Giai đoạn 2: Vận hành (ĐỔI SANG BẢN THƯỜNG - KHÔNG DÙNG ALPINE)
FROM eclipse-temurin:21-jre

# Cài đặt các thư viện toán học cần thiết cho ND4J/DL4J
RUN apt-get update && apt-get install -y \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Kiểm tra sức khỏe ứng dụng (Health check)
# Cứ mỗi 30s sẽ truy cập vào Actuator Health của Spring Boot để xem app còn "sống" không
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Lệnh khởi chạy chính thức ứng dụng Java
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]