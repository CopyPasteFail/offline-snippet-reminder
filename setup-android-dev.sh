#!/usr/bin/env bash
# setup-android-dev.sh
# Bootstrap Android development environment on Debian/Ubuntu (or WSL Ubuntu)
# Places Android SDK at $HOME/Android/Sdk and updates ~/.bashrc
# Usage: chmod +x setup-android-dev.sh && ./setup-android-dev.sh
set -euo pipefail

# CONFIG
SDK_ROOT="$HOME/Android/Sdk"
CMDLINE_TOOLS_DIR="$SDK_ROOT/cmdline-tools"
LATEST_DIR="$CMDLINE_TOOLS_DIR/latest"
TMP_DIR="/tmp/offline-snippet-android-setup"
CMDLINE_ZIP="$TMP_DIR/commandlinetools-linux.zip"
CMDLINE_DL_URL="https://dl.google.com/android/repository/commandlinetools-linux-latest.zip"
REQUIRED_PACKAGES=(openjdk-17-jdk unzip wget curl git)
PLATFORMS=("platform-tools" "platforms;android-36" "build-tools;36.0.0" "emulator")

# Helper: print and run
run() { echo "+ $*"; "$@"; }

if [[ "$(uname -s)" != "Linux" ]]; then
  echo "This script targets Linux (including WSL). Abort."
  exit 1
fi

echo "== Android dev setup script =="
echo "Will install SDK to: $SDK_ROOT"
echo

# 1) Install system deps
echo "-> Installing system packages (sudo required)..."
sudo apt-get update
sudo apt-get install -y "${REQUIRED_PACKAGES[@]}"

# 2) Ensure Java is on PATH
if ! command -v java >/dev/null 2>&1; then
  echo "Java not found after install. Aborting."
  exit 1
fi
JAVA_BIN="$(readlink -f "$(command -v java)")"
JAVA_HOME="$(dirname "$(dirname "$JAVA_BIN")")"
echo "Detected JAVA_HOME: $JAVA_HOME"

# 3) Create SDK directories
mkdir -p "$TMP_DIR"
mkdir -p "$LATEST_DIR"

# 4) Download Android commandline tools (if not present)
if [[ -f "$CMDLINE_ZIP" ]]; then
  echo "Found existing commandline zip: $CMDLINE_ZIP"
else
  echo "Downloading Android command-line tools..."
  run wget -O "$CMDLINE_ZIP" "$CMDLINE_DL_URL"
fi

# 5) Extract into $SDK_ROOT/cmdline-tools/latest
echo "Extracting command-line tools into $LATEST_DIR ..."
run unzip -o "$CMDLINE_ZIP" -d "$TMP_DIR"
# the zip contains a folder named "cmdline-tools". move that into latest/
# accommodate both structures
if [[ -d "$TMP_DIR/cmdline-tools" ]]; then
  rm -rf "$LATEST_DIR"
  mkdir -p "$(dirname "$LATEST_DIR")"
  mv "$TMP_DIR/cmdline-tools" "$LATEST_DIR"
else
  # sometimes the zip unpacks to a different structure; move all contents
  rm -rf "$LATEST_DIR"
  mkdir -p "$LATEST_DIR"
  mv "$TMP_DIR"/* "$LATEST_DIR"/ || true
fi

# 6) Ensure platform-tools and sdkmanager are available
SDKMANAGER="$LATEST_DIR/bin/sdkmanager"
if [[ ! -x "$SDKMANAGER" ]]; then
  echo "sdkmanager not found at $SDKMANAGER. Listing $LATEST_DIR:"
  ls -la "$LATEST_DIR"
  echo "Aborting."
  exit 1
fi

# 7) Add env exports to ~/.bashrc if not already present
BASHRC="$HOME/.bashrc"
ENV_MARKER="# >>> offline-snippet-android-sdk >>>"
if ! grep -q "$ENV_MARKER" "$BASHRC"; then
  echo "Writing Android SDK env variables to $BASHRC"
  cat >> "$BASHRC" <<EOF

$ENV_MARKER
export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_HOME="\$ANDROID_SDK_ROOT"
export PATH="\$PATH:\$ANDROID_SDK_ROOT/platform-tools:\$ANDROID_SDK_ROOT/emulator:$LATEST_DIR/bin"
export JAVA_HOME="$JAVA_HOME"
# <<< offline-snippet-android-sdk <<<
EOF
  echo "Please run: source ~/.bashrc or open a new shell to pick up the changes."
else
  echo "Environment lines already present in $BASHRC, skipping edit."
fi

# 8) Use sdkmanager to install platform-tools and build-tools
echo
echo "-> Installing SDK packages via sdkmanager (this may take a while)."
# run sdkmanager with the SDK root in env
export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$PATH:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$LATEST_DIR/bin"
export JAVA_HOME="$JAVA_HOME"

# Accept licenses non-interactively where possible. Some versions require interactive acceptance.
# We'll attempt to install then run --licenses for manual acceptance if needed.
echo "Installing required packages: ${PLATFORMS[*]}"
run "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" "${PLATFORMS[@]}"

echo
echo "Now accepting licenses. You will be prompted to accept; reply 'y' to each."
run yes | "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" --licenses || true

# 9) Optional: install an x86_64 system image for emulator (comment if you don't want emulator)
echo
read -r -p "Do you want to install Android emulator system-image (android-36 google_apis x86_64)? [y/N] " install_img
if [[ "${install_img,,}" == "y" ]]; then
  echo "Installing system image..."
  run "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" "system-images;android-36;google_apis;x86_64"
  echo "Create an AVD named 'pixel' (x86_64). This requires KVM on real Linux; WSL2 emulator support is limited."
  if command -v avdmanager >/dev/null 2>&1; then
    echo "Creating AVD..."
    run avdmanager create avd -n pixel -k "system-images;android-36;google_apis;x86_64" --force
    echo "AVD 'pixel' created. Start with: emulator -avd pixel"
  else
    echo "avdmanager not found in PATH. You can create an AVD later with avdmanager in $LATEST_DIR/bin"
  fi
fi

# 10) Final notes
echo
echo "== Done =="
echo "Android SDK installed to: $ANDROID_SDK_ROOT"
echo "Command-line tools location: $LATEST_DIR"
echo
echo "Important next steps (run these in new shell or run 'source ~/.bashrc'):"
echo "  export ANDROID_SDK_ROOT=\"$ANDROID_SDK_ROOT\""
echo "  export PATH=\"\$PATH:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$LATEST_DIR/bin\""
echo "Check adb:"
echo "  adb --version"
echo
echo "To build your project:"
echo "  cd /path/to/offline-snippet-reminder"
echo "  ./gradlew assembleDebug"
echo
echo "To install APK on a real device:"
echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo
echo "If something fails, re-run this script or inspect $TMP_DIR for logs and extracted files."
