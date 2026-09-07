---
name: module-manager
description: Hướng dẫn Agent quản lý cấu trúc multi-module, thêm dependency và cấu hình file build.gradle.kts.
metadata:
  author: Android-Expert
  version: "1.0"
---

# QUY TẮC QUẢN LÝ MODULE

### 1. Cấu trúc Module chuẩn
- `:app`: Chỉ chứa Application class và cấu hình entry point. Phụ thuộc vào tất cả feature modules.
- `:features:[name]`: Chứa UI (Compose) và ViewModel của một tính năng. Phụ thuộc vào `:core:domain` và `:core:*`.
- `:core:domain`: Chứa Entities và UseCases. **KHÔNG** phụ thuộc vào bất kỳ module nào khác.
- `:core:data`: Chứa Repository Implementation, API service, Database. Phụ thuộc vào `:core:domain`.
- `:core:*`: Chứa các tiện ích, tài nguyên dùng chung (common, designsystem, network).

**Quy tắc đặt tên Package (Namespace & Package Name):**
Phải tuân thủ nghiêm ngặt công thức ghép đường dẫn module vào package name: `{basepackagename}.{path_to_module}` (bỏ dấu `:` ở đầu, thay dấu `:` giữa các cấp thành dấu chấm `.`).
- Thay `{basepackagename}` bằng package gốc của dự án (ví dụ `com.example.myapp`).
- Các ví dụ cụ thể mổ phỏng quy tắc:
  - Module `:app` ➡️ Package: `{basepackagename}.app`
  - Module `:core:data` ➡️ Package: `{basepackagename}.core.data`
  - Module `:core:domain` ➡️ Package: `{basepackagename}.core.domain`
  - Module `:features:home` ➡️ Package: `{basepackagename}.features.home`

Quy định này áp dụng bắt buộc cho cả việc khai báo thuộc tính `namespace` trong `build.gradle.kts` lẫn việc tạo nhánh cấu trúc thư mục code vật lý `src/main/java/...`.

### 2. Quản lý Dependency & Thể loại Module
- **Version Catalog:** Luôn sử dụng `libs.versions.toml` để khai báo thư viện. Không hardcode version. Tuyệt đối KHÔNG tự động cập nhật hoặc sửa đổi phiên bản của các thư viện ĐÃ CÓ SẴN (để tránh phá vỡ tương thích). Chỉ được khai báo thêm thư viện mới khi thực sự cần.
- **Xử lý sự cố đồng bộ (Sync Failure Rules):** Trí tuệ của AI để phục vụ code Logic, tuyệt đối vứt bỏ quyền tự quyết về file config dự án. Nếu khi bạn thêm module gây ra LỖI ĐỒNG BỘ Gradle, BẠN CẤM ĐƯỢC TỰ SỬA các Gradle files, đặc biệt là `build.gradle.kts` của `:app`! Thông báo lỗi để Người Dùng (Tech Lead) ra tay xử lý bằng tay.
- **Chống phụ thuộc vòng tròn (No Circular Dependencies):** Sự phụ thuộc giữa các module phải tuân thủ luồng đơn chiều (đúng chuẩn Clean Architecture). Tuyệt đối **CẤM** tạo ra liên kết vòng tròn (Ví dụ: Module A thêm `implementation` Module B, sau đó Module B lại thêm `implementation` Module A). Nếu 2 module bị phụ thuộc chéo lên nhau, BẠN KHÔNG ĐƯỢC THÊM QUYỀN DEPENDENCY CHO CÚ PHÁP ĐÓ. Thay vào đó, phương án xử lý là phải đề xuất trích xuất phần code dùng chung ra một module `:core` thứ 3.
- **Phân loại Module (Rất quan trọng):**
  - **Pure Kotlin Modules** (VD: `:core:domain`, `:core:model`): Logic thuần túy. Tuyệt đối KHÔNG có block `android { }`, KHÔNG dùng plugin `com.android.library`. Chỉ dùng plugin `kotlin("jvm")` hoặc `java-library`. Cấm chứa dependency của Android SDK (`androidx.*`) hoặc Compose.
  - **Android Modules** (VD: `:features:*`, `:core:designsystem`): Cần giao diện hoặc Android SDK. Dùng plugin `com.android.library` và có block `android { }`.
- **Dependency Scope:** Dùng `implementation` cho nội bộ, `api` để public thư viện.

### 3. Workflow tạo Module mới cho Agent
Khi được yêu cầu tạo module mới:
1. **Tham chiếu (Clone Config):** KHÔNG bao giờ tự bịa ra file `build.gradle.kts`. PHẢI đọc file `build.gradle.kts` của `:app` hoặc một module có sẵn trước để lấy chuẩn (như `compileSdk`, `minSdk`, `jvmTarget`, `plugin aliases`).
2. Tạo thư mục module và định nghĩa file `build.gradle.kts`. Dán config chuẩn vừa copy sang, chỉ thay đổi `namespace`. **BẮT BUỘC:** Phải tạo kèm theo một file `.gitignore` nằm ngay dưới thư mục gốc của module này với nội dung `/build`.
3. Chỉ thêm các Thư viện (Dependencies) **tối giản nhất** để phục vụ đúng vai trò của module.
4. Đăng ký vào `settings.gradle.kts` và khai báo dependency tại `build.gradle.kts` của lớp cha.
5. **Cập nhật "Bản đồ sống" (Living Map):** Bất cứ khi nào tạo module mới hoặc thay đổi cấu trúc trong `settings.gradle.kts`, BẮT BUỘC phải mở file `ARCHITECTURE_MAP.md` ở thư mục gốc và cập nhật lại sơ đồ kiến trúc để tránh sự hao mòn tài liệu (Documentation Decay).