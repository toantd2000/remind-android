# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này dùng để ghi vết (tracking) quá trình thực thi các tính năng, lịch sử thay đổi kiến trúc và các quyết định kỹ thuật quan trọng của Ứng dụng Báo Thức.

---

## 🚀 Trạng Thái Dự Án
- **Phase Hiện Tại:** Phase 4 (Optimization & UX)
- **Tiến Độ:** 100%
- **Ngày cập nhật cuối:** 2026-04-20



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
### [TDR-011] - Tối giản hệ thống màu sắc và chuẩn hóa Localization
- **Ngày thực hiện:** 2026-04-12
- **Trạng thái:** Accepted
- **Bối cảnh:** Hệ thống 5 bảng màu chuẩn (TDR-006) gây dư thừa. Các chuỗi văn bản trong Cài đặt bị hardcode.
- **Quyết định:** 
  - Rút gọn còn 1 bảng màu **Mặc định** (từ `Color.kt`) và 1 tùy chọn **Màu động** (Dynamic Color).
  - Di chuyển toàn bộ văn bản sang `String Resources` tại module `:features:settings`.
  - Sử dụng `Segmented Button` (M3) để gộp nhóm cài đặt giao diện & màu sắc.
- **Hệ quả:** Giao diện tối giản, nhất quán. Module hóa tài nguyên giúp dễ dàng bảo trì và đa ngôn ngữ hóa.

### [TDR-012] - Cơ chế lựa chọn và Preview Nhạc chuông thực tế
- **Ngày thực hiện:** 2026-04-13
- **Trạng thái:** Accepted
- **Bối cảnh:** Trước đây âm thanh báo thức chỉ là giả lập hoặc mặc định hệ thống. Người dùng cần chọn nhạc chuông từ máy và nghe thử trực tiếp.
- **Quyết định:** 
  - Sử dụng `RingtoneManager` để liệt kê âm thanh từ thiết bị.
  - Dùng `MediaPlayer` với `USAGE_ALARM` (Edit) và `USAGE_MEDIA` (Selection) để preview.
  - Triển khai màn hình `RingtoneSelectionScreen` riêng biệt để quản lý lựa chọn.
- **Hệ quả:** Người dùng có trải nghiệm cá nhân hóa cao, nghe thử âm lượng và rung thực tế trước khi lưu.

### [TDR-013] - Tách biệt Logic Âm thanh sang core:common (Shared Utility)
- **Ngày thực hiện:** 2026-04-13
- **Trạng thái:** Accepted
- **Bối cảnh:** Cả UI (ViewModel) và Service (Background) đều cần logic kiểm tra quyền truy cập file nhạc và cơ chế fallback (default -> first available).
- **Quyết định:** Di chuyển phương thức `getAccessibleRingtoneUri` vào module `:core:common`.
- **Hệ quả:** Tránh lỗi "Unresolved reference" giữa các module core và feature, đảm bảo tính tái sử dụng và nguyên tắc Layered Architecture.

### [TDR-014] - Đồng bộ hóa cài đặt Volume và Rung trên toàn hệ thống
- **Ngày thực hiện:** 2026-04-13
- **Trạng thái:** Accepted
- **Bối cảnh:** Cài đặt âm lượng và rung đôi khi bị hệ thống bỏ qua hoặc không đồng bộ với preview.
- **Quyết định:** 
  - `AlarmService` đọc trực tiếp thuộc tính `volume` và `vibrationEnabled` từ Database cho mỗi lần reo.
  - Sử dụng `audioManager.setStreamVolume` ngay khi người dùng kéo slider để feedback tức thì.
- **Hệ quả:** Báo thức hoạt động chính xác theo cấu hình riêng lẻ của từng item, không bị phụ thuộc vào cài đặt system tại thời điểm reo.

### [TDR-015] - Chuẩn hóa Icon hệ thống sang biến thể Rounded
- **Ngày thực hiện:** 2026-04-16
- **Trạng thái:** Accepted
- **Bối cảnh:** Các icon hiện tại (Default/Filled) có đường nét sắc nhọn, gây cảm giác cứng nhắc. Người dùng yêu cầu giao diện nhẹ nhàng (softer) hơn.
- **Quyết định:** 
  - Chuyển đổi toàn bộ Material Icons sang biến thể `Rounded`.
  - Ép buộc sử dụng `import androidx.compose.material.icons.rounded.*` thay cho các biến thể khác.
- **Hệ quả:** Giao diện nhất quán, mang lại cảm giác thân thiện và nhẹ nhàng hơn cho người dùng. Đảm bảo tính nhất quán giữa các module feature.

### [TDR-016] - Giải quyết xung đột WindowInsets với Nested Scaffold
- **Ngày thực hiện:** 2026-04-16
- **Trạng thái:** Accepted
- **Bối cảnh:** `MainActivity` có chứa `Scaffold` tổng để chứa `BottomNavigationBar`, tuy nhiên các màn hình con bên trong `NavHost` cũng được bọc bởi `ReMindScaffold`. Việc lồng Scaffold ở Jetpack Compose Material 3 khiến WindowInsets (System Navigation Bar) bị tính hai lần, gây hiện tượng khoảng lề đáy bị đẩy lên sai lệch.
- **Quyết định:** 
  - Đặt `contentWindowInsets = WindowInsets(0, 0, 0, 0)` cho Scaffold tại `MainActivity`.
- **Hệ quả:** Ngăn chặn cộng dồn Padding insets, khu vực màn hình con nhận diện chính xác khoảng không thực tế, nội dung hiển thị tràn viền đáy (Edge-To-Edge) hoàn hảo mà không bị che khuất bởi Bottom Bar hay khoảng trắng dư thừa.

### [TDR-017] - StateFlow đếm ngược Auto Silence & Tái sử dụng ReminderController
- **Ngày thực hiện:** 2026-04-16
- **Trạng thái:** Accepted
- **Bối cảnh:** Báo thức cần thông báo trạng thái đếm ngược tự động tắt (`autoSilenceCountdown`) lên UI. Khi hết đếm ngược, Service phải đưa ra quyết định Snooze hoặc Dismiss tuỳ thuộc vào dữ liệu báo thức.
- **Quyết định:** 
  - Khai báo một `StateFlow` thông qua `ReminderRingManager` để truyền giá trị giây còn lại liên tục từ Background lên UI Màn hình đổ chuông (`ReminderRingingScreen`).
  - Phía ViewModel dùng `stateIn` để collect.
  - Khi đếm ngược trôi về 0, thay vì tự tách rời 2 nhánh rẽ, hệ thống gọi thẳng lệnh `snoozeReminder()` có sẵn từ `ReminderControllerImpl`. 
- **Hệ quả:** Vì hàm `snoozeReminder()` được viết chặt chẽ (chỉ đặt báo thức mới nếu còn lượt và có bật Snooze), tính năng Auto Silence tận dụng trọn vẹn luồng validation này. Code không bị trùng lặp, đảm bảo không bao giờ có lỗi logic khi dời hoãn. UI hiển thị rõ ràng cho người dùng số phút còn lại trước khi tự ngắt.

### [TDR-018] - Cơ chế khóa màn hình chống gian lận (Anti-Cheat Snooze Lock) & Trạng thái Bỏ Lỡ
- **Ngày thực hiện:** 2026-04-16
- **Trạng thái:** Accepted
- **Bối cảnh:** Khi báo thức reo mà bị người dùng hoãn lại (Snooze) hoặc không nghe máy dẫn đến quá giờ cúp (Missed), người dùng có thể gian lận bằng cách vào App tắt ngang báo thức. Trong tương lai App muốn bắt buộc người dùng hoàn thành một nhiệm vụ (Task) mới được tắt hẳn báo thức. Vậy nên, cần chặn đứng việc truy cập nội dung App nếu có báo thức đang chưa được giải quyết dứt điểm.
- **Quyết định:** 
  - Khai báo thêm `isMissed` và `snoozeNextTriggerTime` ngay nội tại bản ghi `ReminderEntity` và `Reminder`. Nếu là môi trường chưa Production, `Room` sẽ dùng `fallbackToDestructiveMigration()` để format DB tự động (thay vì tăng version).
  - Main ViewModel (hoặc Provider chặn cửa vào app) sẽ quan sát: Nếu Database tồn tại bất kì bản ghi nào có cờ `isMissed = true` hoặc đang đếm ngược `snoozeNextTriggerTime`, gán cờ `activeBlockingReminderId`. Bắt buộc NavHost mở sang `ReminderRingingScreen` để người dùng đối mặt với báo thức đó ngay lập tức, không cho phép dùng App.
  - Trên màn hình `ReminderRingingScreen`, bổ sung khả năng "Tàng hình/Im lặng": Màn hình đổ chuông nhưng lại không phát bất kì âm thanh nào. Nhờ vậy mới hiển thị được Trạng thái "Đã Bỏ lỡ" (Missed) hoặc đếm ngược tĩnh (Snoozing) mà không gây tốn pin.
- **Hệ quả:** Hoàn toàn bịt được lỗ hổng gian lận khi dùng báo thức. UI Màn hình đổ chuông làm hai nhiệm vụ: đổ chuông thực (khi gọi từ Background) VÀ làm tường chắn (Khóa App) khi báo thức đang hoãn / bỏ lỡ.

### [TDR-019] - Chuẩn hóa Input Field với Action Clear và Floating Label
- **Ngày thực hiện:** 2026-04-17
- **Trạng thái:** Accepted
- **Bối cảnh:** Các ô nhập liệu (Label, Message) trong màn hình chỉnh sửa báo thức hiện chỉ có placeholder, biến mất khi có chữ khiến người dùng khó nhận biết nội dung đang nhập. Đồng thời thiếu cơ chế xóa nhanh (Clear) văn bản.
- **Quyết định:** 
  - Tạo component `ReMindTextField` trong `:core:designsystem` bao bọc `OutlinedTextField`.
  - Tích hợp `trailingIcon` tự động hiển thị nút X để xóa khi ô nhập không trống.
  - Sử dụng `label` (M3) để tạo hiệu ứng Floating Label khi có focus hoặc có text.
- **Hệ quả:** Tăng tính nhất quán cho UI, cải thiện UX khi nhập liệu dài và giúp người dùng xóa văn bản nhanh chóng.

### [TDR-020] - Hỗ trợ chọn Ngày cụ thể cho Báo thức (One-time Alarm)
- **Ngày thực hiện:** 2026-04-17
- **Trạng thái:** Accepted
- **Bối cảnh:** Trước đây báo thức chỉ hỗ trợ lặp lại theo thứ trong tuần hoặc báo thức hàng ngày/ngày mai. Người dùng cần đặt báo thức vào một ngày cụ thể trong tương lai.
- **Quyết định:** 
  - Mở rộng model `Reminder` và `ReminderEntity` với trường `date` (LocalDate).
  - Tích hợp `DatePicker` (M3) vào `RepeatDaySelector` với ràng buộc không cho chọn ngày quá khứ.
  - Thiết lập cơ chế loại trừ tương hỗ: Chọn ngày cụ thể sẽ xóa lặp lại theo thứ, và ngược lại.
  - Reset Database version về 1 theo yêu cầu phát triển (Development phase cleanup).
- **Hệ quả:** Tăng tính linh hoạt cho ứng dụng, cho phép dùng như một công cụ nhắc nhở sự kiện một lần chính xác.
194: 
195: ### [TDR-021] - Tự động hóa Giấy phép và Đánh số phiên bản theo ngày (Build Release)
196: - **Ngày thực hiện:** 2026-04-20
197: - **Trạng thái:** Accepted
198: - **Bối cảnh:** Việc cập nhật thủ công danh sách thư viện và số phiên bản mỗi khi phát hành dễ gây sai sót và tốn thời gian.
199: - **Quyết định:** 
200:   - Tích hợp plugin `AboutLibraries` để tự động quét toàn bộ dependencies.
201:   - Viết Gradle script tự động tạo `versionName` dựa trên ngày hiện tại khi build release.
202: - **Hệ quả:** Đảm bảo tính minh bạch về pháp lý và quy trình phát hành chuyên nghiệp, giảm thiểu sai sót do con người.
203: 
204: ### [TDR-022] - Quản lý Báo thức nâng cao: Nhân bản, Bỏ qua và Hoàn tác
205: - **Ngày thực hiện:** 2026-04-20
206: - **Trạng thái:** Accepted
207: - **Bối cảnh:** Người dùng cần các thao tác quản lý nhanh ngay tại màn hình danh sách mà không làm rối giao diện chính.
208: - **Quyết định:** 
209:   - Sử dụng tương tác nhấn giữ (Long-press) để hiện menu chức năng.
210:   - Triển khai cơ chế `skippedAt` (LocalDate) để đánh dấu bỏ qua lần reo tiếp theo.
211:   - Sử dụng Snackbar với action Undo để cho phép khôi phục báo thức vừa xoá.
212: - **Hệ quả:** Tăng năng suất sử dụng, giảm thiểu rủi ro khi thao tác nhầm, và giữ được ngôn ngữ thiết kế tối giản của ứng dụng.

- **Hệ quả:** Giao diện gọn gàng, hiện đại và tránh dư thừa thông tin trên màn hình chỉnh sửa.

### [TDR-023] - Chuẩn hóa hiển thị Ngày (2 ký tự) và Tối ưu tóm tắt Lặp lại theo ngữ cảnh
- **Ngày thực hiện:** 2026-04-20
- **Trạng thái:** Accepted
- **Bối cảnh:** Tên các thứ trong tuần (Th 2, Th 3...) dài và không đồng nhất (CN). Đồng thời, màn hình Edit báo thức hiển thị quá nhiều thông tin lặp lại (vừa có tóm tắt chữ, vừa có các hình tròn chọn ngày ở dưới).
- **Quyết định:** 
  - Chuyển đổi resource ngày sang định dạng 2 ký tự (T2, T3... CN) để tối ưu không gian.
  - Triển khai `isShortMode` cho hàm tóm tắt: Tại màn hình Edit chỉ hiển thị chữ "Lặp lại", còn màn hình Danh sách hiển thị chi tiết (ví dụ: "Lặp lại: T2, T3").
- **Hệ quả:** Giao diện gọn gàng, hiện đại và tránh dư thừa thông tin trên màn hình chỉnh sửa.

### [TDR-024] - Nâng cấp tính năng Âm lượng tăng dần thành "Nhắc nhở nhẹ nhàng" (Per-alarm)
- **Ngày thực hiện:** 2026-04-20
- **Trạng thái:** Accepted
- **Bối cảnh:** Tính năng âm lượng tăng dần trước đây là cài đặt chung (Global), không linh hoạt cho từng loại báo thức khác nhau (ví dụ: báo thức sáng cần tăng dần, báo thức nhắc việc cần kêu ngay).
- **Quyết định:** 
  - Chuyển đổi từ Global Setting sang Per-reminder Setting.
  - Đổi tên thành "Nhắc nhở nhẹ nhàng" với giao diện chọn thời gian (Off, 15s, 30s, 1m, 5m, 10m) qua Bottom Sheet.
  - Triển khai logic tăng âm lượng từ 1 đến mức tối đa trong `ReminderService` sử dụng Coroutine.
- **Hệ quả:** Tăng tính cá nhân hóa cho trải nghiệm người dùng, giúp việc thức dậy trở nên êm ái hơn theo ý muốn của từng cá nhân.

### [TDR-025] - Tối giản hóa trạng thái trống (Minimalist Empty State)
- **Ngày thực hiện:** 2026-04-20
- **Trạng thái:** Accepted
- **Bối cảnh:** Màn hình danh sách khi trống hiển thị quá nhiều thông tin (Icon, Tiêu đề, Mô tả). Người dùng yêu cầu sự tối giản, chỉ cần 1 dòng thông báo.
- **Quyết định:** 
  - Loại bỏ Icon và dòng Text mô tả ("Nhấn + để thêm...").
  - Chuyển sang sử dụng `Box` căn giữa với duy nhất 1 dòng Text thông báo.
  - Sử dụng Typography `bodyLarge` và màu `onSurfaceVariant` để tạo cảm giác nhẹ nhàng, không gây xao nhãng.
- **Hệ quả:** Giao diện sạch sẽ hơn, tập trung vào nội dung chính khi bắt đầu sử dụng app.

- **Hệ quả:** Giao diện cài đặt trở nên sạch sẽ, nhất quán và tránh gây nhầm lẫn cho người dùng về các tính năng chưa khả dụng.

- **Hệ quả:** Tăng tính chuyên nghiệp, định vị thương hiệu rõ ràng và truyền tải được giá trị cốt lõi của sản phẩm tới người dùng.

- **Hệ quả:** Quy trình cập nhật chuyên nghiệp, giao diện thân thiện và dễ dàng bảo trì thông qua việc AI tự động đồng bộ từ LOGWORK sang JSON.

### [TDR-029] - Tích hợp Splash Screen chuẩn Android 12+ và Branding Slogan
- **Ngày thực hiện:** 2026-04-20
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần một màn hình khởi động mượt mà để che giấu thời gian load App và truyền tải slogan thương hiệu.
- **Quyết định:** 
  - Sử dụng thư viện `androidx.core:core-splashscreen` cho tầng hệ thống (System Splash).
  - Triển khai `BrandingSplashScreen` bằng Jetpack Compose để hiển thị Slogan "Wake up, Challenge, Repeat." ngay sau khi App khởi chạy.
  - Sử dụng hiệu ứng `AnimatedVisibility` (Fade-in) để tạo cảm giác chuyên nghiệp.
- **Hệ quả:** Trải nghiệm người dùng cao cấp ngay từ giây đầu tiên, tạo ấn tượng mạnh về thương hiệu và triết lý của ứng dụng.

### [TDR-030] - Tích hợp liên kết Điều khoản và Bảo mật (Privacy & Terms)
- **Ngày thực hiện:** 2026-04-20
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần cung cấp các tài liệu pháp lý cho người dùng mà không làm tăng dung lượng App và dễ dàng cập nhật.
- **Quyết định:** 
  - Sử dụng `androidx.browser:browser` (Custom Tabs) để mở các liên kết web ngay trong ứng dụng.
  - Các liên kết được trỏ đến hệ thống legal tập trung của LiteVer: `https://legal.litever.io/remind/`.
- **Hệ quả:** Đảm bảo tính pháp lý, nội dung luôn cập nhật và giữ chân người dùng trong App (không bị nhảy sang trình duyệt ngoài).

### [TDR-031] - Tự động nhận diện ngôn ngữ hệ thống (Localization Logic)
- **Ngày thực hiện:** 2026-04-20
- **Trạng thái:** Accepted
- **Bối cảnh:** Ngôn ngữ mặc định đang bị cứng là Tiếng Anh, không đúng với yêu cầu tự động nhận diện Tiếng Việt từ hệ thống.
- **Quyết định:** 
  - Cập nhật logic mặc định trong `ReminderPreferencesDataSource` để kiểm tra `Locale.getDefault().language`.
  - Đồng bộ giá trị `initialValue` trong `MainActivity` và `SettingsViewModel` để tránh hiện tượng nháy ngôn ngữ khi khởi động.
- **Hệ quả:** Trải nghiệm bản địa hóa mượt mà, đúng yêu cầu người dùng ngay từ lần đầu mở App.

### [TDR-032] - Cấu hình Ký số (Signing Config) bảo mật qua local.properties
- **Ngày thực hiện:** 2026-04-21
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần cấu hình ký số cho bản build Release nhưng phải đảm bảo không lộ thông tin nhạy cảm (KeyStore password, Alias) lên Git.
- **Quyết định:** 
  - Đưa các thông số nhạy cảm vào `local.properties` (file này đã được `.gitignore` chặn).
  - Sử dụng đối tượng `Properties` trong `app/build.gradle.kts` để đọc các giá trị này tại thời điểm build.
  - Khai báo `signingConfigs` và gán cho `release` build type.
- **Hệ quả:** Bản build Release có thể được ký tự động mà vẫn đảm bảo tính bảo mật của KeyStore.

### [TDR-033] - Chuyển đổi trạng thái Bỏ Lỡ sang Message Screen và Ưu tiên Điều hướng
- **Ngày thực hiện:** 2026-04-22
- **Trạng thái:** Accepted
- **Bối cảnh:** Trước đây trạng thái "Bỏ lỡ" (Missed) được hiển thị trực tiếp trên màn hình reo chuông (`ReminderRingingScreen`). Tuy nhiên, yêu cầu mới là tách biệt: màn hình reo chuông chỉ dành cho các tác vụ đang diễn ra (Ringing, Snoozing), còn trạng thái "Bỏ lỡ" (đã xảy ra) nên được xử lý bởi `ReminderMessageScreen`.
- **Quyết định:** 
  - Loại bỏ toàn bộ UI liên quan đến "Missed" khỏi `ReminderRingingScreen`.
  - Cập nhật logic điều hướng tại `MainActivity`: Ưu tiên hiển thị báo thức đang reo/hoãn (Ringing/Snooze), sau đó mới đến báo thức đã bỏ lỡ (Missed).
  - Báo thức đã bỏ lỡ sẽ "treo" app tại `ReminderMessageScreen`. Người dùng bắt buộc phải nhấn "OK" (để xóa trạng thái `isMissed`) mới có thể quay lại app.
  - Thêm `BackHandler` vào `ReminderMessageScreen` để ngăn chặn việc thoát ra ngoài mà không xác nhận.
- **Hệ quả:** Đảm bảo người dùng luôn đối mặt và xác nhận các báo thức đã lỡ, đồng thời giữ được sự tách biệt rõ ràng giữa màn hình reo chuông và màn hình thông báo.

### [TDR-034] - Tối ưu hóa vòng đời Auto-Silence và Đồng bộ hóa Lifecycle (Refined)
- **Ngày thực hiện:** 2026-04-22
- **Trạng thái:** Accepted
- **Bối cảnh:** Tính năng Auto-silence gặp lỗi race condition (chạy đua) giữa Main thread và background thread, dẫn đến việc đếm ngược bị hủy sai thời điểm (sau khi Snooze hoặc khi quay về từ màn hình nhiệm vụ). Ngoài ra, người dùng yêu cầu đếm ngược phải dừng khi làm nhiệm vụ và bắt đầu lại từ đầu khi quay về.
- **Quyết định:** 
  - Tách biệt logic dừng chuông (`stopAudibleRinging`) và dừng đếm ngược (`stopAutoSilence`).
  - Đồng bộ hóa toàn bộ logic thay đổi trạng thái chuông và đếm ngược bên trong `withContext(Dispatchers.Main)` của flow collector để đảm bảo tính tuần tự.
  - Áp dụng quy tắc: Dừng đếm ngược khi Muted (Mission) hoặc Snoozing. Khởi động lại đếm ngược từ đầu khi trở lại trạng thái Ringing (Audible).
  - Chuyển đổi toàn bộ `ReminderController` sang `suspend` để xử lý bất đồng bộ chuẩn xác.
- **Hệ quả:** Khắc phục hoàn toàn lỗi mất đếm ngược. Đảm bảo trải nghiệm nhất quán và đúng yêu cầu người dùng: đếm ngược luôn bắt đầu lại đủ thời gian khi người dùng quay lại xử lý báo thức.

### [TDR-035] - Tối ưu hóa Điều hướng để loại bỏ hiện tượng nháy màn hình (Flicker)
- **Ngày thực hiện:** 2026-04-22
- **Trạng thái:** Accepted
- **Bối cảnh:** Khi người dùng nhấn Snooze, màn hình bị nháy (flicker) do cơ chế popBackStack kết hợp với điều hướng cưỡng bức từ `MainActivity`. Việc điều hướng lại diễn ra liên tục mỗi khi trạng thái báo thức cập nhật.
- **Quyết định:** 
  - Thêm kiểm tra `reminderId` hiện tại trong `MainActivity` trước khi thực hiện `navigate`. Chỉ điều hướng nếu đích đến hoặc ID báo thức thực sự thay đổi.
  - Loại bỏ lệnh `onFinish` (pop stack) khi nhấn Snooze để giữ người dùng ở lại màn hình chặn mà không cần khởi tạo lại.
- **Hệ quả:** Trải nghiệm người dùng mượt mà hơn, các nút bấm ẩn hiện tức thì mà không gây cảm giác gián đoạn do chuyển cảnh navigation.

### [TDR-036] - Triển khai Luồng xử lý Báo thức Tuần tự (Sequential Workflow)
- **Ngày thực hiện:** 2026-04-22
- **Trạng thái:** Accepted
- **Bối cảnh:** Khi có nhiều báo thức reo gần nhau, màn hình Ringing và Message bị chồng lấn (overlap). Người dùng yêu cầu phải xác nhận xong báo thức cũ mới hiện màn hình báo thức mới.
- **Quyết định:** 
  - Thêm trạng thái `acknowledgingReminderId` vào `ReminderRingManager` để theo dõi báo thức đang đợi xác nhận.
  - Thay đổi mức độ ưu tiên điều hướng trong `MainActivity`: Hiển thị màn hình Message (xác nhận) trước khi hiển thị màn hình Ringing của báo thức tiếp theo.
  - Sử dụng `popUpTo(reminderListRoute)` khi điều hướng giữa các màn hình chặn để đảm bảo backstack sạch sẽ, không bị chồng đè UI.
- **Hệ quả:** Đảm bảo tính tuần tự, người dùng không bị rối khi có nhiều sự kiện xảy ra cùng lúc, đồng thời giải quyết triệt để lỗi chồng lấn giao diện.

---

## 🛠 Changelog (Tính năng mới)

### [2026-04-22]
- **UX:** Triển khai luồng xử lý báo thức tuần tự: Màn hình xác nhận (Message) luôn được ưu tiên xử lý xong trước khi hiện màn hình báo thức tiếp theo. Khắc phục hoàn toàn lỗi chồng lấn giao diện.
- **Fix:** Loại bỏ hiện tượng nháy màn hình ("nháy") khi nhấn Snooze bằng cách tối ưu hóa logic điều hướng trong `MainActivity` và loại bỏ các lệnh pop backstack dư thừa.
- **Fix:** Khắc phục lỗi Auto-silence hoạt động không ổn định khi người dùng vào màn hình nhiệm vụ (Mute). Timer hiện tại sẽ chạy xuyên suốt và không bị reset.
- **UX:** Triển khai cơ chế "chặn" app khi có báo thức bị bỏ lỡ (Missed). Ứng dụng sẽ treo tại `ReminderMessageScreen` cho đến khi người dùng nhấn "OK".
- **Navigation:** Cập nhật logic điều hướng ưu tiên: Ringing > Snoozing > Missed. Đảm bảo màn hình reo chuông luôn hiển thị trên cùng.
- **UI:** Nâng cấp `ReminderMessageScreen` với icon trạng thái, typography mới và `BackHandler` để ngăn chặn việc bỏ qua xác nhận.

### [2026-04-21]
- **Infra:** Cấu hình ký số (Signing) cho bản build Release thông qua `local.properties`.

### [2026-04-20]
- **Feature:** Triển khai "Nhắc nhở nhẹ nhàng" (Gentle Reminder) cho phép tùy chỉnh thời gian tăng dần âm lượng riêng cho từng báo thức.
- **UX:** Chuẩn hóa tên các thứ trong tuần sang định dạng 2 ký tự (T2, T3... CN).
- **UI:** Tối ưu hóa tóm tắt lặp lại tại màn hình Chỉnh sửa (chỉ hiển thị "Lặp lại") để tránh dư thừa thông tin.
- **Logic:** Nâng cấp `getRepeatSummaryText` hỗ trợ chế độ rút gọn (`isShortMode`).
- **Automation:** Tích hợp plugin `AboutLibraries` để tự động thu thập và hiển thị danh sách giấy phép mã nguồn mở.
- **CI/CD:** Triển khai cơ chế đánh số phiên bản (`versionName`) tự động theo định dạng ngày `YYYY.MM.DD` cho các bản build Release.
- **UI:** Thêm màn hình `LicensesScreen` mới trong phần Cài đặt.
- **Branding:** Cập nhật hiển thị phiên bản ứng dụng thực tế (Dynamic Versioning) trong màn hình Giới thiệu.
- **Feature:** Thêm tính năng Nhân bản (Duplicate) và Bỏ qua lần tới (Skip once) cho báo thức.
- **UX:** Chuyển đổi sang menu ngữ cảnh khi nhấn giữ (Long-press) để tối ưu không gian hiển thị.
- **UX:** Hỗ trợ Hoàn tác (Undo) khi xoá báo thức đơn lẻ hoặc xoá hàng loạt.
- **UI:** Thêm icon minh họa cho các mục trong menu quản lý.
- **UI:** Tối giản hóa trạng thái trống (Empty State) chỉ còn một dòng text duy nhất.
- **UX:** Loại bỏ các thành phần gây xao nhãng (Icon, Description) khi danh sách báo thức trống.
- **UI:** Loại bỏ HorizontalDivider không đồng nhất trong cụm Alarm Settings tại màn hình Chỉnh sửa.
- **Localization:** Chuẩn hóa và bản địa hóa toàn bộ chuỗi văn bản trong `ReminderService` (Thông báo và Kênh thông báo).
- **UI:** Tối giản màn hình Cài đặt, ẩn các mục chưa hoàn thiện (Alarm setting, Support) và chuẩn hóa hiển thị phiên bản.
- **Branding:** Triển khai màn hình Thông tin tác giả (`AuthorInfoScreen`) với Logo thương hiệu LiteVer và Slogan mới.
- **UI:** Kết nối mục "Story - Author" trong Cài đặt với màn hình giới thiệu tác giả.
- **Feature:** Triển khai màn hình Lịch sử cập nhật (`UpdateHistoryScreen`) với giao diện Timeline.
- **Infra:** Tích hợp thư viện `kotlinx-serialization` và cập nhật quy trình phát hành (`release-preparation.md`).
- **Data:** Thiết lập file `changelog.json` trong assets để quản lý nội dung cập nhật cho người dùng.
- **UX:** Tích hợp Splash Screen chuẩn Android 12+ và Branding Screen với slogan "Wake up, Challenge, Repeat.".
- **Refactor:** Tách `BrandLogo` thành component chung trong `:core:designsystem` để tái sử dụng.
- **Legal:** Tích hợp mở Điều khoản dịch vụ và Chính sách bảo mật qua Custom Tabs.
- **Localization:** Cập nhật các chuỗi tài nguyên tiếng Việt cho mục Bảo mật.

### [2026-04-17]
- **Input UX:** Triển khai `ReMindTextField` với Floating Label và nút Xóa nhanh (Clear Action).
- **Feature:** Hỗ trợ đặt báo thức vào một ngày cụ thể (One-time Alarm) qua DatePicker.
- **Logic:** Tự động hủy lặp lại theo thứ khi chọn ngày cụ thể và ngược lại.

### [2026-04-13]
- **Design System:** Thêm thành phần `AlarmLogo` với thiết kế hiện đại, hỗ trợ Dynamic Dark/Light mode và FontSize linh hoạt.
- **UX:** Thay thế tiêu đề văn bản thuần túy bằng `AlarmLogo` trên màn hình danh sách báo thức.
- **UX:** Tự động ẩn `NextAlarmHeader` khi danh sách báo thức trống để giao diện gọn gàng hơn.
- **Feature:** Hoàn thiện màn hình chọn nhạc chuông (Ringtone Selection) từ thiết bị.
- **Audio:** Tích hợp MediaPlayer cho preview âm thanh thực tế kèm cơ chế rung (vibration preview).
- **Control:** Cho phép tùy chỉnh âm lượng (Volume) riêng biệt cho từng báo thức.
- **Refactor:** Di chuyển logic Audio Utility sang `:core:common` để dùng chung cho Service và UI.
- **UX:** Tự động tạm dừng preview khi người dùng thay đổi thiết lập âm lượng/rung.
- **Database:** Nâng cấp Schema lên version 3 để lưu trữ thông tin Volume.

### [2026-04-12]
- **Theming:** Tối giản hệ thống màu sắc, chỉ giữ lại màu Mặc định và màu Động.
- **UI:** Gộp cài đặt Theme và Palette vào một nhóm duy nhất sử dụng Segmented Button.
- **Localization:** Di chuyển toàn bộ text trong màn hình Cài đặt sang String Resources (En/Vi).
- **Cleanup:** Xóa bỏ file cấu hình màu tạm `Color.kt` ở root project.

### [2026-04-10]
- **Branding:** Cập nhật Logo mới và hỗ trợ Adaptive Icons toàn diện.
- **Infra:** Thiết lập hệ thống kỹ năng `@logwork-update` mới.
- **UI:** Thêm Gradient overlay phía dưới danh sách để làm nổi bật nút Lưu.
- **UX:** Thêm cơ chế tự động cuộn đến item đang edit khi bàn phím hiện lên.
- **Logic:** Xử lý post-trigger tự động Reschedule báo thức tuần hoàn ngay sau khi chuông reo.
