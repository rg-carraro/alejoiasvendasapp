# Vendas Simples

Aplicativo Android para vendas de produtos e serviços, com dados em SQLite
local e identidade azul/cinza. Esta é a branch **comercial**; o AleJoias com
Google Planilhas permanece em **master**, preservado pela tag
`alejoias-v2-marco-2026-09-24`.

## Funcionalidades

- Venda à vista ou parcelada, edição, exclusão e clientes.
- Pagamentos parciais, correção de pagamento e cobrança via WhatsApp com
  identificação da parcela, valor pago e total da compra quando disponível.
- Fotos da galeria/câmera, miniatura e acesso a todas as fotos.
- Busca, filtros, painel financeiro e relatórios por período, mês e cliente.
- PDFs com fotos; PDF financeiro segue o mês selecionado no painel.
- Exportação CSV, criação e compartilhamento do backup SQLite com fotos.
- Indicadores clicáveis e notificações de vencimento.

O menu ⋮ no cabeçalho abre **Dados e backup**. Não há integração com Google
Planilhas, login remoto, importação online nem sincronização. Compartilhar PDF,
CSV, backup e abrir WhatsApp utiliza aplicativos externos escolhidos pelo usuário.

## Instalação e build

Identificador próprio: `com.vendassimples.app`. Pode coexistir com AleJoias e
começa com banco separado e vazio; não copia os dados do aplicativo original.
Java do Android Studio e SDK Android configurados em local.properties:

```powershell
.\gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Esta primeira versão comercial é para testes; não é uma publicação em loja.
O fluxo herdado cria e compartilha backups; ainda não há restauração de backup
pela interface. Guarde uma cópia fora do aparelho usando Compartilhar backup.

Veja [escopo comercial](docs/COMERCIAL.md), [funcionalidades](docs/FUNCIONALIDADES.md),
[checklist](docs/CHECKLIST_TESTES.md) e [changelog](CHANGELOG_COMERCIAL.md).