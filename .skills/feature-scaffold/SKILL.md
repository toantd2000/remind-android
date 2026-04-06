---
name: feature-scaffold
description: Kỹ năng chuyên biệt để Agent tự động khởi tạo một module Android mới (Feature Module) đúng cấu trúc Multi-module và Clean Architecture của dự án.
metadata:
  author: Android-Expert
  version: "1.0"
---

# HƯỚNG DẪN KHỞI TẠO FEATURE MODULE

Sử dụng kỹ năng này khi người dùng yêu cầu tạo một tính năng mới (New Feature) cần tách ra một module riêng biệt.

## 1. Quy tắc đặt tên (Naming Conventions)

*   **Tên Module:** Sử dụng chữ thường, phân cách bằng dấu gạch ngang (kebab-case). Ví dụ: `features:cart`, `features:product-detail`.
*   **Package Name:** Phải bắt đầu bằng `com.yourproject.feature.[name]` (Thay `[name]` bằng tên tính năng thực tế).
*   **Thư mục:** Module phải được đặt trong thư mục `features/` ở gốc dự án.

## 2. Cấu trúc thư mục chuẩn

Mọi module feature mới phải chứa các package sau để đảm bảo Clean Architecture:

*   **ui hoặc presentation:** Chứa các Composable Screen, ViewModel và UI State.
*   **domain:** Chứa các UseCase và Entity đặc thù cho tính năng (nếu không dùng chung từ core).
*   **data:** Chứa Repository Implementation và API Service (nếu có).

## 3. Cấu hình Gradle (build.gradle.kts)

Agent phải sử dụng mẫu tại `references/build-gradle-template.txt` để đảm bảo:

*   Sử dụng Version Catalog (`libs.versions.toml`) cho mọi thư viện.
*   Bật `buildFeatures.compose = true`.
*   Tự động include các module phụ thuộc bắt buộc: `:core` và `:domain`.

## 4. Quy trình thực hiện (Workflow)

Khi nhận lệnh "Tạo module feature [X]", Agent phải thực hiện:

1.  **Khởi tạo thư mục:** Tạo thư mục `features/[X]` và các sub-packages cần thiết.
2.  **Cấu hình Gradle:** Tạo file `build.gradle.kts` và cập nhật namespace.
3.  **Cấu hình Manifest:** Tạo file `src/main/AndroidManifest.xml` with package name đúng chuẩn.
4.  **Đăng ký hệ thống:** Thêm lệnh `include(":features:[X]")` vào file `settings.gradle.kts` ở gốc dự án.
5.  **Đăng ký vào App:** Thêm `implementation(project(":features:[X]"))` vào file `build.gradle.kts` của module `:app`.
6.  **Xác nhận:** Thông báo cho người dùng rằng module đã sẵn sàng và yêu cầu Sync Gradle.

## 5. Ràng buộc (Constraints)

*   **KHÔNG** được phép để module feature này phụ thuộc trực tiếp vào một module feature khác.
*   **LUÔN** luôn sử dụng `implementation` thay vì `api` cho các thư viện bên thứ ba trừ khi có chỉ định khác.