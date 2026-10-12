#!/usr/bin/env bash
# Deletes RTDB channels marked {:test true} by preview-deploy sessions
# (see dice_and_clocks.config/preview-channel? and views/enter-channel!).
# Each matched channel's data is dumped to $BACKUP_DIR before removal.
set -euo pipefail

project="${FIREBASE_PROJECT:-clocksanddice-1b45c}"
backup_dir="${BACKUP_DIR:-test-channel-backups}"

# Capture the raw output first: if the CLI fails or prints something that
# isn't JSON (a banner, an auth or permission error), show it instead of
# letting jq fail with a bare parse error. `null` is the CLI's answer for
# "no matching channels".
err=$(mktemp)
status=0
raw=$(firebase database:get "/channels" --project "$project" \
  --order-by test --equal-to true 2>"$err") || status=$?
if [ "$status" -ne 0 ] || ! channels=$(echo "$raw" | jq -r '(. // {}) | keys[]' 2>/dev/null); then
  echo "firebase database:get failed or did not return JSON (exit $status)." >&2
  echo "--- stdout:" >&2; echo "$raw" >&2
  echo "--- stderr:" >&2; cat "$err" >&2
  exit 1
fi

if [ -z "$channels" ]; then
  echo "No test channels found."
  exit 0
fi

mkdir -p "$backup_dir"

echo "$channels" | while read -r channel; do
  echo "Backing up and removing test channel: $channel"
  firebase database:get "/channels/$channel" --project "$project" \
    -o "$backup_dir/$channel.json"
  firebase database:remove "/channels/$channel" --project "$project" -f
done
