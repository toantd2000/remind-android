## [2026-09-21] Decoupling Network Loading from Async AI Processing & Stale Cache Eviction

### Context
Khi triển khai chiến lược tải dữ liệu Cache-First trên `TodayScreen` nhằm giảm thiểu lượt gọi API dư thừa, màn hình rơi vào tình trạng loading vô tận (Infinite Loading Deadlock), nút refresh bị khóa và thời tiết không tự động làm mới khi hết hạn.

### What happened
- Gộp trạng thái tải mạng (`isRefreshing`) và trạng thái AI đang phân tích dữ liệu ở backend (`isProcessing`) thành một biến `isBusy = isRefreshing || isProcessing`.
- Khi API trả về `aiStatus == "processing"`, `isProcessing` chuyển sang `true`, dẫn đến:
  1. Nút refresh ở TopAppBar bị disable (`enabled = !isBusy = false`) và xoay mãi (`loading = isBusy = true`). Người dùng không thể can thiệp.
  2. Dữ liệu tạm thời có `aiStatus == "processing"` được lưu vào DataStore. Do cơ chế Cache-First kiểm tra `currentTime - lastUpdated < 3600000`, mỗi khi mở lại app, Repository coi dữ liệu vừa lưu là hợp lệ và return sớm, không gọi API, khiến dữ liệu bị kẹt vĩnh viễn ở trạng thái `processing`.
  3. Khi quay lại app từ nền (`ON_RESUME`), hàm lắng nghe sự kiện chỉ kích hoạt nếu `isProcessing == true`, khiến dữ liệu thời tiết đã quá hạn 1 tiếng không bao giờ được tự động làm mới.

### Solution & Lessons Learned
- **Tách biệt rõ ràng giữa Network IO State và Business Content State:**
  Không bao giờ gộp trạng thái loading của tầng mạng (`isRefreshing`) với trạng thái xử lý logic bất đồng bộ kéo dài của nghiệp vụ (`isProcessing`). Nút Refresh UI chỉ nên biểu thị `isRefreshing`. Ngay cả khi AI backend đang xử lý, người dùng vẫn phải có quyền chủ động bấm Refresh để thử lại.
- **Dữ liệu tạm thời (In-Flight / Processing) không được coi là Cache hoàn chỉnh:**
  Trong tầng Repository, điều kiện hợp lệ của Cache không chỉ dựa vào Timestamp (TTL) mà bắt buộc phải kiểm tra tính toàn vẹn của dữ liệu (`!isProcessing`). Dữ liệu mang trạng thái đang xử lý dở dang phải cho phép tiếp tục gọi API để lấy kết quả hoàn tất.
- **Tự động làm mới khi hết hạn qua Lifecycle onResume:**
  Tại sự kiện `ON_RESUME`, gọi `refresh(force = false)`. Nhờ cơ chế Cache-First, thao tác này tuyệt đối không tốn tài nguyên mạng nếu dữ liệu vẫn còn trong hạn, nhưng sẽ tự động cập nhật ngay khi TTL hết hạn hoặc sang ngày mới.
- **Tự động thăm dò (Auto Polling) có giới hạn:**
  Đối với các tác vụ AI mất nhiều thời gian ở backend, cần bổ sung cơ chế polling nền (ví dụ: sau mỗi 10 giây, tối đa 6 lần) để tự động cập nhật UI khi backend xử lý xong mà không phụ thuộc hoàn toàn vào hành vi của người dùng.

## [2026-09-17] Compose Configuration.uiMode Synchronization for Dynamic Theming & Previews

### Context
Ảnh minh họa trạng thái rỗng (`no_alarm_illustration.png`) trong `EmptyState` (`AlarmListScreen`) có 2 biến thể: `drawable/` (Light) và `drawable-night/` (Dark). Khi đổi Theme nội bộ trong ứng dụng hoặc xem trong Preview Dark Mode, ảnh không tự động thay đổi theo theme.

### What happened
- Trong Jetpack Compose, hàm `painterResource(id)` tra cứu drawable dựa trên Android `LocalConfiguration.current` (cụ thể là `configuration.uiMode`).
- Khi người dùng thay đổi `themeMode` ("LIGHT" hoặc "DARK") trong cài đặt ứng dụng, giá trị boolean `darkTheme` được truyền vào `ReMindTheme`, nhưng `LocalConfiguration.uiMode` của `Context` vẫn giữ nguyên cấu hình hệ thống nếu không được đồng bộ.
- Dẫn đến việc `painterResource` tiếp tục tải ảnh theo chế độ sáng/tối của hệ điều hành thay vì theme mà người dùng đã chọn trong app.
- Tương tự, `@Preview` mặc định không đặt `uiMode = Configuration.UI_MODE_NIGHT_YES` nên luôn hiển thị ảnh từ thư mục `drawable/` mặc định.

### Solution & Lessons Learned
- **Compose `LocalConfiguration` Synchronization:**
  Trong `ReMindTheme` (`:core:designsystem`), clone `LocalConfiguration.current` và ghi đè cờ `uiMode` (`UI_MODE_NIGHT_YES` / `UI_MODE_NIGHT_NO`) dựa trên cờ `darkTheme`, sau đó cung cấp lại qua `CompositionLocalProvider(LocalConfiguration provides themedConfiguration)`.
  Giải pháp này giúp toàn bộ cây Composable bên dưới, bao gồm mọi hàm gọi `painterResource(...)`, tự động phản hồi tức thì với theme mà không cần can thiệp thủ công ở từng màn hình hay phụ thuộc vào Activity recreation.
- **Dual-Mode Previews for Theme-Dependent Drawables:**
  Luôn khai báo cả hai biến thể Preview (Light và Dark với `uiMode = Configuration.UI_MODE_NIGHT_YES` và `darkTheme = true`) cho các Composable sử dụng tài nguyên drawable phân nhánh theo night mode.

## [2026-09-14] Litever Palette Theming & 8-Cell Palette Grid Implementation

### Context
Cập nhật hệ thống màu sắc theo chuẩn Litever Design System: đổi màu mặc định của toàn bộ ứng dụng sang màu Đỏ (RED) và thiết kế lại bộ chọn màu sắc tại `GeneralSettingsScreen` thành lưới 8 ô chia 2 hàng (gồm 7 màu định sẵn của Litever Palette và 1 tùy chọn màu theo màn hình).

### What happened
- Đổi màu mặc định của ứng dụng sang `RED` trên toàn bộ các tầng: DataStore preferences, SettingsUiState, ReMindTheme và MainActivity collection.
- Ánh xạ trực tiếp 7 bảng màu `LiteverThemeColor` (`RED`, `ORANGE`, `YELLOW`, `GREEN`, `BLUE`, `INDIGO`, `VIOLET`) và Android 12+ wallpaper dynamic color vào `LiteverTheme`.
- Cập nhật thư viện `litever-designsystem` để hàm `LiteverThemeColor.DEFAULT` trỏ về bảng màu Đỏ (`redDarkColorScheme` / `redLightColorScheme`), đồng thời cung cấp hàm tiện ích `lightLiteverColors` và `darkLiteverColors`.
- Tái thiết kế mục chọn màu tại `GeneralSettingsScreen` bằng 2 hàng x 4 ô sử dụng `IconButton` với nền `primaryContainer`, text `primary` căn giữa, bo góc squircle chuẩn Litever và trạng thái nhận biết màu đang chọn bằng viền `primary` 2.dp và biểu tượng `Icons.Rounded.Check`.
- Toàn bộ unit tests chạy thành công 100%.

### Lessons Learned
- **Compose Multi-Palette State Synchronization:**
  Khi hỗ trợ nhiều bảng màu trong Compose, việc delegate trực tiếp sang Design System theme composable (`LiteverTheme(themeColor = ...)`) là giải pháp tối ưu nhất. Tránh việc tính toán hoặc giữ state màu sắc thủ công cục bộ tại từng màn hình; hãy để Compose runtime tự động kích hoạt recomposition toàn app khi `colorPalette` State thay đổi từ DataStore.
- **IconButton Layout Constraints in Dynamic Grids:**
  Mặc định `IconButton` của Material 3 có kích thước cố định `40.dp`. Khi đặt trong các lưới co giãn (dynamic weighted grid cells `Modifier.weight(1f)`), cần bọc `IconButton` trong một `Box` có chiều cao chuẩn (ví dụ `52.dp` để thỏa mãn touch target accessibility >= 48dp) và áp dụng `fillMaxSize()` kèm bo góc và màu nền tương ứng. Việc này giúp các nút co giãn linh hoạt trên mọi kích cỡ màn hình từ điện thoại nhỏ đến tablet/foldable mà không bị vỡ layout.

## [2026-09-12] Package Segregation Migration & Boilerplate Reduction via Opinionated Lv* Components

### Context
Nâng cấp thư viện `litever-designsystem` từ phiên bản `2.0.0` lên `2.1.0` trong ứng dụng `remind-android`. Phiên bản 2.1.0 giải quyết bài toán cấu trúc package bằng cách tách thành các sub-packages theo từng domain (`components.button.*`, `components.textfield.*`, `components.chip.*`, `components.dialog.*`, `components.snackbar.*`, `components.core.*`), đồng thời giới thiệu bộ thành phần Opinionated thế hệ mới `Lv*` (`LvButton`, `LvIconButton`, `LvTextField`, `LvAlertDialog`, `LvChip`, `LvSnackbarHost` / `LvSnackbar`). Mục tiêu là chuyển đổi từ mô hình Component Defaults thủ công (v2.0.0) sang các Opinionated components để cắt giảm tối đa boilerplate code mà vẫn đảm bảo tính chuẩn xác của hệ thống Design Tokens.

### What happened
- Sau khi nâng cấp version catalog lên `2.1.0`, các import cũ dạng `vn.io.litever.designsystem.components.LiteVerButtonDefaults` và `LiteVerTextFieldDefaults` bị gãy do các lớp này đã được chuyển vào các sub-package `components.button` và `components.textfield`.
- Tiến hành rà soát và cập nhật đồng bộ các import trên toàn bộ 6 modules (`:core:designsystem`, `:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, `:app`).
- Chuyển đổi toàn diện các composables M3 nguyên thủy kèm cấu hình defaults rườm rà sang bộ thành phần `Lv*`:
  - Thay thế `Button`/`OutlinedButton` + `LiteVerButtonDefaults` bằng `LvButton(type, semantic)`.
  - Thay thế `IconButton` bằng `LvIconButton(semantic)`.
  - Thay thế `OutlinedTextField` + `LiteVerTextFieldDefaults` bằng `LvTextField` (tự động hỗ trợ `label`, `placeholder`, `errorMessage`, `type`, `semantic`).
  - Thay thế `AlertDialog` bằng `LvAlertDialog` với bo góc squircle 10.dp.
  - Tích hợp `LvSnackbarHost` vào `MainActivity` scaffold.
  - Bổ sung các tokens `neutral` container vào `Color.kt` của `:core:designsystem`.
- Toàn bộ dự án biên dịch sạch sẽ 100% và vượt qua toàn bộ unit test suites.

### Lessons Learned
- **Package Segregation Migration Strategy:**
  Khi một thư viện design system phát triển, việc tách package phẳng thành các namespace theo domain chức năng (`components.button.*`, `components.textfield.*`, `components.dialog.*`) là xu hướng tất yếu để hỗ trợ tree-shaking (R8) và tăng tính tổ chức của codebase. Để quá trình migration diễn ra êm đẹp, cần tuân thủ quy trình: cập nhật version catalog -> cập nhật module adapter `:core:designsystem` -> cascade xuống các feature modules độc lập. Việc này giúp khoanh vùng lỗi import và ngăn chặn lỗi lan truyền trên toàn bộ build graph.
- **Boilerplate Reduction via Opinionated Lv* Components:**
  Ở v2.0.0, việc dùng trực tiếp M3 Composable kết hợp `LiteVerButtonDefaults` đã giải phóng ứng dụng khỏi các pass-through wrapper, nhưng lại tạo ra lượng boilerplate code rất lớn ở từng call-site (phải liên tục truyền `shape`, `colors`, `border`, `contentPadding`). Bộ thành phần Opinionated `Lv*` trong v2.1.0 đạt được điểm cân bằng hoàn hảo: vẫn tái sử dụng Material 3 Composable bên dưới (bảo toàn 100% ripple effect, state layers, accessibility, và gestures), nhưng cung cấp API cấp cao với tham số `type` và `semantic`. Nhà phát triển không còn phải thủ công ghép nối màu sắc hay bo góc, giúp giảm 60% code UI và triệt tiêu nguy cơ bất đồng bộ visual.
- **Ergonomic Text Field Design & Safe State Binding:**
  `LvTextField` đem lại trải nghiệm lập trình (DX) vượt trội nhờ hỗ trợ trực tiếp `label: String?` và `placeholder: String?` thay vì bắt buộc truyền Composable lambdas cồng kềnh. Tuy nhiên, khi áp dụng vào các giao diện tương tác phức tạp (như ô nhập kết quả tính toán `MathMissionContent` cần bàn phím số và phím Done, hoặc `LocationSearchScreen` cần nút xóa nhanh nội dung), cần chú ý truyền trailing icon dưới dạng lambda rõ ràng (`trailingIcon = if (...) { { ... } } else null`) để đảm bảo an toàn kiểu dữ liệu và tránh tái kích hoạt recomposition không cần thiết.
- **Standardized Dialogs vs. Excessive M3 Rounding:**
  `AlertDialog` mặc định của Material 3 có bo góc lên tới 28.dp, tạo cảm giác quá tròn và không ăn nhập với triết lý thiết kế squircle của LiteVer. `LvAlertDialog` giải quyết triệt để vấn đề này bằng cách sử dụng `BasicAlertDialog` kết hợp bo góc 10.dp (`LiteverTheme.shapes.extraLarge`) và tự động bố trí các nút hành động `confirmButton`, `dismissButton` với kiểu dáng chuẩn hóa, giúp loại bỏ hoàn toàn mã nguồn tùy biến layout phức tạp tại các feature screens.

## [2026-09-11] Composite Build (includeBuild) & Lean Material 3 Design System Migration

### Context
Nâng cấp thư viện `litever-designsystem` lên phiên bản `2.0.0` trong ứng dụng `remind-android`. Phiên bản mới thực hiện triết lý Lean Architecture: loại bỏ 24 pass-through wrappers (như `LiteverButton`, `LiteverScaffold`, `LiteverTopAppBar`, `LiteverTextField`, `LiteverCard`, v.v.) và chuyển dịch sang việc dùng trực tiếp Jetpack Compose Material 3 (M3) kết hợp cùng `LiteVerButtonDefaults`, `LiteVerTextFieldDefaults` và hệ thống token khoảng cách `LiteverTheme.spacing`. Đồng thời dự án tích hợp mã nguồn thư viện cục bộ qua Gradle Composite Build (`includeBuild("../litever-designsystem")`).

### What happened
- Tích hợp thành công `includeBuild("../litever-designsystem")` trong `settings.gradle.kts` kết hợp cấu hình `dependencySubstitution` thay thế artifact `com.github.toantd2000:litever-designsystem` trỏ về dự án cục bộ `:designsystem`.
- Nâng cấp `liteverDesignsystem = "2.0.0"` trong `gradle/libs.versions.toml`.
- Tái cấu trúc `:core:designsystem` thành adapter layer mỏng: tái triển khai `ReMindTopAppBar`, `ReMindAlertDialog`, `ReMindBottomBar`, `ReMindLoadingIconButton`, `ReMindSettingIcon`, đồng thời bổ sung `ReMindSettingsGroup`, `ReMindSettingsItem` và `ReMindTimePickerDialog` trên nền tảng Material 3 gốc.
- Refactor toàn diện tất cả các feature modules (`:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`) và `:app`, thay thế hoàn toàn các wrapper cũ bằng M3 native composables và thay thế 100% khoảng cách hardcode `.dp` bằng `LiteverTheme.spacing`.
- Xử lý xung đột insets giữa Root Scaffold tại `:app` và các Nested Scaffolds tại các màn hình con.
- Bổ sung bộ unit test toàn diện cho logic UI tại `:features:mission` và `:features:today`, đảm bảo 100% build và test vượt qua sạch sẽ.

### Lessons Learned
- **Composite Build Dependency Substitution:**
  Khi phát triển song song giữa một ứng dụng chính và một thư viện design system độc lập, cơ chế Gradle Composite Build (`includeBuild`) là giải pháp vượt trội so với việc `mavenLocal()` hay publish các bản SNAPSHOT. Bằng cách khai báo `dependencySubstitution { substitute(module("com.github.toantd2000:litever-designsystem")).using(project(":designsystem")) }` trong `settings.gradle.kts`, Gradle tự động chuyển hướng mọi khai báo dependency dạng maven coordinates sang project con cục bộ một cách trong suốt. Điều này cho phép hot-reloading code của thư viện ngay trong Android Studio, kiểm tra tức thì các thay đổi API mà không gây ô nhiễm Gradle cache hay xung đột phiên bản. Cần lưu ý rằng project được include phải có root project name hoặc module paths tương thích, và task graph của cả hai build sẽ được Gradle tự động giải quyết đồng thời.
- **Lean M3 Wrapper Elimination & Component Defaults Pattern:**
  Tạo pass-through wrappers cho mọi component Material Design (ví dụ tạo `LiteverButton` bọc `androidx.compose.material3.Button`) thoạt nhìn có vẻ giúp kiểm soát design system tập trung, nhưng trên thực tế tạo ra món nợ kỹ thuật (technical debt) khổng lồ: mỗi khi Material 3 cập nhật thêm tham số mới (interactionSource, contentPadding, custom elevation, state layers...), wrapper buộc phải cập nhật theo hoặc làm mất đi tính linh hoạt của Jetpack Compose. Mô hình Component Defaults (`LiteVerButtonDefaults`, `LiteVerTextFieldDefaults`) giải quyết triệt để nghịch lý này: nhà phát triển sử dụng trực tiếp composable chuẩn M3 (`Button`, `OutlinedTextField`, `Scaffold`), đồng thời truyền các preset defaults (`colors = LiteVerButtonDefaults.primaryColors()`, `shape = LiteVerTextFieldDefaults.shape`). Cách tiếp cận này giữ trọn 100% API surface của Material 3, loại bỏ overhead của các wrapper tầng tầng lớp lớp, và tối ưu hóa recomposition.
- **Window Insets & Nested Scaffolds Inset Consumption:**
  Trong kiến trúc Compose hiện đại hỗ trợ Edge-to-Edge, `Scaffold` mặc định sẽ tiêu thụ (consume) Window Insets thông qua thuộc tính `contentWindowInsets`. Khi ứng dụng có một Root Scaffold ở `:app` (để hiển thị BottomBar hoặc FloatingActionButton) và bên trong các màn hình con (Feature Screens) lại khai báo thêm một `Scaffold` riêng (để gắn TopAppBar hoặc Floating Action Button cục bộ), xảy ra hiện tượng Scaffold con tiêu thụ insets thêm một lần nữa hoặc nhận `paddingValues` bị nhân đôi (double padding bug). Giải pháp chuẩn xác là: Root Scaffold xử lý insets tổng thể, còn các `Scaffold` con bên trong các feature screens phải thiết lập rõ ràng `contentWindowInsets = WindowInsets(0, 0, 0, 0)` hoặc chỉ định tiêu thụ insets có chọn lọc (`WindowInsets.safeDrawing.only(...)`), đảm bảo `paddingValues` được áp dụng chính xác cho nội dung cuộn mà không bị hở khoảng trống kỳ dị trên các thiết bị có tai thỏ / navigation bar.
- **Spacing Token Discipline vs. Technical Border Strokes:**
  Việc tùy tiện hardcode các giá trị pixel độc lập (`8.dp`, `12.dp`, `16.dp`, `24.dp`) rải rác trong hàng chục màn hình UI dẫn đến sự phân mảnh giao diện và khó khăn khi muốn thay đổi mật độ hiển thị (layout density). Khi chuyển đổi sang Design System tokens, bắt buộc phải áp dụng triệt để `LiteverTheme.spacing.*` (`tiny` = 2dp, `extraSmall` = 4dp, `small` = 8dp, `smallMedium` = 12dp, `medium` = 16dp, `large` = 24dp, v.v.). Tuy nhiên, cần có sự phân biệt rạch ròi mang tính kỹ thuật: các giá trị như `1.dp` hoặc `2.dp` dùng cho độ dày nét vẽ viền (`BorderStroke(1.dp, color)`) hoặc dải chia ranh giới (`HorizontalDivider(thickness = 1.dp)`) là kích thước đồ họa (graphical stroke weight), KHÔNG phải là khoảng cách bố cục (layout spacing). Việc ép các stroke weight này thành spacing token sẽ làm sai lệch ngữ nghĩa. Spacing tokens chỉ áp dụng cho padding, margin, contentPadding, và `Arrangement.spacedBy(...)`.

## [2026-05-30] Smooth List Reordering with animateItem

### Context
When toggling an alarm, its position in the list changes (due to sorting logic in the ViewModel), but the transition was abrupt.

### What happened
- The `LazyColumn` items were being re-composed in their new positions instantly, which was jarring for the user.
- Even though `key` was provided, Compose doesn't automatically animate the movement of items unless explicitly told to.

### Lessons Learned
- **Item Placement Animation:** In Jetpack Compose (Modern versions/BOM 2024+), `Modifier.animateItem()` is the standard way to animate an item's movement, appearance, and disappearance within a `LazyColumn` or `LazyGrid`.
- **Requirement for Keys:** `animateItem()` only works if the `items` block has a stable `key` defined. This allows the internal `LazyLayout` to track the identity of the item across different scroll positions and sorting orders.

## [2026-05-30] Ripple Leak & Content Clipping in Custom Cards


### Context
`AlarmCard` was showing a ripple effect (click animation) that leaked outside its rounded corners. Additionally, the left accent bar was not following the card's rounded corners.

### What happened
- The card used a `.clickable()` modifier on the root `LiteverCard`. In Compose, applying `.clickable()` outside a Surface/Card that has its own `shape` can sometimes result in the ripple being drawn to the rectangular bounds of the modifier rather than the clipped shape of the component, especially if the component doesn't handle the interaction internally.
- The inner content (the vertical accent bar) was a simple `Box` without clipping, so it remained rectangular even though the parent Card was rounded, causing it to "peek out" at the top-left and bottom-left corners.

### Lessons Learned
- **Internal Interaction Handling:** When using Material 3 `Card` or similar components that provide an `onClick` parameter, always prefer using that internal parameter over applying an external `.clickable()` modifier. The internal implementation is designed to handle the interaction (ripple, state layers) correctly within the component's defined `shape`.
- **Explicit Content Clipping:** If a container has rounded corners (like a Card) and contains children that are aligned to its edges (like a background strip or accent bar), the container's *content* must be explicitly clipped to the same shape using `.clip(shape)` to ensure the children don't bleed out of the rounded bounds.

## [2026-05-26] Jetpack Compose Dialog & stringResource Localization Bug

### Context
User reported that "Support Developer" dialog and other nested dialogs were displaying strings in the wrong language (system default language instead of in-app selected language).

### What happened
- The app uses a custom `CompositionLocalProvider(LocalContext provides localizedContext)` to apply the user's selected language dynamically without recreating the Activity.
- Jetpack Compose's `androidx.compose.ui.window.Dialog` (which `AlertDialog` and `LiteverDialog` use under the hood) creates a new Android `Window` using the `Activity`'s base context (`LocalView.current.context`), which resets `LocalContext` inside the dialog to the system locale.
- Because the functional parameters of `LiteverDialog` (`text = { ... }`, `confirmButton = { ... }`) are composable lambdas evaluated *inside* the Dialog's Composition tree, any `stringResource()` calls inside them read the system language instead of the app's `localizedContext`.

### Lessons Learned
- **Dialog Context Override:** In Jetpack Compose, any Composable lambda executed inside a `Dialog` loses custom `LocalContext` or `LocalConfiguration` provided at the app root level because the Dialog creates a new View backed by the Activity's context.
- **Pre-resolving Strings:** To fix localization issues in Dialogs without propagating `CompositionLocal`s globally to all windows, **always resolve `stringResource(...)` outside the Dialog scope** and pass the resolved `String` variable into the lambda.

## [2026-05-19] Trailing Lambda Resolution & Device-Specific Simulation Fallbacks### Context
Upgrading `ExitAppDialog` and `SupportDeveloperDialog` based on revised design guidelines: removing close buttons, removing rewarded-ad triggers in exit dialogues, limiting rewarded ad counts/simulation exclusively to emulators, and prompting users to interact with native ads in the exit message to support.

### What happened
- Modified `SettingsScreen.kt` to pass the `text = { ... }` body explicitly as a named parameter inside `LiteverDialog`. This fixed a Kotlin compilation type mismatch where the compiler mistakenly resolved the trailing lambda as a default parameter (`properties: DialogProperties`).
- Restructured `SettingsScreen.kt` using `DeviceUtils.isEmulator()` to gate `showRewardedAdSimulator = true`. On physical devices, it shows a non-disruptive native Toast (`rewarded_ad_not_ready`) warning instead.
- Revised `exit_dialog_message` inside `strings.xml` resource files (both English and Vietnamese) to ask the user to support us by interacting with the native ad placed directly below.

### Lessons Learned
- **Kotlin Trailing Lambda Ambiguity**: In Kotlin, when calling a Composable (or function) that contains multiple optional/nullable functional parameters (such as `title`, `text` in `LiteverDialog`), if you pass a trailing lambda block at the end, the compiler may struggle to resolve which functional parameter it maps to or will map it to the first non-functional/default parameter that it can match. To ensure compiler safety and avoid type mismatch errors, **always pass the functional block explicitly as a named parameter** (e.g. `text = { ... }`).
- **Device-Specific Simulation**: Presenting simulated interfaces (like a fake ad progress loader) is excellent for developers on emulators but can look deceptive or broken to real users on physical devices if AdMob fails to load a real ad. Gating the simulator via `DeviceUtils.isEmulator()` and presenting a standard, polite fallback notice (Toast) on real hardware maintains application integrity and professional UX.
- **Explicit Ad Engagement Messages**: When a free tier app uses non-intrusive native ads, advising users explicitly about how they can support the developer (e.g., *"Please interact with the ad below to support us"* / *"Hãy tương tác với quảng cáo phía dưới để ủng hộ chúng tôi nhé"*) is a transparent and highly effective strategy compared to ambiguous support requests.

## [2026-05-18] Settings Restructure & Rewarded Ad Supporter

### Context
Reorganizing the settings tab into three distinct groups, adding the Support Developer Card with premium gradient background, integrating AdMob Rewarded Ads and simulator fallback countdown, and upgrading `ExitAppDialog` by replacing native ads with a rewarded ad supporter button.

### What happened
- Modified `SettingsScreen.kt` to restructure the navigation layout.
- Added `SupportDeveloperCard` with high-aesthetic modern gradient style.
- Created `RewardedAdSimulatorDialog` countdown loader as a cached/offline ad fallback.
- Added dynamic ad-free period (`ADS_DISABLED_UNTIL`) to DataStore and observed it in `AdMobManagerImpl` to instantly block all other native/banner ad loadings.
- Upgraded `ExitAppDialog.kt` to present "Watch ad to support" button, launching simulator/real ad and keeping user in app.
- Fixed a modularization compilation error by adding `:core:datastore` dependency to `:core:ads:impl`.
- Standardized `AdManager` mock instances `PreviewAdManager` by implementing the new `isAdLoaded` method.

### Lessons Learned
- **Clean Architecture Modularization & Dagger Hilt**: When injecting preferences or database classes (from `:core:datastore` / `:core:database`) into an implementation module (like `:core:ads:impl`), always ensure the module's `build.gradle.kts` explicitly declares that project dependency. Hilt/KSP will fail compilation with unhelpful code generation errors if the dependency is missing in the classpath of the module where the injection occurs.
- **Interface Compatibility & Mocks**: When extending a core interface that has multiple mock/preview implementations (like `PreviewAdManager` in Compose previews), remember to immediately implement the default/override methods in all previews. Failing to do so breaks Compose previews in other independent feature modules (like `:features:remind` and `:features:alarms`).
- **Dynamic Ad Suppression Strategy**: Integrating ad-free suppression at the core network/ad manager implementation level is far more stable than trying to handle visibility conditionally on every single screen. By intercepting all `loadNativeAd` and `loadBannerAd` calls instantly in `AdMobManagerImpl` when the ad-free period is active, we guarantee zero flickering and prevent empty ad placeholders.

## [2026-05-09] Release v1.1.3 & AI Lifecycle Management

### Context
Integrating real-time AI status tracking for weather/reminder hints and implementing a smart refresh mechanism to handle asynchronous AI processing.

### What happened
- Added `aiStatus` to domain models and tracked processing states.
- Implemented `checkAndRefreshIfProcessing` logic in `RemindViewModel` to poll for updates.
- Refined UI to group reminders into a single card for a cleaner dashboard.

### Lessons Learned
- **AI UX Transparency**: Users are more patient with AI latency when they see a clear "processing" status rather than a static or empty state.
- **Polling Strategy**: Using a time-based threshold (`lastProcessingRefreshMillis`) prevents excessive network calls while ensuring the UI stays fresh when the user returns to the app or after a short delay.
- **UI Consolidation**: Grouping related features (like weather and reminders) into a unified card reduces visual noise and cognitive load, especially when using complex layouts like M3 Surface cards.

## [2026-05-08] Release v1.1.2 & Changelog Localization

### Context
Preparing for release v1.1.2. Requirement to support English changelog in-app and ensure database stability.

### What happened
- Updated `UpdateHistoryScreen.kt` to handle multi-language notes in `changelog.json`.
- Implemented a fallback mechanism: default to English if the current language is not Vietnamese.
- Verified that no database schema changes were made since v1.1.1, so no version bump was needed.
- Centralized release notes in both `CHANGELOG.md` and `changelog.json`.

### Lessons Learned
- **Scalability of Data Classes**: When designing metadata files like `changelog.json`, using a `Map<String, T>` for localized strings is more flexible than hardcoded fields.
- **Room Migration Hygiene**: Always verify entity changes against the last tagged version before deciding on a migration. No-op migrations are better than accidental data loss, but no bump is cleaner if truly no changes occurred.

## [2026-05-07] Alarm Skip Logic & Sync Manager Consistency
- **Logic Ordering in Sync**: When a sync process performs both validation (e.g., missed alarm check) and cleanup (e.g., clearing expired flags), the validation **MUST** happen before the cleanup if it depends on the data being cleaned. Clearing a "skippedAt" flag before checking for missed alarms causes the system to "forget" the skip intent.
- **Unified Calculation**: Always use the most high-level calculation method (`getNextOccurrence`) that accounts for all modifiers (skips, snoozes) rather than raw calculations.

### Corrective Actions
- Fixed regression in `AlarmSyncManager.sync()` by moving `skippedAt` cleanup to the end of the alarm processing loop.
- Updated `AlarmSyncManagerTest` to verify both the correct skip handling and the eventual cleanup.

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

