# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.4] - 2026-05-12

### Added
- **Exit Verification:** Added an elegant exit dialog to confirm app closure.
- **Improved Missions:** Modernized the Typing Mission UI with "Typing for Memory" insights to enhance cognitive reinforcement.
- **Draft Support:** Implemented a draft pattern for alarm creation with change detection to prevent accidental loss of settings.
- **UI Components:** Added `ReMindLoadingIconButton` for better interaction feedback during reload actions.

### Changed
- **UX Refinement:** Optimized the "Add Alarm" flow for a smoother configuration experience.
- **Performance:** modularized advertisement logic using Firebase Remote Config for more dynamic and stable control.
- **Design System:** Bumped `litever-designsystem` to `v1.0.4`.

### Fixed
- **Cloning Stability:** Fixed a bug where missions and private phrases were duplicated during alarm cloning.
- **Localization:** Improved translations and fixed activity finish logic in the exit dialog when using language overrides.



### Added
- **AI-Powered Insights:** Integrated real-time AI status tracking for weather and reminder hints.
- **Smart Refresh:** Added a smart refresh mechanism that automatically polls for updates when AI is processing.
- **Location Awareness:** Implemented automatic location detection for precise weather forecasts.
- **Attributions & Credits:** A new screen to properly credit open-source libraries and illustrations (Storyset).
- **Personal Thanks:** Included a heartfelt thank you message to the resources and community.

### Changed
- **UI Enhancements:**
    - Grouped reminders into a single elegant card for better focus.
    - Improved Next Alarm header visibility and styling.
    - Redesigned Remind screen with a cleaner top bar and better scrolling.
- **Settings Reorganization:** Moved "Open Source Licenses" into the new Attributions screen for a cleaner Settings layout.
- **Performance:** Reduced advertisement cache duration for fresher content.

## [1.1.2] - 2026-05-08

### Added
- **Custom Ringtone Selection:** Users can now pick any audio file from their device to use as an alarm sound.
- **Permission Optimization:** Implemented using Storage Access Framework (SAF), eliminating the need for `READ_EXTERNAL_STORAGE` permission while maintaining persistent access across reboots.

### Fixed
- **Alarm Skip Logic:** Fixed a bug where skipped alarms were incorrectly marked as "Missed due to power off" if the alarm time passed while the app was closed.
- **Database Maintenance:** Implemented automatic cleanup of expired "Skip once" statuses. Alarms will now correctly reset their skipped state once the skipped occurrence has passed, ensuring both DB and UI stay consistent.

## [1.1.1] - 2026-05-05
