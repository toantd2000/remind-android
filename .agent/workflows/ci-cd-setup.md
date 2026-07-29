---
description: Xây dựng cầu nối tích hợp và phân phối tự động CI/CD cho dự án Android.
---

# Mục tiêu
Thiết lập hệ thống CI/CD chạy tại máy cá nhân (Local CI/CD) với mô hình Human-in-the-loop (Kết hợp giữa Con người và AI Agent). AI Agent sẽ đóng vai trò như một người trợ lý tự động gõ lệnh và rà soát, còn Lập trình viên sẽ là người duyệt và ra quyết định.

# Các thành phần chính

## 1. Fastlane
- **Vị trí**: `fastlane/Fastfile`
- **Nhiệm vụ**: Đóng gói ứng dụng, tải Metadata (Release Notes) và Ảnh chụp màn hình, sau đó tự động phân phối lên Google Play (Internal Track hoặc Production) thông qua Google Play Developer API.
- **Tuỳ biến**: Fastlane đã được thiết lập để có thể linh hoạt chỉ upload Metadata (không cần build lại APK/AAB) nếu không có thay đổi về mã nguồn.

- **Vị trí**: `features/alarms/src/test/`

## 3. AI Agent (Hướng dẫn tự động hóa)
- **Vị trí**: `.agent/workflows/release-preparation.md`
- **Nhiệm vụ**: Chứa "Prompt" và các quy tắc để bất kỳ AI Agent nào cũng có thể hiểu và tự động thực hiện tiến trình giải phóng phiên bản mới. AI sẽ rà soát `git diff`, phân tích thay đổi, chạy lệnh gradle, chạy fastlane và tạo `git tag`.

# Các bước thực hiện chung
1. Thêm các biến môi trường và file credential như `fastlane-credentials.json` (được đưa vào `.gitignore` để bảo mật).
2. Khi muốn Release, người dùng sử dụng file quy trình ở `.agent/workflows/release-preparation.md` để khởi chạy quy trình (nhờ AI Agent đọc và thực thi).
3. Đảm bảo file Changelog trong `docs/changelog` đã được viết.
4. AI chạy quy trình và xin ý kiến duyệt ở các điểm chốt (như chuẩn bị push code hay upload AAB).
