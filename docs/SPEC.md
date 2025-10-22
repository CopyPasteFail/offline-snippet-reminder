# Project Spec: Offline Reminder App for Text Snippets

## Overview
A **personal offline Android app**, designed for sideloading (no Play Store dependency), that periodically reminds the user of custom text snippets through Android notifications. Each list of snippets has its own frequency schedule, and snippets rotate cyclically.

---

## Core Features

### 1. Snippet Lists
- User can create **multiple lists** (e.g., “Motivation”, “Mindfulness”, “Learning”).
- Each list contains **text snippets** (plain text).
- Each list defines its **notification frequency**:
  - Every 30 minutes  
  - Every 1 hour  
  - Every 3 hours  
  - Every 6 hours  
  - Every 12 hours  
  - Every 24 hours (daily)  
  - Every week (7 days)
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
- **Min SDK:** 26 (Android 8.0, for WorkManager support)  
- **UI:** Jetpack Compose (preferred) or XML if simpler  
- **Persistence:** Room (SQLite ORM)  
- **Scheduling:** WorkManager (for repeating background jobs)  
- **Dependency Injection:** Optional (Hilt, if needed)

---

## 2. Data Model

```kotlin
@Entity(tableName = "snippet_lists")
data class SnippetList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val frequencyMinutes: Long // e.g., 30, 60, 1440, etc.
)

@Entity(tableName = "snippets")
data class Snippet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int,
    val text: String,
    val orderIndex: Int
)
```

You can also add a `currentIndex` field in `SnippetList` to track which snippet was last shown.

---

## 3. Scheduling Logic

### WorkManager Job
Use a **PeriodicWorkRequestBuilder** for each list:

```kotlin
val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
    repeatInterval = list.frequencyMinutes,
    repeatIntervalTimeUnit = TimeUnit.MINUTES
).setInputData(workDataOf("listId" to list.id))
 .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "reminder_${list.id}",
    ExistingPeriodicWorkPolicy.UPDATE,
    workRequest
)
```

### ReminderWorker
- Fetch the list by `listId`
- Retrieve current snippet index
- Display notification with that snippet
- Increment index (cyclically) and save

---

## 4. Notification Builder

```kotlin
fun showNotification(context: Context, listName: String, snippet: String) {
    val channelId = "snippet_channel"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Snippets", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_reminder)
        .setContentTitle(listName)
        .setContentText(snippet)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    manager.notify(Random.nextInt(), notification)
}
```

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

## 8. Future Extensions (optional)
- Allow manual trigger “Show next snippet now”
- Add widget with current snippet
- Add backup/export to file
- Dark mode and local font settings

---

## Development Setup

### Prerequisites
- Android Studio Iguana or newer
- Kotlin plugin enabled
- SDK 26+
- Gradle plugin 8+

### Run
1. Clone repository or create new Android project.
2. Add dependencies in `build.gradle`:
   ```gradle
   implementation "androidx.work:work-runtime-ktx:2.9.0"
   implementation "androidx.room:room-ktx:2.6.1"
   implementation "androidx.room:room-runtime:2.6.1"
   kapt "androidx.room:room-compiler:2.6.1"
   implementation "androidx.core:core-ktx:1.13.1"
   implementation "androidx.compose.ui:ui:1.7.0"
   implementation "androidx.compose.material3:material3:1.3.0"
   implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.8.4"
   ```
3. Build → Run on device/emulator.
4. Sideload resulting `.apk`.

---

## Expected Behavior
Once running, each active list will trigger a notification at the defined interval showing the next snippet from that list, cycling indefinitely. Everything works offline, and state persists through reboots.

---

**Author Notes:**
This app intentionally avoids network permissions. It’s a private offline reminder system. The focus is on reliability, minimal footprint, and autonomy.
