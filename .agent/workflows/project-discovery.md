---
description: Khám phá và lập sơ đồ kiến trúc cho dự án đang tồn tại (Onboarding & Project Discovery).
---

# Mục tiêu
Agent cần khả năng tự định vị mình trong một dự án Android không phải do nó tạo ra. Quy trình này mô phỏng hai tuần đầu tiên của một DEV mới: đọc code, tìm hiểu file config, xác định luồng hoạt động chính, và vẽ lại sơ đồ kiến trúc app.

# Các bước thực hiện

## Bước 1: Quét hệ thống Gradle & Configs
1. Cẩn thận rà soát các file `build.gradle`, `build.gradle.kts`, bảng version `libs.versions.toml`.
2. Trả lời các câu hỏi:
   - Các SDK version, Kotlin version đang thiết lập là bao nhiêu?
   - Các core libraries quyết định sống còn (Jetpack Compose hay XML? Hilt/Koin hay thuần Dagger? Retrofit hay Ktor?).
   - Cơ chế xây dựng đa module (Multi-module) hiện đang phân nhánh thế nào (VD: `app`, `core:database`, `feature:auth`...).

## Bước 2: Quét cấu trúc tầng Kiến trúc (App Architecture)
1. Mò vào module `app` tìm class kế thừa `Application` để xem các dependency được khởi tạo ban đầu.
2. Quét tầng Navigation (tìm file `NavGraph`, `NavHost` trên Compose hay cấu hình XML) nhằm hiểu màn hình xuất phát nằm ở đâu.
3. Nhận dạng mẫu Architecture đang thống trị (MVVM, MVI, State Flow based) qua cách package được group (theo tính năng hay theo tầng).

## Bước 3: Theo dấu DI (Dependency Injection) và Data flow
- Lần theo các Interface/Repository đùn đẩy dữ liệu từ Network Module về tới View. Việc này giúp nắm bắt toàn bộ luồng truyền thông lượng.

## Bước 4: Đánh giá tổng quan (Glance Report) & Xuất tài liệu
1. Tổng hợp mọi hiểu biết ở trên thành một file `ARCHITECTURE_MAP.md` lưu tại root dự án.
2. File này chính là "Tấm bản đồ" chứa:
   - Tech Stack hiện tại.
   - Sơ đồ Kiến trúc/Navigation cơ bản.
   - Top 3 đánh giá/rủi ro tiềm ẩn (Technical Debt) phát hiện lúc dò code.
