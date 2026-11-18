# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Quick Alarm (Báo thức nhanh):** Đặt nhanh báo thức ngắn hạn qua FAB Menu và Bottom Sheet trực quan với bộ cộng dồn thời gian (+1m đến +1h), tự động dọn dẹp khỏi CSDL sau khi hoàn thành.
- **Rich Reminder Presentation:** Hiển thị chi tiết chu kỳ lặp lại, khung giờ hoạt động và dòng đếm ngược tương đối ("Lần tới: Hôm nay 14:00 (sau 2 giờ)") trên từng thẻ nhắc nhở.
- **Interval Recurrence Parity:** Hỗ trợ đầy đủ chu kỳ lặp ngày (Hàng tuần, hàng tháng, hàng năm) cho cả hai loại nhắc nhở Theo mốc giờ (`FIXED_TIME`) và Lặp định kỳ (`INTERVAL`).
- **Shared Components in Design System:** Hợp nhất `SnoozeBottomSheet`, `AutoSilenceBottomSheet` và `PermissionWarningBanner` vào `:core:designsystem` dùng chung cho Báo thức và Nhắc nhở.

### Changed
- **Unified Dialogs & Pickers:** Chuẩn hóa toàn bộ Date Picker, Time Picker và form thông tin cơ bản của Nhắc nhở theo phong cách hiện đại của Báo thức.
- **Single Source of Truth for Reminder Lifecycle:** Thống nhất trạng thái hoạt động của Nhắc nhở qua cờ `isEnabled`, loại bỏ cột `isCompleted` dư thừa.
- **Modernized Trigger Controls:** Tinh giản cấu hình điều kiện kích hoạt nhắc nhở ngay trên màn hình biên tập, loại bỏ điều hướng phụ.

### Removed
- Xoá bỏ hoàn toàn module `:core:network`, các model `Weather`/`TodayBriefing` cùng thư viện `Retrofit`, `OkHttp`, `Coil` không còn sử dụng; đánh dấu `@Deprecated` cho các key Remote Config cũ trong `AdPlacement` để tương thích ngược.
- Loại bỏ tầng Domain, Data và DataStore cho Thời tiết và Quote (`WeatherRepository`, `TodayRepository`, `WeatherPreferencesDataSource`) cùng binding DI tương ứng.
- Loại bỏ các thành phần UI Thời tiết (`WeatherInfoView`), Quote hằng ngày (`TodayQuoteView`), 5 bộ minh hoạ `*_pana.xml` khỏi `:core:designsystem` cùng các loại Mission chưa hoàn thiện (`SHAKE`, `QR_CODE`, `STEP`, `COLOR_MATCH`, `TAP_CHALLENGE`, `FIND_ITEM`) và nhãn "Sắp ra mắt".
- Loại bỏ màn hình trung gian sau khi tắt báo thức (`AlarmMessageScreen`) và luồng trạng thái `acknowledgingAlarmId` trong `:core:alarm`, `:features:alarms` và `:app`.
- Loại bỏ hoàn toàn tab Hôm nay (`:features:today`) khỏi thanh điều hướng để tập trung vào 3 tab cốt lõi (Báo thức, Nhắc nhở, Cài đặt); bảo toàn `ExitAppDialog` trong `:app`.
- Loại bỏ hoàn toàn tính năng danh sách công việc phụ (Checklist) khỏi Nhắc nhở để tối giản hóa trải nghiệm người dùng.
- Dọn dẹp các bộ lọc chip và các đoạn mã giao diện mồ côi (`ReminderTimeWindowDialog`).

## [2.2.0] - 2026-09-30
- Loại bỏ tính năng Checklist trong Nhắc nhở và dọn dẹp cơ chế seeding mẫu trong CSDL Debug.

## [2.1.0] - 2026-09-29
- Bổ sung hai loại hình kích hoạt Nhắc nhở độc lập: `FIXED_TIME` và `INTERVAL`.
- Hỗ trợ chọn nhiều ngày trong tháng và nhiều ngày trong năm cho lịch lặp lại.
