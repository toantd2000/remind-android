---
name: litever-design-system
description: >-
  Enforces UI implementation rules for ReMind using :core:designsystem and the shared
  :litever-designsystem composite build library. Use whenever creating or modifying
  Jetpack Compose screens, UI components, themes, colors, or Previews.
metadata:
  author: Litever-Android
  version: "2.0"
---

# Litever & ReMind Design System Rules

## 1. Component Single Source of Truth

The project uses a 2-tier Design System via composite build (`includeBuild("../litever-designsystem")`):
1. **`:litever-designsystem`** (`vn.io.litever.designsystem.*`): Shared foundational M3 library (`LiteverTheme`, `LiteverColors`, `LiteverButton`, `LiteverCard`, etc.). Must remain 100% generic with zero business logic.
2. **`:core:designsystem`** (`vn.io.litever.remind.core.designsystem.*`): App-specific theme tokens and wrapper components for ReMind.

### Component Usage Rules in `:features:*`
- **Search Before Creating:** Always search `:core:designsystem` and `:litever-designsystem` before creating any custom UI component.
- **Interactive & Structural Components (`Button`, `Card`, `Dialog`, `BottomSheet`, `Scaffold`, `Switch`, `TopAppBar`, `TextField`):**
  - **NEVER** call raw `androidx.compose.material3.*` interactive components directly inside `:features:*`.
  - Always reuse or create a reusable wrapper in `:core:designsystem` (or `:litever-designsystem` with prefix `Litever*` if generic across apps).
- **Basic Primitives (`Text`, `Icon`, `Box`, `Row`, `Column`, `Spacer`):**
  - May be used directly in `:features:*`, styled strictly via `LiteverTheme.colors` / `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- **No Hardcoded Visuals:** Never hardcode HEX colors (`Color(0xFF...)`) or raw font sizes inside `:features:*`.

## 2. Adding or Extending Design System Components

1. **In `:litever-designsystem`:**
   - Place in `vn.io.litever.designsystem.components` with prefix `Litever` (e.g., `LiteverChip`).
   - Expose `modifier: Modifier = Modifier`, colors, and typography parameters defaulting to `LiteverTheme.colors` and `LiteverTheme.typography`.
2. **In `:core:designsystem`:**
   - Provide app-specific color palettes by instantiating `LiteverColors` (all 36 M3 color tokens) in `Color.kt` and passing them to `LiteverTheme` in `Theme.kt`.

## 3. Mandatory Previews & Visual Verification

- Every new or modified Screen/Component MUST include `@Preview` functions covering both **Light** and **Dark** modes (and key `UiState` variants: Loading, Success, Empty/Error).
- Wrap previews in the project's theme (`ReMindTheme` / `LiteverTheme`).
- Activate the **`compose-preview`** tool after UI changes to render and visually inspect `@Preview` functions directly.
