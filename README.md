# Mapbox Android Sample

[![Build](https://github.com/Vishnu39/MapboxAndroidSample/actions/workflows/build.yml/badge.svg)](https://github.com/Vishnu39/MapboxAndroidSample/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A small Android app built for the Mapbox booth demo at DevCon 2026 (CityCube Berlin). It shows an interactive 3D map of Berlin with **Mapbox GL JS** running inside a **WebView**, wrapped in a **Jetpack Compose** UI.

## What it does

- **3D map of Berlin**, centred on CityCube Berlin (booth #8), using the Mapbox Standard style with pitch, bearing and 3D buildings.
- **Map controls** inside the map page:
  - Toggle 3D, start or stop a 360° orbit tour, fly back to CityCube, and step through landmarks: Brandenburg Gate, TV Tower and the Reichstag.
  - Style switcher: Standard, Streets, Satellite, Dark and Outdoors.
  - Navigation and geolocation controls, plus an animated booth marker with a popup.
- **Native Compose top bar:**
  - The location and tour buttons call into the page with `evaluateJavascript` (`flyToCityCube()`, `toggleOrbit()`).
  - An info dialog shows the booth raffle details and a masked view of the configured token.
- **Token handling:** the HTML asset only contains a `__MAPBOX_ACCESS_TOKEN__` placeholder. `MainActivity` swaps in the token from `BuildConfig` when it loads the page. If no token is configured, the app shows a setup hint instead of the map.

## Tech stack

Kotlin, Jetpack Compose (Material 3), Android WebView, Mapbox GL JS v3, Android Gradle Plugin 9 and Gradle 9.1. minSdk 29, targetSdk 36.

## Setup

1. **Create a Mapbox account** at [mapbox.com](https://account.mapbox.com/auth/signup/).
2. **Create a public access token** (it starts with `pk.`) on the [Access tokens](https://account.mapbox.com/access-tokens/) page. The default public scopes are enough.
3. **Add it to `local.properties`** in the project root. This file is git-ignored; use [`local.properties.example`](local.properties.example) as a template:

   ```properties
   sdk.dir=/path/to/Android/sdk
   MAPBOX_ACCESS_TOKEN=pk.your_public_token_here
   ```

   You can also supply the token as an environment variable or a Gradle property (for example in `~/.gradle/gradle.properties`) with the same name, `MAPBOX_ACCESS_TOKEN`.

4. **Build and run:**

   ```bash
   ./gradlew assembleDebug
   ```

   Or open the project in Android Studio and run the `app` configuration.

### Optional: secret downloads token

The native Mapbox Maps SDK (`com.mapbox.maps:android`) is served from Mapbox's private Maven repository, which needs a **secret** token (`sk.`) with the `DOWNLOADS:READ` scope. This sample doesn't use the native SDK, so you don't need one. If you add the native SDK, set `MAPBOX_DOWNLOADS_TOKEN` in `local.properties` (or as an environment variable or Gradle property). `settings.gradle.kts` then adds the Mapbox Maven repository with those credentials automatically.

## Keeping tokens safe

- **Never commit tokens.** `local.properties` is git-ignored, and the token only exists in generated build files.
- **Restrict your public token by URL.** A public token ends up in the APK and in the WebView page, so anyone can extract it. In the [Mapbox dashboard](https://account.mapbox.com/access-tokens/), add **URL restrictions** to the token, and rotate it if it leaks.
- **Keep secret (`sk.`) tokens out of the app.** They belong only in `local.properties`, environment variables or CI secrets.

## How the token reaches the map

```text
local.properties ──► app/build.gradle.kts ──► BuildConfig.MAPBOX_ACCESS_TOKEN ──► MainActivity
                                          └─► R.string.mapbox_access_token (resValue)
MainActivity: reads assets/mapbox_map.html, replaces __MAPBOX_ACCESS_TOKEN__,
              loads it with loadDataWithBaseURL("file:///android_asset/", ...)
```

## Known limitations

- The app declares location permissions but doesn't request them at runtime yet. To use the geolocation control, grant location access in the system settings.
- Mapbox GL JS and map tiles load from the network, so the app needs an internet connection.

## License

Licensed under the [Apache License 2.0](LICENSE). Copyright 2026 Vishnu CR.
