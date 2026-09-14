# Banco de dados e IDs

## SQLite

O banco local é criado pelo aplicativo no armazenamento privado do Android. O arquivo usado na versão de referência é:

`alejoias_vendas.db`

Caminho típico no aparelho:

`/data/user/0/<package>/databases/alejoias_vendas.db`

ou:

`/data/data/<package>/databases/alejoias_vendas.db`

## Estrutura lógica preservada da planilha

### CLIENTES
- `id_cliente`
- `nome`

### VENDAS
- `id_venda`
- `id_cliente`
- `descricao`
- `data_venda`
- `data_vencimento`
- `valor_total`
- `parcela_atual`
- `parcelas`
- `id_venda_pai`

### PAGAMENTOS
- `id_pagamento`
- `id_venda`
- `data_pagamento`
- `valor_pago`

### FOTOS_VENDA (somente SQLite local)
- `id_foto`: identificador próprio da foto;
- `id_venda_pai`: associa a imagem a todas as parcelas da venda;
- `imagem`: JPEG comprimido em BLOB.

A migração de `DB_VERSION 1` para `2` cria somente esta tabela e seu índice,
sem alterar CLIENTES, VENDAS, PAGAMENTOS ou os IDs compartilhados. O backup
SQLite contém as fotos; a planilha não armazena imagens.

## Regra de compatibilidade

Os mesmos identificadores devem ser preservados no SQLite e na planilha para permitir sincronização e evitar duplicidades.

Mudanças de schema exigem atenção a:
- incremento de `DB_VERSION` e implementação segura de `onUpgrade` no `SQLiteOpenHelper`;
- importação de dados existentes;
- Apps Script;
- sincronização;
- relatórios;
- PDFs e CSVs.
