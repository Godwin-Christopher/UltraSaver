# Ultra Saver (Nothing Phone 1 Edition)

An ultra power saving utility and launcher built for the Nothing Phone (1).

## Overview

When standard battery saver is not enough, Ultra Saver locks down the device to preserve critical battery life for emergencies:

- **OS-Level App Suspension**: Uses Shizuku to execute `pm suspend` on background applications, preventing wakelocks, push pings, and background sync.
- **OLED Black Interface**: Full `#000000` AMOLED theme that turns off unlit display pixels.
- **60Hz Lock**: Limits display refresh rate to 60Hz from 120Hz to reduce display controller power draw.
- **Home Screen Widget**: A minimalist Nothing-themed widget with battery telemetry and a direct toggle button.
- **Whitelist Protection**: Keep essential apps (Phone, SMS, Maps, etc.) alive while freezing non-essentials.

<img width="720" height="1600" alt="WhatsApp Image 2026-11-23 at 8 15 27 PM" src="https://github.com/user-attachments/assets/ef8d12f4-c604-43fd-ae7c-2fa451fcf387" />
<img width="720" height="1600" alt="WhatsApp Image 2026-11-23 at 8 15 26 PM" src="https://github.com/user-attachments/assets/56bf0b0e-fc6e-4122-b04f-f7734e3bafd9" />


---

## Setup Guide

Ultra Saver uses Shizuku for privileged operations without requiring root access:

1. Install **Shizuku** from the Google Play Store.
2. Enable **Developer Options** and turn on **Wireless Debugging** in device settings.
3. Pair Shizuku using the on-screen pairing code and tap **Start**.
4. Open **Ultra Saver** and grant Shizuku permission.
5. Select your allowed apps and engage the saver mode.

---

## Building from Source

This project is built with Kotlin and Jetpack Compose.

```bash
# Clone the repository
git clone https://github.com/Godwin-Christopher/UltraSaver.git
cd UltraSaver

# Build the debug APK
./gradlew assembleDebug
```

The output APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## License

This project is open-source under the MIT License.
