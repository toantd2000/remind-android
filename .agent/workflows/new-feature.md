# Workflow: Phát triển tính năng mới (New Feature)

Quy trình này áp dụng khi bắt đầu xây dựng một tính năng từ con số 0.

## Giai đoạn 1: Phân tích & Chuẩn bị (Analysis)
1. **Đọc hiểu yêu cầu:** Xác định các User Stories và dữ liệu cần thiết.
2. **Kiểm tra API:** Nếu có Swagger/JSON, hãy sử dụng kỹ năng xử lý API để định nghĩa các DTO (Data Transfer Objects).
3. **Xác định Module:** Sử dụng `@module-manager` để quyết định xem tính năng này nằm trong module hiện có hay cần tạo module `:features:xxx` mới.
4. **Khởi tạo Code Base:** Kích hoạt mã bộ khung `@feature-scaffold` để generate ra cấu trúc thư mục tự động nếu bạn vừa quyết định tạo module mới.
5. **Cấu hình Dependency:** Nếu tạo module mới, hãy đảm bảo `build.gradle.kts` của module đó đã khai báo `implementation(project(":core:designsystem"))` để có quyền truy cập vào Theme và Components.

## Giai đoạn 2: Thiết kế lớp Domain (The Heart)
*Kích hoạt: @clean-arch-logic*
1. Tạo các **Entities** (Domain Models) thuần túy.
2. Định nghĩa **Repository Interface**.
3. Viết các **UseCases** cho từng hành động (ví dụ: `GetUserDataUseCase`).

## Giai đoạn 3: Thực thi lớp Data (Implementation)
*Kích hoạt: @api-integration* (nếu có tương tác với Server Network)
1. Tạo **Remote/Local Data Sources** (Retrofit Service hoặc Room DAO).
2. Triển khai **RepositoryImpl**.
3. Viết **Mappers** để chuyển đổi qua lại giữa DTO và Entity.
4. Kích hoạt: @security-hardening. Kiểm tra xem dữ liệu có nhạy cảm không? Nếu có, yêu cầu sử dụng EncryptedSharedPreferences hoặc mã hóa trước khi lưu

## Giai đoạn 4: Cấu hình Dependency Injection (DI)
*Kích hoạt: @hilt-di-config*
1. Thêm `@Inject constructor` vào các lớp đã tạo.
2. Cập nhật hoặc tạo mới **Hilt Module** để cung cấp (bind/provide) Repository và UseCase.

## Giai đoạn 5: Kiểm thử (Testing)
*Kích hoạt: @testing-logic*
1. Viết **Unit Test** cho UseCase (Mock Repository).
2. Viết **Unit Test** cho RepositoryImpl (nếu cần thiết).
3. Chạy test và đảm bảo tỷ lệ vượt qua là 100%.

## Giai đoạn 6: Xây dựng giao diện (UI & Presentation)
*Kích hoạt: @compose-ui-system*
1. Thiết lập **ViewModel** và **UiState** để kết nối với UseCase.
2. Thiết kế màn hình bằng **Jetpack Compose**.
3. Bắt buộc phải viết **`@Preview`** cho mọi UI component/screen được tạo ra với các trạng thái đầy đủ (Loading, Success, Error).

## Giai đoạn 7: Hoàn tất & Cập nhật Tài liệu (Finalization & Documentation)
1. Liệt kê lại các file đã tạo/chỉnh sửa (bao gồm cả file UI có chứa Preview).
2. **Cập nhật CHANGELOG.md:** Ghi lại tính năng mới cho người dùng.
3. **Cập nhật ARCHITECTURE_MAP.md:** Nếu có module mới được tạo.
4. **Cập nhật DECISION_LOG.md:** Nếu có các kịch bản hành vi quan trọng được chốt.
5. **Kích hoạt `@logwork-update`:** BẮT BUỘC ghi nhận TDR vào `LOGWORK.md` cho các quyết định kiến trúc.
6. **Kích hoạt `@learning-journal`:** Ghi lại các lỗi gặp phải và bài học kinh nghiệm.