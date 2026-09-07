---
name: api-integration
description: Kỹ năng chuyên biệt để Agent tích hợp Retrofit, xử lý DTO và cấu hình Networking.
metadata:
  author: Android-Expert
  version: "1.0"
---

# QUY TẮC NETWORKING

### 1. Cấu trúc Interface
- **Retrofit:** Sử dụng `@GET`, `@POST`, `@PUT`, `@DELETE` đúng chuẩn REST.
- **Endpoint:** Sử dụng `@Path`, `@Query`, `@Body` theo đúng tài liệu API.
- **ReturnType:** Luôn trả về `Response<T>` hoặc tích hợp với một Wrapper xử lý lỗi chung (như `NetworkResult<T>`).

### 2. Xử lý DTO (Data Transfer Object)
- **Serialization:** Sử dụng Kotlin Serialization (hoặc Gson/Moshi tùy dự án).
- **Naming:** Sử dụng `@SerialName` (hoặc `@SerializedName`) để map với key từ JSON nếu key đó không đúng chuẩn CamelCase của Kotlin.
- **Validation:** Đảm bảo các trường bắt buộc không được để Null.

### 3. Bảo mật & Interceptors
- Không lưu Token thủ công trong mỗi hàm. Token phải được xử lý qua `AuthInterceptor`.
- Log dữ liệu API chỉ được bật trong bản `DEBUG`.

# WORKFLOW CHO AGENT
Khi nhận file Swagger hoặc JSON:
1. Tạo thư mục `remote` trong module `data`.
2. Tạo các Data Class cho Request và Response.
3. Tạo Interface API Service.
4. Viết hàm `toDomain()` trong file Mapper để chuyển DTO sang Entity ngay lập tức.