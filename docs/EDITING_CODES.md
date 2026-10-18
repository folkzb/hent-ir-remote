# Editable built-in IR codes / Códigos IR integrados editáveis

## Português (Brasil)

O APK v0.1.1 já carrega os códigos candidatos ao abrir. Não exige importação,
conta, internet ou configuração do usuário. O candidato está ativado inicialmente;
se o usuário o desativar, a preferência é respeitada nas próximas aberturas.

Os arquivos de manutenção ficam dentro do próprio repositório:

- `app/src/main/assets/ir/transcold-candidate.json`: portadora, tempos de pulso,
  códigos de desligar/oscilação e tabela completa de resfriar/auto/ventilar,
  temperaturas 18–30 °C e quatro níveis de ventilação. Cada estado é um código
  hexadecimal de 24 bits. A ordem dos quatro códigos é auto, baixa, média, alta.
- `app/src/main/assets/ir/original-overrides.json`: códigos brutos por botão.
  É o lugar para corrigir/adicionar qualquer um dos 13 comandos originais,
  inclusive Luz, Strong, Eco, Sleep e Display. Veja `PROFILES.md`.

O aplicativo lê esses arquivos automaticamente. Altere o JSON, incremente
versionName/versionCode e gere um novo APK; o usuário apenas instala a atualização.
Não é necessário alterar a interface ou editar configurações no celular.
Códigos brutos integrados têm prioridade sobre o gerador candidato. Um perfil
que o próprio usuário importou também é preservado e tem prioridade sobre o
arquivo integrado de substituições.

Exemplos de estados candidatos a 38 kHz:

| Função | Código |
| --- | --- |
| Resfriar 24 °C, ventilação automática | `EF6154` |
| Desligar | `EF7954` |
| Resfriar 25 °C, ventilação automática | `EF6E54` |
| Resfriar 23 °C, ventilação automática | `EF6954` |
| Resfriar 24 °C, ventilação baixa | `E96154` |
| Resfriar 24 °C, ventilação média | `ED6154` |
| Resfriar 24 °C, ventilação alta | `EB6154` |
| Modo automático 24 °C | `E6E154` |
| Ventilar sem resfriar | `EFCF54` |
| Alternar oscilação | `E76154` |

São sinais documentados para **outro aparelho, Transcold M1-F-NO-6**. Não foram
identificados como códigos originais Hent. Não existem candidatos comprovados
para os cinco botões restantes; eles continuam sem transmitir até receberem
um código real. Não são inventadas funções para preencher a tela.

A codificação atual usa três bytes, mais o complemento de cada byte, em ordem
MSB-first. Se o protocolo real for diferente, use sinais brutos no arquivo
original-overrides.json ou implemente um novo codificador com captura de referência.
O catálogo é validado antes de qualquer transmissão e testado no CI.

## English

v0.1.1 loads its candidate codes automatically. No user import, account, network
or setup is required. The candidate is enabled by default; an explicit decision
to disable it is preserved. The two JSON assets listed above are the source of
truth for built-in signals. Change them, bump the version and rebuild; users
only install the updated APK.

The candidate catalog contains carrier frequency, pulse timings, power-off and
swing commands, and full state tables for cooling/auto/fan-only, 18–30 °C and
four fan speeds (auto/low/medium/high). The original-overrides profile accepts
raw signals for all 13 buttons. Built-in overrides take precedence over the
candidate generator; user-imported profiles remain preserved and take priority
over the bundled overrides file.

These signals belong to the documented Transcold M1-F-NO-6 protocol, not an
identified Hent match. Five original buttons still have no built-in code. Never
invent a command to imply complete compatibility. For a different encoding,
use per-button raw signals or implement a separately tested encoder.
