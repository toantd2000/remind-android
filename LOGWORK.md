# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này dùng để ghi vết (tracking) quá trình thực thi các tính năng, lịch sử thay đổi kiến trúc và các quyết định kỹ thuật quan trọng của Ứng dụng Báo Thức.

---

## 🚀 Trạng Thái Dự Án
- **Phase Hiện Tại:** Phase 11 (Tái Cấu Trúc Vòng Đời Reminder - Loại Bỏ isCompleted & Tối Giản UX Danh Sách Nhắc Nhở)
- **Tiến Độ:** 100%
- **Ngày cập nhật cuối:** 2026-10-01
- **Phiên bản hiện tại:** 2.2.0

## 📍 Lộ Trình Tổng Thể
- [x] **Phase 1:** Hoàn thiện Tầng Dữ liệu (Room DB & Repository)
- [x] **Phase 2:** Cơ Chế Lập Lịch Báo Thức (AlarmManager & Receivers)
- [x] **Phase 3:** Giao diện Người dùng (CRUD Alarm List & Create)
- [x] **Phase 4:** Trải Nghiệm Màn Hình Chuông & Tối ưu UX
- [x] **Phase 5:** Tách Module Design System & Chuẩn hóa Đa dự án (Modularization)
- [x] **Phase 6:** Tối ưu hóa & Bảo trì (Maintenance & Optimization)
- [x] **Phase 7:** Nâng cấp thư viện LiteVer Design System 2.0.0 & Refactor toàn diện Material 3 (includeBuild & M3 Defaults)
- [x] **Phase 8:** Nâng cấp thư viện LiteVer Design System 2.1.0 & Chuyển đổi toàn diện sang bộ thành phần Opinionated Lv* (LvButton, LvIconButton, LvTextField, LvAlertDialog, LvChip, LvSnackbar)
- [x] **Phase 9:** Tái Cấu Trúc CSDL Nhắc Nhở & Hoàn Thiện Mô Hình FIXED_TIME / INTERVAL (Multi-day Recurrence, UrgencyLevel, Room MIGRATION_4_5)
- [x] **Phase 10:** Loại Bỏ Hoàn Toàn Checklist Trong Tính Năng Reminder - Tối Giản Trải Nghiệm & Kiến Trúc
- [x] **Phase 11:** Tái Cấu Trúc Vòng Đời Reminder (Loại bỏ hoàn toàn isCompleted, thống nhất quản lý qua isEnabled, gỡ bỏ Switch khỏi ReminderCard)


---

## 🧠 Nhật Ký Quyết Định Kỹ Thuật (TDR)

### [TDR-091] - Tái Cấu Trúc Toàn Diện ReminderTriggerSection & ReminderEditScreen: Đưa Cấu Hình Lặp Lịch (Repeat) Ra Ngoài Cấp Độ Nhắc Nhở & Làm Sạch TimeWindow
- **Ngày thực hiện:** 2026-10-05
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trước đây, `ReminderRepeatConfigSection` (lặp 1 lần, hàng tuần, hàng tháng, hàng năm) bị nhúng trùng lặp ở cả hai nhánh con: bên trong `ReminderTimeConfigSection` (cho Fixed Time) và bên trong nhánh `INTERVAL` của `ReminderTriggerSection`.
  - Cấu trúc này làm rò rỉ trách nhiệm (props drilling hơn 12 tham số repeat qua lại), vi phạm Single Responsibility Principle và gây rối rắm trải nghiệm người dùng.
  - Về bản chất, **mọi nhắc nhở đều có chu kỳ ngày lặp lại** (Một lần vào ngày cụ thể, hàng tuần theo thứ, hàng tháng, hàng năm). Sự khác biệt giữa `FIXED_TIME` và `INTERVAL` chỉ nằm ở cách kích hoạt trong ngày: reo vào các mốc giờ cố định hay lặp lại theo chu kỳ phút trong các khung giờ hoạt động (`TimeWindow`).
  - Ngoài ra, trong `ReminderTimeWindowConfigSection`, từng khung giờ lại có lựa chọn "Tất cả các ngày" / "Ngày đã chọn" với danh sách chọn thứ trong tuần (`activeDays`), gây dư thừa và xung đột với lịch lặp ngày ở cấp độ nhắc nhở. File `ReminderTimeWindowDialog.kt` bị bỏ quên không sử dụng (dead code).
- **Quyết định:**
  1. **Đưa `ReminderRepeatConfigSection` ra ngoài `ReminderEditScreen`:**
     - Đặt làm Section độc lập ngay dưới `ReminderBasicInfoSection`, định nghĩa chu kỳ lặp lại cấp cao cho toàn bộ nhắc nhở.
     - Loại bỏ hoàn toàn việc nhúng `ReminderRepeatConfigSection` bên trong `ReminderTimeConfigSection` và bên trong `ReminderTriggerSection`.
  2. **Refactor `ReminderTriggerSection` & `ReminderTimeConfigSection`:**
     - Loại bỏ toàn bộ các tham số repeat thừa khỏi `ReminderTriggerSection` và `ReminderTimeConfigSection`.
     - `ReminderTimeConfigSection` chỉ còn nhiệm vụ quản lý danh sách mốc giờ cố định trong ngày (`fixedTimes`).
     - `ReminderTriggerSection` chỉ còn quản lý chuyển đổi giữa Fixed Time và Interval (chu kỳ phút + khung giờ hoạt động `TimeWindow`).
  3. **Làm sạch `ReminderTimeWindowConfigSection`:**
     - Loại bỏ hoàn toàn UI chọn thứ/ngày (`activeDays`), Segmented Button "Tất cả các ngày / Ngày đã chọn" và FlowRow thứ trong tuần.
     - Mỗi khung giờ chỉ đơn giản quản lý khoảng thời gian trong ngày: Giờ bắt đầu (`startTime`) và Giờ kết thúc (`endTime`).
  4. **Dọn dẹp code rác:**
     - Xóa bỏ file dead code `ReminderTimeWindowDialog.kt`.
     - Dọn dẹp các chuỗi tài nguyên không còn sử dụng (`time_window_all_days`, `time_window_selected_days`, `time_window_active_days`, `clear_time_window`, `configure_time_window`) trong `strings.xml` và `values-vi/strings.xml`.
     - Đồng bộ lại `ReminderTriggerEditScreen.kt`.

### [TDR-090] - Nâng Cấp UX Thẻ Nhắc Nhở (ReminderCard): Trực Quan Hóa Đầy Đủ Thông Tin Lặp Lại, Khung Giờ Hoạt Động & Đếm Ngược Lần Nhắc Tiếp Theo
- **Ngày thực hiện:** 2026-10-02
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `ReminderCard` trên danh sách nhắc nhở trước đây chỉ hiển thị mốc giờ/phút lặp cơ bản và tiêu đề.
  - Người dùng khó nắm bắt nhanh:
    + Chu kỳ ngày lặp lại cụ thể (Hàng ngày, T2-T6, T7-CN, ngày trong tháng, hay ngày cụ thể trong năm).
    + Khung thời gian hoạt động áp dụng (`timeWindows`) đối với các nhắc nhở lặp định kỳ (Interval).
    + Thời điểm và khoảng thời gian còn lại đến lượt kích hoạt tiếp theo ("Lần tới sau bao lâu", "Đang hoãn đến mấy giờ", "Đã tắt").
  - Cần nâng cấp giao diện `ReminderCard` theo tiêu chí "Đầy đủ thông tin nhưng gọn gàng, thoáng đãng và có trật tự thị giác cao".
- **Quyết định:**
  1. **Tách biệt và cấu trúc lại các lớp thông tin trên `ReminderCard`:**
     - **Hàng Header / Meta Badges (`FlowRow`):**
       - Badge chính: Kết hợp icon + mốc giờ/tần suất interval + chu kỳ lặp lại (ví dụ: `08:00, 14:00 • T2 - T6` hoặc `Mỗi 2 giờ • Hàng ngày`).
       - Time Window Badge: Hiển thị khung giờ áp dụng nếu có (ví dụ: `08:00 - 17:30`, `08:00 - 11:30, 13:30 - 17:30`).
       - Snoozing Badge: Hiển thị nổi bật khi đang ở trạng thái hoãn (`Đang hoãn (10:35)`).
       - Badge mức độ khẩn cấp (`Khẩn cấp`) và chuông liên tục (`Chuông liên tục`) khi được cấu hình.
     - **Thân thẻ (Body):** Tiêu đề (SemiBold) và tóm tắt ghi chú (nếu có, tối đa 2 dòng).
     - **Hàng Footer đếm ngược lần nhắc kế tiếp (`NextTriggerFooter`):**
       - Tự động tính toán mốc kích hoạt kế tiếp từ `TimeTrigger.getNextOccurrence` hoặc `snoozeNextTriggerTime`.
       - Hiển thị trực quan:
         + Hôm nay: `Lần tới: Hôm nay 14:00 (sau 2 giờ 15 phút)`
         + Ngày mai: `Lần tới: Ngày mai 08:00 (sau 14 giờ)`
         + Ngày trong tuần tới: `Lần tới: T6, 24/10 lúc 09:00 (sau 3 ngày)`
         + Đang hoãn: `Báo lại: 10:35 (còn 12 phút)`
         + Đã tắt: `Đã tắt` (với icon `NotificationsPaused` và màu muted)
         + Đã hoàn tất các lượt: `Đã hoàn thành các lượt nhắc`
  2. **Tạo bộ công cụ định dạng chuyên dụng `ReminderCardFormatter`:**
     - Độc lập, thuần khiết (pure functions) hỗ trợ đa ngôn ngữ (Tiếng Việt & Tiếng Anh).
     - Viết bộ Unit Test toàn diện `ReminderCardFormatterTest` bao phủ 100% các kịch bản định dạng thời gian, chu kỳ và đếm ngược.

### [TDR-089] - Tối Ưu UX Biên Tập Nhắc Nhở: Đưa Cụm Điều Kiện Kích Hoạt Ra Trực Tiếp Màn Hình Biên Tập, Chuẩn Hóa Nhãn Tab Selector & Hỗ Trợ Đầy Đủ Chu Kỳ Lặp Cho Interval
- **Ngày thực hiện:** 2026-10-01
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trước đây, `ReminderEditScreen` chỉ hiển thị tóm tắt điều kiện kích hoạt và người dùng phải nhấn "Chỉnh sửa" để mở một màn hình phụ riêng biệt (`ReminderTriggerEditScreen`).
  - Trong `ReminderTriggerTypeSelector`, cả 2 tab `FIXED_TIME` và `INTERVAL` đều hiển thị chung tiêu đề "Thời gian" (`R.string.trigger_time`) và cùng một icon `ReMindIcons.AccessTime`, gây nhầm lẫn thị giác.
  - Bên cạnh đó, tab `INTERVAL` (Lặp định kỳ) trước đây chưa có thiết lập chu kỳ lặp lại đa dạng như bên `FIXED_TIME` (Một lần, Hàng tuần, Hàng tháng, Hàng năm), khiến người dùng không thể cấu hình các thói quen lặp lại theo chu kỳ ngày cụ thể (ví dụ: uống nước định kỳ chỉ vào các ngày đi làm thứ 2, 4, 6 hoặc chỉ vào ngày cụ thể trong tháng).
  - Hiện tại hệ thống hỗ trợ 2 loại điều kiện kích hoạt thời gian (`FIXED_TIME` và `INTERVAL`), phạm vi cấu hình chưa quá phức tạp (chưa có điều kiện ngoại vi phức tạp như Geofence/Wi-Fi).
  - Việc tách ra màn hình phụ tạo thêm một bước điều hướng dư thừa, làm gián đoạn trải nghiệm người dùng khi tạo hoặc sửa nhắc nhở.
- **Quyết định:**
  1. **Chuẩn hóa nhãn & icon của bộ chọn Tab (`ReminderTriggerTypeSelector`):**
     - `FIXED_TIME` -> Tiêu đề "Mốc thời gian" (`R.string.trigger_fixed_time` / "Fixed Time"), icon `ReMindIcons.AccessTime`.
     - `INTERVAL` -> Tiêu đề "Lặp định kỳ" (`R.string.trigger_interval` / "Interval"), icon `ReMindIcons.Timer`.
  2. **Đồng bộ toàn diện cấu hình chu kỳ lặp (`RepeatMode`) cho Interval:**
     - Tích hợp đầy đủ `ReminderRepeatConfigSection` vào tab `INTERVAL` (hỗ trợ `RepeatMode.NONE` với chọn ngày cụ thể `exactDate`, `RepeatMode.DAILY`, `RepeatMode.WEEKLY` với chọn thứ trong tuần, `RepeatMode.MONTHLY` với lưới ngày 1..31, và `RepeatMode.YEARLY` với danh sách ngày tháng).
     - Nâng cấp `TimeTrigger.kt`: Bổ sung `isDateValid(date: LocalDate)` và `getNextValidDate(from: LocalDate): LocalDate?` để hỗ trợ tính toán ngày hợp lệ theo chu kỳ cho cả Fixed Time và Interval.
     - Thuật toán `getNextIntervalOccurrence()` kiểm tra ngày hợp lệ theo `repeatMode` trước khi tính toán các mốc khoảng cách (interval) và khung giờ hoạt động (timeWindows).
  3. **Đưa toàn bộ cụm điều khiển kích hoạt (`ReminderTriggerSection`) ra ngoài màn hình `ReminderEditScreen`:**
     - Tích hợp trực tiếp `ReminderTriggerSection` vào danh sách cuộn của `ReminderEditScreen`, cho phép người dùng thay đổi tab, chọn giờ/ngày lặp lại, hoặc cài đặt phút lặp/khung giờ áp dụng ngay trong một màn hình duy nhất.
     - Giữ nguyên kiến trúc module và luồng dữ liệu chuẩn `ReminderEditViewModel` với đầy đủ các tương tác thời gian thực.
  4. **Tối ưu hóa và tái sử dụng:**
     - `ReminderTriggerEditScreen` tái sử dụng trực tiếp component `ReminderTriggerSection`, đảm bảo không trùng lặp code giao diện.
     - Khi tương lai cần mở rộng thêm các điều kiện kích hoạt phức tạp (như vị trí địa lý, mạng Wi-Fi, Bluetooth), hệ thống có thể linh hoạt đưa lại màn hình chuyên biệt mà không ảnh hưởng tới kiến trúc hiện tại.

### [TDR-088] - Tái Cấu Trúc Vòng Đời Reminder: Loại Bỏ `isCompleted`, Thống Nhất Trạng Thái Qua `isEnabled` và Tinh Gọn UX Danh Sách
- **Ngày thực hiện:** 2026-10-01
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trước đây, `Reminder` tồn tại đồng thời 2 trường trạng thái: `isEnabled: Boolean` và `isCompleted: Boolean`.
  - Phân tích nghiệp vụ chỉ ra sự dư thừa và xung đột logic:
    + Với nhắc nhở 1 lần (`!isRecurring`): Khi chuông kêu xong hoặc người dùng dismiss, nhắc nhở coi như xong việc. Cả `isCompleted = true` và `isEnabled = false` đều biểu diễn cùng một trạng thái ngừng kích hoạt.
    + Với nhắc nhở lặp lại (`isRecurring` / `INTERVAL`): Việc đánh dấu `isCompleted = true` trước đây khiến bộ lập lịch (`ReminderSchedulerImpl`) bỏ qua không lập lịch cho chu kỳ tiếp theo (`if (!reminder.isEnabled || reminder.isCompleted) return`).
    + Sự nhập nhằng này dẫn đến việc phải duy trì logic reset chu kỳ phức tạp, tạo bug tiềm ẩn.
  - Về mặt UI/UX:
    + Khác với Báo thức (người dùng bật/tắt liên tục hàng ngày/cuối tuần), Nhắc nhở là nhiệm vụ/thói quen có tần suất tắt rất thấp.
    + Việc đặt `LiteverSwitch` ngay trên từng item danh sách (`ReminderCard`) dễ gây chạm nhầm (accidental click) và làm giao diện bị rối mắt.
- **Quyết định:**
  1. **Loại bỏ hoàn toàn trường `isCompleted` khỏi toàn bộ hệ thống:**
     - Domain Model: Xóa `isCompleted` trong `Reminder.kt`.
     - Database & Migration: Xóa `isCompleted` trong `ReminderEntity.kt`, `ReminderDao.kt`. Tăng Room database version từ 7 lên 8 với `MIGRATION_7_8` (tái tạo bảng an toàn bằng SQLite table recreation theo đúng chuẩn Room).
     - Data Mapping: Xóa bỏ `isCompleted` trong `ReminderMapper.kt`.
     - Logic & Scheduler: Cập nhật `ReminderSchedulerImpl.kt`, `ReminderTriggerEvaluator.kt`, `ReminderSyncManager.kt`, `ReminderReceiver.kt`, `ReminderAlertManager.kt`, `ReminderRingingViewModel.kt`.
       * Nhắc nhở 1 lần: Khi kết thúc -> cập nhật `isEnabled = false`.
       * Nhắc nhở lặp lại: Giữ nguyên `isEnabled = true`, chỉ tịnh tiến/cập nhật `lastTriggeredTime` hoặc `snoozeNextTriggerTime`.
  2. **Tinh gọn giao diện và hành vi UI:**
     - `ReminderCard.kt`: Gỡ bỏ `LiteverSwitch`, loại bỏ `onToggle`. Hiển thị làm mờ màu (alpha 0.6f / onSurface) khi `!isEnabled`. Nút menu 3 chấm được căn phải gọn gàng, thoáng đãng.
     - `ReminderActionBottomSheet.kt`: Thay thế tùy chọn "Đánh dấu hoàn thành" bằng "Tắt nhắc nhở" / "Bật nhắc nhở" (`onToggleEnabledClick`).
     - `ReminderEditScreen.kt`: Bổ sung nút bấm dạng Outlined/Primary ở cuối màn hình chỉnh sửa ("Tắt nhắc nhở" / "Bật nhắc nhở") cho các nhắc nhở đã tồn tại (`uiState.id != 0L`).
     - `ReminderListViewModel.kt`: Xóa bỏ logic lọc theo `isCompleted`, xóa `deleteCompletedReminders()`, `toggleCompleted()`.
  3. **Kiểm thử & Đảm bảo chất lượng:**
     - Cập nhật 100% test suites: `ReminderTest`, `ReminderSnoozeChallengerTest`, `ReminderDaoTest`, `ReminderDatabaseChallengerTest`, `ReminderRepositoryImplTest`, `AlarmReminderConcurrencyTest`, `ReminderSyncManagerTest`, `ReminderReceiverTest`, `ReminderSchedulerAndServiceTest`, `ReminderSchedulerImplTest`, `ReminderTriggerEvaluatorTest`, `ReminderListViewModelTest`, `ReminderAdversarialReviewTest`, `ReminderPermissionChallengerTest`, `ReminderR1R2EmpiricalChallengerTest`, `ReminderRingingViewModelTest`, `ScreenshotsTest`.
     - Chạy `./gradlew testDebugUnitTest` vượt qua 100% tests.

### [TDR-087] - Thống nhất PermissionWarningBanner vào :core:designsystem và Cập nhật Tính năng Reminders với Lời văn Chuyên biệt
- **Ngày thực hiện:** 2026-10-01
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Cả hai tính năng Báo thức (`:features:alarms`) và Nhắc nhở (`:features:reminders`) đều cần cảnh báo quyền hệ thống bị thiếu cho người dùng, nhưng trước đó mỗi module lại có banner riêng (`AlarmPermissionWarningBanner` và `ReminderPermissionWarningBanner`), gây phân mảnh và không đồng bộ về giao diện Design System.
  - Cần phân cấp mức độ cảnh báo rõ ràng:
    + **Critical (Lỗi cốt lõi)**: Thiếu quyền thông báo hoặc quyền báo thức chính xác cho tác vụ đang kích hoạt -> Hiển thị cảnh báo lỗi (đỏ/errorContainer) và bắt buộc người dùng sửa để hoạt động, không cho ẩn.
    + **Non-critical (Cảnh báo khuyến nghị)**: Thiếu các quyền tối ưu như Tối ưu hóa pin, Vẽ trên ứng dụng khác -> Hiển thị dạng cảnh báo (vàng/warningContainer) kèm nút "Nhắc tôi sau" để ẩn tạm thời.
  - Lời văn trong tính năng Nhắc nhở phải phù hợp với bối cảnh nhắc việc/thông báo ("Nhắc nhở"), không được dùng ngôn từ của "Báo thức".
- **Quyết định:**
  1. **Chuyển `PermissionWarningBanner` vào `:core:designsystem`:**
     - Tạo `vn.io.litever.remind.core.designsystem.components.PermissionWarningBanner.kt`.
     - Nhận các thuộc tính ngữ nghĩa (`isCritical`, `title`, `message`, `actionLabel`, `dismissLabel`, `onDismiss`).
     - Tuân thủ nghiêm ngặt quy chuẩn của Litever Design System (`LvButton`, `LvSemantic`, `LiteverTheme`).
     - Xóa bỏ hoàn toàn các file banner cũ trong `:features:alarms` và `:features:reminders`.
  2. **Cập nhật `:features:reminders`:**
     - Bổ sung chuỗi tài nguyên tiếng Việt và tiếng Anh dành riêng cho tính năng Nhắc nhở (`reminder_permission_warning_banner_non_critical_title`, `reminder_permission_warning_banner_non_critical_desc`).
     - Mở rộng `ReminderListUiState` với `isCriticalPermissionWarning` và `isPermissionWarningDismissed`.
     - Cung cấp hàm `dismissPermissionWarning()` trong `ReminderListViewModel` và liên kết với nút "Nhắc tôi sau".
  3. **Kiểm thử toàn diện:**
     - Cập nhật test suite `ReminderListViewModelTest`, `ReminderPermissionChallengerTest`, `ReminderR1R2EmpiricalChallengerTest`.
     - Build và chạy thành công toàn bộ test suite và `assembleDebug`.

### [TDR-086] - Thống nhất AutoSilenceBottomSheet vào :core:designsystem và Dọn dẹp Code Smells / Linter Warnings tại ReminderEditScreen & Navigation
- **Ngày thực hiện:** 2026-10-01
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `AutoSilenceBottomSheet` bên Báo thức (`:features:alarms`) và `ReminderAutoSilenceBottomSheetContent` bên Nhắc nhở (`:features:reminders`) phục vụ cùng một mục đích chọn thời gian tự động ngắt chuông khi người dùng không tương tác. Việc duplicate component này vi phạm nguyên tắc DRY và Single Source of Truth của `:core:designsystem`.
  - Chuỗi mô tả hành vi cần giải thích rõ ràng cho cả báo thức và nhắc nhở: "Chuông báo sẽ tự động tắt nếu bạn không tương tác sau một khoảng thời gian."
  - Màn hình `ReminderEditScreen` và file điều hướng `RemindersNavigation.kt` tồn tại nhiều code smell và cảnh báo linter:
    + Cảnh báo `Querying resource values using LocalContext.current is not configuration-aware and may return stale values if the Configuration changes` (3 vị trí).
    + Sử dụng `Uri.parse(string)` thay vì extension `toUri()`.
    + Vị trí `modifier: Modifier = Modifier` không phải là tham số tùy chọn đầu tiên (vi phạm Compose API guidelines).
    + 9 tham số callback trigger thừa trong `ReminderEditScreen` do việc cấu hình trigger đã tách sang `ReminderTriggerEditScreen`.
    + Tham số `reminderId` và `onNavigateToSnoozeSettings` không còn được sử dụng trong `ReminderEditRoute` và `remindersGraph`.
- **Quyết định:**
  1. **Đưa `AutoSilenceBottomSheet` và `AutoSilenceBottomSheetContent` vào `:core:designsystem`:**
     - Tạo `vn.io.litever.remind.core.designsystem.components.AutoSilenceBottomSheet.kt`.
     - Hỗ trợ linh hoạt danh sách tùy chọn (hỗ trợ `0` tương ứng "Không bao giờ" qua `auto_silence_never`).
     - Bổ sung resource strings chuẩn hóa trong cả `values/strings.xml` và `values-vi/strings.xml`.
     - Xóa bỏ hoàn toàn implementation trùng lặp bên `:features:alarms` và `ReminderAutoSilenceBottomSheetContent` trong `ReminderAlertConfigSection.kt`.
  2. **Dọn dẹp triệt để Code Smells & Linter trong `:features:reminders`:**
     - Sử dụng `stringResource(R.string.default_ringtone)` thay cho `context.getString(...)` đảm bảo configuration-aware khi đổi locale / theme.
     - Sử dụng `uiState.ringtoneUri.toUri()`.
     - Chuẩn hóa vị trí `modifier: Modifier = Modifier` thành tham số tùy chọn đầu tiên theo Compose API Guidelines.
     - Loại bỏ hoàn toàn 9 callbacks thừa (`onTriggerTypeSelected`, `onTimeChange`, `onDateChange`, `onRepeatDaysChange`, `onRepeatDayToggle`, `onRepeatIntervalChange`, `onMonthlyDayToggle`, `onAddYearlyDate`, `onRemoveYearlyDate`) khỏi `ReminderEditScreen`, `ReminderEditRoute` và các previews.
     - Loại bỏ `reminderId` và `onNavigateToSnoozeSettings` thừa trong `ReminderEditRoute` và `remindersGraph`.
     - Xóa các unused imports.
  3. **Kiểm thử tự động:**
     - 473/473 unit test tasks toàn bộ hệ thống passed 100%.

### [TDR-070] - Tách biệt thanh Âm lượng và Rung thành 2 thành phần độc lập kèm nút phát thử âm thanh (Play/Stop Preview) cho Alarm và Reminder
- **Ngày thực hiện:** 2026-10-01
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trước đây, ở cả `AlarmEditScreen` và `ReminderEditScreen` (`ReminderAlertConfigSection`), thanh trượt âm lượng và icon rung bị gộp chung vào một hàng (`[VolumeIcon] [Slider] [VibrationIcon]`). Bố cục này gây chật chội, khó thao tác và không tạo sự trực quan cho người dùng khi muốn bật/tắt rung độc lập.
  - Đồng thời, người dùng cần có khả năng nghe thử trực tiếp âm thanh chuông ở mức âm lượng vừa chọn ngay tại thanh âm lượng.
- **Quyết định:**
  1. **Tách riêng 2 hàng cài đặt:**
     - **Hàng Âm lượng:** Gồm icon âm lượng (`VolumeOff`/`VolumeUp`), thanh trượt `Slider`, và ở cuối hàng là nút phát thử âm thanh dạng icon button (`ReMindSettingIcon`) với icon `PlayArrow` (khi dừng) và `Pause`/`Stop` (khi đang phát có highlight màu container).
     - **Hàng Rung:** Trở thành một hàng cài đặt riêng biệt, có tiêu đề, mô tả trạng thái, và công tắc `Switch`/`LiteverSwitch` rõ ràng, tiện lợi.
  2. **Áp dụng đồng bộ cho cả 2 module `:features:alarms` và `:features:reminders`:**
     - Trong `AlarmEditScreen`: Tách Row 3 thành Volume Slider + Play button ở đuôi, Row 4 thành `AlarmSettingRow` cho Rung.
     - Trong `ReminderAlertConfigSection`: Chế độ đổ chuông hiển thị hàng Volume Slider + Play button riêng, và cả 2 chế độ (Đổ chuông & Thông báo thường) đều hiển thị hàng Rung riêng biệt, nhất quán.
  3. **Tích hợp `AudioPlayer` vào `ReminderEditViewModel`:**
     - Inject `AudioPlayer` từ `:core:common` vào `ReminderEditViewModel`.
     - Cung cấp `toggleRingtonePlayback()`, `startRingtonePlayback()`, `stopRingtonePlayback()`.
     - Tự động ngắt phát âm thanh khi đổi chuông, đổi rung, đổi kiểu thông báo, lưu, hủy, hoặc rời khỏi màn hình (`onCleared`, `onDispose`).
  4. **Kiểm thử tự động:**
     - Viết đầy đủ unit tests cho `ReminderEditViewModelTest` đảm bảo logic bật/tắt phát chuông và ngắt chuông an toàn.
     - Toàn bộ 473/473 tasks unit tests của project pass 100%.
- **Hệ quả:**
  - Giao diện biên tập báo thức và nhắc nhở trở nên thoáng đãng, dễ kiểm soát âm lượng và rung độc lập.
  - Người dùng dễ dàng nghe thử âm thanh chuông trực tiếp ngay tại vị trí điều chỉnh âm lượng.

### [TDR-069] - Tái cấu trúc CSDL nhắc nhở (Reminders): Tách bạch FIXED_TIME & INTERVAL, hỗ trợ Multi-day Recurrence và đồng bộ Room Architecture
- **Ngày thực hiện:** 2026-09-29
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Sau commit `82167122df0a9b3c4864400cfde2142852b981e2`, cấu trúc dữ liệu reminder đã loại bỏ các trigger Location/Wifi để tập trung vào TimeTrigger.
  - Tuy nhiên, việc gộp chung các loại kích hoạt và thiếu sự phân tách giữa mốc giờ tĩnh (`FIXED_TIME`) và khoảng lặp động (`INTERVAL`) gây xung đột logic nghiệp vụ: `FIXED_TIME` là mốc giờ cố định (không bị trôi giờ nếu lỡ hẹn), trong khi `INTERVAL` tính khoảng thời gian trôi qua giữa các lần thực hiện và chỉ kích hoạt trong các khung giờ giới hạn (`time_windows`).
  - Nhu cầu lặp lại định kỳ phức tạp: Lặp theo tháng (cho phép chọn nhiều ngày trong tháng `monthlyDays: List<Int>`), lặp theo năm (cho phép chọn nhiều ngày trong năm `yearlyDates: List<ReminderMonthDay>`), lặp theo tuần (`repeatDays: List<DayOfWeek>`).
- **Quyết định:**
  1. **Chuẩn hoá Schema Room Database & Entity (`reminders` table):**
     - Bổ sung `trigger_type` (`FIXED_TIME` | `INTERVAL`).
     - Tách trường `fixed_times: List<String>?` (lưu các mốc giờ cố định dạng HH:mm) và `interval_minutes: Int?` (chu kỳ lặp lại theo phút).
     - Bổ sung `time_windows: List<TimeWindow>?` quy định khung giờ hiệu lực (`startTime`, `endTime`, `activeDays`) cho `INTERVAL`.
     - Hỗ trợ đa ngày lặp lại: `repeat_values: List<String>?` lưu trữ `monthlyDays` (ví dụ `["1", "15", "30"]`) và `yearlyDates` (ví dụ `["MM-dd", ...]`), kết hợp `repeat_days` cho WEEKLY.
     - Đồng bộ các thuộc tính ràng buộc và hành vi: `urgency_level` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), `checklist` (danh sách công việc con), `snooze_config` (`intervalMinutes`, `maxCount`), `is_persistent_alert`.
  2. **Nâng cấp Migration Room DB (`MIGRATION_4_5`):**
     - Đảm bảo migration an toàn, tự động migrate dữ liệu cũ từ `time_trigger_times` và `time_trigger_repeat_interval` sang `fixed_times` / `repeat_values` mà không làm mất mát dữ liệu người dùng.
  3. **Đồng bộ Domain & UI/ViewModel Layer:**
     - Tối ưu `TimeTrigger` với các helper `isRecurring`, hỗ trợ đầy đủ `monthlyDays`, `yearlyDates`.
     - Cập nhật `ReminderEditViewModel` và `ReminderTriggerEditScreen`: Hỗ trợ giao diện chọn nhiều ngày trong tháng và nhiều ngày trong năm linh hoạt.
     - **UI Multi-Time Targets cho `FIXED_TIME`**: Bổ sung bộ chọn nhiều mốc giờ tĩnh trong ngày (`fixedTimes: List<LocalTime>`), hỗ trợ thêm/xóa/sửa từng mốc giờ dạng chip trực quan, đồng bộ lưu trữ vào `TimeTrigger.time`.
     - **Nâng cấp `ReminderCard`**: Tách bạch hiển thị `FIXED_TIME` (tóm tắt các mốc giờ và chu kỳ) và `INTERVAL` (chu kỳ lặp + khung giờ hiệu lực), bổ sung Urgency badge (`HARD`) và Persistent Alert badge ("Chuông liên tục").
     - Cập nhật `ReminderSyncManager` và `ReminderAlertManager`: Xử lý mượt mà khi app mở hoặc alarm kích hoạt.
  4. **Bảo trì & Tinh giản Test Suite:**
     - Xoá bỏ các unit test mồ côi phụ thuộc Location/Wifi cũ trong `:app` và `:core:alarm`.
     - Cập nhật dependency `libs.junit` cho `:features:today` và `:features:mission`.
     - Đưa toàn bộ 475 test task của project vượt qua 100% không có cảnh báo hay lỗi tiềm ẩn.
- **Hệ quả:**
  - Hệ thống dữ liệu nhắc nhở đạt chuẩn kiến trúc sạch (Clean Architecture), tách bạch rõ ràng giữa thời gian tĩnh và chu kỳ động.
  - Hỗ trợ toàn diện các ca sử dụng phức tạp của người dùng (uống nước mỗi 2 tiếng trong giờ làm việc, nhắc đóng tiền nhà ngày 5 và 20 hàng tháng, nhắc sinh nhật nhiều người thân trong năm).
  - Toàn bộ test suite xanh 100%, sẵn sàng cho release.

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
     - `ColorPaletteSelectionDialog`: Thiết kế dạng lưới tròn **3 cột x 3 hàng** (Pixel/Material 3 style) cực kỳ nhỏ gọn, gồm chấm màu tròn 48dp, viền nổi bật (2.5dp) & dấu tích `Check` khi được chọn, cùng tên màu ngắn gọn phía dưới. Giảm 60% chiều cao so với danh sách dọc, không còn chiếm diện tích màn hình.
     - Hỗ trợ nút Cancel (Huỷ) chuẩn thiết kế qua `LvButton`.
- **Hệ quả:**
  - Giao diện cài đặt chung đồng bộ 100%, thanh thoát và tinh tế với 2 nhóm rõ ràng: **Display** (Giao diện & Màu sắc) và **Language & Region** (Ngôn ngữ & Định dạng vùng miền).
  - Dialog chọn màu nhỏ gọn, trực quan, thẩm mỹ cao và thao tác chạm nhanh chóng.
  - Đồng bộ 100% với phong cách toàn bộ ứng dụng (`SettingsScreen`, `AlarmSettingsScreen`).

### [TDR-068] - Đồng bộ LocalConfiguration uiMode & Phân giải ảnh minh hoạ EmptyState theo Theme
- **Ngày thực hiện:** 2026-09-17
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `EmptyState` (`AlarmListScreen`) sử dụng tài nguyên ảnh minh hoạ (`no_alarm_illustration.png`).
  - Khi người dùng thay đổi chế độ hiển thị (Sáng / Tối) trong cài đặt ứng dụng hoặc xem trong Preview Dark Mode, `painterResource` không tự động đổi sang bản Dark nếu OS vẫn đang ở chế độ Light, vì Android `ResourcesImpl` cache và gắn chặt với Context/Configuration của Activity hệ thống.
  - Việc can thiệp override `LocalContext provides themedContext` bằng non-Activity `createConfigurationContext(...)` gây crash runtime layout (`ScaffoldLayout`) do Jetpack Compose yêu cầu Context của Activity.
- **Quyết định:**
  1. **Tuyệt đối không override `LocalContext`:** Chỉ đồng bộ `LocalConfiguration provides themedConfiguration` với cờ `uiMode` (`UI_MODE_NIGHT_YES` / `UI_MODE_NIGHT_NO`) bên trong `ReMindTheme`. Giữ nguyên Activity Context để tránh crash layout.
  2. **Tách biệt tài nguyên & Chọn ảnh theo Theme state:** Khai báo tài nguyên `no_alarm_illustration_dark.png` và điều hướng trực tiếp trong `EmptyState`: nếu `!LiteverTheme.colors.isLight` thì hiển thị `no_alarm_illustration_dark`, ngược lại hiển thị `no_alarm_illustration`.
  3. **Bổ sung Preview Dark Mode:** Đảm bảo `EmptyStateDarkPreview` hiển thị chuẩn xác cả 2 chế độ Sáng/Tối.
- **Hệ quả:**
  - Ứng dụng hoạt động ổn định 100%, không bị crash runtime tại `MainActivity`.
  - Ảnh minh hoạ `EmptyState` chuyển đổi mượt mà, lập tức tương ứng với Dark Mode hay Light Mode khi người dùng đổi cài đặt trong ứng dụng.

### [TDR-069] - Chuẩn hóa Nội dung Bottom Sheet: ReMindBottomSheetContent & M3 ModalBottomSheet
- **Ngày thực hiện:** 2026-09-18
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Các màn hình và thành phần (`SnoozeBottomSheet`, `GentleAlarmBottomSheet`, `AutoSilenceBottomSheet`, `MissionSelectionBottomSheet`, `AlarmActionBottomSheet`, `AddCustomPhraseContent`) đang tự cấu hình header và title thủ công, dẫn đến lặp lại logic padding, kiểu chữ title và khoảng cách thừa khi không có title.
  - Việc bọc toàn bộ `ModalBottomSheet` thành wrapper trung gian gây cồng kềnh, che giấu các tính năng gốc của M3 và trái với triết lý V2 Lean Architecture của Litever Design System.
- **Quyết định:**
  1. **Không tạo wrapper cho `ModalBottomSheet`:** Sử dụng trực tiếp `ModalBottomSheet` gốc của Material 3 kèm `containerColor = LiteverTheme.colors.surface` nhằm giữ trọn vẹn khả năng tùy biến của Material 3.
  2. **Tạo `ReMindBottomSheetContent` tại `:core:designsystem`:**
     - Đóng gói logic hiển thị `title` (với typography chuẩn `titleLarge`, in đậm và padding hợp lý) và lồng `content` bên dưới.
     - Khi `title == null`, `content` bắt đầu ngay sát phần trên, không sinh padding/spacing thừa.
     - Hỗ trợ cả slot composable `title: (@Composable () -> Unit)?` và overload chuỗi `title: String?`.
  3. **Chuyển đổi toàn bộ Bottom Sheet hiện có trong ứng dụng:**
     - `SnoozeBottomSheet.kt`
     - `GentleAlarmBottomSheet.kt`
     - `AutoSilenceBottomSheet.kt`
     - `MissionSelectionBottomSheet.kt`
     - `AlarmActionBottomSheet` trong `AlarmListScreen.kt`
     - Bottom sheet thêm cụm từ trong `PhraseSelectionScreen.kt`
- **Hệ quả:**
  - Đồng bộ 100% trải nghiệm và kiểu dáng Header Bottom Sheet trên toàn bộ ứng dụng.
  - Tối giản mã nguồn, không sinh lớp wrapper thừa, tuân thủ nguyên tắc Lean Architecture.

### [TDR-070] - Bổ sung mô tả AutoSilence & Thẻ tóm tắt báo thức tại AlarmActionBottomSheet
- **Ngày thực hiện:** 2026-09-18
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Bottom sheet `AutoSilenceBottomSheet` cần có một dòng mô tả ngắn giải thích hành vi tắt tiếng/báo lại tự động tương tự `GentleAlarmBottomSheet` để người dùng hiểu rõ tác dụng.
  - `AlarmActionBottomSheet` mở lên từ danh sách báo thức hiện chỉ hiển thị các hành động (Bỏ qua lần này, Xem trước, Nhân bản, Xoá) mà không hiển thị thông tin báo thức đang được tác vụ, gây thiếu ngữ cảnh nhận biết cho người dùng.
- **Quyết định:**
  1. **Bổ sung mô tả cho AutoSilence:**
     - Thêm chuỗi `auto_silence_description` đa ngôn ngữ (Tiếng Anh & Tiếng Việt).
     - Đặt dòng mô tả ngắn phía dưới tiêu đề của `AutoSilenceBottomSheetContent`.
  2. **Hiển thị thẻ tóm tắt báo thức trong `AlarmActionBottomSheet`:**
     - Truyền `is24HourFormat` từ `AlarmListScreen` vào `AlarmActionBottomSheet` và `AlarmActionBottomSheetContent`.
     - Đặt một `ReMindGroupCard` ở đầu danh sách thao tác: hiển thị chu kỳ lặp (`getRepeatSummaryText`), giờ báo thức + AM/PM (`TimeFormatUtils.formatTimeParts`), biểu tượng nhiệm vụ (`MissionIcons`) và nhãn báo thức (`alarm.label`).
     - Tái sử dụng hàm nội bộ `MissionIcons` và `getMissionIcon` từ `AlarmCard.kt`.
- **Hệ quả:**
  - Trải nghiệm người dùng đồng nhất, trực quan và rõ ràng hơn khi thực hiện thao tác nhanh trên từng báo thức.
  - Tận dụng tối đa các thành phần và quy chuẩn có sẵn trong `:core:designsystem`.

### [TDR-071] - Tinh chỉnh giao diện AlarmCard & Thẻ NextAlarmHeader
- **Ngày thực hiện:** 2026-09-18
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `AlarmCard`: Card báo thức cần tăng tính thẩm mỹ và độ tương phản với nền theo đúng các token phân cấp màu (`surfaceContainer` / `surfaceContainerLow`), dùng `Surface` đồng bộ và căn chỉnh font chữ giờ, AM/PM sắc nét hơn.
  - `NextAlarmHeader`: Phần hiển thị báo thức sắp tới đang là một Row đơn giản, chưa nổi bật so với danh sách bên dưới.
- **Quyết định:**
  1. **Nâng cấp `AlarmCard`:**
     - Sử dụng `Surface` với `shape = LiteverTheme.shapes.large` thay cho `Card`.
     - Màu nền phân cấp: Báo thức bật dùng `LiteverTheme.colors.surfaceContainer`, báo thức tắt dùng `LiteverTheme.colors.surfaceContainerLow`.
     - Chữ giờ định dạng `FontWeight.Bold`, AM/PM in hoa với `FontWeight.Bold` và màu `onSurface`.
     - Nhãn báo thức bổ sung màu `onSurfaceVariant`.
  2. **Nâng cấp `NextAlarmHeader` & `ReMindBottomBar`:**
     - `NextAlarmHeader`: Bọc toàn bộ nội dung trong một `Surface` bo góc `LiteverTheme.shapes.large`, màu nền `tertiaryContainer`, `contentColor = onTertiaryContainer` và padding `LiteverTheme.spacing.medium`. Giúp khối thông báo thời gian chuông sắp reo trở nên nổi bật, thanh lịch và tạo điểm nhấn trực quan ở đầu màn hình.
     - `ReMindBottomBar`: Nâng cấp màu nền sang `LiteverTheme.colors.surfaceContainerHigh` nhằm tăng độ tách biệt và tương phản rõ nét so với nội dung cuộn bên dưới.
- **Hệ quả:**
  - Giao diện danh sách báo thức và bottom bar hiện đại, rõ ràng, độ tương phản và trật tự thị giác (visual hierarchy) tốt hơn.

### [TDR-072] - Tự động xuống dòng (FlowRow) cho Button trong LvAlertDialog khi Text dài
- **Ngày thực hiện:** 2026-09-18
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trong `LvAlertDialog`, các nút tác vụ (dismissButton và confirmButton) trước đây được đặt trong một `Row` với `Alignment.End`. Khi tiêu đề của các button quá dài hoặc không đủ diện tích hiển thị trên cùng một hàng ngang, text có thể bị ép nhỏ hoặc gây tràn khung, vỡ bố cục dialog.
- **Quyết định:**
  - Chuyển đổi vùng chứa action buttons từ `Row` sang `FlowRow` với khoảng cách `horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small, Alignment.End)` và `verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)`.
  - Giữ thứ tự hiển thị: dismissButton đi trước, confirmButton đi sau. Khi không đủ không gian ngang, confirmButton hoặc các nút sẽ tự động ngắt và nhảy xuống dòng kế tiếp một cách mượt mà và tự nhiên.
  - Bổ sung Preview `LvAlertDialogLongButtonsPreview` để kiểm thử trường hợp text button dài.
- **Hệ quả:**
  - Đảm bảo Dialog luôn hiển thị trọn vẹn nhãn nút bấm mà không bị tràn khung hay cắt bớt chữ trên mọi kích cỡ màn hình.
  - Tương thích hoàn toàn với các dialog hiện có trong toàn bộ ứng dụng mà không cần thay đổi code ở caller side.

### [TDR-073] - Đồng bộ MissionCompleteContent sử dụng thành phần dùng chung FeedbackStateView
- **Ngày thực hiện:** 2026-09-18
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Màn hình thông báo hoàn thành nhiệm vụ (`MissionCompleteContent`) trước đây tự triển khai một bố cục Column riêng lẻ với Icon CheckCircle và Text tùy biến, chưa tận dụng component `FeedbackStateView` của LiteVer Design System.
- **Quyết định:**
  - Tái cấu trúc `MissionCompleteContent` sang sử dụng trực tiếp `FeedbackStateView` với `type = FeedbackStateType.SUCCESS`.
  - Tận dụng huy hiệu (badge tròn), kiểu màu ngữ nghĩa Success (`successContainer`, `onSuccessContainer`) và spacing chuẩn hóa của Design System.
  - Tùy chỉnh kiểu chữ `displayMedium` in đậm và khoảng cách ký tự `letterSpacing = 4.sp` cùng subtitle `titleMedium` để duy trì sự nổi bật và tạo cảm giác thành tựu khi tắt chuông thành công.
- **Hệ quả:**
  - Tuân thủ nghiêm ngặt nguyên tắc Single Source of Truth cho UI components giữa các module.
  - Giảm thiểu code trùng lặp, đồng bộ hoá nhận diện thương hiệu và trạng thái phản hồi xuyên suốt ứng dụng.
### [TDR-074] - Tái Cấu Trúc Khung Màn Hình Làm Nhiệm Vụ (Mission Ringing & Content Screens) Chuẩn Stitch Design
- **Ngày thực hiện:** 2026-09-21
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Màn hình làm nhiệm vụ giải chuông báo thức cần được nâng cấp visual hierarchy theo thiết kế Stitch Design: hiển thị rõ ràng thông tin chuông, thanh đếm ngược, tiến độ các vòng nhiệm vụ (segmented progress bar), và không gian thao tác sát bàn phím (bottom docked bar).
  - Các màn hình thành phần làm nhiệm vụ (`TypingMissionContent`, `MathMissionContent`, `MemoryTilesMissionContent`) trước đây có nhiều chi tiết vụn vặt (thẻ mẹo thừa thãi, đếm vòng lặp bị lặp lại ở cả trên và dưới).
- **Quyết định:**
  - Khung chính `MissionRingingScreen`:
    - Top App Bar hiển thị tiêu đề thử thách, giờ báo thức, nút bỏ cuộc và badge đếm ngược `mm:ss` nổi bật (chuyển đỏ khi < 10 giây).
    - `MissionProgressScaffold`: Hiển thị tên nhiệm vụ, số vòng và thanh tiến độ phân đoạn `Row` (segmented progress).
    - `MissionBottomDockedBar`: Đặt cố định sát đáy / trên bàn phím ảo (IME padding), chứa thông tin ký tự/số ô và nút "Tiếp tục" chuyển bước.
  - Các màn hình nhiệm vụ (`TypingMissionContent`, `MathMissionContent`, `MemoryTilesMissionContent`):
    - Đưa `MissionInstructionBanner` lên trên cùng: gồm icon ngữ cảnh (`EditNote`, `Calculate`, `GridView`) và dòng yêu cầu ngắn gọn rõ ràng.
    - `TypingMissionContent`: Bảng gõ chữ tương tác hỗ trợ tô màu từng ký tự đúng/sai/gợi ý, ô nhập liệu giới hạn chiều cao tối đa và hỗ trợ cuộn độc lập (`verticalScroll`) khi văn bản dài.
    - `MathMissionContent`: Thẻ phép toán lớn, sắc nét ở giữa kèm ô nhập số ở dưới, loại bỏ các thành phần rườm rà.
    - `MemoryTilesMissionContent`: Hiển thị rõ ràng giai đoạn đếm ngược ghi nhớ hoặc tìm ô kèm lưới ô cờ bo tròn trong `Card`, loại bỏ badge tiến độ trùng lặp. Đồng thời cập nhật callback `onProgressChange` để truyền số ô đã chọn đúng / tổng số ô cần tìm xuống `MissionBottomDockedBar` hiển thị trực tiếp ở chân trang (ví dụ: `2 / 4 ô`).
- **Hệ quả:**
  - Trải nghiệm làm nhiệm vụ trở nên mạch lạc, hiện đại, nhất quán thị giác 100% giữa 3 loại nhiệm vụ.
  - Tương thích tốt với màn hình nhỏ và khi bàn phím ảo bật lên nhờ cơ chế cuộn độc lập và bottom bar docked.

### [TDR-075] - Hiển Thị Trạng Thái Báo Thức Tiếp Theo Tại TodayScreen (Non-interactive & Rich Details)
- **Ngày thực hiện:** 2026-09-21
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `TodayScreen` cần bổ sung một khối thông tin báo thức tiếp theo tương tự như `TodayQuoteView` hoặc banner quảng cáo để người dùng nắm bắt nhanh trạng thái báo thức trong ngày.
  - Yêu cầu đặc thù: Vùng này hoàn toàn tĩnh (non-interactive, không có nút bấm, không click handler). Khi có báo thức kế tiếp thì hiển thị đầy đủ thông tin báo thức (giờ, AM/PM, nhãn, lặp lại, icon nhiệm vụ, badge đếm ngược còn lại) gần giống như `AlarmCard` ở danh sách báo thức. Khi không có báo thức kế tiếp hoặc tất cả tắt thì hiển thị thông điệp truyền cảm hứng ý nghĩa ("Thời gian nghỉ ngơi") kèm ảnh minh hoạ rỗng.
- **Quyết định:**
  - Tách và chuyển `NextAlarmUiState` cùng hàm `calculateNextAlarm` vào `:core:model` để chia sẻ giữa `:features:alarms` và `:features:today` (tuân thủ Clean Architecture và Single Source of Truth), đồng thời bổ sung trường `val alarm: Alarm` vào `NextAlarmUiState.Remaining`.
  - Di chuyển các file ảnh minh họa `no_alarm_illustration.png` và `no_alarm_illustration_dark.png` vào `:core:designsystem/src/main/res/drawable/` để tái sử dụng thống nhất giữa các feature.
  - Nâng cấp `TodayNextAlarmView` trong `:features:today:ui:components`:
    - Đồng bộ định dạng giờ (12h/24h) theo cài đặt hệ thống người dùng thông qua `AlarmPreferencesDataSource` được inject vào `TodayViewModel` và truyền xuống UI.
    - Khi có báo thức sắp tới (`NextAlarmUiState.Remaining`): thiết kế bố cục 2 cột cân đối:
      - **Cột 1**: Icon báo thức dạng ô vuông bo góc (76.dp với nền `primaryContainer` và icon chuông).
      - **Cột 2**:
        - Hàng 1: Text "Báo thức tiếp theo" và badge nổi bật đếm ngược thời gian còn lại (rút gọn: ví dụ "6h 45m").
        - Hàng 2: Giờ báo thức to đậm (`titleLarge` bold + AM/PM tuỳ chế độ 12h/24h) -> cụm icon nhiệm vụ -> ngày lặp lại.
        - Hàng 3: Nhãn báo thức theo font nghiêng.
      - Toàn bộ là view tĩnh (không Switch, không More menu).
    - Khi không có báo thức (`NextAlarmUiState.AllOff`): hiển thị ảnh minh hoạ theo Light/Dark theme kèm tiêu đề ấm áp "Thời gian nghỉ ngơi" và thông điệp truyền cảm hứng "Không có lịch thức dậy nào sắp diễn ra. Hãy tận hưởng trọn vẹn những phút giây nghỉ ngơi và nạp lại năng lượng nhé!".
  - Tích hợp vào `TodayViewModel` thông qua việc quan sát `AlarmRepository.getAllAlarms()` cùng `AlarmPreferencesDataSource.is24HourFormat` và sắp xếp thứ tự hiển thị tại `TodayScreen`: Thời tiết $\rightarrow$ Báo thức tiếp theo (`TodayNextAlarmView`) $\rightarrow$ Quảng cáo (`NativeAdView`) $\rightarrow$ Trích dẫn truyền cảm hứng (`TodayQuoteView`) ở dưới cùng.
- **Hệ quả:**
  - Cung cấp đầy đủ thông tin báo thức một cách trực quan, đồng bộ nhận diện và định dạng giờ cài đặt (12h/24h) với danh sách báo thức mà vẫn duy trì tính chất thuần hiển thị.
  - Biến trạng thái rỗng thành thông điệp tích cực, ấm áp, phù hợp triết lý ReMind.
### [TDR-076] - Tối Ưu Hóa Tải Dữ Liệu Cache-First, Tự Động Làm Mới Khi Hết Hạn & Sửa Lỗi Loading Vô Tận Tại TodayScreen
- **Ngày thực hiện:** 2026-09-21
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - `TodayViewModel` trong hàm `init` gọi `refresh()`, vốn bị gán cứng cờ `force = true` cho cả `weatherRepository.refreshWeather(force = true)` và `todayRepository.refreshTodayBriefing(force = true)`. Hậu quả là cơ chế cache 1 giờ và cache theo ngày bị vô hiệu hoá mỗi lần mở màn hình hoặc ViewModel tái tạo lại, gây gọi mạng dư thừa.
  - Ban đầu khi giới thiệu `isBusy = isRefreshing || isProcessing`, xảy ra lỗi kẹt loading vô tận (Infinite Loading Deadlock): nếu server trả về `aiStatus == "processing"`, `isProcessing` thành `true` khiến nút refresh TopAppBar xoay liên tục và bị vô hiệu hóa (`enabled = !isBusy = false`). Đồng thời `WeatherRepositoryImpl` và `TodayRepositoryImpl` do kiểm tra cache < 1 giờ nên return sớm mà không gọi API, khiến dữ liệu kẹt vĩnh viễn ở trạng thái `processing`.
  - Khi người dùng quay lại app sau hơn 1 giờ, màn hình không có cơ chế tự động gọi làm mới dữ liệu thời tiết đã hết hạn nếu trước đó trạng thái đã là `completed`.
- **Quyết định:**
  - **Áp dụng Cache-First & Bỏ qua cache khi đang `processing`**:
    - Trong `WeatherRepositoryImpl` và `TodayRepositoryImpl`: Kiểm tra nếu dữ liệu trong DataStore đang chứa `aiStatus == "processing"`, hệ thống không được coi là cache hoàn chỉnh và không return sớm, cho phép tiếp tục gọi API để lấy kết quả hoàn tất.
    - Sửa đổi `TodayViewModel.refresh(force: Boolean = false)`: Trong khối `init`, gọi `refresh(force = false)` để kiểm tra và tái sử dụng dữ liệu hợp lệ trong DataStore cache, chỉ gọi network khi cache hết hạn (> 1 giờ với thời tiết, sang ngày mới với briefing), ngôn ngữ thay đổi hoặc dữ liệu đang ở trạng thái `processing`.
  - **Tách biệt `isRefreshing` (mạng) và `isProcessing` (nội dung AI)**:
    - `ReMindLoadingIconButton`: Gán `loading = isRefreshing` và `enabled = !isRefreshing`. Người dùng luôn có thể chủ động bấm làm mới khi không có tiến trình mạng đang chạy, ngay cả khi AI backend đang trong trạng thái processing.
    - `WeatherInfoView`: Truyền `isLocationClickEnabled = !isRefreshing`.
  - **Tự động làm mới khi hết hạn và Polling nền khi Processing**:
    - Trong `TodayViewModel.onResume()` (gắn với `Lifecycle.Event.ON_RESUME` tại `TodayScreen`): Tự động gọi `refresh(force = false)`. Nếu dữ liệu thời tiết đã hết hạn (> 1 giờ) hoặc sang ngày mới, hệ thống tự động làm mới trong suốt mà không tốn công người dùng thao tác. Nếu đang trong trạng thái processing, ép `refresh(force = true)` sau 15 giây.
    - Thêm cơ chế tự động thăm dò (Auto Polling): Khi `isProcessing` là `true`, ViewModel tự động khởi chạy coroutine định kỳ thăm dò sau mỗi 10 giây (tối đa 6 lần) để cập nhật kết quả AI đã hoàn thành.
- **Hệ quả:**
  - Giải quyết dứt điểm lỗi kẹt loading vô tận tại màn hình Today.
  - Đảm bảo thời tiết luôn được tự động làm mới khi hết hạn (TTL 1 giờ) mỗi khi người dùng mở lại ứng dụng.
  - Tiết kiệm băng thông tối đa và tăng tính tương tác, không khóa cứng giao diện của người dùng.

### [TDR-077] - Đặt Màu Đen (Black) Làm Màu Chủ Đạo Mặc Định
- **Ngày thực hiện:** 2026-09-24
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Màu Black (Đen) vừa được thêm vào hệ thống `litever-designsystem`. Người dùng yêu cầu thiết lập màu này thành lựa chọn màu chủ đạo đầu tiên trong cài đặt Theme cũng như làm màu mặc định cho toàn bộ ứng dụng khi khởi tạo thay cho màu Đỏ (RED) trước đây.
- **Quyết định:**
  - Cập nhật giá trị khởi tạo `colorPalette` thành `"BLACK"` trong `SettingsUiState` và `AlarmPreferencesDataSource`.
  - Thiết lập `"BLACK"` làm giá trị mặc định cho `colorPalette` trong composable `ReMindTheme` (tại `Theme.kt`).
  - Cập nhật `resolvedThemeColor` để map `"BLACK"` sang `LiteverThemeColor.BLACK` và dùng `"BLACK"` làm fallback (`else`).
  - Kiểm tra thứ tự trong `GeneralSettingsScreen`, tùy chọn màu "BLACK" đã nằm ở vị trí đầu tiên của danh sách `colorPaletteOptions`.
  - Cập nhật cấu hình Preview `GeneralSettingsScreenPreview` để phản ánh đúng mặc định mới.
- **Hệ quả:**
  - Toàn bộ ứng dụng sẽ sử dụng bộ màu Đen làm màu nhận diện thương hiệu mặc định ban đầu.
  - Tăng độ tùy biến và mang lại cảm giác hiện đại hơn khi cài đặt mặc định là màu trung tính.

### [TDR-078] - Tích Hợp Nhánh Nhắc Nhở (Reminders) & Đánh Giá Xung Đột Hệ Thống Giữa Alarms và Reminders
- **Ngày thực hiện:** 2026-09-28
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Cần đưa toàn bộ tính năng Nhắc nhở (`features:reminders`) từ nhánh `feature/reminders1` tích hợp vào nhánh mới `feature/reminders` dựa trên nền `main` mới nhất (đã nâng cấp Litever 2.1.0 và màu Black mặc định).
  - Yêu cầu cốt lõi: bảo toàn 100% logic báo thức (`AlarmService`, `AlarmControllerImpl`, `AlarmReceiver`), không để logic của Nhắc nhở xâm lấn hay làm gián đoạn chuông báo thức.
- **Quyết định:**
  1. **Tách biệt hoàn toàn tầng Thực thi Nền & Âm thanh (Service & Audio Isolation):**
     - Giữ nguyên `AlarmService` và `AlarmRingManager` phục vụ độc quyền cho Báo thức (`features:alarms`).
     - Toàn bộ cảnh báo nhắc nhở dạng chuông (Persistent Alert / Alarm Stream) sử dụng riêng `ReminderAlertManager`, `ReminderNotificationManager`, và `ReminderMonitorService` (đối với Location & Wi-Fi Geofence).
     - Định tuyến rung/chuông qua `AudioPlayer` với thiết lập usage phù hợp (`USAGE_ALARM` hoặc `USAGE_NOTIFICATION`).
  2. **Tương thích UI với Litever Design System 2.1:**
     - Bổ sung bộ adapter thành phần `LiteverLegacyCompat.kt` (gồm `LiteverTextField`, `LiteverCircularProgressIndicator`, `LiteverLinearProgressIndicator`, `LiteverModalBottomSheet`, `LiteverDatePickerDialog`, `LiteverSegmentedButton`, `LiteverFilterChip`, `LiteverDialog`).
     - Loại bỏ hoàn toàn các thay đổi xâm lấn ngược vào `features/alarms` (ưu tiên tuyệt đối code từ `main`).
  3. **Khử trùng lặp điều hướng chuông (Deduplicate Ringing Navigation):**
     - Trong `MainActivity.kt`, tách bạch thứ tự ưu tiên điều hướng: Priority 1 (Alarm Ringing), Priority 2 (Alarm Snoozing), Priority 3 (Reminder Ringing với cơ chế chống loop `lastNavigatedRingingReminderId`), bảo đảm `startDestinationId` điều hướng mượt mà không crash.
- **Đánh giá Xung đột Hệ thống (Conflict Risk Assessment):**
  - **Audio Focus:** Cả 2 module đều có khả năng chiếm `AUDIOFOCUS_GAIN_TRANSIENT`. Khi cả 2 kích hoạt cùng lúc, `AudioPlayer` sẽ tạm dừng âm thanh trước đó. Khuyến nghị: Cần bổ sung cơ chế ưu tiên Alarm cao hơn Reminder (nếu Alarm kích hoạt, Reminder tự động mute hoặc chuyển sang notification im lặng).
  - **Notification Channels & Full-screen Intent:** Alarm và Reminder dùng channel riêng biệt (`reminders_persistent`, `reminders_standard`, `remind_alarm_channel`). Đảm bảo không xung đột channel priority.
  - **Exact Alarm Quota:** Android 12+ giới hạn số lượng exact alarm được đặt trước (`SCHEDULE_EXACT_ALARM`). Cả 2 cùng sử dụng `AlarmManager.setExactAndAllowWhileIdle`.
  - **Database Migration:** Đã nâng cấp an toàn Room DB từ version 5 lên version 6 (`MIGRATION_5_6`), bảo toàn các bảng `alarms`, `missions`, `reminders`, `checklist_items`.
- **Hệ quả:**
  - Toàn bộ 12 commits đã được cherry-pick sạch sẽ.
  - Toàn bộ unit tests của `:features:alarms`, `:features:reminders`, `:core:alarm`, và `:app` đều pass 100%.
  - Xây dựng nền tảng vững chắc để chuyển sang bước tối ưu trải nghiệm và kiểm thử đồng thời (concurrency testing) giữa 2 module.

### [TDR-079] - Tách Biệt Cấu Hình Điều Kiện Kích Hoạt Sang Màn Hình Chuyên Biệt & Nâng Cấp Thẻ Tóm Tắt (Trigger Summary Card)
- **Ngày thực hiện:** 2026-09-28
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trong `ReminderEditScreen`, các mục cấu hình điều kiện kích hoạt (Giờ & Lặp lại, Vị trí địa lý & Bán kính, Kết nối Wi-Fi) trước đây được render trực tiếp dạng inline cuộn dài trong cùng một màn hình chỉnh sửa nhắc nhở.
  - Việc này khiến màn hình `ReminderEditScreen` quá dài, làm loãng các thông tin cốt lõi (Tiêu đề, Ghi chú, Danh sách công việc Checklist, Âm thanh & Báo thức) và gây rối mắt khi người dùng phải cuộn qua lại giữa các tùy chọn kích hoạt phức tạp.
- **Quyết định:**
  1. **Nâng cấp `ReminderTriggerSection` thành Thẻ Tóm Tắt (Summary Card):**
     - Thay thế toàn bộ khối form inline tại `ReminderEditScreen` bằng một thẻ Card tóm tắt trực quan, phân cấp thị giác rõ ràng:
       - **Badge Icon & Label:** Hiển thị biểu tượng tương ứng với điều kiện kích hoạt hiện tại (`AccessTime` cho Thời gian, `LocationOn` cho Vị trí, `Wifi` cho Wi-Fi) với màu sắc nổi bật.
       - **Headline nổi bật:** Hiển thị giờ kích hoạt (vd: `08:30` định dạng theo cài đặt 12h/24h), tên địa điểm (hoặc toạ độ), hoặc tên mạng Wi-Fi (SSID).
       - **Subtitle chi tiết:** Tóm tắt lịch lặp lại (Một lần, Hàng ngày, Hàng tuần kèm các thứ T2..CN, Hàng tháng ngày X, Hàng năm), loại sự kiện địa lý (Khi đến nơi / Khi rời đi) hoặc sự kiện Wi-Fi (Khi kết nối / Khi ngắt kết nối / Duy trì kết nối) kèm các khung giờ hiệu lực (Active time windows).
       - **Trạng thái hợp lệ / Lỗi:** Đổi màu viền và hiển thị thông điệp đỏ nếu thiếu dữ liệu bắt buộc (chưa chọn vị trí hoặc chưa nhập Wi-Fi).
       - **Affordance & Tương tác:** Bo góc squircle chuẩn, hỗ trợ `clickable` toàn vùng, icon mũi tên điều hướng (`KeyboardArrowRight`) và dòng chữ gợi ý "Nhấn để chỉnh sửa".
  2. **Tạo Màn Hình Cấu Hình Chuyên Biệt `ReminderTriggerEditScreen`:**
     - Thiết kế giao diện chuyên biệt cho điều kiện kích hoạt với `LiteverTopAppBar` mang tiêu đề "Chỉnh sửa điều kiện kích hoạt", nút Back và nút CTA "Xong" (`R.string.action_done`).
     - Thanh tab chọn loại kích hoạt độc lập `ReminderTriggerTypeSelector` (Thời gian, Vị trí, Wi-Fi) ở đầu màn hình.
     - Hiển thị phần cấu hình chuyên sâu tương ứng (`ReminderTimeConfigSection`, `ReminderLocationConfigSection`, `ReminderWifiConfigSection`).
     - `ReMindBottomBar` ở chân màn hình với nút "Xong" giúp hoàn tất chỉnh sửa nhanh chóng bằng thao tác một tay.
  3. **Đồng bộ Dữ liệu Hai Chiều qua Scoped ViewModel (`RemindersNavigation.kt`):**
     - Đăng ký route điều hướng mới `ReminderTriggerEditRoute = "reminder_trigger_edit_route/{reminderId}"`.
     - Tại `remindersGraph`, `ReminderTriggerEditRoute` trỏ tới ViewModel của màn hình chỉnh sửa cha thông qua `navController.getBackStackEntry(createReminderEditRoute(reminderId))` (fallback `previousBackStackEntry`).
     - Nhờ chia sẻ chung thể hiện `ReminderEditViewModel`, mọi thay đổi cấu hình trên màn hình kích hoạt được phản ánh tức thì vào `ReminderEditUiState` theo thời gian thực (Zero Desync, State Hoisting). Khi người dùng nhấn "Xong" hoặc Back, dữ liệu đã được đồng bộ 100% về thẻ tóm tắt trên `ReminderEditScreen`.
  4. **Chuẩn hóa Thành phần Giao diện & Thư viện:**
     - Chuyển đổi toàn bộ dialog cảnh báo quyền và huỷ thay đổi trong `ReminderEditScreen` sang `LvAlertDialog` và `LvButton` chuẩn `litever-designsystem`.
- **Hệ quả:**
  - `ReminderEditScreen` trở nên gọn gàng, súc tích và dễ theo dõi.
  - Người dùng có không gian tập trung tối đa khi tinh chỉnh các điều kiện kích hoạt phức tạp (Geofence, Wi-Fi, Time Windows).
  - Trải nghiệm chuyển màn hình mượt mà, nhất quán, không xảy ra xung đột dữ liệu hay delay khi lưu.
  - Toàn bộ unit tests của các module liên quan tiếp tục đạt 100% kiểm thử thành công.



### [TDR-080] - Tự Động Chụp Ảnh Màn Hình Play Store Bằng Fastlane Screengrab & Cô Lập Dữ Liệu Mock Đa Ngôn Ngữ
- **Ngày thực hiện:** 2026-09-29
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Để chuẩn bị phát hành lên Google Play Store, ứng dụng cần bộ ảnh chụp màn hình hoàn chỉnh, sắc nét trên các tỷ lệ màn hình (Điện thoại và Máy tính bảng) theo 2 ngôn ngữ chính (Tiếng Anh và Tiếng Việt).
  - Việc chụp thủ công tốn nhiều công sức, dễ sai lệch kích thước và khó duy trì khi cập nhật UI. Nếu đưa dữ liệu test vào Room DB thật sẽ gây ô nhiễm cơ sở dữ liệu, phức tạp hóa việc dọn dẹp môi trường và dễ gây flaky test.
- **Quyết định:**
  1. **Tích hợp Fastlane Screengrab & UiAutomator:**
     - Khai báo `tools.fastlane:screengrab:2.1.1` và `androidx.test.uiautomator:uiautomator:2.3.0` trong `libs.versions.toml` và `app/build.gradle.kts`.
     - Áp dụng `UiAutomatorScreenshotStrategy` chụp toàn màn hình Jetpack Compose sắc nét, ổn định từ Android 10 đến các API hiện đại nhất.
     - Cấp các quyền kiểm thử cần thiết trong `app/src/debug/AndroidManifest.xml` (`WRITE_EXTERNAL_STORAGE`, `CHANGE_CONFIGURATION`, `DUMP`).
  2. **Cô Lập Hoàn Toàn Khỏi Room Database (Zero DB Contamination):**
     - Tuyệt đối không chèn bản ghi hay gọi DAO/Room DB trong quá trình chụp ảnh.
     - Dùng `createAndroidComposeRule<ComponentActivity>()` để setContent trực tiếp các Stateless Screen Composables (`AlarmListScreen`, `ReminderListScreen`, `AlarmRingingScreen`, `MissionRingingScreen`, `AlarmMessageScreen`).
     - Toàn bộ dữ liệu mẫu (Mock Data) khởi tạo thuần túy trong bộ nhớ (In-memory) và bọc trong `ReMindTheme(darkTheme = false)` (Light Mode theo chuẩn thiết kế Store).
  3. **Quản Lý Chuỗi Mock Đa Ngôn Ngữ Qua AndroidTest Resources:**
     - Thiết lập tài nguyên chuỗi chuyên biệt trong `app/src/androidTest/res/values/strings.xml` (Tiếng Anh) và `values-vi/strings.xml` (Tiếng Việt).
     - Kết hợp cùng `@ClassRule LocaleTestRule()` của Screengrab để tự động hoán đổi ngôn ngữ (`vi`, `en-US`) trong một lần chạy kiểm thử duy nhất.
  4. **Giải Quyết Triệt Để Xung Đột Scoped Storage & ADB Pull trên Windows:**
     - Phát triển `ExternalStorageScreenshotCallback` kế thừa `ScreenshotCallback`, điều hướng ghi toàn bộ ảnh chụp vào thư mục ngoài của ứng dụng: `context.getExternalFilesDir("screengrab")` (`/sdcard/Android/data/<package>/files/screengrab/<locale>/images/screenshots/`).
     - Cho phép lệnh `adb pull` trên máy host kéo ảnh trực tiếp 100% không bị `Permission denied` (loại bỏ hoàn toàn fallback pipe Unix `tar` bị treo/corrupted trên Windows PowerShell và CMD).
  5. **Tạo Cấu Hình Screengrabfile & Lane Fastlane:**
     - Tạo `fastlane/Screengrabfile` với cấu hình package name (`vn.io.litever.remind.debug` / `.test`), locales `['vi', 'en-US']` (chuẩn ISO thư mục metadata Play Store), `use_timestamp_suffix(false)` và output `fastlane/metadata/android`.
     - Bổ sung lane `:screenshots` trong `fastlane/Fastfile` tự động build `assembleDebug assembleAndroidTest` và kéo ảnh về đúng thư mục Play Store metadata.
- **Hệ quả:**
  - Quy trình chụp ảnh tự động hóa 100% chỉ bằng một lệnh duy nhất: `bundle exec fastlane screenshots`.
  - Sinh và cập nhật thành công 12 ảnh màn hình Play Store chuẩn Light Mode (6 ảnh Tiếng Việt trong `fastlane/metadata/android/vi/images/phoneScreenshots/` và 6 ảnh Tiếng Anh trong `fastlane/metadata/android/en-US/images/phoneScreenshots/`).
  - Tự động sinh báo cáo HTML trực quan `fastlane/metadata/android/screenshots.html`.
  - Tách biệt tuyệt đối khỏi cơ sở dữ liệu Room, đảm bảo tính bất biến, tốc độ chụp cực nhanh (~25s/locale) và không sinh rác dữ liệu trên thiết bị.

### [TDR-081] - Tập Trung Hóa Icons Vào ReMindIcons & Cô Lập Dependency Material Icons
- **Date:** 2026-09-29 | **Status:** Accepted
- **Context:** Các màn hình rải rác import trực tiếp `androidx.compose.material.icons.*` và phụ thuộc `libs.material.icons.extended` ở mọi feature module gây phình classpath và thiếu kiểm soát.
- **Decision:** Tạo object `ReMindIcons` tập trung toàn bộ icons sử dụng trong `:core:designsystem`, xóa hoàn toàn `material.icons.extended` khỏi `:app` và tất cả `:features:*`.
- **Consequences:** Toàn bộ modules chỉ sử dụng icons qua `ReMindIcons`, cô lập triệt để dependency và bảo đảm Single Source of Truth cho icon UI.

### [TDR-082] - Chuẩn Hóa Âm Lượng & Rung Nhắc Nhở, Lưu Bền Vững Checklist Và Tự Động Reset Theo Chu Kỳ
- **Ngày thực hiện:** 2026-09-29
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Nhắc nhở (Reminders) gặp phải các vấn đề:
    1. Không phát chuông hoặc rung đúng mức âm lượng đã cấu hình (`reminder.action.volume`), kênh stream bị lệch giữa standard notification và persistent alert.
    2. Danh sách checklist không được lưu bền vững khi tạo/sửa; người dùng nhập dở checklist item nhưng bấm thẳng nút Lưu (Save) thì bị mất text.
    3. Trạng thái hoàn thành checklist (`isCompleted`) chưa thể tương tác trực tiếp ngoài danh sách nhắc nhở.
    4. Thiếu cơ chế tự động reset checklist khi bước sang chu kỳ mới (ngày/tháng/năm hoặc sau khoảng thời gian lặp `INTERVAL`).
- **Quyết định:**
  1. **Chuẩn Hóa Âm Lượng & Rung (`ReminderAlertManager`, `ReminderNotificationManager`):**
     - Quy đổi tỷ lệ âm lượng `(volume.coerceIn(0, 100)).toFloat() / 100f` cho cả `STANDARD` notification sound lẫn `FULL_SCREEN` alert.
     - Sử dụng `AudioAttributes.USAGE_ALARM` kết hợp `USAGE_NOTIFICATION` đúng ngữ cảnh khi `useAlarmStream || isAlarmStyle`. Phát đúng chuông tùy chỉnh `reminder.action.ringtoneUri` thay vì gán cứng nhạc chuông mặc định.
     - Gắn `AudioAttributes` vào `Vibrator` để đảm bảo xung rung đồng bộ với mức độ ưu tiên của cảnh báo.
  2. **Quản Lý Bền Vững ID & Auto-flush Draft Checklist:**
     - Tại `ReminderRepositoryImpl`: Đảm bảo khi tạo mới Reminder, các checklist item luôn được gán `id = 0L` để Room SQLite tự động sinh khóa chính (Primary Key Auto-generate), tránh tình trạng trùng ID dẫn đến ghi đè bản ghi (`OnConflictStrategy.REPLACE`). Khi cập nhật, chỉ các item mới tạo tạm thời (id âm hoặc nanoTime) mới được reset về 0L để chèn mới.
     - Tại `ReminderEditViewModel`: Thêm cơ chế tự động nạp text đang gõ dở (`draftChecklistText`) vào danh sách checklist trước khi lưu nếu người dùng bấm nút Lưu trên TopBar mà quên bấm nút Thêm (+).
  3. **Tương Tác Checklist Trực Tiếp Tại ReminderCard:**
     - Nâng cấp `ReminderCard`: Người dùng có thể nhấn vào hàng tiến độ checklist để mở rộng (Expand) toàn bộ danh sách các đầu việc và tương tác check/uncheck trực tiếp.
     - `ReminderListViewModel` và `ReminderListScreen`: Thêm callback `toggleChecklistItem` ghi nhận trạng thái tức thời xuống SQLite DB mà không làm reload toàn bộ giao diện hay ngắt quãng trải nghiệm.
  4. **Tự Động Reset Checklist Theo Chu Kỳ (Cycle Auto-Reset):**
     - Bổ sung trường `lastChecklistResetTime: Long?` vào `Reminder` và `ReminderEntity`.
     - Tạo `MIGRATION_6_7` nâng database lên version 7 (`ALTER TABLE reminders ADD COLUMN lastChecklistResetTime INTEGER`).
     - Phát triển `ChecklistCycleUtil`: Tính toán chu kỳ reset dựa trên `repeatMode` (`DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`) hoặc `INTERVAL` (theo khoảng thời gian) so với `lastChecklistResetTime` hoặc `lastTriggeredTime`.
     - Tích hợp tự động quét và reset checklist:
       - Trong `ReminderReceiver`: Khi chu kỳ reo mới được kích hoạt, tự động reset checklist về chưa hoàn thành (`isCompleted = 0`) trước khi phát chuông/thông báo.
       - Trong `ReminderSyncManager` và `ReminderListViewModel`: Định kỳ kiểm tra và reset khi đồng bộ hoặc mở màn hình danh sách nhắc nhở.
- **Hệ quả:**
  - Nhắc nhở đổ chuông và rung chính xác với âm lượng người dùng lựa chọn.
  - Checklist được lưu trữ bền vững 100%, không bị mất nội dung khi gõ nhanh.
  - Các công việc định kỳ hàng ngày/tuần/tháng/năm tự động sẵn sàng cho ngày mới mà không cần thao tác dọn dẹp thủ công.
  - Toàn bộ unit tests bao gồm `ChecklistCycleUtilTest` và `ReminderAlertManagerTest` pass 100%.

### [TDR-083] - Loại Bỏ Hoàn Toàn Checklist Trong Tính Năng Nhắc Nhở (Reminder)
- **Ngày thực hiện:** 2026-09-30
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Nhắc nhở (Reminders) ban đầu tích hợp subtask/checklist nhằm phục vụ việc theo dõi danh sách việc cần làm kèm theo thông báo.
  - Tuy nhiên, trong thực tế sử dụng và định hướng sản phẩm, Reminder cần tập trung tối đa vào độ tin cậy của việc cảnh báo thời gian (`FIXED_TIME` & `INTERVAL`), tránh làm rối trải nghiệm người dùng với việc quản lý danh sách công việc phức tạp vốn thuộc về app Todo chuyên biệt.
  - Sự tồn tại của Checklist làm phát sinh nhiều phức tạp: bảng quan hệ `checklist_items`, `PopulatedReminder`, các vòng lặp reset chu kỳ, thao tác inline checklist toggle trên notification, kéo dài màn hình sửa nhắc nhở và gia tăng rủi ro race condition khi đồng bộ Room.
- **Quyết định:**
  1. **Tầng Model (`:core:model`):**
     - Xóa hoàn toàn model `ChecklistItem.kt`.
     - Loại bỏ trường `checklist` cùng toàn bộ helper tính tiến độ (`checklistProgress`, `totalChecklistCount`, `completedChecklistCount`, `isAllChecklistCompleted`) khỏi model `Reminder.kt`.
  2. **Tầng Cơ Sở Dữ Liệu (`:core:database`):**
     - Xóa `ChecklistItemEntity.kt`, `ChecklistItemDao.kt`, và model quan hệ `PopulatedReminder.kt`.
     - `ReminderDao`: Trực tiếp trả về `ReminderEntity` thay vì bọc trong `PopulatedReminder`, bỏ `updateReminderWithChecklist`.
     - `RemindDatabase`: Loại bỏ `ChecklistItemEntity` khỏi danh sách `@Database.entities`, xóa DAO getter `checklistItemDao()`.
     - `DatabaseModule`: Loại bỏ `provideChecklistItemDao`.
  3. **Tầng Domain & Data (`:core:domain`, `:core:data`):**
     - Xóa tiện ích `ChecklistCycleUtil.kt`.
     - `ReminderRepository`: Loại bỏ các phương thức `updateChecklistItemCompletion`, `resetChecklist`, `checkAndResetRecurringChecklists`.
     - Xóa mapper `ChecklistItemMapper.kt` và mapper test tương ứng.
     - `ReminderMapper`: Tinh gọn chỉ map trực tiếp giữa `ReminderEntity` và `Reminder`.
  4. **Tầng Cảnh Báo & Thông Báo (`:core:alarm`):**
     - Loại bỏ action `ACTION_REMINDER_CHECKLIST_TOGGLE` khỏi `ReminderReceiver` và `AndroidManifest.xml`.
     - `ReminderNotificationManager`: Bỏ việc render checklist tóm tắt và pending intent toggle checklist item inline.
     - `ReminderSyncManager`: Bỏ lệnh tự động kiểm tra và reset recurring checklists.
  5. **Tầng Giao Diện & ViewModel (`:features:reminders`):**
     - Xóa các composable component: `ReminderChecklistBuilderSection.kt` và `ReminderRingingChecklist.kt`.
     - `ReminderCard.kt`: Loại bỏ thanh tiến độ checklist và danh sách checklist con mở rộng.
     - `ReminderListViewModel.kt` & `ReminderListScreen.kt`: Bỏ callback `toggleChecklistItem` và logic duplicate checklist.
     - `ReminderEditViewModel.kt` & `ReminderEditScreen.kt`: Xóa `checklist`, `draftChecklistText`, và toàn bộ hàm CRUD checklist (`addChecklistItem`, `moveChecklistItem`, v.v.).
     - `ReminderRingingViewModel.kt` & `ReminderRingingScreen.kt`: Xóa phần render checklist khi đang reo chuông và action `toggleChecklistItem`.
     - Xóa toàn bộ chuỗi ký tự i18n liên quan đến checklist trong `values/strings.xml` và `values-vi/strings.xml`.
  6. **Đồng Bộ & Dọn Sạch Test Suite:**
     - Tái cấu trúc các challenger test (`ReminderDatabaseChallengerTest`, `ReminderSnoozeChallengerTest`, `ReminderPermissionChallengerTest`) để tập trung vào Snooze, State Isolation và Permission UX.
     - Dọn sạch `ReminderEditViewModelTest`, `ReminderListViewModelTest`, `ReminderRingingViewModelTest`, và `ScreenshotsTest`.
- **Hệ quả:**
  - Kiến trúc Reminder trở nên tinh gọn, hiệu năng truy vấn Room DB tăng đáng kể (không cần JOIN hay Transaction đa bảng).
  - Giao diện người dùng thanh thoát, tập trung đúng vào trọng tâm thiết lập nhắc nhở và báo thức.
  - Toàn bộ 473 task build và unit tests của dự án vượt qua 100% không lỗi.

### [TDR-084] - Loại Bỏ Cơ Chế Tự Động Thêm Dữ Liệu Mẫu Cho Database (Debug Seeding)
- **Ngày thực hiện:** 2026-09-30
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Trước đây, ứng dụng có callback `RemindDatabaseCallback` trong source set `debug` tự động chèn 5 báo thức và 3 nhiệm vụ mẫu (`MATH`, `SHAKE`, `TYPING`) vào SQLite Room DB khi mở CSDL lần đầu nếu bảng `alarms` trống.
  - Việc tự động sinh dữ liệu mẫu này gây phiền toái khi test thực tế hoặc kiểm thử luồng trống (Empty State UI), không phản ánh đúng trạng thái dữ liệu người dùng thực thụ khi cài mới app.
- **Quyết định:**
  - Xóa bỏ file `RemindDatabaseCallback.kt` ở cả hai source set `core/database/src/debug` và `core/database/src/release`.
  - Trong `DatabaseModule.kt`: Loại bỏ `.addCallback(RemindDatabaseCallback())` khỏi builder của Room Database.
- **Hệ quả:**
  - Cơ sở dữ liệu luôn bắt đầu ở trạng thái trống hoàn toàn (clean state), giúp kiểm thử chính xác các màn hình và trạng thái empty state.
  - Toàn bộ 473 task build và test của dự án hoàn thành 100% không lỗi.



### [TDR-085] - Tái Cấu Trúc Logic Báo Lại (Flexible Snooze) và Phân Loại Cảnh Báo Nhắc Nhở
- **Ngày thực hiện:** 2026-10-01
- **Trạng thái:** Accepted
- **Bối cảnh:**
  - Người dùng yêu cầu hiện đại hóa và làm sạch logic nhắc nhở:
    1. Cơ chế hoãn/báo lại linh hoạt (Flexible Snooze): Cho phép chọn nhanh thời gian báo lại (+5m, +10m, +15m) hoặc nhập số phút tùy chỉnh ngay trên màn hình reo chuông, loại bỏ hoàn toàn giới hạn số lần lặp lại (snoozeRepeatCount).
    2. Phân loại kiểu thông báo thành "Thông báo thường" (STANDARD) và "Đổ chuông" (FULL_SCREEN). Mặc định khi tạo mới là "Đổ chuông". Cả hai đều hiển thị giao diện toàn màn hình, nhưng dạng "Đổ chuông" có Foreground Service (ReminderAlertService) phát nhạc chuông liên tục và hỗ trợ tự động tắt tiếng / phát qua kênh báo thức (STREAM_ALARM).
    3. Cả hai kiểu thông báo đều hỗ trợ tùy chỉnh nhạc chuông, âm lượng và rung.
    4. Trên thông báo thu nhỏ (heads-up / banner), cung cấp nút "Báo lại 5 phút" và nút xác nhận hoàn thành ("Hoàn thành").
    5. Dọn dẹp sạch mã nguồn, loại bỏ snoozeRepeatCount và currentSnoozeCount.
- **Quyết định:**
  1. **Domain Model (:core:model):**
     - Loại bỏ snoozeRepeatCount khỏi ReminderAction và currentSnoozeCount khỏi Reminder.
     - Chuyển giá trị mặc định của 
otificationStyle sang NotificationStyle.FULL_SCREEN.
     - Tinh giản canSnooze(): chỉ kiểm tra snoozeEnabled && urgencyLevel != UrgencyLevel.SOFT.
  2. **Data Mapper (:core:data):**
     - Cập nhật ReminderMapper.kt bỏ mapping 2 trường đã xóa sang domain model, gán giá trị mặc định ổn định khi ghi xuống Room Entity.
  3. **Alarm & Notification Subsystem (:core:alarm):**
     - ReminderNotificationManager.kt: Heads-up notification hiển thị nút "Báo lại 5 phút" (kèm EXTRA_SNOOZE_MINUTES = 5) và nút "Hoàn thành".
     - ReminderReceiver.kt: Hỗ trợ EXTRA_SNOOZE_MINUTES để lập lịch snooze theo số phút người dùng chọn.
  4. **Giao diện & ViewModel (:features:reminders):**
     - ReminderAlertConfigSection.kt: Đổi nhãn thành "Thông báo thường" và "Đổ chuông". Cấu hình âm thanh (nhạc chuông, âm lượng, rung) áp dụng cho cả hai chế độ. Tự động tắt tiếng và Kênh báo thức chỉ hiển thị ở chế độ "Đổ chuông". Loại bỏ mục chọn số lần lặp lại snooze.
     - ReminderRingingScreen.kt: Bổ sung hàng nút chọn nhanh (+5m, +10m, +15m) và nút "Tùy chỉnh" mở LvAlertDialog với LiteverTextField để nhập số phút tùy thích.
     - ReminderRingingViewModel.kt: Nâng cấp hàm snooze(minutes: Int, onFinish: () -> Unit) nhận số phút linh hoạt.
     - ReminderEditViewModel.kt & ReminderEditScreen.kt: Loại bỏ toàn bộ references đến snoozeRepeatCount.
  5. **Đồng bộ Unit Tests:**
     - Cập nhật toàn bộ các bài test liên quan trong :core:alarm, :core:model, :core:data, :features:reminders. Toàn bộ 473 task build và unit tests của dự án vượt qua 100%.
- **Hệ quả:**
  - Trải nghiệm người dùng khi nhắc nhở đến hạn trực quan, linh hoạt và thuận tiện.
  - Codebase sạch, tuân thủ Clean Architecture và Design System chung.

### [2026-10-01] Hoàn thiện và làm sạch giao diện Thêm/Sửa Lời nhắc (ReminderEditScreen & ReminderAlertConfigSection)
- **Mục tiêu:**
  - Làm sạch mục "Cảnh báo & Thông báo" trong màn hình Thêm/Sửa lời nhắc:
    - Loại bỏ hoàn toàn bộ chọn UrgencyLevel (SOFT/MEDIUM/HARD) gây rườm rà.
    - Đưa 2 chế độ thông báo lên đầu: "Đổ chuông" (mặc định) và "Thông báo thường" với mô tả chi tiết trực quan.
    - Cấu hình Nhạc chuông, Âm lượng và Rung hiển thị và áp dụng cho cả 2 kiểu thông báo; Tên nhạc chuông được trích xuất động qua RingtoneManager thay vì chuỗi cứng.
    - Tự động tắt tiếng và Kênh âm thanh báo thức hiển thị duy nhất khi chọn "Đổ chuông".
    - Cấu hình Báo lại (Snooze) được nhúng trực tiếp ngay trong section (công tắc bật/tắt + chọn nhanh khoảng thời gian 5p, 10p, 15p, 30p), loại bỏ việc điều hướng sang màn hình riêng và loại bỏ giới hạn số lần lặp lại.
- **Thực hiện:**
  - `features/reminders/src/main/java/vn/io/litever/remind/features/reminders/ui/components/ReminderAlertConfigSection.kt`:
    - Loại bỏ tham số và UI của `UrgencyLevel`.
    - Thêm mô tả trực quan dưới bộ chọn chế độ: `style_full_screen_desc` và `style_standard_desc`.
    - Tích hợp inline Snooze: Switch bật/tắt và `LiteverSingleChoiceSegmentedButtonRow` chọn khoảng thời gian (5, 10, 15, 30 phút).
    - Cập nhật các hàm Preview (chuẩn, thường, chế độ tối).
  - `features/reminders/src/main/java/vn/io/litever/remind/features/reminders/ui/ReminderEditScreen.kt`:
    - Trích xuất tên nhạc chuông từ `RingtoneManager.getRingtone()` dựa trên `uiState.ringtoneUri`.
    - Kết nối `onSnoozeEnabledChange` và `onSnoozeIntervalChange` trực tiếp với ViewModel.
    - Loại bỏ các param dư thừa và listener không dùng đến.
  - `features/reminders/src/main/java/vn/io/litever/remind/features/reminders/viewmodel/ReminderEditViewModel.kt`:
    - Thêm `setSnoozeEnabled` và `setSnoozeIntervalMinutes`.
- **Kết quả kiểm thử:**
  - `./gradlew testDebugUnitTest`: Vượt qua 100% toàn bộ 473 task của dự án không một lỗi nào.

### [2026-10-01] Đổi text tab sang "Nhắc nhở" và loại bỏ bộ lọc ở màn hình danh sách nhắc nhở
- **Mục tiêu:**
  - Đổi nhãn tab điều hướng và tiêu đề từ "Lời nhắc" thành "Nhắc nhở".
  - Loại bỏ hoàn toàn hàng filter chips (`ReminderFilterChips`) cùng toàn bộ logic lọc không cần thiết (ALL, ACTIVE, COMPLETED, TIME, LOCATION, WIFI) khỏi màn hình danh sách nhắc nhở.
  - Tinh giản `ReminderListViewModel`, `ReminderListUiState`, `ReminderListScreen`, `ReminderEmptyState` và cập nhật tương ứng các unit test.
- **Thực hiện:**
  - **Tài nguyên chuỗi:**
    - `app/src/main/res/values-vi/strings.xml`: Cập nhật `navigation_reminders` thành "Nhắc nhở".
    - `features/reminders/src/main/res/values-vi/strings.xml`: Cập nhật `navigation_reminders`, `reminders_title`, `add_reminder_title`, `edit_reminder_title`, `no_reminders`, `empty_reminders_description`, `delete_completed_reminders`, `delete_disabled_reminders`, `reminder_deleted`.
    - Dọn dẹp các chuỗi lọc (`filter_*`, `no_*_reminders`) khỏi `values/strings.xml` và `values-vi/strings.xml`.
  - **Giao diện & Thành phần (`features/reminders`):**
    - `ReminderListScreen.kt`: Xóa hoàn toàn component `ReminderFilterChips` và tham số `onFilterSelected`. Hiển thị trực tiếp danh sách `reminders` đã được sắp xếp hoặc `ReminderEmptyState` đơn giản hóa.
    - `ReminderEmptyState.kt`: Loại bỏ tham số `selectedFilter`, luôn hiển thị trạng thái rỗng chuẩn (`no_reminders`, `empty_reminders_description`).
    - Xóa file `ReminderFilterChips.kt`.
  - **ViewModel & State:**
    - `ReminderListViewModel.kt`: Loại bỏ `_selectedFilter`, `setFilter`, `filterCounts`. Sắp xếp trực tiếp luồng nhắc nhở (ưu tiên nhắc nhở đang snooze lên đầu, sau đó theo `createdAt` giảm dần).
    - `ReminderListUiState`: Tinh giản state chỉ còn `reminders`, `is24HourFormat`, `hasPermissionWarning`, `missingPermissions`.
  - **Kiểm thử đơn vị:**
    - `ReminderListViewModelTest.kt`: Xóa các test chuyển filter, thêm test xác minh quy tắc sắp xếp tự động.
    - `ReminderR1R2EmpiricalChallengerTest.kt`: Cập nhật các test case để kiểm tra quy tắc sắp xếp danh sách trực tiếp không phụ thuộc filter.
- **Kết quả kiểm thử:**
  - `./gradlew testDebugUnitTest`: Vượt qua 100% toàn bộ 473 task thành công.

### [2026-10-01] Loại bỏ thanh chỉnh âm lượng ở chế độ Thông báo thường (NotificationStyle.STANDARD)
- **Bối cảnh & Xác nhận kỹ thuật:**
  - Trong Android (API 26+), thông báo tiêu chuẩn (Notification/NotificationCompat) phát âm thanh thông qua Kênh thông báo hệ thống (`STREAM_NOTIFICATION`).
  - Hệ điều hành Android KHÔNG cung cấp API để đặt âm lượng chuông riêng biệt cho từng thông báo độc lập (chỉ có thể chọn file nhạc chuông và bật/tắt rung). Âm lượng phát luôn tuân theo mức âm lượng thông báo tổng của thiết bị.
  - Do đó, việc hiển thị thanh trượt âm lượng ở chế độ "Thông báo thường" là không có tác dụng thực tế.
- **Thực hiện:**
  - `ReminderAlertConfigSection.kt`:
    - Thanh trượt âm lượng (Volume Slider) chỉ hiển thị ở chế độ "Đổ chuông" (`NotificationStyle.FULL_SCREEN` - nơi có foreground service phát âm thanh liên tục qua `MediaPlayer.setVolume()`).
    - Ở chế độ "Thông báo thường" (`NotificationStyle.STANDARD`), loại bỏ thanh trượt âm lượng và hiển thị một hàng công tắc "Rung" (`LiteverSwitch`) độc lập, kèm mô tả "Rung khi thông báo xuất hiện" / "Tắt".
  - Bổ sung chuỗi tài nguyên `vibration_enabled_desc` trong `values/strings.xml` và `values-vi/strings.xml`.
- **Kết quả kiểm thử:**
  - `./gradlew testDebugUnitTest`: Vượt qua 100% toàn bộ 473 task thành công.



