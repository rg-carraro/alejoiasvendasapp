# AleJoias Vendas

Aplicativo Android para controle de vendas da AleJoias. A base oficial deste
repositório é a versão **2.0 SQLite Sync**, validada em uso real.

## Interface sincronizada

A interface atual usa a paleta verde/dourado da cópia GitHub, com etiquetas de
status nos cards e seleção de mês no dashboard financeiro. O armazenamento
permanece em SQLite e a sincronização com Google Planilhas é opcional.
Consulte [docs/COMPARACAO_COPIAS.md](docs/COMPARACAO_COPIAS.md) para o histórico
da conciliação entre as duas pastas.

## Fotos da venda

Uma nova venda pode incluir fotos do produto, mas elas são opcionais. É possível escolher várias
imagens da galeria ou tirar fotos com a câmera. A primeira foto aparece no
card da venda, na pesquisa da lista e no histórico do cliente; todas aparecem nos
detalhes de cada parcela, no PDF individual do card e nos relatórios PDF.
As fotos ficam no SQLite local, inclusive no backup do banco. A
sincronização com Google Planilhas continua enviando apenas os dados de vendas,
clientes e pagamentos; fotos não são transferidas entre aparelhos.

## Navegação

O menu principal reúne dashboard, resumo geral, relatórios por período/mês/cliente
e exportações em **Financeiro / Relatórios**. A pesquisa da lista de vendas
também encontra cliente, descrição, datas e valor; a tela separada de busca
global foi removida. **Dados e sincronização** fica no botão de opções
`⋮` do menu principal. Os indicadores financeiros do dashboard e dos
resumos geral, mensal e por período abrem a lista de cards correspondente.

## Arquitetura atual

- SQLite local (`alejoias_vendas.db`) como fonte operacional principal;
- acesso ao banco com `SQLiteOpenHelper` em `LocalDatabase.kt`;
- funcionamento local sem internet;
- sincronização opcional com Google Apps Script e Google Planilhas;
- IDs compartilhados preservados: `id_cliente`, `id_venda`, `id_pagamento` e
  `id_venda_pai`.

Detalhes: [README_SQLITE_SYNC.md](README_SQLITE_SYNC.md) e
[docs/ARQUITETURA.md](docs/ARQUITETURA.md).

## Requisitos de desenvolvimento

- Android Studio com JDK 17 ou superior (o projeto está validado com o JBR 21);
- Android SDK 35;
- acesso à internet no primeiro build para baixar o Gradle e as dependências.

Versões principais:

- Gradle Wrapper 8.7;
- Android Gradle Plugin 8.5.2;
- Kotlin 1.9.24;
- `minSdk 23`, `targetSdk 35` e `compileSdk 35`.

## Compilação

No Android Studio, abra a raiz do projeto e use **Build > Build APK(s)**.

Pelo terminal no Windows, com um JDK configurado:

```powershell
.\gradlew.bat assembleDebug
```

O APK debug será criado em `app/build/outputs/apk/debug/app-debug.apk`.

O AGP 8.5.2 pode emitir um aviso por ter sido testado oficialmente até o
`compileSdk 34`. A configuração com SDK 35 compila nesta base estável; versões
não devem ser atualizadas sem um teste completo de regressão.

## Configuração do backend

A URL do Apps Script está definida em
`app/src/main/java/com/example/controlevendas/RetrofitClient.kt`. Se uma nova
implantação gerar outra URL, atualize `BASE_URL` e faça o checklist de regressão.

O backend de referência está em `AppsScript_Codigo.gs`. Ele mantém as ações
anteriores e inclui `sync_export` e `sync_upload`.

## Segurança e versionamento

- Não versione `local.properties`, builds, caches, APKs, chaves ou arquivos de
  assinatura; o `.gitignore` da raiz já cobre esses itens.
- Nunca grave senhas, tokens ou chaves privadas no código.
- A URL publicada do Apps Script identifica o endpoint, mas não é um mecanismo
  de autenticação. Restrinja o acesso na implantação do Google e prefira um
  repositório remoto privado.
- Antes de operações destrutivas de sincronização, mantenha backup da planilha e
  do banco SQLite.

## Política de alterações

Leia [AGENTS.md](AGENTS.md) antes de modificar o projeto. Toda mudança deve ser
pontual, preservar o schema e os IDs e ser validada conforme
[docs/CHECKLIST_TESTES.md](docs/CHECKLIST_TESTES.md).
