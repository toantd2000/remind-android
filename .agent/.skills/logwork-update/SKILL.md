---
name: logwork-update
description: Kỹ năng để ghi lại các quyết định kỹ thuật cốt lõi (TDR) vào LOGWORK.md một cách siêu ngắn gọn (ngăn chặn tình trạng rác tài liệu).
metadata:
  author: Android-Expert
  version: "3.0"
---

# HƯỚNG DẪN CẬP NHẬT LOGWORK (TDR SYSTEM)

Sử dụng kỹ năng này CHỈ khi có thay đổi kiến trúc LỚN hoặc thay đổi thư viện cốt lõi.

## 1. Tiêu chí lựa chọn sự kiện ghi Log (CỰC KỲ NGHIÊM NGẶT)

TUYỆT ĐỐI KHÔNG ghi lại:
- Fix bug, typo, đổi tên biến.
- Cập nhật UI, logic thông thường.
- Các bài học/cách fix lỗi (đó là việc của Learning Journal).

CHỈ ghi lại khi:
1. Thêm/Bớt/Thay thế hoàn toàn một Module hoặc Package lớn.
2. Đổi Pattern kiến trúc cốt lõi (ví dụ: chuyển từ MVVM sang MVI).
3. Đổi/Thêm thư viện 3rd-party cốt lõi có ảnh hưởng toàn hệ thống (Room, Retrofit, Jetpack Compose version lớn).

## 2. Cấu trúc TDR SIÊU NGẮN (Micro-TDR)

Mỗi TDR không được vượt quá 5 dòng. Bỏ qua các định dạng rườm rà.

### [TDR-XXX] - [Tiêu đề < 10 chữ]
- **Date:** YYYY-MM-DD | **Status:** [Accepted/Deprecated]
- **Context:** [1 câu ngắn gọn giải thích tại sao cần đổi]
- **Decision:** [1 câu ngắn gọn mô tả giải pháp]
- **Consequences:** [1 câu ngắn gọn mô tả tác động cốt lõi]

## 3. Quy trình
1. Đọc `LOGWORK.md` để lấy ID tiếp theo.
2. Viết TDR siêu ngắn theo template trên.
3. Nếu hoàn thành Phase, đánh dấu `[x]` vào "Lộ Trình Tổng Thể".
4. Append vào "Nhật Ký Chi Tiết".

## 4. Chống Rác Tài Liệu (Anti-Clutter)
- Không dùng từ ngữ hoa mỹ, dài dòng.
- Không chèn code block vào TDR.
- Đi thẳng vào vấn đề. Nếu 1 TDR dài quá 5 dòng, hãy tự động tóm tắt lại.
