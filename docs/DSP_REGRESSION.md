# DSP regression — 2026-11-10

## Verified defect
The original code used `high=lowpass_3500(hp)` followed by `band=hp-high`, which **subtracted** the low-pass component and unintentionally retained frequencies **above** 3500 Hz. This was a high-pass stage, not a 250–3500 Hz band-pass.

## Fix
Use `band=high` to retain the low-pass output of the 250 Hz high-pass stage.

## Reproducible validation
A local JDK 21 `javac`/ `java` smoke test passed 13 checks after the fix. The previously failing 12 kHz suppression check now passes. For 0.08 input-amplitude steady sine tones at 48 kHz, the observed output RMS was:
- 1 kHz: 0.051943
- 60 Hz: 0.012809
- 12 kHz: 0.017287

These are **digital test signals**, not measured hearing improvement or acoustic SPL. Gradle/Android SDK was unavailable locally, so Android APK and GitHub CI remain unverified. The standalone GitHub Actions job may also fail to start if runners are blocked.
