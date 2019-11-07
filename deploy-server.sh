version=$(git describe --tags --abbrev=0 | tr --delete v)

./gradlew server:dist -Pbuildversion=$version
scp server/build/libs/server-release.jar root@mindustry.nydus.app:/root/mindustry/server/
