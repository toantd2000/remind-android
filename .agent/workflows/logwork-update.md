# Workflow: Cập nhật Nhật ký Phát triển (Logwork Update)

Mục tiêu của quy trình này là đảm bảo mọi quyết định kiến trúc và thay đổi hệ thống đều được ghi lại một cách chuyên nghiệp dưới dạng Technical Decision Records (TDR).

## Giai đoạn 1: Kích hoạt Kỹ năng
1. **Kích hoạt:** `@logwork-update`.
2. **Quét dữ liệu:** Agent tự động đọc `task.md`, `walkthrough.md` và các thay đổi trong session hiện tại để nhận diện các "Quyết định Kỹ thuật" tiềm năng.

## Giai đoạn 2: Trích xuất & Soạn thảo
1. **Xác định ID:** Kiểm tra `LOGWORK.md` để lấy ID tiếp theo (Dạng `TDR-XXX`).
2. **Draft nội dung:** Với mỗi quyết định, soạn thảo theo template:
   - Context (Bối cảnh/Vấn đề).
   - Decision (Giải pháp/Tại sao chọn).
   - Consequences (Hệ quả/Tác động).
3. **Phân loại:** Đảm bảo quyết định đó mang tính hệ thống, không phải chi tiết thực thi nhỏ lẻ.

## Giai đoạn 3: Cập nhật LOGWORK.md
1. **Append:** Thêm các TDR mới vào cuối phần "Nhật Ký Chi Tiết".
2. **Roadmap:** Kiểm tra xem task hiện tại có hoàn thành một Phase nào trong "Lộ Trình Tổng Thể" không? Nếu có, hãy đánh dấu `[x]`.
3. **Commit (Khuyến nghị):** Sau khi cập nhật Logwork, hãy thực hiện một commit riêng cho tài liệu này để đóng gói giai đoạn.

## Giai đoạn 4: Đối soát
1. Kiểm tra tính hiển thị của Markdown (bảng biểu, alert, code block).
2. Đảm bảo ngôn ngữ nhất quán là Tiếng Việt chuyên ngành.

# Lưu ý quan trọng
- Logwork là **Single Source of Truth** về lịch sử kiến trúc. Agent không được tự ý xóa bỏ các TDR cũ trừ khi chúng bị thay thế (mark là `Superseded`).
- Ưu tiên ghi lại **Lý do (Rationale)** hơn là mô tả code.
