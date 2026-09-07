---
name: sync-compose-to-stitch
description: Analyzes Jetpack Compose UI code, translates the layout/components into structured design specifications, and generates/synchronizes screens on Google Stitch using the project's Design System.
metadata:
  author: Android-Expert
  version: "1.0"
---

# Analyze and Synchronize Jetpack Compose UI to Google Stitch

## Objective
Convert existing Jetpack Compose code into an accurate UI screen structure on Google Stitch, ensuring full alignment with the project's established Design System ("ThuChi / LiteVer App").

---

## Execution Steps

### Step 1: Read Source Code & Identify Screen Name
1. Read the designated `.kt` file specified by the user (typically located within feature presentation/UI layers or `:impl` modules).
2. **Identify the exact Composable function name** representing the screen/component (e.g. `AddEditTransactionScreen`, `CategorySelectionSheet`, `SourceSelectionSheet`, `HomeScreen`, `SettingsScreen`).
   - **QUY TẮC ĐẶT TÊN MÀN HÌNH TRÊN STITCH (CRITICAL):**
     - Khi đồng bộ hoặc tạo màn hình trên Stitch, **bắt buộc phải lấy chính xác tên hàm Composable chứa nó** làm tiêu đề/tên màn hình (Screen Title / Screen Name), ví dụ: `AddEditTransactionScreen`, `CategorySelectionSheet`, `SourceSelectionSheet`.
     - Nếu một màn hình có các biến thể con hoặc tab/trạng thái đặc thù (như form Chuyển khoản hoặc Sửa giao dịch), đặt tên theo quy ước: `<TênComposable> - <Biến thể / Trạng thái>`, ví dụ: `AddEditTransactionScreen - Transfer` hoặc `CategorySelectionSheet`.
     - Việc này giúp dễ dàng tìm kiếm, lọc và đối chiếu 1-1 giữa code Compose và màn hình trên Stitch.
3. Traverse and inspect associated sub-composables, item cards, bottom sheets, and dialogs referenced in that screen.

### Step 2: Analyze UI Structure (AST & Layout Mapping)
Extract layout hierarchy and styling rules:
1. **Layout Structures:**
   - Map `Column` to vertical flexbox/layout containers (`flex-direction: column`).
   - Map `Row` to horizontal flexbox/layout containers (`flex-direction: row`).
   - Map `Box` to stack / overlay containers (`position: relative / absolute`).
   - Map `LazyColumn` / `LazyVerticalGrid` to scrollable lists and grid layouts.
2. **Modifiers Translation:**
   - Translate spacing and sizing: `padding(x.dp)` -> padding tokens, `fillMaxWidth()` -> full width (`width: 100%`), `size(x.dp)`, `height(x.dp)`, `width(x.dp)`.
   - Translate alignment & arrangement: `Arrangement.SpaceBetween`, `Arrangement.Center`, `Alignment.CenterVertically`.
3. **M3 Components & Design Tokens:**
   - Identify standard Material 3 components: `Scaffold`, `TopAppBar`, `Card`, `Button`, `IconButton`, `Text`, `FloatingActionButton`, `TextField`, `NavigationBar`.
   - Extract used color tokens (`MaterialTheme.colorScheme.*` or `LiteverTheme.colors.*`) and typography scales (`MaterialTheme.typography.*`).
4. **Structured Representation:**
   - Formulate a detailed, structured UI specification (or HTML/JSON layout hierarchy) capturing elements, labels, layout hierarchy, and styling instructions.

### Step 3: Call Stitch MCP
1. Identify the target Stitch project:
   - Retrieve existing project via `StitchMCP.list_projects` or `StitchMCP.get_project` (e.g., the established project for "ThuChi / LiteVer App", such as `projects/3635605609061760877`).
   - Identify the configured Design System asset (e.g., `assets/17665631694429130515`).
2. Call `StitchMCP.generate_screen_from_text` (or equivalent Stitch tool):
   - Provide `projectId`: Target project ID.
   - Provide `prompt`: The detailed structural UI layout description, hierarchy, component types, visual styling, and content translated from Step 2.
     - **BẮT BUỘC TRONG PROMPT:** Phải chỉ định rõ tiêu đề màn hình trên Stitch là tên hàm Composable: `Screen Title: <TênComposable>` (hoặc `<TênComposable> - <Biến thể>`), ví dụ: `Screen Title: AddEditTransactionScreen` hoặc `Screen Title: CategorySelectionSheet`.
   - Provide `designSystem`: ID of the project's design system asset to enforce brand colors, fonts (e.g. Nunito Sans), and shapes.
   - Provide `deviceType`: Set to `MOBILE` (or `TABLET`/`DESKTOP` according to screen target).
   - Set `modelId` (e.g., `GEMINI_3_1_PRO` or `GEMINI_3_FLASH`).
3. If an asynchronous timeout occurs, query screen status using `StitchMCP.get_screen` periodically.

### Step 4: Return Results & Verification
1. Extract the generated Screen ID and URL from Stitch response.
2. Provide the screen ID, title, and direct link to the user for visual review.
3. Validate that generated components match the original Jetpack Compose UI architecture and Material 3 design tokens.
