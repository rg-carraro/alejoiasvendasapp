# Versionamento das duas linhas

- `master`: AleJoias Vendas com SQLite e Google Planilhas.
- Tag imutável `alejoias-v2-marco-2026-09-24`: marco anterior à linha comercial.
- `comercial`: Vendas Simples, identidade própria e operações locais.

Remoto: https://github.com/rg-carraro/alejoiasvendasapp.git

Antes de publicar, conferir git status, branch, diff e estado remoto. Documentar,
validar com assembleDebug e publicar toda mudança concluída (autorização permanente).
Não integrar a remoção da planilha de volta à master. Não versionar APK, caches,
configurações locais, dados de clientes ou credenciais.

```powershell
git branch --show-current
git remote -v
git ls-remote --heads origin
git push origin comercial
```

Não usar push forçado nem mover a tag do marco. Para voltar ao AleJoias, com
árvore limpa, usar git switch master. Leia docs/MARCO_2026-09-24.md.