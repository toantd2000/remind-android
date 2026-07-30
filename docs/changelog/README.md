# Hướng dẫn viết Changelog

Thư mục này là nơi chứa các bản nháp mô tả phát hành (Release Notes/Changelog) trước khi đẩy lên Google Play.

## Quy trình chuẩn bị phát hành
Mỗi khi chuẩn bị tạo một Release Tag mới, hãy làm theo các bước sau:

1. **Xác định các thay đổi**: Xem lại các tính năng hoặc lỗi đã sửa kể từ bản phát hành trước đó.
2. **Viết nội dung**:
   - Nếu bạn hỗ trợ nhiều ngôn ngữ, tạo các file `.txt` tương ứng trong thư mục ngôn ngữ của fastlane (ví dụ `fastlane/metadata/android/vi-VN/changelogs/default.txt`).
   - Hoặc, nếu sử dụng luồng AI Agent (như trong `.agent/workflows/release-preparation.md`), hãy tạo file `release_notes.txt` ngay tại thư mục này hoặc yêu cầu AI tự tóm tắt từ lịch sử git.
3. **Giới hạn kỹ thuật**:
   - Độ dài tối đa: **500 ký tự** (theo quy định của Google Play).
   - Nội dung: Tập trung vào trải nghiệm người dùng, tránh dùng các từ ngữ kỹ thuật như "Cập nhật AGP", "Refactor", v.v.

## Mẫu nội dung (Tiếng Việt)
```text
Cập nhật trong phiên bản này:
- Thêm tính năng nhắc nhở báo thức thông minh.
- Cải thiện tốc độ mở ứng dụng.
- Khắc phục lỗi không đổ chuông trên một số thiết bị màn hình gập.
Cảm ơn bạn đã sử dụng ReMind!
```

## Dành cho AI Agent
Khi nhận được yêu cầu "Release vX.Y.Z", hãy luôn kiểm tra xem người dùng đã chuẩn bị nội dung phát hành mới nhất hay chưa. Nếu chưa, hãy gợi ý cho người dùng hoặc tự động trích xuất từ git commit (chỉ chọn các thay đổi liên quan đến người dùng).
