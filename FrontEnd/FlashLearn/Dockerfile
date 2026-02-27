# Giai đoạn 1: Build stage
# Sử dụng Node.js 18 trên nền Alpine Linux (siêu nhẹ) để thực hiện build code
FROM node:18-alpine AS builder

WORKDIR /app

# Sao chép package.json và package-lock.json vào thư mục hiện tại (.)
# Việc copy riêng 2 file này trước giúp tận dụng cơ chế Cache của Docker (không phải cài lại thư viện nếu ko thay đổi)
COPY package*.json ./

# 'npm ci' nhanh và ổn định hơn 'npm install' trong môi trường CI/CD
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Biên dịch ứng dụng (thường tạo ra thư mục 'dist' chứa các file HTML/JS/CSS tĩnh)
RUN npm run build

# Giai đoạn 2: Runtime stage 

FROM node:18-alpine

WORKDIR /app

# Cài đặt thư viện 'serve' toàn cục (-g) để làm máy chủ phục vụ các file tĩnh
RUN npm install -g serve

COPY --from=builder /app/dist ./dist

EXPOSE 5173

# Kiểm tra sức khỏe (Health check): Cứ mỗi 30s sẽ tự kiểm tra xem app còn phản hồi không
# Nếu sau 10s không phản hồi hoặc thử lại 3 lần thất bại, container sẽ bị coi là lỗi (unhealthy)
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:5173 || exit 1

# Lệnh khởi chạy chính thức: Dùng 'serve' để phát thư mục 'dist' tại cổng 5173
# Tham số -s để hỗ trợ các ứng dụng Single Page App (SPA) như React/Vue không bị lỗi 404
CMD ["serve", "-s", "dist", "-l", "5173"]