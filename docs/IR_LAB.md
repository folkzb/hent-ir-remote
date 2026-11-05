## Português (Brasil)

O **Hent IR Lab** é um APK separado para identificar o protocolo do ar Hent
12 V. Instala ao lado do controle normal. O lote A traz 25 sinais candidatos
de 13 variantes: Coolix, Midea, Gree YAW1F e YBOFB, TCL112, Goodweather,
Kelon, Kelon168, Haier YRW02, Neoclima, Mirage, Trotec e Airton.

1. Com o ar desligado, aponte o emissor do celular para o receptor do ar.
2. Em um item **Ligar**, toque em **Enviar este sinal**, aguarde alguns segundos
   e registre a reação. Se o ar ligar, teste o **Desligar** da mesma família.
   Se não reagir, pule para o próximo **Ligar**. Kelon tem um único sinal
   de alternância.
3. Toque em **Copiar resultados** e cole o texto no chat. Se reagir, diga
   também o que mudou e em qual estado o ar estava antes.

A mensagem de envio só indica atividade do celular. Os sinais
são de protocolos conhecidos, sem confirmação de compatibilidade com o Hent.

## English

**Hent IR Lab** is a standalone APK that installs beside the regular remote.
Batch A contains 25 candidate signals across 13 protocol/model variants.
Start with the AC off and send one ON signal at a time. If it turns on,
try the matching OFF signal; otherwise skip to the next ON. Kelon has a
single toggle signal. Record the observed reaction and copy the results.

## Signal provenance / Origem dos sinais

Generated and decoded locally using [IRremoteESP8266 at
1e2f0f3](https://github.com/crankyoldgit/IRremoteESP8266/tree/1e2f0f3ef0a93cbf2a8ddb2e95130f8f4c584b3f).
The build bundles the generated timings only. IRremoteESP8266 is LGPL-2.1;
see [third-party notices](../THIRD_PARTY_NOTICES.md).
To regenerate with a checkout at that revision:

```sh
IR_SRC=../IRremoteESP8266
g++ -std=gnu++11 -DUNIT_TEST -I"$IR_SRC/src" -I"$IR_SRC/test" \
    tools/generate_lab.cpp "$IR_SRC"/src/*.cpp -o /tmp/hent-generate-lab
/tmp/hent-generate-lab > /tmp/hent-batch-a.json &&
    cp /tmp/hent-batch-a.json app/src/lab/assets/ir/batch-a.json
./gradlew testLabUnitTest lintLab assembleLab
```
