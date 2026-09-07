---
name: sync-library-theme
description: Extracts and merges Material 3 Design System tokens (Colors, Shapes, Typography) from the remote Litever library and local project overrides, then synchronizes them to Google Stitch.
metadata:
  author: Android-Expert
  version: "1.1"
---

# Extract and Merge Design System from Library and Local Project

## Objective
Extract all Material 3 (M3) specifications—including Colors, Shapes, and Typography—from the remote base library repository and local module overrides, merge them into unified design tokens, and push them to Google Stitch.

---

## Source Repositories & Paths
- **Remote Base Library:** [toantd2000/litever-designsystem](https://github.com/toantd2000/litever-designsystem)
  - Remote theme directory: `src/main/java/vn/io/litever/designsystem/theme/` (or equivalent theme package)
  - Inspect **ALL** theme files in this folder, including but not limited to:
    - `Color.kt`: Base color palettes, light/dark schemes, surface containers, contrast variants.
    - `Shape.kt`: Corner radii tokens (`RoundedCornerShape(x.dp)` for small, medium, large, full).
    - `Type.kt`: Typography scale (display, headline, title, body, label fonts, sizes, weights, line heights).
    - `Spacing.kt`: Spacing and elevation units (padding, margins, standard grid dimensions).
    - Any additional token or dimension definitions located under the theme package.
- **Local Overrides:** `core/designsystem/src/main/java/vn/io/litever/thuchi/core/designsystem/theme/`
  - Inspect **ALL** files present in this directory to detect any overrides against the remote library:
    - `Color.kt`: Local customized palettes (`primaryLight`, `surfaceLight`, `thuChiLightColors`, `thuChiDarkColors`, etc.).
    - `Theme.kt`: Local theme composable, dynamic coloring logic, palette routing.
    - Any other local theme files (e.g., local `Shape.kt`, `Type.kt`, or `Spacing.kt` if defined).

---

## Execution Steps

### Step 1: Extract Local Colors & Overrides
1. Inspect the local codebase in `core/designsystem/src/main/java/vn/io/litever/thuchi/core/designsystem/theme/Color.kt` (or the respective module's theme directory).
2. Retrieve color tokens that override the base theme (e.g., `primaryLight`, `secondaryLight`, `surfaceLight`, `backgroundLight`, and dark-mode variants).
3. Identify the custom primary seed color (e.g., `#4D662A`) and any palette specific customizations (`thuChiLightColors`, `thuChiDarkColors`).

### Step 2: Fetch Base Theme Tokens & Inspect ALL Files in Theme Directory
1. Access the UI library repository `https://github.com/toantd2000/litever-designsystem` using GitHub tools (`github.get_file_contents`, `github.list_directory_contents`, or `github.search_code`).
2. **List and inspect ALL files** in the library's theme directory (`src/main/java/vn/io/litever/designsystem/theme/`):
   - `Shape.kt`: Extract all corner radii definitions (`RoundedCornerShape(x.dp)` across all shape levels: extraSmall, small, medium, large, extraLarge, full).
   - `Type.kt`: Extract all typography definitions (font family, weight, size, line-height, letter-spacing for display, headline, title, body, and label).
   - `Spacing.kt`: Extract all spacing, padding, and layout dimension tokens.
   - `Color.kt`: Extract default base library colors and fallback tokens.
   - Check any other companion files (e.g., elevation, ripple, or custom theme attributes).
3. **Thorough Local Cross-check:**
   - Scan **ALL** files in the local theme directory `core/designsystem/src/main/java/.../designsystem/theme/`.
   - Compare each file against its counterpart in the base library to capture every local override (colors, shapes, type, dimensions).

### Step 3: Parse and Translate to Standard Design System Tokens
1. **Translate Compose Values:**
   - Convert Compose Dp values (e.g., `RoundedCornerShape(16.dp)`) to standard design tokens (e.g., `border-radius: 16px`, roundness enum `ROUND_TWELVE` or `ROUND_EIGHT`).
   - Map Typography scales (`headlineLarge`, `bodyMedium`, etc.) to standard CSS/JSON typography structures (`fontSize: "16px"`, `lineHeight: "24px"`, `fontWeight: "500"`, `fontFamily: "Inter"` / `"Be Vietnam Pro"`).
   - Normalize Compose hex colors (`Color(0xFF4D662A)`) to standard web hex strings (`#4d662a`).
2. **Merge & Resolve Hierarchy:**
   - Combine the remote library's base tokens with the local project's overrides. Local project tokens take precedence over base library tokens.
   - Format the consolidated token configuration into the target JSON structure required by Stitch.

### Step 4: Synchronize with Google Stitch
1. Format the resolved design tokens into the Stitch `DesignTheme` schema:
   - `customColor`: Primary theme color (e.g., `#4d662a`).
   - `colorMode`: `"LIGHT"` or `"DARK"`.
   - `roundness`: Corner roundness enum (e.g., `"ROUND_EIGHT"`, `"ROUND_TWELVE"`).
   - `headlineFont`, `bodyFont`, `labelFont`: Specified typography enums (e.g., `"BE_VIETNAM_PRO"`, `"INTER"`, etc.).
   - `typography`: Map of style level objects (`fontSize`, `fontWeight`, `lineHeight`, etc.).
   - `designMd`: Optional markdown documentation summarizing the design system.
2. Invoke `StitchMCP.update_design_system` (or `upload_design_md` / `create_design_system_from_design_md`):
   - Pass `projectId`, `name` (`assets/{asset_id}`), and the complete `designSystem` payload.
3. Verify successful synchronization and document the updated design tokens.
