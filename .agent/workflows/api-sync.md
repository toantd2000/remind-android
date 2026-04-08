# Workflow: Đồng bộ hóa API (API Synchronization)

Quy trình này áp dụng khi có file Swagger (JSON/YAML) mới hoặc khi một Endpoint API thay đổi cấu trúc.

## Giai đoạn 1: Phân tích tài liệu (Ingestion)
*Kích hoạt: @api-integration*
1. **Đọc file nguồn:** Yêu cầu người dùng cung cấp đường dẫn file Swagger hoặc dán nội dung JSON/YAML của Endpoint cần xử lý.
2. **Trích xuất thông tin:** - Xác định URL Base, Method (GET/POST...), và Header cần thiết.
    - Phân tích cấu trúc Request Body và Response Body.
3. **So sánh thay đổi:** Nếu đây là cập nhật, hãy liệt kê các Field mới, Field bị xóa hoặc thay đổi kiểu dữ liệu (ví dụ: từ Int sang Long).

## Giai đoạn 2: Tạo lớp dữ liệu (DTO Generation)
*Quy tắc đặt tên: Luôn có hậu tố `Response` hoặc `Request` (ví dụ: `UserResponse`).*
1. **Tạo DTO:** Sử dụng Kotlin Data Class với thư viện Serialization đang dùng (Gson, Moshi hoặc Kotlin Serialization).
2. **Handle Nullability:** Luôn kiểm tra xem các trường có thể Null hay không dựa trên tài liệu Swagger. Mặc định nên để Optional (`?`) nếu không chắc chắn.

## Giai đoạn 3: Cập nhật Network Interface
1. **Retrofit Service:** Tạo hoặc cập nhật Interface của Retrofit.
2. **Kiểu trả về:** Sử dụng `Response<T>` hoặc trực tiếp `T` tùy theo cấu trúc xử lý lỗi của dự án.
3. **Documentation:** Thêm KDoc cho các hàm API dựa trên mô tả (description) trong Swagger.

## Giai đoạn 4: Cầu nối Domain (Mapping)
*Kích hoạt: @clean-arch-logic*
1. **Update Entity:** Kiểm tra xem lớp Domain Entity có cần thêm trường dữ liệu mới không.
2. **Refactor Mapper:** Cập nhật hàm `toDomain()` để ánh xạ dữ liệu từ DTO mới sang Entity. Đảm bảo cung cấp giá trị mặc định (Default Value) cho các trường mới.

## Giai đoạn 5: Tích hợp & Kiểm tra
1. **DI:** *Kích hoạt: @hilt-di-config*. Đảm bảo API Service mới đã được cung cấp trong NetworkModule.
2. **Smoke Test:** Đề xuất tạo một Unit Test nhỏ để kiểm tra việc Parse JSON mẫu từ Swagger vào DTO xem có bị lỗi Crash không.

## Giai đoạn 6: Báo cáo
1. Liệt kê danh sách các Endpoint và DTO vừa được cập nhật/tạo mới.
2. Cảnh báo các vị trí trong code có thể bị lỗi do thay đổi kiểu dữ liệu (Breaking Changes).