# Versionamento Git

A tag `sqlite-sync-v2-stable` identifica a base SQLite Sync v2 validada. Mudanças devem ser pontuais e reversíveis.

## Fluxo atual

1. Confira `git status` e o código afetado; isole o trabalho em branch quando necessário.
2. Revise `git diff`, valide com `gradlew.bat assembleDebug` e execute o checklist aplicável em `docs/CHECKLIST_TESTES.md`.
3. Faça commits descritivos sem APKs, caches, configurações locais ou credenciais.
4. Antes de sincronizar a cópia do Android Studio em `C:\Users\rgcar\git\alejoiasvendasapp`, confira sua árvore de trabalho. Prefira avanço rápido sem descartar alterações locais.
5. Toda alteração concluída deve ser documentada e publicada no GitHub, conforme autorização permanente do usuário em 24/09/2026. Atualize o changelog e a documentação afetada, confira o remoto e publique os commits sem pedir nova confirmação. Informe impedimentos e quais testes ainda dependem do aparelho.

## Remoto

O `origin` configurado é `https://github.com/rg-carraro/alejoiasvendasapp.git` e a branch publicada é `master`. O usuário já autorizou essa publicação. Confira o destino antes do push:

```powershell
git remote -v
git ls-remote --heads origin
git push origin HEAD:master
```

O repositório é público; a URL do Apps Script existente no aplicativo não autentica o backend. Não publique segredos novos, arquivos de assinatura ou dados locais.

## Recuperação

Use `git show sqlite-sync-v2-stable` para consultar a base estável. Não use `reset --hard` nem limpeza forçada sem conferir alvo e backup.
