# Hent Controle IR

Português (Brasil) · [English](README.md)

Controle infravermelho Android de código aberto para investigar o ar de teto
Hent 12V/24V com dois ventiladores. **Experimental: compatibilidade ainda não confirmada.**

## Situação real

Os **13 botões físicos** da foto oficial do controle estão no app. Isso **ainda
não é uma cópia totalmente funcional**: os códigos originais não foram obtidos.
O protocolo opcional **Transcold M1-F-NO-6** permite testar oito funções, mas
não foi identificado como protocolo desse Hent. O nome do fornecedor não
comprova compatibilidade.

| Botão original | Candidato integrado | Importa código original |
| --- | --- | --- |
| Lamp / Luz LED | Sem candidato | Sim |
| Ligar / desligar | Comandos separados de ligar e desligar | Sim |
| Temperatura + / − | Faixa de 18–30 °C | Sim, cada botão |
| Speed / Ventilação + / − | Auto / baixa / média / alta | Sim, cada botão |
| Auto | Automático | Sim |
| Strong / Forte | Sem candidato | Sim |
| Eco / Padrão, conforme diagrama Hent | Sem candidato | Sim |
| Sleep / Dormir | Sem candidato | Sim |
| Swing flap / Oscilação | Alternar oscilação | Sim |
| Fresh air / Ventilar sem resfriar | Somente ventilação | Sim |
| Display / Visor | Sem candidato | Sim |

Os candidatos também precisam ser testados. Botões sem código não transmitem,
e não são substituídos por funções inventadas. Códigos importados têm prioridade.
Com o candidato desativado, apenas os sinais importados podem ser enviados.

## Instalar

1. Baixe o APK em **Releases** ou o artefato `hent-ir-remote-debug-apk` de uma
   execução bem-sucedida em **Actions → Build**.
2. Instale em Android 6 ou superior com emissor IR compatível com a API do
   Android, como um Redmi Note 13 Pro compatível. A presença do emissor é
   verificada no app. Emuladores não comprovam funcionamento do infravermelho.
3. Selecione o idioma pelo sistema ou em **Idioma / Language** no app.
   O mesmo APK contém português e inglês.
4. Importe os sinais originais, se disponíveis. O candidato Transcold
   já vem ativado para teste. Não é necessário importar um perfil para testar
   esses códigos. Leia o aviso de compatibilidade.
5. Aponte o topo do celular para o receptor e envie um comando por vez.
   “Enviado” significa que o Android aceitou o envio, sem confirmar recepção.
6. Salve os ajustes, registre o resultado e, se desejar, exporte o relatório.
   Nada é enviado automaticamente.

A temperatura e ventilação mostradas são ajustes **locais do candidato**.
Não há leitura do estado do ar. Ligar/desligar oferece duas escolhas explícitas.
Temperatura +/− seleciona resfriamento; Fresh air seleciona ventilação.
Botões brutos importados não atualizam os ajustes locais, pois o app não conhece
seu significado. Não há busca automática disparando centenas de códigos.

## Completar os 13 códigos originais

Consulte o [formato dos perfis](docs/PROFILES.md) e o
[modelo vazio](profiles/hent-original-template.json).
Um perfil JSON pode preencher todos os botões assim que os sinais forem obtidos.
A API infravermelha padrão do celular só transmite. Para capturar o controle
original, é necessário receptor externo ou uma captura confiável já publicada.
Filmar o LED piscando não fornece os códigos completos.

## Build, pipeline e releases

Requisitos: JDK 17, Android SDK 35 e build-tools 35.0.0.

```sh
python3 tools/check_resources.py
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Configure `ANDROID_HOME` ou `sdk.dir` em `local.properties`, que não vai para o git.
O APK instalável de teste fica em `app/build/outputs/apk/debug/app-debug.apk`.
A compilação release local é **não assinada** sem configurar a assinatura.

A pipeline roda nos pushes para `main`, pull requests e manualmente, executando
testes, lint, verificação de traduções e builds. Ela disponibiliza o APK de
teste e os relatórios em Actions.

Um build bem-sucedido de um push para `main` publica automaticamente a versão
experimental atual, com APK assinado para depuração, se ela ainda não existir.

Para uma publicação manual, envie uma tag igual ao `versionName` (ex.: `v0.1.1`) ou execute
manualmente **Release** com essa tag. O workflow repete as verificações e cria
um **pré-lançamento** com notas nos dois idiomas e checksums SHA-256.

Para assinatura própria, configure os secrets `RELEASE_KEYSTORE_BASE64`,
`RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS` e `RELEASE_KEY_PASSWORD`.
Sem eles, a release publica um APK experimental **assinado para depuração**,
com nome explícito. Builds diferentes podem usar chaves de depuração diferentes;
pode ser necessário desinstalar o APK anterior, apagando dados locais. Exporte
seu perfil antes. Não coloque chaves privadas no repositório.
Incremente `versionCode` para novas atualizações. Releases existentes não são sobrescritas.

## Código aberto e privacidade

Licença **GPL-3.0-ou-posterior**. Qualquer pessoa pode usar, estudar, modificar
e distribuir respeitando a licença. [Créditos de terceiros](THIRD_PARTY_NOTICES.md).
Sem internet, contas, anúncios ou rastreamento no aplicativo. Dados permanecem
no celular até você exportar e compartilhar por conta própria.
Sem vínculo oficial com Hent, Xiaomi ou Transcold.

Veja [as fontes da pesquisa](README.md#research-references) e
[como contribuir](CONTRIBUTING.md). A referência visual é a foto oficial Hent;
a carcaça semelhante do Alibaba não confirma a placa nem o protocolo interno.

## Alterar os códigos integrados

Na v0.1.1, os candidatos já vêm ativados. A tabela de estados, frequência e
pulsos fica em `app/src/main/assets/ir/transcold-candidate.json`.
Correções brutas por botão ficam em `app/src/main/assets/ir/original-overrides.json`.
O usuário não precisa importar nada. Veja [como editar](docs/EDITING_CODES.md).
