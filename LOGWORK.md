# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này dùng để ghi vết (tracking) quá trình thực thi các tính năng, lịch sử thay đổi kiến trúc và các quyết định kỹ thuật quan trọng của Ứng dụng Báo Thức.

---

## 🚀 Trạng Thái Dự Án
- **Phase Hiện Tại:** Phase 6 (Maintenance & Optimization)
- **Tiến Độ:** 100%
- **Ngày cập nhật cuối:** 2026-05-30
- **Phiên bản hiện tại:** 1.1.6 (Build 9)



## 📍 Lộ Trình Tổng Thể
- [x] **Phase 1:** Hoàn thiện Tầng Dữ liệu (Room DB & Repository)
- [x] **Phase 2:** Cơ Chế Lập Lịch Báo Thức (AlarmManager & Receivers)
- [x] **Phase 3:** Giao diện Người dùng (CRUD Alarm List & Create)
- [x] **Phase 4:** Trải Nghiệm Màn Hình Chuông & Tối ưu UX
- [x] **Phase 5:** Tách Module Design System & Chuẩn hóa Đa dự án (Modularization)

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

---

## 🛠 Changelog (Tính năng mới)

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
