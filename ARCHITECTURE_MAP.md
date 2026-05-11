# Sơ Đồ Kiến Trúc Dự Án (Architecture Map)

Dự án ReMind được xây dựng theo kiến trúc **Multi-module** kết hợp với **Clean Architecture**, nhằm tối đa hóa khả năng tái sử dụng, cô lập lỗi và quản lý sự phụ thuộc (Dependency Management).

## 1. Tech Stack
- **UI:** Jetpack Compose (Modern Toolkit).
- **DI:** Hilt (Dagger) - Dependency Injection.
- **Data:** Room Database (Local Persistence) & DataStore (Preferences).
- **Background:** AlarmManager (Exact Alarms), Foreground Services.
- **Architecture:** MVVM + StateFlow + Navigation Compose.

## 2. Hệ Thống Module

| Module | Tầng | Vai trò |
| :--- | :--- | :--- |
| `:app` | Application | Module gốc, cấu hình Hilt App, quản lý Navigation Graph chính. |
| `:features:alarms` | Feature | Quản lý danh sách và thiết lập báo thức. |
| `:features:mission` | Feature | Xử lý logic và UI cho các nhiệm vụ (Typing, Math...). |
| `:features:remind` | Feature | Quản lý các nhắc nhở, ghi chú đi kèm báo thức. |
| `:features:settings` | Feature | Cài đặt ứng dụng, ngôn ngữ, định dạng thời gian. |
| `:core:alarm` | Infrastructure | **(Hạt nhân)** Quản lý Scheduler, Receiver và Service báo thức. |
| `:core:data` | Data | Triển khai Repository, kết nối Database và DataStore. |
| `:core:domain` | Domain | Chứa Interface, Model nghiệp vụ và các UseCase (Pure Kotlin). |
| `:core:model` | Domain Models | Các định nghĩa Data classes dùng chung toàn app. |
| `:core:database` | Data Source | Cấu hình Room Database, Entities và DAO. |
| `:core:datastore` | Data Source | Quản lý Preference-based settings (24h format, volume). |
| `:core:designsystem` | Design System | **(Adapter)** Lớp bọc cung cấp UI Components cho App ReMind. |
| `:litever-designsystem` | Library | **(Core)** Thư viện Design System nền tảng dùng chung cho nhiều App. |
| `:core:common` | Common Utils | Các lớp tiện ích, AudioPlayer, Dispatchers, Extensions dùng chung. |
| `:core:ads:api` | Infrastructure | Chứa Interface và Model cấu hình quảng cáo, không phụ thuộc third-party. |
| `:core:ads:impl` | Infrastructure | Chứa logic AdMob và kết nối Firebase Remote Config. |

---

## 3. Các Mô Hình Giao Tiếp Đặc Thù

### A. Dependency Inversion qua `AlarmIntentProvider`
Tại sao? Do `:core:alarm` (Service) không được phép phụ thuộc vào `:app` (Activity), nhưng Service lại cần mở Activity khi báo thức nổ.
- **Interface:** `AlarmIntentProvider` định nghĩa tại `:core:alarm`.
- **Implementation:** `AlarmIntentProviderImpl` thực thi tại `:app`, biết rõ `MainActivity`.
- **Kết quả:** Service gọi providers để lấy Intent tường minh (Explicit Intent) mà không vi phạm nguyên tắc phụ thuộc.

### B. Luồng Điều Hướng StateFlow-based
Luồng xử lý khi báo thức nổ:
1. `AlarmReceiver` -> `AlarmService` (đổ chuông).
2. `AlarmService` -> `AlarmRingManager.setRinging(alarmId)`.
3. `MainActivity` (Root) quan sát StateFlow của `AlarmRingManager`.
4. Nếu có `alarmId`, App tự động điều hướng sang `AlarmRingingRoute` thông qua `navController`. 
=> Điều này giúp App chủ động nhảy màn hình ngay cả khi đang ở Foreground mà không cần phụ thuộc vào việc nhấn Notification.

### C. Luồng Thêm Nhiệm Vụ (Add Mission Flow)
Để đảm bảo trải nghiệm người dùng liền mạch và linh hoạt, việc thêm nhiệm vụ được thực hiện qua sự kết hợp giữa Bottom Sheet và Navigation:
1. **Kích hoạt:** Người dùng nhấn "Add Mission" tại màn hình chỉnh sửa báo thức -> Hiển thị **Mission Selection Bottom Sheet**.
2. **Bước 1 (Selection):** Người dùng chọn một loại nhiệm vụ từ danh sách trong Bottom Sheet.
3. **Bước 2 (Configuration):**
    - Nếu nhiệm vụ cần cấu hình phức tạp (ví dụ: Gõ chữ), App sẽ điều hướng sang **màn hình cấu hình đầy đủ** (Full Screen).
    - **Lưu ý:** Bottom Sheet vẫn được giữ ở trạng thái "Open" tại màn hình chỉnh sửa báo thức (underlying screen).
4. **Điều hướng ngược (Back):** Nếu người dùng nhấn "Back" từ màn hình cấu hình, họ sẽ quay lại màn hình chỉnh sửa và **Bottom Sheet vẫn đang hiện**, cho phép chọn lại loại nhiệm vụ khác.
5. **Hoàn tất (Finish):** Khi nhấn "Save/Finish" tại màn hình cấu hình, App quay lại màn hình chỉnh sửa, tự động đóng Bottom Sheet và cập nhật danh sách nhiệm vụ mới.

---

## 4. Quy tắc phát triển (Development Rules)
- **Feature cô lập:** Không module Feature nào được phụ thuộc vào module Feature khác. Mọi giao tiếp qua `:core:domain` hoặc Navigation DeepLinks.
- **Explicit Intent:** Tuyệt đối không dùng Implicit Intent (Action string) để gọi Service/Receiver nội bộ. Luôn qua `AlarmIntentProvider` hoặc gọi trực tiếp class trong cùng module.
- **Compose Only:** Không dùng XML cho UI; mọi thành phần mới phải là Compose Function.
- **Dependency Flow:** Core -> Core (ngang hàng) là hạn chế, Feature -> Core (BẮT BUỘC), Feature -> Feature (CẤM).

## 5. Nhật ký rà soát (Review Log)
- **[2026-05-05] Resolved Coupling `:alarms` -> `:mission`**: Đã di chuyển `MissionSelectionBottomSheet` sang `:core:designsystem` và gỡ bỏ phụ thuộc trực tiếp giữa hai feature. Hệ thống hiện đảm bảo tính cô lập hoàn toàn.
