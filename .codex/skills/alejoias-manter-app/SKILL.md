---
name: alejoias-manter-app
description: Implementar e documentar mudanças pontuais no aplicativo Android AleJoias Vendas SQLite Sync v2, preservando vendas, fotos, relatórios e sincronização.
---

# Manter AleJoias Vendas

Use esta skill para pedidos de código, correção, interface ou documentação deste repositório. Leia `AGENTS.md` e inspecione o código envolvido antes de editar. A fonte operacional é o SQLite em `LocalDatabase.kt` (`SQLiteOpenHelper`); o Apps Script e a planilha são secundários. Preserve os IDs `id_cliente`, `id_venda`, `id_pagamento` e `id_venda_pai`, a operação offline e os fluxos de importação, envio e mesclagem.

Consulte `docs/FUNCIONALIDADES.md` para o comportamento atual e `docs/CHECKLIST_TESTES.md` para a regressão aplicável. Na interface, Financeiro reúne o painel e os acessos aos relatórios; seus indicadores e os dos relatórios mensal e por período abrem os cards correspondentes. Dados e sincronização ficam em `⋮`. A busca está integrada à lista de vendas. CSV existe no relatório mensal e em Dados, mas não no relatório por cliente; este mantém histórico e PDF.

Fotos de venda são opcionais, podem vir da galeria ou da câmera e ficam vinculadas localmente por `id_venda_pai`. Elas aparecem nos cards, detalhes e PDFs e entram no backup SQLite; não são enviadas à planilha. Ao mudar venda, parcelamento, PDF ou sync, confira essa distinção em `docs/SINCRONIZACAO.md`.

Faça a menor alteração que resolva o pedido, atualize a documentação afetada e valide com `gradlew.bat assembleDebug` e os testes relevantes. Para comparar ou sincronizar a cópia do Android Studio, use a skill `alejoias-comparar-copias`. Para commits e GitHub, siga `docs/VERSIONAMENTO_GIT.md`; confira o estado de ambas as cópias e do remoto antes de integrar, sem sobrescrever mudanças locais.
