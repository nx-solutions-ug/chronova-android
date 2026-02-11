#!/bin/bash

# Chronova Android Build Script
# This script helps build the APK with proper setup

echo "🚀 Building Chronova Android APK..."

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo "❌ Please run this script from the chronova-android directory"
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Check what type of build to make
BUILD_TYPE="debug"
if [ "$1" = "release" ]; then
    BUILD_TYPE="release"
    echo "🔨 Building RELEASE APK..."
else
    echo "🔨 Building DEBUG APK..."
fi

# Build the APK
if [ "$BUILD_TYPE" = "release" ]; then
    ./gradlew assembleRelease
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
else
    ./gradlew assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

cp "$APK_PATH" "../public/downloads/chronova-$BUILD_TYPE.apk"

# Check if build was successful
if [ -f "$APK_PATH" ]; then
    echo "✅ APK built successfully!"
    echo "📱 Location: $APK_PATH"
    echo "📊 APK Size: $(du -h "$APK_PATH" | cut -f1)"

    # Show install instructions
    echo ""
    echo "📋 To install on your Android device:"
    echo "   1. Enable 'Unknown sources' in Android settings"
    echo "   2. Copy $APK_PATH to your device"
    echo "   3. Tap the APK file to install"
    echo ""
    echo "🔗 Or install directly via ADB:"
    echo "   adb install $APK_PATH"
else
    echo "❌ Build failed! Check the output above for errors."
    exit 1
fi
