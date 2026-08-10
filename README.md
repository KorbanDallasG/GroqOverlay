# Groq Overlay

Плавающее AI-окно поверх любого приложения на Android. Работает на Groq API (Llama, DeepSeek, Gemma, Mixtral).

Поддержать проект: https://send.monobank.ua/jar/4SPUc2SjFW

---

## Возможности

- **Streaming-ответы** — токены печатаются в реальном времени, кнопка «Стоп»
- **Markdown** — код, списки и жирный текст прямо в пузырях (Markwon)
- **Пузыри сообщений** — как в настоящем мессенджере, с временем
- **Персистентная история** — диалоги в Room, переживают перезапуск сервиса
- **Безопасность** — API-ключ в EncryptedSharedPreferences (AES256)
- **Кастомизация** — прозрачность, шрифт, скругления, размытие, темы (включая AMOLED), Material You
- **Умная клавиатура** — фокус переключается между окном и другими приложениями
- **Drag и resize** — с сохранением геометрии окна
- **Long-press меню** — копирование/удаление сообщений, экспорт диалога
- **Множество моделей** — Llama 3.3, DeepSeek-R1, Gemma, Mixtral

## Первый запуск

1. Получите ключ на console.groq.com
2. Вставьте в настройках и нажмите «Проверить ключ»
3. Разрешите «поверх окон» и игнорирование оптимизации батареи
4. Запустите сервис и откройте AI-окно из уведомления

## Сборка из исходников

Требования: JDK 17, Android SDK (compileSdk 34), Gradle 8.7+

    git clone https://github.com/KorbanDallasG/GroqOverlay.git
    cd GroqOverlay
    echo "sdk.dir=/путь/к/Android/Sdk" > local.properties
    gradle assembleDebug --no-daemon

APK окажется в app/build/outputs/apk/debug/.

Подписанная релизная сборка:

    keytool -genkeypair -v -keystore release-key.jks -alias groq-overlay-key -keyalg RSA -keysize 2048 -validity 10000
    gradle assembleRelease --no-daemon

## Структура проекта

    app/src/main/java/com/groqoverlay/app/
    +-- data/    Room, DAO, настройки, шифрованный ключ, Groq-клиент
    +-- ui/      Экраны Compose, адаптер, компоненты, тема
    +-- [root]   MainActivity, AiForegroundService (overlay + streaming)

Около 1800 строк Kotlin.

## Участники

| | |
|---|---|
| **KorbanDallasG** | автор идеи, постановка задач, тестирование, владелец репозитория |
| **Qwen** (Alibaba Cloud) | реализация кода, архитектура, документация, отладка |

Подробнее — CONTRIBUTORS.md.

## Лицензия

Groq Overlay распространяется на условиях **GNU General Public License v3.0** — см. файл LICENSE.

    Copyright (C) 2026 KorbanDallasG

    Это свободная программа: вы можете распространять её и/или изменять
    на условиях GNU GPL, версии 3 или (по вашему выбору) любой более поздней.

Сторонние библиотеки: Markwon, OkHttp, AndroidX — Apache 2.0.

## Приватность

- API-ключ — только в EncryptedSharedPreferences (AES256)
- История диалогов — только локально в Room
- Никакой телеметрии и аналитики
- Трафик — только HTTPS к api.groq.com
