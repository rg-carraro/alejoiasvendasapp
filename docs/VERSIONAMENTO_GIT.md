# Versionamento Git

## Base estável

A tag `sqlite-sync-v2-stable` identifica o código-base validado da versão 2.0
SQLite Sync. Mudanças futuras devem partir dessa base e ser pequenas e
reversíveis.

## Fluxo recomendado

1. Confirmar que a árvore está limpa com `git status`.
2. Criar uma branch curta para cada ajuste: `fix/...`, `feature/...` ou
   `docs/...`.
3. Fazer mudanças pontuais e revisar `git diff`.
4. Executar `gradlew.bat assembleDebug` e o checklist aplicável.
5. Criar commits descritivos sem incluir APKs, caches, configurações locais ou
   credenciais.
6. Integrar a mudança somente depois da validação no aparelho quando ela afetar
   dados, interface, pagamentos ou sincronização.

## Remoto

O repositório remoto deve ser privado, especialmente porque o aplicativo contém
o endereço da implantação do Apps Script. Configure o remoto apenas no serviço
e na conta escolhidos pelo responsável do projeto.

Exemplo, depois de criar o repositório remoto:

```powershell
git remote add origin <URL-DO-REPOSITORIO-PRIVADO>
git push -u origin main --tags
```

## Recuperação

Para consultar a base estável sem alterar arquivos:

```powershell
git show sqlite-sync-v2-stable
```

Não use comandos destrutivos (`reset --hard`, limpeza forçada ou remoção da
base local) sem confirmar o alvo e possuir backup.
