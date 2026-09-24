# Checklist de regressão

Após mudanças, testar pelo menos:

- [ ] app abre sem crash;
- [ ] base SQLite existente abre sem perda;
- [ ] clientes aparecem;
- [ ] nova venda sem foto salva normalmente;
- [ ] nova venda com uma foto da galeria;
- [ ] nova venda com várias fotos da galeria;
- [ ] nova venda com foto tirada pela câmera;
- [ ] salvar venda com foto não apresenta divisão por zero;
- [ ] miniatura aparece no card, pesquisa da lista e histórico do cliente;
- [ ] todas as fotos aparecem nos detalhes de cada parcela após reiniciar;
- [ ] tocar na miniatura ou em Ver fotos da venda abre todas as fotos; deslizar
      até a última e ampliar cada uma, também na pesquisa e no histórico;
- [ ] PDF individual do card contém todas as fotos;
- [ ] PDFs de resumo, mês e cliente incluem fotos dos respectivos cards;
- [ ] backup SQLite contém as fotos;
- [ ] base SQLite v1 migra para v2 sem perder vendas ou pagamentos;
- [ ] nova venda simples;
- [ ] nova venda parcelada;
- [ ] edição de venda;
- [ ] exclusão;
- [ ] pagamento parcial;
- [ ] segundo pagamento;
- [ ] data de pagamento;
- [ ] correção de valor pago;
- [ ] cobrança WhatsApp;
- [ ] cobrança identifica parcela (ex.: 2 de 3), valor pago e saldo do card;
- [ ] total da compra e total pago somam apenas parcelas do mesmo id_venda_pai,
      inclusive parcelas já quitadas e fora do filtro da lista;
- [ ] venda à vista mostra valor, pago e saldo; venda quitada não abre cobrança;
- [ ] venda sem id_venda_pai, com parcelas ausentes ou numeração inconsistente
      não apresenta total da compra estimado; parcela desconhecida é sinalizada;
- [ ] filtros;
- [ ] pesquisa por cliente, descrição, datas e valor na lista de vendas;
- [ ] busca combinada com filtros de data e a receber;
- [ ] menu Financeiro / Relatórios mostra painel mensal e botões uniformes
      para período, mensal, cliente e PDF financeiro;
- [ ] botão `⋮` abre Dados e sincronização;
- [ ] relatório por período e envio de imagem por WhatsApp;
- [ ] resumo financeiro;
- [ ] resumo mensal;
- [ ] relatório por cliente mantém histórico e extrato PDF, sem botão CSV;
- [ ] dashboard, inclusive seleção de outro mês;
- [ ] PDF financeiro corresponde aos seis indicadores e aos cards do mês
      selecionado, com vendas de outros meses excluídas;
- [ ] após usar filtro por período, PDF financeiro e mensal mantêm o próprio
      mês e período; mês vazio mostra referência correta e totais zerados;
- [ ] telas sem imagem de marca-d'água no topo, mantendo título, cores e ícone;
- [ ] tocar em Vendido no mês lista os cards e soma o valor vendido exibido;
- [ ] tocar em Recebido no mês lista os cards pagos e soma o valor recebido exibido;
- [ ] valores e contagens do painel, relatório mensal e por período abrem
      listas com totais correspondentes e voltam ao relatório de origem;
- [ ] PDF;
- [ ] CSV;
- [ ] backup;
- [ ] Planilha → SQLite;
- [ ] SQLite → Planilha;
- [ ] sync repetido sem duplicação;
- [ ] funcionamento offline.
