# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-04-29

### Added
- **Litever Design System:** Extracted a standalone library module `:litever-designsystem` for multi-app reusability.
- **Enhanced Theming:** Expanded custom theme to support all 36 Material 3 color tokens, including `surfaceContainer` variants.
- **Developer Tools:** New Agent workflows and skills for rapid project initialization and design system extraction.

### Changed
- **Architecture:** Refactored `:core:designsystem` into an adapter layer to support the new library module.
- **Components:** Migrated and renamed global UI components to use the `Litever` prefix.

## [1.0.0] - 2026-04-26


### Added
- **Core Architecture:** Clean Architecture with Multi-module setup (app, core, features).
- **Alarm Engine:** Precise alarm scheduling using `AlarmManager` with Battery Optimization support.
- **Missions System:** Implementation of Typing, Math, Shake, and QR/Barcode missions to ensure users wake up.
- **Weather Feature:** Integrated real-time weather forecasts and AI-powered morning hints on the home screen.
- **Location Management:** Smart location search and selection for localized weather data.
- **Reminder Logic:** Support for recurring days and specific one-time dates.
- **Design System:** Custom Material 3 theme with Dynamic Color support and sleek dark mode.
- **Greeting UI:** Dynamic greeting messages based on the time of day.
- **Settings:** Modular settings management with App settings, Support, and About categories.
- **Localization:** Full support for English and Vietnamese.
- **CI/CD & Monitoring:** Firebase Crashlytics and Analytics integration.
- **Quality Control:** R8/Proguard enabled for release builds, comprehensive Lint rules.
