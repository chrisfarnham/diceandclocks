#!/usr/bin/env bash
# Deletes RTDB channels marked {:test true} by preview-deploy sessions
# (see dice_and_clocks.config/preview-channel? and views/enter-channel!).
# Each matched channel's data is dumped to $BACKUP_DIR before removal.
set -euo pipefail

project="${FIREBASE_PROJECT:-clocksanddice-1b45c}"
backup_dir="${BACKUP_DIR:-test-channel-backups}"

channels=$(firebase database:get "/channels" --project "$project" \
  --order-by test --equal-to true | jq -r 'keys[]?')

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
