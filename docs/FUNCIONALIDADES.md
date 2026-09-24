# Funcionalidades existentes

A versão estável possui, entre outras, as seguintes capacidades que devem ser preservadas:

## Vendas
- nova venda;
- cliente;
- descrição;
- valor;
- data;
- parcelas;
- vencimentos;
- edição;
- exclusão.

## Lista/cards
- exibição de valores;
- valor pago;
- saldo;
- vencimento;
- estados visuais;
- filtros;
- pesquisa;
- cobrança via WhatsApp.

## Fotos dos produtos
- fotos opcionais para novas vendas;
- seleção de uma ou várias fotos da galeria;
- captura pela câmera;
- miniatura no card da lista de vendas, pesquisa e histórico do cliente;
- toque na miniatura ou em **Ver fotos da venda** abre os detalhes com todas
  as fotos, contagem e orientação para deslizar; cada foto pode ser ampliada;
- visualização de todas as fotos nos detalhes de cada parcela da mesma venda;
- exportação de PDF individual do card e relatórios com todas as fotos dos registros;
- armazenamento offline no SQLite e inclusão no backup local.

Vendas antigas ou importadas podem não ter foto. As fotos não fazem parte da
sincronização com a planilha.

## Pagamentos
- pagamento parcial;
- data de pagamento;
- correção de valor;
- vínculo pelo `id_venda`.

## Relatórios
- resumo financeiro;
- resumo por período;
- resumo mensal;
- resumo por cliente;
- painel financeiro e relatórios mensal e por período com indicadores
  clicáveis que abrem os cards correspondentes;
- histórico de cliente;
- pesquisa por cliente, descrição, datas e valor integrada à lista de vendas.

O acesso **Financeiro / Relatórios** reúne o painel mensal e botões iguais
para relatório por período, mensal, por cliente e PDF financeiro. CSV continua
disponível no relatório mensal e em Dados; o relatório por cliente mantém PDF e histórico.
Dados e sincronização ficam no botão de opções `⋮` do menu principal.

O PDF financeiro usa a mesma lista do painel, filtrada pela data da venda no
mês selecionado. Valores vendido, recebido, saldo, clientes em débito, vencidas
e quantidade de cards correspondem aos indicadores. O cabeçalho informa o mês
e seu período completo, inclusive sem vendas, sem herdar filtros de outro relatório.
O PDF mensal também recebe explicitamente o mês selecionado.

## Saídas
- PDFs;
- CSV;
- compartilhamento;
- backup local.

## Integração
- importação da planilha para SQLite;
- envio SQLite para planilha;
- sincronização;
- Apps Script.

## Notificações
- alertas relacionados a vencimentos.

Esta lista é uma referência de compatibilidade. Antes de remover ou modificar qualquer item, verificar a implementação real no código.
