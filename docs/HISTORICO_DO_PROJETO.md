# Histórico técnico resumido

## Fase inicial
O AleJoias Vendas começou utilizando Google Planilhas como armazenamento, acessada pelo Android através de Google Apps Script/Retrofit.

Foram evoluindo recursos de vendas, clientes, parcelamento, pagamentos, filtros, resumos, PDFs, CSV, WhatsApp, dashboard e notificações.

## Problemas identificados antes da migração SQLite

Durante revisão da base anterior foram encontrados pontos como:
- índice incorreto/duplicado no menu de ações do card, afetando cobrança via WhatsApp;
- inconsistência no nome do campo de data de pagamento enviado ao Apps Script;
- paliativo de data de pagamento em SharedPreferences;
- limitação na correção de valor pago.

Esses pontos foram corrigidos na evolução para a base atual.

## Migração
Foi criada a linha **AleJoias Vendas SQLite Sync v2**.

Decisão arquitetural:
- SQLite/Room = banco principal;
- Planilha/Apps Script = sincronização e compatibilidade;
- IDs da planilha mantidos no banco local.

## Validação da SQLite Sync v2
O usuário confirmou:
- compilação sem erros;
- APK gerado;
- instalação no telefone;
- importação da base anterior;
- criação de card local;
- sync para a planilha;
- WhatsApp funcionando;
- data de pagamento funcionando.

Após esses testes, a SQLite Sync v2 foi declarada **código-base estável**.

## Regra daqui em diante
Alterações devem ser pontuais e preservar suporte às funcionalidades existentes.
