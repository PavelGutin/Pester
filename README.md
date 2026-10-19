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
