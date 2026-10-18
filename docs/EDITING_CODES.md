# Edit built-in codes / Editar códigos integrados

Only repository assets configure the v0.2.0 remote. The app has no import,
export, protocol selector or code editor. Legacy imported profiles are ignored.
Somente os arquivos do repositório configuram o controle v0.2.0. Não há
importação, exportação, seletor de protocolo nem editor na interface. Perfis
importados em versões antigas são ignorados.

## State table / Tabela de estados

`app/src/main/assets/ir/transcold-candidate.json`

- `frequencyHz`: carrier / portadora.
- `timingsUs`: positive microsecond durations / durações positivas em microssegundos.
- `commands.powerOff`, `commands.swing`: 24-bit hexadecimal commands / comandos hexadecimais de 24 bits.
- `states.cool`, `states.auto`, `states.fan`: temperature keys 18–30, each with
  four codes ordered **auto, low, medium, high** fan / temperaturas de 18–30,
  cada uma com quatro códigos: ventilação **auto, baixa, média, alta**.

The encoder sends three bytes MSB-first, each followed by its complement.
For a different encoding, use raw per-button overrides or add a tested encoder.
O codificador envia três bytes MSB-first, cada um seguido de seu complemento.
Para outro protocolo, use sinais brutos por botão ou um codificador validado.

## Per-button overrides / Substituições por botão

`app/src/main/assets/ir/original-overrides.json`

Use the format in [PROFILES.md](PROFILES.md). A bundled override takes priority
over the state table for that button. No user action is required.
Use o formato de [PROFILES.md](PROFILES.md). Um sinal integrado tem prioridade
sobre a tabela para esse botão. O usuário não precisa fazer configuração.

Change the JSON, bump versionCode/versionName and publish the new APK. The user
only installs the update. Changes are validated by unit tests and the CI build.
Altere o JSON, incremente versionCode/versionName e publique outro APK. O usuário
apenas instala a atualização. Os testes e o CI validam os arquivos.

## Reference codes / Códigos de referência

| Function / Função | Hex |
| --- | --- |
| Cool 24 °C, auto fan / Resfriar 24 °C, ventilação automática | `EF6154` |
| Off / Desligar | `EF7954` |
| Cool 25 °C / Resfriar 25 °C | `EF6E54` |
| Cool 23 °C / Resfriar 23 °C | `EF6954` |
| Low fan, cool 24 °C / Ventilação baixa, 24 °C | `E96154` |
| Medium fan, cool 24 °C / Ventilação média, 24 °C | `ED6154` |
| High fan, cool 24 °C / Ventilação alta, 24 °C | `EB6154` |
| Auto 24 °C / Automático 24 °C | `E6E154` |
| Fan-only / Somente ventilação | `EFCF54` |
| Swing toggle / Alternar oscilação | `E76154` |

These are documented Transcold M1-F-NO-6 signals, not confirmed Hent signals.
Lamp, Strong, Eco, Sleep and Display still need original codes. Their buttons
stay disabled until populated; they never simulate sending.
São sinais documentados para Transcold M1-F-NO-6, ainda não confirmados para Hent.
Luz, Strong, Eco, Sleep e Display continuam aguardando os códigos originais.
Esses botões ficam desabilitados e nunca simulam um envio.
