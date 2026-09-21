---
name: run-locally
description: Build and run the Clocks and Dice app locally, serving it against real production Firebase, so a change can actually be exercised in a browser. Use when asked to run, start, launch, or test this app locally, or before claiming a UI/rendering fix works.
---

# Run Clocks and Dice locally

## Prerequisites (one-time per machine)

1. `src/dice_and_clocks/firebase_config.js` must exist. It's gitignored —
   copy `src/dice_and_clocks/example_firebase_config.js` and fill in the
   real config (ask Chris, or pull from the existing file if already
   present locally). Without it, `firebase_app.cljs`'s `initializeApp`
   call throws immediately at load.
2. `npm install` (installs `firebase`/`firebase-tools`/build deps — may
   get wiped by `just ci`'s `npm ci` step, so re-run after running the
   test suite).

## Build

```bash
just dev                                              # builds CSS (index.html is committed directly, no copy step)
lein run -m shadow.cljs.devtools.cli compile app      # compiles the app.js used by index.html
```

Do **not** use `lein watch` / `just watch` for a one-shot check — it also
tries to compile `:browser-test`/`:karma-test` shadow-cljs builds and hangs
waiting for file changes. `compile app` alone is enough to get a working
build and exits when done.

Confirm success: `Build completed. (N files, M compiled, 0 warnings, ...)`
with 0 warnings.

## Serve

Client-side routes (`/some-channel-name`) are not real files, and a plain
static file server 404s on them instead of falling back to `index.html`
the way Firebase Hosting's rewrite rule does in production. A bare
`python3 -m http.server` will NOT work for anything but the bare `/` route.

Use this SPA-fallback server instead:

```bash
cat > /tmp/dnd-server.py << 'EOF'
import http.server, socketserver, os
from urllib.parse import urlparse

ROOT = '/absolute/path/to/diceandclocks/resources/public'
os.chdir(ROOT)

class Handler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        parsed = urlparse(self.path)
        fs_path = ROOT + parsed.path
        if parsed.path != '/' and not os.path.isfile(fs_path):
            self.path = '/index.html'
        return super().do_GET()

with socketserver.TCPServer(("", 8899), Handler) as httpd:
    httpd.serve_forever()
EOF
python3 /tmp/dnd-server.py &
```

App is then at `http://localhost:8899/`. This talks to the **real
production Firebase project** (`clocksanddice-1b45c`) — there's no local
emulator config in this repo, so any channel joined/created here is real,
live data in the shared database, not a sandbox.

## Verify it's actually working

Load `http://localhost:8899/` in a browser or via Playwright and check for
zero console errors. `firebase_database.cljs:16 Write Succeeded` lines are
expected/healthy (logged on every successful write); anything else is a
real problem.

Drive the golden path to confirm a change actually works, not just that
the build succeeded:
1. Fill in a user name on the landing page, click **Join**.
2. Confirm the URL changes to `/<slugified-channel-name>?<name>` and the
   channel view (not the intro screen) renders.
3. Click **Roll** to roll dice; confirm a message row appears.
4. Click a clock-type icon to create a clock; confirm it appears in the
   clocks panel.

## Common failure: stale/killed server

If a browser tab shows `net::ERR_CONNECTION_REFUSED` for `output.css` or
`app.js`, the background server process died or was killed. Just restart
the `python3 /tmp/dnd-server.py &` command — no rebuild needed unless
source also changed.
