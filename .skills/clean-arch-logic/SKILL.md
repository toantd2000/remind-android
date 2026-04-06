---
name: clean-arch-logic
description: Skill này hướng dẫn Agent tạo cấu trúc Logic (Domain & Data) đúng chuẩn Clean Architecture. Dùng khi người dùng yêu cầu thêm tính năng mới hoặc xử lý dữ liệu.
metadata:
  author: Android-Expert
  version: "1.1"
---

# QUY TẮC CỐT LÕI (CORE RULES)

Bạn là một chuyên gia về Clean Architecture trong Android. Khi thực hiện các tác vụ liên quan đến logic, bạn PHẢI tuân thủ các lớp sau:

### 1. Lớp Domain (Trung tâm)
- **Entities:** Các class dữ liệu thuần túy (Data Class), không chứa logic của Framework (không có annotations của Room hay Gson).
- **Repository Interfaces:** Chỉ định nghĩa các hàm cần thiết (ví dụ: `suspend fun getUser(): User`).
- **UseCases:** Mỗi UseCase chỉ thực hiện MỘT nhiệm vụ duy nhất. Tên class phải bắt đầu bằng động từ (ví dụ: `GetProfileUseCase`, `SaveSettingsUseCase`).

### 2. Lớp Data (Thực thi)
- **DTOs (Data Transfer Objects):** Các class chứa annotation để map với API (Retrofit) hoặc DB (Room).
- **Repository Implementation:** Thực thi Interface từ lớp Domain.
- **Mappers:** Luôn phải có hàm extension để chuyển đổi từ `DTO` sang `Entity` (ví dụ: `UserResponse.toDomain()`).

### 3. Ràng buộc quan trọng (Constraints)
- **KHÔNG** được phép để lớp Domain phụ thuộc (import) vào lớp Data hoặc UI.
- **LUÔN** sử dụng `Result<T>` hoặc một Sealed Class để bọc kết quả trả về từ Repository nhằm xử lý lỗi tập trung.
- **Error Handling Tách Biệt:** Tại lớp `RepositoryImpl`, PHẢI catch các Exceptions đặc thù của Framework/Network (như `HttpException`, `IOException`) và ánh xạ sang các Domain Exceptions thuần túy (như `NoInternetException`, `DataNotFoundException`) rồi mới bọc vào `Result<T>` trả cho UseCase. Cấm leak Exception của thư viện bên ngoài về UseCase hay ViewModel.

# QUY TRÌNH THỰC HIỆN (WORKFLOW)
Khi tôi yêu cầu "Tạo logic cho tính năng [X]", hãy:
1. Đề xuất danh sách các file sẽ tạo (Entity -> Repo Interface -> UseCase -> Repo Impl).
2. Viết Code cho lớp Domain trước.
3. Viết Code cho lớp Data sau.
4. Kiểm tra xem có vi phạm quy tắc "Dependency Rule" (Data -> Domain) không.