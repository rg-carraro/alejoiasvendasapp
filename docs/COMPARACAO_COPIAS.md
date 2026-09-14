# Sincronização das cópias e do GitHub

Em 13/09/2026, a cópia `C:\Users\rgcar\git\alejoiasvendasapp` foi comparada com
este repositório. O código de SQLite (`SQLiteOpenHelper`), modelos, API, Apps
Script, Gradle e recursos era igual por conteúdo. A diferença de aplicativo
estava em `MainActivity.kt` e `app/src/main/res/values/styles.xml`.

As duas alterações de interface foram integradas nesta base: paleta
verde/dourado, diálogos e menus revisados, rótulos de campos, etiquetas de
status nos cards e seleção de mês no dashboard financeiro. O schema SQLite,
os IDs compartilhados e o backend Apps Script permaneceram inalterados.

A documentação desta pasta é a referência atual. O `README.md` e alguns textos
de arquitetura da cópia externa eram históricos: descreviam a planilha como
banco principal ou mencionavam Room, embora o aplicativo use `SQLiteOpenHelper`.
Os documentos corrigidos desta pasta devem acompanhar o código publicado.

A cópia externa tinha alterações locais apenas em `.idea/`, geradas pelo Android
Studio. Esses arquivos não pertencem ao aplicativo e não foram sobrescritos.

Validação: `gradlew.bat assembleDebug --offline` com o JBR do Android Studio
concluiu com sucesso. Testes no aparelho e do fluxo real de sincronização ainda
precisam ser executados antes de considerar a nova interface validada em uso.
