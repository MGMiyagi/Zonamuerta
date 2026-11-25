# Zona Muerta (Touch Blocker)

Android Kotlin project for a prototype rectangular touch-blocking overlay.

## Prototype contents
- Rectangle initially at the upper-left corner.
- Width/height controls in dp.
- Enable/disable controls.
- Persistent notification with stop action.
- Semi-transparent red overlay for verifying placement.

**This is source code, not an APK. It has not been compiled or tested on a device.** Overlay touch interception may vary with Android/REDMAGIC and the damaged touchscreen.

## Cloud build (GitHub Actions)
1. Create a GitHub repository and upload the contents of this folder (`ZonaMuerta`) to its root, including the hidden `.github` folder.
2. Open the repository's **Actions** tab.
3. Select **Build Zona Muerta APK** in the workflow list.
4. Tap **Run workflow** and confirm.
5. Wait for the run to finish successfully, then open that run and download the artifact named **ZonaMuerta-debug-apk**.
6. Extract the artifact ZIP; inside is `app-debug.apk`. Open it on your Android phone and install it. If Android prompts for permission to install unknown apps, allow it for the file manager/browser you used.

## Local build
Open the `ZonaMuerta` folder in Android Studio, sync Gradle, and run/build the app.
