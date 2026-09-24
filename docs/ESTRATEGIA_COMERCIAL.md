# Estratégia comercial — Vendas Simples

Registro das decisões tomadas em 24/09/2026. Este documento consolida o escopo
aprovado; sugestões futuras estão identificadas como propostas, sem compromisso
de implementação ou lançamento.

## Direção do produto aprovada

Criar uma aplicação genérica para controle de vendas simples, independente da
AleJoias e do ramo de joias. Reaproveitar o formato e as funcionalidades locais
da base existente, com outra identidade, evitando reconstruir fluxos funcionais.

Nome inicial aprovado: **Vendas Simples**. Identidade aprovada: **azul e cinza**.
A interface prioriza clareza, poucos elementos decorativos, botões legíveis e
acesso direto a vendas, nova venda, financeiro e clientes.

O produto permite registrar vendas, acompanhar parcelas e pagamentos, consultar
clientes, organizar fotos e preparar cobranças e relatórios. Essas capacidades
definem a proposta inicial; não há pesquisa de mercado ou validação comercial
documentada até este momento.

## Escopo da primeira versão

| Manter | Retirar da linha comercial |
| --- | --- |
| Vendas, edição, exclusão e parcelamento | Marca e imagens AleJoias |
| Pagamentos parciais e correções | Referências a joalheria |
| Fotos, pesquisa e filtros | Backend Google Apps Script |
| Financeiro e relatórios por período, mês e cliente | Importação da planilha |
| PDF, CSV e cobrança por WhatsApp | Envio e mesclagem com planilha |
| Notificações e backup SQLite com fotos | Convite de importação na primeira abertura |

Dados ficam no aparelho. O menu **Dados e backup** oferece exportação CSV,
criação e compartilhamento do backup. WhatsApp e compartilhamento usam os
aplicativos externos escolhidos pelo usuário. Não há conta remota ou serviço
de sincronização próprio.

## Separação entre os produtos

| Linha | Referência | Finalidade |
| --- | --- | --- |
| AleJoias Vendas | `master` | Manter o aplicativo original com integração de planilha |
| Marco AleJoias | `alejoias-v2-marco-2026-09-24` | Preservar o estado anterior à derivação comercial |
| Vendas Simples | `comercial` | Evoluir o aplicativo genérico e local |

A tag do marco não deve ser movida. Não mesclar indiscriminadamente a branch
comercial em master, pois ela remove recursos específicos do AleJoias.
Correções úteis às duas linhas devem ser avaliadas e aplicadas pontualmente,
com validação em cada uma.

O identificador `com.vendassimples.app` e o banco `vendas_simples.db` permitem
instalação paralela e dados separados. O primeiro uso começa vazio; não há
migração automática dos dados AleJoias. Detalhes técnicos em [COMERCIAL.md](COMERCIAL.md).

## Estado de entrega e limites conhecidos

A primeira versão comercial foi implementada no commit `4253efc`, compilada
com `assembleDebug` e publicada na branch comercial. A inspeção do APK confirmou
nome, identificador e ícone próprios e ausência de permissão INTERNET.
As operações locais de vendas, fotos e pagamentos foram comparadas com o marco.

Testes funcionais e visuais no telefone continuam pendentes conforme
[CHECKLIST_TESTES.md](CHECKLIST_TESTES.md). O APK entregue é de testes; não houve
publicação em loja, assinatura comercial ou homologação completa.

O backup atual permite criar e compartilhar o arquivo; **não existe restauração
pela interface**. Também não há sincronização entre aparelhos. Não apresentar
essas capacidades como disponíveis em material comercial.

## Próximos passos propostos — ainda não aprovados

1. Validar os fluxos principais no telefone e registrar resultados do checklist.
2. Experimentar o aplicativo com usuários de diferentes atividades para verificar
   se os termos, relatórios e navegação atendem ao controle de vendas simples.
3. Avaliar a restauração de backup pela interface antes de ampliar a distribuição.
4. Definir o modelo de distribuição, suporte e manutenção antes do lançamento.

Preço, assinatura, licenciamento, anúncios, público-alvo específico, canais de
aquisição, prazo de lançamento e publicação em loja **não foram definidos**.
Este registro não autoriza contratação de serviços, campanhas ou gastos.

## Rotina de evolução aprovada

Documentar cada alteração e sua validação, fazer commit descritivo e publicar
na branch correspondente. Registrar limitações e testes pendentes. Não incluir
dados de clientes, credenciais ou artefatos de build nos commits.
Consultar [VERSIONAMENTO_GIT.md](VERSIONAMENTO_GIT.md) antes da publicação.
