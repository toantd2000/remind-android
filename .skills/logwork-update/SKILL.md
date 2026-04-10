---
name: logwork-update
description: Kỹ năng chuyên biệt để tự động xác định, phân loại và ghi lại các quyết định kỹ thuật quan trọng (Technical Decision Records - TDR) vào LOGWORK.md.
metadata:
  author: Android-Expert
  version: "2.0"
---

# HƯỚNG DẪN CẬP NHẬT LOGWORK (TDR SYSTEM)

Sử dụng kỹ năng này khi hoàn thành một task quan trọng, một Phase, hoặc khi có thay đổi mang tính hệ thống (Kiến trúc, Logic core, Design System).

## 1. Cơ chế Tự động Xác định (Auto-Detection)

Trước khi ghi log, Agent phải rà soát:
*   **Context:** Nội dung trong `walkthrough.md` và `task.md` của session hiện tại.
*   **Changes:** Các file đã thay đổi (Diff).
*   **Rationale:** Tại sao lại thực hiện thay đổi đó? Có giải pháp thay thế nào không? (Dựa trên bối cảnh trò chuyện).

## 2. Tiêu chí lựa chọn sự kiện ghi Log

KHÔNG ghi lại các thay đổi nhỏ lẻ (fix typo, đổi tên biến cục bộ, cập nhật UI minor).
CHỈ ghi lại khi:
- Thay đổi cấu trúc Module/Package.
- Thay đổi Pattern (DI, MVI/MVVM, Clean Arch).
- Quyết định sử dụng/thay đổi thư viện bên thứ 3.
- Thay đổi logic nghiệp vụ cốt lõi (Core Business Logic) ảnh hưởng đến nhiều màn hình.
- Thay đổi quy chuẩn Design System (Color Palette, Typography).

## 3. Cấu trúc Technical Decision Record (TDR)

Mỗi entry mới phải tuân thủ định dạng sau:

### [TDR-XXX] - [Tiêu đề ngắn gọn]
- **Ngày thực hiện:** YYYY-MM-DD
- **Trạng thái:** [Accepted | Deprecated | Superseded by TDR-YYY]
- **Bối cảnh (Context):** Vấn đề gì đang gặp phải? Tại sao cần thay đổi?
- **Quyết định (Decision):** Giải pháp đã thực hiện là gì? Tại sao chọn nó?
- **Hệ quả (Consequences):** Ảnh hưởng tích cực/tiêu cực đến kiến trúc, hiệu năng hoặc trải nghiệm DEV.

## 4. Quy trình thực hiện (Workflow)

Khi nhận lệnh cập nhật Logwork, Agent thực hiện:

1.  **Thu thập:** Đọc `LOGWORK.md` để lấy ID tiếp theo (ví dụ TDR-014).
2.  **Phân loại:** Xác định xem có bao nhiêu quyết định thực sự quan trọng trong session này.
3.  **Draft:** Soạn thảo nội dung TDR theo template. Ngôn ngữ: Tiếng Việt kỹ thuật.
4.  **Cập nhật Roadmap:** Nếu hoàn thành một Phase, đánh dấu `[x]` vào "Lộ Trình Tổng Thể".
5.  **Ghi file:** Append vào mục "Nhật Ký Chi Tiết" trong `LOGWORK.md`.
6.  **Xác nhận:** Liệt kê các ID TDR vừa được thêm mới cho người dùng.

## 5. Ràng buộc (Constraints)

- **ID duy nhất:** Không được trùng lặp ID TDR.
- **Súc tích:** Context và Decision nên viết dưới dạng bullet points hoặc đoạn văn ngắn.
- **Tính khách quan:** Ghi lại cả những rủi ro đi kèm nếu có.
