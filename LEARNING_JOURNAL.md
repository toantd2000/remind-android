# Learning Journal & Self-Correction Log

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
