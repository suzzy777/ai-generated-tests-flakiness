#!/bin/bash
set -e
D="$(cd "$(dirname "$0")" && pwd)"
REPO="${1:-$HOME/.m2/repository}"
mvn -B -q org.apache.maven.plugins:maven-install-plugin:3.1.1:install-file \
  -Dfile="$D/chatunitest/chatunitest-core-2.1.1.jar" -DpomFile="$D/chatunitest/chatunitest-core-2.1.1.pom" \
  -Dmaven.repo.local="$REPO"
JAR="$REPO/io/github/zju-aces-ise/chatunitest-core/2.1.1/chatunitest-core-2.1.1.jar"
if unzip -l "$JAR" | grep -q "zju/cst/aces/util/JUnitFlavor.class"; then
  echo "OK: patched ChatUniTest core installed in $REPO"
else
  echo "ERROR: $JAR is not the patched core (no JUnitFlavor class)"; exit 1
fi
