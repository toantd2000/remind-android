# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này ghi lại lộ trình phát triển và các Quyết Định Kiến Trúc Cốt Lõi (TDR) của ReMind.
*Lưu ý: Chỉ ghi nhận các quyết định kiến trúc lớn (thay đổi module, database schema, library cốt lõi). Không ghi chép bugfix nhỏ hoặc thay đổi UI thông thường. Các TDR cũ được lưu trữ tại `docs/archive/LOGWORK_ARCHIVE.md`.*

---

## 📍 Lộ Trình Phát Triển (Roadmap)
- [x] **Phase 1-6:** Hoàn thiện Tầng Dữ liệu, Cơ chế AlarmManager, UI CRUD, Chuông báo, Tách Design System & Bảo trì.
- [x] **Phase 7:** Nâng cấp LiteVer Design System 2.0.0 & Refactor toàn diện Material 3 (`includeBuild` & M3 Defaults).
- [x] **Phase 8:** Nâng cấp LiteVer Design System 2.1.0 & Chuyển đổi sang bộ thành phần Opinionated `Lv*`.
- [x] **Phase 9:** Tái cấu trúc CSDL Nhắc nhở & Hoàn thiện mô hình FIXED_TIME / INTERVAL (`MIGRATION_4_5`).
- [x] **Phase 10:** Loại bỏ hoàn toàn Checklist trong Nhắc nhở - Tối giản trải nghiệm & Kiến trúc (`MIGRATION_6_7`).
- [x] **Phase 11:** Tái cấu trúc vòng đời Nhắc nhở (Loại bỏ `isCompleted`, thống nhất quản lý qua `isEnabled`, `MIGRATION_7_8`).
- [x] **Phase 12:** Tính năng Báo thức nhanh (Quick Alarm) với FAB Menu & Bottom Sheet, Room `MIGRATION_8_9`.
- [ ] **Phase 13:** Tối ưu hóa hiệu năng & Kiểm toán bảo mật hệ thống toàn diện theo chuẩn Google `android-skills`.

---

## 🧠 Quyết Định Kiến Trúc Gần Nhất (Active Micro-TDRs)

### [TDR-096] - Quy Hoạch Hệ Sinh Thái Agent Skills & Chuẩn Hóa Tài Liệu
- **Date:** 2026-10-28 | **Status:** Accepted
- **Context:** Các file skills và tài liệu bị phân mảnh, trùng lặp nhiều nơi ("reinventing the wheel") gây loãng ngữ cảnh AI.
- **Decision:** Tích hợp trực tiếp kho Google `android-skills` tại `Dev\Github\skills\android-skills`, tinh gọn còn 3 skills dự án (`remind-architecture`, `litever-design-system`, `stitch-design-sync`), chắt lọc tri thức vào `.agent/project-conventions.md` và lưu trữ tài liệu lịch sử vào `docs/archive/`.
- **Consequences:** Giảm hơn 80% dung lượng văn bản dư thừa, tăng độ chính xác của Agent và đồng bộ 100% với chuẩn `agentskills.io`.

### [TDR-095] - Đồng Bộ Dialog Chọn Ngày Giờ Giữa Báo Thức Và Nhắc Nhở
- **Date:** 2026-10-07 | **Status:** Accepted
- **Context:** Các dialog chọn ngày/giờ trong `:features:reminders` dùng wrapper cũ thiếu tiêu đề và không đồng nhất với Báo thức.
- **Decision:** Chuyển sang Material 3 `DatePickerDialog` và `TimePickerDialog` gốc kèm `titleLarge` in đậm và nút bấm `LvButton`.
- **Consequences:** Trải nghiệm chọn thời gian trên toàn bộ ứng dụng đạt tính nhất quán 100%.

### [TDR-094] - Đưa SnoozeBottomSheet Vào :core:designsystem Dùng Chung
- **Date:** 2026-10-06 | **Status:** Accepted
- **Context:** Cấu hình báo lại trong Nhắc nhở dùng control riêng, không tận dụng được Bottom Sheet của Báo thức.
- **Decision:** Chuyển `SnoozeBottomSheet` sang `:core:designsystem` với tham số `repeatCount` tùy chọn để tái sử dụng cho cả hai tính năng.
- **Consequences:** Loại bỏ mã nguồn trùng lặp, đảm bảo nguyên tắc Single Source of Truth cho các thành phần UI dùng chung.

### [TDR-093] - Triển Khai Tính Năng Báo Thức Nhanh (Quick Alarm)
- **Date:** 2026-10-06 | **Status:** Accepted
- **Context:** Người dùng cần đặt nhanh báo thức ngắn hạn (5-30 phút) mà không muốn làm bừa bộn danh sách chính.
- **Decision:** Thêm `isQuickAlarm` vào `AlarmEntity` (`MIGRATION_8_9`), tự động xóa vĩnh viễn khỏi CSDL khi tắt Switch hoặc khi chuông reo xong; tạo UI FAB menu và Bottom Sheet thiết lập nhanh.
- **Consequences:** Đáp ứng trải nghiệm hẹn giờ tức thời mà không làm ảnh hưởng đến cấu trúc vòng đời của báo thức thông thường.

### [TDR-091] - Tách Lịch Lặp Ngày Ra Cấp Độ Toàn Thể Nhắc Nhở
- **Date:** 2026-10-05 | **Status:** Accepted
- **Context:** Cấu hình lặp ngày (`ReminderRepeatConfigSection`) bị nhúng trùng lặp sâu bên trong cả hai nhánh Fixed Time và Interval.
- **Decision:** Đưa `ReminderRepeatConfigSection` ra làm Section độc lập cấp cao nhất trong `ReminderEditScreen`, loại bỏ props drilling.
- **Consequences:** Phân tách rõ ràng giữa chu kỳ ngày (Calendar Recurrence) và cách kích hoạt trong ngày (Within-day Triggers).
