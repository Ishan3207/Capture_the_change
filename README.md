# Capture The Change

Capture The Change is an advanced Android camera application designed to detect motion and visual differences in a scene in real-time. By comparing sequential frames, it highlights exactly what has changed, making it perfect for tracking micro-movements, observing changes over time, and isolating motion. 

It features a custom Delta Engine, multiple output modes (Color/Grayscale), and a sleek Pro Mode giving you hardware-level control over the camera sensor.

## 🛠️ Tech Stack

- **Kotlin**: Primary programming language.
- **Jetpack Compose**: Used exclusively for building the modern, glassmorphic UI.
- **CameraX**: Powers the core camera functionality, lifecycle management, and frame extraction.
- **Camera2Interop**: Used to bypass CameraX limitations and tap directly into lower-level hardware APIs (OIS/EIS, AF/AE/AWB locks).
- **Coroutines & Flow**: Used for state management, asynchronous frame processing, and reactive UI updates.

##  Features

* **Real-Time Delta Engine**: Custom algorithm processing live YUV frames to isolate and highlight motion on the fly.
* **Pro Camera UI**: A minimalist, glassmorphic interface that exposes only the controls that matter for motion detection.
* **Hardware-Level Sensor Control**:
  - **AF Lock (Infinity Override)**: Disabling Auto-Focus locks the lens focus distance to infinity (`0.0f`) to prevent focus breathing from triggering false motion.
  - **AE & AWB Locks**: Lock Auto-Exposure and Auto-White Balance to ensure consistent frame comparisons.
  - **Hardware Stabilization**: Seamlessly toggle between Optical Image Stabilization (OIS), Electronic Image Stabilization (EIS), or both, natively communicating with the camera sensor.
* **Multiple Output Modes**:
  - Color Motion
  - Grayscale Motion
  - Color Static
  - Grayscale Static
* **Customizable Tuning**: Adjust the engine's noise sensitivity threshold (5–255) and timeframe buffer (100–5000ms) in real-time via sleek UI sliders.

##  How It Was Built

The app was built by combining **CameraX's `ImageAnalysis`** with a custom `MotionAnalyzer`. CameraX efficiently streams raw YUV frames into the analyzer, which calculates the delta (difference) between the current frame and a buffered history frame. 

Because motion detection requires the camera sensor to be perfectly still and consistent, standard CameraX use cases weren't enough. We implemented **`Camera2Interop`** to bridge down to the native Android Camera2 API. This allowed us to forcefully lock Auto-Focus (AF), Auto-Exposure (AE), and Auto-White Balance (AWB), and manually control hardware Optical/Electronic stabilization (OIS/EIS). 

Finally, the UI was completely overhauled using **Jetpack Compose**, implementing a clean, responsive, and data-driven architecture using Kotlin Flows to immediately reflect hardware changes on the screen.

##  Disclaimer

**IMPORTANT: This project is in a really early stage of development.** Features, APIs, and functionality are subject to rapid change. Bugs and unexpected behavior may occur. Use at your own risk and feel free to contribute!

##  Getting Started

### Build from Source

**Prerequisites:**
* Android Studio (latest version recommended)
* JDK 17
* Android device running Android 5.0 (API level 21) or higher

**Build Instructions:**
1. Clone this repository to your local machine.
2. Open Android Studio and select **Open an existing Android Studio project**.
3. Allow Gradle to sync the project dependencies.
4. Connect your Android device.
5. Click the **Run** button, or run the following command in the terminal to build and install the debug version:

```bash
./gradlew installDebug
```

##  Contributing

As this is an early-stage project, contributions, issue reports, and pull requests are highly welcome. Please open an issue to discuss proposed changes before submitting large pull requests.
