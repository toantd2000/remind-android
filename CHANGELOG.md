# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.3] - 2026-05-09

### Added
- **Attributions & Credits:** A new screen to properly credit open-source libraries and illustrations (Storyset).
- **Personal Thanks:** Included a heartfelt thank you message to the resources and community.

### Changed
- **Settings Reorganization:** Moved "Open Source Licenses" into the new Attributions screen for a cleaner Settings layout.

## [1.1.2] - 2026-05-08

### Added
- **Custom Ringtone Selection:** Users can now pick any audio file from their device to use as an alarm sound.
- **Permission Optimization:** Implemented using Storage Access Framework (SAF), eliminating the need for `READ_EXTERNAL_STORAGE` permission while maintaining persistent access across reboots.

### Fixed
- **Alarm Skip Logic:** Fixed a bug where skipped alarms were incorrectly marked as "Missed due to power off" if the alarm time passed while the app was closed.
- **Database Maintenance:** Implemented automatic cleanup of expired "Skip once" statuses. Alarms will now correctly reset their skipped state once the skipped occurrence has passed, ensuring both DB and UI stay consistent.

## [1.1.1] - 2026-05-05
