# Validation / Validação

Date / Data: 2026-10-18

- Local Android debug and unsigned release builds: **passed**.
- Unit tests: **34 passed**, zero failures or errors.
- Android lint: **zero issues**.
- PT-BR/English resource parity: **33 entries**, matching format arguments.
- Bundled editable JSON codes: present inside the APK; all 156 state-table entries match the reference encoder.
- CI APKs use a runner debug signing key. Installing over a differently signed build may require uninstalling the previous app first; saved settings are reset.
- Original remote layout: **13 command identifiers** from Hent's published diagram.
- Debug APK signature: verified, v1 and v2 schemes.
- APK metadata: Android 6+ (minSdk 23), targetSdk 35; both English and PT-BR labels.
- Only requested Android permission: `android.permission.TRANSMIT_IR`.
- v0.2.0 local debug/release builds and Android lint: **passed**.
- v0.2.0 adds four control-state tests for power toggle, bounded temperature, fan/mode changes and swing behavior.
- GitHub CI and publication status: see the repository Actions and Releases pages.
- **No physical phone/AC test performed. No original Hent codes verified.**

Local command:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease --no-daemon --no-configuration-cache
python3 tools/check_resources.py
```

The build was performed with a locally installed Android SDK 35, build-tools
35.0.0, Java 17 and Gradle 8.9. Generated outputs were cleared before the final
build. Configuration caching is disabled in the source project.

A build proves that the APK can be compiled, not that the experimental protocol
matches the device. Hardware verification and original code capture are still
required for a fully functional clone.

A compilação comprova que o APK foi gerado, não que o protocolo corresponde ao
aparelho. Ainda faltam teste físico e os códigos originais para uma cópia completa.
