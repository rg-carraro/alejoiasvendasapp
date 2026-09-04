package com.example.controlevendas

data class VendaRelatorio(
    val id_venda: Double,
    val id_cliente: String? = null,
    val nome_cliente: String? = null,
    val descricao: String? = null,
    val data_venda: String? = null,
    val data_vencimento: String? = null,
    val data_pagamento: String? = null,
    val valor_total: Double,
    val total_pago: Double,
    val saldo: Double,
    val parcela_atual: Int? = null,
    val parcelas: Int? = null,
    val id_venda_pai: String? = null
)

data class NovaVendaRequest(
    val action: String = "nova_venda",
    val nome_cliente: String,
    val descricao: String,
    val data_venda: String,
    val valor_total: Double,
    val parcelas: Int
)

data class AtualizarVendaRequest(
    val action: String = "atualizar_venda",
    val id_venda: String,
    val nome_cliente: String,
    val descricao: String,
    val data_venda: String,
    val data_vencimento: String,
    val valor_total: Double,
    val parcelas: Int,
    val parcela_atual: Int
)

data class NovoPagamentoRequest(
    val action: String = "novo_pagamento",
    val id_venda: String,
    val data_pagamento: String,
    val valor_pago: Double
)

data class DeletarVendaRequest(
    val action: String = "deletar_venda",
    val id_venda: String
)
