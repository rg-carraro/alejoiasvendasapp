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

## Regra de compatibilidade

Os mesmos identificadores devem ser preservados no SQLite e na planilha para permitir sincronização e evitar duplicidades.

Mudanças de schema exigem atenção a:
- migração Room;
- importação de dados existentes;
- Apps Script;
- sincronização;
- relatórios;
- PDFs e CSVs.
