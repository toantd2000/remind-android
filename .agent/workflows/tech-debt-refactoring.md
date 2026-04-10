---
description: Quy trình bảo trì, nâng cấp thư viện và refactor diện rộng (Tech-debt maintenance).
---

# Mục tiêu
Nâng cấp các phiên bản thư viện cũ, loại bỏ mã nguồn chắp vá, giải quyết các hàm Deprecated, và refactor trên diện rộng nhưng **đảm bảo an toàn không rớt Build**.

# Các bước thực hiện

## Bước 1: Quét Dependency và mã nguồn lỗi thời (Deprecated Code)
1. Rà soát `libs.versions.toml` hoặc các khối Dependencies để phát hiện version thư viện bị tụt hậu so với framework chung (VD đổi Kotlin version thì phải đổi Compose Compiler tương ứng).
2. Quét qua dự án các Annotation `@Deprecated` đang được cảnh báo.

## Bước 2: Lập Kế hoạch Nâng cấp (Từng phần)
1. **Luôn chia để trị:** Tuyệt đối KHÔNG upgrade tất cả cùng lúc bằng lệnh mù quáng. Xử lý theo từng cụm tính năng logic (VD: Cụm Kotlin/Core, Dagger/Hilt, Compose UI).
2. Sau khi nâng phiên bản 1 cụm nhỏ, luôn phải sync gradle và kiểm tra lỗi API Breaking Changes.
3. Thay thế các function khai tử bằng API mới theo Doc của nhà cung cấp.

## Bước 3: Dọn dẹp Code Smell
1. Xử lý các Warnings từ IDE linter (như wildcard imports, unused variables). Đóng gói các public member không ai gọi thành private.
2. Ép chuẩn Naming Conventions cho thống nhất nếu phát hiện nhiều style ngổn ngang. 

## Bước 4: Kiểm chứng & Ghi vết (Verification & Logging)
1. Viết bổ sung hoặc kích hoạt chạy các `Unit Test` có liên quan tác động đến đoạn Code mới.
2. Phải yêu cầu build thử, đảm bảo ProGuard/R8 rules không cắn mất code do lỗi reflection.
3. **Kích hoạt `@logwork-update`:** Lưu lại các quyết định Refactor quan trọng (thay đổi thư viện, đổi pattern) vào `LOGWORK.md`.
