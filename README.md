# Little LiuYao Android

Little LiuYao is a small Android app for making a simple 小六爻-style card pull.

The app lets you:

- Type an intention or question.
- Pull one card from each of three decks: 六神, 六親, and 地支.
- Keep the pull private on the device by default.
- Reveal cards with a flip animation.
- Save pulls to local records and edit or delete saved intentions.
- Browse a basic knowledge/reference view for the cards.

The app is written in Kotlin with Jetpack Compose and builds with Gradle.

## Screenshots

<p>
  <img src="docs/screenshots/Screenshot_20260514_190742.png" alt="Ask an intention and start a private pull" width="180">
  <img src="docs/screenshots/Screenshot_20260514_190845.png" alt="Pulled cards before reveal" width="180">
  <img src="docs/screenshots/Screenshot_20260514_190855.png" alt="Reveal cards from the pull" width="180">
  <img src="docs/screenshots/Screenshot_20260514_190903.png" alt="Knowledge view for LiuYao cards" width="180">
  <img src="docs/screenshots/Screenshot_20260514_190911.png" alt="Saved pull records" width="180">
</p>

## Notes

- Minimum Android version: Android 8.0, API 26.
- Target SDK: 36.
- Records are saved locally with Android `SharedPreferences`.
- The app has internet permission only for the optional connected pull feature.
