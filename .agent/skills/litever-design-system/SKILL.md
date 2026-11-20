---
name: litever-design-system
description: >-
  Enforces UI implementation rules for ReMind using native Jetpack Compose Material 3,
  app-owned :core:designsystem, and the shared Brand & Token Kit (:litever-designsystem).
  Use whenever creating or modifying Jetpack Compose screens, UI components, themes, colors, or Previews.
metadata:
  author: Litever-Android
  version: "3.0"
---

# Litever Brand Kit & ReMind UI Architecture Rules

## 1. 3-Tier Decoupled UI Architecture (Zero-M3-Coupling Philosophy)

To eliminate version lock-in and wrapper maintenance overhead across projects, UI responsibilities are strictly separated into 3 layers:

1. **Shared Brand & Resource Kit (`:litever-designsystem` / `vn.io.litever.designsystem.*`):**
   - **Role:** Pure Brand Assets & Raw Design Tokens.
   - **Contains:**
     - Brand drawables/vectors: Litever Studio logo, Mascot ("Bé Cam Sành" status illustrations), shared custom icons.
     - Bundled fonts (`res/font/`): `Be Vietnam Pro` (static) and `Nunito` (variable) with OFL licenses.
     - Raw Design Tokens: Color palettes (7 rainbow palettes + semantic `warning`, `success`), `LiteverSpacing`, `LiteverShapes`, and `LiteverTypography`.
   - **Strict Rule:** Do **NOT** add new Material 3 component wrappers (`Lv*` / `Litever*`) to the shared library.

2. **App Design System (`:core:designsystem` / `vn.io.litever.remind.core.designsystem.*`):**
   - **Role:** App-Owned Theme Entry Point & ReMind Domain UI Composites.
   - **Contains:**
     - `ReMindTheme` (`Theme.kt`): Directly owns the app's `MaterialTheme` (or `MaterialExpressiveTheme`), window edge-to-edge insets, and `CompositionLocalProvider` setup using raw tokens from the shared kit.
     - ReMind-specific reusable composite components shared across 2+ features (`ReMindBottomBar`, `SnoozeBottomSheet`, `AutoSilenceBottomSheet`, `MissionSelectionBottomSheet`, `PermissionWarningBanner`, `ReMindGroupCard`, `ReMindIcons`).
   - **Strict Rule:** Never create 1:1 pass-through wrappers around single Material 3 components. `LiteverLegacyCompat.kt` is `@Deprecated` and frozen for backward compatibility only.

3. **Feature UI Layer (`:features:*`):**
   - **Role:** Screen layouts & user flows built directly with native `androidx.compose.material3.*` and styled with tokens.

---

## 2. Component & Token Usage Rules in `:features:*`

- **Use Native Material 3 Directly:**
  - Call `androidx.compose.material3.*` components (`Scaffold`, `TopAppBar`, `Button`, `OutlinedButton`, `TextButton`, `IconButton`, `Card`, `OutlinedCard`, `Switch`, `ModalBottomSheet`, `AlertDialog`, `DatePickerDialog`, `TimePickerDialog`, `OutlinedTextField`, `FilterChip`, `SegmentedButton`, etc.) directly inside `:features:*`.
  - When using `Button` or `OutlinedTextField`, you may optionally pass `LiteVerButtonDefaults` / `LiteVerTextFieldDefaults` or token shapes (`LiteverTheme.shapes.medium`) for brand consistency.
- **Gradual Legacy Migration (Strangler Fig Pattern):**
  - Do **NOT** use `@Deprecated` functions from `LiteverLegacyCompat.kt` (`LiteverScaffold`, `LiteverCard`, `LiteverSwitch`, `LiteverDialog`, etc.) in new code.
  - Whenever modifying an existing screen, replace touched `Litever*` compat calls with native `androidx.compose.material3.*` equivalents.
- **Strict Token-Driven Styling (No Hardcoded Visuals):**
  - **Colors:** Always use `LiteverTheme.colors.*` or `MaterialTheme.colorScheme.*` (including semantic extensions `.warning`, `.success`). Never hardcode HEX colors (`Color(0xFF...)`) in `:features:*`.
  - **Spacing:** Always use `LiteverTheme.spacing.*` (`tiny` 2.dp, `extraSmall` 4.dp, `small` 8.dp, `smallMedium` 12.dp, `medium` 16.dp, `mediumLarge` 20.dp, `large` 24.dp, `extraLarge` 32.dp, `doubleLarge` 48.dp, `tripleLarge` 64.dp) for layout padding and gaps.
  - **Typography & Shapes:** Always use `LiteverTheme.typography.*` / `MaterialTheme.typography.*` and `LiteverTheme.shapes.*` / `MaterialTheme.shapes.*`.

---

## 3. Mandatory Previews & Visual Verification

- Every new or modified Screen/Component MUST include `@Preview` functions covering both **Light** and **Dark** modes (and key `UiState` variants: Loading, Success, Empty/Error).
- Wrap previews in `ReMindTheme`.
- Activate the **`compose-preview`** tool after UI changes to render and visually inspect `@Preview` functions directly.
