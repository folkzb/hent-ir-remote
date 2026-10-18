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

## Repository-only configuration / Configuração apenas pelo repositório

Edit `app/src/main/assets/ir/original-overrides.json` and rebuild the APK.
There is no file picker, import/export or editing interface in the app.
Edite `app/src/main/assets/ir/original-overrides.json` e recompile o APK.
O aplicativo não oferece seletor de arquivos, importação/exportação ou edição.

Validation: schema version 1, nonempty name up to 100 characters, source up to
1000 characters, known button keys, carrier 10000–100000 Hz and 2–4096 positive
integer pulse durations per command. Each signal must last less than 2 seconds.
The phone checks its supported carrier range before transmission.
Validação: versão 1, nome não vazio até 100 caracteres, fonte até 1000 caracteres,
identificadores conhecidos, portadora 10000–100000 Hz e 2–4096 durações inteiras
positivas por comando. Cada sinal deve durar menos de 2 segundos.
O celular confere a portadora antes de enviar.

Missing keys are allowed. A complete mapping has all 13 keys, but mapping count
does not prove hardware compatibility. Overrides take priority over the bundled
state-table generator. User-imported data from older releases is ignored.
É permitido preencher apenas alguns botões. Ter 13 códigos mapeados não comprova
compatibilidade física. As substituições têm prioridade sobre o gerador da
tabela integrada. Dados importados pelo usuário em versões antigas são ignorados.
