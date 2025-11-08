# Commute Tracker

This is an Android application for tracking commutes. It records location data, speed, bearing, and activity type.

## Project Structure

The project is organized into the following directories:

*   `app`: The main application module.
    *   `src/main/java/org/ayal/commute_tracker`: The application's source code.
        *   `data`: Contains the data models, Room database, and repository.
            *   `CommuteData.kt`: Defines the data classes for `TrackingSession` and `TrackPoint`.
            *   `SessionGroup.kt`: Defines the data class for grouping sessions.
            *   `CommuteDatabase.kt`: The Room database implementation.
            *   `LocationRepository.kt`: The repository for accessing location and session data.
        *   `receiver`: Contains the broadcast receivers for activity recognition and tracking control.
            *   `ActivityRecognitionReceiver.kt`: Receives activity recognition updates.
            *   `TrackingControlReceiver.kt`: Controls the tracking service.
        *   `service`: Contains the tracking service.
            *   `TrackingService.kt`: The service that tracks the user's location and activity.
        *   `MainActivity.kt`: The main activity of the application.
        *   `SessionDetailActivity.kt`: The activity that displays the details of a tracking session.
        *   `SessionDetailViewModel.kt`: The ViewModel for `SessionDetailActivity`.
        *   `SessionDetailViewModelFactory.kt`: The factory for creating `SessionDetailViewModel`.
        *   `SessionHistoryActivity.kt`: The activity that displays the user's tracking history.
        *   `SessionHistoryAdapter.kt`: The adapter for the RecyclerView in `SessionHistoryActivity`.
        *   `SessionHistoryViewModel.kt`: The ViewModel for `SessionHistoryActivity`.
        *   `SessionHistoryViewModelFactory.kt`: The factory for creating `SessionHistoryViewModel`.
    *   `src/main/res`: The application's resources.
    *   `build.gradle.kts`: The build script for the application module.
*   `build.gradle.kts`: The top-level build script for the project.
*   `settings.gradle.kts`: The settings script for the project.
