---
title: Agent Guide
description: Hướng dẫn bắt đầu và quy trình làm việc với Agent
---

# Cách bắt đầu một phiên làm việc (Prompting)

Hệ thống được thiết kế hỗ trợ các file Rule Global (`.cursorrules`, `.windsurfrules`, `.github/copilot-instructions.md`). Nếu IDE của bạn đã tự động nhận diện các file này, bạn **không cần** nhồi nhét bối cảnh. Hãy dùng **prompt cực kỳ ngắn gọn**:

> "Tạo tính năng Login."
> "Sửa lỗi crash ở biểu đồ màn hình Home."

**Dành riêng cho Android Studio (phiên bản mới tích hợp Gemini):**
Gemini trên các phiên bản Android Studio mới nhất đã tự động có khả năng nhận diện và quét các file đặc thù như `AGENTS.md` hoặc `AGENT_GUIDE.md` trong Workspace. Đối với nền tảng này, dự án của bạn cũng được tự động kế thừa hoàn toàn bộ não AI mà không phải nhồi thêm context dài dòng bên ngoài!

**Trường hợp không sử dụng các IDE hỗ trợ Global Rules (hoặc chat trên web/Claude/ChatGPT):**
Hãy "đánh thức sâu" (Deep Onboarding) cho Agent bằng câu lệnh chi tiết sau trước khi bắt đầu thay vì đi thẳng vào code:

> "Tác vụ: [Tên tác vụ]. Trước khi bắt đầu, hãy học thuộc file \`.agent/instructions.md\`. Hãy rà soát xem yêu cầu này cần dùng các \`.skills/\` nào, trích xuất quy trình làm việc chuẩn trong \`.agent/workflows/\` và trình bày Implementation Plan cho tôi duyệt trước khi viết code."

# Các quy trình (Workflows) phổ biến

Đừng ra lệnh chung chung. Hãy chỉ định quy trình cụ thể để Agent hoạt động chính xác:

## 1. Khi làm tính năng mới
**Câu lệnh:** "Thực hiện theo quy trình .agent/workflows/new-feature.md để xây dựng tính năng [Tên tính năng]."

**Kết quả:** Agent sẽ đi từ Domain -> Data -> DI -> Test và tự động thiết kế UI/Preview.

## 2. Khi cần sửa Bug
**Câu lệnh:** "Sử dụng quy trình .agent/workflows/bug-fixing.md để xử lý lỗi [Mô tả lỗi]. Đây là Stacktrace: [Dán log vào]."

**Kết quả:** Agent sẽ viết Test để tái hiện lỗi trước khi sửa code.

## 3. Khi cập nhật API từ Backend
**Câu lệnh:** "Chạy quy trình .agent/workflows/api-sync.md với file Swagger này: [Đường dẫn file]."

**Kết quả:** Tự động cập nhật DTO, Retrofit Service và Mappers.

## 4. Rà soát chuẩn hóa Code (Code Review)
**Câu lệnh:** "Sử dụng workflow .agent/workflows/code-review.md để rà soát chất lượng code file [Tên file]."

**Kết quả:** Agent sẽ đóng vai trò Senior đánh giá lại file của bạn (Performance, Clean Architecture, Security) và đưa ra các đề xuất Refactor.

## 5. Khởi tạo dự án mới (Project Init)
**Câu lệnh:** "Chạy quy trình .agent/workflows/project-init.md để thiết lập kiến trúc nền tảng."

**Kết quả:** Agent sẽ thiết lập Version Catalog, xây dựng chuẩn Multi-module với `:core:domain`, `:core:data` và `designsystem` khép kín.

## 6. Cập nhật/Nâng cấp tính năng cũ (Feature Update)
**Câu lệnh:** "Dựa theo .agent/workflows/feature-update.md, hãy thêm/sửa logic [Mô tả] vào tính năng [Tên]."

**Kết quả:** Agent tự động rà soát sự lệ thuộc (Impact Analysis), thiết kế mở rộng code cũ an toàn, bổ sung Unit Test và cập nhật UI/Preview tương ứng.

## 7. Chuẩn bị phát hành (Release)
**Câu lệnh:** "Theo dõi quy trình .agent/workflows/release-preparation.md để chuẩn bị build app."

**Kết quả:** Rà soát tự động Linter, chặn lỗi R8/ProGuard crash ẩn, và gợi ý nâng phiên bản `versionCode`.

## 8. Cập nhật Nhật ký phát triển (Logwork Update)
**Câu lệnh:** "Hãy chạy quy trình .agent/workflows/logwork-update.md để ghi lại các quyết định kỹ thuật vừa rồi."

**Kết quả:** Agent tự động cập nhật `LOGWORK.md`, đánh dấu Phase hoàn thành và lưu trữ các "Rationale" (lý do) đằng sau kiến trúc.

## 9. Học hỏi và Ghi chú sai lầm (Learning & Adaptation)
**Câu lệnh:** "Hãy rút kinh nghiệm từ lỗi vừa rồi (hoặc task vừa xong) bằng cách chạy quy trình .agent/workflows/learning-journal.md."

**Kết quả:** Agent sẽ thực hiện "Post-mortem" phân tích nguyên nhân lỗi, ghi vào `LEARNING_JOURNAL.md`, và tự động tinh chỉnh lại các file `.skills/` thiết yếu để thông minh hơn trong các task sau (tránh lấp lại cùng một lỗi).

## 10. Khám phá Dự án mới (Project Discovery)
**Câu lệnh:** "Dự án này hoàn toàn mới với bạn. Hãy chạy .agent/workflows/project-discovery.md để phân tích kiến trúc."

**Kết quả:** Agent tự động scan Gradle, thư mục DI, Navigation và tạo ra bản đồ kiến trúc toàn cảnh `ARCHITECTURE_MAP.md`.

## 11. Tối ưu Hiệu năng (Performance Tuning)
**Câu lệnh:** "App lướt khá giật lag. Hãy chạy .agent/workflows/performance-tuning.md để tìm memory leak và Recomposition rác."

**Kết quả:** Tự động đánh giá `@Stable`/`@Immutable` trong Compose, rà soát Coroutine Scope và Memory Leak.

## 12. Trả Nợ Kỹ Thuật (Tech-Debt Bản Trì)
**Câu lệnh:** "Hãy chạy .agent/workflows/tech-debt-refactoring.md để nâng cấp thư viện lên bản mới và xử lý code Deprecated."

**Kết quả:** Agent tự lập kế hoạch nâng cấp libs từng cụm, giải quyết an toàn các hàm đã lỗi thời.

## 13. Cài đặt CI/CD Mặc định (Automation)
**Câu lệnh:** "Hãy thiết lập phần Automartion Test CI/CD bằng quy trình .agent/workflows/ci-cd-setup.md."

**Kết quả:** Tự thiết kế file YAML Github Actions chuẩn bao gồm cache và Job rà Linter, Unit Test.

## 14. Nâng SDK & Google Play Policy (SDK Update)
**Câu lệnh:** "Đã đến lúc nâng cấp TargetSdk lên bản mới. Chạy quy trình .agent/workflows/target-sdk-update.md."

**Kết quả:** Agent nâng cấp phiên bản có hệ thống, sửa đổi Manifest, thêm Permissions mới và refactor các đoạn code Breaking Changes của hệ điều hành.

## 15. Nâng Mức Tối Thiểu & Dọn Code Cũ (minSDK Cleanup)
**Câu lệnh:** "Quyết định bỏ các máy đời cũ đi. Hãy nâng minSDK và chạy dọn code qua .agent/workflows/min-sdk-cleanup.md."

**Kết quả:** Sạch sẽ gỡ bỏ các đoạn `if (SDK_INT >= ...)`, xóa các Annotations `@RequiresApi` rác rưởi, và merge thư mục Resources thừa để giảm béo cho App.

# Mẹo để trở thành một "Đạo diễn" giỏi

*   **Sử dụng ký hiệu @:** Để gọi các Kỹ năng (Skill) đặc thù và ép AI phải làm theo phong cách dự án. Các skill nòng cốt bạn nên nhớ:
    *   `@clean-arch-logic`: Phục vụ tầng Domain (UseCase, Entity).
    *   `@api-integration`: Xử lý Data Source, Rest API, Retrofit.
    *   `@compose-ui-system`: Ràng buộc thiết kế UI và bắt buộc sinh Preview.
    *   `@hilt-di-config`: Quản lý Inject phụ thuộc.
    *   `@testing-logic`: Buộc phải tạo Unit Test kèm theo.
    *   `@feature-scaffold`: Tạo khung xương thư mục (Boilerplate) cho tính năng/module mới.
*   **Chia để trị (Divide & Conquer):** Đừng giao toàn bộ một Task khổng lồ cùng lúc. Hãy chia nhỏ thành các chặng (sub-tasks):
    1. "Hãy tạo file cho lớp Domain & Data (Repository & UseCases)."
    2. "Bây giờ viết Unit test cho UseCases đó nhé."
    3. "Tiếp theo, hãy dựng UI và ViewModel dựa trên UseCase đã có."
*   **Kiểm soát theo từng giai đoạn:** Agent được hướng dẫn để dừng lại sau mỗi giai đoạn (Domain, Data, UI). Hãy Review diff trước khi cho phép nó chạy bước kế tiếp.
*   **Cập nhật "Bộ não":** Nếu bạn thấy Agent thường xuyên làm sai một chi tiết nhỏ (ví dụ: đặt tên biến không đúng ý), đừng chỉ sửa code. Hãy sửa trực tiếp file .skills/ tương ứng. Từ đó về sau, Agent sẽ không bao giờ mắc lại lỗi đó nữa.

# Nguyên tắc vàng (Golden Rules)

*   **AI viết code - Bạn duyệt code:** Luôn kiểm tra Diff trước khi Accept.
*   **Không có Test, không có Code:** Mọi Logic mới đều phải đi kèm Unit Test (đã quy định trong Workflow).
*   **Giữ cho .skills luôn sạch:** Đây là "nguồn sự thật" duy nhất về phong cách lập trình của dự án.

---

Hệ thống này giống như một thực thể sống. Khi dự án của bạn lớn lên, hãy bổ sung thêm các Skill mới (ví dụ: performance-optimization, security-check). Bạn đã chính thức bước vào kỷ nguyên "Software Engineering 2.0" – nơi lập trình viên dành nhiều thời gian để Thiết kế tư duy hơn là Gõ phím.

Cảm ơn bạn đã cùng mình xây dựng bộ khung này. Chúc dự án Android của bạn thành công rực rỡ!