---
description: Quy trình học hỏi, phân tích lỗi sai và cập nhật kiến thức dự án (Learning & Self-Correction).
---

# Mục tiêu
Giống như một kỹ sư phần mềm thực thụ, Agent cần có khả năng tự đánh giá, ghi nhận những sai lầm trong quá trình giải quyết vấn đề và chủ động cập nhật "bộ não" của mình để không lặp lại lỗi tương tự trong các Task tiếp theo.

# Ngữ cảnh sử dụng
- Khi Agent vừa gián tiếp hoặc trực tiếp gây ra một bug hoặc có sai sót ảnh hưởng đến kiến trúc chung.
- Khi một Task hoàn thành sau nhiều lần thử và sai (trial and error), lúc này có kiến thức mới quan trọng cần ghi nhớ.
- Khi người dùng phản hồi rằng cách dùng API hoặc framework của Agent đã bị lỗi thời hoặc vi phạm convention của dự án.

# Các bước thực hiện

## Bước 1: Phân tích nguyên nhân cốt lõi (Root Cause Analysis - RCA)
1. **Dừng lại và suy ngẫm:** Rà soát lại dòng code, tài liệu hoặc quyết định thiết kế đã gây ra vấn đề.
2. **Trả lời 3 câu hỏi (Post-mortem):**
   - Mình đã giả định sai điều gì? (Ví dụ: "Tôi tưởng ViewModel được tiêm tự động mà không cần Hilt").
   - Hậu quả của sai sót này là gì?
   - Đâu là giải pháp chuẩn xác (Best Practice) hoặc quy ước đặc thù riêng cho dự án này giúp giải quyết vấn đề?

## Bước 2: Ghi chép vào Nhật ký học tập (Learning Journal)
1. Tìm hoặc mở tệp `LEARNING_JOURNAL.md` ở thư mục gốc của dự án. (Nếu chưa tồn tại, hãy tạo mới file với một tiêu đề phù hợp).
2. Viết thêm (append) nội dung bài học mới theo định dạng tiêu chuẩn sau:
   - **Ngày tháng:** [YYYY-MM-DD]
   - **Vấn đề / Task:** Đoạn mô tả ngắn gọn lỗi sai hoặc bối cảnh.
   - **Phân tích nguyên nhân:** ...
   - **Giải pháp / Rule mới:** [Cách giải quyết đã xác minh đúng]

## Bước 3: Phân loại Scope & Nâng cấp "Bộ não" (Tối Quan Trọng)
Không phải bài học nào cũng đúng với mọi dự án. Agent **CẦN BẮT BUỘC** xác định phạm vi của bài học:

- **Loại 1: Universal Knowledge (Nguyên tắc chuẩn của Android, Kotlin, Thư viện):**
   1. Quét thư mục `.skills/` tìm file phù hợp (VD: `.skills/compose-ui/SKILL.md`).
   2. Khéo léo chèn quy tắc mới (VD: *"⚠️ KINH NGHIỆM: Không bao giờ truyền Context vào ViewModel"*).
   
- **Loại 2: Project-Specific (Đặc thù riêng, Workarounds, Business Logic, API móp méo của BE dự án này):**
   1. Tuyệt đối KHÔNG ghi vào `.skills/` để tránh "làm bẩn" kiến thức nền.
   2. Ghi vào file `.agent/project-conventions.md` (Nếu chưa có thì tự tạo mới).
   3. File này đóng vai trò là "Nguyên tắc Tối Cao" (Supreme Rules) chỉ dành riêng cho dự án hiện tại và sẽ ghi đè các nguyên tắc chung của Skill nếu có xung đột.

## Bước 4: Báo cáo tiến độ cho User
- Thông báo ngắn gọn: "Tôi đã phân tích lỗi kỹ thuật vừa roi, lưu bài học vào `LEARNING_JOURNAL.md`, đồng thời cập nhật quy tắc này vào file `[Tên file skill]` để ghi nhớ sâu cho mọi tác vụ trong tương lai."
