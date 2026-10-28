---
description: Quy trình chuẩn bị phát hành (Release Preparation), kiểm tra chất lượng cuối cùng và đóng gói bản dựng (APK/AAB).
---

# Workflow: Chuẩn bị Phát hành & CI/CD Cục bộ (Release Pipeline)

Quy trình Human-in-the-loop để kiểm định chất lượng, tăng phiên bản và phát hành ứng dụng an toàn.

## Bước 1: Kiểm định Chất lượng & Bảo mật trước Phát hành (Pre-flight Check)
*Skills kích hoạt từ `android-skills`: `r8-analyzer`, `play-policy-insights`, `android-intent-security`*
1. Chạy toàn bộ Unit Test: `./gradlew testDebugUnitTest --continue`.
2. Kiểm tra quy tắc R8/ProGuard và đảm bảo không còn log thông tin nhạy cảm ở bản Release.
3. Kiểm tra trạng thái Git (`git status --short`) đảm bảo working tree sạch sẽ.

## Bước 2: Cập nhật Phiên bản & Tài liệu Phát hành
1. Tăng `app-versionCode` và cập nhật `app-versionName` trong `gradle/libs.versions.toml`.
2. Tổng hợp các thay đổi từ lịch sử commit vào `CHANGELOG.md`.

## Bước 3: Đóng gói & Xác nhận Git Tag (Yêu cầu Xác nhận từ Người dùng)
1. Chạy lệnh kiểm tra bản dựng Release (ví dụ: `./gradlew assembleRelease` hoặc `bundleRelease`).
2. Tạo commit theo chuẩn Conventional Commits: `chore(release): bump version to vX.Y.Z`.
3. **Hỏi ý kiến người dùng** trước khi tạo Git Tag (`git tag vX.Y.Z`) hoặc đẩy lên remote (`git push`).
