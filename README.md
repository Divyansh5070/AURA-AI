# Aura AI Chatbot

Aura AI is an Android chatbot app built with Kotlin and Jetpack Compose. The current scope focuses on a complete chat screen with Gemini-powered responses when an API key is configured, plus a static fallback responder so the app still works without live AI credentials.

## Features

- Jetpack Compose chat screen
- User and AI message bubbles
- Message input with send action
- Typing indicator
- Word-by-word response rendering
- Scrollable conversation history
- Local chat persistence with SharedPreferences
- Gemini API integration through `local.properties`
- Static demo responses when Gemini is not configured
- Google AdMob SDK integration
- App Open Ad lifecycle handling
- Adaptive banner ad placement inside the chat screen

## Screens Implemented

### Chat Screen

The app currently implements only the requested chat experience:

- Chat input box
- Send button
- User message bubbles
- AI response bubbles
- Typing indicator
- Scrollable conversation
- Persistent local chat history
- Inline adaptive banner ad

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Lifecycle ViewModel
- Kotlin Coroutines and StateFlow
- OkHttp
- Google Mobile Ads SDK
- Gson
- Gradle Kotlin DSL

## Gemini Configuration

Gemini credentials are intentionally not committed to Git.

Create a `local.properties` file from the example:

```properties
sdk.dir=/path/to/android/sdk
GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-2.5-flash
```

If `GEMINI_API_KEY` is blank or missing, Aura AI automatically uses static demo responses. This keeps the chat flow functional for review, testing, and APK demos without exposing private API keys.

## AdMob Configuration

The project currently uses Google official test ad IDs:

- App ID: `ca-app-pub-3940256099942544~3347511713`
- App Open Ad Unit: `ca-app-pub-3940256099942544/9257395921`
- Banner Ad Unit: `ca-app-pub-3940256099942544/6300978111`

Before publishing to production, replace these with real AdMob IDs from your AdMob account.

AdMob implementation includes:

- SDK initialization in the application class
- App Open Ad loading on app launch/resume
- Duplicate display prevention
- Ad expiration handling
- Adaptive banner ad inside the chat screen
- AdView cleanup on Compose release

## Build Setup

Clone the repository:

```bash
git clone https://github.com/Divyansh5070/AURA-AI.git
cd AURA-AI
```

Create `local.properties`:

```bash
cp local.properties.example local.properties
```

Update `sdk.dir` and optionally add your Gemini API key.

Build the debug APK:

```bash
./gradlew assembleDebug
```

The generated APK will be available locally at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Security Notes

The repository ignores local secrets and generated artifacts, including:

- `local.properties`
- `.env` files
- keystores and signing files
- `google-services.json`
- generated APK/AAB files
- Gradle build output
- IDE workspace files

Do not commit real API keys, signing keys, or production AdMob credentials.

## Project Structure

```text
app/src/main/java/com/chat/aichatbot/
├── admob/
│   ├── AdBannerView.kt
│   └── AppOpenAdManager.kt
├── data/
│   ├── ApiConfig.kt
│   ├── ChatHistoryStore.kt
│   ├── ChatMessage.kt
│   └── StaticChatResponder.kt
├── ui/
│   ├── ChatScreen.kt
│   └── theme/
├── viewmodel/
│   └── ChatViewModel.kt
├── ChatBotApp.kt
└── MainActivity.kt
```

## Current Status

- Chat screen: complete
- Gemini live response path: implemented
- Static fallback path: implemented
- App Open Ad: implemented with test ad unit
- Banner Ad: implemented with test ad unit
- Debug APK build: verified

## License

This project is currently private/proprietary unless a license file is added.
