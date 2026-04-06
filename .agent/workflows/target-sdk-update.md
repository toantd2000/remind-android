---
description: Quy trình nâng cấp Target SDK và xử lý các Breaking Changes theo chính sách bắt buộc của Google Play.
---

# Mục tiêu
Vào tháng 8 mỗi năm, Google Play yêu cầu toàn bộ App phải cập nhật lên API Level (Target SDK) mới nhất. Quy trình này cung cấp kịch bản an toàn để nâng cấp mã nguồn, xử lý các thay đổi về quyền (Permissions), và thích ứng với Android behavior changes.

# Các bước thực hiện

## Bước 1: Khảo sát phiên bản & Phân tích tác động
1. Đọc file config (như `build.gradle.kts` hoặc `libs.versions.toml`) để tìm giá trị `compileSdk` và `targetSdk` hiện tại.
2. Tìm kiếm các tài liệu, kịch bản (Behavior Changes) giữa SDK cũ và SDK mới.
   *(Ví dụ điển hình: Android 13 (33) yêu cầu quyền `POST_NOTIFICATIONS`; Android 14 (34) yêu cầu khai báo rõ `foregroundServiceType`...)*

## Bước 2: Nâng cấp Gradle & Môi trường (Pre-requisites)
1. Thường thì SDK mới bắt buộc phải đi kèm với phiên bản AGP (Android Gradle Plugin) và Kotlin mới.
2. Nâng cấp bộ 3 hệ sinh thái lõi này trước, Sync Gradle rồi xử lý Compile Error nếu có.

## Bước 3: Thay đổi Version & Giới hạn Manifest
1. Sửa trực tiếp `compileSdk` và `targetSdk` lên phiên bản đích.
2. Soi xét lại file `AndroidManifest.xml`:
   - Bổ sung các cú pháp `<uses-permission>` mới bị Google ép.
   - Thêm các Flag khai báo Intent Filters như `android:exported="true"`.
   - Bổ sung `foregroundServiceType` đối với các Service chạy ngầm.

## Bước 4: Refactor Code tương thích (Compatibility Fix)
1. Sử dụng kỹ thuật rẽ nhánh hệ điều hành để giữ app không sập trên các máy cũ:
   ```kotlin
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
       // Code gọi hàm với SDK mới
   } else {
       // Code fallback cho SDK cũ
   }
   ```
2. Rà soát các hàm bị Gạch bỏ (`@Deprecated`) hoặc thay đổi do Privacy của Android (như truy cập ảnh bừa bãi bị chặn, đổi qua PhotoPicker).

## Bước 5: Viết báo cáo Verification
1. Lập danh sách các tính năng User cần test bằng máy thực (vd: "Phải test luồng xin quyền Notification trên máy ảo Android 14").
