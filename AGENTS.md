# Vendas Simples — branch comercial

## Escopo autorizado

Esta branch é a linha comercial genérica criada em 24/09/2026 a partir da tag
`alejoias-v2-marco-2026-09-24`. O AleJoias original permanece em `master`.
Nome aprovado: Vendas Simples. Identidade: azul e cinza, sem joalheria.
A remoção de planilha, Apps Script, importação remota e sync foi explicitamente
solicitada pelo usuário nesta branch; as regras históricas de preservação de
sync das skills AleJoias não se aplicam aqui.

## Regras de manutenção

- Ler o código existente e docs/INTERFACE_VISUAL.md antes de alterar UI.
- Fazer alterações pontuais; preservar todas as funcionalidades locais.
- SQLiteOpenHelper é a fonte principal, sem Room nem backend de rede.
- Manter schema v2 e os IDs de cliente, venda, pagamento e venda pai.
- Preservar vendas, parcelamento, pagamentos parciais e correções, exclusão,
  fotos, busca, filtros, relatórios, PDF, CSV, notificações e cobrança WhatsApp.
- Fotos são locais e fazem parte do backup SQLite.
- Não reintroduzir sync, endpoint, marca AleJoias ou vínculo com joalheria.
- Não alterar a aplicação original nem publicar esta branch em master.
- applicationId comercial: com.vendassimples.app; banco: vendas_simples.db.
  Namespace Kotlin legado é interno; não implica compartilhamento de dados.
- Documentar em CHANGELOG_COMERCIAL.md e documentos afetados, compilar com
  gradlew.bat assembleDebug, revisar diff e executar checks aplicáveis.
- Publicar mudanças concluídas em origin/comercial, conforme autorização
  permanente. Não solicitar confirmação rotineira. Informar testes pendentes.

Leia docs/COMERCIAL.md, docs/CHECKLIST_TESTES.md e docs/VERSIONAMENTO_GIT.md.