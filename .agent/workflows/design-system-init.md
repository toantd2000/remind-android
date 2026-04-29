---
description: Quy trình trích xuất hoặc khởi tạo Design System bằng cách tái sử dụng thư viện mẫu :litever-designsystem. Đây là quy trình con (sub-workflow) của Project Initialization.
---

# Workflow: Khởi tạo/Trích xuất Design System (Litever Pattern)

## Mục tiêu
- Tiết kiệm token và thời gian bằng cách không viết lại từ đầu.
- Đảm bảo tính nhất quán giữa các dự án của Litever.
- Tự động hóa việc đổi package và prefix.

## Các bước thực hiện

### 1. Chuẩn bị (Scoping)
- Xác định tên module mới (ví dụ: `:shared:designsystem` hoặc `:newapp-designsystem`).
- Xác định prefix cho các component (mặc định là `Litever`).
- Xác định package name mới.

### 2. Nhân bản cấu trúc (Cloning)
- Copy toàn bộ nội dung từ thư mục `litever-designsystem` sang thư mục module mới.
- **Lưu ý:** Không copy thư mục `build/` và các file không cần thiết của IDE.

### 3. Hiệu chỉnh cấu hình Build (Gradle)
- Cập nhật `settings.gradle.kts` để bao gồm module mới.
- Cập nhật `build.gradle.kts` của module mới (namespace, các dependency cần thiết).

### 4. Refactor Package & Naming
- Sử dụng lệnh `replace` hàng loạt để đổi package name trong toàn bộ module mới.
- Đổi tên các file và class nếu có yêu cầu prefix khác (ví dụ: `LiteverButton` -> `MyButton`).
- Cập nhật lại các file Resource (strings.xml, font_certs.xml).

### 5. Thiết lập Adapter/Wrapper (Tại App Module)
- Nếu dự án đã có sẵn code UI cũ, hãy giữ lại module `:core:designsystem` cũ.
- Biến các component cũ thành Wrapper trỏ đến module Design System mới (như cách làm với `ReMindButton` trỏ đến `LiteverButton`).
- Cập nhật `Theme.kt` của App để kế thừa từ `LiteverTheme`.

### 7. Cập nhật Tài liệu (Documentation Sync)
- **BẮT BUỘC:** Cập nhật [LOGWORK.md](file:///d%3A/Dev/Github/toantd2000/remind-android/LOGWORK.md) với một TDR mới về việc khởi tạo Design System.
- **BẮT BUỘC:** Cập nhật [ARCHITECTURE_MAP.md](file:///d%3A/Dev/Github/toantd2000/remind-android/ARCHITECTURE_MAP.md) để phản ánh cấu trúc module mới.
- **LƯU Ý:** Ghi lại các khó khăn vào [LEARNING_JOURNAL.md](file:///d%3A/Dev/Github/toantd2000/remind-android/LEARNING_JOURNAL.md).

## Quy tắc quan trọng
- **KHÔNG** viết lại logic tokens (Color, Type, Shape) nếu không có yêu cầu đặc thù về thiết kế. Chỉ cần truyền `LiteverColors` mới vào `LiteverTheme`.
- **ƯU TIÊN** dùng lệnh `sed` hoặc công cụ replace của IDE để đổi package thay vì sửa từng file thủ công.
