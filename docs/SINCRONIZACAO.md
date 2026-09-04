# Sincronização SQLite ↔ Google Planilhas

## Finalidade

SQLite é a base operacional principal. A planilha permanece disponível como base sincronizável e compatível com o histórico do projeto.

## Operações esperadas

1. **Importar Planilha → SQLite**
   - usada para trazer a base existente para o aparelho;
   - deve preservar IDs.

2. **Enviar SQLite → Planilha**
   - envia o estado local para o backend;
   - deve preservar IDs e relacionamentos.

3. **Sincronizar / Mesclar**
   - concilia dados pelos IDs;
   - deve evitar duplicações;
   - alterações locais pendentes não devem ser descartadas inadvertidamente.

4. **Exportar CSV**
   - saída compatível com Excel.

5. **Backup SQLite**
   - cópia do banco local.

## Testes já realizados com sucesso

- importação inicial da planilha;
- criação de card diretamente no SQLite;
- sincronização do novo card para a planilha;
- funcionamento normal aparente após o sync.

## Cuidados

Qualquer alteração nessa área deve ser testada com:
- venda nova;
- venda parcelada;
- pagamento parcial;
- alteração de pagamento;
- exclusão;
- alteração feita na planilha;
- alteração feita offline;
- repetição do sync para verificar duplicidades.
