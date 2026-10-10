---
name: alejoias-backup
description: Alterar ou validar backup SQLite do AleJoias, escolha de destino e compartilhamento sem perder fotos, IDs ou metadados de sincronização.
---

# Backup SQLite do AleJoias

Leia [banco e IDs](../../../docs/BANCO_DE_DADOS.md), a seção de backup em
[interface visual](../../../docs/INTERFACE_VISUAL.md) e
[checklist](../../../docs/CHECKLIST_TESTES.md). Inspecione `LocalDatabase.kt`
e as rotinas de backup de `MainActivity.kt` antes de modificar.

Fazer Backup usa `ActivityResultContracts.CreateDocument` para escolher nome
e destino. Cancelar o seletor não gera cópia nem fecha o banco. Após receber
um URI, gere uma cópia consistente, reabra a base operacional inclusive em
caso de erro e grave por `contentResolver`; confirme sucesso somente depois
de fechar o stream. Remova a cópia intermediária desse fluxo ao terminar.
Compartilhar Backup mantém a cópia acessível ao FileProvider e concede leitura
temporária ao destinatário escolhido pelo usuário.

Preserve o formato SQLite AleJoias, IDs, pagamentos, fotos em BLOB e tabelas
internas de sync. Não importar o formato/schema/metadata ou restaurador do
Vendas Simples. Mudança de schema requer migração e validação separadas.
Não apague o banco original por falha de destino e não exiba sucesso de uma
gravação incompleta. Um arquivo parcial no provedor não é backup válido.

Valide salvar/renomear/cancelar/repetir, falha de acesso ou espaço, uso do app
após a cópia e compartilhamento. Com fixture sintética, confira integrity_check,
IDs, totais, fotos e metadados na cópia. Não confunda inspeção do arquivo com
restauração homologada. Provedor remoto tem disponibilidade própria: selecionar
uma pasta não comprova upload. Registre build, testes e limites sem usar dados reais.
