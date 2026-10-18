# Contributing / Como contribuir

Issues and pull requests in English or Portuguese are welcome. Report the unit
model, remote model, buttons tested, carrier and captured pulse timings. Do not
include private data or claim compatibility from a supplier name alone.

Edit bundled JSON assets, never add user-facing code configuration. Preserve the
simple remote UI and English/PT-BR localization. New protocols need attribution
and recorded reference signals. Run:

```sh
python3 tools/check_resources.py
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Relatos em português e inglês são bem-vindos. Informe modelos, botões testados,
frequência e pulsos capturados. Altere os JSON integrados, sem adicionar edição
de códigos na interface. Preserve o visual simples e as traduções. Novos
protocolos precisam de créditos, sinais de referência e testes. Não invente
comandos para sugerir compatibilidade completa. Licença GPL-3.0-ou-posterior.
