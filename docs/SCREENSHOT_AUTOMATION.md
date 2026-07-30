# Hướng Dẫn Sinh Ảnh Chụp Màn Hình Tự Động (Screenshot Automation)

Dự án này sử dụng **Roborazzi** và **Robolectric** để sinh tự động các bức ảnh màn hình (screenshot) dùng cho việc đưa ứng dụng lên Google Play Store, hoàn toàn không cần chạy máy ảo (Emulator) thật. Quá trình này rất nhanh và có thể dễ dàng gắn vào CI/CD.

## 1. Cấu trúc thư mục Fastlane

Các test cases sinh ảnh nằm tại:
- `:features:alarms` (`StoreScreenshotTest.kt`)
- `:features:mission` (`MissionScreenshotTest.kt`)

Ảnh sinh ra sẽ tự động được định tuyến lưu vào thư mục chuẩn của **Fastlane** tại:
`fastlane/metadata/android/<ngôn-ngữ>/images/<form-factor>/`
* Ví dụ: 
  - `fastlane/metadata/android/vi-VN/images/phoneScreenshots/1_alarm_list.png`
  - `fastlane/metadata/android/en-US/images/phoneScreenshots/1_alarm_list.png`

Kích thước màn hình hiện tại đang được chuẩn hóa:
- **Phone:** Mô phỏng Google Pixel 8a (xxhdpi, width: 412dp, height: 914dp)
- **Tablet 7-inch:** w600dp-h960dp
- **Tablet 10-inch:** w800dp-h1280dp

## 2. Cách chạy trên máy Local

Để tự động sinh toàn bộ ảnh screenshot mới, mở Terminal ở thư mục gốc của dự án và chạy:
```bash
./gradlew recordRoborazziDebug
```

> **Lưu ý:** Lệnh này sẽ ghi đè lên các ảnh cũ. Nếu bạn chỉ muốn so sánh ảnh hiện tại xem giao diện có bị vỡ hay không (regression testing) thì dùng lệnh: `./gradlew verifyRoborazziDebug`.

## 3. Tích hợp vào CI/CD (GitHub Actions)

Để tự động hoá quá trình chụp ảnh và đẩy lên Store, bạn có thể tạo một Workflow như sau (ví dụ: `.github/workflows/screenshots.yml`):

```yaml
name: Generate Screenshots & Fastlane

on:
  workflow_dispatch: # Cho phép bấm chạy bằng tay từ Github UI
  release:
    types: [published]

jobs:
  screenshots:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Setup JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '17'
          cache: 'gradle'

      - name: Xóa ảnh cũ (Tùy chọn)
        run: rm -rf fastlane/metadata/android/*/images/**/*.png

      - name: Generate Screenshots with Roborazzi
        run: ./gradlew recordRoborazziDebug

      - name: Upload Screenshots Artifact (Nếu muốn kiểm tra tay)
        uses: actions/upload-artifact@v4
        with:
          name: play-store-screenshots
          path: fastlane/metadata/android/*/images/**/*.png

      # Sau khi làm xong bước Setup Fastlane sau này, có thể gọi lệnh:
      # - name: Deploy to Play Store via Fastlane
      #   run: bundle exec fastlane supply
```

## 4. Thêm / Chỉnh sửa màn hình

Nếu bạn cần thêm màn hình, hãy tạo một `@Test` mới ở một trong các file `*ScreenshotTest.kt`. Nhớ gọi `captureScreen("tên_file") { ... }` với component Compose mà bạn muốn chụp. Khung hình (DeviceType) sẽ tự động quyết định xem file ảnh của bạn được đẩy vào `phoneScreenshots`, `sevenInchScreenshots` hay `tenInchScreenshots` của thư mục metadata.
