# AleJoias Vendas — SQLite + Sincronização com Google Planilhas

Esta versão mantém a estrutura de IDs existente na planilha e passa a usar o SQLite local como base principal do aplicativo.

## Arquitetura

- Uso diário: Android -> SQLite local (`alejoias_vendas.db`)
- Sincronização opcional: SQLite <-> Google Apps Script <-> Google Planilhas
- A planilha continua usando as abas `CLIENTES`, `VENDAS` e `PAGAMENTOS` e os mesmos IDs.

## O que mudou

- O app abre e trabalha diretamente no SQLite, mesmo sem internet.
- Nova venda, edição, pagamento e exclusão são gravados primeiro no SQLite.
- Corrigido o menu do card: a opção WhatsApp não chama mais exclusão.
- Corrigido o campo `data_pagamento` enviado ao backend.
- Removido o paliativo de data de pagamento em `SharedPreferences`.
- "Corrigir Valor Pago" agora aceita aumentar ou reduzir o valor, ajustando o pagamento local.
- Tela `Dados / Sync` com:
  - Importar Planilha -> SQLite
  - Enviar SQLite -> Planilha
  - Sincronizar / Mesclar
  - Exportar CSV
  - Backup do banco SQLite
  - Compartilhar backup SQLite

## Regra de sincronização

### Importar Planilha -> SQLite
Substitui toda a base local pelos dados atuais da planilha. Use quando a planilha for a fonte que deve prevalecer.

### Enviar SQLite -> Planilha
Substitui o conteúdo das três abas da planilha pela cópia atual do SQLite, preservando os IDs.

### Sincronizar / Mesclar
1. Baixa clientes, vendas e pagamentos da planilha.
2. Mescla por ID no SQLite.
3. Alterações locais pendentes vencem conflitos com o mesmo ID.
4. Envia a base mesclada de volta para a planilha.

Observação: como a planilha antiga não possui `updated_at` nem tombstones de exclusão, uma exclusão feita somente na planilha não pode ser detectada como conflito durante a mesclagem. Para fazer a planilha prevalecer integralmente, use `Importar Planilha -> SQLite`.

## IMPORTANTE — atualizar o Apps Script

Antes de testar importação/sincronização, abra o Apps Script ligado à planilha, substitua o código pelo arquivo `AppsScript_Codigo.gs` deste projeto e publique uma nova versão da implantação Web App.

A URL atual continua definida em:

`app/src/main/java/com/example/controlevendas/RetrofitClient.kt`

Se a implantação gerar uma URL diferente, atualize `BASE_URL` nesse arquivo.

O Apps Script desta versão adiciona:

- `GET action=sync_export`
- `POST action=sync_upload`

O endpoint antigo `relatorio` e as ações antigas foram mantidos para compatibilidade.

## Primeiro teste recomendado

1. Faça uma cópia de segurança da planilha atual.
2. Atualize e redeploy o Apps Script.
3. Abra o projeto no Android Studio.
4. Aguarde o Gradle Sync.
5. Execute no aparelho.
6. No primeiro uso, aceite `Importar da Planilha` para preencher o SQLite.
7. Confira quantidade de cards e totais.
8. Cadastre uma venda sem internet e confirme que aparece imediatamente.
9. Conecte a internet e use `Dados / Sync -> Enviar SQLite -> Planilha`.
10. Confira os IDs e valores na planilha.

## Build

O projeto usa:

- Android Gradle Plugin 8.5.2
- Kotlin 1.9.24
- Gradle Wrapper 8.7
- compileSdk 35
- minSdk 23

No Android Studio: `File -> Open` na pasta do projeto e depois `Build -> Build APK(s)`.

## Banco local

Arquivo: `alejoias_vendas.db`

Tabelas principais (espelham a planilha):

- `CLIENTES(id_cliente, nome)`
- `VENDAS(id_venda, id_cliente, descricao, data_venda, data_vencimento, valor_total, parcela_atual, parcelas, id_venda_pai)`
- `PAGAMENTOS(id_pagamento, id_venda, data_pagamento, valor_pago)`

Existem também duas tabelas internas (`SYNC_DIRTY` e `SYNC_DELETIONS`) usadas somente para controle da sincronização.
