package com.example.controlevendas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LocalDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "alejoias_vendas.db"
        private const val DB_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE CLIENTES (
                id_cliente TEXT PRIMARY KEY,
                nome TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE VENDAS (
                id_venda TEXT PRIMARY KEY,
                id_cliente TEXT NOT NULL,
                descricao TEXT,
                data_venda TEXT,
                data_vencimento TEXT,
                valor_total REAL NOT NULL,
                parcela_atual INTEGER NOT NULL DEFAULT 1,
                parcelas INTEGER NOT NULL DEFAULT 1,
                id_venda_pai TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE PAGAMENTOS (
                id_pagamento TEXT PRIMARY KEY,
                id_venda TEXT NOT NULL,
                data_pagamento TEXT,
                valor_pago REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX idx_vendas_cliente ON VENDAS(id_cliente)")
        db.execSQL("CREATE INDEX idx_pagamentos_venda ON PAGAMENTOS(id_venda)")

        // Metadados internos de sincronização. Não fazem parte da planilha.
        db.execSQL("""
            CREATE TABLE SYNC_DIRTY (
                entity_type TEXT NOT NULL,
                entity_id TEXT NOT NULL,
                PRIMARY KEY(entity_type, entity_id)
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE SYNC_DELETIONS (
                entity_type TEXT NOT NULL,
                entity_id TEXT NOT NULL,
                PRIMARY KEY(entity_type, entity_id)
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Primeira versão local. Futuras migrações devem ser adicionadas aqui.
    }

    fun isEmpty(): Boolean {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM VENDAS", null).use { c ->
            return !c.moveToFirst() || c.getInt(0) == 0
        }
    }

    fun getRelatorio(): List<VendaRelatorio> {
        val sql = """
            SELECT v.id_venda, v.id_cliente, c.nome, v.descricao, v.data_venda,
                   v.data_vencimento, v.valor_total, v.parcela_atual, v.parcelas,
                   v.id_venda_pai,
                   COALESCE(SUM(p.valor_pago), 0) AS total_pago,
                   MAX(CASE WHEN TRIM(COALESCE(p.data_pagamento,'')) <> '' THEN p.data_pagamento ELSE NULL END) AS data_pagamento
            FROM VENDAS v
            LEFT JOIN CLIENTES c ON c.id_cliente = v.id_cliente
            LEFT JOIN PAGAMENTOS p ON p.id_venda = v.id_venda
            GROUP BY v.id_venda, v.id_cliente, c.nome, v.descricao, v.data_venda,
                     v.data_vencimento, v.valor_total, v.parcela_atual, v.parcelas, v.id_venda_pai
        """.trimIndent()

        val result = mutableListOf<VendaRelatorio>()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                val valor = c.getDouble(c.getColumnIndexOrThrow("valor_total"))
                val pago = c.getDouble(c.getColumnIndexOrThrow("total_pago"))
                val idTexto = c.getString(c.getColumnIndexOrThrow("id_venda"))
                result += VendaRelatorio(
                    id_venda = idTexto.toDoubleOrNull() ?: 0.0,
                    id_cliente = c.getString(c.getColumnIndexOrThrow("id_cliente")),
                    nome_cliente = c.getString(c.getColumnIndexOrThrow("nome")) ?: "Cliente não encontrado",
                    descricao = c.getString(c.getColumnIndexOrThrow("descricao")),
                    data_venda = c.getString(c.getColumnIndexOrThrow("data_venda")),
                    data_vencimento = c.getString(c.getColumnIndexOrThrow("data_vencimento")),
                    data_pagamento = c.getString(c.getColumnIndexOrThrow("data_pagamento")),
                    valor_total = valor,
                    total_pago = pago,
                    saldo = valor - pago,
                    parcela_atual = c.getInt(c.getColumnIndexOrThrow("parcela_atual")),
                    parcelas = c.getInt(c.getColumnIndexOrThrow("parcelas")),
                    id_venda_pai = c.getString(c.getColumnIndexOrThrow("id_venda_pai"))
                )
            }
        }
        return result
    }

    fun novaVenda(request: NovaVendaRequest): String {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val idCliente = obterOuCriarCliente(db, request.nome_cliente, true)
            val qtd = request.parcelas.coerceAtLeast(1)
            val valorBase = request.valor_total / qtd
            val idPai = "VEN-${System.currentTimeMillis()}"
            var acumulado = 0.0

            for (i in 1..qtd) {
                val idVenda = gerarIdNumerico(db, "VENDAS", "id_venda", i)
                val valorParcela = if (i == qtd) request.valor_total - acumulado else arredondar(valorBase)
                acumulado += valorParcela
                val cv = ContentValues().apply {
                    put("id_venda", idVenda)
                    put("id_cliente", idCliente)
                    put("descricao", request.descricao)
                    put("data_venda", request.data_venda)
                    put("data_vencimento", somarDias(request.data_venda, i * 30))
                    put("valor_total", valorParcela)
                    put("parcela_atual", i)
                    put("parcelas", qtd)
                    put("id_venda_pai", idPai)
                }
                db.insertOrThrow("VENDAS", null, cv)
                markDirty(db, "VENDAS", idVenda)
            }
            db.setTransactionSuccessful()
            return idPai
        } finally {
            db.endTransaction()
        }
    }

    fun atualizarVenda(request: AtualizarVendaRequest) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val idCliente = obterOuCriarCliente(db, request.nome_cliente, true)
            val cv = ContentValues().apply {
                put("id_cliente", idCliente)
                put("descricao", request.descricao)
                put("data_venda", request.data_venda)
                put("data_vencimento", request.data_vencimento)
                put("valor_total", request.valor_total)
                put("parcela_atual", request.parcela_atual)
                put("parcelas", request.parcelas.coerceAtLeast(1))
            }
            db.update("VENDAS", cv, "id_venda=?", arrayOf(request.id_venda))
            markDirty(db, "VENDAS", request.id_venda)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun novoPagamento(request: NovoPagamentoRequest): String {
        val db = writableDatabase
        val id = gerarIdNumerico(db, "PAGAMENTOS", "id_pagamento", 0)
        val cv = ContentValues().apply {
            put("id_pagamento", id)
            put("id_venda", request.id_venda)
            put("data_pagamento", request.data_pagamento)
            put("valor_pago", request.valor_pago)
        }
        db.insertOrThrow("PAGAMENTOS", null, cv)
        markDirty(db, "PAGAMENTOS", id)
        return id
    }

    fun corrigirPagamentoTotal(idVenda: String, dataPagamento: String, valorCorreto: Double) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val idsAntigos = mutableListOf<String>()
            db.query("PAGAMENTOS", arrayOf("id_pagamento"), "id_venda=?", arrayOf(idVenda), null, null, null).use { c ->
                while (c.moveToNext()) idsAntigos += c.getString(0)
            }
            idsAntigos.forEach { markDeletion(db, "PAGAMENTOS", it) }
            db.delete("PAGAMENTOS", "id_venda=?", arrayOf(idVenda))

            if (valorCorreto > 0.0) {
                val novoId = gerarIdNumerico(db, "PAGAMENTOS", "id_pagamento", 0)
                val cv = ContentValues().apply {
                    put("id_pagamento", novoId)
                    put("id_venda", idVenda)
                    put("data_pagamento", dataPagamento)
                    put("valor_pago", valorCorreto)
                }
                db.insertOrThrow("PAGAMENTOS", null, cv)
                markDirty(db, "PAGAMENTOS", novoId)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deletarVenda(idVenda: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val pagamentos = mutableListOf<String>()
            db.query("PAGAMENTOS", arrayOf("id_pagamento"), "id_venda=?", arrayOf(idVenda), null, null, null).use { c ->
                while (c.moveToNext()) pagamentos += c.getString(0)
            }
            pagamentos.forEach { markDeletion(db, "PAGAMENTOS", it) }
            db.delete("PAGAMENTOS", "id_venda=?", arrayOf(idVenda))
            db.delete("VENDAS", "id_venda=?", arrayOf(idVenda))
            markDeletion(db, "VENDAS", idVenda)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun exportSyncData(): SyncExportResponse {
        val clientes = mutableListOf<ClienteSync>()
        val vendas = mutableListOf<VendaSync>()
        val pagamentos = mutableListOf<PagamentoSync>()
        val db = readableDatabase

        db.rawQuery("SELECT id_cliente,nome FROM CLIENTES ORDER BY id_cliente", null).use { c ->
            while (c.moveToNext()) clientes += ClienteSync(c.getString(0), c.getString(1))
        }
        db.rawQuery("SELECT id_venda,id_cliente,descricao,data_venda,data_vencimento,valor_total,parcela_atual,parcelas,id_venda_pai FROM VENDAS ORDER BY id_venda", null).use { c ->
            while (c.moveToNext()) vendas += VendaSync(
                id_venda = c.getString(0), id_cliente = c.getString(1), descricao = c.getString(2) ?: "",
                data_venda = c.getString(3) ?: "", data_vencimento = c.getString(4) ?: "", valor_total = c.getDouble(5),
                parcela_atual = c.getInt(6), parcelas = c.getInt(7), id_venda_pai = c.getString(8) ?: ""
            )
        }
        db.rawQuery("SELECT id_pagamento,id_venda,data_pagamento,valor_pago FROM PAGAMENTOS ORDER BY id_pagamento", null).use { c ->
            while (c.moveToNext()) pagamentos += PagamentoSync(c.getString(0), c.getString(1), c.getString(2) ?: "", c.getDouble(3))
        }
        return SyncExportResponse(true, clientes, vendas, pagamentos)
    }

    fun replaceFromRemote(remote: SyncExportResponse) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("PAGAMENTOS", null, null)
            db.delete("VENDAS", null, null)
            db.delete("CLIENTES", null, null)
            remote.clientes.forEach { upsertCliente(db, it) }
            remote.vendas.forEach { upsertVenda(db, it) }
            remote.pagamentos.forEach { upsertPagamento(db, it) }
            clearSyncMetadata(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Mescla dados remotos sem sobrescrever registros modificados localmente. */
    fun mergeFromRemote(remote: SyncExportResponse) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            remote.clientes.forEach { item -> if (canAcceptRemote(db, "CLIENTES", item.id_cliente)) upsertCliente(db, item) }
            remote.vendas.forEach { item -> if (canAcceptRemote(db, "VENDAS", item.id_venda)) upsertVenda(db, item) }
            remote.pagamentos.forEach { item -> if (canAcceptRemote(db, "PAGAMENTOS", item.id_pagamento)) upsertPagamento(db, item) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun markAllSynced() {
        val db = writableDatabase
        clearSyncMetadata(db)
    }

    fun pendingSyncCount(): Int {
        val db = readableDatabase
        val dirty = scalarCount(db, "SELECT COUNT(*) FROM SYNC_DIRTY")
        val deletions = scalarCount(db, "SELECT COUNT(*) FROM SYNC_DELETIONS")
        return dirty + deletions
    }

    private fun upsertCliente(db: SQLiteDatabase, item: ClienteSync) {
        db.insertWithOnConflict("CLIENTES", null, ContentValues().apply {
            put("id_cliente", item.id_cliente); put("nome", item.nome)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun upsertVenda(db: SQLiteDatabase, item: VendaSync) {
        db.insertWithOnConflict("VENDAS", null, ContentValues().apply {
            put("id_venda", item.id_venda); put("id_cliente", item.id_cliente); put("descricao", item.descricao)
            put("data_venda", item.data_venda); put("data_vencimento", item.data_vencimento); put("valor_total", item.valor_total)
            put("parcela_atual", item.parcela_atual); put("parcelas", item.parcelas); put("id_venda_pai", item.id_venda_pai)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun upsertPagamento(db: SQLiteDatabase, item: PagamentoSync) {
        db.insertWithOnConflict("PAGAMENTOS", null, ContentValues().apply {
            put("id_pagamento", item.id_pagamento); put("id_venda", item.id_venda)
            put("data_pagamento", item.data_pagamento); put("valor_pago", item.valor_pago)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun obterOuCriarCliente(db: SQLiteDatabase, nome: String, markAsDirty: Boolean): String {
        val alvo = normalizar(nome)
        db.rawQuery("SELECT id_cliente,nome FROM CLIENTES", null).use { c ->
            while (c.moveToNext()) {
                if (normalizar(c.getString(1)) == alvo) return c.getString(0)
            }
        }
        val id = "CLI-${System.currentTimeMillis()}"
        db.insertOrThrow("CLIENTES", null, ContentValues().apply { put("id_cliente", id); put("nome", nome.trim()) })
        if (markAsDirty) markDirty(db, "CLIENTES", id)
        return id
    }

    private fun gerarIdNumerico(db: SQLiteDatabase, table: String, col: String, offset: Int): String {
        var candidate = System.currentTimeMillis() + offset
        while (exists(db, table, col, candidate.toString())) candidate++
        return candidate.toString()
    }

    private fun exists(db: SQLiteDatabase, table: String, col: String, id: String): Boolean {
        db.rawQuery("SELECT 1 FROM $table WHERE $col=? LIMIT 1", arrayOf(id)).use { return it.moveToFirst() }
    }

    private fun markDirty(db: SQLiteDatabase, type: String, id: String) {
        db.insertWithOnConflict("SYNC_DIRTY", null, ContentValues().apply {
            put("entity_type", type); put("entity_id", id)
        }, SQLiteDatabase.CONFLICT_REPLACE)
        db.delete("SYNC_DELETIONS", "entity_type=? AND entity_id=?", arrayOf(type, id))
    }

    private fun markDeletion(db: SQLiteDatabase, type: String, id: String) {
        db.insertWithOnConflict("SYNC_DELETIONS", null, ContentValues().apply {
            put("entity_type", type); put("entity_id", id)
        }, SQLiteDatabase.CONFLICT_REPLACE)
        db.delete("SYNC_DIRTY", "entity_type=? AND entity_id=?", arrayOf(type, id))
    }

    private fun canAcceptRemote(db: SQLiteDatabase, type: String, id: String): Boolean {
        val dirty = db.rawQuery("SELECT 1 FROM SYNC_DIRTY WHERE entity_type=? AND entity_id=?", arrayOf(type, id)).use { it.moveToFirst() }
        val deleted = db.rawQuery("SELECT 1 FROM SYNC_DELETIONS WHERE entity_type=? AND entity_id=?", arrayOf(type, id)).use { it.moveToFirst() }
        return !dirty && !deleted
    }

    private fun clearSyncMetadata(db: SQLiteDatabase) {
        db.delete("SYNC_DIRTY", null, null)
        db.delete("SYNC_DELETIONS", null, null)
    }

    private fun scalarCount(db: SQLiteDatabase, sql: String): Int = db.rawQuery(sql, null).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    private fun somarDias(dataTexto: String, dias: Int): String {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date: Date = fmt.parse(dataTexto) ?: return dataTexto
            val cal = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, dias) }
            fmt.format(cal.time)
        } catch (_: Exception) { dataTexto }
    }

    private fun arredondar(v: Double): Double = kotlin.math.round(v * 100.0) / 100.0

    private fun normalizar(text: String): String = Normalizer.normalize(text.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
}
