# Hent IR Remote / Hent Controle IR

[Português (Brasil)](README.pt-BR.md) · English

Open-source Android infrared remote for investigating Hent 12V/24V dual-fan
rooftop air conditioners. **Experimental: device compatibility is not verified.**

## What works and what remains

The app includes all **13 physical buttons** from the official Hent remote
photograph. This is **not yet a complete working clone**: original infrared
codes have not been captured. An optional Transcold **M1-F-NO-6** candidate
is available for eight button functions; its compatibility with Hent is unknown.
A supplier named Shenzhen Transcold is not proof that the same protocol is used.
No missing functions are simulated or advertised as working.

| Original button | Built-in candidate | Original raw code import |
| --- | --- | --- |
| Lamp / LED | None | Yes |
| Power | Separate on/off commands | Yes |
| Temperature + / − | 18–30 °C | Yes, each button |
| Speed + / − | Auto / low / medium / high | Yes, each button |
| Auto | Automatic mode | Yes |
| Strong | None | Yes |
| Eco (standard cooling in Hent's diagram) | None | Yes |
| Sleep | None | Yes |
| Swing flap | Toggle | Yes |
| Fresh air | Fan-only mode | Yes |
| Display | None | Yes |

A JSON profile can map **all 13 commands** once the real signals are known.
Unmapped buttons explain what is missing and do not transmit. Imported signals
always take priority over experimental candidates. With the candidate disabled,
only explicitly imported codes can transmit.

## Install and use

1. Download an APK from this repository's **Releases** tab, or get the
   `hent-ir-remote-debug-apk` artifact from a successful **Actions → Build** run.
2. Install on Android 6+ with a Consumer IR emitter (for example a compatible
   Redmi Note 13 Pro). Hardware is checked at runtime; not all Android phones
   have an emitter. Emulators cannot test physical infrared.
3. Choose **Português (Brasil)** or **English** in Language. One APK includes both.
4. Import an original profile if available. The unrelated Transcold candidate is already enabled for testing; read
   the compatibility notice. No profile import is needed for these candidates.
5. Point the top of the phone at the AC receiver and send one command.
   “Sent” means Android accepted transmission, not that the AC responded.
6. Save local settings and record the observed test result. Export a report
   if you want to share feedback yourself.

The Transcold controls represent **locally requested state**, not measured AC
state. Power offers explicit on/off choices; temperature +/− selects cooling
mode in this candidate. Fresh air selects fan-only. Raw imported buttons do
not change the local Transcold state because their semantics are unknown.
Carrier frequency support is checked, transmissions run off the UI thread,
and concurrent button sends are blocked. There is no automatic code sweep.

## Add all original codes

See [profile format](docs/PROFILES.md) and the empty
[original template](profiles/hent-original-template.json).
The phone's standard Consumer IR API is **transmit-only**. Capturing a real
remote requires an external receiver or a trustworthy existing dump. A camera
showing an IR LED flashing is not a full protocol capture.

## Build

Requirements: JDK 17, Android SDK platform 35 and build-tools 35.0.0.
The Gradle 8.9 wrapper and Android Gradle Plugin 8.7.3 are pinned.

```sh
python3 tools/check_resources.py
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Set `ANDROID_HOME` or the ignored `local.properties` (`sdk.dir=...`).
Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
Without signing environment variables, local release builds produce an
**unsigned** release APK; install the signed debug APK for testing instead.

## CI and releases

- Pushes to `main`, pull requests and manual Build runs test the encoder and
  profile parser, check both languages, run Android lint, build debug/release,
  and upload the installable debug APK plus reports.
- A successful push build on `main` automatically publishes the current version
  as a debug-signed experimental prerelease if that version is not published yet.
- Push a tag matching `versionName`, such as `v0.1.1`, or manually run
  **Release** with that tag. It repeats checks before publishing a GitHub
  **prerelease**, with bilingual notes and SHA-256 checksums.
- Optional repository secrets: `RELEASE_KEYSTORE_BASE64`,
  `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.
  When configured, releases contain a maintainer-signed release APK.
- Without those secrets, the workflow deliberately publishes an installable
  **debug-signed experimental APK**, named accordingly. Each runner may use a
  different debug signing key: uninstalling the old APK can be necessary,
  deleting local app data. Export your profile first.
- Never commit signing keys. A release tag must match `versionName`; increase
  `versionCode` for subsequent Android updates. Existing releases are not overwritten.

## Privacy and license

No internet permission, ads, accounts, analytics or automatic uploads. Imported
profiles, local settings and test results stay on the phone. Exported documents
are saved only where you choose through Android's file picker.

**GPL-3.0-or-later.** Contributions and forks are welcome. The Transcold encoder
is adapted from IRremoteESP8266; see [third-party notices](THIRD_PARTY_NOTICES.md).
No affiliation with or endorsement by Hent, Xiaomi or Transcold.

## Research references

- [Official Hent 12V rooftop product, SKU 5284-5](https://hentautomotivos.com.br/ar-condicionado/ar-condicionado-12v-inverter-teto-p)
- [Official remote photograph and labels](https://cdn.dlojavirtual.com/static1/102249/sku/ar-condicionado-ar-condicionado-12v-inverter-teto-p-1753721299939.jpg)
- [User-identified Alibaba product](https://www.alibaba.com/product-detail/Wholesale-12V-24V-AC-Battery-Electric_1601601019022.html)
- [Transcold protocol source](https://github.com/crankyoldgit/IRremoteESP8266/blob/master/src/ir_Transcold.cpp)
- [Original protocol investigation, M1-F-NO-6](https://github.com/crankyoldgit/IRremoteESP8266/issues/1256)

No original Hent remote codes were located or verified during initial research.

## Edit built-in codes

The candidate is enabled on first launch in v0.1.1. All state codes, carrier and
timings are loaded from `app/src/main/assets/ir/transcold-candidate.json`.
Per-button raw fixes belong in `app/src/main/assets/ir/original-overrides.json`.
Users do not need to import anything. See [editing codes](docs/EDITING_CODES.md).
