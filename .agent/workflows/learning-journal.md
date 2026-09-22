---
description: Quy trình học hỏi, phân tích lỗi sai và cập nhật kiến thức dự án (Learning & Self-Correction).
---

# Mục tiêu
Ghi nhận nhanh chóng các bài học từ sai lầm (Self-Correction) bằng định dạng SIÊU NGẮN, tránh tình trạng văn chương lê thê làm rác tài liệu.

# Ngữ cảnh sử dụng
- Khi Agent vừa gây ra bug hoặc áp dụng sai API.
- Khi người dùng chỉ ra quy ước đặc thù riêng (Project-specific rules).

# Các bước thực hiện SIÊU NGẮN (Micro-Learning)

## Bước 1: Trích xuất bài học cốt lõi
Trả lời trong 1 câu duy nhất: Lỗi là gì và cách làm đúng là gì?

## Bước 2: Ghi vào LEARNING_JOURNAL.md
Sử dụng template cực ngắn (Tối đa 3-4 dòng):
- **[YYYY-MM-DD] [Tên Lỗi Ngắn Gọn]**
- **Cause:** [1 câu giải thích sai lầm]
- **Rule:** [1 câu chỉ ra cách làm đúng]

## Bước 3: Nâng cấp "Bộ não" (QUAN TRỌNG NHẤT)
Bài học ghi ở `LEARNING_JOURNAL.md` chỉ là nhật ký. Quan trọng là phải CẬP NHẬT LUẬT:
- **Loại 1: Universal Knowledge:** Khéo léo chèn **1 dòng** vào file tương ứng trong thư mục `.skills/`.
- **Loại 2: Project-Specific:** Khéo léo chèn **1 dòng** vào `.agent/project-conventions.md`.

## Bước 4: Chống rác tài liệu
- KHÔNG phân tích nguyên nhân lê thê.
- KHÔNG copy paste cả đống code rườm rà vào Journal. Chỉ ghi cốt lõi.
- Nếu Journal > 300 dòng, đề xuất archive.
