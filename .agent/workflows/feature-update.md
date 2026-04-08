# Workflow: Cập nhật & Nâng cấp tính năng (Feature Update)

Quy trình này áp dụng khi cần thêm logic mới vào một tính năng đã tồn tại hoặc thay đổi hành vi hiện có.

## Giai đoạn 1: Phân tích tác động (Impact Analysis)
1. **Tìm kiếm:** Xác định các UseCase, Repository và Entity hiện có liên quan đến yêu cầu.
2. **Đánh giá rủi ro:** Liệt kê các phần khác trong dự án đang phụ thuộc vào các lớp này.
3. **Đánh giá Hiệu năng:** Kích hoạt: @performance-optimization. Liệu thay đổi logic này có làm tăng thời gian xử lý hoặc tốn bộ nhớ hơn không?
4. **Lập kế hoạch:** Quyết định xem nên sửa đổi trực tiếp (Modify) hay kế thừa/tạo mới (Extend) để tránh làm hỏng code cũ.

## Giai đoạn 2: Cập nhật Logic (Refactor & Extend)
*Kích hoạt: @clean-arch-logic*
1. **Domain Update:** Cập nhật Entity hoặc thêm phương thức mới vào Repository Interface.
2. **Data Update:** Cập nhật Repository Implementation và Mappers. Đảm bảo dữ liệu cũ vẫn được xử lý đúng (Backward compatibility).

## Giai đoạn 3: Cập nhật DI & Test
1. **DI:** *Kích hoạt: @hilt-di-config*. Cập nhật Module nếu có thêm dependency mới.
2. **Testing:** *Kích hoạt: @testing-logic*.
    - Cập nhật các bản Unit Test cũ để khớp với logic mới.
    - Viết thêm Test cho các trường hợp mới được thêm vào.

## Giai đoạn 4: Cập nhật Giao diện (Presentation & UI)
*Kích hoạt: @compose-ui-system*
1. Cập nhật **ViewModel** và **UiState** để xử lý luồng dữ liệu mới.
2. Chỉnh sửa hoặc thêm mới các component phần **Compose UI**.
3. Cập nhật và bổ sung **`@Preview`** cho các UI state bị thay đổi.

## Giai đoạn 5: Kiểm duyệt (Review)
1. So sánh sự thay đổi (Diff) để đảm bảo không xóa nhầm logic/code lỗi cũ.
2. Đảm bảo project vẫn compile bình thường và giao diện hiển thị đúng định hướng.