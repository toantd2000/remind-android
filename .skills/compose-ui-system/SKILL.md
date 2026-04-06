---
name: compose-ui-system
description: Hướng dẫn Agent thiết kế giao diện bằng Jetpack Compose theo chuẩn Design System của dự án.
metadata:
  author: Android-Expert
  version: "1.0"
---

# QUY TẮC THIẾT KẾ UI

### 1. Nguyên tắc State Management
- **State Hoisting:** Luôn đẩy State lên cấp cao nhất có thể (thường là ViewModel).
- **UI State:** Sử dụng `data class` hoặc `sealed class` để đại diện cho toàn bộ trạng thái màn hình (ví dụ: `HomeUiState`). Khuyến khích sử dụng `Flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)` trong ViewModel thay cho việc update thủ công `MutableStateFlow` nếu có thể.
- **Events:** Truyền các lambda function (ví dụ: `onItemClick: () -> Unit`) xuống các sub-composables thay vì truyền ViewModel.

### 2. Thành phần giao diện (Components & Reusability)
- **Tái sử dụng (Reusability):** Trước khi code một Custom Component, PHẢI dùng lệnh tìm kiếm bên trong `:core:designsystem/src/.../components` xem đã có sẵn component tương tự chưa. Nếu nó chưa tồn tại nhưng có tiềm năng dùng lại (ví dụ một AlarmCard), hãy tạo mới nó ở `:core:designsystem`, tuyệt đối KHÔNG tạo cục bộ trong `:features:xxx`. 
- **Design System:** LUÔN sử dụng các Composable chung từ `ui.components` thay vì dùng API của androidx.compose.
- **Theme:** Sử dụng Theme được cấu hình sẵn cho chuẩn dự án (ví dụ `MaterialTheme.colorScheme`). Tuyệt đối không hardcode mã màu HEX (ví dụ: #FF0000).

### 3. Hiệu suất (Performance)
- Sử dụng `remember` và `derivedStateOf` để tối ưu hóa Recomposition.
- Danh sách dài PHẢI dùng `LazyColumn` hoặc `LazyVerticalGrid`.
- **Side Effects:** Chú ý truyền đúng `key` vào `LaunchedEffect` để tránh fetch data lặp lại vô ích. Giải phóng tài nguyên lắng nghe bằng `DisposableEffect` khi Composable rời khỏi cây (leave composition).

# WORKFLOW CHO AGENT
Khi tôi yêu cầu "Vẽ màn hình [X]":
1. Định nghĩa `UiState` cho màn hình đó.
2. Tạo một Composable cấp cao (Stateless) nhận `UiState` và các `Events`.
3. Tách màn hình thành các Composable nhỏ (Components) để dễ quản lý.
4. Sử dụng `Preview` cho mỗi Composable với các trạng thái khác nhau (Loading, Success, Error).