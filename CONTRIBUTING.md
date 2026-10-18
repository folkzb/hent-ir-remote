# Contributing / Como contribuir

Issues and pull requests in English or Portuguese are welcome.

Report device label/model, remote model, which of the 13 buttons was tested,
carrier frequency and raw pulse timings if available. Exporting the app's
compatibility report does not publish it: review it and attach it manually.
Do not include private information or claim compatibility based on a supplier
name. Do not substitute a guessed “turbo”, “eco” or “sleep” state for an
original command.

Please add source attribution and recorded test vectors for new protocols.
Run `python3 tools/check_resources.py` and
`./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease`.
Changes must preserve PT-BR/English parity and GPL-3.0-or-later compatibility.

São bem-vindos relatos dos testes e códigos capturados do controle original.
Envie modelo, botão testado, frequência e pulsos brutos quando disponíveis.
Revise o relatório antes de anexá-lo por conta própria; o app nunca o envia.
Não invente códigos nem declare compatibilidade pela semelhança visual.
Mantenha as traduções em português e inglês e execute as verificações acima.
