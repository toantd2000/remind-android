---
description: Quy trình kiểm duyệt mã nguồn (Code Review), kiểm toán bảo mật (Intent/Permissions), tối ưu R8 và xử lý nợ kỹ thuật (Tech-debt Refactoring).
---

# Workflow: Kiểm duyệt Mã nguồn & Kiểm toán Hệ thống (Code Review & Audit)

Trong quy trình này, Agent đóng vai trò **Senior Android Tech Lead** rà soát toàn diện kiến trúc, hiệu năng và bảo mật.

## Bước 1: Kiểm tra Kiến trúc & Design System (Project Rules)
*Skills kích hoạt: `remind-architecture`, `litever-design-system`*
1. **Dependency Rule:** Kiểm tra xem `:core:domain` / `:core:model` có import nhầm Android SDK/Room/Retrofit không; `:features:*` có phụ thuộc chéo hoặc gọi thẳng xuống `:core:data` không.
2. **Design System & Token Compliance:** Kiểm tra xem `:features:*` có hardcode mã màu HEX, hardcode kích thước `dp`/`sp` tự do, tự tạo pass-through wrapper thừa thãi, hoặc gọi các hàm `@Deprecated` trong `LiteverLegacyCompat.kt` thay vì dùng trực tiếp Material 3 gốc + `LiteverTheme` tokens không.
3. **Database Safety:** Kiểm tra mọi thay đổi schema trong `:core:database` đã có `Migration(old, new)` đầy đủ chưa.

## Bước 2: Kiểm toán Bảo mật, Hiệu năng & R8 (Google Official Standards)
*Skills kích hoạt từ `android-skills`:*
- **`android-intent-security` & `android-permissions-security`:** Rà soát `AndroidManifest.xml`, `BroadcastReceiver`, `Service`, `PendingIntent` (đặc biệt trong `:core:alarm`) để chống lỗ hổng Intent Redirection, thiếu cờ `FLAG_IMMUTABLE` hoặc lộ `android:exported`.
- **`jetpack-compose-performance`:** Kiểm tra tính ổn định (`@Stable`/`@Immutable`), `remember`, `derivedStateOf`, và `key` trong `LazyColumn`.
- **`r8-analyzer`:** Kiểm tra các file `proguard-rules.pro` và `consumer-rules.pro` để loại bỏ các keep rules quá rộng hoặc dư thừa.

## Bước 3: Báo cáo Review & Thực thi Refactor
Trình bày kết quả theo 3 phần:
1. **[Điểm sáng]:** Các điểm tuân thủ tốt kiến trúc.
2. **[Vi phạm / Rủi ro]:** Chỉ rõ file, dòng code và lý do vi phạm.
3. **[Đề xuất sửa đổi]:** Cung cấp đoạn code tối ưu (chỉ áp dụng chỉnh sửa trực tiếp khi người dùng yêu cầu).
