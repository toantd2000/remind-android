---
name: hilt-di-config
description: Hướng dẫn Agent cấu hình Dependency Injection sử dụng Hilt. Dùng khi cần tạo Repository mới, ViewModel mới hoặc cung cấp các thư viện bên thứ ba.
metadata:
  author: Android-Expert
  version: "1.0"
---

# QUY TẮC CẤU HÌNH HILT

### 1. Quy tắc Inject
- **Constructor Injection:** Luôn ưu tiên sử dụng `@Inject constructor()` cho các lớp như Repository, UseCase, ViewModel.
- **ViewModel:** Luôn annotate lớp ViewModel với `@HiltViewModel`.

### 2. Định nghĩa Module
- Các Interface (như Repository) phải được cung cấp qua một `abstract class` Hilt Module sử dụng `@Binds`.
- Các thư viện bên thứ ba (Retrofit, Room, OkHttp) phải được cung cấp qua một `class` Hilt Module sử dụng `@Provides` và `@Singleton`.
- **Naming:** Tên module phải có hậu tố `Module` (ví dụ: `RepositoryModule`, `NetworkModule`).

### 3. Scope & Component
- Singleton (Toàn ứng dụng): Sử dụng `@InstallIn(SingletonComponent::class)`.
- Activity/Fragment: Chỉ sử dụng khi thực sự cần thiết với các class liên quan đến UI.

# WORKFLOW CHO AGENT
Khi tôi yêu cầu "Cung cấp Repository [X] vào DI":
1. Kiểm tra xem đã có `RepositoryModule` chưa.
2. Thêm hàm `@Binds` để map Interface vào Implementation.
3. Đảm bảo lớp Implementation đã có `@Inject constructor`.