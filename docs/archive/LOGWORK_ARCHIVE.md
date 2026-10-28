# Logwork Archive

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

### [TDR-021] - Chuyển đổi nút chọn Thứ sang LvIconButton
- **Ngày thực hiện:** 2026-04-17
- **Trạng thái:** Accepted
- **Bối cảnh:** Các nút chọn thứ trong tuần tại `RepeatDaySelector` (`AlarmEditScreen.kt`) trước đây dùng `Surface` và `clickable` thủ công, chưa tận dụng hiệu ứng nhấn (ripple effect) và kiểu dáng chuẩn của Design System.
- **Quyết định:** 
  - Thay thế cụm `Surface` bằng `LvIconButton` với `type = if (isSelected) LvButtonType.Tonal else LvButtonType.Outlined` và `semantic = if (isSelected) LvSemantic.Primary else LvSemantic.Neutral`.
- **Hệ quả:** Đảm bảo hiệu ứng phản hồi (ripple/press state) mượt mà có sẵn từ `LvIconButton`, chuẩn hóa các thành phần UI theo LiteVer Design System.
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

### [TDR-061] - Hoàn thiện Nhập liệu Nhiệm vụ Gõ phím: Hiển thị nhiều lỗi và Ký tự thừa
- **Ngày thực hiện:** 2026-08-18
- **Trạng thái:** Accepted
- **Bối cảnh:** 
  - TDR-061 trước đó khóa con trỏ khi gặp lỗi, gây hạn chế cho người dùng muốn nhìn thấy toàn bộ nội dung mình gõ sai.
  - Người dùng yêu cầu hiển thị đồng thời nhiều lỗi và các ký tự nhập thừa so với câu mẫu.
- **Quyết định:** 
  - Cập nhật logic `VisualTransformation`:
    - Duyệt qua toàn bộ `userInput`.
    - So khớp từng ký tự: Đúng hiện **Xanh**, Sai hiện **Đỏ** (hiển thị ký tự người dùng gõ).
    - Mọi ký tự nhập thừa so với độ dài câu mẫu đều hiển thị màu **Đỏ**.
    - Các ký tự còn lại của câu mẫu hiển thị màu **Mờ (Dimmed)**.
  - Thiết lập `OffsetMapping` là 1:1 cho `originalToTransformed`, cho phép con trỏ tự do di chuyển theo nội dung nhập.
- **Hệ quả:** 
  - Phản hồi thị giác cực kỳ chi tiết: người dùng thấy chính xác những gì mình gõ sai và thừa.
  - Trải nghiệm nhập liệu tự nhiên, không bị khựng lại khi có lỗi.
  - Dễ dàng nhận diện và sửa lỗi tại bất kỳ vị trí nào.

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

### [TDR-037] - Tập trung hóa Điều hướng cho các màn hình Chặn (Blocking Screens)
- **Ngày thực hiện:** 2026-04-22
- **Trạng thái:** Accepted
- **Bối cảnh:** Màn hình `ReminderMessageScreen` bị hiển thị lặp lại 2 lần do sự xung đột giữa lệnh điều hướng thủ công trong các Route và logic điều hướng tự động dựa trên trạng thái (`acknowledgingReminderId`) trong `MainActivity`.
- **Quyết định:** 
  - Loại bỏ hoàn toàn việc gọi `navController.navigate` tới các route "chặn" (`reminder_message_route`, `reminder_ringing_route`) từ các sự kiện callback lẻ tẻ.
  - Sử dụng `MainActivity` làm trung tâm điều hướng duy nhất cho các màn hình này bằng cách theo dõi các trạng thái toàn cục (`ringingReminderId`, `acknowledgingReminderId`, `missedReminderId`).
  - Khi một nhiệm vụ hoàn thành, chỉ cần `popBackStack` để quay về màn hình reo chuông, sau đó để logic toàn cục xử lý việc chuyển sang màn hình thông báo.
- **Hệ quả:** Khắc phục triệt để lỗi trùng lặp màn hình. Cấu trúc điều hướng trở nên rõ ràng, dễ bảo trì và tránh được các lỗi về Backstack trong tương lai.

### [TDR-039] - Tách module :litever-designsystem và kiến trúc Theme CompositionLocal
- **Ngày thực hiện:** 2026-04-29
- **Trạng thái:** Accepted
- **Bối cảnh:** Hệ thống UI Component và Theme đang nằm lẫn lộn trong `:core:designsystem` của app ReMind, gây khó khăn cho việc tái sử dụng ở các App khác của Litever.
- **Quyết định:** 
  - Trích xuất toàn bộ UI Components dùng chung sang module độc lập `:litever-designsystem`.
  - Triển khai `LiteverColors` hỗ trợ 100% M3 tokens (36 màu) sử dụng `CompositionLocal`.
  - Biến `:core:designsystem` hiện tại thành một lớp Wrapper (Adapter) để giữ tính tương thích ngược.
- **Hệ quả:** Thư viện thiết kế trở nên "sạch", không chứa logic nghiệp vụ. Có thể dễ dàng tích hợp vào bất kỳ App nào chỉ bằng cách truyền bộ màu mới vào `LiteverTheme`.
352: 
353: ### [TDR-040] - Quản lý Âm thanh tập trung (Centralized Audio Management) & Xử lý Audio Focus
354: - **Ngày thực hiện:** 2026-05-01
355: - **Trạng thái:** Accepted
356: - **Bối cảnh:** Lỗi báo thức vẫn kêu khi có cuộc gọi đến do thiếu xử lý Audio Focus và Telephony state. Code phát nhạc bị phân mảnh ở nhiều ViewModel và Service.
357: - **Quyết định:** 
358:   - Tạo component `AudioPlayer` duy nhất trong `:core:common`.
359:   - Tích hợp `OnAudioFocusChangeListener` để tự động dừng nhạc khi có ứng dụng khác (như cuộc gọi) chiếm quyền ưu tiên.
360:   - Kiểm tra `audioManager.mode` để chặn phát nhạc khi đang trong cuộc gọi.
361: - **Hệ quả:** Giải quyết triệt để lỗi xung đột âm thanh với cuộc gọi. Codebase sạch hơn, dễ bảo trì và đảm bảo tính nhất quán giữa báo thức thực tế và preview.

### [TDR-041] - Tích hợp AdMob và Cấu hình Quảng cáo
- **Ngày thực hiện:** 2026-05-03
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần tích hợp Google Mobile Ads (AdMob) để hỗ trợ mô hình kinh doanh của ứng dụng.
- **Quyết định:** 
  - Thêm dependency `play-services-ads` vào Version Catalog và module `:app`.
  - Cấu hình AdMob App ID trong `AndroidManifest.xml`.
  - Khởi tạo SDK trong `ReMindApplication` ngay khi khởi động app.
- **Hệ quả:** Ứng dụng đã sẵn sàng để hiển thị quảng cáo. Cần lưu ý quản lý ID quảng cáo (Ad Unit ID) trong các feature module sau này.

### [TDR-042] - Cơ chế Caching Native Ads và Tuân thủ Chính sách AdMob
- **Ngày thực hiện:** 2026-05-03
- **Trạng thái:** Accepted
- **Bối cảnh:** Trong Jetpack Compose, việc chuyển đổi Tab gây ra tình trạng quảng cáo bị tải lại liên tục (do Composable bị disposed/recomposed), vi phạm chính sách "Invalid Traffic" của AdMob.
- **Quyết định:** 
  - Triển khai `NativeAdManager` dạng Singleton trong `:core:common` để quản lý vòng đời quảng cáo.
  - Thiết lập cơ chế Cache theo `adId` với thời gian sống 5 phút.
  - Sử dụng `EntryPoint` để truy cập Manager từ các thành phần Design System không thuộc Hilt.
- **Hệ quả:** Khắc phục triệt để hiện tượng tải lại quảng cáo khi chuyển tab, tăng tốc độ hiển thị và đảm bảo an toàn cho tài khoản AdMob của nhà phát triển.

### [TDR-043] - Phân tách Định danh PendingIntent cho Báo thức và Snooze
- **Ngày thực hiện:** 2026-05-05
- **Trạng thái:** Accepted (Hotfix)
- **Bối cảnh:** Lỗi nghiêm trọng khiến báo thức lặp lại bị hủy nếu người dùng tắt (dismiss) hoặc tạm dừng (snooze) báo thức trước đó. Nguyên nhân do `PendingIntent` của báo thức chính và snooze dùng chung `RequestCode` và `Action`, dẫn đến việc ghi đè hoặc hủy nhầm lẫn nhau trong `AlarmManager`.
- **Quyết định:** 
  - Tách biệt `Action` cho hai loại: `ACTION_TRIGGER_ALARM` và `ACTION_TRIGGER_SNOOZE`.
  - Sử dụng `RequestCode` riêng biệt bằng cách cộng thêm offset (1 tỷ) cho các bản ghi Snooze.
  - Cập nhật `AlarmSyncManager` để khôi phục song song cả báo thức chính và snooze sau khi reboot.
- **Hệ quả:** Khắc phục triệt để lỗi mất báo thức lặp lại, đảm bảo tính ổn định tối đa cho tính năng cốt lõi.

#### [TDR-045] - Tự động dọn dẹp trạng thái Bỏ qua hết hạn (Auto-cleanup Expired Skips)
- **Ngày thực hiện:** 2026-05-07
- **Trạng thái:** Accepted
- **Bối cảnh:** Khi người dùng chọn "Bỏ qua lần tới", trường `skippedAt` được lưu vào database. Tuy nhiên, sau khi thời điểm đó trôi qua, trạng thái này không tự động biến mất, gây hiểu lầm trên UI (vẫn hiện "Đã bỏ qua" cho một mốc thời gian cũ).
- **Quyết định:** 
  - Thêm helper `isSkipExpired()` vào model `Alarm`.
  - Tích hợp logic tự động kiểm tra và gọi `updateAlarm` để xóa `skippedAt` ngay trong Flow xử lý danh sách báo thức tại `AlarmListViewModel`.
- **Hệ quả:** Đảm bảo dữ liệu DB luôn sạch và UI hiển thị chính xác trạng thái báo thức mà không cần tác động thủ công từ người dùng.
### [TDR-046] - Đồng bộ hóa Logic Bỏ qua trong AlarmSyncManager
- **Ngày thực hiện:** 2026-05-07
- **Trạng thái:** Accepted
- **Bối cảnh:** Lỗi "Missed due to power off" xuất hiện sai lệch khi báo thức đã được người dùng chủ động bỏ qua.
- **Quyết định:** 
  - Thay đổi phương thức tính toán từ `getActualNextOccurrence` sang `getNextOccurrence` trong `AlarmSyncManager`.
  - Tích hợp logic dọn dẹp `skippedAt` hết hạn vào quy trình `sync()`.
- **Hệ quả:** Loại bỏ hoàn toàn các thông báo "bỏ lỡ" giả mạo cho các báo thức đã skip, tăng tính tin cậy cho hệ thống cảnh báo của ứng dụng.

### [TDR-047] - Lựa chọn Nhạc chuông từ Bộ nhớ (Storage Access Framework)
- **Ngày thực hiện:** 2026-05-07
- **Trạng thái:** Accepted
- **Bối cảnh:** Người dùng cần sử dụng nhạc cá nhân làm báo thức mà không muốn cấp quyền truy cập toàn bộ bộ nhớ (READ_EXTERNAL_STORAGE).
- **Quyết định:** Sử dụng Storage Access Framework (SAF) để chọn tệp và duy trì quyền truy cập (persistable URI).
- **Hệ quả:** Tuân thủ tốt các quy định về bảo mật và quyền riêng tư của Android hiện đại.

### [TDR-048] - Tích hợp trạng thái AI và cơ chế Smart Refresh
- **Ngày thực hiện:** 2026-05-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Các gợi ý AI (Weather hint, Reminder hint) cần thời gian xử lý phía server. Người dùng cần biết khi nào AI đang xử lý và app cần tự động cập nhật khi có kết quả.
- **Quyết định:** 
  - Bổ sung trường `aiStatus` ("processing", "completed", "failed") vào model dữ liệu.
  - Triển khai `checkAndRefreshIfProcessing` trong ViewModel để tự động poll dữ liệu sau mỗi 60 giây nếu AI đang ở trạng thái xử lý.
- **Hệ quả:** Trải nghiệm người dùng minh bạch hơn, không cần refresh thủ công để nhận gợi ý AI.

### [TDR-049] - Tự động nhận diện vị trí cho thời tiết
- **Ngày thực hiện:** 2026-05-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Người dùng di chuyển giữa các vùng miền cần thông tin thời tiết chính xác tại vị trí hiện tại mà không muốn nhập tên thành phố thủ công.
- **Quyết định:** Tích hợp logic tự động lấy tọa độ vị trí (thông qua IP hoặc GPS tùy cấu hình API) để truy vấn thời tiết.
- **Hệ quả:** Tăng tính tiện dụng và độ chính xác của tính năng thời tiết.

### [TDR-051] - Thêm cơ chế Gợi ý (Typing for Memory Tip) trong nhiệm vụ Gõ phím
- **Ngày thực hiện:** 2026-05-10
- **Trạng thái:** Accepted
- **Bối cảnh:** Nhiệm vụ gõ phím (Typing Mission) thường bị coi là phiền phức. Cần giải thích giá trị của hành động gõ tay để người dùng cảm thấy có ích hơn.
- **Quyết định:** 
  - Thêm một thẻ "Mẹo" (Tip Card) trong màn hình thêm cụm từ tùy chỉnh.
  - Nội dung nhấn mạnh việc tự tay gõ lại các việc quan trọng giúp ghi nhớ tốt hơn (Cognitive reinforcement).
  - Sử dụng icon `Lightbulb` và bảng màu `secondaryContainer` để làm nổi bật thông điệp tích cực.
- **Hệ quả:** Tăng giá trị UX bằng cách cung cấp lý do khoa học cho tính năng, giảm cảm giác khó chịu khi phải thực hiện nhiệm vụ khó.

### [TDR-050] - Gộp nhóm Lời nhắc (Reminder Grouping UI)
- **Ngày thực hiện:** 2026-05-09
- **Trạng thái:** Accepted
- **Bối cảnh:** Màn hình chính hiển thị nhiều thông tin rời rạc (Thời tiết, Lời nhắc, Sự kiện) gây loãng.
- **Quyết định:** Thiết kế lại component `ReminderCard` để gộp các thông tin liên quan vào một thẻ duy nhất với layout phân tầng.
- **Hệ quả:** Giao diện gọn gàng, tập trung hơn vào các nội dung quan trọng nhất trong ngày.

### [TDR-052] - Khởi tạo Kiến trúc Module Quảng cáo và Remote Config
- **Ngày thực hiện:** 2026-05-11
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần một hệ thống quảng cáo ổn định, dễ dàng điều khiển tắt bật và điều chỉnh tần suất hiển thị từ xa mà không cần phát hành bản cập nhật mới.
- **Quyết định:** 
  - Tách logic quảng cáo thành 2 module `:core:ads:api` (Interface/Model) và `:core:ads:impl` (Google AdMob, Firebase Remote Config).
  - Tích hợp `firebase-config` để tự động kéo các thông số cấu hình về.
- **Hệ quả:** Chuẩn Clean Architecture, giấu kín thư viện bên thứ 3, dễ dàng test và điều khiển linh hoạt qua Remote Config.
459: 
460: ### [TDR-053] - Triển khai Exit Dialog tích hợp Native Ad và Cơ chế thoát App an toàn
461: - **Ngày thực hiện:** 2026-05-12
462: - **Trạng thái:** Accepted
463: - **Bối cảnh:** Cần một cơ chế "Soft Exit" để tăng doanh thu qua quảng cáo Native và tri ân người dùng trước khi họ rời khỏi ứng dụng.
464: - **Quyết định:** 
465:   - Sử dụng `BackHandler` tại màn hình chính (`AlarmListScreen`) để chặn sự kiện thoát mặc định.
466:   - Hiển thị `ExitAppDialog` sử dụng `LiteverAlertDialog` để đồng bộ Design System.
467:   - Tích hợp `NativeAdView` (vị trí `EXIT_NATIVE`) vào giữa nội dung Dialog.
468:   - Triển khai logic `Context.findActivity()` để thực hiện `finish()` Activity chính xác khi bọc trong `ContextWrapper` (do cơ chế Localization).
469: - **Hệ quả:** Tối ưu hóa trải nghiệm người dùng (không gây phiền nhiễu như Interstitial Ad), đảm bảo tính nhất quán của Design System và xử lý thoát App ổn định.

### [TDR-054] - Tái cấu trúc Giao diện Cài đặt và Cơ chế Quảng cáo Thưởng Ủng hộ (Rewarded Ads Supporter)
- **Ngày thực hiện:** 2026-05-18
- **Trạng thái:** Accepted
- **Bối cảnh:** Giao diện cài đặt (tab setting) cần được tái cơ cấu gọn gàng hơn. Đồng thời, người dùng muốn ủng hộ nhà phát triển mà không bắt buộc dùng tiền mặt, qua việc xem video quảng cáo phần thưởng (Rewarded Ad) để tắt quảng cáo trong 24 giờ.
- **Quyết định:**
  - Gom cài đặt thành 3 nhóm: App Settings (ẩn Alarm Settings chưa triển khai), Support & Community (thêm Thẻ Ủng hộ, FAQ Dialog, Rate App, Share App), About & Legal.
  - Tích hợp Google AdMob Rewarded Ad (`SUPPORT_REWARDED`) và cơ chế `RewardedAdSimulatorDialog` đếm ngược 5 giây làm fallback khi ad chưa load kịp.
  - Loại bỏ hoàn toàn quảng cáo Native Ad ở hộp thoại thoát ứng dụng (`ExitAppDialog`), thay bằng nút xem quảng cáo phần thưởng để ủng hộ. Khi hoàn thành xem quảng cáo, hiển thị hộp thoại cảm ơn và giữ người dùng ở lại app.
- **Hệ quả:** Giao diện cài đặt đẹp mắt, sang trọng (premium gradient card). Tăng doanh thu quảng cáo phần thưởng (Rewarded Ad), giảm thiểu sự khó chịu của Native Ad tại exit dialog và giữ chân người dùng trong app tốt hơn.

### [TDR-055] - Tối ưu hóa Giao diện Cài đặt, Hộp thoại Thoát ứng dụng và Lời nhắn chân thành
- **Ngày thực hiện:** 2026-05-19
- **Trạng thái:** Accepted
- **Bối cảnh:** 
  - Thẻ Ủng hộ nhà phát triển dạng Premium Card tuy đẹp nhưng chiếm diện tích lớn và làm giảm tính đồng nhất của danh sách cài đặt. Người dùng mong muốn mục này hiển thị bình thường như các mục cài đặt khác và mở ra Dialog lựa chọn khi nhấn vào.
  - Các hộp thoại thoát app (`ExitAppDialog`) và hộp thoại ủng hộ cần truyền tải lời nhắn chân thành, ấm áp hơn kèm lời chúc tốt đẹp gửi tới người dùng. Đồng thời, nhúng lại quảng cáo Native Ad (`EXIT_NATIVE`) vào trong Hộp thoại thoát app để tối ưu hóa doanh thu hiển thị.
  - Người dùng mong muốn thông điệp kêu gọi ủng hộ ngắn gọn hơn, tránh sử dụng các từ ngữ mang tính cầu khẩn (ví dụ: "please..." hay "bạn có thể tương tác..."), mà thay bằng lời kêu gọi lịch sự, tinh tế: "Ủng hộ chúng tôi nếu bạn thích ứng dụng" hay "Hãy ủng hộ nếu bạn thích".
- **Quyết định:**
  - Loại bỏ hoàn toàn `SupportDeveloperCard` khỏi `LazyColumn` và xóa hàm Composable này để làm sạch mã nguồn.
  - Thêm một `SettingsItem` tiêu chuẩn vào nhóm **Support & Community** với biểu tượng Trái Tim thân thiện (`Icons.Rounded.Favorite`) và dòng mô tả ngắn gọn: "Ủng hộ chúng tôi nếu bạn thích ứng dụng nhé! ❤️".
  - Triển khai hộp thoại `SupportDeveloperDialog` thông qua `LiteverAlertDialog` để hiển thị nội dung kêu gọi và hai nút nhấn **Xem quảng cáo (Watch Ad)** & **Ủng hộ (Donate)** nằm cạnh nhau.
  - Tách biệt hoàn toàn phần phụ đề cài đặt với nội dung hộp thoại: Định nghĩa chuỗi mới `support_dev_dialog_desc` hiển thị **lời cảm ơn đầy đủ, ấm áp và chân thành** bên trong hộp thoại, trong khi dòng phụ đề trong cài đặt vẫn được giữ ngắn gọn và tinh tế.
  - Thay đổi toàn bộ chuỗi ký tự thông điệp ở cả hai Dialog (hỗ trợ đa ngôn ngữ EN/VI) với lời nhắn vô cùng chân thành, biểu thị lòng biết ơn sâu sắc cùng một lời chúc ngày mới tốt lành tràn ngập năng lượng tích cực gửi tới người dùng. Bỏ đi các câu cầu khẩn, sử dụng cấu trúc tự nhiên hơn: "Hãy ủng hộ nếu bạn thích nhé." / "Support us if you like."
  - Nhúng lại quảng cáo Native Ad (`EXIT_NATIVE`) vào trong `ExitAppDialog`, khôi phục lại cách hiển thị nguyên bản từ lịch sử Git bằng cách bọc trong `Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))` nhằm đảm bảo độ căn lề tối ưu.
- **Hệ quả:** Giao diện cài đặt đồng bộ, gọn gàng và tinh tế hơn. Hộp thoại thoát và ủng hộ trở nên cực kỳ thân thiện, truyền tải sự chân thành và tình cảm của đội ngũ phát triển mà không gây cảm giác khó chịu cho người dùng. Việc nhúng lại quảng cáo Native Ad tại hộp thoại thoát giúp duy trì hiệu quả quảng cáo mà không gây phản cảm nhờ có thông điệp ấm áp và chân thành đi kèm.

### [TDR-056] - Đồng bộ Key Cấu hình Quảng cáo, Quản lý Bộ nhớ đệm (Cache) động, và Hiển thị Trạng thái Tải Quảng cáo thời gian thực
- **Ngày thực hiện:** 2026-05-19
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Cấu hình JSON từ xa chuyển đổi key quảng cáo thưởng từ `SUPPORT_REWARD` thành `SUPPORT_REWARDED`.
  - Quản lý bộ nhớ đệm (Cache) cho quảng cáo phần thưởng cần linh hoạt theo tham số `enableCache` và `intervalSeconds` từ JSON: nếu bật thì dùng lại quảng cáo đã tải trong khoảng thời gian hiệu lực, nếu tắt thì luôn tải quảng cáo mới.
  - Tiến trình tải quảng cáo test AdMob trên thiết bị thật (Physical device) trong quá trình debug bị chặn hiển thị do chính sách bảo vệ lưu lượng của AdMob, dẫn đến luôn báo lỗi "Quảng cáo chưa sẵn sàng" khi nhấn nút xem mà không cách nào chạy thử.
  - Người dùng bấm nút "Xem quảng cáo" mong muốn nhận được thông tin phản hồi trực quan ngay lập tức nếu quảng cáo chưa tải xong (hiển thị vòng xoay loading) và tự động mở quảng cáo ngay khi tải xong mà không phải chờ đợi thủ công.
- **Quyết định:**
  - Khôi phục key ánh xạ cấu hình từ xa đồng bộ 100% với JSON: Sử dụng trực tiếp enum key `SUPPORT_REWARDED` thay thế cho `@SerialName("SUPPORT_REWARD")`.
  - Tích hợp logic kiểm tra bộ nhớ đệm linh hoạt trong `AdMobManagerImpl.loadAd`: nếu `enableCache` là `true` và ad vẫn nằm trong khoảng hiệu lực `intervalSeconds`, dùng lại quảng cáo đã tải; ngược lại, bỏ qua cache và gửi yêu cầu tải mới từ AdMob.
  - Ép buộc sử dụng Google official Test Ad Unit IDs (`ca-app-pub-3940256099942544/5224354917` cho rewarded và `ca-app-pub-3940256099942544/2247696110` cho native) đối với **toàn bộ các bản build debug** chạy trên thiết bị thật. Điều này cho phép máy thật tải quảng cáo test thành công 100% để phục vụ phát triển và kiểm thử.
  - Tích hợp cơ chế tải quảng cáo theo yêu cầu (On-Demand Loading): Loại bỏ hoàn toàn khối lệnh tải trước (preload) khi mở màn hình để tối ưu hóa tài nguyên mạng.
  - Triển khai bộ lắng nghe trạng thái quảng cáo thời gian thực (`collectAsState` từ flow `adState`) và hộp thoại Loading tròn xoay (`CircularProgressIndicator` bọc trong `LiteverDialog`). Khi người dùng nhấn nút "Xem quảng cáo", hệ thống lập tức xoay vòng loading và tự động bung màn hình quảng cáo toàn màn hình ngay khi trạng thái chuyển sang `Loaded`.
- **Hệ quả:** Hoàn thành tối ưu hóa 100% cơ chế tải và quản lý quảng cáo AdMob, tăng tính chuyên nghiệp (premium UX), tối ưu hóa tài nguyên mạng nhờ cơ chế tải theo yêu cầu, và cho phép lập trình viên dễ dàng chạy thử quảng cáo test đầy đủ trên thiết bị thật.

### [TDR-057] - Chuẩn hóa Quan sát State và Xử lý Trạng thái Dialog/Sheet
- **Ngày thực hiện:** 2026-05-19
- **Trạng thái:** Accepted
- **Bối cảnh:** 
  - Xuất hiện các cảnh báo "Assigned value is never read" tại các callback đóng Dialog/Sheet.
  - Cách quan sát dữ liệu từ `SavedStateHandle` không đồng nhất giữa các màn hình (dùng `.value` thay vì delegate).
- **Quyết định:** 
  - Sử dụng property delegate `by ... collectAsState()` cho toàn bộ các biến nhận kết quả từ `SavedStateHandle`.
  - Áp dụng pattern kiểm tra trạng thái trước khi thay đổi: `if (showDialog) showDialog = false` để đảm bảo biến State luôn được "đọc" trước khi gán giá trị mới, giúp xóa bỏ cảnh báo của compiler.
  - Đồng bộ hóa logic reset cờ điều hướng (`isNavigatingToConfig`) trong `LifecycleEventObserver`.
- **Hệ quả:** Mã nguồn sạch hơn, không còn cảnh báo vàng từ IDE, đảm bảo tính ổn định của UI State khi điều hướng phức tạp giữa các màn hình Feature.

### [TDR-058] - Chuẩn hóa Bảng màu Tailwind và Tái cấu trúc Icon Cài đặt (UI Refactoring)
- **Ngày thực hiện:** 2026-06-01
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Dự án sử dụng bảng màu Tailwind CSS nhưng bị phân mảnh (một phần trong thư viện, một phần hardcode).
  - Giao diện chỉnh sửa báo thức (`AlarmEditScreen`) có nhiều đoạn code hiển thị icon lặp lại với logic màu sắc chưa tối ưu cho độ tương phản (Accessibility).
- **Quyết định:**
  - Trích xuất toàn bộ bảng màu Tailwind chuẩn vào file `TailwindColors.kt` nội bộ để làm chủ mã nguồn.
  - Tạo helper component `SettingIcon` để gom nhóm logic hiển thị icon.
  - Sử dụng `LiteverFilledTonalIconButton` cho các icon có tương tác (vibration toggle, ringtone preview).
  - Áp dụng quy tắc màu sắc tương phản: Khi background là `primaryContainer`, tint icon bắt buộc phải là `onPrimaryContainer`.
- **Hệ quả:** Mã nguồn gọn gàng, dễ bảo trì. Giao diện nhất quán và đạt chuẩn độ tương phản cao, giúp người dùng dễ dàng nhận diện trạng thái Kích hoạt/Vô hiệu của các thiết lập.

### [TDR-059] - Kích hoạt buildConfig trong module :app
- **Ngày thực hiện:** 2026-06-10
- **Trạng thái:** Accepted
- **Bối cảnh:** Kể từ AGP 8.0+, `buildConfig` bị vô hiệu hóa mặc định để tối ưu tốc độ build. Dự án gặp lỗi `Unresolved reference 'BuildConfig'` trong `ReMindApplication.kt`.
- **Quyết định:** Bật `buildConfig = true` trong block `buildFeatures` của file `app/build.gradle.kts`.
- **Hệ quả:** Cho phép truy cập vào class `BuildConfig` được tạo tự động để kiểm tra các flag như `DEBUG`, giúp thực hiện các logic có điều kiện theo loại build (build variant).

### [TDR-060] - Áp dụng Bảng màu Material 3 đầy đủ (Amber, Brown, Green)
- **Ngày thực hiện:** 2026-06-11
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần áp dụng bộ màu Material 3 đầy đủ bao gồm các token background, surface, outline để đảm bảo giao diện nhất quán trên toàn bộ ứng dụng.
- **Quyết định:**
    - Cập nhật thông số Hex cho toàn bộ các token màu Material 3 (trừ Error, Success, Warning).
    - Sử dụng bảng màu phối hợp: Amber (Primary), Brown (Secondary), và Green (Tertiary).
    - Bổ sung các token `background`, `surface`, `outline`, `inverseSurface`, `surfaceContainer` để tối ưu giao diện Edge-to-Edge.
- **Hệ quả:** Giao diện ứng dụng đạt độ hoàn thiện cao về mặt thẩm mỹ và khả năng tiếp cận (Accessibility), hỗ trợ tốt các thành phần UI phức tạp của Material 3.

### [TDR-062] - Phân tách ApplicationId, App Name và Monochrome Icon cho môi trường Debug
- **Ngày thực hiện:** 2026-09-21
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần cài đặt song song hai phiên bản Debug và Release trên cùng thiết bị để phục vụ việc kiểm thử, đối soát và tránh ghi đè dữ liệu ứng dụng. Đồng thời cần nhận diện trực quan nhanh chóng giữa bản Debug và Release trên màn hình launcher, cũng như xác minh tính an toàn và tương thích của hệ thống Ads (AdMob).
- **Quyết định:**
  - Cấu hình `applicationIdSuffix = ".debug"` và `versionNameSuffix = "-debug"` trong block `buildTypes.debug` của `app/build.gradle.kts`.
  - Thiết lập source set `app/src/debug/res`:
    - `values/strings.xml` và `values-vi/strings.xml`: Ghi đè `app_name` thành `ReMind (Debug)`.
    - `mipmap-anydpi-v26/ic_launcher.xml` và `ic_launcher_round.xml`: Sử dụng `@drawable/ic_launcher_monochrome` cho trường `foreground`, biến icon bản debug thành phong cách Monochrome tối giản.
  - Bổ sung cấu hình client `vn.io.litever.remind.debug` vào `app/google-services.json` để Google Services Gradle plugin nhận diện chính xác và tránh lỗi mismatch package khi sync/build debug.
  - Rà soát hệ thống Ads: `AdMobManagerImpl` đã có cơ chế tự động chuyển đổi sang Google Official Test Ad Unit ID khi ở môi trường Debug (`FLAG_DEBUGGABLE`), do đó hoàn toàn an toàn và sử dụng chung hạ tầng AdMob mà không gây vi phạm chính sách của Google.
- **Hệ quả:** Cho phép cài đặt song song cả 2 bản trên máy test mà không bị ghi đè, nhận diện tức thì qua icon Monochrome và tên "ReMind (Debug)", hệ thống build tự động nhận diện đúng client Firebase và Ads test chạy trơn tru.

---

## 🛠 Changelog (Tính năng mới)

### [2026-08-18]
- **Feature:** Nâng cấp nhiệm vụ Gõ phím với giao diện nhập liệu trực tiếp (Immersive Input), gộp phần hiển thị và phần nhập thành một khối duy nhất giúp tăng độ tập trung.

### [2026-06-11]
- **Design System:** Áp dụng bảng màu Material 3 đầy đủ (30+ tokens) mang phong cách Amber, Brown, Green tự nhiên và ấm áp.
- **Design System:** Loại bỏ phụ thuộc vào `TailwindColors` và chuyển đổi Logo colors sang mã màu hex trực tiếp.

### [2026-06-10]
- **Infra:** Khắc phục lỗi `Unresolved reference BuildConfig` bằng cách kích hoạt `buildConfig` trong module `:app`.

### [2026-06-01]
- **Design System:** Chuẩn hóa bảng màu Tailwind CSS toàn diện (`TailwindColors.kt`).
- **Refactor:** Tái cấu trúc màn hình Chỉnh sửa báo thức, gom nhóm các icon thiết lập vào component `SettingIcon` dùng chung.
- **UI/UX:** Tối ưu hóa độ tương phản màu sắc cho các icon trạng thái (PrimaryContainer -> OnPrimaryContainer).
- **UX:** Nâng cấp trải nghiệm tương tác nút Rung và Nghe thử nhạc chuông bằng `LiteverFilledTonalIconButton`.

### [2026-05-30] - RELEASE v1.1.6 (Build 9)
- **UI:** Thêm hiệu ứng chuyển động mượt mà khi thay đổi vị trí báo thức trong danh sách sử dụng `Modifier.animateItem()`.
- **Bug Fix:** Khắc phục lỗi animation click (ripple) và dải màu trang trí bị lem ra ngoài bo góc của `AlarmCard`.
- **Bug Fix:** Sửa lỗi đa ngôn ngữ trong Dialog bằng cách resolve `stringResource` sớm.
- **Refactor:** Di chuyển logic xử lý `changelog.json` sang ViewModel trong màn hình Lịch sử cập nhật.
- **Documentation:** Cập nhật Learning Journal về các kỹ thuật clipping nội dung và animation trong danh sách.

### [2026-05-20] - RELEASE v1.1.5 (Build 8)
- **Feature:** Tích hợp Rewarded Ads để tạm thời tắt quảng cáo trong 24 giờ.
- **UX:** Tái cấu trúc màn hình Cài đặt và nâng cấp giao diện nút bấm.
- **Bug Fix:** Khắc phục lỗi Navigation crash khi khởi động ứng dụng.
- **Bug Fix:** Sửa lỗi màn hình chuông không sáng lại sau khi bấm Báo lại (Snooze).
- **Architecture:** Cập nhật nội dung thông báo và logic Audio Focus để chuông kêu ổn định hơn.

### [2026-05-19]
- **Refactor:** Chuẩn hóa cơ chế quan sát `SavedStateHandle` và xử lý triệt để cảnh báo "Assigned value is never read" trên toàn bộ module `:features:alarms`.
- **UI/UX:** Đồng bộ hóa trải nghiệm đóng/mở Dialog và BottomSheet giữa màn hình Danh sách và Chỉnh sửa báo thức.
- **Feature:** Chuyển đổi Thẻ Ủng hộ nhà phát triển thành mục danh sách (`SettingsItem`) tiêu chuẩn dưới nhóm Hỗ trợ với biểu tượng Trái tim (`Icons.Rounded.Favorite`).
- **UI/UX:** Tích hợp Hộp thoại `SupportDeveloperDialog` sử dụng `LiteverAlertDialog` hiển thị mô tả ngắn và hai lựa chọn **Xem quảng cáo (Watch Ad)** & **Ủng hộ (Donate)** theo chuẩn Design System để tối giản giao diện cài đặt và nâng cao tính thẩm mỹ.
- **UI/UX:** Bỏ nút "Đóng" (Close button) khỏi Hộp thoại Ủng hộ nhà phát triển trong mục Cài đặt bằng cách chuyển đổi từ `LiteverAlertDialog` sang dùng `LiteverDialog` với nút confirm rỗng (`confirmButton = {}`) để tuân thủ thiết kế tối giản, gọn gàng.
- **UI/UX:** Nhúng lại quảng cáo Native Ad (`EXIT_NATIVE`) vào trong Hộp thoại thoát ứng dụng (`ExitAppDialog`), khôi phục chính xác cấu trúc Box container từ lịch sử Git.
- **UI/UX:** Loại bỏ hoàn toàn nút xem quảng cáo ủng hộ khỏi Hộp thoại thoát ứng dụng (`ExitAppDialog`).
- **UI/UX:** Loại bỏ hoàn toàn cơ chế preload quảng cáo khi mở màn hình, chuyển sang cơ chế tải theo yêu cầu (On-Demand Loading) chỉ khi người dùng click xem.
- **UI/UX:** Triển khai cơ chế theo dõi trạng thái tải quảng cáo thời gian thực, tự động kích hoạt hộp thoại loading xoay tròn (`CircularProgressIndicator`) và mở quảng cáo ngay khi sẵn sàng, nâng cấp trải nghiệm mượt mà và cao cấp.
- **Ad-Free Control:** Liên kết trạng thái ủng hộ (`isAdFreeActive`) vào `ExitAppDialog` để tự động hóa giao diện: ẩn toàn bộ quảng cáo Native Ad và loại bỏ thông điệp kêu gọi ủng hộ khi người dùng đang trong trạng thái không quảng cáo.
- **Ad Configuration:** Cấu trúc lại và thiết lập ánh xạ tự động thông qua thẻ `@SerialName("SUPPORT_REWARD")` trên Enum `AdPlacement.SUPPORT_REWARDED`, giúp tương thích ngược hoàn toàn với key cấu hình JSON mới mà không cần chỉnh sửa/refactor diện rộng trong mã nguồn Kotlin.
- **Ad Configuration:** Đồng bộ hóa hoàn toàn key remote config với JSON dạng `SUPPORT_REWARDED` và hỗ trợ cấu hình cache động (`enableCache` và `intervalSeconds`).
- **Development & Testing:** Ép buộc sử dụng các ID quảng cáo thử nghiệm chính thức của Google cho tất cả các bản debug chạy trên thiết bị thật, loại bỏ hoàn toàn lỗi chặn tải quảng cáo và báo "Quảng cáo chưa sẵn sàng" khi phát triển.
- **Supporter Reward Tuning:** Triển khai cơ chế thưởng thời gian không quảng cáo động dựa trên kiểu build (Build Variant): ở phiên bản Debug, thời gian miễn quảng cáo sẽ là **30 giây** thay vì 1 ngày (24 giờ) để hỗ trợ test nhanh và ổn định, trong khi bản Release vẫn giữ nguyên 24 giờ.
- **Localization:** Tách biệt chuỗi mô tả Cài đặt (ngắn gọn) và nội dung Hộp thoại Ủng hộ nhà phát triển (lời cảm ơn chân thành đầy đủ), đồng thời tối ưu hóa thông điệp của cả hai Dialog loại bỏ các từ ngữ mang tính cầu khẩn gây phiền toái.

### [2026-05-18]
- **Feature:** Tái cấu trúc toàn diện màn hình Cài đặt thành 3 nhóm trực quan: App Settings, Support & Community, About & Legal.
- **UI/UX:** Thêm Thẻ Ủng hộ nhà phát triển (Support Developer Card) cực kỳ sang trọng với hiệu ứng Premium Gradient.
- **Feature:** Triển khai cơ chế Quảng cáo Thưởng Ủng hộ (Rewarded Ads Supporter) tích hợp Google AdMob và trình giả lập Ad Simulator (5s countdown) làm fallback. Xem quảng cáo sẽ tạm tắt toàn bộ quảng cáo trong ứng dụng trong vòng 24 giờ.
- **UI/UX:** Loại bỏ Native Ad tại Hộp thoại thoát app (`ExitAppDialog`), thay thế bằng nút xem quảng cáo phần thưởng để ủng hộ. Hoàn thành xem sẽ hiện Dialog cảm ơn và giữ người dùng ở lại ứng dụng.
- **Feature:** Thêm hộp thoại FAQ thông tin tính năng tương lai và cơ chế Rate App/Share App.

### [2026-05-12] - RELEASE v1.1.4 (Build 7)
- **Feature:** Triển khai Hộp thoại thoát ứng dụng (Exit Dialog) tích hợp quảng cáo Native Ad và cơ chế thoát an toàn.
- **Architecture:** Mô-đun hóa hệ thống quảng cáo và tích hợp Firebase Remote Config.
- **UX:** Cải tiến quy trình tạo báo thức với mẫu Draft và phát hiện thay đổi (Change detection).
- **UX:** Hiện đại hóa giao diện Nhiệm vụ Gõ phím, thêm mẹo ghi nhớ "Typing for Memory".
- **UI:** Thêm component `ReMindLoadingIconButton` và tối ưu hóa hiệu ứng tải.
- **Bug Fix:** Khắc phục lỗi trùng lặp nhiệm vụ khi nhân bản báo thức.
- **Refactor:** Cập nhật `litever-designsystem` lên v1.0.4 và chuẩn hóa đa ngôn ngữ cho các hộp thoại.

### [2026-05-12]
- **Feature:** Triển khai Hộp thoại thoát ứng dụng (Exit Dialog) tích hợp quảng cáo Native Ad.
- **UX:** Kêu gọi người dùng ủng hộ nhà phát triển trước khi thoát app một cách tinh tế.
- **Architecture:** Tách biệt vị trí quảng cáo `EXIT_NATIVE` để điều khiển độc lập qua Remote Config.
- **Bug Fix:** Khắc phục lỗi không thoát được app khi nút "Thoát ngay" bị bọc trong ContextWrapper của đa ngôn ngữ.

### [2026-05-10]
- **UX:** Thêm gợi ý "Typing for Memory" trong nhiệm vụ Gõ phím để khuyến khích người dùng ghi nhớ việc cần làm.
- **UI:** Thêm các Preview cho màn hình thêm cụm từ tùy chỉnh trong nhiệm vụ Gõ phím.
- **Documentation:** Cập nhật TDR-051 ghi lại quyết định UX về cơ chế ghi nhớ.

### [2026-05-09] - RELEASE v1.1.3 (Build 6)
- **Feature:** Tích hợp trạng thái AI thời gian thực và cơ chế tự động cập nhật gợi ý thông minh.
- **Feature:** Tự động nhận diện vị trí cho dự báo thời tiết.
- **Feature:** Thêm màn hình "Ghi công & Tài nguyên" (Attributions) để vinh danh Storyset và cộng đồng.
- **UI:** Gộp nhóm các lời nhắc vào một thẻ duy nhất giúp tăng sự tập trung.
- **UX:** Tái cấu trúc mục Giới thiệu và Cài đặt, tối ưu hóa giao diện Top Bar.
- **Performance:** Giảm thời gian cache quảng cáo để cập nhật nội dung nhanh hơn.

### [2026-05-08] - RELEASE v1.1.2 (Build 5)
- **Feature:** Hỗ trợ chọn nhạc chuông tự chọn từ bộ nhớ thiết bị qua SAF.
- **Localization:** Đa ngôn ngữ hóa màn hình Lịch sử cập nhật (Update History). Tự động hiển thị tiếng Anh cho các ngôn ngữ khác tiếng Việt.
- **UX:** Cập nhật `changelog.json` với nội dung chi tiết cho các phiên bản 1.1.1 và 1.1.2.
- **Stability:** Khắc phục lỗi báo thức bị đánh dấu "Missed due to power off" sai lệch khi đã được người dùng bỏ qua (Skip once).
- **Cleanup:** Tự động dọn dẹp trạng thái "Bỏ qua lần tới" đã hết hạn.

### [2026-05-07]
- **Feature:** Hỗ trợ chọn nhạc chuông tự chọn từ bộ nhớ thiết bị.
- **Privacy:** Sử dụng Storage Access Framework để không cần xin quyền READ_EXTERNAL_STORAGE.
- **Stability:** Triển khai `takePersistableUriPermission` giúp nhạc chuông tự chọn hoạt động sau khi reboot.
- **Bug Fix:** Khắc phục lỗi báo thức bị đánh dấu "Missed due to power off" sai lệch khi đã được người dùng bỏ qua (Skip once).
- **Data Integrity:** Cập nhật `AlarmSyncManager` sử dụng `getNextOccurrence()` và dọn dẹp `skippedAt` hết hạn.
- **Automation:** Logic dọn dẹp được tích hợp ngầm vào `AlarmListViewModel` thông qua cơ chế phản ứng của Flow.
- **Testing:** Bổ sung Unit Test `AlarmSyncManagerTest` để kiểm chứng logic đồng bộ.

### [2026-05-05] - HOTFIX v1.1.1 (Build 4)
- **Critical Fix:** Khắc phục lỗi báo thức lặp lại không đặt lịch cho ngày tiếp theo sau khi đã reo hoặc bị tạm dừng.
- **Stability:** Tách biệt định danh (Action & RequestCode) giữa báo thức chính và báo thức Snooze để tránh xung đột trong `AlarmManager`.
- **Reliability:** Cập nhật `AlarmSyncManager` để khôi phục đồng thời cả báo thức chính và các lịch snooze còn hiệu lực sau khi khởi động lại máy.
- **Documentation:** Cập nhật TDR-043 và hồ sơ kỹ thuật liên quan đến cơ chế đặt lịch.

### [2026-05-03] - RELEASE v1.1.0 (Build 3)
- **Consolidation:** Gộp toàn bộ các thay đổi từ tag v1.0.0 thành bản phát hành chính thức 1.1.0.
- **Versioning:** Cập nhật versionCode lên 3 và versionName lên 1.1.0 trong Version Catalog.
- **Documentation:** Đồng bộ hóa CHANGELOG.md và changelog.json (Assets) để chuẩn bị phát hành.
- **Legal:** Cập nhật Chính sách bảo mật (`privacy.html`) để khai báo việc sử dụng Google AdMob, đảm bảo tuân thủ Google Play.
- **Verification:** Kiểm tra tính toàn vẹn của bản build và các thành phần phụ thuộc.

### [2026-05-03] - Pre-release updates
- **AdMob Optimization:** Triển khai cơ chế Singleton AdManager giúp cache quảng cáo Native, ngăn chặn việc tải lại liên tục khi chuyển Tab, đảm bảo tuân thủ chính sách AdMob.
- **Native Ad Implementation:** Triển khai hiển thị quảng cáo Native thực tế sử dụng AdLoader và AndroidView trong Jetpack Compose.
- **AdMob Integration:** Tích hợp thành công Google Mobile Ads SDK (AdMob) với App ID được cấu hình sẵn.
- **Infra:** Cập nhật Version Catalog và logic khởi tạo SDK trong `ReMindApplication`.
- **UI:** Nâng cấp `NativeAdView` từ placeholder sang giao diện quảng cáo thực tế với đầy đủ Headline, Body, Icon và Call-to-action.


### [2026-05-01]

- **Fix:** Giải quyết lỗi báo thức vẫn kêu khi có cuộc gọi đến bằng cách triển khai cơ chế Audio Focus và kiểm tra trạng thái Telephony.
- **Architecture:** Tập trung hóa logic phát âm thanh vào component `AudioPlayer` dùng chung.
- **Refactor:** Loại bỏ mã nguồn phát nhạc trùng lặp tại `AlarmService` và các ViewModel (`RingtoneSelection`, `AlarmPreview`, `AlarmEdit`).

### [2026-04-29]
- **Modularization:** Tách module `:litever-designsystem` thành thư viện dùng chung cho mọi App của Litever.
- **Theming:** Nâng cấp hệ thống Theme hỗ trợ 36 tokens Material 3 (bao gồm `surfaceContainer`, `inverseSurface`, v.v.).
- **UI:** Di chuyển và chuẩn hóa tên các component (`LiteverButton`, `LiteverTextField`, `LiteverSwitch`, `LiteverLogo`).
- **DevEx:** Cập nhật Workflow và Skill của Agent để tự động hóa việc khởi tạo Design System cho dự án mới thông qua cơ chế "Copy & Adapt".
- **Documentation:** Đồng bộ hóa LOGWORK và LEARNING_JOURNAL theo chuẩn kiến trúc mới.

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

### [TDR-056] - Di chuyển Changelog thành tài liệu web nội bộ
- **Ngày thực hiện:** 2026-07-16
- **Trạng thái:** Accepted
- **Bối cảnh:** Việc nhúng file changelog.json vào assets làm tăng kích thước bản build và khó cập nhật nội dung tức thời.
- **Quyết định:**
  - Xóa file changelog.json khỏi assets.
  - Tách thành en.json và i.json đặt tại thư mục docs/changelog.
  - Mở changelog thông qua web URL (sử dụng Custom Tabs) tương tự Term & Privacy, kèm theo tham số ?lang= để tự động chuyển ngôn ngữ.
- **Hệ quả:** Giảm nhẹ dung lượng ứng dụng, đồng nhất cơ chế hiển thị tài liệu điều khoản và tăng tính linh hoạt khi cập nhật phiên bản.

### [TDR-057] - Nâng cấp thư viện Litever Design System lên 1.1.6 & Chuẩn hóa ReMindTopAppBar
- **Ngày thực hiện:** 2026-09-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Thư viện `litever-designsystem` cập nhật lên version `1.1.6` mang lại nhiều cải tiến về Material 3 (hỗ trợ các biến thể TopAppBar, điều chỉnh TextField signature với `showClearButton`, thêm `LiteverBottomBar`, và tối ưu Lint). Việc cập nhật chữ ký của `LiteverTopAppBar` (tách thành các overload chuẩn hóa M3 và `onNavigationClick`) đòi hỏi giải pháp nhất quán trong toàn bộ app.
- **Quyết định:**
  - Nâng cấp dependency `liteverDesignsystem` trong `libs.versions.toml` lên `1.1.6`.
  - Tạo component wrapper `ReMindTopAppBar` trong `:core:designsystem` kế thừa `LiteverTopAppBar` để đóng gói tiện ích điều hướng quay lại (`onBackClick`) chuẩn hóa theo thiết kế Litever.
  - Thay thế trực tiếp các tham số TextField lỗi thời (`onClearClick` chuyển thành `showClearButton = true`) trong `AlarmEditScreen`.
- **Hệ quả:** Đồng bộ toàn diện với bản phát hành mới nhất của `litever-designsystem`, code tuân thủ nghiêm ngặt nguyên tắc Single Source of Truth cho UI qua `:core:designsystem`.

### [TDR-058] - Đồng bộ và Khởi tạo Design System ReMind Android lên Stitch
- **Ngày thực hiện:** 2026-09-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Cần đồng bộ toàn bộ ngôn ngữ thiết kế, Design Tokens (bảng màu Terracotta/Warm amber, typography Nunito Sans, hình khối Bo tròn 8dp, spacing scale và các custom component wrapper) của ReMind Android lên nền tảng thiết kế Stitch để phục vụ việc thiết kế và sinh màn hình UI đồng nhất.
- **Quyết định:**
  - Khởi tạo Stitch Project độc lập `ReMind Android` (ID: `15621181851926747824`).
  - Trích xuất toàn bộ Color tokens từ `:core:designsystem` (`remindLightColors`, `remindDarkColors`) với tông chính `#8C4E28`, Typography `NUNITO_SANS`, Roundness `ROUND_EIGHT`, Spacing scale và biên soạn tài liệu `designMd` chứa đặc tả token, core components.
  - Khởi tạo và gắn kết Design System `ReMind Design System` (Asset ID: `16615139910500015624`) vào Project thông qua Stitch MCP tools (`create_design_system`, `update_design_system`).
- **Hệ quả:** Stitch Project hiện tại phản ánh chính xác 100% token thực tế của ứng dụng ReMind Android, sẵn sàng cho việc sinh và cập nhật giao diện mới một cách tự động và nhất quán.

### [TDR-059] - Đồng bộ toàn bộ các Màn hình Ứng dụng ReMind Android lên Stitch
- **Ngày thực hiện:** 2026-09-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Sau khi khởi tạo Design System trên Stitch, cần đồng bộ toàn diện tất cả các màn hình hiện tại của ReMind Android lên Stitch canvas nhằm tạo cơ sở thiết kế visual, review UX và đồng nhất quy chuẩn thiết kế.
- **Quyết định:**
  - Sử dụng công cụ `generate_screen_from_text` (kết hợp `ReMind Design System` asset) đồng bộ tuần tự 10 màn hình cốt lõi:
    1. `AlarmListScreen` (Tab Báo thức): Logo ReMind, thẻ đếm ngược, danh sách báo thức, FAB.
    2. `TodayScreen` (Tab Hôm nay): Thẻ thời tiết AI WeatherInfoView, trích dẫn động lực TodayQuoteView.
    3. `SettingsScreen` (Tab Cài đặt): Cấu trúc nhóm LiteverSettingsGroup, các liên kết cài đặt.
    4. `AlarmEditScreen` (Thêm / Sửa báo thức): Trình chọn giờ số lớn, chip lặp lại T2..CN, âm lượng, mission và ReMindBottomBar.
    5. `AlarmRingingScreen` (Màn hình chuông reo): Edge-to-edge, đồng hồ số cực lớn, auto-silence timer, nút Dismiss & Snooze.
    6. `PermissionSettingsScreen` (Cài đặt quyền): Trạng thái Exact Alarm, Notification, Overlay, Battery optimization.
    7. `GeneralSettingsScreen` (Cài đặt chung): Định dạng giờ 12h/24h, Sáng/Tối, bảng màu ReMind Warm Terracotta vs Dynamic.
    8. `AttributionsScreen` (Lời cảm ơn & Tài nguyên): Ghi nhận OSS Libraries, Storyset illustrations, WeatherAPI.
    9. `SnoozeSettingsScreen` (Cài đặt hoãn): Switch bật/tắt, interval và số lần lặp lại.
    10. `MissionRingingScreen` (Thử thách thức giấc): Giao diện gõ chữ với tiến trình 3 bước, trích dẫn động lực và đếm ngược.
- **Hệ quả:** Toàn bộ 10 màn hình đã hiện diện đầy đủ trên Stitch Canvas của project `ReMind Android`, tuân thủ đồng nhất 100% token thiết kế Material 3 ấm áp (Terracotta).

### [TDR-060] - Tinh chỉnh Màn hình theo Component Gốc Litever Design System & Bổ sung Module Mission lên Stitch
- **Ngày thực hiện:** 2026-09-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Các màn hình đã sinh ban đầu cần được đối chiếu trực tiếp với mã nguồn chi tiết của thư viện `:litever-designsystem` và các custom component wrapper (`ReMindTopAppBar`, `ReMindBottomBar`, `LiteverSettingsGroup`, `LiteverSettingsItem`, `LiteverSingleChoiceSegmentedButtonRow`) để đảm bảo tính chuẩn xác tuyệt đối về mặt cấu trúc và kiểu dáng. Đồng thời cần bổ sung đầy đủ các giao diện cấu hình thử thách trong module `:features:mission`.
- **Quyết định:**
  - Rà soát trực tiếp mã nguồn thư viện `:litever-designsystem` từ workspace local.
  - Sử dụng `edit_screens` để tinh chỉnh các màn hình hiện có (`AlarmListScreen`, `AlarmEditScreen`, `SettingsScreen`, `GeneralSettingsScreen`, `PermissionSettingsScreen`, `AttributionsScreen`, `SnoozeSettingsScreen`, `MissionRingingScreen`) khớp từng thuộc tính component (ReMindLogo, nút Hủy/Lưu ở bottom bar, RadioButton, SegmentedButton).
  - Bổ sung 3 màn hình còn lại của module Mission:
    1. `TypingMissionConfigScreen` (Cấu hình nhiệm vụ gõ phím: Stepper số lần lặp, RadioButton chế độ, thẻ xem trước cụm từ).
    2. `PhraseSelectionScreen` (Màn hình chọn cụm từ: Tabs Động lực/Cơ bản/Của tôi, Checkboxes, Thêm cụm từ mới).
    3. `MemoryGameConfigScreen` (Cấu hình trò chơi ô nhớ: Stepper số vòng, chọn độ khó lưới 3x3 đến 7x7 và bảng xem trước).
- **Hệ quả:** Stitch Project `ReMind Android` hiện phản ánh đầy đủ và chuẩn xác 100% tất cả các màn hình và thành phần component từ codebase thực tế.

### [TDR-061] - Nâng cấp Thư viện LiteVer Design System 2.0.0 và Refactor toàn diện sang Native Material 3 & LiteVer Defaults
- **Ngày thực hiện:** 2026-09-11
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Thư viện `litever-designsystem` phát hành phiên bản 2.0.0 với triết lý kiến trúc tinh gọn (Lean Architecture): loại bỏ hoàn toàn 24 pass-through wrapper components (như `LiteverButton`, `LiteverScaffold`, `LiteverTopAppBar`, `LiteverTextField`, `LiteverCard`, `LiteverCircularProgressIndicator`, `LiteverIconButton`, `LiteverSwitch`, v.v.). Các wrapper này trước đây tạo ra tầng trung gian không cần thiết, làm giới hạn khả năng tiếp cận các API và tham số phong phú của Jetpack Compose Material 3 (M3), đồng thời gây khó khăn khi nâng cấp Jetpack Compose BOM.
  - Thay vì duy trì pass-through wrappers, Litever 2.0.0 cung cấp mô hình Component Defaults (`LiteVerButtonDefaults`, `LiteVerTextFieldDefaults`) kết hợp cùng hệ thống Design Tokens phong phú (`LiteverTheme.spacing`, `LiteverTheme.shapes`, semantic colors `warning`/`onWarning`, `success`/`onSuccess`).
  - Dự án `remind-android` cần liên kết trực tiếp với mã nguồn cục bộ của thư viện thông qua cơ chế `includeBuild("../litever-designsystem")` để vừa thử nghiệm vừa phát triển song song. Đồng thời cần refactor toàn bộ các màn hình và thành phần UI trong tất cả các module (`:core:designsystem`, `:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, `:app`) sang Material 3 gốc kết hợp LiteVer Defaults và Spacing Tokens.
- **Quyết định:**
  1. **Cấu hình Composite Build & Dependency Substitution:**
     - Khai báo `includeBuild("../litever-designsystem")` trong `settings.gradle.kts` kèm theo khối `dependencySubstitution` thay thế artifact `com.github.toantd2000:litever-designsystem` bằng dự án cục bộ `:designsystem`.
     - Cập nhật phiên bản thư viện `liteverDesignsystem` lên `2.0.0` trong `gradle/libs.versions.toml`.
  2. **Tái cấu trúc `:core:designsystem` thành Adapter chuẩn mực:**
     - Đồng bộ `ReMindTheme` với `LiteverTheme`, bổ sung đầy đủ các token màu semantic (`warning`, `onWarning`, `success`, `onSuccess`).
     - Tái cấu trúc các custom wrappers chuyên biệt của ReMind (`ReMindTopAppBar`, `ReMindAlertDialog`, `ReMindBottomBar`, `ReMindLoadingIconButton`, `ReMindSettingIcon`) trực tiếp trên nền tảng Material 3 composables (`TopAppBar`, `AlertDialog`, `Surface`, `IconButton`, v.v.) kết hợp `LiteVerButtonDefaults`.
     - Tái tạo các component đặc thù bị thiếu ở v2.0.0 (`ReMindSettingsGroup`, `ReMindSettingsItem`, `ReMindTimePickerDialog`) trong `:core:designsystem` để bảo đảm nguyên tắc Single Source of Truth cho UI.
     - Refactor các màn hình dùng chung trong core (`RingtoneSelectionScreen`, `SnoozeSettingsScreen`, `WeatherInfoView`, `TodayQuoteView`, `MissionSelectionBottomSheet`) sang Material 3 và `LiteverTheme.spacing`.
  3. **Refactor toàn diện các Feature Modules sang M3 & LiteVer Defaults:**
     - `:features:alarms`: Refactor `AlarmListScreen`, `AlarmEditScreen`, `AlarmRingingScreen`, `AlarmMessageScreen`, `AlarmCard`, `ExitAppDialog`, `MissedAlarmDialog`, `PermissionWarningBanner`.
     - `:features:settings`: Refactor `SettingsScreen`, `AlarmSettingsScreen`, `GeneralSettingsScreen`, `PermissionSettingsScreen`, `AttributionsScreen`, `LicensesScreen`.
     - `:features:mission`: Refactor `TypingMissionConfigScreen`, `PhraseSelectionScreen`, `MemoryGameConfigScreen`, `MissionRingingScreen` cùng các content components (`MathMissionContent`, `MemoryTilesMissionContent`, `MissionCompleteContent`, `TypingMissionContent`).
     - `:features:today`: Refactor `TodayScreen` và `LocationSearchScreen`.
     - `:app`: Refactor `MainActivity` scaffold, navigation host và window insets.
  4. **Quy chuẩn Hóa Spacing Tokens & Window Insets:**
     - Xóa bỏ 100% các giá trị khoảng cách hardcode `.dp` trong layout, thay thế bằng `LiteverTheme.spacing` (`tiny`, `extraSmall`, `small`, `smallMedium`, `medium`, `large`, `extraLarge`, v.v.). Các giá trị `1.dp` duy nhất được giữ lại là độ dày đường viền kỹ thuật (`BorderStroke`).
     - Root `Scaffold` tại `:app` đảm nhiệm xử lý system bars insets; các `Scaffold` con tại các màn hình con cấu hình `contentWindowInsets = WindowInsets(0, 0, 0, 0)` để triệt tiêu lỗi double-padding (cộng dồn insets).
- **Hệ quả:**
  - **Kiến trúc tinh gọn (Lean Architecture):** Loại bỏ hoàn toàn sự phụ thuộc vào các pass-through wrapper mong manh, mã nguồn Compose trực diện, dễ đọc, dễ tiếp cận các API và modifier mới nhất của Google Material 3.
  - **Tính nhất quán Visual:** Toàn bộ khoảng cách, màu sắc và kiểu dáng nút/input đều được kiểm soát chặt chẽ bởi Design System tokens thông qua `LiteverTheme` và `LiteVerButtonDefaults`.
  - **Quy trình phát triển liền mạch:** Composite build giúp phản ánh ngay lập tức các thay đổi từ thư viện `litever-designsystem` mà không cần chu kỳ release/publish phức tạp.
  - **Chất lượng và Độ ổn định:** Tất cả các module biên dịch sạch sẽ 100%, bổ sung bộ unit test logic UI toàn diện cho mission và today features bảo đảm không xảy ra hồi quy.

### [TDR-062] - Nâng cấp Thư viện LiteVer Design System 2.1.0 và Chuyển đổi sang Bộ Thành phần Opinionated Lv* (LvButton, LvIconButton, LvTextField, LvAlertDialog, LvChip, LvSnackbar)
- **Ngày thực hiện:** 2026-09-12
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Sau khi hoàn thành Phase 7 (chuyển dịch từ 24 wrappers cũ sang Material 3 gốc kết hợp Component Defaults ở LiteVer v2.0.0), việc phải cấu hình lặp đi lặp lại các tham số `colors = LiteVerButtonDefaults.primaryColors()`, `shape = LiteVerButtonDefaults.shape`, `colors = LiteVerTextFieldDefaults.outlinedColors()`, `border = ...` tại từng Composable trong hàng chục màn hình vẫn tạo ra lượng boilerplate code đáng kể, đồng thời tiềm ẩn rủi ro thiếu nhất quán về ngữ nghĩa giao diện (semantic intents) và kiểu dáng bo góc squircle.
  - Thư viện `litever-designsystem` phát hành phiên bản `2.1.0` với hai cải tiến đột phá:
    1. **Tái cơ cấu Package theo Domain:** Tách các thành phần từ package phẳng `vn.io.litever.designsystem.components.*` sang các sub-package chuyên biệt: `components.button.*`, `components.textfield.*`, `components.chip.*`, `components.dialog.*`, `components.snackbar.*`, và `components.core.*`.
    2. **Bộ thành phần Opinionated thế hệ mới `Lv*`:** Giới thiệu `LvButton`, `LvIconButton`, `LvTextField`, `LvAlertDialog`, `LvChip`, `LvSnackbarHost` / `LvSnackbar` được định hình sẵn chuẩn bo góc squircle (6.dp cho control nhỏ, 10.dp cho dialog), tích hợp chặt chẽ với hệ thống màu ngữ nghĩa `LvSemantic` (`Primary`, `Secondary`, `Tertiary`, `Neutral`, `Success`, `Destructive`, `Warning`) và các biến thể kiểu dáng (`LvButtonType`, `LvTextFieldType`).
    3. **Mở rộng Semantic Tokens:** Bổ sung các tokens `neutral`, `onNeutral`, `neutralContainer`, `onNeutralContainer` trong `LiteverTheme.colors`.
  - Dự án `remind-android` cần căn chỉnh lại toàn bộ các import bị gãy do việc tách package, đồng thời chuyển đổi mã nguồn UI trên toàn bộ các modules (`:core:designsystem`, `:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, `:app`) sang các opinionated components thế hệ mới của v2.1.0.
- **Quyết định:**
  1. **Version Catalog & Package Import Alignment:** Cập nhật `liteverDesignsystem = "2.1.0"` trong `gradle/libs.versions.toml`. Chuyển đổi toàn bộ import sang các package chuyên biệt (`components.button.LiteVerButtonDefaults`, `components.textfield.LiteVerTextFieldDefaults`, `components.button.LvButton`, v.v.).
  2. **Mở rộng Semantic Colors tại `:core:designsystem`:** Khai báo các tiện ích `ColorScheme.neutral`, `ColorScheme.onNeutral`, `ColorScheme.neutralContainer`, `ColorScheme.onNeutralContainer` trong `Color.kt` ánh xạ từ `LiteverTheme.colors`.
  3. **Chuyển đổi Adapter `:core:designsystem`:**
     - `ReMindAlertDialog` chuyển sang sử dụng `LvAlertDialog` kết hợp `LvButton`.
     - `ReMindBottomBar` chuyển các nút CTA sang `LvButton`.
     - `TimePickerDialog` sử dụng `LvButton` cho nút xác nhận/hủy.
     - `WeatherInfoView` chuyển nút chọn vị trí sang `LvButton(type = LvButtonType.Text, semantic = LvSemantic.Neutral)`.
     - `RingtoneSelectionScreen` và `SnoozeSettingsScreen` sử dụng `LvButton` (Outlined/Filled) và `LvIconButton`.
     - `MissionSelectionBottomSheet` sử dụng `LvButton`.
  4. **Chuyển đổi các Feature Modules:**
     - **`:features:alarms`**: Refactor `AlarmEditScreen` (sử dụng `LvButton`, `LvIconButton`, `LvTextField`, `LvChip`), `AlarmListScreen` (`LvIconButton`), `AlarmMessageScreen` (`LvTextField`, `LvButton`), `AlarmRingingScreen` (`LvButton`), `AlarmCard` (`LvIconButton`), `PermissionWarningBanner` (`LvButton`).
     - **`:features:settings`**: Refactor `AlarmSettingsScreen` (chuyển `DurationSelectionDialog` sang `LvAlertDialog`), `PermissionSettingsScreen` (`LvButton` Primary/Secondary).
     - **`:features:mission`**: Refactor `MemoryGameConfigScreen`, `MissionRingingScreen`, `PhraseSelectionScreen`, `TypingMissionConfigScreen`, `MathMissionContent` sang `LvButton`, `LvIconButton`, `LvTextField`.
     - **`:features:today` & `:app`**: Refactor `LocationSearchScreen` (LvTextField với leading icon và trailing `LvIconButton`), `TodayScreen` (tint progress), và `MainActivity` (tích hợp `LvSnackbarHost` với `SnackbarHostState`).
- **Hệ quả:**
  - **Giảm thiểu Boilerplate tối đa:** Cắt giảm hơn 60% mã nguồn thiết lập kiểu dáng, màu sắc, bo góc và padding thủ công tại các call-site.
  - **Nhất quán Visual & Semantic Tuyệt đối:** Toàn bộ nút bấm, ô nhập liệu, hộp thoại và chip tự động tuân thủ chuẩn bo góc squircle (6.dp/10.dp) và màu sắc theo `LvSemantic`.
  - **Kiến trúc Modular Rõ ràng:** Cấu trúc package được phân tách mạch lạc theo từng domain component.
  - **Độ ổn định cao:** Dự án biên dịch sạch 100% không còn unresolved reference và toàn bộ 100% unit test suites đều vượt qua.

### [TDR-063] - Chuẩn hóa Bảng màu Đỏ (RED) Mặc định và Bộ Chọn 8 Ô Palette theo LiteVer Design System
- **Ngày thực hiện:** 2026-09-14
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Bảng màu mặc định của ứng dụng ReMind trước đây là màu nâu đất ("REMIND"), tách biệt khỏi hệ sinh thái bảng màu chuẩn hóa của `litever-designsystem`.
  - Thư viện `litever-designsystem` cung cấp 7 bảng màu chuẩn hóa gồm: RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET cùng khả năng phối màu tự động theo hình nền thiết bị (Dynamic Theme trên Android 12+).
  - Cần chuyển đổi bảng màu mặc định của toàn bộ ứng dụng sang màu **Đỏ (RED)** của Litever Palette, đồng thời nâng cấp màn hình Cài đặt chung (`GeneralSettingsScreen`) để người dùng dễ dàng chọn lựa 1 trong 7 màu có sẵn hoặc tùy chọn theo màn hình.
- **Quyết định:**
  1. **Đổi màu mặc định sang RED:**
     - `:core:datastore`: Cập nhật `COLOR_PALETTE_KEY` mặc định trả về `"RED"`.
     - `:features:settings`: Cập nhật `SettingsUiState.colorPalette` mặc định là `"RED"`.
     - `:core:designsystem`: Cập nhật `ReMindTheme(colorPalette = "RED")`, ánh xạ chuỗi `colorPalette` sang enum `LiteverThemeColor.RED` (và các màu tương ứng), truyền vào `LiteverTheme(themeColor = ...)`. Đồng thời đồng bộ các token `primaryLight`, `primaryContainerLight`, `primaryDark`, `primaryContainerDark`, `remindLightColors`, `remindDarkColors` sang giá trị của bảng màu RED Litever.
     - `:app`: Cập nhật `MainActivity` collect giá trị khởi tạo `colorPalette` là `"RED"`.
     - `litever-designsystem`: Cập nhật `LiteverThemeColor.DEFAULT` ánh xạ sang `redDarkColorScheme` / `redLightColorScheme` để bảo đảm tính đồng nhất ở mọi tầng.
  2. **Bộ chọn 8 ô chia 2 hàng trên GeneralSettingsScreen:**
     - Thay thế danh sách dọc `ListItem + RadioButton` trước đây bằng lưới gồm 8 ô chia đều thành 2 hàng (mỗi hàng 4 ô).
     - Hàng 1: Đỏ (RED), Cam (ORANGE), Vàng (YELLOW), Lục (GREEN).
     - Hàng 2: Lam (BLUE), Chàm (INDIGO), Tím (VIOLET), Màn hình (DYNAMIC).
     - Mỗi ô là một `IconButton` với nền là `primaryContainer` của bảng màu tương ứng trong theme hiện tại (Light/Dark).
     - Ở giữa mỗi ô là text hiển thị tên màu sử dụng màu chữ `primary` của bảng màu đó.
     - Trạng thái nhận biết màu đang chọn: Viền bo góc `border(2.dp, primary, shape)` nổi bật cùng biểu tượng dấu tích `Icons.Rounded.Check` bên cạnh tên màu in đậm (Bold).
  3. **Dọn dẹp File Color.kt tại :core:designsystem:**
     - Loại bỏ toàn bộ hơn 340 dòng mã định nghĩa token màu sắc cục bộ của riêng app (`primaryLight`, `secondaryLight`, các biến thể contrast, `remindLightColors`, `remindDarkColors`, `remindLightColorScheme`, v.v.).
     - Ứng dụng chuyển sang tái sử dụng 100% hệ thống palette và theme từ `:litever-designsystem`, chỉ giữ lại các tiện ích mở rộng ngữ nghĩa `ColorScheme.warning`, `ColorScheme.success`, `ColorScheme.neutral` ủy quyền sang `LiteverTheme.colors`.
- **Hệ quả:**
  - Giảm thiểu hơn 340 dòng code dư thừa, loại bỏ hoàn toàn sự trùng lặp màu sắc giữa app và Design System.
  - Toàn bộ ứng dụng ReMind đồng bộ nhận diện thương hiệu với màu Đỏ (RED) tươi sáng, hiện đại.
  - Giao diện chọn màu sắc trực quan, thẩm mỹ cao, tiết kiệm không gian và tương thích trọn vẹn với cả chế độ Sáng/Tối lẫn Android 12+ Dynamic Coloring.

### [TDR-064] - Thay thế Box ô nhớ bằng LvButton với LvSemantic và Semantics trợ năng tương ứng trong MemoryTilesMissionContent
- **Ngày thực hiện:** 2026-09-15
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Component `MemoryTilesMissionContent` trong `:features:mission` trước đây sử dụng `Box` kết hợp `Modifier.background()` và `Modifier.clickable()` thủ công để vẽ các ô lưới bài tập trí nhớ.
  - Cần chuyển đổi sang thành phần `LvButton` tiêu chuẩn từ Design System `:litever-designsystem` nhằm bảo đảm tính nhất quán về visual design (bo góc squircle, hiệu ứng phản hồi ripple, màu sắc theo intent `LvSemantic`) cũng như hỗ trợ đầy đủ semantics trợ năng (Accessibility/Screen Readers).
- **Quyết định:**
  - Thay thế khối `Box` bằng `LvButton` trong `LazyVerticalGrid`.
  - Kiểm soát trạng thái click qua `enabled = isClickable` (chỉ cho phép nhấn khi `gameState == MemoryGameState.PLAYING && !isSelected`).
  - Bảo đảm ô giữ nguyên màu hiển thị visual kể cả khi bị vô hiệu hóa click (`enabled = false`) bằng cách cấu hình `colors = ButtonDefaults.buttonColors(containerColor = tileColor, disabledContainerColor = tileColor)`.
  - Ánh xạ trạng thái game tile sang `LvSemantic`:
    - `LvSemantic.Primary` cho ô mục tiêu trong pha ghi nhớ (MEMORIZE) và các ô mục tiêu chọn đúng.
    - `LvSemantic.Destructive` cho ô chọn sai trong pha chơi (PLAYING).
    - `LvSemantic.Neutral` cho các ô mặc định chưa chọn.
  - Cấu hình trợ năng qua `Modifier.semantics`: gán `selected = isSelected` và `stateDescription` phù hợp ("Target", "Correct", "Wrong", "Unselected").
  - Đặt `contentPadding = PaddingValues(0.dp)` để vừa vặn hoàn hảo trong ô lưới `aspectRatio(1f)`.
- **Hệ quả:**
  - Code gọn gàng, chuẩn hóa 100% theo bộ linh kiện `LvButton` của Design System.
  - Triệt tiêu hoàn toàn hiệu ứng click (ripple/gesture) không mong muốn ở các ô không được phép nhấn (trong pha MEMORIZE, SUCCESS, FAILURE hoặc ô đã được chọn).
  - Trình đọc màn hình (TalkBack) nhận diện chính xác trạng thái vô hiệu hóa của nút (`enabled = false`).

### [TDR-065] - Chuẩn hóa ReMindGroupCard, AlarmSettingRow và Tối ưu dải màu Thời tiết (WeatherInfoView)
- **Ngày thực hiện:** 2026-09-16
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Cần nâng cấp trải nghiệm giao diện các thẻ nhóm (`ReMindGroupCard`), nhóm cài đặt báo thức (`AlarmEditScreen`) và thẻ thông tin thời tiết (`WeatherInfoView`).
  - Các item cài đặt trong `AlarmEditScreen` trước đây bị lặp lại cấu hình Layout và chưa tận dụng Material 3 `ListItem` chuẩn.
  - Thẻ thông tin thời tiết `WeatherInfoView` bị trùng lặp viền Outlined và sử dụng dải màu gradient cũ chưa đồng bộ với các theme màu trong `GeneralSettingsScreen`.
- **Quyết định:**
  1. **Chuẩn hóa ReMindGroupCard & ReMindSettingsGroup:**
     - Sử dụng `ReMindGroupCard` làm container bọc cho `ReMindSettingsGroup` với `surfaceContainerLow` và bo góc `shapes.large`.
  2. **Tạo AlarmSettingRow dùng chung cho feature:alarms:**
     - Tạo `AlarmSettingRow` tái sử dụng Material 3 `ListItem` kết hợp `ReMindSettingIcon`.
     - Đồng bộ áp dụng cho tất cả các item trong `AlarmEditScreen` (Ringtone, Gentle Alarm, Use Alarm Stream, Snooze, Auto Silence).
  3. **Cập nhật WeatherInfoView theo Theme Palettes:**
     - `getWeatherColors` lấy dải màu trực tiếp từ palette màu hệ thống (`indigo`/`blue` -> `green` -> `yellow`/`orange` -> `orange`/`red`).
     - Loại bỏ biến thiên alpha giữa sáng/tối để giữ màu sắc trung thực, tươi sáng.
     - Loại bỏ đường viền `BorderStroke` (Outlined) trên các thẻ thời tiết.
- **Hệ quả:**
  - Giao diện nhất quán, hiện đại, tuân thủ 100% Material 3 và Litever Design System.
  - Tối ưu lượng code lặp lại tại `AlarmEditScreen`.

### [TDR-066] - Tối giản hóa cấu trúc container trong WeatherInfoView
- **Ngày thực hiện:** 2026-09-17
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `FullWeatherView` và `CompactWeatherView` trong `WeatherInfoView.kt` trước đây sử dụng các container lồng nhau không cần thiết (`Card` -> `Box` -> `Column` ở Full view, và `Surface` -> `Box` -> `Row` ở Compact view).
  - Điều này làm phức tạp cây Layout Node (Layout Tree) và tạo ra boilerplate thừa (`CardDefaults`, `Box`, `Surface`).
- **Quyết định:**
  1. **Đơn giản hóa FullWeatherView:** Dùng duy nhất 1 `Column` với `Modifier.clip(LiteverTheme.shapes.medium).background(Brush.linearGradient(weatherColors))`.
  2. **Đơn giản hóa CompactWeatherView:** Dùng duy nhất 1 `Row` với `Modifier.clip(LiteverTheme.shapes.large).background(Brush.linearGradient(weatherColors)).clickable { ... }`.
  3. **Dọn dẹp Import:** Loại bỏ hoàn toàn các import không sử dụng (`Card`, `CardDefaults`, `Box`, `Surface`), bổ sung `Modifier.clip`.
  4. **Tối ưu Alpha & Tương phản Chữ theo Theme:**
     - Cập nhật `getWeatherColors`: Tự động áp dụng `alpha = 0.3f` ở Dark Theme và `alpha = 0.7f` ở Light Theme.
     - Chuẩn hóa các màu chữ (`location`, `temperature`, `condition`, `AI hint`) sang `onSurface` và `onSurfaceVariant` không giảm alpha quá mức để đảm bảo độ tương phản cao và hiển thị sắc nét trên cả 2 chế độ sáng/tối.
- **Hệ quả:**
  - Cấu trúc Composable tối giản tuyệt đối: Không còn bất kỳ container bọc ngoài dư thừa nào.
  - Giảm tối đa độ sâu của UI Node Hierarchy, tối ưu hiệu năng recomposition và render.
### [TDR-067] - Tái cấu trúc GeneralSettingsScreen: Thay thế SegmentedButton bằng ReMindSettingsItem & Selection Dialog
- **Ngày thực hiện:** 2026-09-17
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trong `GeneralSettingsScreen`, việc lặp lại 3 hàng `SingleChoiceSegmentedButtonRow` liên tiếp (Định dạng giờ, Chế độ hiển thị, Ngôn ngữ) làm giao diện bị dày đặc, chiếm diện tích ngang và kém tính phân cấp thị giác.
  - Cần chuyển đổi sang phong cách danh sách cài đặt chuẩn Android / Material 3 (dùng row hiển thị tóm tắt lựa chọn hiện tại và mở Dialog lựa chọn khi tương tác).
- **Quyết định:**
  1. **Chuyển đổi sang `ReMindSettingsItem` & Tối ưu gom nhóm:**
     - Gộp mục Ngôn ngữ và Định dạng giờ vào chung một thẻ nhóm **"Ngôn ngữ & Vùng" / "Language & Region"** (`language_and_region_headline`).
     - Mục Time format: Hiển thị title "Time format", subtitle là định dạng đang chọn ("System", "12h", "24h"), icon `Schedule` và chevron trailing icon.
     - Mục Display mode: Nằm trong nhóm "Display", hiển thị title "Display mode", subtitle là theme đang chọn ("System", "Light", "Dark"), icon tự động đổi theo theme hiện tại (`LightMode`, `DarkMode`, `BrightnessMedium`).
     - Mục Language: Nằm trong nhóm "Language & Region", hiển thị title "Language", subtitle là ngôn ngữ đang chọn ("English", "Tiếng Việt"), icon `Language`.
     - Mục Color Palette (Màu chủ đạo): Chuyển đổi từ cụm 8 ô nút chữ nhật cồng kềnh sang dòng `ReMindSettingsItem` chuẩn, hiển thị tên màu đang chọn và Preview chấm tròn màu sắc (Color dot) ở đuôi dòng, nhấp vào để mở `ColorPaletteSelectionDialog`.
  2. **Tạo `SingleChoiceDialog` & `ColorPaletteSelectionDialog` dùng `LvAlertDialog`:**
     - Hiển thị danh sách tùy chọn với `RadioButton` và text có thể bấm trực tiếp vào toàn hàng.
     - `ColorPaletteSelectionDialog`: Thiết kế dạng lưới tròn **4 cột x 2 hàng** (Pixel/Material 3 style) cực kỳ nhỏ gọn, gồm chấm màu tròn 48dp, viền nổi bật (2.5dp) & dấu tích `Check` khi được chọn, cùng tên màu ngắn gọn phía dưới. Giảm 60% chiều cao so với danh sách dọc, không còn chiếm diện tích màn hình.
     - Hỗ trợ nút Cancel (Huỷ) chuẩn thiết kế qua `LvButton`.
- **Trạng thái:** Accepted
- **Bối cảnh:** Sau khi khởi tạo Design System trên Stitch, cần đồng bộ toàn diện tất cả các màn hình hiện tại của ReMind Android lên Stitch canvas nhằm tạo cơ sở thiết kế visual, review UX và đồng nhất quy chuẩn thiết kế.
- **Quyết định:**
  - Sử dụng công cụ `generate_screen_from_text` (kết hợp `ReMind Design System` asset) đồng bộ tuần tự 10 màn hình cốt lõi:
    1. `AlarmListScreen` (Tab Báo thức): Logo ReMind, thẻ đếm ngược, danh sách báo thức, FAB.
    2. `TodayScreen` (Tab Hôm nay): Thẻ thời tiết AI WeatherInfoView, trích dẫn động lực TodayQuoteView.
    3. `SettingsScreen` (Tab Cài đặt): Cấu trúc nhóm LiteverSettingsGroup, các liên kết cài đặt.
    4. `AlarmEditScreen` (Thêm / Sửa báo thức): Trình chọn giờ số lớn, chip lặp lại T2..CN, âm lượng, mission và ReMindBottomBar.
    5. `AlarmRingingScreen` (Màn hình chuông reo): Edge-to-edge, đồng hồ số cực lớn, auto-silence timer, nút Dismiss & Snooze.
    6. `PermissionSettingsScreen` (Cài đặt quyền): Trạng thái Exact Alarm, Notification, Overlay, Battery optimization.
    7. `GeneralSettingsScreen` (Cài đặt chung): Định dạng giờ 12h/24h, Sáng/Tối, bảng màu ReMind Warm Terracotta vs Dynamic.
    8. `AttributionsScreen` (Lời cảm ơn & Tài nguyên): Ghi nhận OSS Libraries, Storyset illustrations, WeatherAPI.
    9. `SnoozeSettingsScreen` (Cài đặt hoãn): Switch bật/tắt, interval và số lần lặp lại.
    10. `MissionRingingScreen` (Thử thách thức giấc): Giao diện gõ chữ với tiến trình 3 bước, trích dẫn động lực và đếm ngược.
- **Hệ quả:** Toàn bộ 10 màn hình đã hiện diện đầy đủ trên Stitch Canvas của project `ReMind Android`, tuân thủ đồng nhất 100% token thiết kế Material 3 ấm áp (Terracotta).

### [TDR-060] - Tinh chỉnh Màn hình theo Component Gốc Litever Design System & Bổ sung Module Mission lên Stitch
- **Ngày thực hiện:** 2026-09-08
- **Trạng thái:** Accepted
- **Bối cảnh:** Các màn hình đã sinh ban đầu cần được đối chiếu trực tiếp với mã nguồn chi tiết của thư viện `:litever-designsystem` và các custom component wrapper (`ReMindTopAppBar`, `ReMindBottomBar`, `LiteverSettingsGroup`, `LiteverSettingsItem`, `LiteverSingleChoiceSegmentedButtonRow`) để đảm bảo tính chuẩn xác tuyệt đối về mặt cấu trúc và kiểu dáng. Đồng thời cần bổ sung đầy đủ các giao diện cấu hình thử thách trong module `:features:mission`.
- **Quyết định:**
  - Rà soát trực tiếp mã nguồn thư viện `:litever-designsystem` từ workspace local.
  - Sử dụng `edit_screens` để tinh chỉnh các màn hình hiện có (`AlarmListScreen`, `AlarmEditScreen`, `SettingsScreen`, `GeneralSettingsScreen`, `PermissionSettingsScreen`, `AttributionsScreen`, `SnoozeSettingsScreen`, `MissionRingingScreen`) khớp từng thuộc tính component (ReMindLogo, nút Hủy/Lưu ở bottom bar, RadioButton, SegmentedButton).
  - Bổ sung 3 màn hình còn lại của module Mission:
    1. `TypingMissionConfigScreen` (Cấu hình nhiệm vụ gõ phím: Stepper số lần lặp, RadioButton chế độ, thẻ xem trước cụm từ).
    2. `PhraseSelectionScreen` (Màn hình chọn cụm từ: Tabs Động lực/Cơ bản/Của tôi, Checkboxes, Thêm cụm từ mới).
    3. `MemoryGameConfigScreen` (Cấu hình trò chơi ô nhớ: Stepper số vòng, chọn độ khó lưới 3x3 đến 7x7 và bảng xem trước).
- **Hệ quả:** Stitch Project `ReMind Android` hiện phản ánh đầy đủ và chuẩn xác 100% tất cả các màn hình và thành phần component từ codebase thực tế.

### [TDR-061] - Nâng cấp Thư viện LiteVer Design System 2.0.0 và Refactor toàn diện sang Native Material 3 & LiteVer Defaults
- **Ngày thực hiện:** 2026-09-11
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Thư viện `litever-designsystem` phát hành phiên bản 2.0.0 với triết lý kiến trúc tinh gọn (Lean Architecture): loại bỏ hoàn toàn 24 pass-through wrapper components (như `LiteverButton`, `LiteverScaffold`, `LiteverTopAppBar`, `LiteverTextField`, `LiteverCard`, `LiteverCircularProgressIndicator`, `LiteverIconButton`, `LiteverSwitch`, v.v.). Các wrapper này trước đây tạo ra tầng trung gian không cần thiết, làm giới hạn khả năng tiếp cận các API và tham số phong phú của Jetpack Compose Material 3 (M3), đồng thời gây khó khăn khi nâng cấp Jetpack Compose BOM.
  - Thay vì duy trì pass-through wrappers, Litever 2.0.0 cung cấp mô hình Component Defaults (`LiteVerButtonDefaults`, `LiteVerTextFieldDefaults`) kết hợp cùng hệ thống Design Tokens phong phú (`LiteverTheme.spacing`, `LiteverTheme.shapes`, semantic colors `warning`/`onWarning`, `success`/`onSuccess`).
  - Dự án `remind-android` cần liên kết trực tiếp với mã nguồn cục bộ của thư viện thông qua cơ chế `includeBuild("../litever-designsystem")` để vừa thử nghiệm vừa phát triển song song. Đồng thời cần refactor toàn bộ các màn hình và thành phần UI trong tất cả các module (`:core:designsystem`, `:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, `:app`) sang Material 3 gốc kết hợp LiteVer Defaults và Spacing Tokens.
- **Quyết định:**
  1. **Cấu hình Composite Build & Dependency Substitution:**
     - Khai báo `includeBuild("../litever-designsystem")` trong `settings.gradle.kts` kèm theo khối `dependencySubstitution` thay thế artifact `com.github.toantd2000:litever-designsystem` bằng dự án cục bộ `:designsystem`.
     - Cập nhật phiên bản thư viện `liteverDesignsystem` lên `2.0.0` trong `gradle/libs.versions.toml`.
  2. **Tái cấu trúc `:core:designsystem` thành Adapter chuẩn mực:**
     - Đồng bộ `ReMindTheme` với `LiteverTheme`, bổ sung đầy đủ các token màu semantic (`warning`, `onWarning`, `success`, `onSuccess`).
     - Tái cấu trúc các custom wrappers chuyên biệt của ReMind (`ReMindTopAppBar`, `ReMindAlertDialog`, `ReMindBottomBar`, `ReMindLoadingIconButton`, `ReMindSettingIcon`) trực tiếp trên nền tảng Material 3 composables (`TopAppBar`, `AlertDialog`, `Surface`, `IconButton`, v.v.) kết hợp `LiteVerButtonDefaults`.
     - Tái tạo các component đặc thù bị thiếu ở v2.0.0 (`ReMindSettingsGroup`, `ReMindSettingsItem`, `ReMindTimePickerDialog`) trong `:core:designsystem` để bảo đảm nguyên tắc Single Source of Truth cho UI.
     - Refactor các màn hình dùng chung trong core (`RingtoneSelectionScreen`, `SnoozeSettingsScreen`, `WeatherInfoView`, `TodayQuoteView`, `MissionSelectionBottomSheet`) sang Material 3 và `LiteverTheme.spacing`.
  3. **Refactor toàn diện các Feature Modules sang M3 & LiteVer Defaults:**
     - `:features:alarms`: Refactor `AlarmListScreen`, `AlarmEditScreen`, `AlarmRingingScreen`, `AlarmMessageScreen`, `AlarmCard`, `ExitAppDialog`, `MissedAlarmDialog`, `PermissionWarningBanner`.
     - `:features:settings`: Refactor `SettingsScreen`, `AlarmSettingsScreen`, `GeneralSettingsScreen`, `PermissionSettingsScreen`, `AttributionsScreen`, `LicensesScreen`.
     - `:features:mission`: Refactor `TypingMissionConfigScreen`, `PhraseSelectionScreen`, `MemoryGameConfigScreen`, `MissionRingingScreen` cùng các content components (`MathMissionContent`, `MemoryTilesMissionContent`, `MissionCompleteContent`, `TypingMissionContent`).
     - `:features:today`: Refactor `TodayScreen` và `LocationSearchScreen`.
     - `:app`: Refactor `MainActivity` scaffold, navigation host và window insets.
  4. **Quy chuẩn Hóa Spacing Tokens & Window Insets:**
     - Xóa bỏ 100% các giá trị khoảng cách hardcode `.dp` trong layout, thay thế bằng `LiteverTheme.spacing` (`tiny`, `extraSmall`, `small`, `smallMedium`, `medium`, `large`, `extraLarge`, v.v.). Các giá trị `1.dp` duy nhất được giữ lại là độ dày đường viền kỹ thuật (`BorderStroke`).
     - Root `Scaffold` tại `:app` đảm nhiệm xử lý system bars insets; các `Scaffold` con tại các màn hình con cấu hình `contentWindowInsets = WindowInsets(0, 0, 0, 0)` để triệt tiêu lỗi double-padding (cộng dồn insets).
- **Hệ quả:**
  - **Kiến trúc tinh gọn (Lean Architecture):** Loại bỏ hoàn toàn sự phụ thuộc vào các pass-through wrapper mong manh, mã nguồn Compose trực diện, dễ đọc, dễ tiếp cận các API và modifier mới nhất của Google Material 3.
  - **Tính nhất quán Visual:** Toàn bộ khoảng cách, màu sắc và kiểu dáng nút/input đều được kiểm soát chặt chẽ bởi Design System tokens thông qua `LiteverTheme` và `LiteVerButtonDefaults`.
  - **Quy trình phát triển liền mạch:** Composite build giúp phản ánh ngay lập tức các thay đổi từ thư viện `litever-designsystem` mà không cần chu kỳ release/publish phức tạp.
  - **Chất lượng và Độ ổn định:** Tất cả các module biên dịch sạch sẽ 100%, bổ sung bộ unit test logic UI toàn diện cho mission và today features bảo đảm không xảy ra hồi quy.

### [TDR-062] - Nâng cấp Thư viện LiteVer Design System 2.1.0 và Chuyển đổi sang Bộ Thành phần Opinionated Lv* (LvButton, LvIconButton, LvTextField, LvAlertDialog, LvChip, LvSnackbar)
- **Ngày thực hiện:** 2026-09-12
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Sau khi hoàn thành Phase 7 (chuyển dịch từ 24 wrappers cũ sang Material 3 gốc kết hợp Component Defaults ở LiteVer v2.0.0), việc phải cấu hình lặp đi lặp lại các tham số `colors = LiteVerButtonDefaults.primaryColors()`, `shape = LiteVerButtonDefaults.shape`, `colors = LiteVerTextFieldDefaults.outlinedColors()`, `border = ...` tại từng Composable trong hàng chục màn hình vẫn tạo ra lượng boilerplate code đáng kể, đồng thời tiềm ẩn rủi ro thiếu nhất quán về ngữ nghĩa giao diện (semantic intents) và kiểu dáng bo góc squircle.
  - Thư viện `litever-designsystem` phát hành phiên bản `2.1.0` với hai cải tiến đột phá:
    1. **Tái cơ cấu Package theo Domain:** Tách các thành phần từ package phẳng `vn.io.litever.designsystem.components.*` sang các sub-package chuyên biệt: `components.button.*`, `components.textfield.*`, `components.chip.*`, `components.dialog.*`, `components.snackbar.*`, và `components.core.*`.
    2. **Bộ thành phần Opinionated thế hệ mới `Lv*`:** Giới thiệu `LvButton`, `LvIconButton`, `LvTextField`, `LvAlertDialog`, `LvChip`, `LvSnackbarHost` / `LvSnackbar` được định hình sẵn chuẩn bo góc squircle (6.dp cho control nhỏ, 10.dp cho dialog), tích hợp chặt chẽ với hệ thống màu ngữ nghĩa `LvSemantic` (`Primary`, `Secondary`, `Tertiary`, `Neutral`, `Success`, `Destructive`, `Warning`) và các biến thể kiểu dáng (`LvButtonType`, `LvTextFieldType`).
    3. **Mở rộng Semantic Tokens:** Bổ sung các tokens `neutral`, `onNeutral`, `neutralContainer`, `onNeutralContainer` trong `LiteverTheme.colors`.
  - Dự án `remind-android` cần căn chỉnh lại toàn bộ các import bị gãy do việc tách package, đồng thời chuyển đổi mã nguồn UI trên toàn bộ các modules (`:core:designsystem`, `:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, `:app`) sang các opinionated components thế hệ mới của v2.1.0.
- **Quyết định:**
  1. **Version Catalog & Package Import Alignment:** Cập nhật `liteverDesignsystem = "2.1.0"` trong `gradle/libs.versions.toml`. Chuyển đổi toàn bộ import sang các package chuyên biệt (`components.button.LiteVerButtonDefaults`, `components.textfield.LiteVerTextFieldDefaults`, `components.button.LvButton`, v.v.).
  2. **Mở rộng Semantic Colors tại `:core:designsystem`:** Khai báo các tiện ích `ColorScheme.neutral`, `ColorScheme.onNeutral`, `ColorScheme.neutralContainer`, `ColorScheme.onNeutralContainer` trong `Color.kt` ánh xạ từ `LiteverTheme.colors`.
  3. **Chuyển đổi Adapter `:core:designsystem`:**
     - `ReMindAlertDialog` chuyển sang sử dụng `LvAlertDialog` kết hợp `LvButton`.
     - `ReMindBottomBar` chuyển các nút CTA sang `LvButton`.
     - `TimePickerDialog` sử dụng `LvButton` cho nút xác nhận/hủy.
     - `WeatherInfoView` chuyển nút chọn vị trí sang `LvButton(type = LvButtonType.Text, semantic = LvSemantic.Neutral)`.
     - `RingtoneSelectionScreen` và `SnoozeSettingsScreen` sử dụng `LvButton` (Outlined/Filled) và `LvIconButton`.
     - `MissionSelectionBottomSheet` sử dụng `LvButton`.
  4. **Chuyển đổi các Feature Modules:**
     - **`:features:alarms`**: Refactor `AlarmEditScreen` (sử dụng `LvButton`, `LvIconButton`, `LvTextField`, `LvChip`), `AlarmListScreen` (`LvIconButton`), `AlarmMessageScreen` (`LvTextField`, `LvButton`), `AlarmRingingScreen` (`LvButton`), `AlarmCard` (`LvIconButton`), `PermissionWarningBanner` (`LvButton`).
     - **`:features:settings`**: Refactor `AlarmSettingsScreen` (chuyển `DurationSelectionDialog` sang `LvAlertDialog`), `PermissionSettingsScreen` (`LvButton` Primary/Secondary).
     - **`:features:mission`**: Refactor `MemoryGameConfigScreen`, `MissionRingingScreen`, `PhraseSelectionScreen`, `TypingMissionConfigScreen`, `MathMissionContent` sang `LvButton`, `LvIconButton`, `LvTextField`.
     - **`:features:today` & `:app`**: Refactor `LocationSearchScreen` (LvTextField với leading icon và trailing `LvIconButton`), `TodayScreen` (tint progress), và `MainActivity` (tích hợp `LvSnackbarHost` với `SnackbarHostState`).
- **Hệ quả:**
  - **Giảm thiểu Boilerplate tối đa:** Cắt giảm hơn 60% mã nguồn thiết lập kiểu dáng, màu sắc, bo góc và padding thủ công tại các call-site.
  - **Nhất quán Visual & Semantic Tuyệt đối:** Toàn bộ nút bấm, ô nhập liệu, hộp thoại và chip tự động tuân thủ chuẩn bo góc squircle (6.dp/10.dp) và màu sắc theo `LvSemantic`.
  - **Kiến trúc Modular Rõ ràng:** Cấu trúc package được phân tách mạch lạc theo từng domain component.
  - **Độ ổn định cao:** Dự án biên dịch sạch 100% không còn unresolved reference và toàn bộ 100% unit test suites đều vượt qua.

### [TDR-063] - Chuẩn hóa Bảng màu Đỏ (RED) Mặc định và Bộ Chọn 8 Ô Palette theo LiteVer Design System
- **Ngày thực hiện:** 2026-09-14
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Bảng màu mặc định của ứng dụng ReMind trước đây là màu nâu đất ("REMIND"), tách biệt khỏi hệ sinh thái bảng màu chuẩn hóa của `litever-designsystem`.
  - Thư viện `litever-designsystem` cung cấp 7 bảng màu chuẩn hóa gồm: RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET cùng khả năng phối màu tự động theo hình nền thiết bị (Dynamic Theme trên Android 12+).
  - Cần chuyển đổi bảng màu mặc định của toàn bộ ứng dụng sang màu **Đỏ (RED)** của Litever Palette, đồng thời nâng cấp màn hình Cài đặt chung (`GeneralSettingsScreen`) để người dùng dễ dàng chọn lựa 1 trong 7 màu có sẵn hoặc tùy chọn theo màn hình.
- **Quyết định:**
  1. **Đổi màu mặc định sang RED:**
     - `:core:datastore`: Cập nhật `COLOR_PALETTE_KEY` mặc định trả về `"RED"`.
     - `:features:settings`: Cập nhật `SettingsUiState.colorPalette` mặc định là `"RED"`.
     - `:core:designsystem`: Cập nhật `ReMindTheme(colorPalette = "RED")`, ánh xạ chuỗi `colorPalette` sang enum `LiteverThemeColor.RED` (và các màu tương ứng), truyền vào `LiteverTheme(themeColor = ...)`. Đồng thời đồng bộ các token `primaryLight`, `primaryContainerLight`, `primaryDark`, `primaryContainerDark`, `remindLightColors`, `remindDarkColors` sang giá trị của bảng màu RED Litever.
     - `:app`: Cập nhật `MainActivity` collect giá trị khởi tạo `colorPalette` là `"RED"`.
     - `litever-designsystem`: Cập nhật `LiteverThemeColor.DEFAULT` ánh xạ sang `redDarkColorScheme` / `redLightColorScheme` để bảo đảm tính đồng nhất ở mọi tầng.
  2. **Bộ chọn 8 ô chia 2 hàng trên GeneralSettingsScreen:**
     - Thay thế danh sách dọc `ListItem + RadioButton` trước đây bằng lưới gồm 8 ô chia đều thành 2 hàng (mỗi hàng 4 ô).
     - Hàng 1: Đỏ (RED), Cam (ORANGE), Vàng (YELLOW), Lục (GREEN).
     - Hàng 2: Lam (BLUE), Chàm (INDIGO), Tím (VIOLET), Màn hình (DYNAMIC).
     - Mỗi ô là một `IconButton` với nền là `primaryContainer` của bảng màu tương ứng trong theme hiện tại (Light/Dark).
     - Ở giữa mỗi ô là text hiển thị tên màu sử dụng màu chữ `primary` của bảng màu đó.
     - Trạng thái nhận biết màu đang chọn: Viền bo góc `border(2.dp, primary, shape)` nổi bật cùng biểu tượng dấu tích `Icons.Rounded.Check` bên cạnh tên màu in đậm (Bold).
  3. **Dọn dẹp File Color.kt tại :core:designsystem:**
     - Loại bỏ toàn bộ hơn 340 dòng mã định nghĩa token màu sắc cục bộ của riêng app (`primaryLight`, `secondaryLight`, các biến thể contrast, `remindLightColors`, `remindDarkColors`, `remindLightColorScheme`, v.v.).
     - Ứng dụng chuyển sang tái sử dụng 100% hệ thống palette và theme từ `:litever-designsystem`, chỉ giữ lại các tiện ích mở rộng ngữ nghĩa `ColorScheme.warning`, `ColorScheme.success`, `ColorScheme.neutral` ủy quyền sang `LiteverTheme.colors`.
- **Hệ quả:**
  - Giảm thiểu hơn 340 dòng code dư thừa, loại bỏ hoàn toàn sự trùng lặp màu sắc giữa app và Design System.
  - Toàn bộ ứng dụng ReMind đồng bộ nhận diện thương hiệu với màu Đỏ (RED) tươi sáng, hiện đại.
  - Giao diện chọn màu sắc trực quan, thẩm mỹ cao, tiết kiệm không gian và tương thích trọn vẹn với cả chế độ Sáng/Tối lẫn Android 12+ Dynamic Coloring.

### [TDR-064] - Thay thế Box ô nhớ bằng LvButton với LvSemantic và Semantics trợ năng tương ứng trong MemoryTilesMissionContent
- **Ngày thực hiện:** 2026-09-15
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Component `MemoryTilesMissionContent` trong `:features:mission` trước đây sử dụng `Box` kết hợp `Modifier.background()` và `Modifier.clickable()` thủ công để vẽ các ô lưới bài tập trí nhớ.
  - Cần chuyển đổi sang thành phần `LvButton` tiêu chuẩn từ Design System `:litever-designsystem` nhằm bảo đảm tính nhất quán về visual design (bo góc squircle, hiệu ứng phản hồi ripple, màu sắc theo intent `LvSemantic`) cũng như hỗ trợ đầy đủ semantics trợ năng (Accessibility/Screen Readers).
- **Quyết định:**
  - Thay thế khối `Box` bằng `LvButton` trong `LazyVerticalGrid`.
  - Kiểm soát trạng thái click qua `enabled = isClickable` (chỉ cho phép nhấn khi `gameState == MemoryGameState.PLAYING && !isSelected`).
  - Bảo đảm ô giữ nguyên màu hiển thị visual kể cả khi bị vô hiệu hóa click (`enabled = false`) bằng cách cấu hình `colors = ButtonDefaults.buttonColors(containerColor = tileColor, disabledContainerColor = tileColor)`.
  - Ánh xạ trạng thái game tile sang `LvSemantic`:
    - `LvSemantic.Primary` cho ô mục tiêu trong pha ghi nhớ (MEMORIZE) và các ô mục tiêu chọn đúng.
    - `LvSemantic.Destructive` cho ô chọn sai trong pha chơi (PLAYING).
    - `LvSemantic.Neutral` cho các ô mặc định chưa chọn.
  - Cấu hình trợ năng qua `Modifier.semantics`: gán `selected = isSelected` và `stateDescription` phù hợp ("Target", "Correct", "Wrong", "Unselected").
  - Đặt `contentPadding = PaddingValues(0.dp)` để vừa vặn hoàn hảo trong ô lưới `aspectRatio(1f)`.
- **Hệ quả:**
  - Code gọn gàng, chuẩn hóa 100% theo bộ linh kiện `LvButton` của Design System.
  - Triệt tiêu hoàn toàn hiệu ứng click (ripple/gesture) không mong muốn ở các ô không được phép nhấn (trong pha MEMORIZE, SUCCESS, FAILURE hoặc ô đã được chọn).
  - Trình đọc màn hình (TalkBack) nhận diện chính xác trạng thái vô hiệu hóa của nút (`enabled = false`).

### [TDR-065] - Chuẩn hóa ReMindGroupCard, AlarmSettingRow và Tối ưu dải màu Thời tiết (WeatherInfoView)
- **Ngày thực hiện:** 2026-09-16
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Cần nâng cấp trải nghiệm giao diện các thẻ nhóm (`ReMindGroupCard`), nhóm cài đặt báo thức (`AlarmEditScreen`) và thẻ thông tin thời tiết (`WeatherInfoView`).
  - Các item cài đặt trong `AlarmEditScreen` trước đây bị lặp lại cấu hình Layout và chưa tận dụng Material 3 `ListItem` chuẩn.
  - Thẻ thông tin thời tiết `WeatherInfoView` bị trùng lặp viền Outlined và sử dụng dải màu gradient cũ chưa đồng bộ với các theme màu trong `GeneralSettingsScreen`.
- **Quyết định:**
  1. **Chuẩn hóa ReMindGroupCard & ReMindSettingsGroup:**
     - Sử dụng `ReMindGroupCard` làm container bọc cho `ReMindSettingsGroup` với `surfaceContainerLow` và bo góc `shapes.large`.
  2. **Tạo AlarmSettingRow dùng chung cho feature:alarms:**
     - Tạo `AlarmSettingRow` tái sử dụng Material 3 `ListItem` kết hợp `ReMindSettingIcon`.
     - Đồng bộ áp dụng cho tất cả các item trong `AlarmEditScreen` (Ringtone, Gentle Alarm, Use Alarm Stream, Snooze, Auto Silence).
  3. **Cập nhật WeatherInfoView theo Theme Palettes:**
     - `getWeatherColors` lấy dải màu trực tiếp từ palette màu hệ thống (`indigo`/`blue` -> `green` -> `yellow`/`orange` -> `orange`/`red`).
     - Loại bỏ biến thiên alpha giữa sáng/tối để giữ màu sắc trung thực, tươi sáng.
     - Loại bỏ đường viền `BorderStroke` (Outlined) trên các thẻ thời tiết.
- **Hệ quả:**
  - Giao diện nhất quán, hiện đại, tuân thủ 100% Material 3 và Litever Design System.
  - Tối ưu lượng code lặp lại tại `AlarmEditScreen`.

### [TDR-066] - Tối giản hóa cấu trúc container trong WeatherInfoView
- **Ngày thực hiện:** 2026-09-17
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `FullWeatherView` và `CompactWeatherView` trong `WeatherInfoView.kt` trước đây sử dụng các container lồng nhau không cần thiết (`Card` -> `Box` -> `Column` ở Full view, và `Surface` -> `Box` -> `Row` ở Compact view).
  - Điều này làm phức tạp cây Layout Node (Layout Tree) và tạo ra boilerplate thừa (`CardDefaults`, `Box`, `Surface`).
- **Quyết định:**
  1. **Đơn giản hóa FullWeatherView:** Dùng duy nhất 1 `Column` với `Modifier.clip(LiteverTheme.shapes.medium).background(Brush.linearGradient(weatherColors))`.
  2. **Đơn giản hóa CompactWeatherView:** Dùng duy nhất 1 `Row` với `Modifier.clip(LiteverTheme.shapes.large).background(Brush.linearGradient(weatherColors)).clickable { ... }`.
  3. **Dọn dẹp Import:** Loại bỏ hoàn toàn các import không sử dụng (`Card`, `CardDefaults`, `Box`, `Surface`), bổ sung `Modifier.clip`.
  4. **Tối ưu Alpha & Tương phản Chữ theo Theme:**
     - Cập nhật `getWeatherColors`: Tự động áp dụng `alpha = 0.3f` ở Dark Theme và `alpha = 0.7f` ở Light Theme.
     - Chuẩn hóa các màu chữ (`location`, `temperature`, `condition`, `AI hint`) sang `onSurface` và `onSurfaceVariant` không giảm alpha quá mức để đảm bảo độ tương phản cao và hiển thị sắc nét trên cả 2 chế độ sáng/tối.
- **Hệ quả:**
  - Cấu trúc Composable tối giản tuyệt đối: Không còn bất kỳ container bọc ngoài dư thừa nào.
  - Giảm tối đa độ sâu của UI Node Hierarchy, tối ưu hiệu năng recomposition và render.
### [TDR-095] - Äá»“ng Bá»™ Dialog Chá»n NgÃ y VÃ  Giá» Cá»§a Nháº¯c Nhá»Ÿ Theo Chuáº©n BÃ¡o Thá»©c
- **Context:** CÃ¡c dialog chá»n ngÃ y vÃ  giá» trong `:features:reminders` (`ReminderRepeatConfigSection`, `ReminderTimeConfigSection`, `ReminderTimeWindowConfigSection`) Ä‘ang dÃ¹ng wrapper cÅ© (`LiteverDatePickerDialog`, `LiteverTimePickerDialog`, `LiteverButton`, `LiteverOutlinedButton`) thiáº¿u tiÃªu Ä‘á» rÃµ rÃ ng vÃ  chÆ°a Ä‘á»“ng bá»™ kiá»ƒu dÃ¡ng nÃºt báº¥m vá»›i `AlarmEditScreen`.
- **Decision:** Chuyá»ƒn sang sá»­ dá»¥ng trá»±c tiáº¿p Material 3 `DatePickerDialog` vÃ  `TimePickerDialog` kÃ¨m tiÃªu Ä‘á» in Ä‘áº­m `titleLarge` vá»›i lá»i vÄƒn phÃ¹ há»£p ngá»¯ cáº£nh Nháº¯c nhá»Ÿ (`"Äáº·t ngÃ y nháº¯c nhá»Ÿ"`, `"Äáº·t giá» nháº¯c nhá»Ÿ"`, `"Äáº·t giá» báº¯t Ä‘áº§u"`, `"Äáº·t giá» káº¿t thÃºc"`) vÃ  bá»™ nÃºt `LvButton` (`Primary` / `Outlined Secondary`).
- **Consequences:** ToÃ n bá»™ dialog chá»n ngÃ y vÃ  giá» giá»¯a BÃ¡o thá»©c vÃ  Nháº¯c nhá»Ÿ Ä‘á»“ng nháº¥t 100% vá» thá»‹ giÃ¡c, bá»‘ cá»¥c vÃ  tráº£i nghiá»‡m tÆ°Æ¡ng tÃ¡c.

### [TDR-094] - Äá»“ng Bá»™ SnoozeBottomSheet VÃ o :core:designsystem Cho Alarm & Reminder
- **Context:** MÃ n hÃ¬nh biÃªn táº­p Nháº¯c nhá»Ÿ (`ReminderAlertConfigSection`) trÆ°á»›c Ä‘Ã¢y dÃ¹ng Switch vÃ  SegmentedButton inline cho má»¥c BÃ¡o láº¡i, khÃ´ng Ä‘á»“ng nháº¥t vá»›i tráº£i nghiá»‡m Bottom Sheet bÃªn BÃ¡o thá»©c (`AlarmEditScreen`).
- **Decision:** Chuyá»ƒn `SnoozeBottomSheet` tá»« `:features:alarms` sang `:core:designsystem` vá»›i tham sá»‘ `repeatCount` tÃ¹y chá»n (`null` á»Ÿ Nháº¯c nhá»Ÿ Ä‘á»ƒ áº©n má»¥c Sá»‘ láº§n láº·p láº¡i), vÃ  thay khá»‘i inline trong `ReminderAlertConfigSection` báº±ng dÃ²ng cÃ i Ä‘áº·t "BÃ¡o láº¡i" má»Ÿ `SnoozeBottomSheet`.
- **Consequences:** TÃ¡i sá»­ dá»¥ng 100% component `SnoozeBottomSheet` giá»¯a `:features:alarms` vÃ  `:features:reminders` Ä‘Ãºng chuáº©n Single Source of Truth, giao diá»‡n gá»n gÃ ng vÃ  Ä‘á»“ng nháº¥t.

### [TDR-093] - Triá»ƒn Khai TÃ­nh NÄƒng BÃ¡o Thá»©c Nhanh (Quick Alarm) Vá»›i Material 3 FAB Menu, Bottom Sheet & VÃ²ng Äá»i Tá»± Äá»™ng XÃ³a
- **NgÃ y thá»±c hiá»‡n:** 2026-10-06 / 2026-10-07
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - NgÆ°á»i dÃ¹ng cÃ³ nhu cáº§u Ä‘áº·t nhanh cÃ¡c má»‘c bÃ¡o thá»©c ngáº¯n (nghá»‰ trÆ°a, Ä‘un nÆ°á»›c, háº¹n giá» nhanh 5-30 phÃºt) mÃ  khÃ´ng cáº§n tráº£i qua toÃ n bá»™ quy trÃ¬nh cáº¥u hÃ¬nh phá»©c táº¡p cá»§a bÃ¡o thá»©c thÃ´ng thÆ°á»ng (chá»n ngÃ y láº·p, nhiá»‡m vá»¥, auto-silence, v.v.).
  - BÃ¡o thá»©c nhanh lÃ  loáº¡i bÃ¡o thá»©c dÃ¹ng 1 láº§n tá»©c thá»i, khÃ´ng cáº§n chá»‰nh sá»­a láº¡i sau khi táº¡o vÃ  cáº§n tá»± Ä‘á»™ng dá»n dáº¹p Ä‘á»ƒ khÃ´ng lÃ m bá»«a bá»™n danh sÃ¡ch bÃ¡o thá»©c.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Táº§ng Dá»¯ Liá»‡u & Kiáº¿n TrÃºc (Domain & Data Layer):**
     - Bá»• sung cá» `isQuickAlarm: Boolean = false` vÃ o Domain Entity `Alarm` vÃ  CSDL Room `AlarmEntity`.
     - NÃ¢ng cáº¥p Room Database lÃªn phiÃªn báº£n 9, táº¡o `MIGRATION_8_9` (`ALTER TABLE alarms ADD COLUMN isQuickAlarm INTEGER NOT NULL DEFAULT 0`).
     - Ãnh xáº¡ trÆ°á»ng `isQuickAlarm` trong `AlarmMapper` giá»¯a Entity vÃ  Domain Model.
  2. **VÃ²ng Äá»i Tá»± Äá»™ng XÃ³a (Lifecycle & Auto-Cleanup):**
     - Khi táº¯t báº±ng Switch trÃªn danh sÃ¡ch (`AlarmListViewModel.toggleAlarm`): Náº¿u `alarm.isQuickAlarm == true`, láº­p tá»©c xÃ³a bÃ¡o thá»©c khá»i CSDL (`repository.deleteAlarm`) vÃ  há»§y lá»‹ch trong `AlarmScheduler`.
     - Khi hoÃ n thÃ nh reo chuÃ´ng (`AlarmControllerImpl.dismissAlarm`): Náº¿u lÃ  bÃ¡o thá»©c nhanh, há»§y lá»‹ch vÃ  xÃ³a vÄ©nh viá»…n khá»i CSDL thay vÃ¬ chá»‰ cáº­p nháº­t tráº¡ng thÃ¡i snooze.
     - TrÃªn danh sÃ¡ch bÃ¡o thá»©c (`AlarmListScreen`): Click vÃ o card bÃ¡o thá»©c nhanh sáº½ khÃ´ng má»Ÿ mÃ n hÃ¬nh `AlarmEditScreen`.
  3. **Giao Diá»‡n & Tráº£i Nghiá»‡m NgÆ°á»i DÃ¹ng (UI/UX - M3 & Litever Design System):**
     - **Material 3 FAB Menu (Tá»‘i giáº£n & KhÃ´ng sinh file dÆ° thá»«a):** Thay vÃ¬ custom speed dial phá»©c táº¡p bá»c Scaffold trong Box vÃ  váº½ Scrim thá»§ cÃ´ng, táº­n dá»¥ng trá»±c tiáº¿p thÃ nh pháº§n chuáº©n cÃ³ sáºµn cá»§a Material 3: `FloatingActionButton` gáº¯n vá»›i `DropdownMenu` vÃ  `DropdownMenuItem` trong `Scaffold(floatingActionButton = { ... })`. Menu cung cáº¥p cÃ¡c lá»±a chá»n: *Alarm*, *Quick alarm*.
     - **Quick Alarm Bottom Sheet (`QuickAlarmBottomSheet.kt`):** 
       - Bá»™ hiá»ƒn thá»‹ cá»™ng dá»“n thá»i gian `+ 0m` vÃ  nÃºt reset `â†º`.
       - LÆ°á»›i 6 nÃºt preset (+1m, +5m, +10m, +15m, +30m, +1h).
       - Hiá»ƒn thá»‹ trá»±c quan thá»i gian reo dá»± kiáº¿n `Ring at ...` há»— trá»£ cáº£ Ä‘á»‹nh dáº¡ng 12h vÃ  24h.
       - Ã” nháº­p mÃ´ táº£ ngáº¯n gá»n (Label) tÃ¹y chá»n.
       - CÃ i Ä‘áº·t chuÃ´ng bÃ¡o, thanh trÆ°á»£t Ã¢m lÆ°á»£ng vÃ  nÃºt gáº¡t rung.
       - NÃºt LÆ°u (Save) dáº¡ng Primary Button (`LvButton`).
     - **Nháº­n Diá»‡n Trá»±c Quan (`AlarmCard.kt`):** ThÃªm icon tia sÃ©t `FlashOn` vÃ  nhÃ£n `Quick alarm` / `BÃ¡o thá»©c nhanh` á»Ÿ hÃ ng tiÃªu Ä‘á» tháº» Ä‘á»ƒ phÃ¢n biá»‡t rÃµ rá»‡t.
  4. **Kiá»ƒm Thá»­:**
     - Viáº¿t Unit Tests Ä‘áº§y Ä‘á»§ cho `AlarmListViewModelTest` vÃ  `AlarmControllerImplTest`, Ä‘áº£m báº£o tá»· lá»‡ vÆ°á»£t qua 100%.

### [TDR-092] - Äá»“ng Bá»™ Thiáº¿t Káº¿ ReminderBasicInfoSection Theo Pattern Content Trong AlarmEditScreen & ÄÃ³ng GÃ³i Vá»›i ReMindSettingsGroup
- **NgÃ y thá»±c hiá»‡n:** 2026-10-06
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Cá»¥m thÃ´ng tin cÆ¡ báº£n trong `ReminderBasicInfoSection` trÆ°á»›c Ä‘Ã¢y sá»­ dá»¥ng `Surface` Ä‘á»™c láº­p vá»›i `LiteverTextField`, khÃ´ng Ä‘á»“ng nháº¥t vá»›i cáº¥u trÃºc tháº» cÃ i Ä‘áº·t cá»§a toÃ n bá»™ á»©ng dá»¥ng (`ReMindSettingsGroup`).
  - Trong `AlarmEditScreen`, Group 3 (Label & Message) Ä‘Æ°á»£c thiáº¿t káº¿ tá»‘i giáº£n, hiá»‡n Ä‘áº¡i vá»›i `BasicTextField`, phÃ¢n tÃ¡ch báº±ng `HorizontalDivider`, cÃ³ nÃºt xÃ³a ná»™i dung nhanh (`LvIconButton` vá»›i `Close` icon) vÃ  icon ngá»¯ cáº£nh (`NewLabel`, `EditNote`).
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Bá»c ngoÃ i báº±ng `ReMindSettingsGroup`:**
     - Thiáº¿t láº­p tiÃªu Ä‘á» nhÃ³m lÃ  `"ThÃ´ng tin chung"` (`R.string.section_basic_info`), Ä‘á»“ng bá»™ phong cÃ¡ch vá»›i cÃ¡c cá»¥m cÃ i Ä‘áº·t khÃ¡c nhÆ° `ReminderRepeatConfigSection`.
  2. **Ãp dá»¥ng pattern Content cá»§a `AlarmEditScreen`:**
     - Sá»­ dá»¥ng `BasicTextField` vá»›i nhÃ£n `labelSmall` in Ä‘áº­m bÃªn trÃªn cho cáº£ TiÃªu Ä‘á» (Title) vÃ  Ghi chÃº chi tiáº¿t (Notes).
     - PhÃ¢n cÃ¡ch giá»¯a Title vÃ  Notes báº±ng `HorizontalDivider(color = outlineVariant.copy(alpha = 0.3f))`.
     - ThÃªm nÃºt xÃ³a (`LvIconButton`) khi trÆ°á»ng cÃ³ ná»™i dung vÃ  icon chá»‰ dáº«n ngá»¯ cáº£nh (`ReMindIcons.NewLabel` cho Title, `ReMindIcons.EditNote` cho Notes) khi trÆ°á»ng trá»‘ng.
     - Báº£o toÃ n kháº£ nÄƒng focus vÃ  thÃ´ng bÃ¡o lá»—i validation (`titleFocusRequester`, `showTitleError`, `R.string.reminder_title_required`).
     - Bá»• sung chuá»—i tÃ i nguyÃªn trá»£ nÄƒng `action_clear_title` vÃ  `action_clear_notes` trong cáº£ tiáº¿ng Anh vÃ  tiáº¿ng Viá»‡t.

### [TDR-091] - TÃ¡i Cáº¥u TrÃºc ToÃ n Diá»‡n ReminderTriggerSection & ReminderEditScreen: ÄÆ°a Cáº¥u HÃ¬nh Láº·p Lá»‹ch (Repeat) Ra NgoÃ i Cáº¥p Äá»™ Nháº¯c Nhá»Ÿ & LÃ m Sáº¡ch TimeWindow
- **NgÃ y thá»±c hiá»‡n:** 2026-10-05
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - TrÆ°á»›c Ä‘Ã¢y, `ReminderRepeatConfigSection` (láº·p 1 láº§n, hÃ ng tuáº§n, hÃ ng thÃ¡ng, hÃ ng nÄƒm) bá»‹ nhÃºng trÃ¹ng láº·p á»Ÿ cáº£ hai nhÃ¡nh con: bÃªn trong `ReminderTimeConfigSection` (cho Fixed Time) vÃ  bÃªn trong nhÃ¡nh `INTERVAL` cá»§a `ReminderTriggerSection`.
  - Cáº¥u trÃºc nÃ y lÃ m rÃ² rá»‰ trÃ¡ch nhiá»‡m (props drilling hÆ¡n 12 tham sá»‘ repeat qua láº¡i), vi pháº¡m Single Responsibility Principle vÃ  gÃ¢y rá»‘i ráº¯m tráº£i nghiá»‡m ngÆ°á»i dÃ¹ng.
  - Vá» báº£n cháº¥t, **má»i nháº¯c nhá»Ÿ Ä‘á»u cÃ³ chu ká»³ ngÃ y láº·p láº¡i** (Má»™t láº§n vÃ o ngÃ y cá»¥ thá»ƒ, hÃ ng tuáº§n theo thá»©, hÃ ng thÃ¡ng, hÃ ng nÄƒm). Sá»± khÃ¡c biá»‡t giá»¯a `FIXED_TIME` vÃ  `INTERVAL` chá»‰ náº±m á»Ÿ cÃ¡ch kÃ­ch hoáº¡t trong ngÃ y: reo vÃ o cÃ¡c má»‘c giá» cá»‘ Ä‘á»‹nh hay láº·p láº¡i theo chu ká»³ phÃºt trong cÃ¡c khung giá» hoáº¡t Ä‘á»™ng (`TimeWindow`).
  - NgoÃ i ra, trong `ReminderTimeWindowConfigSection`, tá»«ng khung giá» láº¡i cÃ³ lá»±a chá»n "Táº¥t cáº£ cÃ¡c ngÃ y" / "NgÃ y Ä‘Ã£ chá»n" vá»›i danh sÃ¡ch chá»n thá»© trong tuáº§n (`activeDays`), gÃ¢y dÆ° thá»«a vÃ  xung Ä‘á»™t vá»›i lá»‹ch láº·p ngÃ y á»Ÿ cáº¥p Ä‘á»™ nháº¯c nhá»Ÿ. File `ReminderTimeWindowDialog.kt` bá»‹ bá» quÃªn khÃ´ng sá»­ dá»¥ng (dead code).
- **Quyáº¿t Ä‘á»‹nh:**
  1. **ÄÆ°a `ReminderRepeatConfigSection` ra ngoÃ i `ReminderEditScreen`:**
     - Äáº·t lÃ m Section Ä‘á»™c láº­p ngay dÆ°á»›i `ReminderBasicInfoSection`, Ä‘á»‹nh nghÄ©a chu ká»³ láº·p láº¡i cáº¥p cao cho toÃ n bá»™ nháº¯c nhá»Ÿ.
     - Loáº¡i bá» hoÃ n toÃ n viá»‡c nhÃºng `ReminderRepeatConfigSection` bÃªn trong `ReminderTimeConfigSection` vÃ  bÃªn trong `ReminderTriggerSection`.
  2. **Refactor `ReminderTriggerSection` & `ReminderTimeConfigSection`:**
     - Loáº¡i bá» toÃ n bá»™ cÃ¡c tham sá»‘ repeat thá»«a khá»i `ReminderTriggerSection` vÃ  `ReminderTimeConfigSection`.
     - `ReminderTimeConfigSection` chá»‰ cÃ²n nhiá»‡m vá»¥ quáº£n lÃ½ danh sÃ¡ch má»‘c giá» cá»‘ Ä‘á»‹nh trong ngÃ y (`fixedTimes`).
     - `ReminderTriggerSection` chá»‰ cÃ²n quáº£n lÃ½ chuyá»ƒn Ä‘á»•i giá»¯a Fixed Time vÃ  Interval (chu ká»³ phÃºt + khung giá» hoáº¡t Ä‘á»™ng `TimeWindow`).
  3. **LÃ m sáº¡ch `ReminderTimeWindowConfigSection`:**
     - Loáº¡i bá» hoÃ n toÃ n UI chá»n thá»©/ngÃ y (`activeDays`), Segmented Button "Táº¥t cáº£ cÃ¡c ngÃ y / NgÃ y Ä‘Ã£ chá»n" vÃ  FlowRow thá»© trong tuáº§n.
     - Má»—i khung giá» chá»‰ Ä‘Æ¡n giáº£n quáº£n lÃ½ khoáº£ng thá»i gian trong ngÃ y: Giá» báº¯t Ä‘áº§u (`startTime`) vÃ  Giá» káº¿t thÃºc (`endTime`).
  4. **Dá»n dáº¹p code rÃ¡c:**
     - XÃ³a bá» file dead code `ReminderTimeWindowDialog.kt`.
     - Dá»n dáº¹p cÃ¡c chuá»—i tÃ i nguyÃªn khÃ´ng cÃ²n sá»­ dá»¥ng (`time_window_all_days`, `time_window_selected_days`, `time_window_active_days`, `clear_time_window`, `configure_time_window`) trong `strings.xml` vÃ  `values-vi/strings.xml`.
     - Äá»“ng bá»™ láº¡i `ReminderTriggerEditScreen.kt`.

### [TDR-090] - NÃ¢ng Cáº¥p UX Tháº» Nháº¯c Nhá»Ÿ (ReminderCard): Trá»±c Quan HÃ³a Äáº§y Äá»§ ThÃ´ng Tin Láº·p Láº¡i, Khung Giá» Hoáº¡t Äá»™ng & Äáº¿m NgÆ°á»£c Láº§n Nháº¯c Tiáº¿p Theo
- **NgÃ y thá»±c hiá»‡n:** 2026-10-02
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - `ReminderCard` trÃªn danh sÃ¡ch nháº¯c nhá»Ÿ trÆ°á»›c Ä‘Ã¢y chá»‰ hiá»ƒn thá»‹ má»‘c giá»/phÃºt láº·p cÆ¡ báº£n vÃ  tiÃªu Ä‘á».
  - NgÆ°á»i dÃ¹ng khÃ³ náº¯m báº¯t nhanh:
    + Chu ká»³ ngÃ y láº·p láº¡i cá»¥ thá»ƒ (HÃ ng ngÃ y, T2-T6, T7-CN, ngÃ y trong thÃ¡ng, hay ngÃ y cá»¥ thá»ƒ trong nÄƒm).
    + Khung thá»i gian hoáº¡t Ä‘á»™ng Ã¡p dá»¥ng (`timeWindows`) Ä‘á»‘i vá»›i cÃ¡c nháº¯c nhá»Ÿ láº·p Ä‘á»‹nh ká»³ (Interval).
    + Thá»i Ä‘iá»ƒm vÃ  khoáº£ng thá»i gian cÃ²n láº¡i Ä‘áº¿n lÆ°á»£t kÃ­ch hoáº¡t tiáº¿p theo ("Láº§n tá»›i sau bao lÃ¢u", "Äang hoÃ£n Ä‘áº¿n máº¥y giá»", "ÄÃ£ táº¯t").
  - Cáº§n nÃ¢ng cáº¥p giao diá»‡n `ReminderCard` theo tiÃªu chÃ­ "Äáº§y Ä‘á»§ thÃ´ng tin nhÆ°ng gá»n gÃ ng, thoÃ¡ng Ä‘Ã£ng vÃ  cÃ³ tráº­t tá»± thá»‹ giÃ¡c cao".
- **Quyáº¿t Ä‘á»‹nh:**
  1. **TÃ¡ch biá»‡t vÃ  cáº¥u trÃºc láº¡i cÃ¡c lá»›p thÃ´ng tin trÃªn `ReminderCard`:**
     - **HÃ ng Header / Meta Badges (`FlowRow`):**
       - Badge chÃ­nh: Káº¿t há»£p icon + má»‘c giá»/táº§n suáº¥t interval + chu ká»³ láº·p láº¡i (vÃ­ dá»¥: `08:00, 14:00 â€¢ T2 - T6` hoáº·c `Má»—i 2 giá» â€¢ HÃ ng ngÃ y`).
       - Time Window Badge: Hiá»ƒn thá»‹ khung giá» Ã¡p dá»¥ng náº¿u cÃ³ (vÃ­ dá»¥: `08:00 - 17:30`, `08:00 - 11:30, 13:30 - 17:30`).
       - Snoozing Badge: Hiá»ƒn thá»‹ ná»•i báº­t khi Ä‘ang á»Ÿ tráº¡ng thÃ¡i hoÃ£n (`Äang hoÃ£n (10:35)`).
       - Badge má»©c Ä‘á»™ kháº©n cáº¥p (`Kháº©n cáº¥p`) vÃ  chuÃ´ng liÃªn tá»¥c (`ChuÃ´ng liÃªn tá»¥c`) khi Ä‘Æ°á»£c cáº¥u hÃ¬nh.
     - **ThÃ¢n tháº» (Body):** TiÃªu Ä‘á» (SemiBold) vÃ  tÃ³m táº¯t ghi chÃº (náº¿u cÃ³, tá»‘i Ä‘a 2 dÃ²ng).
     - **HÃ ng Footer Ä‘áº¿m ngÆ°á»£c láº§n nháº¯c káº¿ tiáº¿p (`NextTriggerFooter`):**
       - Tá»± Ä‘á»™ng tÃ­nh toÃ¡n má»‘c kÃ­ch hoáº¡t káº¿ tiáº¿p tá»« `TimeTrigger.getNextOccurrence` hoáº·c `snoozeNextTriggerTime`.
       - Hiá»ƒn thá»‹ trá»±c quan:
         + HÃ´m nay: `Láº§n tá»›i: HÃ´m nay 14:00 (sau 2 giá» 15 phÃºt)`
         + NgÃ y mai: `Láº§n tá»›i: NgÃ y mai 08:00 (sau 14 giá»)`
         + NgÃ y trong tuáº§n tá»›i: `Láº§n tá»›i: T6, 24/10 lÃºc 09:00 (sau 3 ngÃ y)`
         + Äang hoÃ£n: `BÃ¡o láº¡i: 10:35 (cÃ²n 12 phÃºt)`
         + ÄÃ£ táº¯t: `ÄÃ£ táº¯t` (vá»›i icon `NotificationsPaused` vÃ  mÃ u muted)
         + ÄÃ£ hoÃ n táº¥t cÃ¡c lÆ°á»£t: `ÄÃ£ hoÃ n thÃ nh cÃ¡c lÆ°á»£t nháº¯c`
  2. **Táº¡o bá»™ cÃ´ng cá»¥ Ä‘á»‹nh dáº¡ng chuyÃªn dá»¥ng `ReminderCardFormatter`:**
     - Äá»™c láº­p, thuáº§n khiáº¿t (pure functions) há»— trá»£ Ä‘a ngÃ´n ngá»¯ (Tiáº¿ng Viá»‡t & Tiáº¿ng Anh).
     - Viáº¿t bá»™ Unit Test toÃ n diá»‡n `ReminderCardFormatterTest` bao phá»§ 100% cÃ¡c ká»‹ch báº£n Ä‘á»‹nh dáº¡ng thá»i gian, chu ká»³ vÃ  Ä‘áº¿m ngÆ°á»£c.

### [TDR-089] - Tá»‘i Æ¯u UX BiÃªn Táº­p Nháº¯c Nhá»Ÿ: ÄÆ°a Cá»¥m Äiá»u Kiá»‡n KÃ­ch Hoáº¡t Ra Trá»±c Tiáº¿p MÃ n HÃ¬nh BiÃªn Táº­p, Chuáº©n HÃ³a NhÃ£n Tab Selector & Há»— Trá»£ Äáº§y Äá»§ Chu Ká»³ Láº·p Cho Interval
- **NgÃ y thá»±c hiá»‡n:** 2026-10-01
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - TrÆ°á»›c Ä‘Ã¢y, `ReminderEditScreen` chá»‰ hiá»ƒn thá»‹ tÃ³m táº¯t Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t vÃ  ngÆ°á»i dÃ¹ng pháº£i nháº¥n "Chá»‰nh sá»­a" Ä‘á»ƒ má»Ÿ má»™t mÃ n hÃ¬nh phá»¥ riÃªng biá»‡t (`ReminderTriggerEditScreen`).
  - Trong `ReminderTriggerTypeSelector`, cáº£ 2 tab `FIXED_TIME` vÃ  `INTERVAL` Ä‘á»u hiá»ƒn thá»‹ chung tiÃªu Ä‘á» "Thá»i gian" (`R.string.trigger_time`) vÃ  cÃ¹ng má»™t icon `ReMindIcons.AccessTime`, gÃ¢y nháº§m láº«n thá»‹ giÃ¡c.
  - BÃªn cáº¡nh Ä‘Ã³, tab `INTERVAL` (Láº·p Ä‘á»‹nh ká»³) trÆ°á»›c Ä‘Ã¢y chÆ°a cÃ³ thiáº¿t láº­p chu ká»³ láº·p láº¡i Ä‘a dáº¡ng nhÆ° bÃªn `FIXED_TIME` (Má»™t láº§n, HÃ ng tuáº§n, HÃ ng thÃ¡ng, HÃ ng nÄƒm), khiáº¿n ngÆ°á»i dÃ¹ng khÃ´ng thá»ƒ cáº¥u hÃ¬nh cÃ¡c thÃ³i quen láº·p láº¡i theo chu ká»³ ngÃ y cá»¥ thá»ƒ (vÃ­ dá»¥: uá»‘ng nÆ°á»›c Ä‘á»‹nh ká»³ chá»‰ vÃ o cÃ¡c ngÃ y Ä‘i lÃ m thá»© 2, 4, 6 hoáº·c chá»‰ vÃ o ngÃ y cá»¥ thá»ƒ trong thÃ¡ng).
  - Hiá»‡n táº¡i há»‡ thá»‘ng há»— trá»£ 2 loáº¡i Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t thá»i gian (`FIXED_TIME` vÃ  `INTERVAL`), pháº¡m vi cáº¥u hÃ¬nh chÆ°a quÃ¡ phá»©c táº¡p (chÆ°a cÃ³ Ä‘iá»u kiá»‡n ngoáº¡i vi phá»©c táº¡p nhÆ° Geofence/Wi-Fi).
  - Viá»‡c tÃ¡ch ra mÃ n hÃ¬nh phá»¥ táº¡o thÃªm má»™t bÆ°á»›c Ä‘iá»u hÆ°á»›ng dÆ° thá»«a, lÃ m giÃ¡n Ä‘oáº¡n tráº£i nghiá»‡m ngÆ°á»i dÃ¹ng khi táº¡o hoáº·c sá»­a nháº¯c nhá»Ÿ.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Chuáº©n hÃ³a nhÃ£n & icon cá»§a bá»™ chá»n Tab (`ReminderTriggerTypeSelector`):**
     - `FIXED_TIME` -> TiÃªu Ä‘á» "Má»‘c thá»i gian" (`R.string.trigger_fixed_time` / "Fixed Time"), icon `ReMindIcons.AccessTime`.
     - `INTERVAL` -> TiÃªu Ä‘á» "Láº·p Ä‘á»‹nh ká»³" (`R.string.trigger_interval` / "Interval"), icon `ReMindIcons.Timer`.
  2. **Äá»“ng bá»™ toÃ n diá»‡n cáº¥u hÃ¬nh chu ká»³ láº·p (`RepeatMode`) cho Interval:**
     - TÃ­ch há»£p Ä‘áº§y Ä‘á»§ `ReminderRepeatConfigSection` vÃ o tab `INTERVAL` (há»— trá»£ `RepeatMode.NONE` vá»›i chá»n ngÃ y cá»¥ thá»ƒ `exactDate`, `RepeatMode.DAILY`, `RepeatMode.WEEKLY` vá»›i chá»n thá»© trong tuáº§n, `RepeatMode.MONTHLY` vá»›i lÆ°á»›i ngÃ y 1..31, vÃ  `RepeatMode.YEARLY` vá»›i danh sÃ¡ch ngÃ y thÃ¡ng).
     - NÃ¢ng cáº¥p `TimeTrigger.kt`: Bá»• sung `isDateValid(date: LocalDate)` vÃ  `getNextValidDate(from: LocalDate): LocalDate?` Ä‘á»ƒ há»— trá»£ tÃ­nh toÃ¡n ngÃ y há»£p lá»‡ theo chu ká»³ cho cáº£ Fixed Time vÃ  Interval.
     - Thuáº­t toÃ¡n `getNextIntervalOccurrence()` kiá»ƒm tra ngÃ y há»£p lá»‡ theo `repeatMode` trÆ°á»›c khi tÃ­nh toÃ¡n cÃ¡c má»‘c khoáº£ng cÃ¡ch (interval) vÃ  khung giá» hoáº¡t Ä‘á»™ng (timeWindows).
  3. **ÄÆ°a toÃ n bá»™ cá»¥m Ä‘iá»u khiá»ƒn kÃ­ch hoáº¡t (`ReminderTriggerSection`) ra ngoÃ i mÃ n hÃ¬nh `ReminderEditScreen`:**
     - TÃ­ch há»£p trá»±c tiáº¿p `ReminderTriggerSection` vÃ o danh sÃ¡ch cuá»™n cá»§a `ReminderEditScreen`, cho phÃ©p ngÆ°á»i dÃ¹ng thay Ä‘á»•i tab, chá»n giá»/ngÃ y láº·p láº¡i, hoáº·c cÃ i Ä‘áº·t phÃºt láº·p/khung giá» Ã¡p dá»¥ng ngay trong má»™t mÃ n hÃ¬nh duy nháº¥t.
     - Giá»¯ nguyÃªn kiáº¿n trÃºc module vÃ  luá»“ng dá»¯ liá»‡u chuáº©n `ReminderEditViewModel` vá»›i Ä‘áº§y Ä‘á»§ cÃ¡c tÆ°Æ¡ng tÃ¡c thá»i gian thá»±c.
  4. **Tá»‘i Æ°u hÃ³a vÃ  tÃ¡i sá»­ dá»¥ng:**
     - `ReminderTriggerEditScreen` tÃ¡i sá»­ dá»¥ng trá»±c tiáº¿p component `ReminderTriggerSection`, Ä‘áº£m báº£o khÃ´ng trÃ¹ng láº·p code giao diá»‡n.
     - Khi tÆ°Æ¡ng lai cáº§n má»Ÿ rá»™ng thÃªm cÃ¡c Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t phá»©c táº¡p (nhÆ° vá»‹ trÃ­ Ä‘á»‹a lÃ½, máº¡ng Wi-Fi, Bluetooth), há»‡ thá»‘ng cÃ³ thá»ƒ linh hoáº¡t Ä‘Æ°a láº¡i mÃ n hÃ¬nh chuyÃªn biá»‡t mÃ  khÃ´ng áº£nh hÆ°á»Ÿng tá»›i kiáº¿n trÃºc hiá»‡n táº¡i.

### [TDR-088] - TÃ¡i Cáº¥u TrÃºc VÃ²ng Äá»i Reminder: Loáº¡i Bá» `isCompleted`, Thá»‘ng Nháº¥t Tráº¡ng ThÃ¡i Qua `isEnabled` vÃ  Tinh Gá»n UX Danh SÃ¡ch
- **NgÃ y thá»±c hiá»‡n:** 2026-10-01
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - TrÆ°á»›c Ä‘Ã¢y, `Reminder` tá»“n táº¡i Ä‘á»“ng thá»i 2 trÆ°á»ng tráº¡ng thÃ¡i: `isEnabled: Boolean` vÃ  `isCompleted: Boolean`.
  - PhÃ¢n tÃ­ch nghiá»‡p vá»¥ chá»‰ ra sá»± dÆ° thá»«a vÃ  xung Ä‘á»™t logic:
    + Vá»›i nháº¯c nhá»Ÿ 1 láº§n (`!isRecurring`): Khi chuÃ´ng kÃªu xong hoáº·c ngÆ°á»i dÃ¹ng dismiss, nháº¯c nhá»Ÿ coi nhÆ° xong viá»‡c. Cáº£ `isCompleted = true` vÃ  `isEnabled = false` Ä‘á»u biá»ƒu diá»…n cÃ¹ng má»™t tráº¡ng thÃ¡i ngá»«ng kÃ­ch hoáº¡t.
    + Vá»›i nháº¯c nhá»Ÿ láº·p láº¡i (`isRecurring` / `INTERVAL`): Viá»‡c Ä‘Ã¡nh dáº¥u `isCompleted = true` trÆ°á»›c Ä‘Ã¢y khiáº¿n bá»™ láº­p lá»‹ch (`ReminderSchedulerImpl`) bá» qua khÃ´ng láº­p lá»‹ch cho chu ká»³ tiáº¿p theo (`if (!reminder.isEnabled || reminder.isCompleted) return`).
    + Sá»± nháº­p nháº±ng nÃ y dáº«n Ä‘áº¿n viá»‡c pháº£i duy trÃ¬ logic reset chu ká»³ phá»©c táº¡p, táº¡o bug tiá»m áº©n.
  - Vá» máº·t UI/UX:
    + KhÃ¡c vá»›i BÃ¡o thá»©c (ngÆ°á»i dÃ¹ng báº­t/táº¯t liÃªn tá»¥c hÃ ng ngÃ y/cuá»‘i tuáº§n), Nháº¯c nhá»Ÿ lÃ  nhiá»‡m vá»¥/thÃ³i quen cÃ³ táº§n suáº¥t táº¯t ráº¥t tháº¥p.
    + Viá»‡c Ä‘áº·t `LiteverSwitch` ngay trÃªn tá»«ng item danh sÃ¡ch (`ReminderCard`) dá»… gÃ¢y cháº¡m nháº§m (accidental click) vÃ  lÃ m giao diá»‡n bá»‹ rá»‘i máº¯t.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Loáº¡i bá» hoÃ n toÃ n trÆ°á»ng `isCompleted` khá»i toÃ n bá»™ há»‡ thá»‘ng:**
     - Domain Model: XÃ³a `isCompleted` trong `Reminder.kt`.
     - Database & Migration: XÃ³a `isCompleted` trong `ReminderEntity.kt`, `ReminderDao.kt`. TÄƒng Room database version tá»« 7 lÃªn 8 vá»›i `MIGRATION_7_8` (tÃ¡i táº¡o báº£ng an toÃ n báº±ng SQLite table recreation theo Ä‘Ãºng chuáº©n Room).
     - Data Mapping: XÃ³a bá» `isCompleted` trong `ReminderMapper.kt`.
     - Logic & Scheduler: Cáº­p nháº­t `ReminderSchedulerImpl.kt`, `ReminderTriggerEvaluator.kt`, `ReminderSyncManager.kt`, `ReminderReceiver.kt`, `ReminderAlertManager.kt`, `ReminderRingingViewModel.kt`.
       * Nháº¯c nhá»Ÿ 1 láº§n: Khi káº¿t thÃºc -> cáº­p nháº­t `isEnabled = false`.
       * Nháº¯c nhá»Ÿ láº·p láº¡i: Giá»¯ nguyÃªn `isEnabled = true`, chá»‰ tá»‹nh tiáº¿n/cáº­p nháº­t `lastTriggeredTime` hoáº·c `snoozeNextTriggerTime`.
  2. **Tinh gá»n giao diá»‡n vÃ  hÃ nh vi UI:**
     - `ReminderCard.kt`: Gá»¡ bá» `LiteverSwitch`, loáº¡i bá» `onToggle`. Hiá»ƒn thá»‹ lÃ m má» mÃ u (alpha 0.6f / onSurface) khi `!isEnabled`. NÃºt menu 3 cháº¥m Ä‘Æ°á»£c cÄƒn pháº£i gá»n gÃ ng, thoÃ¡ng Ä‘Ã£ng.
     - `ReminderActionBottomSheet.kt`: Thay tháº¿ tÃ¹y chá»n "ÄÃ¡nh dáº¥u hoÃ n thÃ nh" báº±ng "Táº¯t nháº¯c nhá»Ÿ" / "Báº­t nháº¯c nhá»Ÿ" (`onToggleEnabledClick`).
     - `ReminderEditScreen.kt`: Bá»• sung nÃºt báº¥m dáº¡ng Outlined/Primary á»Ÿ cuá»‘i mÃ n hÃ¬nh chá»‰nh sá»­a ("Táº¯t nháº¯c nhá»Ÿ" / "Báº­t nháº¯c nhá»Ÿ") cho cÃ¡c nháº¯c nhá»Ÿ Ä‘Ã£ tá»“n táº¡i (`uiState.id != 0L`).
     - `ReminderListViewModel.kt`: XÃ³a bá» logic lá»c theo `isCompleted`, xÃ³a `deleteCompletedReminders()`, `toggleCompleted()`.
  3. **Kiá»ƒm thá»­ & Äáº£m báº£o cháº¥t lÆ°á»£ng:**
     - Cáº­p nháº­t 100% test suites: `ReminderTest`, `ReminderSnoozeChallengerTest`, `ReminderDaoTest`, `ReminderDatabaseChallengerTest`, `ReminderRepositoryImplTest`, `AlarmReminderConcurrencyTest`, `ReminderSyncManagerTest`, `ReminderReceiverTest`, `ReminderSchedulerAndServiceTest`, `ReminderSchedulerImplTest`, `ReminderTriggerEvaluatorTest`, `ReminderListViewModelTest`, `ReminderAdversarialReviewTest`, `ReminderPermissionChallengerTest`, `ReminderR1R2EmpiricalChallengerTest`, `ReminderRingingViewModelTest`, `ScreenshotsTest`.
     - Cháº¡y `./gradlew testDebugUnitTest` vÆ°á»£t qua 100% tests.

### [TDR-087] - Thá»‘ng nháº¥t PermissionWarningBanner vÃ o :core:designsystem vÃ  Cáº­p nháº­t TÃ­nh nÄƒng Reminders vá»›i Lá»i vÄƒn ChuyÃªn biá»‡t
- **NgÃ y thá»±c hiá»‡n:** 2026-10-01
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Cáº£ hai tÃ­nh nÄƒng BÃ¡o thá»©c (`:features:alarms`) vÃ  Nháº¯c nhá»Ÿ (`:features:reminders`) Ä‘á»u cáº§n cáº£nh bÃ¡o quyá»n há»‡ thá»‘ng bá»‹ thiáº¿u cho ngÆ°á»i dÃ¹ng, nhÆ°ng trÆ°á»›c Ä‘Ã³ má»—i module láº¡i cÃ³ banner riÃªng (`AlarmPermissionWarningBanner` vÃ  `ReminderPermissionWarningBanner`), gÃ¢y phÃ¢n máº£nh vÃ  khÃ´ng Ä‘á»“ng bá»™ vá» giao diá»‡n Design System.
  - Cáº§n phÃ¢n cáº¥p má»©c Ä‘á»™ cáº£nh bÃ¡o rÃµ rÃ ng:
    + **Critical (Lá»—i cá»‘t lÃµi)**: Thiáº¿u quyá»n thÃ´ng bÃ¡o hoáº·c quyá»n bÃ¡o thá»©c chÃ­nh xÃ¡c cho tÃ¡c vá»¥ Ä‘ang kÃ­ch hoáº¡t -> Hiá»ƒn thá»‹ cáº£nh bÃ¡o lá»—i (Ä‘á»/errorContainer) vÃ  báº¯t buá»™c ngÆ°á»i dÃ¹ng sá»­a Ä‘á»ƒ hoáº¡t Ä‘á»™ng, khÃ´ng cho áº©n.
    + **Non-critical (Cáº£nh bÃ¡o khuyáº¿n nghá»‹)**: Thiáº¿u cÃ¡c quyá»n tá»‘i Æ°u nhÆ° Tá»‘i Æ°u hÃ³a pin, Váº½ trÃªn á»©ng dá»¥ng khÃ¡c -> Hiá»ƒn thá»‹ dáº¡ng cáº£nh bÃ¡o (vÃ ng/warningContainer) kÃ¨m nÃºt "Nháº¯c tÃ´i sau" Ä‘á»ƒ áº©n táº¡m thá»i.
  - Lá»i vÄƒn trong tÃ­nh nÄƒng Nháº¯c nhá»Ÿ pháº£i phÃ¹ há»£p vá»›i bá»‘i cáº£nh nháº¯c viá»‡c/thÃ´ng bÃ¡o ("Nháº¯c nhá»Ÿ"), khÃ´ng Ä‘Æ°á»£c dÃ¹ng ngÃ´n tá»« cá»§a "BÃ¡o thá»©c".
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Chuyá»ƒn `PermissionWarningBanner` vÃ o `:core:designsystem`:**
     - Táº¡o `vn.io.litever.remind.core.designsystem.components.PermissionWarningBanner.kt`.
     - Nháº­n cÃ¡c thuá»™c tÃ­nh ngá»¯ nghÄ©a (`isCritical`, `title`, `message`, `actionLabel`, `dismissLabel`, `onDismiss`).
     - TuÃ¢n thá»§ nghiÃªm ngáº·t quy chuáº©n cá»§a Litever Design System (`LvButton`, `LvSemantic`, `LiteverTheme`).
     - XÃ³a bá» hoÃ n toÃ n cÃ¡c file banner cÅ© trong `:features:alarms` vÃ  `:features:reminders`.
  2. **Cáº­p nháº­t `:features:reminders`:**
     - Bá»• sung chuá»—i tÃ i nguyÃªn tiáº¿ng Viá»‡t vÃ  tiáº¿ng Anh dÃ nh riÃªng cho tÃ­nh nÄƒng Nháº¯c nhá»Ÿ (`reminder_permission_warning_banner_non_critical_title`, `reminder_permission_warning_banner_non_critical_desc`).
     - Má»Ÿ rá»™ng `ReminderListUiState` vá»›i `isCriticalPermissionWarning` vÃ  `isPermissionWarningDismissed`.
     - Cung cáº¥p hÃ m `dismissPermissionWarning()` trong `ReminderListViewModel` vÃ  liÃªn káº¿t vá»›i nÃºt "Nháº¯c tÃ´i sau".
  3. **Kiá»ƒm thá»­ toÃ n diá»‡n:**
     - Cáº­p nháº­t test suite `ReminderListViewModelTest`, `ReminderPermissionChallengerTest`, `ReminderR1R2EmpiricalChallengerTest`.
     - Build vÃ  cháº¡y thÃ nh cÃ´ng toÃ n bá»™ test suite vÃ  `assembleDebug`.

### [TDR-086] - Thá»‘ng nháº¥t AutoSilenceBottomSheet vÃ o :core:designsystem vÃ  Dá»n dáº¹p Code Smells / Linter Warnings táº¡i ReminderEditScreen & Navigation
- **NgÃ y thá»±c hiá»‡n:** 2026-10-01
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - `AutoSilenceBottomSheet` bÃªn BÃ¡o thá»©c (`:features:alarms`) vÃ  `ReminderAutoSilenceBottomSheetContent` bÃªn Nháº¯c nhá»Ÿ (`:features:reminders`) phá»¥c vá»¥ cÃ¹ng má»™t má»¥c Ä‘Ã­ch chá»n thá»i gian tá»± Ä‘á»™ng ngáº¯t chuÃ´ng khi ngÆ°á»i dÃ¹ng khÃ´ng tÆ°Æ¡ng tÃ¡c. Viá»‡c duplicate component nÃ y vi pháº¡m nguyÃªn táº¯c DRY vÃ  Single Source of Truth cá»§a `:core:designsystem`.
  - Chuá»—i mÃ´ táº£ hÃ nh vi cáº§n giáº£i thÃ­ch rÃµ rÃ ng cho cáº£ bÃ¡o thá»©c vÃ  nháº¯c nhá»Ÿ: "ChuÃ´ng bÃ¡o sáº½ tá»± Ä‘á»™ng táº¯t náº¿u báº¡n khÃ´ng tÆ°Æ¡ng tÃ¡c sau má»™t khoáº£ng thá»i gian."
  - MÃ n hÃ¬nh `ReminderEditScreen` vÃ  file Ä‘iá»u hÆ°á»›ng `RemindersNavigation.kt` tá»“n táº¡i nhiá»u code smell vÃ  cáº£nh bÃ¡o linter:
    + Cáº£nh bÃ¡o `Querying resource values using LocalContext.current is not configuration-aware and may return stale values if the Configuration changes` (3 vá»‹ trÃ­).
    + Sá»­ dá»¥ng `Uri.parse(string)` thay vÃ¬ extension `toUri()`.
    + Vá»‹ trÃ­ `modifier: Modifier = Modifier` khÃ´ng pháº£i lÃ  tham sá»‘ tÃ¹y chá»n Ä‘áº§u tiÃªn (vi pháº¡m Compose API guidelines).
    + 9 tham sá»‘ callback trigger thá»«a trong `ReminderEditScreen` do viá»‡c cáº¥u hÃ¬nh trigger Ä‘Ã£ tÃ¡ch sang `ReminderTriggerEditScreen`.
    + Tham sá»‘ `reminderId` vÃ  `onNavigateToSnoozeSettings` khÃ´ng cÃ²n Ä‘Æ°á»£c sá»­ dá»¥ng trong `ReminderEditRoute` vÃ  `remindersGraph`.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **ÄÆ°a `AutoSilenceBottomSheet` vÃ  `AutoSilenceBottomSheetContent` vÃ o `:core:designsystem`:**
     - Táº¡o `vn.io.litever.remind.core.designsystem.components.AutoSilenceBottomSheet.kt`.
     - Há»— trá»£ linh hoáº¡t danh sÃ¡ch tÃ¹y chá»n (há»— trá»£ `0` tÆ°Æ¡ng á»©ng "KhÃ´ng bao giá»" qua `auto_silence_never`).
     - Bá»• sung resource strings chuáº©n hÃ³a trong cáº£ `values/strings.xml` vÃ  `values-vi/strings.xml`.
     - XÃ³a bá» hoÃ n toÃ n implementation trÃ¹ng láº·p bÃªn `:features:alarms` vÃ  `ReminderAutoSilenceBottomSheetContent` trong `ReminderAlertConfigSection.kt`.
  2. **Dá»n dáº¹p triá»‡t Ä‘á»ƒ Code Smells & Linter trong `:features:reminders`:**
     - Sá»­ dá»¥ng `stringResource(R.string.default_ringtone)` thay cho `context.getString(...)` Ä‘áº£m báº£o configuration-aware khi Ä‘á»•i locale / theme.
     - Sá»­ dá»¥ng `uiState.ringtoneUri.toUri()`.
     - Chuáº©n hÃ³a vá»‹ trÃ­ `modifier: Modifier = Modifier` thÃ nh tham sá»‘ tÃ¹y chá»n Ä‘áº§u tiÃªn theo Compose API Guidelines.
     - Loáº¡i bá» hoÃ n toÃ n 9 callbacks thá»«a (`onTriggerTypeSelected`, `onTimeChange`, `onDateChange`, `onRepeatDaysChange`, `onRepeatDayToggle`, `onRepeatIntervalChange`, `onMonthlyDayToggle`, `onAddYearlyDate`, `onRemoveYearlyDate`) khá»i `ReminderEditScreen`, `ReminderEditRoute` vÃ  cÃ¡c previews.
     - Loáº¡i bá» `reminderId` vÃ  `onNavigateToSnoozeSettings` thá»«a trong `ReminderEditRoute` vÃ  `remindersGraph`.
     - XÃ³a cÃ¡c unused imports.
  3. **Kiá»ƒm thá»­ tá»± Ä‘á»™ng:**
     - 473/473 unit test tasks toÃ n bá»™ há»‡ thá»‘ng passed 100%.

### [TDR-070] - TÃ¡ch biá»‡t thanh Ã‚m lÆ°á»£ng vÃ  Rung thÃ nh 2 thÃ nh pháº§n Ä‘á»™c láº­p kÃ¨m nÃºt phÃ¡t thá»­ Ã¢m thanh (Play/Stop Preview) cho Alarm vÃ  Reminder
- **NgÃ y thá»±c hiá»‡n:** 2026-10-01
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - TrÆ°á»›c Ä‘Ã¢y, á»Ÿ cáº£ `AlarmEditScreen` vÃ  `ReminderEditScreen` (`ReminderAlertConfigSection`), thanh trÆ°á»£t Ã¢m lÆ°á»£ng vÃ  icon rung bá»‹ gá»™p chung vÃ o má»™t hÃ ng (`[VolumeIcon] [Slider] [VibrationIcon]`). Bá»‘ cá»¥c nÃ y gÃ¢y cháº­t chá»™i, khÃ³ thao tÃ¡c vÃ  khÃ´ng táº¡o sá»± trá»±c quan cho ngÆ°á»i dÃ¹ng khi muá»‘n báº­t/táº¯t rung Ä‘á»™c láº­p.
  - Äá»“ng thá»i, ngÆ°á»i dÃ¹ng cáº§n cÃ³ kháº£ nÄƒng nghe thá»­ trá»±c tiáº¿p Ã¢m thanh chuÃ´ng á»Ÿ má»©c Ã¢m lÆ°á»£ng vá»«a chá»n ngay táº¡i thanh Ã¢m lÆ°á»£ng.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **TÃ¡ch riÃªng 2 hÃ ng cÃ i Ä‘áº·t:**
     - **HÃ ng Ã‚m lÆ°á»£ng:** Gá»“m icon Ã¢m lÆ°á»£ng (`VolumeOff`/`VolumeUp`), thanh trÆ°á»£t `Slider`, vÃ  á»Ÿ cuá»‘i hÃ ng lÃ  nÃºt phÃ¡t thá»­ Ã¢m thanh dáº¡ng icon button (`ReMindSettingIcon`) vá»›i icon `PlayArrow` (khi dá»«ng) vÃ  `Pause`/`Stop` (khi Ä‘ang phÃ¡t cÃ³ highlight mÃ u container).
     - **HÃ ng Rung:** Trá»Ÿ thÃ nh má»™t hÃ ng cÃ i Ä‘áº·t riÃªng biá»‡t, cÃ³ tiÃªu Ä‘á», mÃ´ táº£ tráº¡ng thÃ¡i, vÃ  cÃ´ng táº¯c `Switch`/`LiteverSwitch` rÃµ rÃ ng, tiá»‡n lá»£i.
  2. **Ãp dá»¥ng Ä‘á»“ng bá»™ cho cáº£ 2 module `:features:alarms` vÃ  `:features:reminders`:**
     - Trong `AlarmEditScreen`: TÃ¡ch Row 3 thÃ nh Volume Slider + Play button á»Ÿ Ä‘uÃ´i, Row 4 thÃ nh `AlarmSettingRow` cho Rung.
     - Trong `ReminderAlertConfigSection`: Cháº¿ Ä‘á»™ Ä‘á»• chuÃ´ng hiá»ƒn thá»‹ hÃ ng Volume Slider + Play button riÃªng, vÃ  cáº£ 2 cháº¿ Ä‘á»™ (Äá»• chuÃ´ng & ThÃ´ng bÃ¡o thÆ°á»ng) Ä‘á»u hiá»ƒn thá»‹ hÃ ng Rung riÃªng biá»‡t, nháº¥t quÃ¡n.
  3. **TÃ­ch há»£p `AudioPlayer` vÃ o `ReminderEditViewModel`:**
     - Inject `AudioPlayer` tá»« `:core:common` vÃ o `ReminderEditViewModel`.
     - Cung cáº¥p `toggleRingtonePlayback()`, `startRingtonePlayback()`, `stopRingtonePlayback()`.
     - Tá»± Ä‘á»™ng ngáº¯t phÃ¡t Ã¢m thanh khi Ä‘á»•i chuÃ´ng, Ä‘á»•i rung, Ä‘á»•i kiá»ƒu thÃ´ng bÃ¡o, lÆ°u, há»§y, hoáº·c rá»i khá»i mÃ n hÃ¬nh (`onCleared`, `onDispose`).
  4. **Kiá»ƒm thá»­ tá»± Ä‘á»™ng:**
     - Viáº¿t Ä‘áº§y Ä‘á»§ unit tests cho `ReminderEditViewModelTest` Ä‘áº£m báº£o logic báº­t/táº¯t phÃ¡t chuÃ´ng vÃ  ngáº¯t chuÃ´ng an toÃ n.
     - ToÃ n bá»™ 473/473 tasks unit tests cá»§a project pass 100%.
- **Há»‡ quáº£:**
  - Giao diá»‡n biÃªn táº­p bÃ¡o thá»©c vÃ  nháº¯c nhá»Ÿ trá»Ÿ nÃªn thoÃ¡ng Ä‘Ã£ng, dá»… kiá»ƒm soÃ¡t Ã¢m lÆ°á»£ng vÃ  rung Ä‘á»™c láº­p.
  - NgÆ°á»i dÃ¹ng dá»… dÃ ng nghe thá»­ Ã¢m thanh chuÃ´ng trá»±c tiáº¿p ngay táº¡i vá»‹ trÃ­ Ä‘iá»u chá»‰nh Ã¢m lÆ°á»£ng.

### [TDR-069] - TÃ¡i cáº¥u trÃºc CSDL nháº¯c nhá»Ÿ (Reminders): TÃ¡ch báº¡ch FIXED_TIME & INTERVAL, há»— trá»£ Multi-day Recurrence vÃ  Ä‘á»“ng bá»™ Room Architecture
- **NgÃ y thá»±c hiá»‡n:** 2026-09-29
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Sau commit `82167122df0a9b3c4864400cfde2142852b981e2`, cáº¥u trÃºc dá»¯ liá»‡u reminder Ä‘Ã£ loáº¡i bá» cÃ¡c trigger Location/Wifi Ä‘á»ƒ táº­p trung vÃ o TimeTrigger.
  - Tuy nhiÃªn, viá»‡c gá»™p chung cÃ¡c loáº¡i kÃ­ch hoáº¡t vÃ  thiáº¿u sá»± phÃ¢n tÃ¡ch giá»¯a má»‘c giá» tÄ©nh (`FIXED_TIME`) vÃ  khoáº£ng láº·p Ä‘á»™ng (`INTERVAL`) gÃ¢y xung Ä‘á»™t logic nghiá»‡p vá»¥: `FIXED_TIME` lÃ  má»‘c giá» cá»‘ Ä‘á»‹nh (khÃ´ng bá»‹ trÃ´i giá» náº¿u lá»¡ háº¹n), trong khi `INTERVAL` tÃ­nh khoáº£ng thá»i gian trÃ´i qua giá»¯a cÃ¡c láº§n thá»±c hiá»‡n vÃ  chá»‰ kÃ­ch hoáº¡t trong cÃ¡c khung giá» giá»›i háº¡n (`time_windows`).
  - Nhu cáº§u láº·p láº¡i Ä‘á»‹nh ká»³ phá»©c táº¡p: Láº·p theo thÃ¡ng (cho phÃ©p chá»n nhiá»u ngÃ y trong thÃ¡ng `monthlyDays: List<Int>`), láº·p theo nÄƒm (cho phÃ©p chá»n nhiá»u ngÃ y trong nÄƒm `yearlyDates: List<ReminderMonthDay>`), láº·p theo tuáº§n (`repeatDays: List<DayOfWeek>`).
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Chuáº©n hoÃ¡ Schema Room Database & Entity (`reminders` table):**
     - Bá»• sung `trigger_type` (`FIXED_TIME` | `INTERVAL`).
     - TÃ¡ch trÆ°á»ng `fixed_times: List<String>?` (lÆ°u cÃ¡c má»‘c giá» cá»‘ Ä‘á»‹nh dáº¡ng HH:mm) vÃ  `interval_minutes: Int?` (chu ká»³ láº·p láº¡i theo phÃºt).
     - Bá»• sung `time_windows: List<TimeWindow>?` quy Ä‘á»‹nh khung giá» hiá»‡u lá»±c (`startTime`, `endTime`, `activeDays`) cho `INTERVAL`.
     - Há»— trá»£ Ä‘a ngÃ y láº·p láº¡i: `repeat_values: List<String>?` lÆ°u trá»¯ `monthlyDays` (vÃ­ dá»¥ `["1", "15", "30"]`) vÃ  `yearlyDates` (vÃ­ dá»¥ `["MM-dd", ...]`), káº¿t há»£p `repeat_days` cho WEEKLY.
     - Äá»“ng bá»™ cÃ¡c thuá»™c tÃ­nh rÃ ng buá»™c vÃ  hÃ nh vi: `urgency_level` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), `checklist` (danh sÃ¡ch cÃ´ng viá»‡c con), `snooze_config` (`intervalMinutes`, `maxCount`), `is_persistent_alert`.
  2. **NÃ¢ng cáº¥p Migration Room DB (`MIGRATION_4_5`):**
     - Äáº£m báº£o migration an toÃ n, tá»± Ä‘á»™ng migrate dá»¯ liá»‡u cÅ© tá»« `time_trigger_times` vÃ  `time_trigger_repeat_interval` sang `fixed_times` / `repeat_values` mÃ  khÃ´ng lÃ m máº¥t mÃ¡t dá»¯ liá»‡u ngÆ°á»i dÃ¹ng.
  3. **Äá»“ng bá»™ Domain & UI/ViewModel Layer:**
     - Tá»‘i Æ°u `TimeTrigger` vá»›i cÃ¡c helper `isRecurring`, há»— trá»£ Ä‘áº§y Ä‘á»§ `monthlyDays`, `yearlyDates`.
     - Cáº­p nháº­t `ReminderEditViewModel` vÃ  `ReminderTriggerEditScreen`: Há»— trá»£ giao diá»‡n chá»n nhiá»u ngÃ y trong thÃ¡ng vÃ  nhiá»u ngÃ y trong nÄƒm linh hoáº¡t.
     - **UI Multi-Time Targets cho `FIXED_TIME`**: Bá»• sung bá»™ chá»n nhiá»u má»‘c giá» tÄ©nh trong ngÃ y (`fixedTimes: List<LocalTime>`), há»— trá»£ thÃªm/xÃ³a/sá»­a tá»«ng má»‘c giá» dáº¡ng chip trá»±c quan, Ä‘á»“ng bá»™ lÆ°u trá»¯ vÃ o `TimeTrigger.time`.
     - **NÃ¢ng cáº¥p `ReminderCard`**: TÃ¡ch báº¡ch hiá»ƒn thá»‹ `FIXED_TIME` (tÃ³m táº¯t cÃ¡c má»‘c giá» vÃ  chu ká»³) vÃ  `INTERVAL` (chu ká»³ láº·p + khung giá» hiá»‡u lá»±c), bá»• sung Urgency badge (`HARD`) vÃ  Persistent Alert badge ("ChuÃ´ng liÃªn tá»¥c").
     - Cáº­p nháº­t `ReminderSyncManager` vÃ  `ReminderAlertManager`: Xá»­ lÃ½ mÆ°á»£t mÃ  khi app má»Ÿ hoáº·c alarm kÃ­ch hoáº¡t.
  4. **Báº£o trÃ¬ & Tinh giáº£n Test Suite:**
     - XoÃ¡ bá» cÃ¡c unit test má»“ cÃ´i phá»¥ thuá»™c Location/Wifi cÅ© trong `:app` vÃ  `:core:alarm`.
     - Cáº­p nháº­t dependency `libs.junit` cho `:features:today` vÃ  `:features:mission`.
     - ÄÆ°a toÃ n bá»™ 475 test task cá»§a project vÆ°á»£t qua 100% khÃ´ng cÃ³ cáº£nh bÃ¡o hay lá»—i tiá»m áº©n.
- **Há»‡ quáº£:**
  - Há»‡ thá»‘ng dá»¯ liá»‡u nháº¯c nhá»Ÿ Ä‘áº¡t chuáº©n kiáº¿n trÃºc sáº¡ch (Clean Architecture), tÃ¡ch báº¡ch rÃµ rÃ ng giá»¯a thá»i gian tÄ©nh vÃ  chu ká»³ Ä‘á»™ng.
  - Há»— trá»£ toÃ n diá»‡n cÃ¡c ca sá»­ dá»¥ng phá»©c táº¡p cá»§a ngÆ°á»i dÃ¹ng (uá»‘ng nÆ°á»›c má»—i 2 tiáº¿ng trong giá» lÃ m viá»‡c, nháº¯c Ä‘Ã³ng tiá»n nhÃ  ngÃ y 5 vÃ  20 hÃ ng thÃ¡ng, nháº¯c sinh nháº­t nhiá»u ngÆ°á»i thÃ¢n trong nÄƒm).
  - ToÃ n bá»™ test suite xanh 100%, sáºµn sÃ ng cho release.

### [TDR-067] - TÃ¡i cáº¥u trÃºc GeneralSettingsScreen: Thay tháº¿ SegmentedButton báº±ng ReMindSettingsItem & Selection Dialog
- **NgÃ y thá»±c hiá»‡n:** 2026-09-17
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Trong `GeneralSettingsScreen`, viá»‡c láº·p láº¡i 3 hÃ ng `SingleChoiceSegmentedButtonRow` liÃªn tiáº¿p (Äá»‹nh dáº¡ng giá», Cháº¿ Ä‘á»™ hiá»ƒn thá»‹, NgÃ´n ngá»¯) lÃ m giao diá»‡n bá»‹ dÃ y Ä‘áº·c, chiáº¿m diá»‡n tÃ­ch ngang vÃ  kÃ©m tÃ­nh phÃ¢n cáº¥p thá»‹ giÃ¡c.
  - Cáº§n chuyá»ƒn Ä‘á»•i sang phong cÃ¡ch danh sÃ¡ch cÃ i Ä‘áº·t chuáº©n Android / Material 3 (dÃ¹ng row hiá»ƒn thá»‹ tÃ³m táº¯t lá»±a chá»n hiá»‡n táº¡i vÃ  má»Ÿ Dialog lá»±a chá»n khi tÆ°Æ¡ng tÃ¡c).
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Chuyá»ƒn Ä‘á»•i sang `ReMindSettingsItem` & Tá»‘i Æ°u gom nhÃ³m:**
     - Gá»™p má»¥c NgÃ´n ngá»¯ vÃ  Äá»‹nh dáº¡ng giá» vÃ o chung má»™t tháº» nhÃ³m **"NgÃ´n ngá»¯ & VÃ¹ng" / "Language & Region"** (`language_and_region_headline`).
     - Má»¥c Time format: Hiá»ƒn thá»‹ title "Time format", subtitle lÃ  Ä‘á»‹nh dáº¡ng Ä‘ang chá»n ("System", "12h", "24h"), icon `Schedule` vÃ  chevron trailing icon.
     - Má»¥c Display mode: Náº±m trong nhÃ³m "Display", hiá»ƒn thá»‹ title "Display mode", subtitle lÃ  theme Ä‘ang chá»n ("System", "Light", "Dark"), icon tá»± Ä‘á»™ng Ä‘á»•i theo theme hiá»‡n táº¡i (`LightMode`, `DarkMode`, `BrightnessMedium`).
     - Má»¥c Language: Náº±m trong nhÃ³m "Language & Region", hiá»ƒn thá»‹ title "Language", subtitle lÃ  ngÃ´n ngá»¯ Ä‘ang chá»n ("English", "Tiáº¿ng Viá»‡t"), icon `Language`.
     - Má»¥c Color Palette (MÃ u chá»§ Ä‘áº¡o): Chuyá»ƒn Ä‘á»•i tá»« cá»¥m 8 Ã´ nÃºt chá»¯ nháº­t cá»“ng ká»nh sang dÃ²ng `ReMindSettingsItem` chuáº©n, hiá»ƒn thá»‹ tÃªn mÃ u Ä‘ang chá»n vÃ  Preview cháº¥m trÃ²n mÃ u sáº¯c (Color dot) á»Ÿ Ä‘uÃ´i dÃ²ng, nháº¥p vÃ o Ä‘á»ƒ má»Ÿ `ColorPaletteSelectionDialog`.
  2. **Táº¡o `SingleChoiceDialog` & `ColorPaletteSelectionDialog` dÃ¹ng `LvAlertDialog`:**
     - Hiá»ƒn thá»‹ danh sÃ¡ch tÃ¹y chá»n vá»›i `RadioButton` vÃ  text cÃ³ thá»ƒ báº¥m trá»±c tiáº¿p vÃ o toÃ n hÃ ng.
     - `ColorPaletteSelectionDialog`: Thiáº¿t káº¿ dáº¡ng lÆ°á»›i trÃ²n **3 cá»™t x 3 hÃ ng** (Pixel/Material 3 style) cá»±c ká»³ nhá» gá»n, gá»“m cháº¥m mÃ u trÃ²n 48dp, viá»n ná»•i báº­t (2.5dp) & dáº¥u tÃ­ch `Check` khi Ä‘Æ°á»£c chá»n, cÃ¹ng tÃªn mÃ u ngáº¯n gá»n phÃ­a dÆ°á»›i. Giáº£m 60% chiá»u cao so vá»›i danh sÃ¡ch dá»c, khÃ´ng cÃ²n chiáº¿m diá»‡n tÃ­ch mÃ n hÃ¬nh.
     - Há»— trá»£ nÃºt Cancel (Huá»·) chuáº©n thiáº¿t káº¿ qua `LvButton`.
- **Há»‡ quáº£:**
  - Giao diá»‡n cÃ i Ä‘áº·t chung Ä‘á»“ng bá»™ 100%, thanh thoÃ¡t vÃ  tinh táº¿ vá»›i 2 nhÃ³m rÃµ rÃ ng: **Display** (Giao diá»‡n & MÃ u sáº¯c) vÃ  **Language & Region** (NgÃ´n ngá»¯ & Äá»‹nh dáº¡ng vÃ¹ng miá»n).
  - Dialog chá»n mÃ u nhá» gá»n, trá»±c quan, tháº©m má»¹ cao vÃ  thao tÃ¡c cháº¡m nhanh chÃ³ng.
  - Äá»“ng bá»™ 100% vá»›i phong cÃ¡ch toÃ n bá»™ á»©ng dá»¥ng (`SettingsScreen`, `AlarmSettingsScreen`).

### [TDR-068] - Äá»“ng bá»™ LocalConfiguration uiMode & PhÃ¢n giáº£i áº£nh minh hoáº¡ EmptyState theo Theme
- **NgÃ y thá»±c hiá»‡n:** 2026-09-17
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - `EmptyState` (`AlarmListScreen`) sá»­ dá»¥ng tÃ i nguyÃªn áº£nh minh hoáº¡ (`no_alarm_illustration.png`).
  - Khi ngÆ°á»i dÃ¹ng thay Ä‘á»•i cháº¿ Ä‘á»™ hiá»ƒn thá»‹ (SÃ¡ng / Tá»‘i) trong cÃ i Ä‘áº·t á»©ng dá»¥ng hoáº·c xem trong Preview Dark Mode, `painterResource` khÃ´ng tá»± Ä‘á»™ng Ä‘á»•i sang báº£n Dark náº¿u OS váº«n Ä‘ang á»Ÿ cháº¿ Ä‘á»™ Light, vÃ¬ Android `ResourcesImpl` cache vÃ  gáº¯n cháº·t vá»›i Context/Configuration cá»§a Activity há»‡ thá»‘ng.
  - Viá»‡c can thiá»‡p override `LocalContext provides themedContext` báº±ng non-Activity `createConfigurationContext(...)` gÃ¢y crash runtime layout (`ScaffoldLayout`) do Jetpack Compose yÃªu cáº§u Context cá»§a Activity.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Tuyá»‡t Ä‘á»‘i khÃ´ng override `LocalContext`:** Chá»‰ Ä‘á»“ng bá»™ `LocalConfiguration provides themedConfiguration` vá»›i cá» `uiMode` (`UI_MODE_NIGHT_YES` / `UI_MODE_NIGHT_NO`) bÃªn trong `ReMindTheme`. Giá»¯ nguyÃªn Activity Context Ä‘á»ƒ trÃ¡nh crash layout.
  2. **TÃ¡ch biá»‡t tÃ i nguyÃªn & Chá»n áº£nh theo Theme state:** Khai bÃ¡o tÃ i nguyÃªn `no_alarm_illustration_dark.png` vÃ  Ä‘iá»u hÆ°á»›ng trá»±c tiáº¿p trong `EmptyState`: náº¿u `!LiteverTheme.colors.isLight` thÃ¬ hiá»ƒn thá»‹ `no_alarm_illustration_dark`, ngÆ°á»£c láº¡i hiá»ƒn thá»‹ `no_alarm_illustration`.
  3. **Bá»• sung Preview Dark Mode:** Äáº£m báº£o `EmptyStateDarkPreview` hiá»ƒn thá»‹ chuáº©n xÃ¡c cáº£ 2 cháº¿ Ä‘á»™ SÃ¡ng/Tá»‘i.
- **Há»‡ quáº£:**
  - á»¨ng dá»¥ng hoáº¡t Ä‘á»™ng á»•n Ä‘á»‹nh 100%, khÃ´ng bá»‹ crash runtime táº¡i `MainActivity`.
  - áº¢nh minh hoáº¡ `EmptyState` chuyá»ƒn Ä‘á»•i mÆ°á»£t mÃ , láº­p tá»©c tÆ°Æ¡ng á»©ng vá»›i Dark Mode hay Light Mode khi ngÆ°á»i dÃ¹ng Ä‘á»•i cÃ i Ä‘áº·t trong á»©ng dá»¥ng.

### [TDR-069] - Chuáº©n hÃ³a Ná»™i dung Bottom Sheet: ReMindBottomSheetContent & M3 ModalBottomSheet
- **NgÃ y thá»±c hiá»‡n:** 2026-09-18
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - CÃ¡c mÃ n hÃ¬nh vÃ  thÃ nh pháº§n (`SnoozeBottomSheet`, `GentleAlarmBottomSheet`, `AutoSilenceBottomSheet`, `MissionSelectionBottomSheet`, `AlarmActionBottomSheet`, `AddCustomPhraseContent`) Ä‘ang tá»± cáº¥u hÃ¬nh header vÃ  title thá»§ cÃ´ng, dáº«n Ä‘áº¿n láº·p láº¡i logic padding, kiá»ƒu chá»¯ title vÃ  khoáº£ng cÃ¡ch thá»«a khi khÃ´ng cÃ³ title.
  - Viá»‡c bá»c toÃ n bá»™ `ModalBottomSheet` thÃ nh wrapper trung gian gÃ¢y cá»“ng ká»nh, che giáº¥u cÃ¡c tÃ­nh nÄƒng gá»‘c cá»§a M3 vÃ  trÃ¡i vá»›i triáº¿t lÃ½ V2 Lean Architecture cá»§a Litever Design System.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **KhÃ´ng táº¡o wrapper cho `ModalBottomSheet`:** Sá»­ dá»¥ng trá»±c tiáº¿p `ModalBottomSheet` gá»‘c cá»§a Material 3 kÃ¨m `containerColor = LiteverTheme.colors.surface` nháº±m giá»¯ trá»n váº¹n kháº£ nÄƒng tÃ¹y biáº¿n cá»§a Material 3.
  2. **Táº¡o `ReMindBottomSheetContent` táº¡i `:core:designsystem`:**
     - ÄÃ³ng gÃ³i logic hiá»ƒn thá»‹ `title` (vá»›i typography chuáº©n `titleLarge`, in Ä‘áº­m vÃ  padding há»£p lÃ½) vÃ  lá»“ng `content` bÃªn dÆ°á»›i.
     - Khi `title == null`, `content` báº¯t Ä‘áº§u ngay sÃ¡t pháº§n trÃªn, khÃ´ng sinh padding/spacing thá»«a.
     - Há»— trá»£ cáº£ slot composable `title: (@Composable () -> Unit)?` vÃ  overload chuá»—i `title: String?`.
  3. **Chuyá»ƒn Ä‘á»•i toÃ n bá»™ Bottom Sheet hiá»‡n cÃ³ trong á»©ng dá»¥ng:**
     - `SnoozeBottomSheet.kt`
     - `GentleAlarmBottomSheet.kt`
     - `AutoSilenceBottomSheet.kt`
     - `MissionSelectionBottomSheet.kt`
     - `AlarmActionBottomSheet` trong `AlarmListScreen.kt`
     - Bottom sheet thÃªm cá»¥m tá»« trong `PhraseSelectionScreen.kt`
- **Há»‡ quáº£:**
  - Äá»“ng bá»™ 100% tráº£i nghiá»‡m vÃ  kiá»ƒu dÃ¡ng Header Bottom Sheet trÃªn toÃ n bá»™ á»©ng dá»¥ng.
  - Tá»‘i giáº£n mÃ£ nguá»“n, khÃ´ng sinh lá»›p wrapper thá»«a, tuÃ¢n thá»§ nguyÃªn táº¯c Lean Architecture.

### [TDR-070] - Bá»• sung mÃ´ táº£ AutoSilence & Tháº» tÃ³m táº¯t bÃ¡o thá»©c táº¡i AlarmActionBottomSheet
- **NgÃ y thá»±c hiá»‡n:** 2026-09-18
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Bottom sheet `AutoSilenceBottomSheet` cáº§n cÃ³ má»™t dÃ²ng mÃ´ táº£ ngáº¯n giáº£i thÃ­ch hÃ nh vi táº¯t tiáº¿ng/bÃ¡o láº¡i tá»± Ä‘á»™ng tÆ°Æ¡ng tá»± `GentleAlarmBottomSheet` Ä‘á»ƒ ngÆ°á»i dÃ¹ng hiá»ƒu rÃµ tÃ¡c dá»¥ng.
  - `AlarmActionBottomSheet` má»Ÿ lÃªn tá»« danh sÃ¡ch bÃ¡o thá»©c hiá»‡n chá»‰ hiá»ƒn thá»‹ cÃ¡c hÃ nh Ä‘á»™ng (Bá» qua láº§n nÃ y, Xem trÆ°á»›c, NhÃ¢n báº£n, XoÃ¡) mÃ  khÃ´ng hiá»ƒn thá»‹ thÃ´ng tin bÃ¡o thá»©c Ä‘ang Ä‘Æ°á»£c tÃ¡c vá»¥, gÃ¢y thiáº¿u ngá»¯ cáº£nh nháº­n biáº¿t cho ngÆ°á»i dÃ¹ng.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Bá»• sung mÃ´ táº£ cho AutoSilence:**
     - ThÃªm chuá»—i `auto_silence_description` Ä‘a ngÃ´n ngá»¯ (Tiáº¿ng Anh & Tiáº¿ng Viá»‡t).
     - Äáº·t dÃ²ng mÃ´ táº£ ngáº¯n phÃ­a dÆ°á»›i tiÃªu Ä‘á» cá»§a `AutoSilenceBottomSheetContent`.
  2. **Hiá»ƒn thá»‹ tháº» tÃ³m táº¯t bÃ¡o thá»©c trong `AlarmActionBottomSheet`:**
     - Truyá»n `is24HourFormat` tá»« `AlarmListScreen` vÃ o `AlarmActionBottomSheet` vÃ  `AlarmActionBottomSheetContent`.
     - Äáº·t má»™t `ReMindGroupCard` á»Ÿ Ä‘áº§u danh sÃ¡ch thao tÃ¡c: hiá»ƒn thá»‹ chu ká»³ láº·p (`getRepeatSummaryText`), giá» bÃ¡o thá»©c + AM/PM (`TimeFormatUtils.formatTimeParts`), biá»ƒu tÆ°á»£ng nhiá»‡m vá»¥ (`MissionIcons`) vÃ  nhÃ£n bÃ¡o thá»©c (`alarm.label`).
     - TÃ¡i sá»­ dá»¥ng hÃ m ná»™i bá»™ `MissionIcons` vÃ  `getMissionIcon` tá»« `AlarmCard.kt`.
- **Há»‡ quáº£:**
  - Tráº£i nghiá»‡m ngÆ°á»i dÃ¹ng Ä‘á»“ng nháº¥t, trá»±c quan vÃ  rÃµ rÃ ng hÆ¡n khi thá»±c hiá»‡n thao tÃ¡c nhanh trÃªn tá»«ng bÃ¡o thá»©c.
  - Táº­n dá»¥ng tá»‘i Ä‘a cÃ¡c thÃ nh pháº§n vÃ  quy chuáº©n cÃ³ sáºµn trong `:core:designsystem`.

### [TDR-071] - Tinh chá»‰nh giao diá»‡n AlarmCard & Tháº» NextAlarmHeader
- **NgÃ y thá»±c hiá»‡n:** 2026-09-18
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - `AlarmCard`: Card bÃ¡o thá»©c cáº§n tÄƒng tÃ­nh tháº©m má»¹ vÃ  Ä‘á»™ tÆ°Æ¡ng pháº£n vá»›i ná»n theo Ä‘Ãºng cÃ¡c token phÃ¢n cáº¥p mÃ u (`surfaceContainer` / `surfaceContainerLow`), dÃ¹ng `Surface` Ä‘á»“ng bá»™ vÃ  cÄƒn chá»‰nh font chá»¯ giá», AM/PM sáº¯c nÃ©t hÆ¡n.
  - `NextAlarmHeader`: Pháº§n hiá»ƒn thá»‹ bÃ¡o thá»©c sáº¯p tá»›i Ä‘ang lÃ  má»™t Row Ä‘Æ¡n giáº£n, chÆ°a ná»•i báº­t so vá»›i danh sÃ¡ch bÃªn dÆ°á»›i.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **NÃ¢ng cáº¥p `AlarmCard`:**
     - Sá»­ dá»¥ng `Surface` vá»›i `shape = LiteverTheme.shapes.large` thay cho `Card`.
     - MÃ u ná»n phÃ¢n cáº¥p: BÃ¡o thá»©c báº­t dÃ¹ng `LiteverTheme.colors.surfaceContainer`, bÃ¡o thá»©c táº¯t dÃ¹ng `LiteverTheme.colors.surfaceContainerLow`.
     - Chá»¯ giá» Ä‘á»‹nh dáº¡ng `FontWeight.Bold`, AM/PM in hoa vá»›i `FontWeight.Bold` vÃ  mÃ u `onSurface`.
     - NhÃ£n bÃ¡o thá»©c bá»• sung mÃ u `onSurfaceVariant`.
  2. **NÃ¢ng cáº¥p `NextAlarmHeader` & `ReMindBottomBar`:**
     - `NextAlarmHeader`: Bá»c toÃ n bá»™ ná»™i dung trong má»™t `Surface` bo gÃ³c `LiteverTheme.shapes.large`, mÃ u ná»n `tertiaryContainer`, `contentColor = onTertiaryContainer` vÃ  padding `LiteverTheme.spacing.medium`. GiÃºp khá»‘i thÃ´ng bÃ¡o thá»i gian chuÃ´ng sáº¯p reo trá»Ÿ nÃªn ná»•i báº­t, thanh lá»‹ch vÃ  táº¡o Ä‘iá»ƒm nháº¥n trá»±c quan á»Ÿ Ä‘áº§u mÃ n hÃ¬nh.
     - `ReMindBottomBar`: NÃ¢ng cáº¥p mÃ u ná»n sang `LiteverTheme.colors.surfaceContainerHigh` nháº±m tÄƒng Ä‘á»™ tÃ¡ch biá»‡t vÃ  tÆ°Æ¡ng pháº£n rÃµ nÃ©t so vá»›i ná»™i dung cuá»™n bÃªn dÆ°á»›i.
- **Há»‡ quáº£:**
  - Giao diá»‡n danh sÃ¡ch bÃ¡o thá»©c vÃ  bottom bar hiá»‡n Ä‘áº¡i, rÃµ rÃ ng, Ä‘á»™ tÆ°Æ¡ng pháº£n vÃ  tráº­t tá»± thá»‹ giÃ¡c (visual hierarchy) tá»‘t hÆ¡n.

### [TDR-072] - Tá»± Ä‘á»™ng xuá»‘ng dÃ²ng (FlowRow) cho Button trong LvAlertDialog khi Text dÃ i
- **NgÃ y thá»±c hiá»‡n:** 2026-09-18
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Trong `LvAlertDialog`, cÃ¡c nÃºt tÃ¡c vá»¥ (dismissButton vÃ  confirmButton) trÆ°á»›c Ä‘Ã¢y Ä‘Æ°á»£c Ä‘áº·t trong má»™t `Row` vá»›i `Alignment.End`. Khi tiÃªu Ä‘á» cá»§a cÃ¡c button quÃ¡ dÃ i hoáº·c khÃ´ng Ä‘á»§ diá»‡n tÃ­ch hiá»ƒn thá»‹ trÃªn cÃ¹ng má»™t hÃ ng ngang, text cÃ³ thá»ƒ bá»‹ Ã©p nhá» hoáº·c gÃ¢y trÃ n khung, vá»¡ bá»‘ cá»¥c dialog.
- **Quyáº¿t Ä‘á»‹nh:**
  - Chuyá»ƒn Ä‘á»•i vÃ¹ng chá»©a action buttons tá»« `Row` sang `FlowRow` vá»›i khoáº£ng cÃ¡ch `horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small, Alignment.End)` vÃ  `verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)`.
  - Giá»¯ thá»© tá»± hiá»ƒn thá»‹: dismissButton Ä‘i trÆ°á»›c, confirmButton Ä‘i sau. Khi khÃ´ng Ä‘á»§ khÃ´ng gian ngang, confirmButton hoáº·c cÃ¡c nÃºt sáº½ tá»± Ä‘á»™ng ngáº¯t vÃ  nháº£y xuá»‘ng dÃ²ng káº¿ tiáº¿p má»™t cÃ¡ch mÆ°á»£t mÃ  vÃ  tá»± nhiÃªn.
  - Bá»• sung Preview `LvAlertDialogLongButtonsPreview` Ä‘á»ƒ kiá»ƒm thá»­ trÆ°á»ng há»£p text button dÃ i.
- **Há»‡ quáº£:**
  - Äáº£m báº£o Dialog luÃ´n hiá»ƒn thá»‹ trá»n váº¹n nhÃ£n nÃºt báº¥m mÃ  khÃ´ng bá»‹ trÃ n khung hay cáº¯t bá»›t chá»¯ trÃªn má»i kÃ­ch cá»¡ mÃ n hÃ¬nh.
  - TÆ°Æ¡ng thÃ­ch hoÃ n toÃ n vá»›i cÃ¡c dialog hiá»‡n cÃ³ trong toÃ n bá»™ á»©ng dá»¥ng mÃ  khÃ´ng cáº§n thay Ä‘á»•i code á»Ÿ caller side.

### [TDR-073] - Äá»“ng bá»™ MissionCompleteContent sá»­ dá»¥ng thÃ nh pháº§n dÃ¹ng chung FeedbackStateView
- **NgÃ y thá»±c hiá»‡n:** 2026-09-18
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - MÃ n hÃ¬nh thÃ´ng bÃ¡o hoÃ n thÃ nh nhiá»‡m vá»¥ (`MissionCompleteContent`) trÆ°á»›c Ä‘Ã¢y tá»± triá»ƒn khai má»™t bá»‘ cá»¥c Column riÃªng láº» vá»›i Icon CheckCircle vÃ  Text tÃ¹y biáº¿n, chÆ°a táº­n dá»¥ng component `FeedbackStateView` cá»§a LiteVer Design System.
- **Quyáº¿t Ä‘á»‹nh:**
  - TÃ¡i cáº¥u trÃºc `MissionCompleteContent` sang sá»­ dá»¥ng trá»±c tiáº¿p `FeedbackStateView` vá»›i `type = FeedbackStateType.SUCCESS`.
  - Táº­n dá»¥ng huy hiá»‡u (badge trÃ²n), kiá»ƒu mÃ u ngá»¯ nghÄ©a Success (`successContainer`, `onSuccessContainer`) vÃ  spacing chuáº©n hÃ³a cá»§a Design System.
  - TÃ¹y chá»‰nh kiá»ƒu chá»¯ `displayMedium` in Ä‘áº­m vÃ  khoáº£ng cÃ¡ch kÃ½ tá»± `letterSpacing = 4.sp` cÃ¹ng subtitle `titleMedium` Ä‘á»ƒ duy trÃ¬ sá»± ná»•i báº­t vÃ  táº¡o cáº£m giÃ¡c thÃ nh tá»±u khi táº¯t chuÃ´ng thÃ nh cÃ´ng.
- **Há»‡ quáº£:**
  - TuÃ¢n thá»§ nghiÃªm ngáº·t nguyÃªn táº¯c Single Source of Truth cho UI components giá»¯a cÃ¡c module.
  - Giáº£m thiá»ƒu code trÃ¹ng láº·p, Ä‘á»“ng bá»™ hoÃ¡ nháº­n diá»‡n thÆ°Æ¡ng hiá»‡u vÃ  tráº¡ng thÃ¡i pháº£n há»“i xuyÃªn suá»‘t á»©ng dá»¥ng.
### [TDR-074] - TÃ¡i Cáº¥u TrÃºc Khung MÃ n HÃ¬nh LÃ m Nhiá»‡m Vá»¥ (Mission Ringing & Content Screens) Chuáº©n Stitch Design
- **NgÃ y thá»±c hiá»‡n:** 2026-09-21
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - MÃ n hÃ¬nh lÃ m nhiá»‡m vá»¥ giáº£i chuÃ´ng bÃ¡o thá»©c cáº§n Ä‘Æ°á»£c nÃ¢ng cáº¥p visual hierarchy theo thiáº¿t káº¿ Stitch Design: hiá»ƒn thá»‹ rÃµ rÃ ng thÃ´ng tin chuÃ´ng, thanh Ä‘áº¿m ngÆ°á»£c, tiáº¿n Ä‘á»™ cÃ¡c vÃ²ng nhiá»‡m vá»¥ (segmented progress bar), vÃ  khÃ´ng gian thao tÃ¡c sÃ¡t bÃ n phÃ­m (bottom docked bar).
  - CÃ¡c mÃ n hÃ¬nh thÃ nh pháº§n lÃ m nhiá»‡m vá»¥ (`TypingMissionContent`, `MathMissionContent`, `MemoryTilesMissionContent`) trÆ°á»›c Ä‘Ã¢y cÃ³ nhiá»u chi tiáº¿t vá»¥n váº·t (tháº» máº¹o thá»«a thÃ£i, Ä‘áº¿m vÃ²ng láº·p bá»‹ láº·p láº¡i á»Ÿ cáº£ trÃªn vÃ  dÆ°á»›i).
- **Quyáº¿t Ä‘á»‹nh:**
  - Khung chÃ­nh `MissionRingingScreen`:
    - Top App Bar hiá»ƒn thá»‹ tiÃªu Ä‘á» thá»­ thÃ¡ch, giá» bÃ¡o thá»©c, nÃºt bá» cuá»™c vÃ  badge Ä‘áº¿m ngÆ°á»£c `mm:ss` ná»•i báº­t (chuyá»ƒn Ä‘á» khi < 10 giÃ¢y).
    - `MissionProgressScaffold`: Hiá»ƒn thá»‹ tÃªn nhiá»‡m vá»¥, sá»‘ vÃ²ng vÃ  thanh tiáº¿n Ä‘á»™ phÃ¢n Ä‘oáº¡n `Row` (segmented progress).
    - `MissionBottomDockedBar`: Äáº·t cá»‘ Ä‘á»‹nh sÃ¡t Ä‘Ã¡y / trÃªn bÃ n phÃ­m áº£o (IME padding), chá»©a thÃ´ng tin kÃ½ tá»±/sá»‘ Ã´ vÃ  nÃºt "Tiáº¿p tá»¥c" chuyá»ƒn bÆ°á»›c.
  - CÃ¡c mÃ n hÃ¬nh nhiá»‡m vá»¥ (`TypingMissionContent`, `MathMissionContent`, `MemoryTilesMissionContent`):
    - ÄÆ°a `MissionInstructionBanner` lÃªn trÃªn cÃ¹ng: gá»“m icon ngá»¯ cáº£nh (`EditNote`, `Calculate`, `GridView`) vÃ  dÃ²ng yÃªu cáº§u ngáº¯n gá»n rÃµ rÃ ng.
    - `TypingMissionContent`: Báº£ng gÃµ chá»¯ tÆ°Æ¡ng tÃ¡c há»— trá»£ tÃ´ mÃ u tá»«ng kÃ½ tá»± Ä‘Ãºng/sai/gá»£i Ã½, Ã´ nháº­p liá»‡u giá»›i háº¡n chiá»u cao tá»‘i Ä‘a vÃ  há»— trá»£ cuá»™n Ä‘á»™c láº­p (`verticalScroll`) khi vÄƒn báº£n dÃ i.
    - `MathMissionContent`: Tháº» phÃ©p toÃ¡n lá»›n, sáº¯c nÃ©t á»Ÿ giá»¯a kÃ¨m Ã´ nháº­p sá»‘ á»Ÿ dÆ°á»›i, loáº¡i bá» cÃ¡c thÃ nh pháº§n rÆ°á»m rÃ .
    - `MemoryTilesMissionContent`: Hiá»ƒn thá»‹ rÃµ rÃ ng giai Ä‘oáº¡n Ä‘áº¿m ngÆ°á»£c ghi nhá»› hoáº·c tÃ¬m Ã´ kÃ¨m lÆ°á»›i Ã´ cá» bo trÃ²n trong `Card`, loáº¡i bá» badge tiáº¿n Ä‘á»™ trÃ¹ng láº·p. Äá»“ng thá»i cáº­p nháº­t callback `onProgressChange` Ä‘á»ƒ truyá»n sá»‘ Ã´ Ä‘Ã£ chá»n Ä‘Ãºng / tá»•ng sá»‘ Ã´ cáº§n tÃ¬m xuá»‘ng `MissionBottomDockedBar` hiá»ƒn thá»‹ trá»±c tiáº¿p á»Ÿ chÃ¢n trang (vÃ­ dá»¥: `2 / 4 Ã´`).
- **Há»‡ quáº£:**
  - Tráº£i nghiá»‡m lÃ m nhiá»‡m vá»¥ trá»Ÿ nÃªn máº¡ch láº¡c, hiá»‡n Ä‘áº¡i, nháº¥t quÃ¡n thá»‹ giÃ¡c 100% giá»¯a 3 loáº¡i nhiá»‡m vá»¥.
  - TÆ°Æ¡ng thÃ­ch tá»‘t vá»›i mÃ n hÃ¬nh nhá» vÃ  khi bÃ n phÃ­m áº£o báº­t lÃªn nhá» cÆ¡ cháº¿ cuá»™n Ä‘á»™c láº­p vÃ  bottom bar docked.

### [TDR-075] - Hiá»ƒn Thá»‹ Tráº¡ng ThÃ¡i BÃ¡o Thá»©c Tiáº¿p Theo Táº¡i TodayScreen (Non-interactive & Rich Details)
- **NgÃ y thá»±c hiá»‡n:** 2026-09-21
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - `TodayScreen` cáº§n bá»• sung má»™t khá»‘i thÃ´ng tin bÃ¡o thá»©c tiáº¿p theo tÆ°Æ¡ng tá»± nhÆ° `TodayQuoteView` hoáº·c banner quáº£ng cÃ¡o Ä‘á»ƒ ngÆ°á»i dÃ¹ng náº¯m báº¯t nhanh tráº¡ng thÃ¡i bÃ¡o thá»©c trong ngÃ y.
  - YÃªu cáº§u Ä‘áº·c thÃ¹: VÃ¹ng nÃ y hoÃ n toÃ n tÄ©nh (non-interactive, khÃ´ng cÃ³ nÃºt báº¥m, khÃ´ng click handler). Khi cÃ³ bÃ¡o thá»©c káº¿ tiáº¿p thÃ¬ hiá»ƒn thá»‹ Ä‘áº§y Ä‘á»§ thÃ´ng tin bÃ¡o thá»©c (giá», AM/PM, nhÃ£n, láº·p láº¡i, icon nhiá»‡m vá»¥, badge Ä‘áº¿m ngÆ°á»£c cÃ²n láº¡i) gáº§n giá»‘ng nhÆ° `AlarmCard` á»Ÿ danh sÃ¡ch bÃ¡o thá»©c. Khi khÃ´ng cÃ³ bÃ¡o thá»©c káº¿ tiáº¿p hoáº·c táº¥t cáº£ táº¯t thÃ¬ hiá»ƒn thá»‹ thÃ´ng Ä‘iá»‡p truyá»n cáº£m há»©ng Ã½ nghÄ©a ("Thá»i gian nghá»‰ ngÆ¡i") kÃ¨m áº£nh minh hoáº¡ rá»—ng.
- **Quyáº¿t Ä‘á»‹nh:**
  - TÃ¡ch vÃ  chuyá»ƒn `NextAlarmUiState` cÃ¹ng hÃ m `calculateNextAlarm` vÃ o `:core:model` Ä‘á»ƒ chia sáº» giá»¯a `:features:alarms` vÃ  `:features:today` (tuÃ¢n thá»§ Clean Architecture vÃ  Single Source of Truth), Ä‘á»“ng thá»i bá»• sung trÆ°á»ng `val alarm: Alarm` vÃ o `NextAlarmUiState.Remaining`.
  - Di chuyá»ƒn cÃ¡c file áº£nh minh há»a `no_alarm_illustration.png` vÃ  `no_alarm_illustration_dark.png` vÃ o `:core:designsystem/src/main/res/drawable/` Ä‘á»ƒ tÃ¡i sá»­ dá»¥ng thá»‘ng nháº¥t giá»¯a cÃ¡c feature.
  - NÃ¢ng cáº¥p `TodayNextAlarmView` trong `:features:today:ui:components`:
    - Äá»“ng bá»™ Ä‘á»‹nh dáº¡ng giá» (12h/24h) theo cÃ i Ä‘áº·t há»‡ thá»‘ng ngÆ°á»i dÃ¹ng thÃ´ng qua `AlarmPreferencesDataSource` Ä‘Æ°á»£c inject vÃ o `TodayViewModel` vÃ  truyá»n xuá»‘ng UI.
    - Khi cÃ³ bÃ¡o thá»©c sáº¯p tá»›i (`NextAlarmUiState.Remaining`): thiáº¿t káº¿ bá»‘ cá»¥c 2 cá»™t cÃ¢n Ä‘á»‘i:
      - **Cá»™t 1**: Icon bÃ¡o thá»©c dáº¡ng Ã´ vuÃ´ng bo gÃ³c (76.dp vá»›i ná»n `primaryContainer` vÃ  icon chuÃ´ng).
      - **Cá»™t 2**:
        - HÃ ng 1: Text "BÃ¡o thá»©c tiáº¿p theo" vÃ  badge ná»•i báº­t Ä‘áº¿m ngÆ°á»£c thá»i gian cÃ²n láº¡i (rÃºt gá»n: vÃ­ dá»¥ "6h 45m").
        - HÃ ng 2: Giá» bÃ¡o thá»©c to Ä‘áº­m (`titleLarge` bold + AM/PM tuá»³ cháº¿ Ä‘á»™ 12h/24h) -> cá»¥m icon nhiá»‡m vá»¥ -> ngÃ y láº·p láº¡i.
        - HÃ ng 3: NhÃ£n bÃ¡o thá»©c theo font nghiÃªng.
      - ToÃ n bá»™ lÃ  view tÄ©nh (khÃ´ng Switch, khÃ´ng More menu).
    - Khi khÃ´ng cÃ³ bÃ¡o thá»©c (`NextAlarmUiState.AllOff`): hiá»ƒn thá»‹ áº£nh minh hoáº¡ theo Light/Dark theme kÃ¨m tiÃªu Ä‘á» áº¥m Ã¡p "Thá»i gian nghá»‰ ngÆ¡i" vÃ  thÃ´ng Ä‘iá»‡p truyá»n cáº£m há»©ng "KhÃ´ng cÃ³ lá»‹ch thá»©c dáº­y nÃ o sáº¯p diá»…n ra. HÃ£y táº­n hÆ°á»Ÿng trá»n váº¹n nhá»¯ng phÃºt giÃ¢y nghá»‰ ngÆ¡i vÃ  náº¡p láº¡i nÄƒng lÆ°á»£ng nhÃ©!".
  - TÃ­ch há»£p vÃ o `TodayViewModel` thÃ´ng qua viá»‡c quan sÃ¡t `AlarmRepository.getAllAlarms()` cÃ¹ng `AlarmPreferencesDataSource.is24HourFormat` vÃ  sáº¯p xáº¿p thá»© tá»± hiá»ƒn thá»‹ táº¡i `TodayScreen`: Thá»i tiáº¿t $\rightarrow$ BÃ¡o thá»©c tiáº¿p theo (`TodayNextAlarmView`) $\rightarrow$ Quáº£ng cÃ¡o (`NativeAdView`) $\rightarrow$ TrÃ­ch dáº«n truyá»n cáº£m há»©ng (`TodayQuoteView`) á»Ÿ dÆ°á»›i cÃ¹ng.
- **Há»‡ quáº£:**
  - Cung cáº¥p Ä‘áº§y Ä‘á»§ thÃ´ng tin bÃ¡o thá»©c má»™t cÃ¡ch trá»±c quan, Ä‘á»“ng bá»™ nháº­n diá»‡n vÃ  Ä‘á»‹nh dáº¡ng giá» cÃ i Ä‘áº·t (12h/24h) vá»›i danh sÃ¡ch bÃ¡o thá»©c mÃ  váº«n duy trÃ¬ tÃ­nh cháº¥t thuáº§n hiá»ƒn thá»‹.
  - Biáº¿n tráº¡ng thÃ¡i rá»—ng thÃ nh thÃ´ng Ä‘iá»‡p tÃ­ch cá»±c, áº¥m Ã¡p, phÃ¹ há»£p triáº¿t lÃ½ ReMind.
### [TDR-076] - Tá»‘i Æ¯u HÃ³a Táº£i Dá»¯ Liá»‡u Cache-First, Tá»± Äá»™ng LÃ m Má»›i Khi Háº¿t Háº¡n & Sá»­a Lá»—i Loading VÃ´ Táº­n Táº¡i TodayScreen
- **NgÃ y thá»±c hiá»‡n:** 2026-09-21
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - `TodayViewModel` trong hÃ m `init` gá»i `refresh()`, vá»‘n bá»‹ gÃ¡n cá»©ng cá» `force = true` cho cáº£ `weatherRepository.refreshWeather(force = true)` vÃ  `todayRepository.refreshTodayBriefing(force = true)`. Háº­u quáº£ lÃ  cÆ¡ cháº¿ cache 1 giá» vÃ  cache theo ngÃ y bá»‹ vÃ´ hiá»‡u hoÃ¡ má»—i láº§n má»Ÿ mÃ n hÃ¬nh hoáº·c ViewModel tÃ¡i táº¡o láº¡i, gÃ¢y gá»i máº¡ng dÆ° thá»«a.
  - Ban Ä‘áº§u khi giá»›i thiá»‡u `isBusy = isRefreshing || isProcessing`, xáº£y ra lá»—i káº¹t loading vÃ´ táº­n (Infinite Loading Deadlock): náº¿u server tráº£ vá» `aiStatus == "processing"`, `isProcessing` thÃ nh `true` khiáº¿n nÃºt refresh TopAppBar xoay liÃªn tá»¥c vÃ  bá»‹ vÃ´ hiá»‡u hÃ³a (`enabled = !isBusy = false`). Äá»“ng thá»i `WeatherRepositoryImpl` vÃ  `TodayRepositoryImpl` do kiá»ƒm tra cache < 1 giá» nÃªn return sá»›m mÃ  khÃ´ng gá»i API, khiáº¿n dá»¯ liá»‡u káº¹t vÄ©nh viá»…n á»Ÿ tráº¡ng thÃ¡i `processing`.
  - Khi ngÆ°á»i dÃ¹ng quay láº¡i app sau hÆ¡n 1 giá», mÃ n hÃ¬nh khÃ´ng cÃ³ cÆ¡ cháº¿ tá»± Ä‘á»™ng gá»i lÃ m má»›i dá»¯ liá»‡u thá»i tiáº¿t Ä‘Ã£ háº¿t háº¡n náº¿u trÆ°á»›c Ä‘Ã³ tráº¡ng thÃ¡i Ä‘Ã£ lÃ  `completed`.
- **Quyáº¿t Ä‘á»‹nh:**
  - **Ãp dá»¥ng Cache-First & Bá» qua cache khi Ä‘ang `processing`**:
    - Trong `WeatherRepositoryImpl` vÃ  `TodayRepositoryImpl`: Kiá»ƒm tra náº¿u dá»¯ liá»‡u trong DataStore Ä‘ang chá»©a `aiStatus == "processing"`, há»‡ thá»‘ng khÃ´ng Ä‘Æ°á»£c coi lÃ  cache hoÃ n chá»‰nh vÃ  khÃ´ng return sá»›m, cho phÃ©p tiáº¿p tá»¥c gá»i API Ä‘á»ƒ láº¥y káº¿t quáº£ hoÃ n táº¥t.
    - Sá»­a Ä‘á»•i `TodayViewModel.refresh(force: Boolean = false)`: Trong khá»‘i `init`, gá»i `refresh(force = false)` Ä‘á»ƒ kiá»ƒm tra vÃ  tÃ¡i sá»­ dá»¥ng dá»¯ liá»‡u há»£p lá»‡ trong DataStore cache, chá»‰ gá»i network khi cache háº¿t háº¡n (> 1 giá» vá»›i thá»i tiáº¿t, sang ngÃ y má»›i vá»›i briefing), ngÃ´n ngá»¯ thay Ä‘á»•i hoáº·c dá»¯ liá»‡u Ä‘ang á»Ÿ tráº¡ng thÃ¡i `processing`.
  - **TÃ¡ch biá»‡t `isRefreshing` (máº¡ng) vÃ  `isProcessing` (ná»™i dung AI)**:
    - `ReMindLoadingIconButton`: GÃ¡n `loading = isRefreshing` vÃ  `enabled = !isRefreshing`. NgÆ°á»i dÃ¹ng luÃ´n cÃ³ thá»ƒ chá»§ Ä‘á»™ng báº¥m lÃ m má»›i khi khÃ´ng cÃ³ tiáº¿n trÃ¬nh máº¡ng Ä‘ang cháº¡y, ngay cáº£ khi AI backend Ä‘ang trong tráº¡ng thÃ¡i processing.
    - `WeatherInfoView`: Truyá»n `isLocationClickEnabled = !isRefreshing`.
  - **Tá»± Ä‘á»™ng lÃ m má»›i khi háº¿t háº¡n vÃ  Polling ná»n khi Processing**:
    - Trong `TodayViewModel.onResume()` (gáº¯n vá»›i `Lifecycle.Event.ON_RESUME` táº¡i `TodayScreen`): Tá»± Ä‘á»™ng gá»i `refresh(force = false)`. Náº¿u dá»¯ liá»‡u thá»i tiáº¿t Ä‘Ã£ háº¿t háº¡n (> 1 giá») hoáº·c sang ngÃ y má»›i, há»‡ thá»‘ng tá»± Ä‘á»™ng lÃ m má»›i trong suá»‘t mÃ  khÃ´ng tá»‘n cÃ´ng ngÆ°á»i dÃ¹ng thao tÃ¡c. Náº¿u Ä‘ang trong tráº¡ng thÃ¡i processing, Ã©p `refresh(force = true)` sau 15 giÃ¢y.
    - ThÃªm cÆ¡ cháº¿ tá»± Ä‘á»™ng thÄƒm dÃ² (Auto Polling): Khi `isProcessing` lÃ  `true`, ViewModel tá»± Ä‘á»™ng khá»Ÿi cháº¡y coroutine Ä‘á»‹nh ká»³ thÄƒm dÃ² sau má»—i 10 giÃ¢y (tá»‘i Ä‘a 6 láº§n) Ä‘á»ƒ cáº­p nháº­t káº¿t quáº£ AI Ä‘Ã£ hoÃ n thÃ nh.
- **Há»‡ quáº£:**
  - Giáº£i quyáº¿t dá»©t Ä‘iá»ƒm lá»—i káº¹t loading vÃ´ táº­n táº¡i mÃ n hÃ¬nh Today.
  - Äáº£m báº£o thá»i tiáº¿t luÃ´n Ä‘Æ°á»£c tá»± Ä‘á»™ng lÃ m má»›i khi háº¿t háº¡n (TTL 1 giá») má»—i khi ngÆ°á»i dÃ¹ng má»Ÿ láº¡i á»©ng dá»¥ng.
  - Tiáº¿t kiá»‡m bÄƒng thÃ´ng tá»‘i Ä‘a vÃ  tÄƒng tÃ­nh tÆ°Æ¡ng tÃ¡c, khÃ´ng khÃ³a cá»©ng giao diá»‡n cá»§a ngÆ°á»i dÃ¹ng.

### [TDR-077] - Äáº·t MÃ u Äen (Black) LÃ m MÃ u Chá»§ Äáº¡o Máº·c Äá»‹nh
- **NgÃ y thá»±c hiá»‡n:** 2026-09-24
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - MÃ u Black (Äen) vá»«a Ä‘Æ°á»£c thÃªm vÃ o há»‡ thá»‘ng `litever-designsystem`. NgÆ°á»i dÃ¹ng yÃªu cáº§u thiáº¿t láº­p mÃ u nÃ y thÃ nh lá»±a chá»n mÃ u chá»§ Ä‘áº¡o Ä‘áº§u tiÃªn trong cÃ i Ä‘áº·t Theme cÅ©ng nhÆ° lÃ m mÃ u máº·c Ä‘á»‹nh cho toÃ n bá»™ á»©ng dá»¥ng khi khá»Ÿi táº¡o thay cho mÃ u Äá» (RED) trÆ°á»›c Ä‘Ã¢y.
- **Quyáº¿t Ä‘á»‹nh:**
  - Cáº­p nháº­t giÃ¡ trá»‹ khá»Ÿi táº¡o `colorPalette` thÃ nh `"BLACK"` trong `SettingsUiState` vÃ  `AlarmPreferencesDataSource`.
  - Thiáº¿t láº­p `"BLACK"` lÃ m giÃ¡ trá»‹ máº·c Ä‘á»‹nh cho `colorPalette` trong composable `ReMindTheme` (táº¡i `Theme.kt`).
  - Cáº­p nháº­t `resolvedThemeColor` Ä‘á»ƒ map `"BLACK"` sang `LiteverThemeColor.BLACK` vÃ  dÃ¹ng `"BLACK"` lÃ m fallback (`else`).
  - Kiá»ƒm tra thá»© tá»± trong `GeneralSettingsScreen`, tÃ¹y chá»n mÃ u "BLACK" Ä‘Ã£ náº±m á»Ÿ vá»‹ trÃ­ Ä‘áº§u tiÃªn cá»§a danh sÃ¡ch `colorPaletteOptions`.
  - Cáº­p nháº­t cáº¥u hÃ¬nh Preview `GeneralSettingsScreenPreview` Ä‘á»ƒ pháº£n Ã¡nh Ä‘Ãºng máº·c Ä‘á»‹nh má»›i.
- **Há»‡ quáº£:**
  - ToÃ n bá»™ á»©ng dá»¥ng sáº½ sá»­ dá»¥ng bá»™ mÃ u Äen lÃ m mÃ u nháº­n diá»‡n thÆ°Æ¡ng hiá»‡u máº·c Ä‘á»‹nh ban Ä‘áº§u.
  - TÄƒng Ä‘á»™ tÃ¹y biáº¿n vÃ  mang láº¡i cáº£m giÃ¡c hiá»‡n Ä‘áº¡i hÆ¡n khi cÃ i Ä‘áº·t máº·c Ä‘á»‹nh lÃ  mÃ u trung tÃ­nh.

### [TDR-078] - TÃ­ch Há»£p NhÃ¡nh Nháº¯c Nhá»Ÿ (Reminders) & ÄÃ¡nh GiÃ¡ Xung Äá»™t Há»‡ Thá»‘ng Giá»¯a Alarms vÃ  Reminders
- **NgÃ y thá»±c hiá»‡n:** 2026-09-28
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Cáº§n Ä‘Æ°a toÃ n bá»™ tÃ­nh nÄƒng Nháº¯c nhá»Ÿ (`features:reminders`) tá»« nhÃ¡nh `feature/reminders1` tÃ­ch há»£p vÃ o nhÃ¡nh má»›i `feature/reminders` dá»±a trÃªn ná»n `main` má»›i nháº¥t (Ä‘Ã£ nÃ¢ng cáº¥p Litever 2.1.0 vÃ  mÃ u Black máº·c Ä‘á»‹nh).
  - YÃªu cáº§u cá»‘t lÃµi: báº£o toÃ n 100% logic bÃ¡o thá»©c (`AlarmService`, `AlarmControllerImpl`, `AlarmReceiver`), khÃ´ng Ä‘á»ƒ logic cá»§a Nháº¯c nhá»Ÿ xÃ¢m láº¥n hay lÃ m giÃ¡n Ä‘oáº¡n chuÃ´ng bÃ¡o thá»©c.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **TÃ¡ch biá»‡t hoÃ n toÃ n táº§ng Thá»±c thi Ná»n & Ã‚m thanh (Service & Audio Isolation):**
     - Giá»¯ nguyÃªn `AlarmService` vÃ  `AlarmRingManager` phá»¥c vá»¥ Ä‘á»™c quyá»n cho BÃ¡o thá»©c (`features:alarms`).
     - ToÃ n bá»™ cáº£nh bÃ¡o nháº¯c nhá»Ÿ dáº¡ng chuÃ´ng (Persistent Alert / Alarm Stream) sá»­ dá»¥ng riÃªng `ReminderAlertManager`, `ReminderNotificationManager`, vÃ  `ReminderMonitorService` (Ä‘á»‘i vá»›i Location & Wi-Fi Geofence).
     - Äá»‹nh tuyáº¿n rung/chuÃ´ng qua `AudioPlayer` vá»›i thiáº¿t láº­p usage phÃ¹ há»£p (`USAGE_ALARM` hoáº·c `USAGE_NOTIFICATION`).
  2. **TÆ°Æ¡ng thÃ­ch UI vá»›i Litever Design System 2.1:**
     - Bá»• sung bá»™ adapter thÃ nh pháº§n `LiteverLegacyCompat.kt` (gá»“m `LiteverTextField`, `LiteverCircularProgressIndicator`, `LiteverLinearProgressIndicator`, `LiteverModalBottomSheet`, `LiteverDatePickerDialog`, `LiteverSegmentedButton`, `LiteverFilterChip`, `LiteverDialog`).
     - Loáº¡i bá» hoÃ n toÃ n cÃ¡c thay Ä‘á»•i xÃ¢m láº¥n ngÆ°á»£c vÃ o `features/alarms` (Æ°u tiÃªn tuyá»‡t Ä‘á»‘i code tá»« `main`).
  3. **Khá»­ trÃ¹ng láº·p Ä‘iá»u hÆ°á»›ng chuÃ´ng (Deduplicate Ringing Navigation):**
     - Trong `MainActivity.kt`, tÃ¡ch báº¡ch thá»© tá»± Æ°u tiÃªn Ä‘iá»u hÆ°á»›ng: Priority 1 (Alarm Ringing), Priority 2 (Alarm Snoozing), Priority 3 (Reminder Ringing vá»›i cÆ¡ cháº¿ chá»‘ng loop `lastNavigatedRingingReminderId`), báº£o Ä‘áº£m `startDestinationId` Ä‘iá»u hÆ°á»›ng mÆ°á»£t mÃ  khÃ´ng crash.
- **ÄÃ¡nh giÃ¡ Xung Ä‘á»™t Há»‡ thá»‘ng (Conflict Risk Assessment):**
  - **Audio Focus:** Cáº£ 2 module Ä‘á»u cÃ³ kháº£ nÄƒng chiáº¿m `AUDIOFOCUS_GAIN_TRANSIENT`. Khi cáº£ 2 kÃ­ch hoáº¡t cÃ¹ng lÃºc, `AudioPlayer` sáº½ táº¡m dá»«ng Ã¢m thanh trÆ°á»›c Ä‘Ã³. Khuyáº¿n nghá»‹: Cáº§n bá»• sung cÆ¡ cháº¿ Æ°u tiÃªn Alarm cao hÆ¡n Reminder (náº¿u Alarm kÃ­ch hoáº¡t, Reminder tá»± Ä‘á»™ng mute hoáº·c chuyá»ƒn sang notification im láº·ng).
  - **Notification Channels & Full-screen Intent:** Alarm vÃ  Reminder dÃ¹ng channel riÃªng biá»‡t (`reminders_persistent`, `reminders_standard`, `remind_alarm_channel`). Äáº£m báº£o khÃ´ng xung Ä‘á»™t channel priority.
  - **Exact Alarm Quota:** Android 12+ giá»›i háº¡n sá»‘ lÆ°á»£ng exact alarm Ä‘Æ°á»£c Ä‘áº·t trÆ°á»›c (`SCHEDULE_EXACT_ALARM`). Cáº£ 2 cÃ¹ng sá»­ dá»¥ng `AlarmManager.setExactAndAllowWhileIdle`.
  - **Database Migration:** ÄÃ£ nÃ¢ng cáº¥p an toÃ n Room DB tá»« version 5 lÃªn version 6 (`MIGRATION_5_6`), báº£o toÃ n cÃ¡c báº£ng `alarms`, `missions`, `reminders`, `checklist_items`.
- **Há»‡ quáº£:**
  - ToÃ n bá»™ 12 commits Ä‘Ã£ Ä‘Æ°á»£c cherry-pick sáº¡ch sáº½.
  - ToÃ n bá»™ unit tests cá»§a `:features:alarms`, `:features:reminders`, `:core:alarm`, vÃ  `:app` Ä‘á»u pass 100%.
  - XÃ¢y dá»±ng ná»n táº£ng vá»¯ng cháº¯c Ä‘á»ƒ chuyá»ƒn sang bÆ°á»›c tá»‘i Æ°u tráº£i nghiá»‡m vÃ  kiá»ƒm thá»­ Ä‘á»“ng thá»i (concurrency testing) giá»¯a 2 module.

### [TDR-079] - TÃ¡ch Biá»‡t Cáº¥u HÃ¬nh Äiá»u Kiá»‡n KÃ­ch Hoáº¡t Sang MÃ n HÃ¬nh ChuyÃªn Biá»‡t & NÃ¢ng Cáº¥p Tháº» TÃ³m Táº¯t (Trigger Summary Card)
- **NgÃ y thá»±c hiá»‡n:** 2026-09-28
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Trong `ReminderEditScreen`, cÃ¡c má»¥c cáº¥u hÃ¬nh Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t (Giá» & Láº·p láº¡i, Vá»‹ trÃ­ Ä‘á»‹a lÃ½ & BÃ¡n kÃ­nh, Káº¿t ná»‘i Wi-Fi) trÆ°á»›c Ä‘Ã¢y Ä‘Æ°á»£c render trá»±c tiáº¿p dáº¡ng inline cuá»™n dÃ i trong cÃ¹ng má»™t mÃ n hÃ¬nh chá»‰nh sá»­a nháº¯c nhá»Ÿ.
  - Viá»‡c nÃ y khiáº¿n mÃ n hÃ¬nh `ReminderEditScreen` quÃ¡ dÃ i, lÃ m loÃ£ng cÃ¡c thÃ´ng tin cá»‘t lÃµi (TiÃªu Ä‘á», Ghi chÃº, Danh sÃ¡ch cÃ´ng viá»‡c Checklist, Ã‚m thanh & BÃ¡o thá»©c) vÃ  gÃ¢y rá»‘i máº¯t khi ngÆ°á»i dÃ¹ng pháº£i cuá»™n qua láº¡i giá»¯a cÃ¡c tÃ¹y chá»n kÃ­ch hoáº¡t phá»©c táº¡p.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **NÃ¢ng cáº¥p `ReminderTriggerSection` thÃ nh Tháº» TÃ³m Táº¯t (Summary Card):**
     - Thay tháº¿ toÃ n bá»™ khá»‘i form inline táº¡i `ReminderEditScreen` báº±ng má»™t tháº» Card tÃ³m táº¯t trá»±c quan, phÃ¢n cáº¥p thá»‹ giÃ¡c rÃµ rÃ ng:
       - **Badge Icon & Label:** Hiá»ƒn thá»‹ biá»ƒu tÆ°á»£ng tÆ°Æ¡ng á»©ng vá»›i Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t hiá»‡n táº¡i (`AccessTime` cho Thá»i gian, `LocationOn` cho Vá»‹ trÃ­, `Wifi` cho Wi-Fi) vá»›i mÃ u sáº¯c ná»•i báº­t.
       - **Headline ná»•i báº­t:** Hiá»ƒn thá»‹ giá» kÃ­ch hoáº¡t (vd: `08:30` Ä‘á»‹nh dáº¡ng theo cÃ i Ä‘áº·t 12h/24h), tÃªn Ä‘á»‹a Ä‘iá»ƒm (hoáº·c toáº¡ Ä‘á»™), hoáº·c tÃªn máº¡ng Wi-Fi (SSID).
       - **Subtitle chi tiáº¿t:** TÃ³m táº¯t lá»‹ch láº·p láº¡i (Má»™t láº§n, HÃ ng ngÃ y, HÃ ng tuáº§n kÃ¨m cÃ¡c thá»© T2..CN, HÃ ng thÃ¡ng ngÃ y X, HÃ ng nÄƒm), loáº¡i sá»± kiá»‡n Ä‘á»‹a lÃ½ (Khi Ä‘áº¿n nÆ¡i / Khi rá»i Ä‘i) hoáº·c sá»± kiá»‡n Wi-Fi (Khi káº¿t ná»‘i / Khi ngáº¯t káº¿t ná»‘i / Duy trÃ¬ káº¿t ná»‘i) kÃ¨m cÃ¡c khung giá» hiá»‡u lá»±c (Active time windows).
       - **Tráº¡ng thÃ¡i há»£p lá»‡ / Lá»—i:** Äá»•i mÃ u viá»n vÃ  hiá»ƒn thá»‹ thÃ´ng Ä‘iá»‡p Ä‘á» náº¿u thiáº¿u dá»¯ liá»‡u báº¯t buá»™c (chÆ°a chá»n vá»‹ trÃ­ hoáº·c chÆ°a nháº­p Wi-Fi).
       - **Affordance & TÆ°Æ¡ng tÃ¡c:** Bo gÃ³c squircle chuáº©n, há»— trá»£ `clickable` toÃ n vÃ¹ng, icon mÅ©i tÃªn Ä‘iá»u hÆ°á»›ng (`KeyboardArrowRight`) vÃ  dÃ²ng chá»¯ gá»£i Ã½ "Nháº¥n Ä‘á»ƒ chá»‰nh sá»­a".
  2. **Táº¡o MÃ n HÃ¬nh Cáº¥u HÃ¬nh ChuyÃªn Biá»‡t `ReminderTriggerEditScreen`:**
     - Thiáº¿t káº¿ giao diá»‡n chuyÃªn biá»‡t cho Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t vá»›i `LiteverTopAppBar` mang tiÃªu Ä‘á» "Chá»‰nh sá»­a Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t", nÃºt Back vÃ  nÃºt CTA "Xong" (`R.string.action_done`).
     - Thanh tab chá»n loáº¡i kÃ­ch hoáº¡t Ä‘á»™c láº­p `ReminderTriggerTypeSelector` (Thá»i gian, Vá»‹ trÃ­, Wi-Fi) á»Ÿ Ä‘áº§u mÃ n hÃ¬nh.
     - Hiá»ƒn thá»‹ pháº§n cáº¥u hÃ¬nh chuyÃªn sÃ¢u tÆ°Æ¡ng á»©ng (`ReminderTimeConfigSection`, `ReminderLocationConfigSection`, `ReminderWifiConfigSection`).
     - `ReMindBottomBar` á»Ÿ chÃ¢n mÃ n hÃ¬nh vá»›i nÃºt "Xong" giÃºp hoÃ n táº¥t chá»‰nh sá»­a nhanh chÃ³ng báº±ng thao tÃ¡c má»™t tay.
  3. **Äá»“ng bá»™ Dá»¯ liá»‡u Hai Chiá»u qua Scoped ViewModel (`RemindersNavigation.kt`):**
     - ÄÄƒng kÃ½ route Ä‘iá»u hÆ°á»›ng má»›i `ReminderTriggerEditRoute = "reminder_trigger_edit_route/{reminderId}"`.
     - Táº¡i `remindersGraph`, `ReminderTriggerEditRoute` trá» tá»›i ViewModel cá»§a mÃ n hÃ¬nh chá»‰nh sá»­a cha thÃ´ng qua `navController.getBackStackEntry(createReminderEditRoute(reminderId))` (fallback `previousBackStackEntry`).
     - Nhá» chia sáº» chung thá»ƒ hiá»‡n `ReminderEditViewModel`, má»i thay Ä‘á»•i cáº¥u hÃ¬nh trÃªn mÃ n hÃ¬nh kÃ­ch hoáº¡t Ä‘Æ°á»£c pháº£n Ã¡nh tá»©c thÃ¬ vÃ o `ReminderEditUiState` theo thá»i gian thá»±c (Zero Desync, State Hoisting). Khi ngÆ°á»i dÃ¹ng nháº¥n "Xong" hoáº·c Back, dá»¯ liá»‡u Ä‘Ã£ Ä‘Æ°á»£c Ä‘á»“ng bá»™ 100% vá» tháº» tÃ³m táº¯t trÃªn `ReminderEditScreen`.
  4. **Chuáº©n hÃ³a ThÃ nh pháº§n Giao diá»‡n & ThÆ° viá»‡n:**
     - Chuyá»ƒn Ä‘á»•i toÃ n bá»™ dialog cáº£nh bÃ¡o quyá»n vÃ  huá»· thay Ä‘á»•i trong `ReminderEditScreen` sang `LvAlertDialog` vÃ  `LvButton` chuáº©n `litever-designsystem`.
- **Há»‡ quáº£:**
  - `ReminderEditScreen` trá»Ÿ nÃªn gá»n gÃ ng, sÃºc tÃ­ch vÃ  dá»… theo dÃµi.
  - NgÆ°á»i dÃ¹ng cÃ³ khÃ´ng gian táº­p trung tá»‘i Ä‘a khi tinh chá»‰nh cÃ¡c Ä‘iá»u kiá»‡n kÃ­ch hoáº¡t phá»©c táº¡p (Geofence, Wi-Fi, Time Windows).
  - Tráº£i nghiá»‡m chuyá»ƒn mÃ n hÃ¬nh mÆ°á»£t mÃ , nháº¥t quÃ¡n, khÃ´ng xáº£y ra xung Ä‘á»™t dá»¯ liá»‡u hay delay khi lÆ°u.
  - ToÃ n bá»™ unit tests cá»§a cÃ¡c module liÃªn quan tiáº¿p tá»¥c Ä‘áº¡t 100% kiá»ƒm thá»­ thÃ nh cÃ´ng.



### [TDR-080] - Tá»± Äá»™ng Chá»¥p áº¢nh MÃ n HÃ¬nh Play Store Báº±ng Fastlane Screengrab & CÃ´ Láº­p Dá»¯ Liá»‡u Mock Äa NgÃ´n Ngá»¯
- **NgÃ y thá»±c hiá»‡n:** 2026-09-29
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Äá»ƒ chuáº©n bá»‹ phÃ¡t hÃ nh lÃªn Google Play Store, á»©ng dá»¥ng cáº§n bá»™ áº£nh chá»¥p mÃ n hÃ¬nh hoÃ n chá»‰nh, sáº¯c nÃ©t trÃªn cÃ¡c tá»· lá»‡ mÃ n hÃ¬nh (Äiá»‡n thoáº¡i vÃ  MÃ¡y tÃ­nh báº£ng) theo 2 ngÃ´n ngá»¯ chÃ­nh (Tiáº¿ng Anh vÃ  Tiáº¿ng Viá»‡t).
  - Viá»‡c chá»¥p thá»§ cÃ´ng tá»‘n nhiá»u cÃ´ng sá»©c, dá»… sai lá»‡ch kÃ­ch thÆ°á»›c vÃ  khÃ³ duy trÃ¬ khi cáº­p nháº­t UI. Náº¿u Ä‘Æ°a dá»¯ liá»‡u test vÃ o Room DB tháº­t sáº½ gÃ¢y Ã´ nhiá»…m cÆ¡ sá»Ÿ dá»¯ liá»‡u, phá»©c táº¡p hÃ³a viá»‡c dá»n dáº¹p mÃ´i trÆ°á»ng vÃ  dá»… gÃ¢y flaky test.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **TÃ­ch há»£p Fastlane Screengrab & UiAutomator:**
     - Khai bÃ¡o `tools.fastlane:screengrab:2.1.1` vÃ  `androidx.test.uiautomator:uiautomator:2.3.0` trong `libs.versions.toml` vÃ  `app/build.gradle.kts`.
     - Ãp dá»¥ng `UiAutomatorScreenshotStrategy` chá»¥p toÃ n mÃ n hÃ¬nh Jetpack Compose sáº¯c nÃ©t, á»•n Ä‘á»‹nh tá»« Android 10 Ä‘áº¿n cÃ¡c API hiá»‡n Ä‘áº¡i nháº¥t.
     - Cáº¥p cÃ¡c quyá»n kiá»ƒm thá»­ cáº§n thiáº¿t trong `app/src/debug/AndroidManifest.xml` (`WRITE_EXTERNAL_STORAGE`, `CHANGE_CONFIGURATION`, `DUMP`).
  2. **CÃ´ Láº­p HoÃ n ToÃ n Khá»i Room Database (Zero DB Contamination):**
     - Tuyá»‡t Ä‘á»‘i khÃ´ng chÃ¨n báº£n ghi hay gá»i DAO/Room DB trong quÃ¡ trÃ¬nh chá»¥p áº£nh.
     - DÃ¹ng `createAndroidComposeRule<ComponentActivity>()` Ä‘á»ƒ setContent trá»±c tiáº¿p cÃ¡c Stateless Screen Composables (`AlarmListScreen`, `ReminderListScreen`, `AlarmRingingScreen`, `MissionRingingScreen`, `AlarmMessageScreen`).
     - ToÃ n bá»™ dá»¯ liá»‡u máº«u (Mock Data) khá»Ÿi táº¡o thuáº§n tÃºy trong bá»™ nhá»› (In-memory) vÃ  bá»c trong `ReMindTheme(darkTheme = false)` (Light Mode theo chuáº©n thiáº¿t káº¿ Store).
  3. **Quáº£n LÃ½ Chuá»—i Mock Äa NgÃ´n Ngá»¯ Qua AndroidTest Resources:**
     - Thiáº¿t láº­p tÃ i nguyÃªn chuá»—i chuyÃªn biá»‡t trong `app/src/androidTest/res/values/strings.xml` (Tiáº¿ng Anh) vÃ  `values-vi/strings.xml` (Tiáº¿ng Viá»‡t).
     - Káº¿t há»£p cÃ¹ng `@ClassRule LocaleTestRule()` cá»§a Screengrab Ä‘á»ƒ tá»± Ä‘á»™ng hoÃ¡n Ä‘á»•i ngÃ´n ngá»¯ (`vi`, `en-US`) trong má»™t láº§n cháº¡y kiá»ƒm thá»­ duy nháº¥t.
  4. **Giáº£i Quyáº¿t Triá»‡t Äá»ƒ Xung Äá»™t Scoped Storage & ADB Pull trÃªn Windows:**
     - PhÃ¡t triá»ƒn `ExternalStorageScreenshotCallback` káº¿ thá»«a `ScreenshotCallback`, Ä‘iá»u hÆ°á»›ng ghi toÃ n bá»™ áº£nh chá»¥p vÃ o thÆ° má»¥c ngoÃ i cá»§a á»©ng dá»¥ng: `context.getExternalFilesDir("screengrab")` (`/sdcard/Android/data/<package>/files/screengrab/<locale>/images/screenshots/`).
     - Cho phÃ©p lá»‡nh `adb pull` trÃªn mÃ¡y host kÃ©o áº£nh trá»±c tiáº¿p 100% khÃ´ng bá»‹ `Permission denied` (loáº¡i bá» hoÃ n toÃ n fallback pipe Unix `tar` bá»‹ treo/corrupted trÃªn Windows PowerShell vÃ  CMD).
  5. **Táº¡o Cáº¥u HÃ¬nh Screengrabfile & Lane Fastlane:**
     - Táº¡o `fastlane/Screengrabfile` vá»›i cáº¥u hÃ¬nh package name (`vn.io.litever.remind.debug` / `.test`), locales `['vi', 'en-US']` (chuáº©n ISO thÆ° má»¥c metadata Play Store), `use_timestamp_suffix(false)` vÃ  output `fastlane/metadata/android`.
     - Bá»• sung lane `:screenshots` trong `fastlane/Fastfile` tá»± Ä‘á»™ng build `assembleDebug assembleAndroidTest` vÃ  kÃ©o áº£nh vá» Ä‘Ãºng thÆ° má»¥c Play Store metadata.
- **Há»‡ quáº£:**
  - Quy trÃ¬nh chá»¥p áº£nh tá»± Ä‘á»™ng hÃ³a 100% chá»‰ báº±ng má»™t lá»‡nh duy nháº¥t: `bundle exec fastlane screenshots`.
  - Sinh vÃ  cáº­p nháº­t thÃ nh cÃ´ng 12 áº£nh mÃ n hÃ¬nh Play Store chuáº©n Light Mode (6 áº£nh Tiáº¿ng Viá»‡t trong `fastlane/metadata/android/vi/images/phoneScreenshots/` vÃ  6 áº£nh Tiáº¿ng Anh trong `fastlane/metadata/android/en-US/images/phoneScreenshots/`).
  - Tá»± Ä‘á»™ng sinh bÃ¡o cÃ¡o HTML trá»±c quan `fastlane/metadata/android/screenshots.html`.
  - TÃ¡ch biá»‡t tuyá»‡t Ä‘á»‘i khá»i cÆ¡ sá»Ÿ dá»¯ liá»‡u Room, Ä‘áº£m báº£o tÃ­nh báº¥t biáº¿n, tá»‘c Ä‘á»™ chá»¥p cá»±c nhanh (~25s/locale) vÃ  khÃ´ng sinh rÃ¡c dá»¯ liá»‡u trÃªn thiáº¿t bá»‹.

### [TDR-081] - Táº­p Trung HÃ³a Icons VÃ o ReMindIcons & CÃ´ Láº­p Dependency Material Icons
- **Date:** 2026-09-29 | **Status:** Accepted
- **Context:** CÃ¡c mÃ n hÃ¬nh ráº£i rÃ¡c import trá»±c tiáº¿p `androidx.compose.material.icons.*` vÃ  phá»¥ thuá»™c `libs.material.icons.extended` á»Ÿ má»i feature module gÃ¢y phÃ¬nh classpath vÃ  thiáº¿u kiá»ƒm soÃ¡t.
- **Decision:** Táº¡o object `ReMindIcons` táº­p trung toÃ n bá»™ icons sá»­ dá»¥ng trong `:core:designsystem`, xÃ³a hoÃ n toÃ n `material.icons.extended` khá»i `:app` vÃ  táº¥t cáº£ `:features:*`.
- **Consequences:** ToÃ n bá»™ modules chá»‰ sá»­ dá»¥ng icons qua `ReMindIcons`, cÃ´ láº­p triá»‡t Ä‘á»ƒ dependency vÃ  báº£o Ä‘áº£m Single Source of Truth cho icon UI.

### [TDR-082] - Chuáº©n HÃ³a Ã‚m LÆ°á»£ng & Rung Nháº¯c Nhá»Ÿ, LÆ°u Bá»n Vá»¯ng Checklist VÃ  Tá»± Äá»™ng Reset Theo Chu Ká»³
- **NgÃ y thá»±c hiá»‡n:** 2026-09-29
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Nháº¯c nhá»Ÿ (Reminders) gáº·p pháº£i cÃ¡c váº¥n Ä‘á»:
    1. KhÃ´ng phÃ¡t chuÃ´ng hoáº·c rung Ä‘Ãºng má»©c Ã¢m lÆ°á»£ng Ä‘Ã£ cáº¥u hÃ¬nh (`reminder.action.volume`), kÃªnh stream bá»‹ lá»‡ch giá»¯a standard notification vÃ  persistent alert.
    2. Danh sÃ¡ch checklist khÃ´ng Ä‘Æ°á»£c lÆ°u bá»n vá»¯ng khi táº¡o/sá»­a; ngÆ°á»i dÃ¹ng nháº­p dá»Ÿ checklist item nhÆ°ng báº¥m tháº³ng nÃºt LÆ°u (Save) thÃ¬ bá»‹ máº¥t text.
    3. Tráº¡ng thÃ¡i hoÃ n thÃ nh checklist (`isCompleted`) chÆ°a thá»ƒ tÆ°Æ¡ng tÃ¡c trá»±c tiáº¿p ngoÃ i danh sÃ¡ch nháº¯c nhá»Ÿ.
    4. Thiáº¿u cÆ¡ cháº¿ tá»± Ä‘á»™ng reset checklist khi bÆ°á»›c sang chu ká»³ má»›i (ngÃ y/thÃ¡ng/nÄƒm hoáº·c sau khoáº£ng thá»i gian láº·p `INTERVAL`).
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Chuáº©n HÃ³a Ã‚m LÆ°á»£ng & Rung (`ReminderAlertManager`, `ReminderNotificationManager`):**
     - Quy Ä‘á»•i tá»· lá»‡ Ã¢m lÆ°á»£ng `(volume.coerceIn(0, 100)).toFloat() / 100f` cho cáº£ `STANDARD` notification sound láº«n `FULL_SCREEN` alert.
     - Sá»­ dá»¥ng `AudioAttributes.USAGE_ALARM` káº¿t há»£p `USAGE_NOTIFICATION` Ä‘Ãºng ngá»¯ cáº£nh khi `useAlarmStream || isAlarmStyle`. PhÃ¡t Ä‘Ãºng chuÃ´ng tÃ¹y chá»‰nh `reminder.action.ringtoneUri` thay vÃ¬ gÃ¡n cá»©ng nháº¡c chuÃ´ng máº·c Ä‘á»‹nh.
     - Gáº¯n `AudioAttributes` vÃ o `Vibrator` Ä‘á»ƒ Ä‘áº£m báº£o xung rung Ä‘á»“ng bá»™ vá»›i má»©c Ä‘á»™ Æ°u tiÃªn cá»§a cáº£nh bÃ¡o.
  2. **Quáº£n LÃ½ Bá»n Vá»¯ng ID & Auto-flush Draft Checklist:**
     - Táº¡i `ReminderRepositoryImpl`: Äáº£m báº£o khi táº¡o má»›i Reminder, cÃ¡c checklist item luÃ´n Ä‘Æ°á»£c gÃ¡n `id = 0L` Ä‘á»ƒ Room SQLite tá»± Ä‘á»™ng sinh khÃ³a chÃ­nh (Primary Key Auto-generate), trÃ¡nh tÃ¬nh tráº¡ng trÃ¹ng ID dáº«n Ä‘áº¿n ghi Ä‘Ã¨ báº£n ghi (`OnConflictStrategy.REPLACE`). Khi cáº­p nháº­t, chá»‰ cÃ¡c item má»›i táº¡o táº¡m thá»i (id Ã¢m hoáº·c nanoTime) má»›i Ä‘Æ°á»£c reset vá» 0L Ä‘á»ƒ chÃ¨n má»›i.
     - Táº¡i `ReminderEditViewModel`: ThÃªm cÆ¡ cháº¿ tá»± Ä‘á»™ng náº¡p text Ä‘ang gÃµ dá»Ÿ (`draftChecklistText`) vÃ o danh sÃ¡ch checklist trÆ°á»›c khi lÆ°u náº¿u ngÆ°á»i dÃ¹ng báº¥m nÃºt LÆ°u trÃªn TopBar mÃ  quÃªn báº¥m nÃºt ThÃªm (+).
  3. **TÆ°Æ¡ng TÃ¡c Checklist Trá»±c Tiáº¿p Táº¡i ReminderCard:**
     - NÃ¢ng cáº¥p `ReminderCard`: NgÆ°á»i dÃ¹ng cÃ³ thá»ƒ nháº¥n vÃ o hÃ ng tiáº¿n Ä‘á»™ checklist Ä‘á»ƒ má»Ÿ rá»™ng (Expand) toÃ n bá»™ danh sÃ¡ch cÃ¡c Ä‘áº§u viá»‡c vÃ  tÆ°Æ¡ng tÃ¡c check/uncheck trá»±c tiáº¿p.
     - `ReminderListViewModel` vÃ  `ReminderListScreen`: ThÃªm callback `toggleChecklistItem` ghi nháº­n tráº¡ng thÃ¡i tá»©c thá»i xuá»‘ng SQLite DB mÃ  khÃ´ng lÃ m reload toÃ n bá»™ giao diá»‡n hay ngáº¯t quÃ£ng tráº£i nghiá»‡m.
  4. **Tá»± Äá»™ng Reset Checklist Theo Chu Ká»³ (Cycle Auto-Reset):**
     - Bá»• sung trÆ°á»ng `lastChecklistResetTime: Long?` vÃ o `Reminder` vÃ  `ReminderEntity`.
     - Táº¡o `MIGRATION_6_7` nÃ¢ng database lÃªn version 7 (`ALTER TABLE reminders ADD COLUMN lastChecklistResetTime INTEGER`).
     - PhÃ¡t triá»ƒn `ChecklistCycleUtil`: TÃ­nh toÃ¡n chu ká»³ reset dá»±a trÃªn `repeatMode` (`DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`) hoáº·c `INTERVAL` (theo khoáº£ng thá»i gian) so vá»›i `lastChecklistResetTime` hoáº·c `lastTriggeredTime`.
     - TÃ­ch há»£p tá»± Ä‘á»™ng quÃ©t vÃ  reset checklist:
       - Trong `ReminderReceiver`: Khi chu ká»³ reo má»›i Ä‘Æ°á»£c kÃ­ch hoáº¡t, tá»± Ä‘á»™ng reset checklist vá» chÆ°a hoÃ n thÃ nh (`isCompleted = 0`) trÆ°á»›c khi phÃ¡t chuÃ´ng/thÃ´ng bÃ¡o.
       - Trong `ReminderSyncManager` vÃ  `ReminderListViewModel`: Äá»‹nh ká»³ kiá»ƒm tra vÃ  reset khi Ä‘á»“ng bá»™ hoáº·c má»Ÿ mÃ n hÃ¬nh danh sÃ¡ch nháº¯c nhá»Ÿ.
- **Há»‡ quáº£:**
  - Nháº¯c nhá»Ÿ Ä‘á»• chuÃ´ng vÃ  rung chÃ­nh xÃ¡c vá»›i Ã¢m lÆ°á»£ng ngÆ°á»i dÃ¹ng lá»±a chá»n.
  - Checklist Ä‘Æ°á»£c lÆ°u trá»¯ bá»n vá»¯ng 100%, khÃ´ng bá»‹ máº¥t ná»™i dung khi gÃµ nhanh.
  - CÃ¡c cÃ´ng viá»‡c Ä‘á»‹nh ká»³ hÃ ng ngÃ y/tuáº§n/thÃ¡ng/nÄƒm tá»± Ä‘á»™ng sáºµn sÃ ng cho ngÃ y má»›i mÃ  khÃ´ng cáº§n thao tÃ¡c dá»n dáº¹p thá»§ cÃ´ng.
  - ToÃ n bá»™ unit tests bao gá»“m `ChecklistCycleUtilTest` vÃ  `ReminderAlertManagerTest` pass 100%.

### [TDR-083] - Loáº¡i Bá» HoÃ n ToÃ n Checklist Trong TÃ­nh NÄƒng Nháº¯c Nhá»Ÿ (Reminder)
- **NgÃ y thá»±c hiá»‡n:** 2026-09-30
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - Nháº¯c nhá»Ÿ (Reminders) ban Ä‘áº§u tÃ­ch há»£p subtask/checklist nháº±m phá»¥c vá»¥ viá»‡c theo dÃµi danh sÃ¡ch viá»‡c cáº§n lÃ m kÃ¨m theo thÃ´ng bÃ¡o.
  - Tuy nhiÃªn, trong thá»±c táº¿ sá»­ dá»¥ng vÃ  Ä‘á»‹nh hÆ°á»›ng sáº£n pháº©m, Reminder cáº§n táº­p trung tá»‘i Ä‘a vÃ o Ä‘á»™ tin cáº­y cá»§a viá»‡c cáº£nh bÃ¡o thá»i gian (`FIXED_TIME` & `INTERVAL`), trÃ¡nh lÃ m rá»‘i tráº£i nghiá»‡m ngÆ°á»i dÃ¹ng vá»›i viá»‡c quáº£n lÃ½ danh sÃ¡ch cÃ´ng viá»‡c phá»©c táº¡p vá»‘n thuá»™c vá» app Todo chuyÃªn biá»‡t.
  - Sá»± tá»“n táº¡i cá»§a Checklist lÃ m phÃ¡t sinh nhiá»u phá»©c táº¡p: báº£ng quan há»‡ `checklist_items`, `PopulatedReminder`, cÃ¡c vÃ²ng láº·p reset chu ká»³, thao tÃ¡c inline checklist toggle trÃªn notification, kÃ©o dÃ i mÃ n hÃ¬nh sá»­a nháº¯c nhá»Ÿ vÃ  gia tÄƒng rá»§i ro race condition khi Ä‘á»“ng bá»™ Room.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Táº§ng Model (`:core:model`):**
     - XÃ³a hoÃ n toÃ n model `ChecklistItem.kt`.
     - Loáº¡i bá» trÆ°á»ng `checklist` cÃ¹ng toÃ n bá»™ helper tÃ­nh tiáº¿n Ä‘á»™ (`checklistProgress`, `totalChecklistCount`, `completedChecklistCount`, `isAllChecklistCompleted`) khá»i model `Reminder.kt`.
  2. **Táº§ng CÆ¡ Sá»Ÿ Dá»¯ Liá»‡u (`:core:database`):**
     - XÃ³a `ChecklistItemEntity.kt`, `ChecklistItemDao.kt`, vÃ  model quan há»‡ `PopulatedReminder.kt`.
     - `ReminderDao`: Trá»±c tiáº¿p tráº£ vá» `ReminderEntity` thay vÃ¬ bá»c trong `PopulatedReminder`, bá» `updateReminderWithChecklist`.
     - `RemindDatabase`: Loáº¡i bá» `ChecklistItemEntity` khá»i danh sÃ¡ch `@Database.entities`, xÃ³a DAO getter `checklistItemDao()`.
     - `DatabaseModule`: Loáº¡i bá» `provideChecklistItemDao`.
  3. **Táº§ng Domain & Data (`:core:domain`, `:core:data`):**
     - XÃ³a tiá»‡n Ã­ch `ChecklistCycleUtil.kt`.
     - `ReminderRepository`: Loáº¡i bá» cÃ¡c phÆ°Æ¡ng thá»©c `updateChecklistItemCompletion`, `resetChecklist`, `checkAndResetRecurringChecklists`.
     - XÃ³a mapper `ChecklistItemMapper.kt` vÃ  mapper test tÆ°Æ¡ng á»©ng.
     - `ReminderMapper`: Tinh gá»n chá»‰ map trá»±c tiáº¿p giá»¯a `ReminderEntity` vÃ  `Reminder`.
  4. **Táº§ng Cáº£nh BÃ¡o & ThÃ´ng BÃ¡o (`:core:alarm`):**
     - Loáº¡i bá» action `ACTION_REMINDER_CHECKLIST_TOGGLE` khá»i `ReminderReceiver` vÃ  `AndroidManifest.xml`.
     - `ReminderNotificationManager`: Bá» viá»‡c render checklist tÃ³m táº¯t vÃ  pending intent toggle checklist item inline.
     - `ReminderSyncManager`: Bá» lá»‡nh tá»± Ä‘á»™ng kiá»ƒm tra vÃ  reset recurring checklists.
  5. **Táº§ng Giao Diá»‡n & ViewModel (`:features:reminders`):**
     - XÃ³a cÃ¡c composable component: `ReminderChecklistBuilderSection.kt` vÃ  `ReminderRingingChecklist.kt`.
     - `ReminderCard.kt`: Loáº¡i bá» thanh tiáº¿n Ä‘á»™ checklist vÃ  danh sÃ¡ch checklist con má»Ÿ rá»™ng.
     - `ReminderListViewModel.kt` & `ReminderListScreen.kt`: Bá» callback `toggleChecklistItem` vÃ  logic duplicate checklist.
     - `ReminderEditViewModel.kt` & `ReminderEditScreen.kt`: XÃ³a `checklist`, `draftChecklistText`, vÃ  toÃ n bá»™ hÃ m CRUD checklist (`addChecklistItem`, `moveChecklistItem`, v.v.).
     - `ReminderRingingViewModel.kt` & `ReminderRingingScreen.kt`: XÃ³a pháº§n render checklist khi Ä‘ang reo chuÃ´ng vÃ  action `toggleChecklistItem`.
     - XÃ³a toÃ n bá»™ chuá»—i kÃ½ tá»± i18n liÃªn quan Ä‘áº¿n checklist trong `values/strings.xml` vÃ  `values-vi/strings.xml`.
  6. **Äá»“ng Bá»™ & Dá»n Sáº¡ch Test Suite:**
     - TÃ¡i cáº¥u trÃºc cÃ¡c challenger test (`ReminderDatabaseChallengerTest`, `ReminderSnoozeChallengerTest`, `ReminderPermissionChallengerTest`) Ä‘á»ƒ táº­p trung vÃ o Snooze, State Isolation vÃ  Permission UX.
     - Dá»n sáº¡ch `ReminderEditViewModelTest`, `ReminderListViewModelTest`, `ReminderRingingViewModelTest`, vÃ  `ScreenshotsTest`.
- **Há»‡ quáº£:**
  - Kiáº¿n trÃºc Reminder trá»Ÿ nÃªn tinh gá»n, hiá»‡u nÄƒng truy váº¥n Room DB tÄƒng Ä‘Ã¡ng ká»ƒ (khÃ´ng cáº§n JOIN hay Transaction Ä‘a báº£ng).
  - Giao diá»‡n ngÆ°á»i dÃ¹ng thanh thoÃ¡t, táº­p trung Ä‘Ãºng vÃ o trá»ng tÃ¢m thiáº¿t láº­p nháº¯c nhá»Ÿ vÃ  bÃ¡o thá»©c.
  - ToÃ n bá»™ 473 task build vÃ  unit tests cá»§a dá»± Ã¡n vÆ°á»£t qua 100% khÃ´ng lá»—i.

### [TDR-084] - Loáº¡i Bá» CÆ¡ Cháº¿ Tá»± Äá»™ng ThÃªm Dá»¯ Liá»‡u Máº«u Cho Database (Debug Seeding)
- **NgÃ y thá»±c hiá»‡n:** 2026-09-30
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - TrÆ°á»›c Ä‘Ã¢y, á»©ng dá»¥ng cÃ³ callback `RemindDatabaseCallback` trong source set `debug` tá»± Ä‘á»™ng chÃ¨n 5 bÃ¡o thá»©c vÃ  3 nhiá»‡m vá»¥ máº«u (`MATH`, `SHAKE`, `TYPING`) vÃ o SQLite Room DB khi má»Ÿ CSDL láº§n Ä‘áº§u náº¿u báº£ng `alarms` trá»‘ng.
  - Viá»‡c tá»± Ä‘á»™ng sinh dá»¯ liá»‡u máº«u nÃ y gÃ¢y phiá»n toÃ¡i khi test thá»±c táº¿ hoáº·c kiá»ƒm thá»­ luá»“ng trá»‘ng (Empty State UI), khÃ´ng pháº£n Ã¡nh Ä‘Ãºng tráº¡ng thÃ¡i dá»¯ liá»‡u ngÆ°á»i dÃ¹ng thá»±c thá»¥ khi cÃ i má»›i app.
- **Quyáº¿t Ä‘á»‹nh:**
  - XÃ³a bá» file `RemindDatabaseCallback.kt` á»Ÿ cáº£ hai source set `core/database/src/debug` vÃ  `core/database/src/release`.
  - Trong `DatabaseModule.kt`: Loáº¡i bá» `.addCallback(RemindDatabaseCallback())` khá»i builder cá»§a Room Database.
- **Há»‡ quáº£:**
  - CÆ¡ sá»Ÿ dá»¯ liá»‡u luÃ´n báº¯t Ä‘áº§u á»Ÿ tráº¡ng thÃ¡i trá»‘ng hoÃ n toÃ n (clean state), giÃºp kiá»ƒm thá»­ chÃ­nh xÃ¡c cÃ¡c mÃ n hÃ¬nh vÃ  tráº¡ng thÃ¡i empty state.
  - ToÃ n bá»™ 473 task build vÃ  test cá»§a dá»± Ã¡n hoÃ n thÃ nh 100% khÃ´ng lá»—i.



### [TDR-085] - TÃ¡i Cáº¥u TrÃºc Logic BÃ¡o Láº¡i (Flexible Snooze) vÃ  PhÃ¢n Loáº¡i Cáº£nh BÃ¡o Nháº¯c Nhá»Ÿ
- **NgÃ y thá»±c hiá»‡n:** 2026-10-01
- **Tráº¡ng thÃ¡i:** Accepted
- **Bá»‘i cáº£nh:**
  - NgÆ°á»i dÃ¹ng yÃªu cáº§u hiá»‡n Ä‘áº¡i hÃ³a vÃ  lÃ m sáº¡ch logic nháº¯c nhá»Ÿ:
    1. CÆ¡ cháº¿ hoÃ£n/bÃ¡o láº¡i linh hoáº¡t (Flexible Snooze): Cho phÃ©p chá»n nhanh thá»i gian bÃ¡o láº¡i (+5m, +10m, +15m) hoáº·c nháº­p sá»‘ phÃºt tÃ¹y chá»‰nh ngay trÃªn mÃ n hÃ¬nh reo chuÃ´ng, loáº¡i bá» hoÃ n toÃ n giá»›i háº¡n sá»‘ láº§n láº·p láº¡i (snoozeRepeatCount).
    2. PhÃ¢n loáº¡i kiá»ƒu thÃ´ng bÃ¡o thÃ nh "ThÃ´ng bÃ¡o thÆ°á»ng" (STANDARD) vÃ  "Äá»• chuÃ´ng" (FULL_SCREEN). Máº·c Ä‘á»‹nh khi táº¡o má»›i lÃ  "Äá»• chuÃ´ng". Cáº£ hai Ä‘á»u hiá»ƒn thá»‹ giao diá»‡n toÃ n mÃ n hÃ¬nh, nhÆ°ng dáº¡ng "Äá»• chuÃ´ng" cÃ³ Foreground Service (ReminderAlertService) phÃ¡t nháº¡c chuÃ´ng liÃªn tá»¥c vÃ  há»— trá»£ tá»± Ä‘á»™ng táº¯t tiáº¿ng / phÃ¡t qua kÃªnh bÃ¡o thá»©c (STREAM_ALARM).
    3. Cáº£ hai kiá»ƒu thÃ´ng bÃ¡o Ä‘á»u há»— trá»£ tÃ¹y chá»‰nh nháº¡c chuÃ´ng, Ã¢m lÆ°á»£ng vÃ  rung.
    4. TrÃªn thÃ´ng bÃ¡o thu nhá» (heads-up / banner), cung cáº¥p nÃºt "BÃ¡o láº¡i 5 phÃºt" vÃ  nÃºt xÃ¡c nháº­n hoÃ n thÃ nh ("HoÃ n thÃ nh").
    5. Dá»n dáº¹p sáº¡ch mÃ£ nguá»“n, loáº¡i bá» snoozeRepeatCount vÃ  currentSnoozeCount.
- **Quyáº¿t Ä‘á»‹nh:**
  1. **Domain Model (:core:model):**
     - Loáº¡i bá» snoozeRepeatCount khá»i ReminderAction vÃ  currentSnoozeCount khá»i Reminder.
     - Chuyá»ƒn giÃ¡ trá»‹ máº·c Ä‘á»‹nh cá»§a 
otificationStyle sang NotificationStyle.FULL_SCREEN.
     - Tinh giáº£n canSnooze(): chá»‰ kiá»ƒm tra snoozeEnabled && urgencyLevel != UrgencyLevel.SOFT.
  2. **Data Mapper (:core:data):**
     - Cáº­p nháº­t ReminderMapper.kt bá» mapping 2 trÆ°á»ng Ä‘Ã£ xÃ³a sang domain model, gÃ¡n giÃ¡ trá»‹ máº·c Ä‘á»‹nh á»•n Ä‘á»‹nh khi ghi xuá»‘ng Room Entity.
  3. **Alarm & Notification Subsystem (:core:alarm):**
     - ReminderNotificationManager.kt: Heads-up notification hiá»ƒn thá»‹ nÃºt "BÃ¡o láº¡i 5 phÃºt" (kÃ¨m EXTRA_SNOOZE_MINUTES = 5) vÃ  nÃºt "HoÃ n thÃ nh".
     - ReminderReceiver.kt: Há»— trá»£ EXTRA_SNOOZE_MINUTES Ä‘á»ƒ láº­p lá»‹ch snooze theo sá»‘ phÃºt ngÆ°á»i dÃ¹ng chá»n.
  4. **Giao diá»‡n & ViewModel (:features:reminders):**
     - ReminderAlertConfigSection.kt: Äá»•i nhÃ£n thÃ nh "ThÃ´ng bÃ¡o thÆ°á»ng" vÃ  "Äá»• chuÃ´ng". Cáº¥u hÃ¬nh Ã¢m thanh (nháº¡c chuÃ´ng, Ã¢m lÆ°á»£ng, rung) Ã¡p dá»¥ng cho cáº£ hai cháº¿ Ä‘á»™. Tá»± Ä‘á»™ng táº¯t tiáº¿ng vÃ  KÃªnh bÃ¡o thá»©c chá»‰ hiá»ƒn thá»‹ á»Ÿ cháº¿ Ä‘á»™ "Äá»• chuÃ´ng". Loáº¡i bá» má»¥c chá»n sá»‘ láº§n láº·p láº¡i snooze.
     - ReminderRingingScreen.kt: Bá»• sung hÃ ng nÃºt chá»n nhanh (+5m, +10m, +15m) vÃ  nÃºt "TÃ¹y chá»‰nh" má»Ÿ LvAlertDialog vá»›i LiteverTextField Ä‘á»ƒ nháº­p sá»‘ phÃºt tÃ¹y thÃ­ch.
     - ReminderRingingViewModel.kt: NÃ¢ng cáº¥p hÃ m snooze(minutes: Int, onFinish: () -> Unit) nháº­n sá»‘ phÃºt linh hoáº¡t.
     - ReminderEditViewModel.kt & ReminderEditScreen.kt: Loáº¡i bá» toÃ n bá»™ references Ä‘áº¿n snoozeRepeatCount.
  5. **Äá»“ng bá»™ Unit Tests:**
     - Cáº­p nháº­t toÃ n bá»™ cÃ¡c bÃ i test liÃªn quan trong :core:alarm, :core:model, :core:data, :features:reminders. ToÃ n bá»™ 473 task build vÃ  unit tests cá»§a dá»± Ã¡n vÆ°á»£t qua 100%.
- **Há»‡ quáº£:**
  - Tráº£i nghiá»‡m ngÆ°á»i dÃ¹ng khi nháº¯c nhá»Ÿ Ä‘áº¿n háº¡n trá»±c quan, linh hoáº¡t vÃ  thuáº­n tiá»‡n.
  - Codebase sáº¡ch, tuÃ¢n thá»§ Clean Architecture vÃ  Design System chung.

### [2026-10-01] HoÃ n thiá»‡n vÃ  lÃ m sáº¡ch giao diá»‡n ThÃªm/Sá»­a Lá»i nháº¯c (ReminderEditScreen & ReminderAlertConfigSection)
- **Má»¥c tiÃªu:**
  - LÃ m sáº¡ch má»¥c "Cáº£nh bÃ¡o & ThÃ´ng bÃ¡o" trong mÃ n hÃ¬nh ThÃªm/Sá»­a lá»i nháº¯c:
    - Loáº¡i bá» hoÃ n toÃ n bá»™ chá»n UrgencyLevel (SOFT/MEDIUM/HARD) gÃ¢y rÆ°á»m rÃ .
    - ÄÆ°a 2 cháº¿ Ä‘á»™ thÃ´ng bÃ¡o lÃªn Ä‘áº§u: "Äá»• chuÃ´ng" (máº·c Ä‘á»‹nh) vÃ  "ThÃ´ng bÃ¡o thÆ°á»ng" vá»›i mÃ´ táº£ chi tiáº¿t trá»±c quan.
    - Cáº¥u hÃ¬nh Nháº¡c chuÃ´ng, Ã‚m lÆ°á»£ng vÃ  Rung hiá»ƒn thá»‹ vÃ  Ã¡p dá»¥ng cho cáº£ 2 kiá»ƒu thÃ´ng bÃ¡o; TÃªn nháº¡c chuÃ´ng Ä‘Æ°á»£c trÃ­ch xuáº¥t Ä‘á»™ng qua RingtoneManager thay vÃ¬ chuá»—i cá»©ng.
    - Tá»± Ä‘á»™ng táº¯t tiáº¿ng vÃ  KÃªnh Ã¢m thanh bÃ¡o thá»©c hiá»ƒn thá»‹ duy nháº¥t khi chá»n "Äá»• chuÃ´ng".
    - Cáº¥u hÃ¬nh BÃ¡o láº¡i (Snooze) Ä‘Æ°á»£c nhÃºng trá»±c tiáº¿p ngay trong section (cÃ´ng táº¯c báº­t/táº¯t + chá»n nhanh khoáº£ng thá»i gian 5p, 10p, 15p, 30p), loáº¡i bá» viá»‡c Ä‘iá»u hÆ°á»›ng sang mÃ n hÃ¬nh riÃªng vÃ  loáº¡i bá» giá»›i háº¡n sá»‘ láº§n láº·p láº¡i.
- **Thá»±c hiá»‡n:**
  - `features/reminders/src/main/java/vn/io/litever/remind/features/reminders/ui/components/ReminderAlertConfigSection.kt`:
    - Loáº¡i bá» tham sá»‘ vÃ  UI cá»§a `UrgencyLevel`.
    - ThÃªm mÃ´ táº£ trá»±c quan dÆ°á»›i bá»™ chá»n cháº¿ Ä‘á»™: `style_full_screen_desc` vÃ  `style_standard_desc`.
    - TÃ­ch há»£p inline Snooze: Switch báº­t/táº¯t vÃ  `LiteverSingleChoiceSegmentedButtonRow` chá»n khoáº£ng thá»i gian (5, 10, 15, 30 phÃºt).
    - Cáº­p nháº­t cÃ¡c hÃ m Preview (chuáº©n, thÆ°á»ng, cháº¿ Ä‘á»™ tá»‘i).
  - `features/reminders/src/main/java/vn/io/litever/remind/features/reminders/ui/ReminderEditScreen.kt`:
    - TrÃ­ch xuáº¥t tÃªn nháº¡c chuÃ´ng tá»« `RingtoneManager.getRingtone()` dá»±a trÃªn `uiState.ringtoneUri`.
    - Káº¿t ná»‘i `onSnoozeEnabledChange` vÃ  `onSnoozeIntervalChange` trá»±c tiáº¿p vá»›i ViewModel.
    - Loáº¡i bá» cÃ¡c param dÆ° thá»«a vÃ  listener khÃ´ng dÃ¹ng Ä‘áº¿n.
  - `features/reminders/src/main/java/vn/io/litever/remind/features/reminders/viewmodel/ReminderEditViewModel.kt`:
    - ThÃªm `setSnoozeEnabled` vÃ  `setSnoozeIntervalMinutes`.
- **Káº¿t quáº£ kiá»ƒm thá»­:**
  - `./gradlew testDebugUnitTest`: VÆ°á»£t qua 100% toÃ n bá»™ 473 task cá»§a dá»± Ã¡n khÃ´ng má»™t lá»—i nÃ o.

### [2026-10-01] Äá»•i text tab sang "Nháº¯c nhá»Ÿ" vÃ  loáº¡i bá» bá»™ lá»c á»Ÿ mÃ n hÃ¬nh danh sÃ¡ch nháº¯c nhá»Ÿ
- **Má»¥c tiÃªu:**
  - Äá»•i nhÃ£n tab Ä‘iá»u hÆ°á»›ng vÃ  tiÃªu Ä‘á» tá»« "Lá»i nháº¯c" thÃ nh "Nháº¯c nhá»Ÿ".
  - Loáº¡i bá» hoÃ n toÃ n hÃ ng filter chips (`ReminderFilterChips`) cÃ¹ng toÃ n bá»™ logic lá»c khÃ´ng cáº§n thiáº¿t (ALL, ACTIVE, COMPLETED, TIME, LOCATION, WIFI) khá»i mÃ n hÃ¬nh danh sÃ¡ch nháº¯c nhá»Ÿ.
  - Tinh giáº£n `ReminderListViewModel`, `ReminderListUiState`, `ReminderListScreen`, `ReminderEmptyState` vÃ  cáº­p nháº­t tÆ°Æ¡ng á»©ng cÃ¡c unit test.
- **Thá»±c hiá»‡n:**
  - **TÃ i nguyÃªn chuá»—i:**
    - `app/src/main/res/values-vi/strings.xml`: Cáº­p nháº­t `navigation_reminders` thÃ nh "Nháº¯c nhá»Ÿ".
    - `features/reminders/src/main/res/values-vi/strings.xml`: Cáº­p nháº­t `navigation_reminders`, `reminders_title`, `add_reminder_title`, `edit_reminder_title`, `no_reminders`, `empty_reminders_description`, `delete_completed_reminders`, `delete_disabled_reminders`, `reminder_deleted`.
    - Dá»n dáº¹p cÃ¡c chuá»—i lá»c (`filter_*`, `no_*_reminders`) khá»i `values/strings.xml` vÃ  `values-vi/strings.xml`.
  - **Giao diá»‡n & ThÃ nh pháº§n (`features/reminders`):**
    - `ReminderListScreen.kt`: XÃ³a hoÃ n toÃ n component `ReminderFilterChips` vÃ  tham sá»‘ `onFilterSelected`. Hiá»ƒn thá»‹ trá»±c tiáº¿p danh sÃ¡ch `reminders` Ä‘Ã£ Ä‘Æ°á»£c sáº¯p xáº¿p hoáº·c `ReminderEmptyState` Ä‘Æ¡n giáº£n hÃ³a.
    - `ReminderEmptyState.kt`: Loáº¡i bá» tham sá»‘ `selectedFilter`, luÃ´n hiá»ƒn thá»‹ tráº¡ng thÃ¡i rá»—ng chuáº©n (`no_reminders`, `empty_reminders_description`).
    - XÃ³a file `ReminderFilterChips.kt`.
  - **ViewModel & State:**
    - `ReminderListViewModel.kt`: Loáº¡i bá» `_selectedFilter`, `setFilter`, `filterCounts`. Sáº¯p xáº¿p trá»±c tiáº¿p luá»“ng nháº¯c nhá»Ÿ (Æ°u tiÃªn nháº¯c nhá»Ÿ Ä‘ang snooze lÃªn Ä‘áº§u, sau Ä‘Ã³ theo `createdAt` giáº£m dáº§n).
    - `ReminderListUiState`: Tinh giáº£n state chá»‰ cÃ²n `reminders`, `is24HourFormat`, `hasPermissionWarning`, `missingPermissions`.
  - **Kiá»ƒm thá»­ Ä‘Æ¡n vá»‹:**
    - `ReminderListViewModelTest.kt`: XÃ³a cÃ¡c test chuyá»ƒn filter, thÃªm test xÃ¡c minh quy táº¯c sáº¯p xáº¿p tá»± Ä‘á»™ng.
    - `ReminderR1R2EmpiricalChallengerTest.kt`: Cáº­p nháº­t cÃ¡c test case Ä‘á»ƒ kiá»ƒm tra quy táº¯c sáº¯p xáº¿p danh sÃ¡ch trá»±c tiáº¿p khÃ´ng phá»¥ thuá»™c filter.
- **Káº¿t quáº£ kiá»ƒm thá»­:**
  - `./gradlew testDebugUnitTest`: VÆ°á»£t qua 100% toÃ n bá»™ 473 task thÃ nh cÃ´ng.

### [2026-10-01] Loáº¡i bá» thanh chá»‰nh Ã¢m lÆ°á»£ng á»Ÿ cháº¿ Ä‘á»™ ThÃ´ng bÃ¡o thÆ°á»ng (NotificationStyle.STANDARD)
- **Bá»‘i cáº£nh & XÃ¡c nháº­n ká»¹ thuáº­t:**
  - Trong Android (API 26+), thÃ´ng bÃ¡o tiÃªu chuáº©n (Notification/NotificationCompat) phÃ¡t Ã¢m thanh thÃ´ng qua KÃªnh thÃ´ng bÃ¡o há»‡ thá»‘ng (`STREAM_NOTIFICATION`).
  - Há»‡ Ä‘iá»u hÃ nh Android KHÃ”NG cung cáº¥p API Ä‘á»ƒ Ä‘áº·t Ã¢m lÆ°á»£ng chuÃ´ng riÃªng biá»‡t cho tá»«ng thÃ´ng bÃ¡o Ä‘á»™c láº­p (chá»‰ cÃ³ thá»ƒ chá»n file nháº¡c chuÃ´ng vÃ  báº­t/táº¯t rung). Ã‚m lÆ°á»£ng phÃ¡t luÃ´n tuÃ¢n theo má»©c Ã¢m lÆ°á»£ng thÃ´ng bÃ¡o tá»•ng cá»§a thiáº¿t bá»‹.
  - Do Ä‘Ã³, viá»‡c hiá»ƒn thá»‹ thanh trÆ°á»£t Ã¢m lÆ°á»£ng á»Ÿ cháº¿ Ä‘á»™ "ThÃ´ng bÃ¡o thÆ°á»ng" lÃ  khÃ´ng cÃ³ tÃ¡c dá»¥ng thá»±c táº¿.
- **Thá»±c hiá»‡n:**
  - `ReminderAlertConfigSection.kt`:
    - Thanh trÆ°á»£t Ã¢m lÆ°á»£ng (Volume Slider) chá»‰ hiá»ƒn thá»‹ á»Ÿ cháº¿ Ä‘á»™ "Äá»• chuÃ´ng" (`NotificationStyle.FULL_SCREEN` - nÆ¡i cÃ³ foreground service phÃ¡t Ã¢m thanh liÃªn tá»¥c qua `MediaPlayer.setVolume()`).
    - á»ž cháº¿ Ä‘á»™ "ThÃ´ng bÃ¡o thÆ°á»ng" (`NotificationStyle.STANDARD`), loáº¡i bá» thanh trÆ°á»£t Ã¢m lÆ°á»£ng vÃ  hiá»ƒn thá»‹ má»™t hÃ ng cÃ´ng táº¯c "Rung" (`LiteverSwitch`) Ä‘á»™c láº­p, kÃ¨m mÃ´ táº£ "Rung khi thÃ´ng bÃ¡o xuáº¥t hiá»‡n" / "Táº¯t".
  - Bá»• sung chuá»—i tÃ i nguyÃªn `vibration_enabled_desc` trong `values/strings.xml` vÃ  `values-vi/strings.xml`.
- **Káº¿t quáº£ kiá»ƒm thá»­:**
  - `./gradlew testDebugUnitTest`: VÆ°á»£t qua 100% toÃ n bá»™ 473 task thÃ nh cÃ´ng.

### [2026-10-01] Refactor PaletteColorOption & Loáº¡i bá» callback navigate thá»«a trong SettingsScreen
- **Má»¥c tiÃªu:**
  - Cáº­p nháº­t `PaletteColorOption` trong `GeneralSettingsScreen` sá»­ dá»¥ng cáº·p mÃ u `primary` vÃ  `onPrimary` (thay tháº¿ cho `primaryContainer`).
  - Dá»n dáº¹p cÃ¡c callback Ä‘iá»u hÆ°á»›ng khÃ´ng sá»­ dá»¥ng (`onNavigateToQA`, `onNavigateToAlarmSettings`, `onNavigateToLicenses`) khá»i `SettingsRoute`, `SettingsScreen`, `settingsGraph` vÃ  `MainActivity`.
- **Thá»±c hiá»‡n:**
  - `GeneralSettingsScreen.kt`:
    - Äá»•i trÆ°á»ng `primaryContainerLight` / `primaryContainerDark` cá»§a `PaletteColorOption` sang `onPrimaryLight` / `onPrimaryDark`.
    - Cáº­p nháº­t dáº£i mÃ u máº«u `ColorCircleSwatch` Ä‘á»ƒ hiá»ƒn thá»‹ Icon Check cÃ³ mÃ u tint tÆ°Æ¡ng pháº£n `onPrimaryColor` trÃªn ná»n trÃ²n `primaryColor`.
  - `SettingsScreen.kt`, `SettingsNavigation.kt`, `MainActivity.kt`:
    - Loáº¡i bá» tham sá»‘ Ä‘iá»u hÆ°á»›ng khÃ´ng dÃ¹ng (`onNavigateToQA`, `onNavigateToAlarmSettings`, `onNavigateToLicenses`) khá»i `SettingsRoute` & `SettingsScreen`.
    - Tinh giáº£n `settingsGraph` vÃ  loáº¡i bá» cÃ¡c import/call thá»«a á»Ÿ `MainActivity`.

### [2026-10-01] Refactor UI ReminderTriggerSection - ÄÆ°a cá»¥m nÃºt chá»n khoáº£ng thá»i gian láº·p vÃ o cÃ¹ng surface container
- **Má»¥c tiÃªu:**
  - Gom cá»¥m cÃ¡c nÃºt báº¥m chá»n nhanh khoáº£ng thá»i gian láº·p (`15m`, `30m`, `1h`, `2h`, `3h`, `4h`) vÃ o cÃ¹ng khá»‘i card container `surfaceContainer` vá»›i hiá»ƒn thá»‹ sá»‘ thá»i gian láº·p (`Repeat Every`).
- **Thá»±c hiá»‡n:**
  - `ReminderTriggerSection.kt`:
    - Chuyá»ƒn container tá»« dáº¡ng `Row` sang `Column` vá»›i Ä‘á»‹nh dáº¡ng `.clip(RoundedCornerShape(12.dp)).background(LiteverTheme.colors.surfaceContainer).padding(LiteverTheme.spacing.medium)`.
    - ÄÆ°a tiÃªu Ä‘á» + hiá»ƒn thá»‹ thá»i gian + nÃºt báº¥m Äáº·t láº¡i (`Reset`) vÃ o thanh `Row` phÃ­a trÃªn, Ä‘á»“ng thá»i Ä‘áº·t danh sÃ¡ch nÃºt preset bÃªn trong container nÃ y Ä‘á»ƒ chia sáº» chung mÃ u ná»n surface.
- **Káº¿t quáº£ kiá»ƒm thá»­:**
  - `ReminderTriggerSectionIntervalPreview` hiá»ƒn thá»‹ Ä‘Ãºng giao diá»‡n thiáº¿t káº¿ má»›i.
  - Build Gradle `:features:reminders:assembleDebug` thÃ nh cÃ´ng.





