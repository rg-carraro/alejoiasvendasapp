# Interface visual e padrões de evolução

Este documento registra a forma de aplicar melhorias de interface no AleJoias
Vendas sem perder a identidade da marca ou alterar o funcionamento offline.

## Identidade preservada

Os valores visuais atuais ficam centralizados no `MainActivity.kt`:

| Uso | Cor aproximada | Função |
| --- | --- | --- |
| Fundo | `#F7F3EC` | canvas quente do aplicativo |
| Superfície | `#FFFDF9` | cards, campos e diálogos |
| Primária | `#145A4A` | ações principais e navegação |
| Primária escura | `#0B3D33` | títulos e contraste |
| Destaque | `#B88A44` | subtítulos, focos e acentos |
| Texto | `#27312E` | conteúdo principal |
| Borda | `#DED3C2` | separação discreta |

A marca existente, `watermark_alejoias.png`, o ícone e os recursos Android devem
continuar sendo usados. Uma futura extração das cores para `AppTheme` ou um
objeto de estilo é aceitável, mas deve ser mecânica e visualmente equivalente.

## Estrutura visual aplicada

O menu inicial segue esta sequência:

```text
marca e cabeçalho
status do SQLite
painel de saudação e resumo
Ações rápidas
  Vendas | Nova venda
  Financeiro | Clientes
Acompanhe sua operação
  recebido | a receber | vencidos | cards totais
nota sobre SQLite e sincronização
```

O padrão prioriza uma ação por card, títulos curtos, subtítulo explicativo,
espaçamento generoso, cantos arredondados e estados financeiros legíveis. Os
cards de vendas continuam mostrando etiquetas textuais (`QUITADO`, `VENCIDO`,
`PAGO PARCIALMENTE`, `EM ABERTO`); cor nunca é o único indicador.

## Mapa de implementação

- `MainActivity.kt`: composição das telas, tema atual, cards, botões, diálogos,
  carregamento do SQLite e navegação por estados (`telaAtual`).
- `LocalDatabase.kt`: fonte operacional dos dados; a UI apenas consulta e grava
  por meio dos métodos existentes.
- `RetrofitClient.kt`, `ApiService.kt` e `SyncModels.kt`: integração remota e
  sincronização; mudanças de UI não devem alterar esse contrato.
- `res/drawable` e `res/mipmap-*`: marca, watermark, ícones e recursos nativos.

## Padrões para próximas mudanças

1. Reutilizar `fundoArredondadoComBorda`, `margemCard`, `texto`,
   `campoRotulado`, `botaoQuadrado` e `botaoVoltar` antes de criar uma variação.
2. Para uma nova área, começar por título/status, ação principal, estado vazio,
   conteúdo e feedback de sucesso/erro.
3. Usar `adicionarCardResumo` para indicadores clicáveis e preservar a abertura
   dos detalhes financeiros.
4. Manter telas roláveis, campos acessíveis em teclado aberto e botões com área
   mínima confortável em telefones estreitos.
5. Preferir títulos em frase; preservar os nomes funcionais já conhecidos pelo
   usuário (`Vendas`, `Financeiro`, `Clientes`, `Dados e sincronização`).
6. Manter feedback em `Toast`, `statusText` ou diálogo conforme o fluxo atual;
   nunca esconder falha de gravação ou sincronização.

## Limites de segurança e dados

Uma melhoria visual não pode:

- trocar SQLite local por chamadas obrigatórias à internet;
- alterar `id_cliente`, `id_venda`, `id_pagamento` ou `id_venda_pai`;
- remover a sincronização, backup, importação ou exportação;
- mudar cálculo de saldo, parcelas ou datas de pagamento;
- colocar dados de vendas em logs, imagens de diagnóstico ou mensagens abertas;
- substituir componentes funcionais por protótipos sem persistência.

## Checklist de validação

Após uma mudança de UI:

- compilar `assembleDebug` ou `assembleRelease`;
- abrir o menu inicial, lista de vendas, nova venda, detalhes, pagamento,
  financeiro, cliente e dados/sincronização;
- testar uma venda existente, pagamento parcial, correção e cobrança WhatsApp;
- verificar operação sem internet nas ações locais;
- conferir que fotos, PDFs, CSV, backup e sincronização continuam disponíveis;
- verificar layout em telefone estreito e rolagem com teclado;
- revisar o diff para confirmar que somente os arquivos necessários mudaram;
- registrar o resultado em `CHANGELOG_SQLITE_SYNC.txt`.

## Estado da aplicação deste padrão

A primeira aplicação foi concluída em 22/09/2026. A hierarquia de painel e
seções está presente no menu, vendas, financeiro, detalhamentos, relatórios,
histórico de cliente e dados/sincronização. Os estados vazios dessas áreas usam
mensagens orientativas. O build debug foi validado depois da alteração.

## Como repetir o padrão do MVP clínico

O AleJoias pode adotar gradualmente a mesma hierarquia de painel, seção e
indicadores, mas deve manter a linguagem da marca. A sequência recomendada é:

1. extrair cores e espaçamentos para um pequeno conjunto de helpers;
2. padronizar cabeçalhos de seção e estados vazios;
3. reutilizar cards de resumo em dashboard, período, mês e cliente;
4. melhorar uma tela por vez, validando SQLite e sincronização após cada etapa;
5. só depois avaliar navegação mais ampla, como drawer ou abas, sem remover os
   atalhos atuais antes de uma validação no telefone.

O objetivo é consistência visual entre telas, não uma conversão para Flutter ou
uma cópia da identidade do outro projeto.
