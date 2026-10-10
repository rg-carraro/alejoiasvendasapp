---
name: alejoias-interface
description: Evoluir interface, menus, navegação, cards e diálogos do AleJoias Vendas preservando marca e recursos SQLite/Planilhas.
---

# Interface e navegação do AleJoias

Leia [interface visual](../../../docs/INTERFACE_VISUAL.md),
[checklist](../../../docs/CHECKLIST_TESTES.md) e a implementação afetada em
`MainActivity.kt`. Use a master do AleJoias como destino; comercial não é alvo
das melhorias atuais. A paleta e a marca são do AleJoias: referência de outro
aplicativo orienta usabilidade, não substitui identidade ou funcionalidades.

Configure retornos por `configurarRetorno`: seta e Voltar Android devem chamar
a mesma ação. No menu, o retorno é nulo e a seta fica oculta. Histórico volta
para clientes e detalhamento financeiro para seu relatório de origem. Preserve
filtros e meses existentes; não prometa restauração de rolagem ou rascunhos
após rotação/process death sem implementá-la e testar.

Mantenha ações dos diálogos acessíveis fora da área rolável, campos e textos
completos, fotos ampliáveis e opção de continuar editando ao confirmar descarte.
Adapte agrupamentos à largura e escala de fonte, com toque mínimo de 48 dp.
Reutilize helpers locais e preserve o acesso a Dados e sincronização no menu ⋮.

Compare o código de gravação, pagamentos, cálculos, exportações e sync antes
e depois. Não importar Billing, limites FREE, schema ou configuração do
Vendas Simples como parte de uma adaptação visual.

Compile com o wrapper do AleJoias e execute os cenários afetados com dados
sintéticos: retorno, teclado, cancelamento, fotos, tela estreita, fonte ampliada
e paisagem. Build não aprova aparência ou persistência. Registre resultados e
limites na documentação e changelog; versionamento em
[VERSIONAMENTO_GIT.md](../../../docs/VERSIONAMENTO_GIT.md).
