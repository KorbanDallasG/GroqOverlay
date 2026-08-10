<p align="center">
  <img src="logo.png" width="220" alt="Groq Overlay">
</p>

<h1 align="center">Groq Overlay</h1>

<p align="center">
  <b>A floating AI window on top of any Android app — no root required</b>
</p>

<p align="center">
  <img alt="License" src="https://img.shields.io/badge/License-GPLv3-blue.svg">
  <img alt="platform" src="https://img.shields.io/badge/platform-Android%208%2B-green">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-1.9.24-purple">
  <img alt="UI" src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange">
  <img alt="API" src="https://img.shields.io/badge/Groq-streaming-yellow">
</p>

<p align="center">
  <a href="README.md">Русский</a> · English
</p>

<p align="center">
  Support the project: <a href="https://send.monobank.ua/jar/4SPUc2SjFW">send.monobank.ua</a>
</p>

---

## What is this

Groq Overlay is a floating window with an AI assistant powered by the Groq API (Llama 3.3, DeepSeek-R1, Gemma, Mixtral), available on top of any app. Ask anything without leaving your current app.

Answers stream in real time, Markdown renders right in the bubbles, history is stored locally, and the API key is encrypted.

## Features

- **Streaming responses** — tokens print in real time, with a Stop button
- **Markdown** — code, lists and bold right in the bubbles (Markwon)
- **Message bubbles** — like a real messenger, with timestamps
- **Persistent history** — dialogs in Room, survive restarts
- **Security** — API key in EncryptedSharedPreferences (AES256)
- **Customization** — opacity, font, corners, blur, themes (incl. AMOLED), Material You
- **Smart keyboard** — focus switches between the window and other apps
- **Drag & resize** — with geometry persistence
- **Long-press menu** — copy/delete messages, export dialog

## First launch

1. Get a key at [console.groq.com](https://console.groq.com)
2. Paste it in settings and tap "Check key"
3. Allow "display over other apps" and battery optimization exemption
4. Start the service and open the AI window from the notification

## Building from source

### Requirements

- JDK 17
- Android SDK (compileSdk 34, build-tools 34+)
- Gradle 8.7+ (or Android Studio)

### Steps

    git clone https://github.com/KorbanDallasG/GroqOverlay.git
    cd GroqOverlay
    echo "sdk.dir=/path/to/Android/Sdk" > local.properties
    gradle assembleDebug --no-daemon

The APK will be at app/build/outputs/apk/debug/.

### Signed release build

    keytool -genkeypair -v -keystore release-key.jks -alias groq-overlay-key -keyalg RSA -keysize 2048 -validity 10000
    gradle assembleRelease --no-daemon

The project is open: release-key.jks is in the repo, passwords android/android. Want privacy — create your own keystore.

---

## Project structure

    app/src/main/java/com/groqoverlay/app/
    +-- data/    Room, DAO, prefs, encrypted key, Groq client
    +-- ui/      Compose screens, adapter, components, theme
    +-- [root]   MainActivity, AiForegroundService (overlay + streaming)

About 1800 lines of Kotlin.

---

## Contributors

| | |
|---|---|
| **KorbanDallasG** | idea, requirements, testing, repo owner |
| **Qwen** (Alibaba Cloud) | code, architecture, docs, debugging |

See CONTRIBUTORS.md for details.

---

## License

Groq Overlay is licensed under the **GNU General Public License v3.0** — see the LICENSE file.

    Copyright (C) 2026 KorbanDallasG

    This is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

Third-party libraries: Markwon, OkHttp, AndroidX — Apache 2.0.

---

## Privacy

- API key — only in EncryptedSharedPreferences (AES256)
- Dialog history — local only (Room)
- No telemetry or analytics
- Traffic — HTTPS to api.groq.com only
