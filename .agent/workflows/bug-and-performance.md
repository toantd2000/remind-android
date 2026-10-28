---
description: Quy trình điều tra sửa lỗi (Bug Fixing), xử lý Crash/ANR và tối ưu hóa hiệu năng (Compose UI Jank, Memory Leak).
---

# Workflow: Sửa lỗi & Tối ưu Hiệu năng (Bug & Performance Triage)

Quy trình này áp dụng khi xử lý Bug Report, Crash, ANR hoặc hiện tượng giật lag UI (Jank).

## Bước 1: Chẩn đoán & Phân loại (Diagnose)
Kích hoạt các skills chuyên sâu từ thư viện `android-skills` tùy theo triệu chứng:
- **Giật lag UI / Recomposition thừa trên Jetpack Compose:** Kích hoạt `jetpack-compose-performance`.
- **Lỗi ANR / Treo Main Thread / BroadcastReceiver quá hạn:** Kích hoạt `anr-audit-and-fix`.
- **Đo đạc CPU / Memory Leak:** Kích hoạt `android-profiler`.
- **Lỗi mất class/field trên bản Release do R8/ProGuard:** Kích hoạt `r8-analyzer`.
- **Lỗi Logic / Mapping dữ liệu:** Kích hoạt `remind-architecture`.

## Bước 2: Tái hiện bằng Failing Test (TDD Red -> Green)
1. Viết một Unit Test mô phỏng chính xác điều kiện gây lỗi (test phải **Fail** trước khi sửa code).
2. Sửa lỗi tại tầng thấp nhất có thể (ưu tiên `:core:domain` hoặc `:core:data` trước khi can thiệp lên UI).
3. Chạy lại Unit Test vừa viết và toàn bộ test suite của module liên quan để đảm bảo không gây Regression.

## Bước 3: Tự hiệu chỉnh (Self-Correction & Micro-Learning)
Nếu lỗi xuất phát từ việc hiểu sai quy ước dự án hoặc một bẫy kỹ thuật đặc thù:
1. Ghi **Micro-Learning** (tối đa 3-4 dòng: `Cause` & `Rule`) vào `LEARNING_JOURNAL.md`.
2. Nếu là quy tắc đặc thù của riêng dự án ReMind, bổ sung **1 dòng** vào `.agent/project-conventions.md`.
