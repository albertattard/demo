#!/usr/bin/env bash
set -euo pipefail

echo 'Building the image'
docker build \
  --file './containers/Dockerfile-with-builder' \
  --tag 'demo:application-monolithic-layered-single-project' \
  --load \
  .

echo 'Running the image'
docker run \
  --rm \
  --detach \
  --name 'application-monolithic-layered-single-project' \
  --publish 8080:8080 \
  'demo:application-monolithic-layered-single-project'

echo 'Waiting for the application to start'
while [ "$(curl --silent --output /dev/null --write-out '%{http_code}' 'http://localhost:8080/cart/1')" -ne '200' ]; do echo 'Waiting for the application to start'; sleep 1; done

echo 'Trying the application'
curl --silent 'http://localhost:8080/catalogue/item/1' | jq
curl --silent 'http://localhost:8080/cart/1' | jq

echo 'Stopping the container'
docker stop 'application-monolithic-layered-single-project'

echo 'Image size'
docker images 'demo:application-monolithic-layered-single-project' --format "{{.Repository}}:{{.Tag}} -> {{.Size}}"

echo 'Done'