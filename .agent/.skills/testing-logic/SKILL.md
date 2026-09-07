---
name: testing-logic
description: Hướng dẫn Agent viết Unit Test cho UseCase và Repository bằng MockK và JUnit.
metadata:
  author: Android-Expert
  version: "1.0"
---

# QUY TẮC VIẾT TEST

### 1. Công nghệ sử dụng
- **JUnit 5 / JUnit 4:** Framework chạy test chính (JUnit 4 thường dùng cho UI Test).
- **MockK:** Thư viện để mock các dependency trong Unit Test.
- **Turbine:** Thư viện để test các luồng dữ liệu `Flow`.
- **Compose UI Test:** Sử dụng `createComposeRule` để test giao diện Compose.

### 2. Cấu trúc một bản Test
- **Naming:** [ClassName]Test.kt đặt trong thư mục `test` (không phải `androidTest`).
- **Pattern:** Sử dụng mô hình **Given - When - Then**.
- **Mocks:** Mock tất cả các Repository khi test UseCase.
- **Coroutines:** Bắt buộc inject `CoroutineDispatcher` qua constructor trong UseCase/ViewModel. Trong quá trình test, luôn cung cấp `UnconfinedTestDispatcher` hoặc `StandardTestDispatcher` để kiểm soát thời gian của luồng ảo thay vì hardcode Dispatchers.

### 3. Workflow cho Agent (Domain/Data Logic)
Mỗi khi tạo xong một UseCase hoặc Repository:
1. Tự động tạo file Test tương ứng ở gói `test`.
2. Viết ít nhất 2 trường hợp: `Success` (trả về dữ liệu) và `Failure` (trả về Exception/Error).
3. Nếu là Repository dùng Flow, phải dùng thư viện Turbine (`test { ... }`) để kiểm tra dữ liệu phát ra.

### 4. Workflow cho Agent (UI Compose)
Khi được yêu cầu viết kiểm thử cho UI (Screen/Component):
1. Định nghĩa `composeTestRule` (`createComposeRule()`).
2. Fake các trạng thái của `UiState` đầu vào.
3. Kiểm tra việc hiển thị đúng của các element bằng `onNodeWithTag` hoặc `onNodeWithText`.
4. Giả lập hành vi người dùng bằng các lệnh như `performClick()`, `performScrollTo()`.