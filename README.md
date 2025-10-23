# Offline Snippet Reminder

A minimal, **offline Android app** that periodically reminds you of short text snippets (quotes, affirmations, ideas, etc.) via **system notifications**.  
Each snippet list has its own frequency (every 30 min, hour, day, or week), and snippets rotate cyclically without needing any network or user interaction.

---

## ✨ Features

- Create **multiple snippet lists** (e.g., “Motivation”, “Mindfulness”, “Tech Notes”)
- Each list defines its own **notification interval**
- Notifications show the next snippet **automatically** (no tapping required)
- Works **100% offline**
- No login, analytics, or ads
- Fully local storage (Room database)
- Persistent across device reboots
- Simple Jetpack Compose UI

---

## 🧠 Concept

The app acts as a **personal recall tool** — snippets you’ve saved reappear as lightweight reminders throughout your day.  
You can dismiss notifications normally; they’ll keep cycling on their own schedule.

---

## 🧱 Architecture

| Layer | Component | Description |
|-------|------------|-------------|
| **UI** | Jetpack Compose | Modern declarative UI |
| **Storage** | Room ORM | Local SQLite persistence |
| **Scheduling** | WorkManager | Reliable background jobs |
| **Notifications** | NotificationCompat | System reminders |
| **Language** | Kotlin | Clean and idiomatic |

---

## 🗂️ Repo Structure

```
offline-snippet-reminder/
├── app/                     # Android app source (created by Codex/Android Studio)
├── docs/
│   └── SPEC.md              # Detailed design and architecture spec
├── .codex-prompt.txt        # Prompt for generating the app with VS Code Codex
├── README.md                # This file
├── build.gradle
├── settings.gradle
└── .gitignore
```

---

## ⚙️ Build & Run

### Prerequisites
- Android Studio Iguana (or newer)
- Kotlin plugin enabled
- SDK 26 or higher
- Gradle 8.x

### Steps
```bash
git clone https://github.com/CopyPasteFail/offline-snippet-reminder.git
cd offline-snippet-reminder
```

1. Open the project in Android Studio  
2. Sync Gradle and build the project  
3. Run on a connected device or emulator  
4. Once built, locate the generated `.apk` under:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📱 Sideload Installation

1. Transfer `app-debug.apk` to your Android device  
2. Enable “Install unknown apps” for your file manager or browser  
3. Tap the `.apk` and install  
4. Grant the **notification permission** when prompted  

That’s it — the reminders will begin according to your configured snippet lists.

---

## 🧩 Development with Codex

If using VS Code’s Codex AI extension, this project includes:
- `/docs/SPEC.md` — full feature and architecture definition  
- `/.codex-prompt.txt` — instructions for generating the Android codebase  

You can regenerate or extend the app at any point by re-running Codex with the included prompt.

---

## 🚀 Future Enhancements
- Manual “Next snippet” button in notifications  
- Homescreen widget  
- JSON export/import for backups  
- Dark mode and custom fonts  

---

## 📄 License

MIT License © 2025 [Omer Reznik](https://github.com/CopyPasteFail)
