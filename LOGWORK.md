# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này dùng để ghi vết (tracking) quá trình thực thi các tính năng, lịch sử thay đổi kiến trúc và các quyết định kỹ thuật quan trọng của Ứng dụng Báo Thức.

## Lộ Trình Tổng Thể
- [x] **Phase 1:** Hoàn thiện Tầng Dữ liệu (Room DB & Repository)
- [x] **Phase 2:** Cơ Chế Lập Lịch Báo Thức (AlarmManager & Receivers)
- [ ] **Phase 3:** Giao diện Người dùng (CRUD Alarm List & Create)
- [ ] **Phase 4:** Trải Nghiệm Màn Hình Chuông (Foreground Service & Full-Screen UI)

---

## Nhật Ký Chi Tiết

### [2026-04-07] - Khởi tạo Blueprint & Quyết định Kiến trúc
- **Quyết định 1:** Chốt sử dụng `AlarmManager` (kết hợp `setExactAndAllowWhileIdle`) làm core logic gọi giờ thay vì WorkManager, nhằm đảm bảo độ chính xác tới từng giây dẫu bị Doze mode.
- **Quyết định 2:** Dùng tạm nhạc chuông hệ thống trước để đẩy nhanh tiến độ logic.
- **Quyết định 3:** Tạm thời GỘP giao diện Màn Hình Rung Chuông (`RingingActivity`) và `AlarmService` vào chung một module là `:features:alarm`. Việc này tránh hiện tượng Over-engineering (code dư thừa / chia nhỏ quá đà) trong giai đoạn đầu, duy trì cấu trúc mạch lạc hơn. 
- **Quyết định 4:** (Phase 1) Tách `DataStore` (hàng đợi cấu hình Settings chung) khỏi `:core:database` sang một module chuyên biệt mới là `:core:datastore`. Điều này đảm bảo tính chia cắt rành mạch giữa CSDL cục bộ (Room) và Preferences (DataStore).

... (Sẽ tiếp tục cập nhật vào đây sau mỗi Phase hoàn thành) ...
