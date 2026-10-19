# 🦟 Pester

An alarm/reminder app that refuses to be dismissed from across the room.

## Why

Some things need to actually happen, not just get acknowledged. Your spouse
asks you to move the laundry to the dryer, you say "yep," and then forty-five
minutes later it's still sitting there wet because a single "ok" to another
person is not a system — it's a thing you meant to remember. Same shape of
problem shows up everywhere:

- "Move the laundry to the dryer" — said out loud, immediately forgotten.
- Something's defrosting on the counter and needs to go in the oven by 5:30,
  or dinner's two hours later than planned.
- Take the trash cans in from the curb before you leave for work, not after
  you've already backed out of the driveway.
- Take your medication with lunch, not whenever you happen to remember you
  didn't.
- Bread's proofing, meat's marinating, something needs to come off the
  stove in 10 minutes — any kitchen task where a single timer alert is easy
  to half-hear and ignore.

A normal reminder app pings you once. You swipe it away — on purpose or by
reflex — and the task is now *more* forgotten than before, because your
brain filed it as "handled." Pester's whole premise is that dismissing the
notification and actually doing the thing are two different actions, and it
refuses to treat them as the same one. It keeps nagging, repeatedly, until
you open the app itself and confirm the task is done — not until you've
cleared a notification.

Two front ends over one nag engine:
- **Reminders** — one-shot "this needs to happen, and soon" nudges. The
  general-purpose case above.
- **Alarms** — recurring, day-of-week wake-up calls, for when the task is
  "get up."

Both nag identically once triggered: every 30 seconds, repeatedly, until you
open the app and hit **SWAT IT**. No snooze, anywhere, on purpose — snoozing
is just a slower way of doing the thing Pester exists to prevent.

## How this idea came about

The idea started from a narrower, more personal problem: I use a Garmin
watch as my morning alarm because its vibration is quiet enough not to wake
the rest of the household — but that also makes it trivially easy to
dismiss with a half-asleep wrist tap, so I kept not actually waking up.
Putting a loud phone alarm in another room (the classic "walk across the
room to turn it off" trick) wasn't an option either, since any sound loud
enough to hear from elsewhere in the house would wake everyone else too.

The fix turned out to be a neat trick: smartwatches buzz for *any* mirrored
phone notification, independently of whether the phone itself makes a sound
or vibrates. So the phone doesn't need to ring at all — it can sit in
another room, completely silent, and just keep re-posting a notification.
The watch buzzes every time, the phone never does, and the only way to stop
it is to walk over and deal with it directly. Once that mechanism existed,
it was obvious it wasn't really an alarm-clock feature — it was a general
"keep nagging me until I actually handle this" engine that happens to be
useful for alarms too. The Garmin angle is still in there (see
[Testing against a real Garmin](#testing-against-a-real-garmin)), but it's
one use case among several now, not the point of the app.

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
