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
     - `ColorPaletteSelectionDialog`: Thiết kế dạng lưới tròn **4 cột x 2 hàng** (Pixel/Material 3 style) cực kỳ nhỏ gọn, gồm chấm màu tròn 48dp, viền nổi bật (2.5dp) & dấu tích `Check` khi được chọn, cùng tên màu ngắn gọn phía dưới. Giảm 60% chiều cao so với danh sách dọc, không còn chiếm diện tích màn hình.
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

