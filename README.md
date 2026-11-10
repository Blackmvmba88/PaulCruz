# PaulCruz v0.1 — Android
BlackMamba Corp experimental audio assistance prototype. **Not a certified hearing aid or medical device.**

## Implemented scope
- Live microphone input (Android AudioRecord) → speech-focused DSP → AudioTrack output
- 48 kHz, 16-bit mono, 10 ms application blocks; requested low-latency output mode
- Simple high-pass + low-pass speech band (~250–3500 Hz), elementary background noise gate
- Dynamic compression, adjustable gain 0–2× (default 0.5×), hard digital ceiling ±0.35
- Runtime microphone permission, wired/USB headset-only playback, stop on background/unplug
- No cloud processing, no recordings, no network permission, no account

## Build
Use Android Studio with Android SDK 35 and JDK 17 or execute:
```sh
gradle :app:testDebugUnitTest :app:assembleDebug
```
CI uses Gradle 8.13 and Android Gradle Plugin 8.13.2. Minimum Android API 26.

## Safety and measurements
**The digital limiter is not an acoustic SPL safety limit.** Actual headset output level depends on phone volume, headset sensitivity and hardware. Start with phone volume at minimum. No human hearing-assistance use before calibrated acoustic measurements.

Wired/USB headset required; Bluetooth and speaker excluded in v0.1. Requested low-latency mode does not guarantee round-trip latency, and some hardware audio routes may be unavailable.

DSP is intentionally basic: not neural speech enhancement, adaptive noise cancellation, beamforming, calibrated feedback rejection or individualized audiogram. The filter may alter speech timbre; speech clarity benefit has not been demonstrated.

## Test plan
1. Run `gradle :app:testDebugUnitTest :app:assembleDebug` (GitHub Actions).
2. Verify permission denial and grant, headphones unplug, app background, routing errors on physical devices.
3. Record loopback latency p50/p95 and underruns across phones, wired headsets and USB DACs.
4. Measure actual acoustic sound pressure with calibrated equipment and multiple output volume settings.
5. Compare intelligibility with level-matched speech/noise stimuli; do not confuse making all sound quieter with improving signal-to-noise ratio.

## Structure
- `app/src/main/java/com/blackmamba/paulcruz/MainActivity.java`: minimal foreground UI.
- `app/src/main/java/com/blackmamba/paulcruz/audio/AudioLoop.java`: capture/routing/playback.
- `app/src/main/java/com/blackmamba/paulcruz/dsp/SafeSpeechProcessor.java`: pure Java DSP.
- `app/src/test/java/com/blackmamba/paulcruz/dsp/`: deterministic unit tests.

## Roadmap
v0.2: measured latency, route listener, output calibration and noise tests. Later: adaptive denoising, proper feedback suppression and separate validated hearing profiles.
