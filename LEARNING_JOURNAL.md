# Nhật Ký Học Tập Của Agent (Learning Journal)

Đây là nơi Agent (với tư cách là một Android Developer hiện đại) ghi chép lại những sai lầm gặp phải, những giả định chưa đúng trong quá trình phát triển, cũng như các thói quen, quy chuẩn code đúc rút ra được.
Cơ chế này đảm bảo "sự tiến hóa" liên tục qua các task, tránh việc một lỗi vi phạm lặp đi lặp lại.

> **Quy tắc:** Agent sẽ tự cập nhật vào cuối file này khi chạy quy trình `/learning-journal`.

---

## Ngày tháng: 2026-04-06
**Vấn đề / Task:** Dùng trực tiếp các thành phần `androidx.compose.material3.*` (như Button, Icon) tại module `:features:alarm` vì thấy `:core:designsystem` lúc đó đang cạn/rỗng.
**Phân tích nguyên nhân:** 
* Giả định sai lầm: Tôi đã lầm tưởng rằng nếu `designsystem` chưa định nghĩa các thư viện Wrapper (Button riêng, Icon riêng), thì việc dùng tạm thư viện gốc Material3 ở Feature module là được phép để giải quyết nhanh tính năng.
* Hậu quả: Phá vỡ nguyên tắc "Single Source of Truth", khiến cho bộ giao diện của dự án bị phân mảnh. Việc đổi mới toàn bộ giao diện (Rebrand/Dark theme/Style update) sau này sẽ cực kỳ tốn kém vì phải sửa thủ công ở mọi file thuộc Feature.
**Giải pháp / Rule mới:** 
* Đã nhận thức rõ: **Triết lý Clean Architecture & Design System không cho phép "đường tắt".**  Kể cả khi `:core:designsystem` rỗng, bước đi duy nhất đúng là phải nhảy sang `:core:designsystem` để khởi tạo cấu trúc và viết Component bọc (Wrapper) đầu tiên (ví dụ `AlarmButton`) trước. Sau đó mới quay lại gọi nó ở `:features:...`.
