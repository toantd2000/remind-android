# Nhật Ký Phát Triển Dự Án (Logwork)

Tài liệu này dùng để ghi vết (tracking) quá trình thực thi các tính năng, lịch sử thay đổi kiến trúc và các quyết định kỹ thuật quan trọng của Ứng dụng Báo Thức.

---

## 🚀 Trạng Thái Dự Án
- **Phase Hiện Tại:** Phase 8 (LiteVer 2.1.0 & Opinionated Lv* Components Adoption)
- **Tiến Độ:** 100%
- **Ngày cập nhật cuối:** 2026-09-12
- **Phiên bản hiện tại:** 1.1.7 (Build 10)



## 📍 Lộ Trình Tổng Thể
- [x] **Phase 1:** Hoàn thiện Tầng Dữ liệu (Room DB & Repository)
- [x] **Phase 2:** Cơ Chế Lập Lịch Báo Thức (AlarmManager & Receivers)
- [x] **Phase 3:** Giao diện Người dùng (CRUD Alarm List & Create)
- [x] **Phase 4:** Trải Nghiệm Màn Hình Chuông & Tối ưu UX
- [x] **Phase 5:** Tách Module Design System & Chuẩn hóa Đa dự án (Modularization)
- [x] **Phase 6:** Tối ưu hóa & Bảo trì (Maintenance & Optimization)
- [x] **Phase 7:** Nâng cấp thư viện LiteVer Design System 2.0.0 & Refactor toàn diện Material 3 (includeBuild & M3 Defaults)
- [x] **Phase 8:** Nâng cấp thư viện LiteVer Design System 2.1.0 & Chuyển đổi toàn diện sang bộ thành phần Opinionated Lv* (LvButton, LvIconButton, LvTextField, LvAlertDialog, LvChip, LvSnackbar)

---

## 🧠 Nhật Ký Quyết Định Kỹ Thuật (TDR)

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

