# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Settings Restructure:** Reorganized the settings screen into three distinct groups: App Settings, Support & Community, and About & Legal.
- **Support Developer Refactoring:** Converted the full-width premium card into a standard settings list item with a Heart icon under the Support category. Clicking it triggers a beautiful custom modal dialog with actions to Watch Ad or Donate. Decoupled the settings item subtitle (short & concise) from the dialog content (displays the full, heartfelt thank-you message).
- **Rewarded Ad Support:** Implemented AdMob Rewarded Ads support (`SUPPORT_REWARDED`) to temporarily disable ads for 24 hours in exchange for watching an ad.
- **Ad Placement Serialization Mapping:** Tagged the `SUPPORT_REWARDED` enum value with `@SerialName("SUPPORT_REWARD")` to automatically support the new ads configuration JSON placement key without renaming or breaking any existing references.
- **Supporter Reward Tuning:** Added a dynamic build-type check that automatically limits the granted ad-free supporter duration to **30 seconds** on Debug variants instead of the standard 24 hours to support rapid, iterative developer verification.
- **Ad Simulator Fallback:** Created a Compose-based interactive countdown `RewardedAdSimulatorDialog` as a graceful fallback when real ads are not loaded.
- **FAQ Stub:** Added future plans FAQ dialog to notify users of upcoming updates.
- **Exit Dialog Upgrades:** Re-embedded Native Ads (`EXIT_NATIVE`) into `ExitAppDialog` inside the original `Box` container structure. Upgraded both the exit and support developer dialogs with highly sincere, heartfelt messages and a warm daily wish.
- **Conditional Ad-Free Exit UI:** Connected supporter reward state (`isAdFreeActive`) to the exit dialog, dynamically removing the support plea and hiding native ads entirely when the user is enjoying their ad-free status.

### Changed
- **Settings Dialog Refinements:** Removed the redundant close button from the Support Developer dialog using a custom `confirmButton = {}` layout wrapper.
- **Exit Dialog Polish:** Removed the rewarded ad button ("Watch ad to support...") from `ExitAppDialog` completely, ensuring a lightweight and non-disruptive exit confirmation flow.
- **Navigation Tweaks:** Hidden the unimplemented Alarm Settings menu item from the settings tab.

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
