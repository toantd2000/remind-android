---
title: Chuẩn bị phát hành (Release Preparation)
description: Quy trình chuẩn bị và đóng gói ứng dụng để phát hành (Hướng dẫn cho AI Agent).
---

# Quy trình Release Tự động (Dành cho AI Agent)

> **Gửi AI Agent**: Khi người dùng yêu cầu bạn "Bắt đầu quy trình release cho bản phát hành mới", bạn **PHẢI** tuân thủ nghiêm ngặt các bước dưới đây. Luôn dùng tiếng Việt để giao tiếp với người dùng và **chờ họ xác nhận ở các bước có cờ [CẦN XÁC NHẬN]**.

## Bước 1: Xác định phiên bản mới (Versioning)
1. Tự động kiểm tra phiên bản hiện tại bằng cách đọc `versionName` trong `build.gradle.kts` (app module) HOẶC chạy lệnh `git describe --tags --abbrev=0`.
2. Đề xuất phiên bản tiếp theo bằng cách tăng số Patch (ví dụ: `v1.2.3` -> `v1.2.4`) hoặc tăng Minor nếu có tính năng lớn.
3. **[CẦN XÁC NHẬN]** Hỏi người dùng: "Phiên bản hiện tại là `vX.Y.Z`. Tôi đề xuất bản phát hành tiếp theo là `vA.B.C`. Bạn có đồng ý với phiên bản này không?".

## Bước 2: Kiểm tra Changelog (Release Notes)
1. Đọc nội dung trong thư mục `docs/changelog/`.
2. Kiểm tra xem người dùng đã viết mô tả cho bản cập nhật mới chưa.
3. Nếu chưa có:
   - Tự động chạy `git log` từ tag gần nhất đến `HEAD`.
   - Lọc các commit (bỏ các từ khóa kỹ thuật như "gradle", "refactor", "agp").
   - Trình bày một bản nháp Changelog (dưới 500 ký tự) cho người dùng.
   - **[CẦN XÁC NHẬN]** Hỏi người dùng: "Bạn có muốn sử dụng changelog này không? Nếu có, tôi sẽ cập nhật vào fastlane metadata".
4. Copy nội dung changelog cuối cùng vào thư mục ngôn ngữ tương ứng trong `fastlane/metadata/android/`.

## Bước 3: Kiểm tra Thay đổi Giao diện (UI) & Sinh ảnh
1. Chạy `git diff --name-only <last-tag> HEAD` để xem các file bị thay đổi.
2. Cảnh báo người dùng nếu thấy có file thay đổi trong `features/`, `ui/` hoặc `components/`.
4. Nếu người dùng đồng ý (Y):
   - Kiểm tra xem các file ảnh đã được sinh ra đúng trong `fastlane/metadata/android/` chưa.

## Bước 4: Cập nhật Version Code & Name
1. Mở file `build.gradle.kts` (app module).
2. Tự động tăng `versionCode` (cộng thêm 1 hoặc dùng unix timestamp).
3. Đổi `versionName` thành tag mới (ví dụ: `1.2.3`).
4. Dùng tool chỉnh sửa file để lưu lại thay đổi.

## Bước 5: Chạy Fastlane (Deploy Internal)
1. **[CẦN XÁC NHẬN]** Hỏi người dùng: "Mọi thứ đã sẵn sàng. Bạn muốn đẩy (upload) CẢ file AAB lẫn metadata, hay CHỈ upload metadata/ảnh (skip AAB)?".
2. Chạy lệnh Fastlane tương ứng:
   - Đẩy tất cả: `fastlane deploy_internal`
   - Chỉ đẩy metadata: `fastlane deploy_internal skip_upload_apk:true`
3. Theo dõi log và thông báo kết quả.

## Bước 6: Git Commit & Tag
1. Sau khi Fastlane hoàn thành thành công, tự động thực hiện:
   - `git add .`
   - `git commit -m "chore: release v1.2.3"`
   - `git tag v1.2.3`
2. **[CẦN XÁC NHẬN]** Hỏi người dùng: "Tôi đã tạo commit và tag cục bộ. Bạn có muốn tôi push chúng lên remote (`git push --tags && git push origin main`) không?".

---
## Tóm tắt cho Lập trình viên (Con người)
Mỗi khi cần phát hành, bạn chỉ cần:
1. Đảm bảo tính năng đã code xong và pass các bài unit test cơ bản.
2. Viết changelog vào `docs/changelog` (hoặc phó mặc cho AI đọc commit).
3. Ra lệnh cho Agent: *"Bắt đầu quy trình release cho phiên bản vX.Y.Z"* và trả lời các câu hỏi xác nhận của nó.
