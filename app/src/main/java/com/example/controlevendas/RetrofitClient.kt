package com.example.controlevendas

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // TROQUE ABAIXO pela URL base do seu Apps Script.
    // Exemplo:
    // URL final: https://script.google.com/macros/s/AKfycbxxxxxxx/exec
    // BASE_URL:  https://script.google.com/macros/s/AKfycbxxxxxxx/
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycby3EN0uTw3a2eYjv9rDQCds0CN0UwVga9OP43izq_RBF-T3AWz89QYUjak-2MT9ghKsKw/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}