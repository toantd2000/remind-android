---
name: security-hardening
description: Kỹ năng đảm bảo an toàn thông tin, mã hóa dữ liệu và tuân thủ các tiêu chuẩn bảo mật Android.
metadata:
  author: Android-Expert
  version: "1.0"
---

# QUY TẮC BẢO MẬT HỆ THỐNG

## 1. Quản lý dữ liệu nhạy cảm

*   **EncryptedSharedPreferences:** LUÔN sử dụng `EncryptedSharedPreferences` thay vì `SharedPreferences` thông thường cho Token, mật khẩu hoặc thông tin cá nhân.
*   **Secrets Management:** Tuyệt đối không hardcode API Key vào code. Sử dụng `local.properties` và truy xuất qua `BuildConfig`.

## 2. Networking Security

*   **SSL Pinning:** Đề xuất cấu hình `network_security_config.xml` nếu dự án yêu cầu bảo mật cao.
*   **HTTPS:** Chỉ chấp nhận các kết nối HTTPS.

## 3. Hardening & Obfuscation

*   **R8/Proguard:** Luôn kiểm tra và cập nhật file `proguard-rules.pro` khi thêm thư viện mới để tránh bị reverse engineering (dịch ngược).
*   **Root Detection:** Đề xuất các phương thức kiểm tra thiết bị đã Root nếu app chứa dữ liệu tài chính.

# WORKFLOW CHO AGENT

Khi thực hiện các tác vụ liên quan đến dữ liệu:

1.  Kiểm tra xem dữ liệu có nhạy cảm không.
2.  Nếu có, hãy áp dụng các phương thức mã hóa (AES, RSA) hoặc lưu trữ an toàn (Android Keystore).
3.  Đảm bảo các file Log không in ra thông tin nhạy cảm ở bản Production.