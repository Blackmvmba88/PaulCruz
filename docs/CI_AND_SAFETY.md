# v0.1 CI and safety findings

## Observed on 2026-11-10
- PR #1 merged into main (b5abfd367024eecba24ef77afc46f9025a8df563).
- Codex review was completed with no code-review findings.
- Android Actions runs 42816884364 (main) and 42816808315 (PR) reported failure.
- GitHub returned jobs with zero available steps; attempts to fetch job logs returned HTTP 404 BlobNotFound.
- No confirmed Gradle or Java compiler error is available. The ~4-second jobs without steps are compatible with a failure before a runner starts, but runner provisioning/billing is **not verified**.

## Changes made
- Start output muted and validate actual routed headset before unmuting.
- Check route again before every write and mute immediately on detected mismatch.
- Prevent a new audio worker starting while the previous worker is closing.
- DSP startup gain ramp, immediate attenuation when the configured gain decreases.
- Extend deterministic JVM DSP tests.

## Remaining safety limitations
Audio routing can change between route check and a blocking write; this is **not a guarantee that a speaker can never receive audio**. Real hardware requires device callbacks, headset compatibility tests, acoustic calibration and fault injection. Digital ceiling 0.35 is not sound pressure calibration. No medically safe output claim. Do not use as a substitute for a validated hearing aid.

## CI recovery
1. Open GitHub Actions failed run and review the platform's top-level runner/usage warning.
2. Verify Actions enablement and account billing/usage quota if jobs never schedule.
3. Once a runner starts, record step logs and execute `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug --stacktrace`.
4. Test on Android device with wired and USB headset, background/permission loss and unplug events before enabling live use.
