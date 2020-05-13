#!/usr/bin/env bash
set -e

if [[ $(git describe --tags --dirty) =~ ^V?([0-9][0-9.]*(-dirty)?)$ ]]; then
  VERSION="${BASH_REMATCH[1]}"
elif [[ $(git describe --tags --dirty) =~ ^V?([0-9][0-9.]*)-([0-9]+)-g(.+(-dirty)?)$ ]]; then
  VERSION="${BASH_REMATCH[1]}-${BASH_REMATCH[3]}"
else
  exit 1
fi

echo $VERSION
