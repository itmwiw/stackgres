#!/bin/sh

set -e

DISTRIBUTEDLOGS_CONTROLLER_IMAGE_NAME="${DISTRIBUTEDLOGS_CONTROLLER_IMAGE_NAME:-"stackgres/distributedlogs-controller:main"}"
BASE_IMAGE="registry.access.redhat.com/ubi8-minimal:8.4-208"
TARGET_DISTRIBUTEDLOGS_CONTROLLER_IMAGE_NAME="${TARGET_DISTRIBUTEDLOGS_CONTROLLER_IMAGE_NAME:-$DISTRIBUTEDLOGS_CONTROLLER_IMAGE_NAME}"

docker build -t "$TARGET_DISTRIBUTEDLOGS_CONTROLLER_IMAGE_NAME" --build-arg BASE_IMAGE="$BASE_IMAGE" -f distributedlogs-controller/src/main/docker/Dockerfile.native distributedlogs-controller
