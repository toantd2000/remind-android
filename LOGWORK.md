# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này dùng để ghi vết (tracking) quá trình thực thi các tính năng, lịch sử thay đổi kiến trúc và các quyết định kỹ thuật quan trọng của Ứng dụng Báo Thức.

---

## 🚀 Trạng Thái Dự Án
- **Phase Hiện Tại:** Phase 4 (Optimization & UX)
- **Tiến Độ:** 95%
- **Ngày cập nhật cuối:** 2026-04-10

## 📍 Lộ Trình Tổng Thể
- [x] **Phase 1:** Hoàn thiện Tầng Dữ liệu (Room DB & Repository)
- [x] **Phase 2:** Cơ Chế Lập Lịch Báo Thức (AlarmManager & Receivers)
- [x] **Phase 3:** Giao diện Người dùng (CRUD Alarm List & Create)
- [x] **Phase 4:** Trải Nghiệm Màn Hình Chuông & Tối ưu UX

---

## 🧠 Nhật Ký Quyết Định Kỹ Thuật (TDR)

### [TDR-001] - Sử dụng AlarmManager cho Core Logic
- **Ngày thực hiện:** 2026-04-07
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần một cơ chế kích hoạt báo thức chính xác tuyệt đối ngay cả khi thiết bị ở chế độ Doze mode.
- **Quyết định:** Sử dụng `AlarmManager` kết hợp với `setExactAndAllowWhileIdle`.
- **Hệ quả:** Đảm bảo độ chính xác tới từng giây, nhưng cần quản lý chặt chẽ Resource vì OS có giới hạn số lượng báo thức chính xác.

### [TDR-002] - Module Design: Gộp tạm RingingActivity và Service
- **Ngày thực hiện:** 2026-04-07
- **Trạng thái:** Superseded by [TDR-005]
- **Bối cảnh:** Giai đoạn đầu cần đẩy nhanh tiến độ logic chuông.
- **Quyết định:** Gộp `RingingActivity` và `AlarmService` vào module `:features:alarm`.
- **Hệ quả:** Tăng tốc độ dev nhưng gây khó khăn cho việc tái sử dụng và vi phạm nguyên tắc tách biệt Infrastructure.

### [TDR-003] - Tách Module DataStore riêng biệt
- **Ngày thực hiện:** 2026-04-07
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần lưu trữ Preference (Settings) tách biệt với CSDL quan hệ (Room).
- **Quyết định:** Di chuyển logic `DataStore` từ `:core:database` sang `:core:datastore`.
- **Hệ quả:** Kiến trúc rành mạch hơn, các module khác có thể dùng DataStore mà không cần kéo theo Room dependency.

### [TDR-004] - Kiến trúc Back-end báo thức (Dependency Inversion)
- **Ngày thực hiện:** 2026-04-07
- **Trạng thái:** Accepted
- **Bối cảnh:** Tránh Implicit Intent (lỗi Android 8+) và Circular Dependency khi module thấp gọi activity ở module cao.
- **Quyết định:** 
  - Tách module `:core:alarms` chuyên biệt cho Scheduler/Receiver.
  - Áp dụng Dependency Inversion qua interface `AlarmIntentProvider`.
- **Hệ quả:** Giải quyết triệt để lỗi Background limit và làm sạch đồ thị phụ thuộc module.

### [TDR-005] - Tập trung hóa Logic Thời gian (Single Source of Truth)
- **Ngày thực hiện:** 2026-04-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Logic tính toán báo thức tiếp theo bị phân mảnh giữa UI và Service.
- **Quyết định:** Đưa phương thức `getNextOccurrence` vào trực tiếp Domain Model `Alarm`.
- **Hệ quả:** Toàn bộ hệ thống (UI, Scheduler, DB) dùng chung một logic tính toán duy nhất.

### [TDR-006] - Material Design 3 Dynamic Theming
- **Ngày thực hiện:** 2026-04-10
- **Trạng thái:** Accepted
- **Bối cảnh:** Hệ thống theme cũ bị phân mảnh và hardcode màu OLED.
- **Quyết định:** 
  - Triển khai 5 bảng màu Material 3 chuẩn (Blue, Purple, Green, Orange, Indigo).
  - Ép buộc sử dụng `MaterialTheme.colorScheme`.
- **Hệ quả:** Giao diện nhất quán tuyệt đối, hỗ trợ tốt chế độ Sáng/Tối tự động.

### [TDR-007] - Chuyển đổi TimePicker sang TimeInput
- **Ngày thực hiện:** 2026-04-10
- **Trạng thái:** Accepted
- **Bối cảnh:** TimePicker dạng xoay (Radial) chiếm quá nhiều diện tích và khó nhập chính xác trên màn hình nhỏ.
- **Quyết định:** Sử dụng `TimeInput` (nhập số trực tiếp) trong Dialog chỉnh sửa.
- **Hệ quả:** UX nhanh hơn, tiết kiệm diện tích cho các thiết lập khác (Day picker, Label).

### [TDR-008] - Xử lý Insets và Keyboard tập trung
- **Ngày thực hiện:** 2026-04-10
- **Trạng thái:** Accepted
- **Bối cảnh:** Bàn phím che khuất nút "Lưu" hoặc các field nhập liệu ở dưới cùng.
- **Quyết định:** Triển khai `imePadding` và `navigationBarsPadding` trực tiếp tại `AlarmScaffold`.
- **Hệ quả:** Giao diện tự động co dãn (Adjust Resize) mượt mà trên mọi dòng máy Android.

### [TDR-009] - Chuẩn hóa và Tự động hóa Nhật ký Phát triển (Logwork)
- **Ngày thực hiện:** 2026-04-10
- **Trạng thái:** Accepted
- **Bối cảnh:** LOGWORK.md cũ thiếu cấu trúc, khó theo dõi lịch sử kiến trúc và quy trình cập nhật thủ công dễ bị lãng quên.
- **Quyết định:** 
  - Triển khai hệ thống Technical Decision Record (TDR) với format chuẩn.
  - Tạo kỹ năng `@logwork-update` để tự động hóa việc rà soát và ghi log.
  - Tích hợp bước ghi log vào cuối mọi workflow phát triển chính.
- **Hệ quả:** Giảm thiểu sai sót tài liệu, tăng tính minh bạch cho các quyết định kỹ thuật và giúp Agent (AI) nắm bắt bối cảnh dự án nhanh hơn.

### [TDR-010] - Cập nhật Bộ nhận diện thương hiệu (Logo & Adaptive Icons)
- **Ngày thực hiện:** 2026-04-10
- **Trạng thái:** Accepted
- **Bối cảnh:** Logo cũ không hỗ trợ Adaptive Icons và thiếu biểu tượng Monochrome cho Android 13+.
- **Quyết định:** 
  - Cập nhật layer Foreground/Background.
  - Chuyển cấu hình XML vào thư mục `mipmap-anydpi-v26`.
  - Bổ sung `ic_launcher_monochrome.xml`.
- **Hệ quả:** Icon ứng dụng hiển thị chuẩn trên mọi launcher, hỗ trợ đổi màu theo Dynamic Theme của hệ thống.

---

## 🛠 Changelog (Tính năng mới)

### [2026-04-10]
- **Branding:** Cập nhật Logo mới và hỗ trợ Adaptive Icons toàn diện.
- **Infra:** Thiết lập hệ thống kỹ năng `@logwork-update` mới.
- **UI:** Thêm Gradient overlay phía dưới danh sách để làm nổi bật nút Lưu.
- **UX:** Thêm cơ chế tự động cuộn đến item đang edit khi bàn phím hiện lên.
- **Logic:** Xử lý post-trigger tự động Reschedule báo thức tuần hoàn ngay sau khi chuông reo.
