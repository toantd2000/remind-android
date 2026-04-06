---
description: Nâng cấp minSDK và dọn dẹp các mã nguồn tương thích ngược (Legacy Code Cleanup).
---

# Mục tiêu
Khi dự án quyết định ngừng hỗ trợ các máy Android đời rất cũ (tăng `minSdk`), Agent sẽ đảm nhiệm việc đi lục lọi và **Xóa bỏ (Remove)** hàng loạt các đoạn code "chắp vá" (workarounds) và các nhánh kiểm tra `Build.VERSION.SDK_INT` đã trở nên thừa thãi. Việc này giúp code sạch hơn và giảm kích thước App.

# Các bước thực hiện

## Bước 1: Nâng cấp biến cấu hình
1. Sửa trực tiếp biến `minSdk` (hoặc `minSdkVersion`) trong file `build.gradle.kts` hoặc bảng `libs.versions.toml`.
2. Sync lại dự án và kiểm tra các thay đổi thư viện nếu có thư viện nào yêu cầu minSDK đồng bộ.

## Bước 2: Quét và "Dọn rác" Branching Logic (SDK_INT)
1. Sử dụng công cụ `grep_search` hoặc các bộ Linter để tìm kiếm các đoạn mã `Build.VERSION.SDK_INT`.
2. **Cắt tỉa code:** Nếu giá trị kiểm tra (ví dụ `>= 24`) hiện tại đã nhỏ hơn hoặc bằng `minSdk` mới:
   - Xóa bỏ khối lệnh `else` (fallback dành cho đời cũ).
   - Bỏ lệnh `if` và giữ lại duy nhất đoạn logic chuẩn của đời máy mới.

## Bước 3: Dọn dẹp Annotations
1. Tìm và gỡ bỏ tất cả các Annotation `@RequiresApi(api = XX)` hoặc `@TargetApi(XX)` trùng phẫu với mức `minSdk` mới.
2. Việc xóa các thẻ này giúp cấu trúc class gọn gàng, hết bị Linter gạch chân vàng cảnh báo.

## Bước 4: Hợp nhất (Merge) Resources và Thư viện
1. Kiểm tra lại thư mục `res/`. Nếu có thư mục như `values-v24` (mà `minSdk` giờ đã là 24), hãy merge nội dung của chúng xuống thư mục `values` gốc và xóa thư mục phân mảnh đi.
2. Mở `build.gradle`, xem có thư viện nào dạng `-compat` (như các lib polyfill, threedtenabp) không, nếu OS mới đã hỗ trợ Native thì xóa thư viện đó khỏi App để giảm dung lượng file APK/AAB.

## Bước 5: Báo cáo Dọn Rác (Cleanup Metrics)
1. Thống kê nhanh: "Việc tăng minSdk lên [Version] đã giúp xóa đi 5 khối check if-else cũ kỹ và giảm số dòng code đáng kể ở file [Tên file]".
2. Mời User tự chạy build một lượt để an tâm tuyệt đối.
