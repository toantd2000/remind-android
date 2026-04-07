---
description: Quy trình cập nhật nhật ký phát triển (Logwork) để ghi vết tiến độ và quyết định kỹ thuật.
---

# Mục tiêu
Đảm bảo mọi thay đổi quan trọng về kiến trúc, logic nghiệp vụ và tiến độ các Phase đều được ghi chép lại một cách hệ thống. `LOGWORK.md` đóng vai trò là "hộp đen" của dự án, giúp DEV (và AI) nắm bắt được lịch sử và lý do đằng sau các quyết định kỹ thuật.

# Các bước thực hiện

## Bước 1: Xác định thời điểm cập nhật
Cần kích hoạt quy trình này khi:
1. Hoàn thành một **Phase** (Giai đoạn) trong lộ trình tổng thể.
2. Đưa ra một **Quyết định Kỹ thuật (Technical Decision)** quan trọng (VD: đổi thư viện, tách module, thay đổi pattern DI).
3. Thực hiện một đợt **Refactor** diện rộng ảnh hưởng đến cấu trúc nhiều module.
4. Phát hiện và xử lý một **Bug nghiêm trọng** hoặc lỗi kiến trúc tiềm ẩn.

## Bước 2: Thu thập thông tin
1. Kiểm tra trạng thái các task trong `task.md`.
2. Rà soát lại các thay đổi trong `walkthrough.md` gần nhất.
3. Tổng hợp các lý do (Rationale) dẫn đến sự thay đổi (Tại sao chọn cách này? Có ưu/nhược điểm gì?).

## Bước 3: Cập nhật nội dung LOGWORK.md
1. **Tiến độ tổng thể:** Đánh dấu `[x]` cho các Phase đã hoàn thành trong phần "Lộ Trình Tổng Thể".
2. **Nhật ký chi tiết:** Thêm mục mới dưới "Nhật Ký Chi Tiết" theo định dạng:
   - Ngày tháng thực hiện.
   - Tiêu đề tóm tắt (Phase hoặc Sự kiện).
   - Danh sách các Quyết định/Thay đổi (Ghi rõ số thứ tự Quyết định).
3. **Nội dung Quyết định:** 
   - Viết ngắn gọn, súc tích nhưng đủ ý.
   - Nêu rõ vấn đề gặp phải và giải pháp đã thực hiện.
   - Nhấn mạnh vào lợi ích kiến trúc (Clean Architecture, Multi-module, Performance).

## Bước 4: Kiểm tra và Duyệt
1. Đảm bảo ngôn ngữ sử dụng là tiếng Việt chuyên ngành kỹ thuật (có thể đan xen thuật ngữ tiếng Anh gốc).
2. Kiểm tra tính nhất quán giữa Logwork và mã nguồn thực tế.
3. Thực hiện Commit sau khi cập nhật Logwork để đóng gói giai đoạn.

# Lưu ý quan trọng
- Không lạm dụng ghi chép các thay đổi nhỏ lẻ (fix typo, đổi tên biến). Chỉ ghi lại những gì ảnh hưởng đến "xương sống" của dự án.
- Logwork là tài liệu sống, cần được cập nhật ngay sau khi quyết định được thực thi để tránh quên bối cảnh.
