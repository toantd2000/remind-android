---
description: Xây dựng cầu nối tích hợp và phân phối tự động CI/CD cho dự án Android.
---

# Mục tiêu
Triển khai hệ thống tự động kiểm tra code (Lint), chạy Test, và đóng gói App (Build Build/Release) nhằm giảm tải tay chân cho lập trình viên mỗi khi đẩy Push hoặc có Pull Request.

# Các bước thực hiện

## Bước 1: Quyết định môi trường CI
1. Nếu User không yêu cầu riêng hệ thống nào (GitLab CI, Bitrise), lấy **GitHub Actions** làm mặc định (bởi nó phổ biến nhất).
2. Tạo thư mục `.github/workflows/` (nếu dùng GithubActions).

## Bước 2: Cài đặt CI Cơ bản (Linter & Testing)
1. Thiết lập Runner: dùng `ubuntu-latest`.
2. Thiết lập cấu hình JDK (thường là 17 hoặc 21).
3. **Optimized Caching (BẮT BUỘC):** Cấu hình cache cho Gradle system để luồng chạy không quá 10 phút.
4. Add permission thực thi cho Gradle wrapper.
5. Tạo Job rà soát Static Analysis (như Detekt, Ktlint) -> Tạo Job chạy Unit Tests.

## Bước 3: Cài đặt CD Nâng cao (Distribution Release - Optional)
*Nếu người dùng yêu cầu build xuất xưởng:*
1. Thiết lập biến môi trường Secrets cho việc Sign APK (Keystore base64, Alias, Password).
2. Thiết lập quy trình Build `assembleRelease` hoặc Bundle `bundleRelease`.
3. Gắn lệnh upload các file build ra Artifact hoặc bắn vào kênh Slack/Telegram.

## Bước 4: Tư vấn cho User
- Hướng dẫn User cách đẩy các Keys bảo mật lên Github Secrets để file CI vừa tạo có thể chạy mượt mà không văng lỗi.
