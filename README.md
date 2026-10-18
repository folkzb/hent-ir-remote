# Hent Remote

[Português (Brasil)](README.pt-BR.md) · English

An open-source Android infrared remote with the familiar feel of a home AC
controller: a pale body, green LCD, temperature rocker, one-touch power button
and a small transmission light at the top.

[Download v0.2.0 APK](https://github.com/folkzb/hent-ir-remote/releases/download/v0.2.0/hent-ir-remote-v0.2.0-debug-signed.apk) · [Releases](https://github.com/folkzb/hent-ir-remote/releases) · [Builds](https://github.com/folkzb/hent-ir-remote/actions)

## Using the remote

Install the APK on Android 6+ with a Consumer IR emitter. The app follows your
phone's language (English or Brazilian Portuguese). Android 13+ also provides
per-app language selection in system settings.

- `IR: OK` means the IR emitter was detected; `IR: NOT OK` also appears when
  transmission fails or the requested carrier is unsupported.
- Tap Power to alternate on/off. Temperature, fan, automatic mode, fan-only and
  swing work directly from the main screen.
- The red light turns on during a transmission. There are no “sending” messages,
  protocol selectors, technical notices, imports, exports or code-editing screens.
- Settings are saved automatically after successful sends. The LCD shows the
  state requested by this remote; IR does not read the physical unit's state.
- All 13 original button positions are retained. Buttons with no bundled code
  are dimmed and disabled; they never pretend to send a command.

No account, network permission, ads or analytics. Codes load automatically from
repository assets. User-imported profiles from older app versions are ignored;
only bundled repository files can change signals.

## Maintainer: change codes in the repository

- `app/src/main/assets/ir/transcold-candidate.json`: carrier, pulse timings,
  power-off/swing commands and the complete temperature/fan/mode state table.
- `app/src/main/assets/ir/original-overrides.json`: raw signals for any of the
  13 original buttons; overrides take priority over the state-table encoder.

See [editing built-in codes](docs/EDITING_CODES.md) and the
[raw code format](docs/PROFILES.md). Change JSON, bump `versionCode` and
`versionName`, then publish an updated APK. End users never need to edit files
or configure the protocol.

## Compatibility research

The current code table comes from the documented Transcold **M1-F-NO-6**
protocol. It has **not** been verified with the Hent 12V/24V dual-fan rooftop
unit. A supplier name or similar case is not proof of protocol compatibility.
Eight button functions have candidate signals. Lamp, Strong, Eco, Sleep and
Display still need original codes and remain disabled until supplied in the
repository. A UI redesign does not establish hardware compatibility.

The phone's IR API only transmits. Capturing a different remote requires an
external receiver or a trustworthy existing dump.

## Build and release

JDK 17, Android SDK 35, build-tools 35.0.0, Gradle 8.9, AGP 8.7.3.
Configure `ANDROID_HOME` or the ignored `local.properties` with `sdk.dir`.

```sh
python3 tools/check_resources.py
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Local release builds
are unsigned unless maintainer signing variables are configured.

The Build workflow checks both languages, unit tests, lint and debug/release
builds. Successful pushes to `main` publish the current version as a prerelease
with an installable debug-signed APK and SHA-256 checksum if that version has
not already been published. Existing releases are preserved.

The separate Release workflow supports version tags/manual runs and optional
secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
`RELEASE_KEY_PASSWORD` for a maintainer-signed APK. Without them it publishes a
clearly named debug-signed APK. Different CI runners may use different debug
keys; uninstalling the previous build may be required, clearing saved settings.
Never commit signing keys. Version tags must match `versionName`; increment
`versionCode` for updates.

## License and research sources

**GPL-3.0-or-later.** [Third-party notices](THIRD_PARTY_NOTICES.md).
No affiliation with Hent, Xiaomi or Transcold.

- [Hent rooftop AC, SKU 5284-5](https://hentautomotivos.com.br/ar-condicionado/ar-condicionado-12v-inverter-teto-p)
- [Official remote diagram](https://cdn.dlojavirtual.com/static1/102249/sku/ar-condicionado-ar-condicionado-12v-inverter-teto-p-1753721299939.jpg)
- [User-identified Alibaba product](https://www.alibaba.com/product-detail/Wholesale-12V-24V-AC-Battery-Electric_1601601019022.html)
- [IRremoteESP8266 Transcold source](https://github.com/crankyoldgit/IRremoteESP8266/blob/master/src/ir_Transcold.cpp)
- [Original protocol investigation](https://github.com/crankyoldgit/IRremoteESP8266/issues/1256)
