<p align="center">
  <img src="logo.png" width="220" alt="Groq Overlay">
</p>

<h1 align="center">Groq Overlay</h1>

<p align="center">
  <b>Плавающее AI-окно поверх любого приложения на Android — без root-прав</b>
</p>

<p align="center">
  <img alt="License" src="https://img.shields.io/badge/License-GPLv3-blue.svg">
  <img alt="platform" src="https://img.shields.io/badge/platform-Android%208%2B-green">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-1.9.24-purple">
  <img alt="UI" src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange">
  <img alt="API" src="https://img.shields.io/badge/Groq-streaming-yellow">
</p>

<p align="center">
  Русский · <a href="README_EN.md">English</a>
</p>

<p align="center">
  Поддержать проект можно тут: <a href="https://send.monobank.ua/jar/4SPUc2SjFW">send.monobank.ua</a>
</p>

---

## Что это

Groq Overlay — плавающее окно с AI-ассистентом на базе Groq API (Llama 3.3, DeepSeek-R1, Gemma, Mixtral), доступное поверх любого приложения. Спрашивай что угодно, не сворачивая текущее приложение.

Ответы приходят потоком (streaming), Markdown рендерится прямо в пузырях, история хранится локально, а API-ключ зашифрован.

## Возможности

- **Streaming-ответы** — токены печатаются в реальном времени, кнопка «Стоп»
- **Markdown** — код, списки и жирный текст прямо в пузырях (Markwon)
- **Пузыри сообщений** — как в настоящем мессенджере, с временем
- **Персистентная история** — диалоги в Room, переживают перезапуск
- **Безопасность** — ключ в EncryptedSharedPreferences (AES256)
- **Кастомизация** — прозрачность, шрифт, скругления, размытие, темы (включая AMOLED), Material You
- **Умная клавиатура** — фокус переключается между окном и другими приложениями
- **Drag и resize** — с сохранением геометрии окна
- **Long-press меню** — копирование/удаление сообщений, экспорт диалога

## Первый запуск

1. Получите ключ на [console.groq.com](https://console.groq.com)
2. Вставьте в настройках и нажмите «Проверить ключ»
3. Разрешите «поверх окон» и игнорирование оптимизации батареи
4. Запустите сервис и откройте AI-окно из уведомления

## Сборка из исходников

### Что понадобится

- JDK 17
- Android SDK (compileSdk 34, build-tools 34+)
- Gradle 8.7+ (или Android Studio)

### Шаги

    git clone https://github.com/KorbanDallasG/GroqOverlay.git
    cd GroqOverlay
    echo "sdk.dir=/путь/к/Android/Sdk" > local.properties
    gradle assembleDebug --no-daemon

Готовый APK окажется в app/build/outputs/apk/debug/.

### Termux

На Android (Termux) aapt2 из Maven не работает, поэтому укажите системный (однократно):

    mkdir -p ~/.gradle
    echo "android.aapt2FromMavenOverride=$PREFIX/bin/aapt2" >> ~/.gradle/gradle.properties

### Подписанная релизная сборка

    keytool -genkeypair -v -keystore release-key.jks -alias groq-overlay-key -keyalg RSA -keysize 2048 -validity 10000
    gradle assembleRelease --no-daemon

Релизный APK подписан ключом разработчика. Чтобы собрать свою подписанную версию — создайте собственный keystore командой выше.

---

## Структура проекта

    app/src/main/java/com/groqoverlay/app/
    +-- data/    Room, DAO, настройки, шифрованный ключ, Groq-клиент
    +-- ui/      Экраны Compose, адаптер, компоненты, тема
    +-- [root]   MainActivity, AiForegroundService (overlay + streaming)

Около 1800 строк Kotlin.

---

## Участники

| | |
|---|---|
| **KorbanDallasG** | автор идеи, постановка задач, тестирование, владелец репозитория |
| **Qwen** (Alibaba Cloud) | реализация кода, архитектура, документация, отладка |

Подробнее — CONTRIBUTORS.md.

---

## Лицензия

Groq Overlay распространяется на условиях **GNU General Public License v3.0** — см. файл LICENSE.

    Copyright (C) 2026 KorbanDallasG

    Это свободная программа: вы можете распространять её и/или изменять
    на условиях GNU GPL, версии 3 или (по вашему выбору) любой более поздней.

Сторонние библиотеки: Markwon, OkHttp, AndroidX — Apache 2.0.

---

## Приватность

- API-ключ — только в EncryptedSharedPreferences (AES256)
- История диалогов — только локально в Room
- Никакой телеметрии и аналитики
- Трафик — только HTTPS к api.groq.com
