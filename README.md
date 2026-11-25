# Credit Auto (Android)

An Android companion app (Kotlin + Jetpack Compose) that detects the store you're in, picks the credit card in your wallet that maximizes your rewards, and launches Google Wallet in one tap.

## What it does
- **Automatic Store Detection (Free, No API Key Required):** Uses your GPS + OpenStreetMap Overpass API to detect nearby stores and map them to spend categories (Grocery, Dining, Gas, Wholesale Clubs, Drugstores, Travel, etc.). Supports an optional Google Places API key in Settings, plus custom **Saved Places** with GPS matching and card/network overrides.
- **Smart Credit Card Engine:** Evaluates 22 preloaded popular US credit cards (Chase, Amex, Capital One, Citi, Discover, Wells Fargo, Bilt, US Bank, BofA, Amazon, Target) plus unlimited custom cards. Accounts for:
  - Category bonus multipliers & merchant-specific bonuses (e.g., Whole Foods 5%, Target 5%)
  - Google Pay / mobile wallet tap bonuses (e.g., US Bank Altitude Reserve 3x)
  - Editable point valuations (¢ per point)
  - Network restrictions (e.g., Costco Visa-only, "No Amex" toggle)
  - Foreign transaction fee penalties (~3% FTF)
  - Pinned card overrides for saved stores
- **Quick Settings Tile ("Best Card"):** Pull down Android's Quick Settings shade and tap **Best Card** to open a fast checkout overlay anywhere, see the #1 card, and jump straight into Google Wallet.
- **1-Tap Google Wallet Launcher + Reminder Notification:** Opens Google Wallet directly and posts a temporary high-priority reminder notification showing the winning card name & last 4 digits so you can swipe right to it before tapping the NFC terminal.

> **Platform note:** Android and Google Wallet do not expose any public API for third-party apps to programmatically change the default or active NFC tap-to-pay card inside Google Wallet. Credit Auto eliminates the mental math and gets you into Google Wallet with the winning card displayed at the top of your screen.

## Build & Install

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Install to a connected Android phone via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Enable the Quick Settings Tile on Android
1. Swipe down twice from the top of your Android screen to open Quick Settings.
2. Tap the **Edit (pencil)** icon.
3. Find **Best Card** and drag it into your active Quick Settings row.
