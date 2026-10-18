## Português (Brasil)

Controle Hent v0.2.2 — correção de fechamento ao abrir.

- Corrigido acesso à janela antes de sua criação durante a ativação da tela inteira. A falha foi reproduzida no Android 14 e 15.
- A tela inteira é ativada depois de criar o conteúdo; modo escuro e controles foram mantidos.
- Adicionado teste de abertura, foco e retorno do segundo plano no Android 9, 14 e 15, com Robolectric.
- Validação local: 37 testes aprovados, lint e APKs debug/release compilados.

Os sinais candidatos ainda precisam de validação física no Hent. Os cinco
botões sem códigos conhecidos permanecem desabilitados.

## English

Hent Remote v0.2.2 — fix for crash on launch.

- Fixed access to the window before its decor was created when enabling full screen. The crash was reproduced on Android 14 and 15.
- Full screen is enabled after creating the content; dark mode and controls are retained.
- Added Activity launch, focus and background/resume regression tests on Android 9, 14 and 15 using Robolectric.
- Local validation: 37 tests passed, lint passed and debug/release APKs built.

The candidate signals still require physical Hent testing. Five buttons without
known codes remain disabled.
