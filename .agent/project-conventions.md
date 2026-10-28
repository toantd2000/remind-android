# Đặc thù & Quy ước Dự Án (Project Conventions)

Tài liệu này chứa các quy tắc nghiệp vụ, giải pháp kỹ thuật và bài học đã được chốt từ quá trình phát triển dự án **ReMind** (`vn.io.litever.remind`).
Đây là **luật bất di bất dịch (Invariants)**, có mức ưu tiên cao nhất, ghi đè mọi best practices thông thường nếu có xung đột.

---

## 1. Cơ Chế Lập Lịch Báo Thức & Nhắc Nhở (Alarm & Scheduler)
- **PendingIntent Identity:** Android phân biệt `PendingIntent` bằng `(ComponentName, Action, RequestCode)` mà **không xét Intent extras**.
  - Báo thức chính dùng `ACTION_TRIGGER_ALARM` và `requestCode = alarm.id.hashCode()`.
  - Báo lại (Snooze) dùng `ACTION_TRIGGER_SNOOZE` và `requestCode = alarm.id.hashCode() + 1_000_000_000`. Tuyệt đối không dùng chung id để tránh ghi đè hoặc cancel nhầm lịch.
- **Quick Alarm (Báo thức nhanh):** Là tác vụ tạm thời (transient).
  - Khi người dùng gạt tắt Switch trên danh sách (`toggleAlarm`) hoặc khi hoàn thành tắt chuông (`dismissAlarm`): Bắt buộc xóa vĩnh viễn khỏi CSDL Room và hủy lịch trong `AlarmScheduler`. Không mở màn hình Edit khi bấm vào card.
- **Ringing Priority Arbitration:** Khi cả Báo thức và Nhắc nhở cùng kích hoạt chuông, màn hình `MainActivity` ưu tiên điều hướng hiển thị Báo thức (`AlarmRinging`) trước Nhắc nhở (`ReminderRinging`).
- **Dependency Inversion (`AlarmIntentProvider`):** `:core:alarm` (Service) không được phụ thuộc vào `:app`. Gọi Activity thông qua interface `AlarmIntentProvider` định nghĩa tại `:core:alarm` và triển khai tại `:app`.

## 2. Quản Lý Âm Thanh & Media Player
- **Safe-Start Pattern:** Mọi thao tác điều khiển `MediaPlayer` (start, stop, release) **bắt buộc chạy trên Main Thread**.
- **Chuẩn bị không block UI:** `MediaPlayer.prepare()` hoặc nạp Audio URI phải chạy trên **Dispatcher IO** để tránh gây hiện tượng ANR/Jank.
- **Kiểm tra Coroutine Active:** Trước khi gọi `player.start()`, luôn kiểm tra `coroutineContext.isActive` để tránh phát âm thanh nếu phiên reo chuông đã bị hủy giữa chừng.
- **Tắt chuông khi làm nhiệm vụ (Mission):** Khi bắt đầu nhiệm vụ (Math, Typing), gọi `mute()` để dừng âm thanh và tạm dừng auto-silence timer. Nếu bấm Back/Bỏ dở, lập tức gọi `unmute()` để kích hoạt lại âm thanh.

## 3. Cơ Sở Dữ Liệu & Tầng Dữ Liệu (Room & Data)
- **Room Database Migration:** Cấm dùng `fallbackToDestructiveMigration()` trên Production. Bất kỳ thay đổi Entity nào trong `:core:database` đều phải tăng `version` và viết `Migration(old, new)` tường minh với câu lệnh SQL.
- **Exception Boundary Isolation:** Tại `RepositoryImpl` trong `:core:data`, phải bắt mọi ngoại lệ framework (`SQLiteException`, `HttpException`, `IOException`) và ánh xạ sang Domain Exceptions thuần túy trước khi bọc vào `Result<T>`.
- **Tách bạch DAO:** Hàm trả về `Flow` dùng cho reactive streaming, hàm `suspend` dùng cho one-shot query. Với các quan hệ phức tạp, bắt buộc đánh dấu `@Transaction`.
- **Vòng đời Nhắc nhở (`isEnabled` là Single Source of Truth):** Đã loại bỏ hoàn toàn cột `isCompleted` khỏi `reminders`. Nhắc nhở 1 lần hoàn thành bằng cách tắt (`isEnabled = false`), nhắc nhở định kỳ chuyển mốc tiếp theo và giữ `isEnabled = true`.

## 4. Giao Diện & Design System (Compose & Litever)
- **Interactive Component Rule:** Tuyệt đối không gọi trực tiếp các interactive Material 3 composables (`Button`, `Card`, `Dialog`, `Scaffold`, `Switch`, `TopAppBar`, `TextField`) trong `:features:*`. Bắt buộc tái sử dụng qua `:core:designsystem` (hoặc composite build `:litever-designsystem` với prefix `Litever*` / `Lv*`).
- **Theme & Spacing:** Sử dụng `LiteverTheme.colors` (36 M3 tokens), `LiteverTheme.shapes` và `LiteverTheme.spacing` (`none`, `tiny`, `small`, `medium`, `large`...). Cấm hardcode mã màu HEX và kích thước `.dp` tự do.
- **Preview Bắt Buộc:** Mọi Composable Screen/Component phải có `@Preview` cho cả Light và Dark mode. Render kiểm tra ngay qua `compose-preview`.
- **Exit App Dialog:** Màn hình chính bắt sự kiện Back bằng `BackHandler` hiển thị Exit Dialog. Nếu người dùng chưa bật tính năng ủng hộ (Ad-free), hiển thị Native Ad; nếu đã bật, ẩn hoàn toàn quảng cáo và lời kêu gọi.

---
> *Khi phát hiện một quy tắc hoặc bài học mới "chỉ-có-dự-án-này-mới-thế", Agent bổ sung trực tiếp 1-2 dòng vào nhóm tương ứng trong file này.*
