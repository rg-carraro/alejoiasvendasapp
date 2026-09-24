# Interface Vendas Simples

Identidade aprovada: azul e cinza, sem marca ou elementos de joalheria.

| Uso | Cor |
| --- | --- |
| Fundo | #F5F7FB |
| Superfície | #FFFFFF |
| Primária | #2563EB |
| Primária escura | #1E3A5F |
| Destaque | #475569 |
| Texto | #1E293B |
| Texto secundário | #64748B |
| Borda | #CBD5E1 |

Cores em MainActivity.kt e res/values/styles.xml. Ícone vetorial de comprovante
em drawable/ic_vendas_simples.xml. Estados pago/vencido preservam cores
semânticas e etiquetas textuais, sem depender apenas da cor.

Navegação preservada: Vendas, Nova venda, Financeiro e Clientes. O cabeçalho
Vendas Simples / Seu controle de vendas contém ⋮, que abre Dados e backup.
Sem imagem de marca-d'água nem elementos AleJoias.

Cabeçalho com margem superior de 16 dp, insets para barras/câmera/teclado.
Botões com 8 dp acima/abaixo. Ver fotos fica ao lado da miniatura; abre todas
as fotos nos detalhes, com ampliação individual e orientação para deslizar.

Manter telas roláveis, áreas de toque, busca, indicadores financeiros clicáveis
e coerência dos PDFs com os filtros. Reutilizar helpers existentes. Não adicionar
cabeçalhos ou painéis decorativos sem necessidade. Validar telefone estreito,
fonte ampliada, câmera, rotação e teclado. Documentar e compilar a cada mudança.