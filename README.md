# ReMind Android

Ứng dụng Báo Thức & Nhắc Nhở hiện đại xây dựng trên nền tảng **Android Modern Stack**: Jetpack Compose, Clean Architecture, Multi-module, Kotlin Coroutines & Flow, Hilt và Room Database.

---

## 1. Tech Stack & Cấu Trúc Dự Án

- **Ngôn ngữ & Bất đồng bộ:** Kotlin 2.x, Coroutines & Reactive Flow.
- **UI & Design System:** Jetpack Compose (Material 3), kết nối composite build với thư viện nền tảng `litever-designsystem` (`includeBuild("../litever-designsystem")`).
- **Kiến trúc:** Clean Architecture + Multi-module (18 modules cô lập).
- **Dependency Injection:** Dagger Hilt.
- **Lưu trữ dữ liệu:** Room Database (SQLite với Migration tường minh) & DataStore Preferences.
- **Background Engine:** AlarmManager (`setExactAndAllowWhileIdle`), Foreground Services, BroadcastReceivers.
- **Bảo mật & Tối ưu:** R8 full-mode optimization, Intent/Permission hardening theo chuẩn Google.

### Sơ Đồ Hệ Thống Module

```
:app (Application entry point, Root navigation, DI Assembly)
 ├── :features:alarms      (CRUD báo thức, Quick Alarm, FAB menu)
 ├── :features:reminders   (CRUD nhắc nhở Fixed Time / Interval)
 ├── :features:mission     (Nhiệm vụ tắt chuông: Math, Typing)
 ├── :features:settings    (Cài đặt âm lượng, giao diện, Ad-free supporter)
 ├── :core:designsystem    (Adapter theme tokens & component wrappers)
 │    └── [includeBuild] :litever-designsystem (Thư viện UI chuẩn M3)
 ├── :core:alarm           (Hạt nhân Scheduler, AlarmReceiver, AlarmService)
 ├── :core:domain          (Pure Kotlin: UseCases, Repository interfaces)
 ├── :core:model           (Data models dùng chung toàn hệ thống)
 ├── :core:data            (Triển khai OfflineFirst repositories & mappers)
 ├── :core:database        (Room Database, Entities, DAOs, Migrations)
 ├── :core:datastore       (DataStore Preferences: 24h format, Ad-free token)
 ├── :core:network         (Retrofit, OkHttp, Kotlinx Serialization)
 ├── :core:common          (Dispatchers, AudioPlayer, Common utils)
 ├── :core:ads:api / :impl (Module hạ tầng AdMob & Remote Config)
 ├── :core:analytics       (Firebase Analytics & Crashlytics tracking)
 └── :core:testing         (Test rules, fake repositories, coroutine dispatchers)
```

---

## 2. Quy Tắc & Cơ Chế Cốt Lõi

1. **Dependency Inversion cho Alarm Ringing:**
   - `:core:alarm` (Service) kích hoạt Activity thông qua interface `AlarmIntentProvider` mà không phụ thuộc ngược vào `:app`.
2. **Single Source of Truth cho UI:**
   - Không gọi trực tiếp interactive components của Material 3 bên trong `:features:*`. Bắt buộc tái sử dụng qua `:core:designsystem` và `:litever-designsystem`.
3. **Bảo toàn dữ liệu (Zero Data Loss):**
   - Mọi thay đổi schema Room bắt buộc tăng `version` và có `Migration(old, new)`. Tuyệt đối không dùng `fallbackToDestructiveMigration()`.
4. **Quy tắc chi tiết:**
   - Xem toàn bộ quy ước bất biến tại [`.agent/project-conventions.md`](file:///.agent/project-conventions.md).

---

## 3. Quy Trình Làm Việc Của AI Agent (5 Core Workflows)

Mọi tác vụ kỹ thuật được điều phối qua 5 workflows chuẩn hóa trong [`.agent/workflows/`](file:///.agent/workflows/):

| Workflow | Mục đích chính |
| :--- | :--- |
| **`feature-development.md`** | Xây dựng/nâng cấp tính năng mới, tích hợp API, mở rộng UI Compose và render `@Preview`. |
| **`bug-and-performance.md`** | Sửa lỗi theo TDD (`Red -> Green`), xử lý Crash/ANR và tối ưu giật lag UI với `android-skills`. |
| **`code-review-and-audit.md`** | Đóng vai Senior Tech Lead kiểm duyệt Clean Architecture, bảo mật Intent/Permissions và tối ưu R8. |
| **`sdk-and-build-maintenance.md`**| Nâng cấp Target SDK, `minSdk`, AGP 9+, cập nhật dependency và xử lý STOP Protocol khi lỗi Gradle. |
| **`release-pipeline.md`** | Kiểm định pre-flight test suite, cập nhật version và tạo Git Tag phát hành. |

*Hệ thống kỹ năng chuyên biệt được đồng bộ từ kho Google [android-skills](https://github.com/android/skills) tại `C:\Users\Admin\OneDrive\Desktop\Dev\Github\skills\android-skills` và 3 skills nội bộ tại `.agent/skills/`.*
