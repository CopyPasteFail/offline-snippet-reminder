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

## Signed Build for Release

If you want to create a signed release build, follow these steps.

### Step 1: Create the Key (The One-Time Task)

You only have to do this once for all your personal projects.

1.  Go to **Build > Generate Signed Bundle / APK...**.
2.  Select **APK**, click **Next**.
3.  Click **Create new...**.
4.  Fill it out and save the key file (e.g., `C:\Users\YourName\android_keys\personal.jks`). Remember the passwords and back up this file. This whole step takes about 60 seconds.

### Step 2: Configure Android Studio to Remember Your Key for Release Builds

This is the magic step that makes all future builds simple.

1.  In your project, go to **File > Project Structure**.
2.  Select **Build Variants** from the left menu.
3.  Go to the **Signing** tab.
4.  Click the **+** button to add a new signing configuration.
5.  Name it something like "release-signing".
6.  Fill in the details for the key you just created (the file path and the passwords).
7.  Now, go back to the **Build Types** tab.
8.  Select the **release** build type on the left.
9.  In the properties on the right, find **Signing Config** and choose your new "release-signing" configuration from the dropdown.
10. Click **OK**.

### Step 3: The New "Simple Way" to Install a Secure Build

Now that the one-time setup is complete, installing a secure, production-quality build on your phone is incredibly easy.

1.  In Android Studio, open the **Build Variants** tool window (usually on the left side).
2.  In the **Active Build Variant** column for your app module, change the selection from `debug` to `release`.
3.  Now, just click the regular green **Run** button!

Android Studio will now automatically:
*   Build the release version of your app.
*   Apply all the release rules (like stripping out the logs with the ProGuard rule you added).
*   Sign it with your secure release key (using the configuration you just saved).
*   Install it directly on your connected phone.

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
