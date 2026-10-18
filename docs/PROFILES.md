# Raw profiles / Perfis brutos

A profile maps an original remote button to an infrared carrier and pulse
sequence. It does not enable reception or learning on the phone.
Um perfil associa cada botão a uma frequência e sequência de pulsos. Ele não
ativa recepção ou aprendizado pelo celular.

```json
{
  "schemaVersion": 1,
  "name": "My captured Hent remote",
  "source": "Original remote capture; describe model and receiver here",
  "commands": {}
}
```

The shipped template is deliberately empty: **no original Hent codes have
been verified**. Do not copy placeholder pulses and claim they are working.
O modelo é intencionalmente vazio: **nenhum código original Hent foi validado**.

For each actual captured command, add an object to `commands` with:

| Field / Campo | Meaning / Significado |
| --- | --- |
| `frequencyHz` | Carrier in Hz, e.g. 38000 only if measured or documented / portadora em Hz |
| `patternUs` | Array of positive integer durations in microseconds, alternating mark, space, mark, space… / duração de pulsos e pausas, começando por pulso |

The trailing gap, if included, is another space duration. Values are not
frequency samples or decoded hex values: they are the complete raw waveform.
A pausa final pode fazer parte do vetor. Não são amostras de frequência nem
apenas um código hexadecimal: é a sequência bruta completa de pulsos e pausas.

Exact supported keys / Identificadores dos 13 botões:

| Key | Original label | Português |
| --- | --- | --- |
| `light` | Lamp | Luz LED |
| `power` | Power | Ligar/desligar |
| `temp_up` | Temp + | Temperatura + |
| `temp_down` | Temp − | Temperatura − |
| `fan_up` | Speed + | Ventilação + |
| `fan_down` | Speed − | Ventilação − |
| `auto` | Auto | Automático |
| `strong` | Strong | Resfriamento forte |
| `eco` | Eco | Resfriamento padrão, conforme Hent |
| `sleep` | Sleep | Dormir |
| `swing` | Swing flap | Oscilar |
| `fresh_air` | Fresh air | Ventilar sem resfriar |
| `display` | Display | Ligar/desligar visor |

Validation: UTF-8 JSON, maximum 1 MiB, schema version 1, nonempty name up to
100 characters, optional source up to 1000 characters, known button keys,
carrier 10000–100000 Hz and 2–4096 positive integer pulse durations per command.
Each complete signal must be shorter than 2 seconds (Android API limit).
Unknown buttons, fractional values, zero/negative pulses and excessively long
patterns are rejected before replacing the current profile. Import asks before
replacement and does not transmit anything. The phone checks its own supported
carrier range before transmission.

Validação: JSON UTF-8 até 1 MiB, versão 1, nome não vazio até 100 caracteres,
fonte opcional até 1000 caracteres, identificadores conhecidos, portadora entre
10000–100000 Hz e 2–4096 durações inteiras positivas por comando. Cada sinal
completo deve durar menos de 2 segundos. Valores fracionários e inválidos são
recusados antes de substituir o perfil. A importação pede confirmação e não
envia sinais. A frequência também é conferida no hardware do celular.

Missing keys are allowed. A complete mapping includes all 13 keys; the app
reports mapping count, not verified compatibility. A recorded test result is
user feedback, not proof that every button works. Exporting a report includes
the profile and last send/error details; it never uploads the file.

Um perfil pode conter apenas alguns botões. A contagem de 13 mapeados não
significa 13 validados. O resultado registrado pelo usuário não comprova todos
os botões. O relatório exportado contém perfil e detalhes do último envio/erro;
o app nunca faz upload desse arquivo.
