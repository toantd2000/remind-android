# Skill: Design System Management (@design-system-management)

Kỹ năng này tập trung vào việc quản lý, bảo trì và mở rộng hệ thống thiết kế `:litever-designsystem`.

## Bối cảnh sử dụng
Sử dụng kỹ năng này khi:
- Cần thêm mới một UI Component vào thư viện dùng chung.
- Cần cập nhật bảng màu (Color Palette) cho một App cụ thể.
- Cần refactor hoặc tối ưu hóa hệ thống Theme.
- Cần xử lý các vấn đề liên quan đến `CompositionLocal`.

## Nguyên tắc thiết kế
1. **Generic First:** Mọi component trong `:litever-designsystem` phải mang tính tổng quát, không chứa logic business.
2. **Customizable:** Sử dụng tham số (Modifier, Colors, Typography) để cho phép App bên ngoài tùy biến mà không cần sửa code thư viện.
3. **M3 Compliance:** Luôn tuân thủ chuẩn Material 3. Khi định nghĩa màu mới, phải đảm bảo có đủ cặp (Color, OnColor).
4. **Theme Delegation:** App cụ thể nên định nghĩa bộ màu riêng thông qua `LiteverColors` và truyền vào `LiteverTheme`, thay vì tự viết lại `Theme.kt`.

## Cách thực hiện các tác vụ phổ biến

### Thêm Component mới
1. Tạo file mới trong `vn.io.litever.designsystem.components`.
2. Đặt tên với prefix `Litever` (ví dụ: `LiteverCard`).
3. Sử dụng `LiteverTheme.colors` và `LiteverTheme.typography` làm giá trị mặc định.
4. Bắt buộc thêm `@Preview` cho cả Light và Dark mode.

### Cập nhật Bảng màu (App-specific)
1. Trong module `:core:designsystem` (hoặc module tương đương của App), tìm file `Color.kt`.
2. Khởi tạo instance mới của `LiteverColors`.
3. Điền đầy đủ 36 tokens màu (có thể copy từ `liteverLightColors` và sửa những màu cần thiết).
4. Truyền instance này vào `LiteverTheme` trong file `Theme.kt`.

### Bảo trì Tương thích ngược
1. Khi di chuyển một component từ App vào thư viện, hãy để lại một bản wrapper tại vị trí cũ.
2. Đánh dấu `@Deprecated` nếu cần thiết để khuyến khích chuyển sang dùng component từ thư viện.

## Công cụ hỗ trợ
- **CompositionLocalProvider:** Dùng để cung cấp `LocalLiteverColors` và `LocalLiteverTypography`.
- **MaterialTheme:** Luôn bọc bên trong `LiteverTheme` để hỗ trợ các component chuẩn của Google.
