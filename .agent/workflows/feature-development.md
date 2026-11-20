---
description: Quy trình chuẩn phát triển tính năng mới, nâng cấp tính năng hiện có, tích hợp API và mở rộng UI/Design System.
---

# Workflow: Phát triển & Nâng cấp Tính năng (Feature Development)

Quy trình này hợp nhất việc xây dựng tính năng mới, cập nhật tính năng hiện có, đồng bộ API và mở rộng giao diện.

## Bước 1: Phân tích Tác động & Lập Kế hoạch (Impact & Plan)
1. Kiểm tra cấu trúc module hiện tại trong `settings.gradle.kts`.
2. Xác định rõ các tầng bị ảnh hưởng (`:core:model`, `:core:domain`, `:core:data`, `:core:database`, `:core:alarm`, `:core:designsystem`, `:features:*`).
3. Trình bày **Execution Plan** ngắn gọn (danh sách file dự kiến tạo/sửa) trước khi viết code.

## Bước 2: Triển khai Domain & Data Layer
*Skill kích hoạt tự động: `remind-architecture`*
1. **Domain (`:core:model`, `:core:domain`):** Định nghĩa Model thuần Kotlin, Repository Interface và UseCase đơn nhiệm.
2. **Data (`:core:data`, `:core:database`, `:core:network`):**
   - Triển khai `RepositoryImpl` và Mapper (`toDomain()`, `toEntity()`).
   - Bắt buộc catch Exception của Framework tại `RepositoryImpl` và ánh xạ sang Domain Exception trước khi bọc vào `Result<T>`.
   - Nếu thay đổi bảng Room trong `:core:database`: **Bắt buộc** tăng version và viết `Migration(old, new)` tường minh.
3. **DI (Hilt):** Cung cấp binding/provider trong module DI tương ứng và inject `CoroutineDispatcher` qua constructor.

## Bước 3: Xây dựng Giao diện & Kiểm thử Trực quan (UI & Preview)
*Skills kích hoạt tự động: `litever-design-system`, `compose-preview`, `edge-to-edge`, `adaptive` (từ `android-skills`), `stitch-design-sync` (nếu đồng bộ Stitch)*
1. Sử dụng trực tiếp các component chuẩn của `androidx.compose.material3.*` kết hợp chặt chẽ với Design Tokens (`LiteverTheme.colors` / `MaterialTheme.colorScheme`, `LiteverTheme.spacing`, `LiteverTheme.shapes`, `LiteverTheme.typography`) và Brand Assets. Chỉ tạo composite component trong `:core:designsystem` khi mang nghiệp vụ đặc thù dùng chung của ReMind; không tạo hoặc dùng các wrapper `@Deprecated` trong `LiteverLegacyCompat.kt`.
2. Viết `@Preview` (Light & Dark mode, các trạng thái Loading/Success/Empty) cho mọi Screen/Component.
3. Sử dụng công cụ **`compose-preview`** để render và kiểm tra trực quan giao diện ngay sau khi sửa.

## Bước 4: Kiểm thử Tự động & Đồng bộ Tài liệu (Verify & Docs)
*Skill hỗ trợ: `testing-setup` (từ `android-skills`)*
1. Viết Unit Test (JUnit + MockK + Turbine) cho UseCase và Repository vừa thêm/sửa (tối thiểu 2 nhánh `Success` và `Failure`).
2. Chạy lệnh `./gradlew :<module>:testDebugUnitTest` để xác nhận pass 100%.
3. **Cập nhật tài liệu (Chỉ khi có thay đổi tương ứng - Chống rác tài liệu):**
   - `ARCHITECTURE_MAP.md`: Chỉ khi thêm/xóa module trong `settings.gradle.kts`.
   - `LOGWORK.md`: Chỉ ghi Micro-TDR (tối đa 5 dòng) khi có quyết định kiến trúc lớn hoặc thay đổi thư viện cốt lõi.
   - `DECISION_LOG.md` & `CHANGELOG.md`: Khi chốt kịch bản hành vi mới hoặc hoàn tất tính năng hướng người dùng.
