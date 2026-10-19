# 🦟 Pester

An alarm/reminder app that refuses to be dismissed from across the room.

## Why

Smartwatches mirror phone notifications and buzz for them independently of
whether the phone itself makes any sound. Pester exploits exactly that: it
schedules an alarm or reminder like normal, but when it fires it **never
rings or vibrates the phone itself** — it just keeps re-posting a silent
notification every 30 seconds. Your paired watch (Garmin, Wear OS, whatever)
buzzes on every one of those, because that's how it's configured to behave
for any notification. The phone can sit in another room, silent, and the
only way to stop the nagging is to physically walk over, pick it up, and hit
**SWAT IT** inside the app. A notification swipe or a half-asleep wrist tap
does nothing.

Two front ends over one nag engine:
- **Alarms** — recurring, day-of-week wake-up calls.
- **Reminders** — one-shot "do not let me forget this" nudges.

Both nag identically once triggered. No snooze, anywhere, on purpose.

## Design

Dusk/bug-zapper theme: deep purple-black background, acid-green and
hot-pink neon accents, comic-style "BZZZT!" payoff when you finally swat
the alarm. Mascots are a mosquito (Alarms — the annoying one you swat) and
a firefly (Reminders — the friendly one you catch). App icon is the
mosquito.

## Screenshots

<!-- screenshots -->

## Getting it running

1. Open this project in Android Studio.
2. Let Gradle sync (uses the committed wrapper, Kotlin 2.0, Compose, Room).
3. Run on a **physical device** paired with your watch — an emulator can't
   test real Bluetooth notification mirroring.
4. On first launch, grant the notification permission, the exact-alarm
   permission, and the battery-optimization exemption when Pester asks.
5. In your watch's companion app (e.g. Garmin Connect), make sure Pester
   isn't excluded from notification mirroring.

Or grab the latest debug APK from the
[Releases](../../releases) page and sideload it directly.

## Testing against a real Garmin

This is the part that matters: the phone must stay silent and the watch must
still buzz. Here's how to actually verify that end to end.

1. Pair your Garmin with the phone running Pester via the **Garmin Connect**
   app, if it isn't already.
2. In Garmin Connect, go to the device's **Notifications** settings and
   confirm **Pester** is allowed through (not in an excluded-apps list). Some
   Garmin models also have a **Do Not Disturb** schedule on the watch itself
   under Settings → Notifications — make sure it isn't active during your
   test window, and check the watch's own "vibrate on notification" setting
   is on.
3. Put the phone on **silent/vibrate-off** (or just confirm Pester's two
   notification channels — "Pester nags" and "Pester is pestering" — are set
   to no sound/no vibration in Android's own per-app notification settings,
   which is their default on first install).
4. Create an Alarm or Reminder in Pester a couple of minutes out. Leave the
   phone screen off, ideally in another room.
5. Expected result: the phone makes **zero** sound or vibration. The watch
   buzzes once the nag fires, then keeps buzzing roughly every 30 seconds —
   each cycle is a fresh notification (Pester cancels and re-posts rather
   than updating in place) specifically so the watch treats every cycle as a
   new alert worth buzzing for, not a silent update it can ignore.
6. Walk to the phone, open Pester (tapping the notification works even
   screen-off, since it's a full-screen intent), and hit **SWAT IT**.
   Confirm the watch stops buzzing within that same cycle.
7. If the watch never buzzes at all: check step 2 again first — a Garmin
   notification exclusion or an active watch-side Do Not Disturb schedule is
   the most common cause, not a bug in the app.

## Architecture

- `data/` — Room entity/DAO for the shared `Nag` model (alarms and
  reminders are the same table, differing only in recurrence).
- `alarm/` — `AlarmManager` scheduling, the trigger `BroadcastReceiver`,
  and boot-persistence.
- `nag/` — the foreground service that loops the silent notification every
  30s, and the full-screen `DismissActivity` with the SWAT button.
- `ui/` — Compose UI: Alarms/Reminders tabs, add/edit dialog, theme.

## License

Personal project, no license granted.
