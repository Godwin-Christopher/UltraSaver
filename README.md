# Ultra Saver — Nothing Phone (1) Edition

An aggressive, OLED-optimized Ultra Battery Saver designed specifically for the **Nothing Phone (1)**.

When engaged, it transforms your phone into an emergency survival device:
- **0% Background Leakage**: Force-stops and **suspends (`pm suspend`)** all installed apps except Phone and your 3–5 chosen essentials. Suspended apps cannot fire alarms, push notifications, or run background jobs.
- **OLED Pitch Black (`#000000`)**: Completely shuts off pixels on the Phone (1) OLED panel, reducing display draw by up to 70%.
- **Hardware Lockdown**: Caps screen refresh rate from 120Hz down to **60Hz**, clamps screen timeout to **15 seconds**, and triggers deep Android Doze mode.
- **Non-Root Elevated Privileges**: Uses **Shizuku** (Wireless Debugging / ADB) to perform privileged system commands safely without voiding warranty or unlocking bootloader.
- **Nothing OS Aesthetic**: Minimalist dot-matrix clock, monochrome app tiles, and high-contrast telemetry.

---

## Project Structure

```
BatterySaver/
├── app/
│   ├── build.gradle.kts           # AGP 8.3 + Compose BOM + Shizuku API
│   └── src/main/
│       ├── AndroidManifest.xml    # Launcher & Home intent filters, camera flashlight
│       ├── java/com/nothing/ultrasaver/
│       │   ├── UltraSaverApplication.kt
│       │   ├── MainActivity.kt    # Mode router (Setup vs Ultra Launcher)
│       │   ├── core/
│       │   │   ├── ShizukuManager.kt          # Shizuku binder handshake & shell runner
│       │   │   ├── AppFreezeController.kt     # Package scanner, whitelist, pm suspend/unsuspend
│       │   │   └── HardwarePowerController.kt # 60Hz lock, 15s timeout, system power-mode
│       │   ├── data/
│       │   │   ├── AppInfo.kt
│       │   │   ├── PowerSnapshot.kt           # Saves user settings to restore on exit
│       │   │   └── PreferencesManager.kt      # Jetpack DataStore preferences
│       │   ├── service/
│       │   │   ├── BatteryTelemetry.kt        # Battery level & survival estimation
│       │   │   └── FlashlightManager.kt       # Camera torch toggle
│       │   └── ui/
│       │       ├── theme/                     # Nothing OS OLED dark palette & typography
│       │       ├── components/                # Dot-matrix clock, battery gauge, monochrome app tile
│       │       ├── screens/
│       │       │   ├── UltraLauncherScreen.kt # The pitch-black survival home screen
│       │       │   └── SetupWhitelistScreen.kt# App checklist & Shizuku status
│       │       └── MainViewModel.kt
│       └── res/                               # Icons, strings, OLED themes
├── gradle/libs.versions.toml      # Modern Version Catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## How to Open & Build

### 1. Requirements
* **Android Studio** (Hedgehog 2023.1.1 or Ladybug / Koala / newer).
* **JDK 17** (bundled with Android Studio).

### 2. Open in Android Studio
1. Launch Android Studio.
2. Select **Open** and browse to `C:\Users\Advina\Desktop\BatterySaver`.
3. Let Gradle sync dependencies (Compose BOM, Shizuku API, Material 3).
4. Connect your **Nothing Phone (1)** via USB or Wi-Fi.
5. Click **Run (`Shift + F10`)** to install the app.

---

## How to Set Up Shizuku on Nothing Phone (1) (1-Time Setup)

You do **not** need root or a computer:
1. Install **Shizuku** from Google Play Store.
2. Go to your phone's **Settings > About phone > Software info** and tap **Build number 7 times** to unlock Developer Options.
3. Go to **Settings > System > Developer options**:
   - Turn **ON** `Wireless debugging`.
   - Tap `Wireless debugging` > **Pair device with pairing code**.
4. Open the Shizuku app:
   - Tap **Pairing**, enter the 6-digit code from notification.
   - Tap **Start** in Shizuku.
5. Open **Ultra Saver** and tap **GRANT ACCESS**.

---

## How to Use Ultra Saver
1. Check 3 to 5 apps you want to keep alive (e.g., WhatsApp, Maps, SMS). Phone and Keyboard are protected automatically.
2. Tap **ENGAGE ULTRA SAVER**.
3. Your phone locks to 60Hz, freezes all other background activity, and launches the OLED dot-matrix survival screen.
4. When you're ready, tap **EXIT ULTRA SAVER** to restore all apps and your normal 120Hz launcher.
