---
description: Quy trình nâng cấp Target SDK, minSdk, Android Gradle Plugin (AGP 9+) và khám phá kiến trúc dự án (Project Discovery).
---

# Workflow: Bảo trì SDK, Build System & Khám phá Kiến trúc

Quy trình này áp dụng khi nâng cấp `compileSdk`/`targetSdk`, tăng `minSdk`, nâng cấp AGP/Kotlin hoặc lập bản đồ kiến trúc dự án.

## Kịch bản A: Nâng cấp AGP / Target SDK / Edge-to-Edge
*Skills kích hoạt từ `android-skills`: `agp-9-upgrade`, `edge-to-edge`, `android-permissions-security`, `play-policy-insights`*
1. Khảo sát `gradle/libs.versions.toml` và các `build.gradle.kts` hiện tại.
2. Nếu nâng cấp AGP 9+: Tuân thủ hướng dẫn từ skill `agp-9-upgrade`.
3. Nếu nâng cấp `targetSdk`:
   - Rà soát các thay đổi hành vi hệ điều hành (Foreground Service types, Exact Alarm permissions, Notification permissions, Edge-to-Edge enforcement).
   - Cập nhật `AndroidManifest.xml` và nhánh kiểm tra `Build.VERSION.SDK_INT`.
4. **STOP Protocol:** Nếu thay đổi cấu hình Gradle gây lỗi đồng bộ không rõ nguyên nhân, dừng lại ngay và báo cáo log lỗi cho người dùng xử lý.

## Kịch bản B: Nâng `minSdk` & Dọn dẹp Legacy Code
1. Cập nhật `minSdk` và tìm kiếm toàn bộ các nhánh `Build.VERSION.SDK_INT`, `@RequiresApi`, `@TargetApi` hoặc thư mục resource `values-vXX` nhỏ hơn hoặc bằng `minSdk` mới.
2. Loại bỏ các nhánh fallback cũ và báo cáo số lượng nhánh điều kiện đã dọn dẹp.

## Kịch bản C: Khám phá & Cập nhật Bản đồ Kiến trúc (`ARCHITECTURE_MAP.md`)
1. Quét `settings.gradle.kts`, `gradle/libs.versions.toml` và cây thư mục `:app`, `:core:*`, `:features:*`.
2. Cập nhật sơ đồ phụ thuộc module và tech stack thực tế vào `ARCHITECTURE_MAP.md`.
