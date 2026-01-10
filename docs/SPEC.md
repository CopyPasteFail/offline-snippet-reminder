# Project Spec: Offline Reminder App for Text Snippets

## Overview
A **personal offline Android app**, designed for sideloading (no Play Store dependency), that periodically reminds the user of custom text snippets through Android notifications. Each list of snippets has its own frequency schedule, and snippets rotate cyclically.

---

## Core Features

### 1. Snippet Lists
- User can create **multiple lists** (e.g., “Motivation”, “Mindfulness”, “Learning”).
- Each list contains **text snippets** (plain text).
- Each list defines its **notification frequency** using a numeric value plus a time unit
  (minutes, hours, days). The minimum allowed interval is 15 minutes.
- Each snippet list cycles through its items sequentially — when it reaches the end, it wraps back to the first snippet.

### 2. Notifications
- The app sends **system notifications** (using `NotificationManager`).
- Each notification displays:
  - Title: list name
  - Body: current snippet text
- Tapping the notification opens the app to the relevant list.
- Notifications continue to appear even if the app is not open (background Job/WorkManager).
- No internet access or online dependencies.
- The user can **dismiss** notifications normally — swiping them away or clearing them does **not** affect snippet progression.
- The app automatically advances to the next snippet at the next scheduled interval (no user interaction required).

### 3. App Behavior
- Fully **offline**. All data is stored locally (e.g., in `Room` or `SharedPreferences`).
- No permissions beyond `POST_NOTIFICATIONS` (Android 13+) and background scheduling.
- Lightweight, persistent between device reboots.
- No login, no analytics, no ads.

---

## Technical Architecture

### 1. Tech Stack
- **Language:** Kotlin  
- **UI:** Jetpack Compose (preferred) or XML if simpler  
- **Persistence:** Room (SQLite ORM)  
- **Scheduling:** WorkManager (for repeating background jobs)  
- **Dependency Injection:** Optional (Hilt, if needed)

---

## 2. Data Model

Define two tables: `SnippetList` and `Snippet`.

`SnippetList` fields:
- `id` (Long, primary key)
- `name` (String)
- `frequencyMinutes` (Int)
- `enabled` (Boolean)
- `currentIndex` (Int)
- `lastNotifiedAt` (Long, epoch millis)

`Snippet` fields:
- `id` (Long, primary key)
- `listId` (Long, foreign key to `SnippetList.id`)
- `text` (String)
- `position` (Int)

You can also add a `currentIndex` field in `SnippetList` to track which snippet was last shown.

---

## 3. Scheduling Logic

### WorkManager Job
Use a **PeriodicWorkRequestBuilder** for each list. Frequencies are stored in seconds.

### ReminderWorker
- Fetch the list by `listId`
- Retrieve current snippet index
- Display notification with that snippet
- Increment index (cyclically) and save

---

## 4. Notification Builder

Create a dedicated notification channel for reminders, with low or default importance to avoid noisy alerts.
Each notification should:
- Use a stable `notificationId` per list (e.g., `listId.toInt()`) so new reminders replace the previous one.
- Set the title to the list name and the content text to the current snippet.
- Use a `PendingIntent` that opens the app to the list detail screen.
- Respect Android 13+ runtime notification permission handling.

Optional:
- Add a small icon indicating reminders.
- Set `setAutoCancel(true)` so tapping clears it.

---

## 5. UI Structure

### Main Screen
- List of snippet lists (RecyclerView / Compose list)
- FAB to create new list

### List Detail Screen
- Shows snippets in that list
- Button to add new snippet
- Dropdown to set frequency
- Button to enable/disable reminders

---

## 6. Offline Persistence
- Store all snippet data locally via Room.
- Optionally export/import JSON backup manually (no sync).

Example JSON:
```json
{
  "name": "Mindfulness",
  "frequencyMinutes": 60,
  "snippets": [
    "Breathe before you react",
    "Notice your posture",
    "Pause between tasks"
  ]
}
```

---

## 7. Permissions and Battery
- Require `POST_NOTIFICATIONS` (Android 13+)
- Use `WorkManager` instead of alarms for battery optimization.
- Ensure background tasks run reliably with `setRequiresBatteryNotLow(false)`.

---

## 8. Future Enhancements (optional)
- Add “Next snippet” action/button in notifications
- Add a homescreen widget with the current snippet
- Add JSON export/import for backups
- Add dark mode and custom font settings

---

## Expected Behavior
Once running, each active list will trigger a notification at the defined interval showing the next snippet from that list, cycling indefinitely. Everything works offline, and state persists through reboots.

---

**Author Notes:**
This app intentionally avoids network permissions. It’s a private offline reminder system. The focus is on reliability, minimal footprint, and autonomy.
