#!/bin/bash

set -euo pipefail

rm -f ~/.local/share/PrismLauncher/instances/'1.8.9 Java 17'/.minecraft/mods/ModernJavaLauncher-*
./gradlew :agent:build :build
cp ./build/libs/ModernJavaLauncher-1.0.0.jar ~/.local/share/PrismLauncher/instances/'1.8.9 Java 17'/.minecraft/mods




