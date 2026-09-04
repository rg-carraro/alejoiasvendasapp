# Arquitetura

## Estado atual validado

O aplicativo AleJoias Vendas usa SQLite/Room como armazenamento principal local e mantém integração com o backend anterior em Google Apps Script + Google Planilhas.

```text
Android
  |
  v
Room / SQLite (fonte principal)
  |
  +--> telas, vendas, pagamentos, relatórios, PDFs
  |
  v
Sincronização
  |
  v
Google Apps Script
  |
  v
Google Planilhas
```

## Objetivos da arquitetura

- resposta rápida no telefone;
- operação offline;
- independência da latência do Apps Script;
- manter a planilha disponível;
- facilitar exportação/backup;
- preservar IDs entre os dois bancos.

## Regra

Não transformar novamente a planilha em fonte obrigatória para o funcionamento normal do app sem uma decisão explícita do usuário.
