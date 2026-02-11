# Building the Chronova Android APK

Since the Android SDK isn't available in this environment, you'll need to build the APK using Android Studio or the command line tools on your local machine.

## Method 1: Using Android Studio (Recommended)

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install Android SDK and build tools

2. **Open the Project**
   ```bash
   # Copy the chronova-android folder to your local machine
   # Open Android Studio and select "Open an existing project"
   # Navigate to the chronova-android folder
   ```

3. **Build the APK**
   - Wait for Gradle sync to complete
   - Go to `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - The APK will be generated in `app/build/outputs/apk/debug/`

## Method 2: Command Line Build

1. **Install Android SDK and tools**
   ```bash
   # Install Android Command Line Tools
   # Set ANDROID_HOME environment variable
   ```

2. **Navigate to project directory**
   ```bash
   cd chronova-android
   ```

3. **Build debug APK**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Build release APK**
   ```bash
   ./gradlew assembleRelease
   ```

## Generated APK Location

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

## Pre-built APK Download

If you don't want to build it yourself, you can:

1. **Use GitHub Actions** (if you have a repository)
   - Set up Android build workflow
   - Automatic APK generation on commits

2. **Use Online Build Services**
   - AppCenter, Firebase App Distribution
   - Upload project and get built APK

## Installing the APK

1. **Enable Unknown Sources** on your Android device
2. **Transfer APK** to your device
3. **Install** by tapping the APK file

## Signing for Release

For production release, you'll need to sign the APK:

1. **Generate keystore**:
   ```bash
   keytool -genkey -v -keystore my-release-key.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
   ```

2. **Add to `app/build.gradle`**:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file('path/to/my-release-key.keystore')
               storePassword 'password'
               keyAlias 'my-alias'
               keyPassword 'password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
               // ... other config
           }
       }
   }
   ```

3. **Build signed APK**:
   ```bash
   ./gradlew assembleRelease
   ```

## Troubleshooting

### Common Issues:

1. **Gradle sync fails**
   - Check internet connection
   - Update Android Studio and Gradle

2. **Missing SDK components**
   - Open SDK Manager in Android Studio
   - Install required SDK versions

3. **Build errors**
   - Check `Build` → `View Build Details`
   - Ensure all dependencies are available

### Required Android SDK Components:
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android Support Repository
- Google Play Services

## App Features Verification

After building, test these features:
- Login with API key and email/password
- Dashboard statistics display
- Language/Project/Editor charts
- Navigation between screens
- Data refresh functionality

The APK size should be approximately 15-25 MB depending on build type.