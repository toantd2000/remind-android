# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này dùng để ghi vết (tracking) quá trình thực thi các tính năng, lịch sử thay đổi kiến trúc và các quyết định kỹ thuật quan trọng của Ứng dụng Báo Thức.

## Lộ Trình Tổng Thể
- [x] **Phase 1:** Hoàn thiện Tầng Dữ liệu (Room DB & Repository)
- [x] **Phase 2:** Cơ Chế Lập Lịch Báo Thức (AlarmManager & Receivers)
- [x] **Phase 3:** Giao diện Người dùng (CRUD Alarm List & Create)
- [x] **Phase 4:** Trải Nghiệm Màn Hình Chuông (Foreground Service & Full-Screen UI)

---

## Nhật Ký Chi Tiết

### [2026-04-07] - Khởi tạo Blueprint & Quyết định Kiến trúc
- **Quyết định 1:** Chốt sử dụng `AlarmManager` (kết hợp `setExactAndAllowWhileIdle`) làm core logic gọi giờ thay vì WorkManager, nhằm đảm bảo độ chính xác tới từng giây dẫu bị Doze mode.
- **Quyết định 2:** Dùng tạm nhạc chuông hệ thống trước để đẩy nhanh tiến độ logic.
- **Quyết định 3:** Tạm thời GỘP giao diện Màn Hình Rung Chuông (`RingingActivity`) và `AlarmService` vào chung một module là `:features:alarm`. Việc này tránh hiện tượng Over-engineering (code dư thừa / chia nhỏ quá đà) trong giai đoạn đầu, duy trì cấu trúc mạch lạc hơn. 
- **Quyết định 4:** (Phase 1) Tách `DataStore` (hàng đợi cấu hình Settings chung) khỏi `:core:database` sang một module chuyên biệt mới là `:core:datastore`. Điều này đảm bảo tính chia cắt rành mạch giữa CSDL cục bộ (Room) và Preferences (DataStore).

- **Quyết định 5:** (Phase 4 Refactor) Chốt phương án Tách Module `:core:alarms` chuyên biệt cho hạ tầng Back-end (Service, Receiver, Scheduler). Áp dụng **Dependency Inversion** thông qua interface `AlarmIntentProvider` để xóa bỏ hoàn toàn Implicit Intent (gây ra lỗi Background limit trên Android 8+). Đồng thời cấu hình `MainActivity` ở tầng App làm EntryPoint duy nhất điều phối Navigation Route dựa trên StateFlow báo thức (`AlarmRingManager`).

### [2026-04-08] - Hoàn thiện Premium UI (Phase 3) & Logic Hậu Kích Hoạt (Phase 4)
- **Quyết định 6:** (Phase 3 UI) Triển khai giao diện Minimalist cao cấp bằng cách phân nhóm các Card không viền bo góc lớn, đồng thời áp dụng Hybrid Localization (tách resource string core/feature).
- **Quyết định 7:** (Kiến trúc) Đưa logic tính toán thời gian kích hoạt kế tiếp (`getNextOccurrence`) vào trung tâm của Domain Model `Alarm`, đảm bảo UI, Scheduler và Danh sách dùng chung một nguồn sự thật duy nhất.
- **Quyết định 8:** (Phase 4 UI) Cập nhật màn hình Đổ chuông (`AlarmRingingScreen`) để tuân thủ hoàn toàn `MaterialTheme.colorScheme` thay vì thiết lập hardcode màu OLED, giúp tự động thích ứng Sáng/Tối. Lược bỏ nút Snooze khi chưa xây dựng tầng logic tương ứng.
- **Quyết định 9:** (Phase 4 Logic) Xử lý logic Post-trigger bên trong `AlarmService`: Khởi chạy Coroutine để đọc Database ngay khi chuông reo -> Tự động chuyển trạng thái Disabled (cho báo thức 1 lần) hoặc Reschedule (cho báo thức tuần hoàn).

... (Sẽ tiếp tục cập nhật vào đây sau mỗi Phase hoàn thành) ...
