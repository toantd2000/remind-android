# Workflow: Cập nhật Nhật ký Phát triển (Logwork Update)

Mục tiêu: Ghi lại các quyết định KIẾN TRÚC LỚN nhất một cách siêu ngắn gọn (Micro-TDR) để tránh làm phình to tài liệu.

## Giai đoạn 1: Lọc Quyết Định
1. Đánh giá xem thay đổi vừa rồi có thực sự là thay đổi KIẾN TRÚC LỚN hay thư viện cốt lõi không.
2. Nếu chỉ là fix bug, UI, logic thông thường -> DỪNG NGAY. KHÔNG GHI VÀO LOGWORK.

## Giai đoạn 2: Draft Micro-TDR
1. Lấy ID tiếp theo từ `LOGWORK.md`.
2. Soạn nội dung TDR **tối đa 5 dòng**:
   - Tiêu đề < 10 chữ.
   - Context: 1 câu ngắn.
   - Decision: 1 câu ngắn.
   - Consequences: 1 câu ngắn.

## Giai đoạn 3: Cập nhật & Tối ưu
1. Append vào `LOGWORK.md`. Đánh dấu `[x]` Phase nếu cần.
2. Kiểm tra lại toàn bộ file `LOGWORK.md`, nếu file quá dài (> 500 dòng), tự động đề xuất tạo `LOGWORK_ARCHIVE.md` để lưu các TDR cũ.

# Ràng buộc Tuyệt đối
- TUYỆT ĐỐI KHÔNG dùng văn phong kể lể dài dòng. Không chèn code block. Đi thẳng vào vấn đề.
