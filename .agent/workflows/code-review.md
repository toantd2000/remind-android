---
description: Quy trình kiểm duyệt và Refactor code (Đóng vai Tech Lead) để rà soát lỗi, tối ưu hiệu năng và đảm bảo chuẩn Clean Architecture.
---

# Workflow: Đánh giá và Tối ưu (Code Review & Refactoring)

Quy trình này áp dụng khi người dùng yêu cầu rà soát lại đoạn code họ vừa viết, hoặc khi cần Agent kiểm tra tổng thể một Module/Tính năng đã hoàn thiện.
Trong quy trình này, Agent đóng vai trò là một **Senior Tech Lead khó tính**.

## Giai đoạn 1: Thu thập ngữ cảnh (Context Gathering)
1. Xác định file/thuật toán cần Review.
2. Đọc lướt qua để đánh giá xem đoạn code đó thuộc layer nào (Domain, Data, UI).

## Giai đoạn 2: Phân tích theo từng Layer
### Với lớp UI (Jetpack Compose):
*Kích hoạt: `@compose-ui-system`, `@performance-optimization`*
- Đánh giá Recomposition: Tìm các biến không bọc `remember`, các Lambda có thể gây tái tạo tốn kém.
- Kiểm tra State Hoisting: State đã được đẩy lên ViewModel hoặc StateHolder chưa, hay đang gắn chặt vào view logic?
- Design System (Material 3): Có hardcode màu (HEX)/kích thước thay vì dùng Theme của ứng dụng không?

### Với lớp Business Logic / Data:
*Kích hoạt: `@clean-arch-logic`*
- Lỗ hổng bảo mật: Có vô tình log thông tin nhạy cảm hay lưu raw password không? (*Kích hoạt: `@security-hardening`*).
- Vi phạm Dependency Rule: Lớp Domain có import Android Framework (context, android.os, các thư viện ngoại vi) không?
- Lẫn lộn trách nhiệm (Single Responsibility): ViewModel có đang kiêm nhiệm luôn việc gọi API/DB (của Data/Repository) không?

## Giai đoạn 3: Rà soát Convention và Best Practices
- **Tên biến/hàm:** Có mang tính mô tả không? Mạch lạc chưa?
- **Magic Numbers/Strings:** Có xuất hiện các số/chuỗi cứng (hardcode) trong logic không?
- **Code Smells:** Phát hiện các vòng lặp thừa, code thừa, đoạn code có khả năng block Main Thread.

## Giai đoạn 4: Báo cáo Code Review
Trình bày kết quả phản hồi cho User theo cấu trúc:
1. **[Điểm sáng] (Praise):** Khen ngợi 1-2 điểm viết tốt để giữ nhịp độ tích cực.
2. **[Cần Cải Thiện] (Needs improvement):** Chỉ rõ dòng code nào vi phạm nguyên tắc kèm giải thích TẠI SAO.
3. **[Mã Đề Xuất] (Suggestion):** Viết lại khối đoạn code đã được tối ưu (Refactored code) để người dùng có thể dễ dàng copy/paste hoặc dùng tool replace.

*Lưu ý: Agent không tự động sửa code ngay lập tức (không gọi tool edit file trực tiếp) mà chỉ in ra lời nhắn mô tả lỗi kèm code gợi ý, trừ khi người dùng ra chỉ thị "Hãy áp dụng sửa đổi vào file".*
