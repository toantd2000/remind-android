# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.2] - 2026-05-07

### Fixed
- **Alarm Skip Logic:** Fixed a bug where skipped alarms were incorrectly marked as "Missed due to power off" if the alarm time passed while the app was closed.
- **Database Maintenance:** Implemented automatic cleanup of expired "Skip once" statuses. Alarms will now correctly reset their skipped state once the skipped occurrence has passed, ensuring both DB and UI stay consistent.

## [1.1.1] - 2026-05-05

### Fixed
- **Alarm Scheduling:** Resolved a critical bug where repeating alarms (e.g., Mon, Tue) failed to schedule the next occurrence if the current one was snoozed or dismissed. This was caused by `PendingIntent` collisions in `AlarmManager`.
- **System Reliability:** Improved `AlarmSyncManager` to correctly restore both main alarms and active snoozes after a device reboot.

## [1.1.0] - 2026-05-03

### Added
- **AdMob Integration:** Integrated Google Mobile Ads SDK (AdMob) with pre-configured App ID and Native Ad caching for optimization.
- **Edge-to-edge Support:** Implemented full edge-to-edge display support and Android 15 compliance for a modern look.
- **Litever Design System:** Extracted a standalone library module `:litever-designsystem` for multi-app reusability and enhanced theming (36 M3 tokens).
- **Missed Alarm Logic:** Implemented `AlarmSyncManager` to handle missed alarms after reboot and improve notification reliability.
- **UI Polish:** Applied italic style and bookmark effects to quotes and weather hints; updated icons to Rounded variants.

### Fixed
- **Audio Handling:** Resolved bugs where alarms would ring during active phone calls or fail to respect Audio Focus.
- **Stability:** Fixed a critical crash when adding phrases to new alarms and resolved UI button alignment issues.

### Changed
- **Architecture:** Centralized audio and vibration logic into `AudioPlayer` and refactored theme system into an adapter layer.
- **UX:** Implemented sequential alarm processing and "Gentle Reminder" (gradual volume) for a better wake-up experience.

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
