---
description: Quy trình khởi tạo kiến trúc nền tảng cho một dự án Android mới, bao gồm thiết lập Version Catalog và xây dựng các Core modules (như designsystem, network, common).
---

# Workflow: Khởi tạo Dự án Mới (Project Initialization)

Quy trình này áp dụng khi bắt đầu một dự án Android hoàn toàn mới (hoặc tái cấu trúc toàn diện), nhằm thiết lập nền móng vững chắc với kiến trúc Multi-module, Design System và các thành phần cốt lõi dùng chung (Core Components).

## Giai đoạn 1: Thiết lập Hệ thống Quản lý (Foundation)
1. **Version Catalog:** Tạo/Cập nhật file `gradle/libs.versions.toml` để quản lý phiên bản thư viện tập trung (Compose, Hilt, Coroutines, Room, Retrofit, v.v.).
2. **Dependency Management:** Đảm bảo `settings.gradle.kts` đã đăng ký đầy đủ các thư mục theo chuẩn (ví dụ: `include(":core:designsystem")`).

## Giai đoạn 2: Xây dựng Core Modules (Core Framework)
Khởi tạo các module thuộc nhóm `:core:` đóng vai trò cung cấp tài nguyên, tiện ích, và nền tảng cho toàn bộ các module tính năng (`:features:`):

1. **Module `:core:designsystem`:**
   * Định nghĩa toàn bộ hệ thống màu sắc, Typography, Shapes theo chuẩn Design System của dự án.
   * Cung cấp các Composable Widgets dùng chung xuyên suốt dự án (VD: `AppButton`, `AppTopBar`, `LoadingScreen`).
   * *Kích hoạt Skill: `@compose-ui-system`* để đảm bảo tuân thủ nguyên tắc thiết kế.

2. **Module `:core:network`:**
   * Cấu hình HTTP Client (Retrofit/Ktor), OkHttp Interceptors (Log, Auth).
   * Quản lý trạng thái kết nối và các lớp bọc (Wrapper) bắt lỗi API dùng chung.

3. **Module `:core:common` (hoặc `:core:utils`):**
   * Các Base class (`BaseViewModel`), Result wrappers (`Result<T>`), Regex helpers, Dispatcher Providers, và hệ thống Log.

4. **Module `:core:testing` (Khuyên dùng):**
   * Chứa các tiện ích dùng cho Test (Test Rules, Fake Data Builders, Mock Dispatchers).

## Giai đoạn 3: Domain & Data cốt lõi
1. **Module `:core:domain` (hoặc `:core:model`):**
   * Chứa các Data Classes/Entities, Repository Interfaces và UseCases được tái sử dụng ở nhiều feature (ví dụ: `User`, `AppConfig`, `ErrorModel`). Không chứa logic framework ở đây.
2. **Module `:core:data`:**
   * Triển khai các Repository dùng chung.
   * Cấu hình cơ sở dữ liệu cục bộ (Room Database).
   * Thiết lập hệ thống lưu trữ Key-Value (DataStore/SharedPreferences) cho Token, Theme, Ngôn ngữ.
   * *Kích hoạt Skill: `@security-hardening`* yêu cầu sử dụng `EncryptedSharedPreferences` cho Token/Mật khẩu.

## Giai đoạn 4: Thiết lập Ứng dụng gốc (:app)
*Kích hoạt Skill: `@hilt-di-config`*
1. Tạo class kế thừa `Application` và thêm annotaion `@HiltAndroidApp`.
2. Thiết lập Entry Point hiển thị (trong `MainActivity.kt`).
3. Khởi tạo cây Điều hướng gốc (Root Navigation Graph) để định tuyến giữa các Module Features sau này.

## Giai đoạn 5: Công cụ và Chất lượng Code (Code Quality)
1. Tùy chọn thiết lập hệ thống kiểm tra mã nguồn (Linter) như **Detekt** hoặc **Ktlint**.
2. Đảm bảo file `.gitignore` đã loại bỏ các file rác đúng chuẩn Android.

## Giai đoạn 6: Báo cáo & Bàn giao
1. Agent in ra **Cấu trúc cây thư mục (Project Tree)** đại diện cho toàn bộ các Core modules vừa tạo.
2. Yêu cầu người dùng (Tech Lead) chạy Sync Gradle và Review lại toàn bộ kiến trúc nền tảng trước khi bắt tay vào Feature số 1.
