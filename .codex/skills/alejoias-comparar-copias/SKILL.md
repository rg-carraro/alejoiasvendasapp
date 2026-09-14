---
name: alejoias-comparar-copias
description: Comparar cópias do app AleJoias Vendas e reconciliar documentação ou mudanças pontuais sem confundir a base SQLite Sync v2 com versões históricas.
---

# Comparar cópias do AleJoias Vendas

Use esta skill quando o usuário pedir para confrontar outra pasta do aplicativo
com este repositório ou documentar diferenças entre elas.

Leia `AGENTS.md` e `docs/COMPARACAO_COPIAS.md` deste repositório. Confirme a
situação atual de cada cópia com `git status`, inventário de arquivos e comparação
de conteúdo; o documento de comparação registra uma fotografia, não substitui
nova verificação. Ignore artefatos gerados (`build`, `.gradle`, `.idea`) ao
comparar código, mas informe mudanças locais relevantes sem sobrescrevê-las.

Para cada divergência, indique em qual cópia ela está, se é documentação,
interface ou regra de negócio, e qual impacto teria uma integração. Confira a
implementação real antes de descrever uma capacidade como existente. O banco
local usa `SQLiteOpenHelper`, não Room; preserve os IDs compartilhados e a
sincronização com Google Planilhas.

Atualize a documentação quando houver fato novo. Porte código entre cópias
somente quando o pedido incluir essa mudança; mantenha a alteração pontual e
valide as funções afetadas conforme `docs/CHECKLIST_TESTES.md`.

Quando o usuário pedir sincronização, confira o commit e o estado de ambas as cópias antes de avançar a cópia do Android Studio. Consulte `docs/VERSIONAMENTO_GIT.md` para o fluxo com o GitHub; não trate o histórico de `docs/COMPARACAO_COPIAS.md` como estado atual.
