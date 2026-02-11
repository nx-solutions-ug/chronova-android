#!/bin/bash

# Docker-based Android APK build script
echo "🐳 Building Chronova Android APK using Docker..."

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Build the Docker image
echo "🔨 Building Docker image..."
docker build -f Dockerfile.build -t chronova-android-builder .

# Create output directory
mkdir -p build-output

# Run the build
echo "📱 Building APK..."
docker run --rm \
    -v "$(pwd)/build-output:/app/app/build/outputs" \
    chronova-android-builder

# Check if APK was created
APK_FILE="build-output/apk/debug/app-debug.apk"
if [ -f "$APK_FILE" ]; then
    echo "✅ APK built successfully!"
    echo "📱 Location: $APK_FILE"
    echo "📊 APK Size: $(du -h "$APK_FILE" | cut -f1)"
else
    echo "❌ Build failed! Check the Docker output above."
    exit 1
fi