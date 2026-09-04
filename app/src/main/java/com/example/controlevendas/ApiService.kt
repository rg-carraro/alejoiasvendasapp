package com.example.controlevendas

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("exec")
    fun getRelatorio(@Query("action") action: String = "relatorio"): Call<List<VendaRelatorio>>

    @GET("exec")
    fun getSyncData(@Query("action") action: String = "sync_export"): Call<SyncExportResponse>

    @POST("exec")
    fun novaVenda(@Body body: NovaVendaRequest): Call<Map<String, Any>>

    @POST("exec")
    fun atualizarVenda(@Body body: AtualizarVendaRequest): Call<Map<String, Any>>

    @POST("exec")
    fun novoPagamento(@Body body: NovoPagamentoRequest): Call<Map<String, Any>>

    @POST("exec")
    fun deletarVenda(@Body body: DeletarVendaRequest): Call<Map<String, Any>>

    @POST("exec")
    fun uploadSyncData(@Body body: SyncUploadRequest): Call<SyncUploadResponse>
}
