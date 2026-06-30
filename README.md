# IronLog

IronLog is a clean, modern, and powerful workout tracker built with Jetpack Compose. It's designed for lifters who want to track their progress without the clutter, focusing on data-driven trends and a seamless logging experience.

## Features

### 🏋️‍♂️ Smart Workout Logging
*   **Intuitive Interface:** Log your weights, sets, and notes with ease.
*   **Real-time Timer:** Track your workout duration with a built-in timer (pause/resume supported).
*   **Progressive Overload:** See your previous lift data and Personal Records (PRs) directly on the exercise card.
*   **Flexible Control:** Skip exercises you can't do today, reorder your routine on the fly, or add new movements mid-workout.

### 📈 Detailed Progress Analytics
*   **Visual Trends:** Interactive line charts for weight progression and volume over time.
*   **Consistency Tracking:** Monitor your workout frequency with weekly and monthly bar charts.
*   **Performance Metrics:** Track your streaks (current/best), total volume, average duration, and total workouts.
*   **PR Highlights:** Automatically identifies and showcases your top lifts and new personal records.

### 📋 Workout Management
*   **Custom Plans:** Create multiple workout splits (Push/Pull/Legs, Upper/Lower, etc.).
*   **History:** A comprehensive log of all your past sessions with exercise-level details.
*   **Exercise Library:** Easily add from common favorites or create your own custom movements.

## Tech Stack

*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
*   **Architecture:** MVVM (Model-View-ViewModel) with `StateFlow` and `ViewModel`.
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) for local persistence.
*   **Asynchronous:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html).
*   **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation).
*   **Design:** Material 3 with a custom aesthetic focusing on readability and "Soft Card" components.

## Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 17+
*   Android SDK 34+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/IronLog.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
