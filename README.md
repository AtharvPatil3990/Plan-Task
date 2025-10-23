# 🗓️ PlanTask – Smart To-Do List App

**PlanTask** is a modern, minimal, and productivity-focused To-Do List Android app built with **Java** and **Android Studio**.  
It helps users plan their daily tasks, track progress, set reminders, and view task history — all in a beautiful, intuitive interface.


## 📘 Table of Contents
- [Features](#features)
- [UI & UX Design](#ui--ux-design)
- [Tech Stack](#tech-stack)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [How to Run the Project](#how-to-run-the-project)
- [Future Improvements](#future-improvements)
- [Author](#author)


## ✨ Features
- 🏠 **Home Screen** — Add, update, delete, and mark tasks as completed.
- 📊 **Daily Progress Bar** — See “X/Y Tasks Completed” and a smooth progress indicator.
- 📅 **History View** — View completed/pending tasks by date or month.
- 🌓 **Dark Mode & Customization** — User-friendly settings for personalization.
- ⏰ **Reminders (Upcoming)** — Set reminders to stay on track.
- 📈 **Reports (Planned)** — View daily and weekly completion summaries.
- 🔔 **Smart Notifications (Planned)** — Get notified for due tasks.


## 🎨 UI & UX Design
- Built with **Material Design 3** principles.
- Clean, minimal layout with `ConstraintLayout` and modern components.
- **Background:** `#F4F6F8` (Soft Light Gray)  
- **Card Color:** with white text contrast.
- Consistent use of `CardView`, `Chip`, and `ProgressIndicator`.
- Smooth user navigation via **BottomNavigationView** and **Navigation Component**.


## 🧱 Tech Stack
- **Language:** Java  
- **IDE:** Android Studio  
- **Architecture:** Single Activity + Multiple Fragments using `NavHostFragment`  
- **Database:** SQLite (via `SQLiteOpenHelper`)  
- **UI Components:** RecyclerView, CardView, Material Components  
- **Navigation:** Android Navigation Component  


## 🗄️ Database Schema

**Table: `table_task`**

| Column          | Type                              | Description                 |
|-----------------|-----------------------------------|-----------------------------|
| `task_id`       | INTEGER PRIMARY KEY AUTOINCREMENT | Unique task ID              |
| `title`         | TEXT                              | Task title                  |
| `description`   | TEXT                              | Optional task details       |
| `status`        | TEXT                              | `to_do` or `completed`      |
| `creation_date` | INTEGER                           | Task creation timestamp     |
| `task_priority` | TEXT                              | Low / Medium / High         |
| `reminder_time` | INTEGER                           | Reminder timestamp (if any) |
Note: Due Date is the same day that reminder_time is set for if reminder_time is not set then due_date for task is the same day as the task was created (i.e. task's creation_time)


## 🧩 Project Structure

com.android.plantask/
├── MainActivity.java
|
├── database/
|   └── TaskDatabaseHelper.java
|
├── fragments/
│   ├── HomeFragment.java
│   ├── AddTaskFragment.java
│   ├── HistoryFragment.java
│   └── SettingsFragment.java
|
├── adapters/
│   └── TaskAdapter.java
|
├── models/
│   └── TaskModel.java
|
├── utils/
|
└── DateUtils.java

## How to Run the Project

1. Download the .apk uploaded with the project's code
2. Install the file on your physical Android device
3. Look for updates in future

OR

1. Clone this repository:
   ```bash
   git clone https://github.com/AtharvPatil3990/Plan-Task.git
2. Open the project in Android Studio.
3. Let Gradle build finish.
4. Run the app on an emulator or physical Android device.


## 🚀 Future Improvements
- 🔔 Add alarm/notification system for reminders.
- 📆 Calendar view for quick task overview.
- ☁️ Backup and sync with Firebase.
- 🧠 Smart suggestions for recurring tasks.
- 💬 Add motivational quotes on Home screen.

  
## 👨‍💻 Author
Developed by **Atharv Patil**
📧 Email: atharvapatil3990@gmail.com  
🌐 GitHub: [github.com/AtharvPatil3990](https://github.com/AtharvPatil3990)
