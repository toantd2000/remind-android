---
name: stitch-design-sync
description: >-
  Synchronizes Material 3 Design System tokens (from :litever-designsystem and :core:designsystem)
  and Jetpack Compose UI screens to Google Stitch via StitchMCP. Use when the user asks to
  push, generate, or sync themes or Compose screens to Stitch.
metadata:
  author: Litever-Android
  version: "2.0"
---

# Google Stitch Synchronization (Theme & Compose Screens)

## Workflow A: Sync Library & Local Theme Tokens to Stitch

1. **Extract Base & Local Tokens:**
   - Inspect base library tokens in `../litever-designsystem/designsystem/src/main/java/vn/io/litever/designsystem/theme/` (`Color.kt`, `Shape.kt`, `Type.kt`, `Spacing.kt`).
   - Inspect local app overrides in `core/designsystem/src/main/java/vn/io/litever/remind/core/designsystem/theme/` (`Color.kt`, `Theme.kt`).
2. **Merge & Normalize:**
   - Local tokens in `:core:designsystem` override base tokens in `:litever-designsystem`.
   - Convert Compose `Color(0xFFRRGGBB)` to `#rrggbb`, `RoundedCornerShape(x.dp)` to Stitch roundness enums (`ROUND_EIGHT`, `ROUND_TWELVE`, etc.), and `Typography` to Stitch font definitions.
3. **Push to StitchMCP:**
   - Call `StitchMCP.list_design_systems` / `StitchMCP.update_design_system` (or `create_design_system_from_design_md`) with the consolidated `DesignTheme` payload.

---

## Workflow B: Sync Jetpack Compose Screen to Stitch

1. **Identify Exact Composable Name (CRITICAL):**
   - Read the target `.kt` screen file in `:features:*` and its child components.
   - **Mandatory Naming Rule:** The Stitch Screen Title MUST match the exact Composable function name:
     - Standard screen: `<ComposableName>` (e.g., `AlarmEditScreen`, `RemindersScreen`).
     - Variant/Tab/State: `<ComposableName> - <Variant>` (e.g., `AlarmEditScreen - MissionSelect`).
2. **Translate Layout & Tokens:**
   - Map `Column` -> vertical flex container, `Row` -> horizontal flex container, `Box` -> overlay stack, `LazyColumn` -> vertical scrollable list.
   - Map modifiers (`padding`, `fillMaxWidth`, `Arrangement`, `Alignment`) and `:core:designsystem` components/tokens into a structured layout prompt.
3. **Generate/Update on StitchMCP:**
   - Retrieve target `projectId` and `designSystem` asset ID via `StitchMCP.list_projects` / `StitchMCP.list_design_systems`.
   - Invoke `StitchMCP.generate_screen_from_text` (or `edit_screens`) with `Screen Title: <ComposableName>`, `deviceType: MOBILE`, and the linked `designSystem`.
   - Return the generated Stitch Screen ID and preview details to the user.
