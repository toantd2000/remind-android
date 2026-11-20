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
- **Text-to-Speech (TTS) Visibility & Recovery:** `AndroidManifest.xml` bắt buộc giữ `<queries>` với `INTENT_ACTION_TTS_SERVICE` cho Android 11+; `TtsSpeaker` phải reset `tts = null` và `shutdown()` khi `onInit` lỗi để có thể thử lại, đồng thời tự nhận diện ngôn ngữ (`detectLocale`) nếu `locale = null`.

## 3. Cơ Sở Dữ Liệu & Tầng Dữ Liệu (Room & Data)
- **Room Database Migration:** Cấm dùng `fallbackToDestructiveMigration()` trên Production. Bất kỳ thay đổi Entity nào trong `:core:database` đều phải tăng `version` và viết `Migration(old, new)` tường minh với câu lệnh SQL.
- **Exception Boundary Isolation:** Tại `RepositoryImpl` trong `:core:data`, phải bắt mọi ngoại lệ framework (`SQLiteException`, `HttpException`, `IOException`) và ánh xạ sang Domain Exceptions thuần túy trước khi bọc vào `Result<T>`.
- **Tách bạch DAO:** Hàm trả về `Flow` dùng cho reactive streaming, hàm `suspend` dùng cho one-shot query. Với các quan hệ phức tạp, bắt buộc đánh dấu `@Transaction`.
- **Vòng đời Nhắc nhở (`isEnabled` là Single Source of Truth):** Đã loại bỏ hoàn toàn cột `isCompleted` khỏi `reminders`. Nhắc nhở 1 lần hoàn thành bằng cách tắt (`isEnabled = false`), nhắc nhở định kỳ chuyển mốc tiếp theo và giữ `isEnabled = true`.

## 4. Giao Diện & Design System (Compose & Litever)
- **M3 Native & Token-First Rule:** Trong `:features:*`, gọi trực tiếp các component của `androidx.compose.material3.*` (`Scaffold`, `TopAppBar`, `Button`, `Card`, `Switch`, `ModalBottomSheet`, `AlertDialog`, `OutlinedTextField`...). Tuyệt đối không viết thêm pass-through wrapper bọc lại component M3 cơ bản; các hàm trong `LiteverLegacyCompat.kt` đã bị `@Deprecated` và sẽ được thay thế dần theo nguyên tắc Strangler Fig khi sửa màn hình cũ.
- **App-Owned Theme Entry Point (`ReMindTheme`):** `:core:designsystem` của ứng dụng trực tiếp làm chủ cấu hình `MaterialTheme` / `MaterialExpressiveTheme` và `CompositionLocalProvider` trong `ReMindTheme` (chỉ đọc Raw Tokens, Palettes, Bundled Fonts và Brand Assets/Mascot từ thư viện dùng chung) để cắt đứt sự phụ thuộc phiên bản Material 3 giữa các dự án.
- **Theme & Spacing Tokens:** Sử dụng `LiteverTheme.colors` / `MaterialTheme.colorScheme` (kèm extension `.warning`, `.success`), `LiteverTheme.shapes` và `LiteverTheme.spacing` (`none`, `tiny`, `small`, `medium`, `large`...). Cấm hardcode mã màu HEX và kích thước `.dp` tự do.
- **Preview Bắt Buộc:** Mọi Composable Screen/Component phải có `@Preview` cho cả Light và Dark mode bọc trong `ReMindTheme`. Render kiểm tra ngay qua `compose-preview`.
- **Exit App Dialog:** Màn hình chính bắt sự kiện Back bằng `BackHandler` hiển thị Exit Dialog. Nếu người dùng chưa bật tính năng ủng hộ (Ad-free), hiển thị Native Ad; nếu đã bật, ẩn hoàn toàn quảng cáo và lời kêu gọi.
- **Bundled Typography Fonts:** Thư viện tài nguyên dùng chung nhúng sẵn file `.ttf` (`Be Vietnam Pro` static + `Nunito` variable) trong `res/font/` thay cho `GoogleFont` tải về qua GMS để loại bỏ hiện tượng giật layout (FOUT) khi mở app/chuyển màn hình.

---
> *Khi phát hiện một quy tắc hoặc bài học mới "chỉ-có-dự-án-này-mới-thế", Agent bổ sung trực tiếp 1-2 dòng vào nhóm tương ứng trong file này.*
