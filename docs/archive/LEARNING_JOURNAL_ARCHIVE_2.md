## [2026-10-07] Material 3 Native Components Over Custom Wrappers & Material 3 Expressive Lifecycle
### Key Lessons
- **Scaffold Hygiene & Native Component Reuse**: Wrapping `Scaffold` in an outer `Box` with custom full-screen Scrim overlays and custom Speed Dial components creates unnecessary layout nesting and maintenance overhead. Instead, anchoring Material 3's native `DropdownMenu` inside `Scaffold(floatingActionButton = { Box { FloatingActionButton(...) DropdownMenu(...) } })` leverages standard M3 dismissal, animation, and scrim behavior without producing orphaned files or polluting the root UI tree.
- **Material 3 Expressive API Availability**: While Material 3 Expressive introduces official components like `FloatingActionButtonMenu`, `ToggleFloatingActionButton`, and `FloatingActionButtonMenuItem` to replace Speed Dials, these APIs reside strictly in the `1.5.0-alpha` track and remain `internal` in `1.4.0` stable. Projects on stable Compose BOMs must avoid forcing experimental alpha APIs that cause compilation failures (`Cannot access internal annotation class ExperimentalMaterial3ExpressiveApi`).
- **Transient Entity Lifecycle Pattern**: For one-off, short-duration alerts (Quick Alarms), persisting them with a regular editing lifecycle introduces UX friction. Treating them as transient tasks that automatically purge on dismissal (`dismissAlarm`) and on manual disablement (`toggleAlarm`), while restricting edit screen access, keeps the main alarm list decluttered.
- **Feature Scope Hygiene**: Unimplemented placeholders (like habit alarm options) should not be leaked into UI menus and localization strings. Pruning them keeps the UX focused and codebase clean.

### Corrective Actions
- Removed custom `AlarmSpeedDialFab` and outer Box wrapper; restored `Scaffold` as the root container in `AlarmListScreen`.
- Implemented M3 FAB menu using `FloatingActionButton` + `DropdownMenu` with options for normal alarm and quick alarm, pruning unimplemented habit alarm references.
- Automated deletion of quick alarms upon switch deactivation and ringing dismissal in `AlarmListViewModel` and `AlarmControllerImpl`.
- Documented technical investigation of Material 3 Expressive 1.5.0 vs 1.4.0 in TDR and changelog.

## [2026-10-06] Compose Preview Out-of-Sync R Class Field Discrepancies
### Key Lessons
- **AAPT & Bytecode Desynchronization in Compose Previews**: When adding new string resources (e.g. `action_clear_title` in `strings.xml`) during feature development, Android Studio Preview renders composables by loading compiled bytecode `.class` files (such as `R$string.class`) from the build output directory. If the module is not recompiled after adding resources, `stringResource(R.string.action_clear_title)` throws a `java.lang.NoSuchFieldError` at preview layout time.
- **Gradle Task Remediation for Preview Render Errors**: Rebuilding the target module's assemble task (e.g. `./gradlew :features:reminders:assembleDebug`) forces AAPT2 and the Kotlin compiler to regenerate `R.java`, `R.class`, and updated composable bytecode, resolving preview render failures without altering source code.

### Corrective Actions
- Executed `:features:reminders:assembleDebug` build task to sync AAPT-generated `R$string` field IDs.
- Verified Compose Preview rendering (`ReminderEditScreenDarkPreview`, `ReminderBasicInfoSectionPreview`) via `render_compose_preview`.

## [2026-10-05] Separation of Concerns: Recurrence Schedule vs Within-Day Trigger Invariants
### Key Lessons
- **Hierarchy of Scheduling Concerns**: In reminder systems, calendar recurrence (which days/dates to trigger: One-time, Weekly, Monthly, Yearly) is fundamentally an orthogonal concern to intra-day triggers (how and when to ring during an active day: fixed times vs interval cycles with time windows). Embedding recurrence selection into specific trigger branches results in duplicate UI, state fragmentation, and excessive props drilling. Lifting recurrence to the top level of the reminder editor restores clean Single Responsibility and simplifies child composables.
- **Micro-scope vs Macro-scope Filters**: `TimeWindow` represents an intra-day active window (e.g. 08:00 to 18:00) during which periodic interval alarms can ring. Giving `TimeWindow` its own day-of-week selection creates semantic conflicts and user confusion with the reminder's top-level calendar recurrence. Eliminating day selection from `TimeWindow` keeps it simple, focused, and free of redundant state.
- **Dead Composable Hygiene**: During rapid prototyping or iterative UI changes, dialogs or legacy composables (e.g., `ReminderTimeWindowDialog`) often become orphaned while inline replacements are developed. Periodic static scans and git search for zero-reference UI components prevent codebase bloat.

### Corrective Actions
- Lifted `ReminderRepeatConfigSection` to the top-level `ReminderEditScreen`.
- Stripped 12+ repeat callback props from `ReminderTriggerSection` and `ReminderTimeConfigSection`.
- Removed weekday selection controls from `ReminderTimeWindowConfigSection`.
- Deleted orphaned `ReminderTimeWindowDialog.kt` and associated unused string resources.

## [2026-09-29] Multi-module Test Dependency Hygiene & Schema Separation
### Key Lessons
- **Multi-module Test Scope Visibility**: In Gradle multi-module projects, `testImplementation` dependencies declared in `:features:reminders` or `:core:testing` do not automatically propagate to other sibling feature modules (`:features:today`, `:features:mission`). Running module-specific test tasks might give a false sense of security while a root `./gradlew test` will fail due to unresolved references in siblings. Always run `./gradlew test` to ensure cross-module health.
- **Trigger Specialization**: Attempting to model periodic recurring intervals (INTERVAL) as static points in time (FIXED_TIME) introduces cognitive complexity, unnecessary array mutations, and time-drift bugs. Keeping them in distinct fields (`fixed_times` vs `interval_minutes` + `time_windows`) ensures clean business invariants and straightforward Room queries.
- **Dangling Test Cleanup**: When refactoring architectural components (e.g. deprecating location/wifi triggers in favor of time triggers), test suites in upper consumer layers (`:app`) that test legacy combinations must be systematically audited and cleaned or upgraded, rather than left dangling to fail in CI.

### Corrective Actions
- Added `libs.junit` to `features/today/build.gradle.kts` and `features/mission/build.gradle.kts`.
- Refactored Room entity `ReminderEntity` to explicitly handle `FIXED_TIME` and `INTERVAL`.
- Removed legacy location/wifi tests from `:app` and `:core:alarm`.

## [2026-05-07] Automatic Database Cleanup & Flow Loops
### Key Lessons
- **Flow Trigger Loops**: When a `Flow` from a database (like Room) is mapped in a `ViewModel`, performing a database update within that `map` can potentially cause an infinite loop if the update triggers another emission that satisfies the same condition.
- **Self-Terminating Updates**: To safely update the database from within a `Flow.map`, ensure the update logic modifies the data such that it **no longer satisfies** the trigger condition on the next emission. For example, clearing an "expired" flag or a past timestamp.
- **Batch vs. Individual Updates**: For multiple items requiring cleanup, launching individual coroutines is acceptable for small datasets, but consider batch updates for performance-critical scenarios or large lists.
- **State Consistency**: Providing a "cleaned" list immediately in the `map` ensures the UI is responsive, while the background database update ensures persistence.

### Corrective Actions
- Implemented `isSkipExpired` cleanup in `AlarmListViewModel`'s `alarms` flow.
- Added safety check to only launch update if the condition is met, preventing loops.

## [2026-05-05] PendingIntent Identity & Alarm Collisions
### Key Lessons
- **PendingIntent Equality**: In Android, two `PendingIntent` objects are considered equal if their `ComponentName`, `Action`, and `RequestCode` match. `Intent` extras are **NOT** part of the identity.
- **Silent Overwrites**: If you schedule two alarms (e.g., a main alarm and a snooze) with the same identity, the second one will silently overwrite the first in `AlarmManager`.
- **Cancellation Side Effects**: Calling `alarmManager.cancel(pendingIntent)` will cancel ANY alarm matching that identity. If a main alarm and a snooze share an identity, cancelling the snooze will accidentally kill the main alarm's future schedule.
- **Deterministic Unique IDs**: When multiple intents are needed for the same entity (like an Alarm), use a deterministic offset or bit-flag in the `requestCode` and different `Actions` to guarantee uniqueness.

### Corrective Actions
- Implemented `ACTION_TRIGGER_SNOOZE` and a +1B offset for snooze request codes.
- Updated `AlarmSyncManager` to restore both main and snooze schedules after reboot.

## [2026-04-29] Design System Modularization & Custom Themes
### Key Lessons
- **Custom Theme Architecture**: Using `staticCompositionLocalOf` to provide a custom `LiteverColors` class allows for a more flexible and multi-app design system than relying solely on `MaterialTheme.colorScheme`. This pattern enables each application to define its own branding while reusing the same base components.
- **Full M3 Token Mapping**: Standard Material 3 components (like `NavigationBar`) rely on modern color tokens such as `surfaceContainer`. Missing these tokens in a custom theme causes visual inconsistencies and broken dynamic coloring. Always map all 36+ color tokens for full compatibility.
- **Naming Conflicts in Compose**: Naming a variable exactly like its class (e.g., `val Shapes = Shapes(...)`) can cause confusing compilation errors in Compose, especially when both are in the same package. Prefixing custom instances (e.g., `LiteverShapes`) is a safer practice.
- **Composable Context in Extensions**: Extension properties that access `@Composable` values (like `CompositionLocal.current`) must also be marked with `@get:Composable` or used within a Composable function. Direct property access will fail compilation.
- **Workflow Efficiency**: Instead of writing UI components from scratch for every new module, a "Copy & Adapt" workflow (automated via Agent scripts) significantly reduces token usage and prevents "hallucinated" variations of common components.

### Corrective Actions
- Fixed compilation errors in `LiteverSwitch` by moving `@Composable` access to the correct context.
- Resolved naming ambiguity between the `Shapes` class and the `Shapes` variable.
- Patched Windows-specific `bundleLibCompileToJarDebug` file access errors by stopping Gradle daemons and retrying builds.
- Refactored `core:designsystem` into an adapter layer to maintain backward compatibility for existing feature modules.


## [2026-04-21] Stabilizing Alarm & Mission Logic
### Key Lessons
- **Reactive Flow Integrity**: When using `combine` or `flatMapLatest` in a Service, ensure all potential state changes (like Mute/Unmute) are captured at the top level of the flow.
- **Direct UI-Service Signals**: For critical UI feedback like "Back from Mission -> Sound ON", calling the Manager/Repository directly from the ViewModel is more reliable than waiting for navigation results.
- **Resource Lifecycle**: Always wrap `MediaPlayer.stop()` in try-catch and check `isActive` before `start()` in asynchronous contexts.
- **Process Improvement**: Established `DECISION_LOG.md` to record solidified behavioral scenarios. This prevents arbitrary changes to complex logic once it's tested and approved.

### Corrective Actions
- Fixed KSP errors by splitting Flow/Suspend DAO methods.
- Implemented Safe-Start pattern for MediaPlayer.
- Synchronized Auto-Silence timer with mission states.

Đây là nơi Agent (với tư cách là một Android Developer hiện đại) ghi chép lại những sai lầm gặp phải, những giả định chưa đúng trong quá trình phát triển, cũng như các thói quen, quy chuẩn code đúc rút ra được.
Cơ chế này đảm bảo "sự tiến hóa" liên tục qua các task, tránh việc một lỗi vi phạm lặp đi lặp lại.

> **Quy tắc:** Agent sẽ tự cập nhật vào cuối file này khi chạy quy trình `/learning-journal`.

---

## [2026-05-03] AdMob Integration & Native Ad Caching
### Key Lessons
- **Composable Lifecycle & Ad Fraud**: In Jetpack Compose Navigation, switching tabs causes the composable for the previous tab to be disposed. If ads are loaded directly within the composable (e.g., via `LaunchedEffect`), they will be reloaded every time the user returns to the tab. This can be flagged as "Invalid Traffic" by AdMob.
- **Singleton Ad Management**: To comply with ad policies and improve UX, ad loading and storage should be managed outside the UI lifecycle (e.g., in a Singleton Manager or a long-lived ViewModel).
- **Native Ad Viewbinding in Compose**: Since Compose doesn't have a built-in Native Ad component, `AndroidView` must be used to wrap the XML/View-based `com.google.android.gms.ads.nativead.NativeAdView`. It is crucial to correctly bind all components (headline, icon, CTA) to the `NativeAdView` so that AdMob can track impressions and clicks accurately.
- **Hilt EntryPoints**: For modules like `:core:designsystem` that provide generic UI components but are not themselves Hilt-managed (like a Composable function), `EntryPoints.get` is an effective way to access Singleton dependencies (like `NativeAdManager`) from the `ApplicationContext`.

### Corrective Actions
- Refactored `NativeAdView` to use a `NativeAdManager` singleton instead of local loading logic.
- Implemented a 5-minute cache for Native Ads to prevent excessive network requests.
- Fixed an issue where the package declaration was accidentally removed during a file replacement.

---

## Ngày tháng: 2026-04-06

**Vấn đề / Task:** Dùng trực tiếp các thành phần `androidx.compose.material3.*` (như Button, Icon) tại module `:features:alarm` vì thấy `:core:designsystem` lúc đó đang cạn/rỗng.
**Phân tích nguyên nhân:** 
* Giả định chưa chính xác: Tôi đã lầm tưởng rằng có thể dùng trực tiếp thư viện gốc Material3 ở Feature module đối với mọi loại Component khi `designsystem` còn rỗng.
* Nhận định đúng (Pragmatic Approach): Việc bọc 100% mọi component (kể cả Text) ngay từ đầu có thể gây dư thừa (Over-engineering).
**Giải pháp / Rule mới:** 
* **Quy mô áp dụng mếm mỏng:** Tuân thủ Design System (Wrapper component) với các cấu trúc tương tác lớn, định hình UI (như `Button`, `Scaffold`, `Switch`, `Dialog`...). Kể cả khi `:core:designsystem` rỗng, BẮT BUỘC phải tạo lớp bọc cho chúng (VD: `AlarmButton`) trước khi dùng. 
* Đối với các thành phần cơ bản thuần tuý như `Text` hay `MaterialTheme.typography`, CÓ THỂ linh động sử dụng trực tiếp từ `androidx.compose.material3.*` tại feature module để giảm chi phí phát triển không cần thiết.

---

## Ngày tháng: 2026-04-07
**Vấn đề / Task:** Gặp lỗi `Implicit Intent` không đặt được báo thức và `Foreground Service Notification` bị chậm hiển thị 10s.
**Phân tích nguyên nhân:** 
* **Implicit Intent:** Từ Android 8+, OS cấm Broadcast ngầm định để tiết kiệm pin. Việc dùng `Intent(ACTION_STRING)` mà không set Package/Class khiến Receiver không bao giờ nhận được báo thức.
* **Notification Delay:** Android 12+ mặc định delay hiển thị Foreground Notification 10 giây để tránh làm phiền người dùng. Điều này gây thảm họa cho App Báo Thức (nhạc kêu nhưng không có nút tắt).
* **Dependency Violation:** Module `:core:alarms` (Back-end) không thể gọi trực tiếp `MainActivity` (App module) vì gây circular dependency.
**Giải pháp / Rule mới:** 
* **Quy tắc 1 (Explicit Intent):** LUÔN sử dụng Explicit Intent (chỉ đích danh Class) cho mọi Broadcast/Service/Activity nội bộ.
* **Quy tắc 2 (Dependency Inversion):** Khi module tầng thấp cần gọi Activity/Module tầng cao, BẮT BUỘC dùng Interface (`AlarmIntentProvider`) định nghĩa ở tầng thấp, và implement tại tầng cao nhất (`:app`).
* **Quy tắc 3 (Bẻ gãy OS Delay):** Dùng `.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)` và ép `manager.notify()` ngay TRƯỚC khi gọi `startForeground()` để đảm bảo UI hiện lên lập tức cùng nhịp với âm thanh chuông.

---

## Ngày tháng: 2026-04-08
**Vấn đề / Task:** Ứng dụng bị crash (`IllegalStateException: A migration from...`) khi chạy trên phiên bản cũ có sẵn cấu trúc Database, sau khi thêm các trường mới (ví dụ `vibrationEnabled`, `ringtoneUri`) vào Entity.
**Phân tích nguyên nhân:** 
* Giả định chưa chính xác: Đã không lường trước việc cập nhật cấu trúc bảng (thêm cột) sẽ làm Room Database báo lỗi phi tương thích lược đồ (schema) trên thiết bị đã có sẵn dữ liệu trước đó.
* Hậu quả: App bị crash (Force Close) ngay lập tức khi khởi tạo Database. Kể cả việc thêm fallback (`fallbackToDestructiveMigration()`) cũng là điều cấm kỵ ở Production vì nó sẽ xóa sạch toàn bộ báo thức quan trọng của người dùng.
**Giải pháp / Rule mới:** 
* **Bảo Toàn Dữ Liệu:** BẤT CỨ KHI NÀO chỉnh sửa cấu trúc của Entity (thêm/sửa cột), BẮT BUỘC phải thực hiện 2 việc:
   1. Tăng số `version` trong `@Database`.
   2. Viết object `Migration(old_version, new_version)` với lệnh SQL tương ứng (Ví dụ: `ALTER TABLE ... ADD COLUMN ...`) và truyền vào `addMigrations()` của Room Builder để nâng cấp mượt mà, giữ nguyên dữ liệu gốc.

---

## Ngày tháng: 2026-04-09
**Vấn đề / Task:** App crash với exception `IllegalStateException: Asking for intrinsic measurements of SubcomposeLayout layouts is not supported` khi sử dụng `LazyRow` bên trong thuộc tính `supportingContent` của Compose `ListItem`.
**Phân tích nguyên nhân:** 
* `ListItem` của Material3 tính toán dàn layout dựa trên cơ chế `Intrinsic measurements` (đo kích thước tự nhiên tối thiểu của các children như `headlineContent`, `supportingContent`).
* `LazyRow` và `LazyColumn` được xây dựng dựa trên `SubcomposeLayout`. Thành phần này hoàn toàn KHÔNG HỖ TRỢ trả về `Intrinsic measurements` nên ngay khi `ListItem` "hỏi" kích thước, nó lập tức văng exception (do không thể hỗ trợ pre-measure một list lazy chưa biết giới hạn).
**Giải pháp / Rule mới:** 
* **TUYỆT ĐỐI KHÔNG** dùng các Layout có đặc tính Lazy (như `LazyRow`, `LazyColumn`, `BoxWithConstraints`) lồng bên trong các Compose components đo nội dung bằng kích thước gốc (ví dụ như `ListItem`, hoặc làm thẻ child khi height/width đang set là `IntrinsicSize.Min`/`Max`).
---

## Ngày tháng: 2026-04-10
**Vấn đề / Task:** Khi mở màn hình Edit báo thức ở chế độ 24h, `TimeInput` luôn hiển thị thời gian hiện tại (Now + 1m) thay vì thời gian của báo thức đang cần sửa.
**Phân tích nguyên nhân:** 
* `rememberTimePickerState` được khởi tạo bằng `uiState.time` ngay từ Composition đầu tiên.
* Do `uiState.time` ban đầu mang giá trị mặc định và chỉ được cập nhật sau khi `loadAlarm(id)` (bất đồng bộ) hoàn tất, nên `rememberTimePickerState` bị kẹt ở giá trị cũ. 
* Cơ chế `remember` không tự nhận diện sự thay đổi của các tham số `initialHour`/`initialMinute` để khởi tạo lại state bên trong.
**Giải pháp / Rule mới:** 
* **Quy tắc về State Initialization:** Khi một Composable State (`rememberXState`) phụ thuộc vào dữ liệu được tải bất đồng bộ (Async Data), BẮT BUỘC phải bọc nó trong `androidx.compose.runtime.key` với các tham số định danh (như `id` của đối tượng).
* Cụ thể: `key(uiState.id) { rememberTimePickerState(...) }`. Khi `id` thay đổi từ 0 (initial) sang ID thực tế, state sẽ được buộc phải reset và nhận giá trị mới từ ViewModel.

---

## Ngày tháng: 2026-09-17
**Vấn đề / Task:** Ảnh minh hoạ `EmptyState` (`no_alarm_illustration.png`) không đổi theo theme sáng/tối trong ứng dụng, và sau khi thử bọc `LocalContext provides themedContext` bằng `createConfigurationContext(...)` thì ứng dụng bị crash runtime tại `MainActivity` (`ScaffoldLayout`).
**Phân tích nguyên nhân:**
1. **Compose Context Hierarchy:** Các thành phần Compose gốc (đặc biệt là Material 3 `Scaffold`, Navigation, Dialog) mong đợi `LocalContext.current` là instance của `ComponentActivity` / `Activity`. Việc tạo `ConfigurationContext` độc lập và gán vào `LocalContext` đã tước bỏ các đặc tính Activity (như WindowInsets controller, WindowManager, LifecycleOwner), dẫn đến crash tại runtime khi layout/measure pass.
2. **Resource Resolution trong Compose:** Khi app đổi theme ở runtime qua In-App Settings nhưng không thay đổi chế độ của toàn hệ điều hành (OS Night Mode), `painterResource(id)` gọi đến Android `Resources` vẫn bị phụ thuộc vào Activity Configuration ban đầu, dẫn đến việc thư mục `drawable-night/` không được kích hoạt tự động nếu Activity chưa cấu hình lại `uiMode`.
**Giải pháp / Rule mới:**
* **TUYỆT ĐỐI KHÔNG** override `LocalContext provides ...` bằng `createConfigurationContext(...)` ở root level Composable.
* **Xử lý ảnh/drawable theo theme trong Jetpack Compose:**
  1. Giữ nguyên `LocalConfiguration provides themedConfiguration` để các API query `LocalConfiguration.current` nhận đúng `uiMode`.
  2. Với các ảnh asset cần hiển thị khác biệt theo Dark/Light mode của app, hãy khai báo riêng biệt (ví dụ: `no_alarm_illustration` và `no_alarm_illustration_dark`) và chủ động rẽ nhánh dựa theo theme state trong Compose:
     `val illustrationRes = if (!LiteverTheme.colors.isLight) R.drawable.no_alarm_illustration_dark else R.drawable.no_alarm_illustration`
  3. Cách tiếp cận này hoàn toàn an toàn về mặt luồng thực thi, không đụng chạm đến Context gốc, hỗ trợ 100% Android Studio Preview và đổi theme mượt mà tức thì.

---

## [2026-09-29] Fastlane Screengrab trên Android Hiện Đại (API 29+) & Tương Thích Môi Trường Windows
### Vấn đề / Task:
Khi chạy lane `fastlane screenshots` trên thiết bị Android 13+ (API 33), lệnh kéo ảnh qua `adb pull` thất bại với lỗi `Permission denied`. Sau đó Fastlane chuyển sang cơ chế fallback Unix pipe `run-as ... tar -cC ... | tar -xv ...`, lệnh này bị treo vô hạn (hang/freeze) trên Windows PowerShell và Command Prompt.

### Phân tích nguyên nhân:
1. **Screengrab Library lưu vào Internal Storage trên API > 29:**
   Lớp mặc định `FileWritingScreenshotCallback` trong thư viện `tools.fastlane:screengrab` có đoạn mã `if (Build.VERSION.SDK_INT > 29) context.getDir("screengrab", Context.MODE_PRIVATE)`. Đoạn mã này ép ghi ảnh vào thư mục riêng tư `/data/user/0/<package>/app_screengrab/`.
2. **Hạn chế của `adb pull` & sự cố Pipe trên Windows:**
   - Trên các thiết bị thương mại không root, `adb pull` không có quyền đọc trực tiếp từ thư mục riêng tư `/data/user/0/...` do cơ chế bảo mật Scoped Storage và sandbox của Android.
   - Khi `adb pull` bị `Permission denied`, Fastlane fallback sang pipeline: `adb shell run-as <pkg> "tar -cC <dir> <base>" | tar -xv ...`. Trên Windows, stdout qua pipe của `adb shell` mặc định chuyển đổi LF thành CRLF làm hỏng binary stream của file `.tar`, khiến tiến trình `tar` giải nén bị kẹt hoặc treo vô hạn.
3. **Quy ước thư mục Locale của Google Play Console:**
   Google Play Store sử dụng mã ngôn ngữ `vi` (chứ không phải `vi-VN`) làm thư mục metadata cho Tiếng Việt (`fastlane/metadata/android/vi/`). Nếu để `locales(['vi-VN', ...])`, Screengrab sẽ tạo thư mục mới `vi-VN` thay vì đồng bộ vào `vi`.

### Giải pháp / Rule mới:
1. **Tạo `ExternalStorageScreenshotCallback`:**
   Implement `ScreenshotCallback` tùy biến, chuyển hướng lưu toàn bộ ảnh chụp vào thư mục dữ liệu ngoài của ứng dụng: `appContext.getExternalFilesDir("screengrab")` (tương ứng với `/sdcard/Android/data/<package>/files/screengrab/<locale>/images/screenshots/`).
   - Thư mục này được Android cấp quyền tự động cho ứng dụng mà không cần xin quyền runtime storage nguy hiểm.
   - `adb pull` có toàn quyền đọc từ thư mục `/sdcard/Android/data/<package>/files/` mà không bị `Permission denied`.
   - Screengrab runner trong Fastlane tự động ưu tiên quét đường dẫn external storage này trước. Vì `adb pull` thành công 100%, Fastlane không bao giờ kích hoạt fallback pipeline `tar` bị lỗi trên Windows.
2. **Chuẩn hóa Screengrabfile:**
   - Đặt `locales(['vi', 'en-US'])` khớp chuẩn 100% với cấu trúc Play Store metadata directory.
   - Đặt `use_timestamp_suffix(false)` để sinh tên file ảnh sạch sẽ (`1_alarm_list.png` đến `6_alarm_message.png`), tự động ghi đè và cập nhật chính xác bộ ảnh Play Store.

---

## [2026-09-29] Bài Học Về Quản Lý State Form Nhập Liệu, Khóa Chính Room Cho Child Items & Âm Lượng Cảnh Báo
### Vấn đề / Task:
Trong quá trình phát triển tính năng Nhắc nhở (Reminders):
1. Người dùng phàn nàn âm lượng chuông nhắc nhở không đổi theo cấu hình (`volume`), chuông bị mặc định và không rung.
2. Danh sách checklist khi tạo mới hoặc sửa đôi khi bị mất mục hoặc không lưu được.
3. Người dùng nhập nội dung vào ô checklist nhưng quên nhấn nút (+) mà nhấn thẳng nút Lưu trên TopBar dẫn đến mất dữ liệu vừa gõ.
4. Nhu cầu tự động xóa dấu tick (reset completion) khi bước sang ngày mới cho các nhắc nhở lặp lại.

### Phân tích nguyên nhân:
1. **Xung đột Khóa chính Room (PrimaryKey Conflict) khi dùng timestamp giả lập:**
   - Trong ViewModel, các checklist item chưa lưu được tạo với `id = System.currentTimeMillis()`. Khi người dùng thêm nhiều item liên tiếp trong cùng 1 mili-giây hoặc khi lưu nhiều item cùng lúc, các item có cùng ID.
   - Lệnh `@Insert(onConflict = OnConflictStrategy.REPLACE)` của Room sẽ coi các item cùng ID là cùng một bản ghi và ghi đè lên nhau, chỉ còn lại 1 item duy nhất trong database.
2. **Hiện tượng "Bỏ quên Draft Text" khi Submit Form:**
   - Người dùng có thói quen: gõ xong chữ trong ô nhập liệu rồi lập tức đưa ngón tay lên góc trên màn hình bấm icon "Lưu" (Save).
   - Nếu luồng logic chỉ đọc mảng `checklist` hiện tại mà không gom giá trị đang lưu trong `draftText`, dữ liệu của người dùng sẽ bị biến mất âm thầm, gây ức chế cao.
3. **Âm lượng và Audio Stream của Notification vs Alarm:**
   - `RingtoneManager` hoặc `MediaPlayer` nếu không được set volume theo tỉ lệ `0.0f .. 1.0f` qua `setVolume()` thì sẽ luôn phát ở mức âm lượng hệ thống mặc định.
   - Notification Sound nếu không được gắn đúng `AudioAttributes.USAGE_ALARM` khi người dùng chọn chế độ báo động (Persistent Alert) sẽ bị tắt tiếng khi thiết bị ở chế độ im lặng/rung.

### Giải pháp / Rule mới:
1. **Quy tắc Sinh Khóa Chính cho Child Entities trong Room:**
   - Khi lưu một entity cha mới (hoặc các child items mới được thêm vào), LUÔN LUÔN gán `id = 0L` cho child items trước khi gọi DAO `insertChecklistItems()`. Điều này kích hoạt cơ chế `autoGenerate = true` của SQLite, bảo đảm mỗi bản ghi có ID duy nhất tuyệt đối.
   - Trong UI State / ViewModel tạm thời, nếu cần ID làm key cho LazyColumn, sử dụng `System.nanoTime()` hoặc ID âm giảm dần (`-1L, -2L...`) thay vì `currentTimeMillis()`.
2. **Quy tắc "Flush On Submit" cho Form Nhập Liệu:**
   - Bất cứ form nhập liệu nào có ô nhập draft (như checklist, tags, keywords), trong hàm `save()` hoặc `submit()` của ViewModel PHẢI LUÔN LUÔN tự động kiểm tra: nếu `draftText.isNotBlank()`, tự động thêm vào danh sách trước khi thực hiện lưu dữ liệu.
3. **Quy tắc Tự Động Reset Theo Chu Kỳ (Cycle Reset Rule):**
   - Lưu trữ dấu mốc `lastChecklistResetTime` trên thực thể Reminder.
   - Khi reminder kích hoạt chu kỳ mới (hoặc khi app mở lên / sync data), so sánh thời gian hiện tại với `lastChecklistResetTime` dựa trên quy tắc chu kỳ (`ChecklistCycleUtil`: cùng ngày, cùng tuần, cùng tháng hay cùng năm). Nếu đã sang chu kỳ mới, thực hiện `resetChecklistCompletionForReminder` đồng thời cập nhật `lastChecklistResetTime = System.currentTimeMillis()`.
4. **Quy tắc Quản Lý Âm Lượng Nhắc Nhở:**
   - Luôn quy đổi volume `0..100` thành `0.0f..1.0f` và áp dụng trực tiếp cho `MediaPlayer.setVolume(vol, vol)`.
   - Kết hợp `AudioAttributes.USAGE_ALARM` cho cả âm thanh và `Vibrator` khi nhắc nhở yêu cầu báo động ưu tiên cao.

