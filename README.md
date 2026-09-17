# Step Counter Android App

An Android application built in Kotlin that tracks daily step counts using the device's physical step counter sensor, estimates distance walked and calories burned, allows users to set custom step goals, and saves step history for the past 7 days.

## 👤 Developer Info
- **Developer Name**: Tilak Pandya
- **Enrollment Number**: 25012012037

---

## 📱 Features

- **Real-Time Step Tracking**: Reads step counts directly from the device hardware sensor (`Sensor.TYPE_STEP_COUNTER`).
- **Distance & Calorie Estimation**:
  - **Distance**: Calculated based on average stride length (`0.75 meters/step`).
  - **Calories**: Estimated at `0.04 kcal/step`.
- **Daily Goal Setting & Progress Bar**:
  - Default daily goal of 5,000 steps (customizable).
  - Live progress percentage and horizontal progress bar updates.
- **7-Day Step History**: Automatically records daily steps using `SharedPreferences` and displays history for the last 7 days.
- **Automatic & Manual Reset**:
  - Automatically resets step count when a new calendar day begins.
  - Manual reset button to restart today's step count.
- **Runtime Permissions**: Requests `ACTIVITY_RECOGNITION` permission on Android 10+ (API level 29+) required for physical activity tracking.

---

## 🛠️ Project Structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/mad_todo_list_assignment/
│   ├── MainActivity.kt        # Main screen logic, step sensor listener, goal & reset handling
│   └── HistoryActivity.kt     # Step history screen displaying the past 7 days
└── res/
    ├── layout/
    │   ├── activity_main.xml    # Scrollable layout for step counter, progress, goal input & actions
    │   └── activity_history.xml # Layout for displaying the 7-day step history list
    └── values/
        ├── colors.xml         # Color palette (Primary Green, Dark Green, Orange Goal, etc.)
        ├── strings.xml        # String resources
        └── themes.xml         # Material3 theme definitions
```

---

## ⚙️ Requirements & Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)
- **UI Framework**: Android XML Views / AppCompat / Material Components
- **Hardware Requirement**: Physical Android device with a built-in step counter sensor (`Sensor.TYPE_STEP_COUNTER`)

---

## 🚀 Getting Started

1. **Clone or Open the Project**:
   Open the project folder in **Android Studio**.

2. **Sync Gradle**:
   Allow Android Studio to download dependencies and sync Gradle files.

3. **Run on Device**:
   - Deploy the application to a physical Android device or an emulator that supports hardware sensor simulation.
   - *Note*: Step counting requires physical movement or sensor simulation in the emulator's extended controls.

4. **Permissions**:
   Upon launching the app, grant the **Physical Activity** permission when prompted.

---

## 📄 License

This project is created for educational and assignment purposes.
