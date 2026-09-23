# AleJoias Vendas — Instruções para Codex

## Base oficial estável

A base oficial do projeto é **AleJoias Vendas SQLite Sync v2**.

Esta versão foi validada em testes reais pelo usuário:
- compilação no Android Studio OK;
- geração do APK OK;
- instalação no telefone OK;
- importação dos dados existentes da planilha para SQLite OK;
- criação de novo card usando SQLite OK;
- sincronização SQLite → Google Planilhas OK;
- cobrança via WhatsApp OK;
- data de pagamento corrigida e funcionando.

## Regra principal

Toda alteração futura deve ser **pontual** e feita sobre a versão SQLite Sync v2.

Antes de alterar:
1. Ler a implementação existente.
2. Ler `docs/INTERFACE_VISUAL.md` quando a alteração envolver interface.
3. Identificar o menor conjunto de arquivos necessário.
4. Preservar todas as funcionalidades existentes.
5. Evitar refatorações amplas sem necessidade.
6. Não substituir componentes funcionais por versões simplificadas.
7. Manter compatibilidade com SQLite e Google Planilhas.
8. Não alterar a estrutura dos IDs sem necessidade.

## Arquitetura

O banco principal é **SQLite local**, implementado diretamente com
`SQLiteOpenHelper` em `LocalDatabase.kt` (não utiliza a biblioteca Room).

A Google Planilha + Apps Script permanece como backend secundário para sincronização, compatibilidade e possível uso futuro.

Fluxo principal:

Android UI
→ SQLite (`SQLiteOpenHelper`)
→ Repository / regras locais
→ Sincronização opcional
→ Google Apps Script
→ Google Planilhas

O aplicativo deve continuar funcional sem internet para as operações locais.

## IDs compartilhados

Preservar os IDs já utilizados na planilha e no SQLite:

- `id_cliente`
- `id_venda`
- `id_pagamento`
- `id_venda_pai`

Eles são fundamentais para sincronização e prevenção de duplicidades.

## Sincronização

Preservar suporte para:
- Planilha → SQLite
- SQLite → Planilha
- sincronização/mesclagem
- exportação CSV
- backup local SQLite

Durante alterações no sync, priorizar prevenção de perda ou duplicação de dados.

## Funcionalidades que não podem regredir

- cadastro de vendas;
- edição de vendas/cards;
- parcelamento;
- pagamentos parciais;
- correção de pagamento;
- exclusão;
- cobrança por WhatsApp;
- filtros e pesquisa;
- resumo financeiro;
- resumo mensal;
- resumo por cliente;
- dashboard financeiro;
- busca por cliente, descrição, datas e valor integrada à lista de vendas;
- notificações;
- geração de PDF;
- exportação CSV;
- backup SQLite;
- importação da planilha;
- sincronização com Google Planilhas.

## Política de mudanças

Ao receber um pedido:
- classificar mentalmente como bug, melhoria ou nova feature;
- corrigir primeiro o comportamento existente quando houver bug;
- implementar a menor mudança segura;
- conferir impactos em vendas, pagamentos, parcelamento, relatórios e sync;
- manter o schema compatível, salvo autorização explícita.

Não alterar o backend Apps Script desnecessariamente.
