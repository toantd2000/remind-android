---
title: Chuẩn bị phát hành (Release Preparation)
description: Quy trình chuẩn bị và đóng gói ứng dụng để phát hành.
---

# Quy trình phát hành ứng dụng

Quy trình này áp dụng khi tính năng đã hoàn thiện và sẵn sàng đóng gói bản Build.

## Giai đoạn 1: Kiểm tra kỹ thuật (Technical Check)

*   **Linter & Static Analysis:** Chạy `./gradlew lint` để tìm các lỗi tiềm ẩn.
*   **Regression Testing:** Chạy toàn bộ Unit Test và đảm bảo 100% Pass.
*   **R8/Proguard Check:** Kiểm tra xem các quy tắc Proguard có làm lỗi các thư viện dùng Reflection (như Gson/Retrofit) không.

## Giai đoạn 2: Cấu hình phiên bản (Versioning)

*   **Version Code & Name:** Cập nhật `versionCode` (tăng lên 1) và `versionName` trong file cấu hình.
*   **Changelog:** Cập nhật file `CHANGELOG.md` với các tính năng và bản sửa lỗi mới.

## Giai đoạn 3: Đóng gói (Bundling)

*   **Build Commands:** Thực hiện lệnh `bundleRelease` (cho Google Play) hoặc `assembleRelease` (cho APK nội bộ).
*   **APK Size:** Kiểm tra dung lượng file. Nếu quá lớn, hãy đề xuất tối ưu tài nguyên hình ảnh.

## Giai đoạn 4: Chữ ký & Phân phối

*   **Keystore:** Kiểm tra tính hợp lệ của Keystore (Chữ ký số).
*   **Distribution:** Đưa bản build lên Firebase App Distribution hoặc Play Console (Internal Testing).