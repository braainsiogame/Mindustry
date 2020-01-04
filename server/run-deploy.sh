#!/usr/bin/env bash
./gradlew server:dist
scp server/build/libs/server-release.jar root@mindustry.nydus.app:/root/crater/
