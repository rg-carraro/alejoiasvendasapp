# Histórico Vendas Simples

## 24/09/2026 — Estratégia comercial documentada

- Consolidados nome, identidade, escopo local e separação das branches em
  docs/ESTRATEGIA_COMERCIAL.md.
- Registrados estado da entrega, limitações do backup e validações pendentes.
- Próximos passos apresentados como propostas; preço, distribuição e lançamento
  permanecem sem definição. Nenhuma mudança no aplicativo neste registro.
- Validação documental: revisão de conteúdo, links locais e git diff --check.

## 24/09/2026 — 1.0-comercial

- Branch comercial derivada da tag alejoias-v2-marco-2026-09-24.
- Nome aprovado Vendas Simples; identidade azul/cinza e ícone de comprovante.
- Aplicação com identificador com.vendassimples.app e banco vendas_simples.db,
  permitindo coexistência com AleJoias sem compartilhar ou migrar seus dados.
- Retirados backend Apps Script, Retrofit, endpoint, permissão INTERNET,
  importação inicial e rotinas/menus de importação, envio e mesclagem de planilha.
- Dados e backup mantém CSV, criação e compartilhamento de cópia SQLite.
- Vendas, parcelas, pagamentos, fotos, busca, financeiro, PDFs, cobrança WhatsApp
  e notificações mantêm o formato e as operações locais herdadas.
- Textos, PDFs, notificações, arquivos e recursos visuais sem marca AleJoias.
- Schema SQLite v2 e IDs preservados; metadados legados de sync permanecem
  internos, sem consumidor remoto. Sem nova funcionalidade de restauração.
- assembleDebug concluído com sucesso. APK inspecionado: applicationId e
  rótulo comerciais, ícone próprio, ausência de permissão INTERNET.
- Comparação com o marco confirmou operações SQLite de criar/editar venda,
  salvar fotos, pagar, corrigir pagamento e excluir sem alterações.
- Conferência visual e regressão funcional no telefone pendentes. Ver checklist.

O histórico anterior pertence à base AleJoias e está em CHANGELOG_SQLITE_SYNC.txt.
