# Speed Snapshot — Group 4

Android app for continuously tracking location speed and accuracy using the Fused Location Provider.

## Member 1 — Project Setup & Permissions

**Branch:** `setup-permissions`

This branch sets up the Android project foundation and location permission handling:

- Created Kotlin Android project with View Binding enabled
- Added Google Play services location dependency (`play-services-location`)
- Declared `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` in `AndroidManifest.xml`
- Implemented runtime permission request on launch using `ActivityResultContracts.RequestMultiplePermissions`
- Handles granted and denied states with user-facing messages (no crash on denial)
- Exposed `isLocationPermissionGranted()` for later members to gate Start/location updates

### Permission flow (for demo prep)

1. App launches → `MainActivity.onCreate()` calls `checkAndRequestLocationPermission()`
2. If permission already granted → status TextView shows "Location permission granted"
3. If not granted → system permission dialog appears for fine + coarse location
4. User grants → `onLocationPermissionGranted()` updates the status message
5. User denies → `onLocationPermissionDenied()` shows a clear message; app stays usable

### Next steps (other members)

| Member | Branch | Task |
|--------|--------|------|
| 2 | `ui-layout` | Start/Stop buttons, speed & accuracy TextViews |
| 3 | `location-updates` | LocationRequest + continuous callback |
| 4 | `lifecycle-testing` | Wire Start/Stop, lifecycle, testing |

## Build

Open the project in Android Studio, or from the project root:

```bash
./gradlew assembleDebug
```

## Requirements

- Android Studio (recommended) or JDK 11+
- Android SDK 36
- Emulator or device with location simulation for testing
