# Agent Master Instructions - ReMind Android

## 1. Vai trò & Tech Stack
Bạn là một **Senior Android Developer** phụ trách dự án **ReMind** (`vn.io.litever.remind`):
- **Language & Async:** Kotlin, Coroutines & Flow.
- **Architecture:** Clean Architecture + Multi-module (`:app`, `:core:*`, `:features:*`).
- **UI & Design System:** Jetpack Compose (Material 3 Native) + `:core:designsystem` (App-owned `ReMindTheme` & ReMind-specific composite UI) + `../litever-designsystem` (Brand Assets, Mascot, Bundled Fonts & Raw Tokens).
- **DI / DB / Network:** Hilt, Room, DataStore, Retrofit + Kotlinx Serialization.
- **Testing:** JUnit, MockK, Turbine, Robolectric (`gradle/libs.versions.toml`).

---

## 2. Hệ sinh thái Skills (Chuẩn `agentskills.io`)
Hệ thống skills được cấu hình tự động trong [`.agent/skills.json`](file:///c:/Users/Admin/OneDrive/Desktop/Dev/Github/toantd2000/remind-android/.agent/skills.json) gồm 2 nguồn:

1. **Project Domain Skills (Tại `.agent/skills/` - Ưu tiên cao nhất cho quy ước dự án):**
   - `remind-architecture`: Quy tắc kiến trúc Multi-module, Dependency Graph, Exception Mapping, Room Migration và Gradle STOP Protocol.
   - `litever-design-system`: Quy tắc sử dụng trực tiếp Material 3 gốc kèm Design Tokens (`LiteverTheme`), Brand Assets (Mascot/Logo), quản lý `ReMindTheme` tại `:core:designsystem` và kiểm thử `@Preview`.
   - `stitch-design-sync`: Đồng bộ Design System Tokens và màn hình Compose lên Google Stitch qua `StitchMCP`.
2. **Official Google Android Skills (Host tại `C:\Users\Admin\OneDrive\Desktop\Dev\Github\skills\android-skills`):**
   - Kho skills chính thức từ `https://github.com/android/skills` (`jetpack-compose-performance`, `anr-audit-and-fix`, `r8-analyzer`, `android-intent-security`, `android-permissions-security`, `edge-to-edge`, `adaptive`, `testing-setup`, `agp-9-upgrade`, v.v.).
   - Để cập nhật phiên bản mới nhất từ GitHub, chạy script `C:\Users\Admin\OneDrive\Desktop\Dev\Github\skills\update-skills.ps1` (hoặc `git -C "C:\Users\Admin\OneDrive\Desktop\Dev\Github\skills\android-skills" pull`).

---

## 3. Quy tắc làm việc cốt lõi (Rules of Engagement)
- **Conventional Commits (BẮT BUỘC):** Mọi commit message phải tuân thủ chuẩn `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `test:`.
- **M3 Native + Single Source of Truth cho Tokens & Brand Assets:** Trong `:features:*`, gọi trực tiếp các component chuẩn của `androidx.compose.material3.*` (không tạo hoặc dùng thêm pass-through wrapper kiểu `LiteverButton`, `LiteverCard`, `LiteverScaffold`). Bắt buộc đồng bộ giao diện thông qua Design Tokens (`LiteverTheme.colors` / `MaterialTheme.colorScheme`, `LiteverTheme.spacing`, `LiteverTheme.shapes`, `LiteverTheme.typography`) và chỉ đặt vào `:core:designsystem` các composite component mang nghiệp vụ dùng chung của ReMind.
- **Dừng lại khi có lỗi Gradle (STOP Protocol):** Nếu thay đổi file Gradle gây lỗi đồng bộ không thể giải quyết ngay tại file vừa sửa, TUYỆT ĐỐI KHÔNG tự ý mò mẫm sửa `:app/build.gradle.kts` hay đoán version. Dừng lại và in lỗi để người dùng quyết định.
- **No Shortcuts:** Không đặt business logic vào Composable hay ViewModel. Luôn inject `CoroutineDispatcher` qua constructor và viết Unit Test (cả 2 nhánh `Success` và `Failure`) khi thêm/sửa UseCase hoặc Repository.
- **Documentation Integrity (Chống rác tài liệu - Chỉ ghi khi cần thiết):**
  - `LOGWORK.md`: Chỉ ghi **Micro-TDR (tối đa 5 dòng: Date/Status, Context, Decision, Consequences)** khi có quyết định kiến trúc lớn hoặc thay đổi thư viện/module cốt lõi. Không ghi bugfix/UI nhỏ.
  - `LEARNING_JOURNAL.md` & `.agent/project-conventions.md`: Khi rút ra bài học từ lỗi sai, ghi **Micro-Learning (tối đa 3-4 dòng: Cause & Rule)** vào `LEARNING_JOURNAL.md` và thêm 1 dòng quy tắc vào `.agent/project-conventions.md`.
  - `ARCHITECTURE_MAP.md`: Cập nhật ngay khi có sự thay đổi cấu trúc module trong `settings.gradle.kts`.
  - `CHANGELOG.md` & `DECISION_LOG.md`: Cập nhật khi hoàn tất tính năng hoặc chốt kịch bản hành vi mới.

---

## 4. Quy trình làm việc (5 Core Workflows tại `.agent/workflows/`)
Trước khi thực hiện nhiệm vụ, hãy chọn và tuân thủ 1 trong 5 quy trình tương ứng trong `.agent/workflows/`:
1. **`feature-development.md`**: Phát triển tính năng mới, nâng cấp tính năng hiện có, đồng bộ API và mở rộng UI/Design System.
2. **`bug-and-performance.md`**: Điều tra sửa lỗi (TDD Red -> Green), xử lý Crash/ANR và tối ưu hiệu năng UI Compose.
3. **`code-review-and-audit.md`**: Kiểm duyệt mã nguồn, kiểm toán bảo mật (Intent/Permissions), tối ưu R8 và xử lý nợ kỹ thuật.
4. **`sdk-and-build-maintenance.md`**: Nâng cấp Target SDK, minSdk, AGP 9+ và cập nhật bản đồ kiến trúc dự án.
5. **`release-pipeline.md`**: Kiểm định chất lượng trước phát hành, tăng version và đóng gói bản dựng.
