#!/bin/sh
set -eu

recording=${1:?Usage: ./run-workload.sh RECORDING}
mkdir -p target "$(dirname "$recording")"
java -XX:FlightRecorderOptions=stackdepth=128 -jar target/socket-io-1.0.0.jar > target/application.log 2>&1 &
pid=$!
cleanup() { kill "$pid" 2>/dev/null || true; wait "$pid" 2>/dev/null || true; }
trap cleanup EXIT HUP INT TERM

ready=0
for attempt in $(seq 1 60); do
    if curl --silent --fail http://127.0.0.1:8080/quote/random > /dev/null; then
        ready=1
        break
    fi
    if ! kill -0 "$pid" 2>/dev/null; then
        cat target/application.log >&2
        exit 1
    fi
    sleep 1
done
if [ "$ready" -ne 1 ]; then
    cat target/application.log >&2
    exit 1
fi

jcmd "$pid" JFR.start name=SocketIO settings=profile > /dev/null
for request in 1 2 3 4 5; do
    curl --silent --show-error --fail --output /dev/null --write-out 'Request time: %{time_total} s\n' http://127.0.0.1:8080/quote/random
done
jcmd "$pid" JFR.stop name=SocketIO filename="$recording" > /dev/null
printf 'Recording: %s\n' "$recording"
