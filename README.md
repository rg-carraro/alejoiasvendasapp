# Controle de Vendas Básico

Projeto Android básico funcional usando Google Planilhas + Apps Script como banco de dados.

## O que faz

- Lista vendas com valor total, total pago e saldo devedor.
- Cadastra nova venda.
- Registra pagamento parcial.
- Atualiza relatório direto da planilha.

## Como usar

1. Abra este projeto no Android Studio.
2. Vá no arquivo:

`app/src/main/java/com/example/controlevendas/RetrofitClient.kt`

3. Troque:

`https://script.google.com/macros/s/SEU_ID_AQUI/`

pela URL base do seu Apps Script.

Exemplo:

Se sua URL final for:

`https://script.google.com/macros/s/AKfycbxxxxxxxxxxxx/exec`

Use:

`https://script.google.com/macros/s/AKfycbxxxxxxxxxxxx/`

4. Clique em Run para testar.
5. Para gerar APK: Build > Build APK(s).

## Atenção

Seu Apps Script precisa ter os endpoints:

- GET action=relatorio
- POST action=nova_venda
- POST action=novo_pagamento

## Nova versão

- Interface em tons de cinza.
- Ícone de diamante de exemplo.
- Lista exibe nome do cliente, data da venda, valor total, total pago e saldo devedor.
- Incluído arquivo `AppsScript_Codigo.gs` com backend atualizado para retornar o nome do cliente.

## Estrutura da aba CLIENTES

`id_cliente | nome`

Exemplo:

`1 | Maria Silva`

A venda deve usar o mesmo `id_cliente` cadastrado na aba CLIENTES.


## Atualização: abas e pagamento por venda

Esta versão adiciona:

- Aba **Vendas**
- Aba **Resumo**
- Ao tocar em uma venda, abre opções:
  - Registrar pagamento
  - Ver detalhes
- O pagamento já usa automaticamente o ID da venda selecionada.
- A lista de vendas fica separada da visão de resumo.



## Atualização ERP simples

Esta versão adiciona o campo **Descrição da venda**.

### Estrutura atualizada das abas

CLIENTES:

`id_cliente | nome`

VENDAS:

`id_venda | id_cliente | descricao | data_venda | valor_total | parcelas`

PAGAMENTOS:

`id_pagamento | id_venda | data_pagamento | valor_pago`

### Importante

Depois de substituir o código no Apps Script, publique uma nova implantação ou atualize a implantação existente.
No Android, mantenha a URL base no arquivo:

`app/src/main/java/com/example/controlevendas/RetrofitClient.kt`



## Atualização: JSON robusto + interface moderna

### Backend
Substitua o código do Apps Script pelo arquivo `AppsScript_Codigo.gs`.

Ele agora:
- sempre retorna JSON;
- cria abas ausentes automaticamente;
- cria cabeçalhos se a aba estiver vazia;
- padroniza data como yyyy-MM-dd.

### App
- menu principal separado;
- tela exclusiva de lista de vendas;
- tela de resumo financeiro;
- cards mais modernos em tons de cinza;
- pagamento ao tocar na venda.


## Atualização: ícone, botões e estrutura da planilha

### Ícone
Foram gerados PNGs nos tamanhos corretos do Android:

- mipmap-mdpi: 48x48
- mipmap-hdpi: 72x72
- mipmap-xhdpi: 96x96
- mipmap-xxhdpi: 144x144
- mipmap-xxxhdpi: 192x192

Isso evita distorção no celular.

### Botões
Os botões do menu agora ficam lado a lado, em blocos quadrados, com cantos arredondados e aparência translúcida.

### Planilha
Veja o arquivo `ESTRUTURA_PLANILHA.txt` para conferir os nomes corretos das colunas.


## Ajustes desta versão

- Backend lê as colunas pelo nome do cabeçalho.
- Se o id_cliente não existir, ele é cadastrado automaticamente.
- Nova venda agora possui campo Nome do cliente.
- Botões do menu em cinza claro translúcido.
- Botão voltar do Android retorna ao menu em Vendas ou Resumo.
- Cards de venda aparecem apenas no menu Lista de vendas.
- Card de venda exibe nome do cliente, valor da venda, já pago e faltante.


## Correções desta versão

- Corrige desalinhamento de valor_total e parcelas na aba VENDAS.
- Cliente é cadastrado automaticamente pelo nome.
- O app não exibe IDs para o usuário.
- Nova venda não pede id_cliente.
- Campo de data abre calendário.
- Card de venda aparece apenas no menu Vendas.
- Card mostra nome do cliente, valor vendido e valor recebido.


## Final Ajustes

- Ícone anexado aplicado ao app.
- Atualização automática a cada 5 minutos.
- Botão Atualizar removido do menu.
- Resumo financeiro separado da lista de vendas.
- Backend corrigido para CLIENTES, VENDAS e PAGAMENTOS.


## Correções desta versão

- Menu principal sem card de resumo.
- Card de vendas simplificado.
- Detalhes completos ao clicar no card.
- Nova venda só salva com Nome, Data e Valor.
- Backend corrigido para CLIENTES e VENDAS.


## Correção do ícone

Esta versão aplica o ícone em dois formatos:

- Ícones PNG legados em `mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi` e `mipmap-xxxhdpi`.
- Ícone adaptável Android em `mipmap-anydpi-v26`.

Se o celular ainda mostrar o ícone antigo:
1. Desinstale o app antigo do celular.
2. Limpe o projeto no Android Studio: Build > Clean Project.
3. Gere o APK novamente.
4. Instale o novo APK.


## Correção de Build do Ícone

Corrigido erro:

`@drawable-nodpi/ic_launcher_foreground is incompatible with attribute drawable`

Agora o adaptive icon usa:

`@drawable/ic_launcher_foreground`

Arquivos corrigidos:
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
- app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
- app/src/main/res/drawable/ic_launcher_foreground.png


## Ajustes desta versão

- Cards coloridos conforme pagamento.
- Filtro por data de venda.
- Filtro A Receber.
- Sugestão de clientes cadastrados.
- Descrição com 3 linhas no final.
- Parcelas padrão 1 e nunca zero.
- Venda parcelada cria vários cards com vencimento a cada 30 dias.
- Resumo por cliente com histórico.
- Resumo do mês.


## Ajustes desta versão

- Vencimento de toda venda em 30 dias após a compra.
- Parcelamento com vencimentos 30/60/90 dias.
- Data de vencimento visível nos cards.
- Card vermelho somente quando vencido e não quitado.
- Relatório por período personalizado.
- UI modernizada e responsiva para diferentes telas.


## Ajuste desta versão

- Menu principal mostra apenas:
  - Vendas / Lista
  - Nova / Venda
  - Resumo / Relatórios
  - Clientes / Histórico
- Removido o dashboard/card do menu principal.
- Mantida atualização automática a cada 5 minutos.
- Mantida atualização automática após nova venda ou registro de pagamento.


## Ajuste desta versão

- Histórico do cliente agora aparece como lista de cards, parecido com os detalhes/lista de vendas.
- Cada venda do histórico mostra compra, vencimento, parcela, valor, pago e faltante.
- Card verde somente quando Valor Pago é maior ou igual ao Valor da Venda.


## Correção de Build

Corrigido erro Kotlin `Expecting '"'` no `MainActivity.kt`.
A causa era quebra indevida de strings no card do histórico do cliente.


## Marca d'água AleJoias

- Adicionada a imagem `watermark_alejoias.png` em `app/src/main/res/drawable`.
- Marca d'água centralizada no fundo do menu/telas com opacidade de 10%.
- Layout usa `FrameLayout` para manter a imagem atrás dos botões e cards.


## Ajuste: Resumo mensal selecionável

- O resumo do mês agora permite selecionar outros meses.
- A tela mostra a mesma visão para o mês escolhido:
  - Quantidade de cards
  - Cards em aberto
  - Cards vencidos
  - Cards quitados
  - Total vendido
  - Total recebido
  - Faltante


## Ajuste: Cores no Clientes Histórico

Aplicada a mesma regra da lista de vendas no resumo/histórico de clientes:

- Vermelho claro: cliente possui venda vencida e não quitada.
- Verde claro: todas as vendas/cards do cliente estão quitados.
- Branco: cliente possui vendas em aberto, mas nenhuma vencida.


## Fase 1 aplicada

Funcionalidades adicionadas:
- Pesquisa por nome do cliente no menu Vendas.
- WhatsApp direto para cobrança no card da venda.
- Dashboard financeiro.
- Notificações de vencimento.

Observação:
- Em Android 13+, o sistema pode solicitar permissão de notificações.
- A cobrança via WhatsApp abre a tela do WhatsApp com mensagem pronta.


## Correção de build

Corrigido erro:
`Conflicting overloads: cardDashboard`

A função `cardDashboard(...)` estava duplicada no `MainActivity.kt`.
A duplicidade foi removida.


## Ajustes pontuais somente no app

- Cards de venda ordenados por data de vencimento decrescente.
- Cards pagos exibem Data Pagamento local para pagamentos feitos a partir desta versão.
- Clientes / Histórico ganhou pesquisa por nome do cliente.
- Resumo por período ganhou botão para enviar por WhatsApp.
- Correção de pagamento por complemento:
  - Exemplo: se lançou R$ 1,00 mas queria R$ 100,00, informe R$ 100,00 e o app registra complemento de R$ 99,00.
  - Redução de valor pago não foi implementada porque exigiria alteração no backend.


## Ajustes desta versão

- Data de pagamento agora pode vir do backend pelo campo `data_pagamento`.
- O Apps Script foi atualizado para devolver a última data de pagamento de cada card.
- O relatório por período agora compartilha uma imagem/foto do resumo exibido.
- Tela de resumo compactada para evitar botões atrás da navegação do telefone.
- Scroll recebeu margem inferior para melhorar encaixe em diferentes celulares.

Backend necessário:
- Substituir o Apps Script pelo `AppsScript_Codigo.gs` deste pacote.
- Depois publicar uma nova implantação do Web App.


## Correção de build

Corrigido erro:
`Unresolved reference: data_pagamento`

A versão agora compila mesmo sem o campo `data_pagamento` no model Android.
A data exibida no card continua funcionando para pagamentos lançados pelo app, salva localmente no aparelho.

Observação:
Para buscar datas antigas diretamente do backend, será necessário evoluir o contrato app/API de forma controlada numa próxima etapa.


## Correção definitiva de build

Removida qualquer referência Kotlin a `data_pagamento`.
A data de pagamento exibida no app fica salva localmente para pagamentos feitos pelo próprio aplicativo.


## Evolução profissional aplicada

Incluído no app:
- PDF de extrato do cliente.
- PDF do resumo.
- Exportação CSV compatível com Excel.
- Pesquisa global.
- Backup local em CSV.
- Gráfico financeiro simples no dashboard.

Observação:
- Alterações aplicadas no app. O backend foi preservado.
- Arquivos PDF/CSV são gerados localmente e compartilhados pelo Android.


## Correção de build

Corrigido erro:
`Conflicting declarations: val linha3`

A duplicidade da variável no menu principal foi removida/renomeada.


## Ajustes desta versão

- Cards do menu Vendas ficaram menores.
- Data de pagamento agora é lida do backend pelo campo `data_pagamento`, com fallback local.
- Resumo de qualquer mês selecionado agora possui:
  - Gerar PDF Deste Mês
  - Exportar CSV Deste Mês

Backend:
- Para a data de pagamento vir da planilha, substitua o Apps Script pelo `AppsScript_Codigo.gs` deste pacote e publique nova implantação.
- Alteração pontual no backend: `getRelatorio()` agora retorna `data_pagamento` com a última data de pagamento da venda.


## Refinamento do PDF

O PDF exportado agora exibe cada entrada com título de campo e valor:

- Cliente
- Descrição
- Data Da Compra
- Data De Vencimento
- Parcela
- Valor Da Venda
- Valor Pago
- Saldo Faltante
- Status


## PDF refinado e deletar card

- PDF com cabeçalho profissional, mês/período, resumo executivo, detalhamento e rodapé.
- Adicionada opção Deletar Card no menu do card de venda.
- Para deletar da planilha, substitua o Apps Script pelo `AppsScript_Codigo.gs` deste ZIP e publique uma nova implantação.
