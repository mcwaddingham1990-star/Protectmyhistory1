# ProtectMyHistory

Standalone Android memory-breadcrumb app for the owner's Motorola phone.

## Current beta records

- Browser URL-bar changes exposed through Android Accessibility, including best-effort Chrome Incognito and Edge InPrivate labels.
- Email addresses and phone numbers shown or entered in non-password fields, with the originating app.
- SMS history (number, direction, timestamp and body), call history, contact additions/changes, and best-effort contact activity.
- Standard on-device DNS queries through a local VPN relay, with best-effort originating-app labels. Encrypted DNS/DoH inside an app cannot be decoded.
- Local tab-separated timeline with manual export. No cloud service is used.

## Honest Android limitations

Android and browsers may hide address-bar values, contact-access events, encrypted DNS/DoH, or app login state. This app records only information Android exposes after the owner grants permissions. It deliberately excludes password fields.

## Build

Open **Actions → Build ProtectMyHistory APK → Run workflow**. After the job finishes, download the `ProtectMyHistory-debug-apk` artifact and unzip it to obtain `app-debug.apk`.
