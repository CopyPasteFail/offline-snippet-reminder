# Offline Snippet Reminder

A minimal, **offline Android app** that periodically reminds you of short text snippets (quotes, affirmations, ideas, etc.) via **system notifications**.  
Each snippet list has its own frequency (every 30 min, hour, day, or week), and snippets rotate cyclically without needing any network or user interaction.
The app acts as a **personal recall tool** — snippets you’ve saved reappear as lightweight reminders throughout your day.

---

## Architecture

| Layer | Component | Description |
|-------|------------|-------------|
| **UI** | Jetpack Compose | Modern declarative UI |
| **Storage** | Room ORM | Local SQLite persistence |
| **Scheduling** | WorkManager | Reliable background jobs |
| **Notifications** | NotificationCompat | System reminders |
| **Language** | Kotlin | Clean and idiomatic |

---

## Features

- Create **multiple snippet lists** (e.g., “Motivation”, “Mindfulness”, “Tech Notes”)
- Each list defines its own **notification interval**
- Notifications show the next snippet **automatically** (no tapping required)
- Works **100% offline**
- No login, analytics, or ads
- Fully local storage (Room database)
- Persistent across device reboots
- Simple Jetpack Compose UI

---


## Repo Structure

```
offline-snippet-reminder/
├── app/                     # Android app source (created by Codex/Android Studio)
├── docs/
│   └── SPEC.md              # Detailed design and architecture spec
├── .codex-prompt.txt        # Prompt for generating the app with VS Code Codex
├── setup-android-dev.sh     # Environment bootstrap script for Linux/WSL
├── README.md                # This file
├── build.gradle
├── settings.gradle
└── .gitignore
```

---

## Build

```bash
git clone https://github.com/CopyPasteFail/offline-snippet-reminder.git
cd offline-snippet-reminder
```

---

### Using Android Studion

#### Prerequisites
- Android Studio Iguana (or newer)
- Kotlin plugin enabled
- SDK 26 or higher
- Gradle 8.x


#### Steps
1. Open the project in Android Studio  
2. Sync Gradle and build the project  
3. Run on a connected device or emulator  
4. Once built, locate the generated `.apk` under:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Using VS Code in WSL

You can work from VS Code, but you still need the Android SDK and a JDK (the script handles that). Recommended VS Code extensions:
- Kotlin support
- Android for Visual Studio Code / ADB helpers
- Gradle Tasks
- XML / Android resource helpers
- Codex or Copilot for AI-assisted code generation

Use `.codex-prompt.txt` (root) for initial skeleton generation and `.codex-prompt-continue.txt` to continue incomplete work without overwriting finished files.

#### One-command environment setup (Linux / WSL)

I provide `setup-android-dev.sh` to bootstrap the Android SDK and required command-line tools on Debian/Ubuntu-based systems (including WSL). Place it in the repo root and run:

```bash
./setup-android-dev.sh
```

The script will:
- install OpenJDK 17 and required utilities
- download Android command-line tools and place them under `$HOME/Android/Sdk`
- install `platform-tools`, `build-tools:36.0.0` and `platforms;android-36`
- add environment exports to `~/.bashrc` (ANDROID_SDK_ROOT, PATH, JAVA_HOME)
- optionally install an emulator system image and create an AVD

Notes:
- The script requires `sudo` to install packages.
- On WSL, emulator support is limited; prefer testing on a real device via `adb`.
- After the script finishes, run `source ~/.bashrc` or open a new shell to pick up env vars.

### Once env is ready

```bash
./gradlew assembleDebug
```

## Install

### Using adb

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Grant the app the notification permission when the device asks.

---

### Sideload Installation

1. Transfer `app-debug.apk` to your Android device  
2. Enable “Install unknown apps” for your file manager or browser  
3. Tap the `.apk` and install  
4. Grant the **notification permission** when prompted  

---

## Future Enhancements
- Manual “Next snippet” button in notifications  
- Homescreen widget  
- JSON export/import for backups  
- Dark mode and custom fonts  

---

## Quick tips

- Prefer testing on a real phone (adb + USB debugging). Emulator on WSL is flaky unless you run a full Linux VM with KVM.
- Use `./gradlew installDebug` to build+install in one command (device must be connected and authorized).
- If Gradle complains about Java version, ensure `java -version` reports 17+.

---

## License

MIT License © 2025
