#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

JAVA_17_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
export JAVA_HOME="${JAVA_HOME:-$JAVA_17_HOME}"
export PATH="$JAVA_HOME/bin:$PATH"

if ! command -v java >/dev/null 2>&1; then
  echo "Java 17 is required. Set JAVA_HOME to a Java 17 installation."
  exit 1
fi

java_version="$(java -version 2>&1 | head -n 1)"
if ! echo "$java_version" | grep -E -q '"17| 17'; then
  echo "Java 17 is required. Set JAVA_HOME to a Java 17 installation."
  exit 1
fi

mvn -U -DskipTests clean install
mvn spring-boot:run
