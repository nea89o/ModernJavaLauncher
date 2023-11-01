#!/bin/bash

set -euo pipefail

rm -f ~/.local/share/PrismLauncher/instances/1.8.9/.minecraft/mods/examplemod-*
./gradlew :build :target:build
cp ./build/libs/examplemod-1.0.0.jar ~/.local/share/PrismLauncher/instances/1.8.9/.minecraft/mods




