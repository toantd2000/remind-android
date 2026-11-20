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

### [TDR-099] - Chuyển Dịch Sang Mô Hình Brand Resource Kit & App-Owned Material 3 Theme
- **Date:** 2026-11-20 | **Status:** Accepted
- **Context:** Việc bọc lại các component Material 3 (`Litever*` / `Lv*`) và ủy quyền hàm `LiteverTheme` cho thư viện ngoài gây nợ kỹ thuật lớn (507 dòng `LiteverLegacyCompat.kt`, ~302 điểm gọi) và khóa chặt phiên bản Compose/M3 giữa các dự án.
- **Decision:** Chuyển sang mô hình 3 tầng độc lập: (1) Thư viện chung chỉ giữ Brand Assets (Logo, Mascot), Bundled Fonts và Raw Tokens; (2) `:core:designsystem` trực tiếp làm chủ `ReMindTheme` (`MaterialTheme` + `CompositionLocalProvider`) và composite UI nghiệp vụ của ReMind; (3) `:features:*` gọi thẳng `androidx.compose.material3.*` kết hợp Tokens và đánh dấu `@Deprecated` toàn bộ `LiteverLegacyCompat.kt` để di cư dần (Strangler Fig).
- **Consequences:** Cắt đứt hoàn toàn sự phụ thuộc phiên bản M3 với thư viện gốc, sẵn sàng nâng cấp từng phần lên M3 Expressive mà không làm vỡ code hiện tại.

### [TDR-098] - Chuyển Typography Sang Bundled Fonts Trong LiteVer Design System
- **Date:** 2026-11-20 | **Status:** Accepted
- **Context:** `GoogleFont.Provider` tải bất đồng bộ qua GMS IPC gây hiện tượng giật chữ/nhảy layout (FOUT) khi vào app hoặc chuyển màn hình trên máy yếu và phụ thuộc mạng lần đầu.
- **Decision:** Nhúng đủ 18 file static `.ttf` (`Be Vietnam Pro`) và 2 file variable `.ttf` (`Nunito`) kèm giấy phép OFL-1.1 vào `litever-designsystem`, thay thế `GoogleFont` trong `Type.kt` bằng `Font(R.font.*)`.
- **Consequences:** Render font chuẩn ngay từ frame đầu tiên (0 ms), hoạt động 100% offline, tăng ~1.5 MB dung lượng nén.

### [TDR-097] - Tối Giản Hoá Kiến Trúc Cốt Lõi: Loại Bỏ Thời Tiết, Quote & Lớp Mạng
- **Date:** 2026-11-18 | **Status:** Accepted
- **Context:** Các tính năng Thời tiết, Quote hằng ngày, màn hình đệm `AlarmMessageScreen` và các loại Mission chưa hoàn thiện làm phình to kiến trúc và đi ngược triết lý ứng dụng Báo thức & Nhắc nhở tối giản.
- **Decision:** Xoá bỏ toàn bộ `:features:today`, `:core:network` (Retrofit/OkHttp/Coil), `WeatherRepository`, `TodayRepository`, `AlarmMessageScreen` và các `MissionType` "Sắp ra mắt"; giữ `@Deprecated` trên key Remote Config cũ trong `AdPlacement` để bảo toàn deserialization.
- **Consequences:** Thu gọn hệ thống từ 19 xuống 17 modules, thanh điều hướng còn 3 tab cốt lõi (Alarms, Reminders, Settings) và đưa nghiệp vụ chính về 100% Offline-first.

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
