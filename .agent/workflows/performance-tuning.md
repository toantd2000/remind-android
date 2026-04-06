---
description: Phân tích và tối ưu hiệu năng App (Memory Leak, Jank UI, ANR).
---

# Mục tiêu
Agent biến hình thành Optimization Engineer, bắt tay vào săn tìm và loại bỏ rò rỉ bộ nhớ, tối ưu lại UI drawing, và khống chế ANR để trả lại sự mượt mà cho App.

# Các bước thực hiện

## Bước 1: Thẩm định Logs và Metrics
1. User cung cấp các Log (từ nền tảng Crashlytics, LeakCanary report, hoặc profiler logs).
2. Nếu là rò rỉ bộ nhớ (Leak): Trace ngược stacktrace dể truy vết xem đâu là Object giữ References vòng, ví dụ việc pass thẳng Activity/Context vào Singleton hoặc Coroutine không cancel.

## Bước 2: Tối ưu UI Rendering
1. **Với Jetpack Compose:**
   - Soi và dán annotation `@Stable`, `@Immutable` cho các Data class nhằm skip Recomposition thừa.
   - Cấm việc Allocation (New class) ở trong thân một Composable function hoặc vòng lặp list dầy đặc.
   - Tối ưu State hosting, dùng `derivedStateOf`.
2. **Với XML Constraints:**
   - Giảm Overdraw, làm phẳng các layout lồng nhau.

## Bước 3: Quản lý luồng (Threading & Coroutines)
1. Đảm bảo mọi Request Network, Read/Write Database phải nằm ngoài Main Thread (Sử dụng Dispatchers.IO chuẩn).
2. Thay thế `GlobalScope` bằng `viewModelScope` để tránh các Thread chạy vô tận kể cả khi màn hình đã chế.

## Bước 4: Báo cáo Tối ưu hoá
- Agent xuất report chỉ ra Tình cạng Trước / Giải pháp đã fix / Tình trạng Sau để đưa vào Pull Request review.
