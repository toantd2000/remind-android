# Nhật Ký Học Tập Của Agent (Learning Journal)

Đây là nơi Agent (với tư cách là một Android Developer hiện đại) ghi chép lại những sai lầm gặp phải, những giả định chưa đúng trong quá trình phát triển, cũng như các thói quen, quy chuẩn code đúc rút ra được.
Cơ chế này đảm bảo "sự tiến hóa" liên tục qua các task, tránh việc một lỗi vi phạm lặp đi lặp lại.

> **Quy tắc:** Agent sẽ tự cập nhật vào cuối file này khi chạy quy trình `/learning-journal`.

---

## Ngày tháng: 2026-04-06
**Vấn đề / Task:** Dùng trực tiếp các thành phần `androidx.compose.material3.*` (như Button, Icon) tại module `:features:alarm` vì thấy `:core:designsystem` lúc đó đang cạn/rỗng.
**Phân tích nguyên nhân:** 
* Giả định chưa chính xác: Tôi đã lầm tưởng rằng có thể dùng trực tiếp thư viện gốc Material3 ở Feature module đối với mọi loại Component khi `designsystem` còn rỗng.
* Nhận định đúng (Pragmatic Approach): Việc bọc 100% mọi component (kể cả Text) ngay từ đầu có thể gây dư thừa (Over-engineering).
**Giải pháp / Rule mới:** 
* **Quy mô áp dụng mếm mỏng:** Tuân thủ Design System (Wrapper component) với các cấu trúc tương tác lớn, định hình UI (như `Button`, `Scaffold`, `Switch`, `Dialog`...). Kể cả khi `:core:designsystem` rỗng, BẮT BUỘC phải tạo lớp bọc cho chúng (VD: `AlarmButton`) trước khi dùng. 
* Đối với các thành phần cơ bản thuần tuý như `Text` hay `MaterialTheme.typography`, CÓ THỂ linh động sử dụng trực tiếp từ `androidx.compose.material3.*` tại feature module để giảm chi phí phát triển không cần thiết.
