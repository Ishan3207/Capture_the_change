# Capture The Change

Capture The Change is an Android camera application that detects motion and differences in the scene, highlighting what has changed in real-time. It features multiple output modes (Color and Grayscale) and a Pro mode for advanced camera controls.

## Disclaimer

**IMPORTANT: This project is in a really early stage of development.** Features, APIs, and functionality are subject to rapid change. Bugs and unexpected behavior may occur. Use at your own risk and feel free to contribute!

## Features

* Real-time delta motion detection preview
* Multiple visual modes:
  - Color Motion
  - Grayscale Motion
  - Color Static
  - Grayscale Static
* Pro Mode:
  - Adjustable motion sensitivity threshold
  - Configurable timeframe buffer for delta comparisons
  - Auto-Exposure (AE) and Auto-White Balance (AWB) locking
  - Resolution selector (480p, 720p, 1080p)
* Background processing with CameraX
* Save captures directly to the device gallery

## Getting Started

You have two choices to try out Capture The Change: downloading a pre-built APK or building it from the source code.

### Option 1: Download the APK

If you just want to install and use the app without compiling the code:
1. Go to the Releases page of this GitHub repository.
2. Download the latest APK file to your Android device.
3. Open the downloaded file to install it. (You may need to enable "Install from unknown sources" in your Android settings).

### Option 2: Build from Source

If you want to view the code, modify it, or build it yourself:

**Prerequisites:**
* Android Studio (latest version recommended)
* JDK 17
* Android device running Android 5.0 (API level 21) or higher

**Build Instructions:**
1. Clone this repository to your local machine using git.
2. Open Android Studio.
3. Select **Open an existing Android Studio project** and navigate to the cloned directory.
4. Allow Gradle to sync the project dependencies.
5. Connect your Android device.
6. Click the **Run** button in Android Studio, or run the following command in the terminal to build and install the debug version:

```bash
./gradlew installDebug
```

## Contributing

As this is an early-stage project, contributions, issue reports, and pull requests are highly welcome. Please open an issue to discuss proposed changes before submitting large pull requests.
