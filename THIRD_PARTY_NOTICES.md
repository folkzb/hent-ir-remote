# Third-party notices / Créditos de terceiros

The Java Transcold encoder is adapted from IRremoteESP8266:
https://github.com/crankyoldgit/IRremoteESP8266

- `src/ir_Transcold.cpp` (upstream blob `0de3513684761e6ca8b9e5d38123332a99f0f570`)
- `src/ir_Transcold.h`
- Golden examples: `test/ir_Transcold_test.cpp`
- Copyright 2020 Chandrashekar Shetty (iamDshetty)
- Copyright 2020 crankyoldgit
- Copyright 2021 siriuslzx
- Upstream license: GNU LGPL version 2.1 or later.

For the adapted Java encoder, the option in LGPL 2.1 section 3 is exercised:
this adaptation is distributed under GNU GPL version 3 or later, matching
this application's license. Preserve the upstream attribution and this notice.
The upstream LGPL text is included in `docs/LGPL-2.1.txt` for provenance;
it does not replace this application's GPL-3.0-or-later license.

The source files were adapted to Java/Android: an immutable 24-bit signal
encoder, bounded pulse arrays, and a restricted cooling/auto/fan-only API.
Upstream model compatibility is not evidence of Hent rooftop compatibility.

The Gradle Wrapper is from Gradle 8.9.0, licensed under Apache License 2.0.
See `docs/Apache-2.0.txt`. Wrapper source:
https://github.com/gradle/gradle/tree/v8.9.0/gradle/wrapper

JUnit 4 (EPL-1.0) and JSON-java (public domain) are test-only dependencies.
No upstream product photography is distributed in this repository.
Hent, Xiaomi and Transcold names identify compatibility research only.
No endorsement, ownership relationship or verified OEM link is claimed.
