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
- dashboard com detalhamento dos cards vendidos e pagos do mês selecionado;
- histórico de cliente;
- pesquisa por cliente, descrição, datas e valor integrada à lista de vendas.

Dashboard, resumo geral, relatórios por período/mês/cliente, PDFs e CSVs
ficam reunidos no acesso **Financeiro / Relatórios** do menu principal.

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
