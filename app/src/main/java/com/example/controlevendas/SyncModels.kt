package com.example.controlevendas

data class ClienteSync(
    val id_cliente: String,
    val nome: String
)

data class VendaSync(
    val id_venda: String,
    val id_cliente: String,
    val descricao: String = "",
    val data_venda: String = "",
    val data_vencimento: String = "",
    val valor_total: Double = 0.0,
    val parcela_atual: Int = 1,
    val parcelas: Int = 1,
    val id_venda_pai: String = ""
)

data class PagamentoSync(
    val id_pagamento: String,
    val id_venda: String,
    val data_pagamento: String = "",
    val valor_pago: Double = 0.0
)

data class SyncExportResponse(
    val ok: Boolean = true,
    val clientes: List<ClienteSync> = emptyList(),
    val vendas: List<VendaSync> = emptyList(),
    val pagamentos: List<PagamentoSync> = emptyList(),
    val erro: String? = null
)

data class SyncUploadRequest(
    val action: String = "sync_upload",
    val clientes: List<ClienteSync>,
    val vendas: List<VendaSync>,
    val pagamentos: List<PagamentoSync>
)

data class SyncUploadResponse(
    val ok: Boolean = false,
    val status: String? = null,
    val clientes: Int = 0,
    val vendas: Int = 0,
    val pagamentos: Int = 0,
    val erro: String? = null
)
