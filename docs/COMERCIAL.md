# Vendas Simples — primeira versão comercial

Criada em 24/09/2026 a partir do marco AleJoias, por solicitação do usuário.
Mantém formato de navegação e funcionalidades locais com identidade genérica.

A direção do produto, a separação das duas linhas e os próximos passos estão
consolidados em [ESTRATEGIA_COMERCIAL.md](ESTRATEGIA_COMERCIAL.md), distinguindo
decisões aprovadas de propostas ainda não definidas.

## Identidade e isolamento

Nome Vendas Simples, paleta azul/cinza, ícone vetorial de comprovante com check.
Título, PDFs, cobranças, notificações e nomes de arquivos usam a nova identidade.
applicationId com.vendassimples.app e banco vendas_simples.db são independentes
do AleJoias. FileProvider usa o applicationId em execução. Namespace Kotlin
interno preservado para evitar refatoração sem benefício funcional.

## Remoção da integração remota

Removidos Retrofit, cliente HTTP, endpoint Apps Script, modelos e rotinas de
importação/exportação/mesclagem remota, menus de sync e convite de importação
na primeira abertura. A aplicação não solicita permissão INTERNET.
O Apps Script e estrutura de planilha continuam disponíveis na master e na tag.
O aplicativo abre vazio e permite cadastrar a primeira venda normalmente.

SQLite mantém schema v2 e IDs. Tabelas SYNC_DIRTY e SYNC_DELETIONS e seus registros
internos legados foram preservados para não alterar schema/regras locais; não
há consumidor remoto nem envio. Fotos continuam vinculadas por id_venda_pai.

## Backup e saídas

Dados e backup oferece CSV, criar backup e compartilhar backup. O arquivo .db
inclui dados e fotos. O fluxo fecha o SQLite antes da cópia, como na base original.
Não foi adicionada restauração pela UI nem migração entre os dois aplicativos.
PDFs, CSV e WhatsApp mantêm os fluxos herdados, sem conexão ao antigo backend.

## Validação

Consultar CHANGELOG_COMERCIAL.md e docs/CHECKLIST_TESTES.md. A compilação e a
inspeção do APK não substituem testes funcionais no telefone. Priorizar primeira
venda, parcelamento, fotos, pagamentos, PDFs, CSV e backup, além da coexistência
com AleJoias. Sem publicação em loja ou configuração de assinatura comercial.
