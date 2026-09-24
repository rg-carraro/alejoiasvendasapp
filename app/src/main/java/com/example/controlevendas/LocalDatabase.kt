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
        const val DB_NAME = "vendas_simples.db"
        private const val DB_VERSION = 2
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

        criarTabelaFotos(db)

        // Metadados legados preservados para manter a estrutura SQLite v2. Sem integração remota.
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
        if (oldVersion < 2) criarTabelaFotos(db)
    }

    private fun criarTabelaFotos(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE FOTOS_VENDA (
                id_foto TEXT PRIMARY KEY,
                id_venda_pai TEXT NOT NULL,
                imagem BLOB NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_fotos_venda_pai ON FOTOS_VENDA(id_venda_pai)")
    }

    fun getPrimeiraFotoVenda(idVendaPai: String?): ByteArray? {
        if (idVendaPai.isNullOrBlank()) return null
        readableDatabase.query(
            "FOTOS_VENDA", arrayOf("imagem"), "id_venda_pai=?",
            arrayOf(idVendaPai), null, null, "rowid", "1"
        ).use { c ->
            return if (c.moveToFirst()) c.getBlob(0) else null
        }
    }

    fun getFotosVenda(idVendaPai: String?): List<ByteArray> {
        if (idVendaPai.isNullOrBlank()) return emptyList()
        val fotos = mutableListOf<ByteArray>()
        readableDatabase.query(
            "FOTOS_VENDA", arrayOf("imagem"), "id_venda_pai=?",
            arrayOf(idVendaPai), null, null, "rowid"
        ).use { c ->
            while (c.moveToNext()) fotos += c.getBlob(0)
        }
        return fotos
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

    fun novaVenda(request: NovaVendaRequest, fotos: List<ByteArray>): String {
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
            fotos.forEach { imagem ->
                db.insertOrThrow("FOTOS_VENDA", null, ContentValues().apply {
                    put("id_foto", java.util.UUID.randomUUID().toString())
                    put("id_venda_pai", idPai)
                    put("imagem", imagem)
                })
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
            var idPai: String? = null
            db.query("VENDAS", arrayOf("id_venda_pai"), "id_venda=?", arrayOf(idVenda), null, null, null).use { c ->
                if (c.moveToFirst()) idPai = c.getString(0)
            }
            db.delete("VENDAS", "id_venda=?", arrayOf(idVenda))
            if (!idPai.isNullOrBlank()) {
                db.rawQuery("SELECT 1 FROM VENDAS WHERE id_venda_pai=? LIMIT 1", arrayOf(idPai)).use { c ->
                    if (!c.moveToFirst()) db.delete("FOTOS_VENDA", "id_venda_pai=?", arrayOf(idPai))
                }
            }
            markDeletion(db, "VENDAS", idVenda)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
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
