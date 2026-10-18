# Controle Hent

Português (Brasil) · [English](README.md)

Controle infravermelho Android de código aberto com aparência de controle de
ar residencial: tela inteira em modo escuro, visor verde, ajuste de temperatura, botão central
de energia e uma luz de transmissão no topo.

[Baixar APK v0.2.2](https://github.com/folkzb/hent-ir-remote/releases/download/v0.2.2/hent-ir-remote-v0.2.2-debug-signed.apk) · [Versões](https://github.com/folkzb/hent-ir-remote/releases) · [Builds](https://github.com/folkzb/hent-ir-remote/actions)

## Como usar

Instale em Android 6 ou superior com emissor IR. O app usa o idioma do celular,
com português brasileiro e inglês. No Android 13+, o idioma também pode ser
selecionado nas configurações de idioma por aplicativo do sistema.

- `IR: OK` indica emissor detectado. `IR: NOT OK` também aparece se o envio
  falhar ou a portadora não for suportada.
- Um toque no botão de energia alterna ligar/desligar, sem diálogos.
  Temperatura, ventilação, automático, ventilar e oscilar ficam na tela principal.
- A luz vermelha do topo acende durante o envio. Não há texto de “enviando”,
  avisos técnicos, seletor de protocolo, importação, exportação ou edição de códigos.
- Os ajustes são salvos automaticamente após o envio bem-sucedido.
  O visor representa os ajustes pedidos pelo controle; IR não lê o estado do ar.
- Os 13 botões originais estão presentes. Botões sem código integrado ficam
  discretamente desabilitados e não simulam uma transmissão.

Sem conta, internet, anúncios ou rastreamento. Os códigos vêm no APK. Perfis
importados em versões antigas são ignorados; só os arquivos do repositório
configuram os sinais.

## Alteração dos códigos pelo mantenedor

- `app/src/main/assets/ir/transcold-candidate.json`: frequência, pulsos,
  desligar, oscilação e tabela de temperatura/ventilação/modo.
- `app/src/main/assets/ir/original-overrides.json`: sinais brutos para qualquer
  um dos 13 botões originais, com prioridade sobre o codificador da tabela.

Veja [como editar](docs/EDITING_CODES.md) e o [formato dos sinais](docs/PROFILES.md).
Altere o JSON, incremente `versionCode` e `versionName` e gere outro APK.
O usuário final apenas instala a atualização, sem editar arquivos ou escolher
um protocolo.

## Pesquisa de compatibilidade

A tabela atual usa o protocolo documentado para **Transcold M1-F-NO-6**.
Ainda não houve validação com o Hent 12V/24V de teto com dois ventiladores.
A semelhança do nome do fornecedor ou da carcaça não comprova compatibilidade.
Há sinais candidatos para oito funções. Luz, Strong, Eco, Sleep e Display
continuam sem códigos originais e ficam desabilitados até serem preenchidos
no repositório. A mudança visual não confirma funcionamento no aparelho.

O celular só transmite IR. Capturar outro controle exige receptor externo ou
uma captura confiável já publicada.

## Build e publicação

JDK 17, Android SDK 35, build-tools 35.0.0, Gradle 8.9 e AGP 8.7.3.
Configure `ANDROID_HOME` ou `sdk.dir` em `local.properties`, ignorado pelo git.

```sh
python3 tools/check_resources.py
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

O APK de teste fica em `app/build/outputs/apk/debug/app-debug.apk`.
O build release local é não assinado sem configurar a assinatura.

O workflow Build verifica traduções, testes (incluindo abertura e retomada da tela
no Android 9, 14 e 15), lint e builds. Um push aprovado
em `main` publica a versão atual como pré-lançamento com APK instalável de teste
e SHA-256, caso a versão ainda não exista. Versões publicadas são preservadas.

O workflow Release também aceita tags/manual e assinatura própria com os
secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`
e `RELEASE_KEY_PASSWORD`. Sem eles, publica APK assinado para depuração, com
nome explícito. Runners diferentes podem gerar chaves diferentes; pode ser
necessário desinstalar o APK anterior, apagando os ajustes. Não coloque chaves
privadas no repositório. A tag deve corresponder a `versionName`; incremente
`versionCode` nas atualizações.

## Licença

**GPL-3.0-ou-posterior.** [Créditos de terceiros](THIRD_PARTY_NOTICES.md).
Sem vínculo oficial com Hent, Xiaomi ou Transcold.
[Fontes da pesquisa](README.md#license-and-research-sources).

O controle ocupa a tela inteira em modo escuro. Deslize a partir de uma borda
para mostrar temporariamente as barras do Android. Os recortes físicos da câmera
são respeitados; janelas pequenas podem rolar para manter os botões acessíveis.
