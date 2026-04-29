# Workflow: Sửa lỗi (Bug Fixing)

Quy trình này áp dụng khi có báo cáo lỗi (Bug Report) hoặc Crash.

## Giai đoạn 1: Tái hiện & Phân tích (Reproduce)
1. **Thu thập dữ liệu:** Yêu cầu người dùng cung cấp Stacktrace, Logcat, hoặc các bước tái hiện (Steps to reproduce). Nếu lỗi từ Production (Release build), hãy cảnh giác và hỏi xem dự án có dùng R8/ProGuard không, và lỗi xảy ra cụ thể trên phiên bản Android nào.
2. **Phân tích chuyên sâu:**
   - Nếu là lỗi logic: Sử dụng @clean-arch-logic.
   - Nếu là lỗi Crash/Lag: Kích hoạt: @performance-optimization để tìm Memory Leak hoặc ANR.
   - Nếu là lỗi dữ liệu: Kích hoạt: @security-hardening để kiểm tra quyền truy cập hoặc lỗ hổng mã hóa.
3. **Viết Failing Test:** *Kích hoạt: @testing-logic*. Tạo một Unit Test mô phỏng đúng điều kiện gây lỗi. Test này PHẢI thất bại (Fail) trước khi sửa code.
4. **Trace Code:** Tìm nguyên nhân gốc rễ (Root Cause) trong UseCase, Repository hoặc Mapper dựa trên Test vừa viết.

## Giai đoạn 2: Thực thi sửa lỗi (Fixing)
1. **Đề xuất giải pháp:** Giải thích tại sao lỗi xảy ra và cách sửa (ví dụ: thiếu xử lý Null, sai logic ánh xạ dữ liệu).
2. **Sửa code:** Thực hiện sửa đổi tại lớp thấp nhất có thể (ưu tiên Domain hoặc Data layer).
3. **Verify:** Chạy lại Unit Test ở Giai đoạn 1. Test phải vượt qua (Pass).

## Giai đoạn 3: Kiểm tra tác động (Regression Check)
1. Chạy toàn bộ Test Suite của module liên quan để đảm bảo việc sửa lỗi không làm hỏng các tính năng khác.
2. Nếu lỗi liên quan đến UI, hãy kiểm tra lại trạng thái (State) trong ViewModel.

## Giai đoạn 4: Đúc kết & Cập nhật Tài liệu
1. Giải thích ngắn gọn nguyên nhân và giải pháp.
2. **Kích hoạt `@learning-journal`:** BẮT BUỘC ghi lại Root Cause và cách khắc phục để tránh tái diễn.
3. **Cập nhật CHANGELOG.md:** Ghi nhận lỗi đã được fix (nếu là lỗi quan trọng).
4. **Cập nhật LOGWORK.md:** Nếu cách sửa lỗi dẫn đến một quyết định kiến trúc mới.