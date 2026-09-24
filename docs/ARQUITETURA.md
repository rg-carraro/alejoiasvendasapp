# Arquitetura comercial

Vendas Simples usa SQLiteOpenHelper, sem Room e sem backend remoto.

Android UI → regras locais → SQLite → PDF / CSV / backup / compartilhamento.

MainActivity.kt mantém telas e fluxos. LocalDatabase.kt mantém persistência,
transações, fotos e IDs da base v2. Models.kt contém os dados das operações locais.
Não há Retrofit, endpoint Apps Script ou permissão INTERNET no aplicativo.
WhatsApp e compartilhamentos são abertos por Intent em outro aplicativo.

O namespace interno com.example.controlevendas foi preservado; applicationId
com.vendassimples.app e banco vendas_simples.db isolam o novo app do AleJoias.
Leia docs/COMERCIAL.md para as decisões e limites desta primeira versão.