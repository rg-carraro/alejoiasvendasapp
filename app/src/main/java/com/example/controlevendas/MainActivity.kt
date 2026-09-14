package com.example.controlevendas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Paleta visual AleJoias.
    private val corFundo = Color.rgb(247, 243, 236)
    private val corSuperficie = Color.rgb(255, 253, 249)
    private val corPrimaria = Color.rgb(20, 90, 74)
    private val corPrimariaEscura = Color.rgb(11, 61, 51)
    private val corDestaque = Color.rgb(184, 138, 68)
    private val corTexto = Color.rgb(39, 49, 46)
    private val corTextoSecundario = Color.rgb(100, 108, 104)
    private val corBorda = Color.rgb(222, 211, 194)
    private val corQuitado = Color.rgb(226, 244, 235)
    private val corVencido = Color.rgb(253, 232, 229)

    private lateinit var content: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var localDb: LocalDatabase

    private var vendasCache: List<VendaRelatorio> = emptyList()
    private var telaAtual = "menu"
    private var mesResumoSelecionado: String? = null
    private var mesDashboardSelecionado: String? = null

    private var filtroInicio: String? = null
    private var filtroFim: String? = null
    private var filtrarAReceber = false
    private var filtroCliente: String = ""
    private var filtroClienteResumo: String = ""
    private var pesquisaGlobal: String = ""

    private var relatorioInicio: String? = null
    private var relatorioFim: String? = null

    private val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val handler = Handler(Looper.getMainLooper())

    private val hoje: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR")).format(Date())

    private val mesAtual: String
        get() = SimpleDateFormat("yyyy-MM", Locale("pt", "BR")).format(Date())

    private val autoRefreshRunnable = object : Runnable {
        override fun run() {
            carregarRelatorio {
                when (telaAtual) {
                    "vendas" -> abrirListaVendas()
                    "resumo" -> abrirResumo()
                    "dashboard" -> abrirDashboardFinanceiro()
                    "resumo_clientes" -> abrirResumoClientes()
                    "resumo_periodo" -> abrirResumoPeriodo()
                    else -> abrirMenuPrincipal()
                }
            }
            handler.postDelayed(this, 5 * 60 * 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        montarTela()
        criarCanalNotificacoes()
        localDb = LocalDatabase(this)
        val bancoVazio = localDb.isEmpty()
        carregarRelatorio {
            abrirMenuPrincipal()
            if (bancoVazio) sugerirImportacaoInicial()
        }
        handler.postDelayed(autoRefreshRunnable, 5 * 60 * 1000L)
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoRefreshRunnable)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (telaAtual != "menu") abrirMenuPrincipal() else super.onBackPressed()
    }

    private fun montarTela() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(corFundo)
        }

        val watermark = ImageView(this).apply {
            setImageResource(R.drawable.watermark_alejoias)
            alpha = 0.38f
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Marca AleJoias"
        }

        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(16))
            setBackgroundColor(Color.TRANSPARENT)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(10), dp(10), dp(12))
            background = fundoArredondadoComBorda(corSuperficie, 28f, corBorda)
        }

        val titulo = TextView(this).apply {
            text = "AleJoias Vendas"
            textSize = if (resources.displayMetrics.widthPixels < 900) 24f else 29f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(corPrimariaEscura)
        }

        val subtitulo = TextView(this).apply {
            text = "ERP Simples De Vendas"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(corDestaque)
        }

        header.addView(titulo)
        header.addView(subtitulo)

        statusText = TextView(this).apply {
            text = ""
            textSize = 14f
            setTextColor(corTextoSecundario)
            setPadding(dp(4), dp(12), dp(4), dp(8))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = false
            clipToPadding = false
            setPadding(0, 0, 0, dp(96))
            setBackgroundColor(Color.TRANSPARENT)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(96))
            setBackgroundColor(Color.TRANSPARENT)
        }

        scroll.addView(content)

        mainContainer.addView(
            watermark,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(96)
            ).apply {
                setMargins(dp(24), 0, dp(24), dp(6))
            }
        )

        mainContainer.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(8))
            }
        )

        mainContainer.addView(statusText)

        mainContainer.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            mainContainer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    private fun abrirMenuPrincipal() {
        telaAtual = "menu"
        content.removeAllViews()
        statusText.text = "Menu Principal"

        val linha1 = linhaBotoes()
        linha1.addView(botaoQuadrado("Vendas", "Lista", 1f) { abrirListaVendas() })
        linha1.addView(botaoQuadrado("Nova", "Venda", 1f) { abrirDialogVenda(null) })
        content.addView(linha1)

        val linha2 = linhaBotoes()
        linha2.addView(botaoQuadrado("Resumo", "Relatórios", 1f) { abrirResumo() })
        linha2.addView(botaoQuadrado("Clientes", "Histórico", 1f) { abrirResumoClientes() })
        content.addView(linha2)

        val linha3 = linhaBotoes()
        linha3.addView(botaoQuadrado("Busca", "Global", 1f) { abrirBuscaGlobal() })
        linha3.addView(botaoQuadrado("Dados", "Sync", 1f) { abrirBackupLocal() })
        content.addView(linha3)

        val linhaExtra2 = linhaBotoes()
        linhaExtra2.addView(botaoQuadrado("Dashboard", "Financeiro", 1f) { abrirDashboardFinanceiro() })
        content.addView(linhaExtra2)
    }

    private fun adicionarDashboardCompacto() {
        val vendasMes = vendasCache.filter { (it.data_venda ?: "").startsWith(mesAtual) }
        val totalReceber = vendasCache.sumOf { it.saldo }
        val vencidas = vendasCache.count { estaVencida(it) }
        val recebidasMes = vendasMes.sumOf { it.total_pago }

        val linha1 = linhaBotoes()
        linha1.addView(cardDashboard("Recebido Mês", moeda.format(recebidasMes), 1f))
        linha1.addView(cardDashboard("A Receber", moeda.format(totalReceber), 1f))
        content.addView(linha1)

        val linha2 = linhaBotoes()
        linha2.addView(cardDashboard("Cards Vencidos", vencidas.toString(), 1f))
        linha2.addView(cardDashboard("Cards Totais", vendasCache.size.toString(), 1f))
        content.addView(linha2)
    }

    private fun vendasFiltradas(): List<VendaRelatorio> {
        return vendasCache.filter { venda ->
            val data = venda.data_venda ?: ""
            val okInicio = filtroInicio?.let { data >= it } ?: true
            val okFim = filtroFim?.let { data <= it } ?: true
            val okReceber = if (filtrarAReceber) venda.saldo > 0.0 else true
            val okCliente = if (filtroCliente.isBlank()) true else (venda.nome_cliente ?: "").contains(filtroCliente, ignoreCase = true)
            okInicio && okFim && okReceber && okCliente
        }
    }

    private fun abrirListaVendas() {
        telaAtual = "vendas"
        content.removeAllViews()
        val lista = vendasFiltradas().sortedByDescending { it.data_vencimento ?: "" }
        statusText.text = "Lista De Vendas | ${lista.size} Cards"

        content.addView(botaoVoltar("Voltar Ao Menu") { abrirMenuPrincipal() })

        val pesquisaCliente = campo("Pesquisar Cliente").apply {
            setText(filtroCliente)
            setSingleLine(true)
            setOnEditorActionListener { _, _, _ ->
                filtroCliente = text.toString()
                abrirListaVendas()
                true
            }
        }
        content.addView(campoRotulado("Cliente:", pesquisaCliente), margemCard())

        content.addView(botaoVoltar("Aplicar Pesquisa") {
            filtroCliente = pesquisaCliente.text.toString()
            abrirListaVendas()
        })

        val filtros = linhaBotoes()
        filtros.addView(botaoQuadrado("Data", "Filtrar", 1f) { abrirFiltroPeriodoVendas() })
        filtros.addView(botaoQuadrado(if (filtrarAReceber) "Todos" else "A Receber", "Cards", 1f) {
            filtrarAReceber = !filtrarAReceber
            abrirListaVendas()
        })
        content.addView(filtros)

        if (filtroInicio != null || filtroFim != null || filtrarAReceber) {
            content.addView(botaoVoltar("Limpar Filtros") {
                filtroInicio = null
                filtroFim = null
                filtrarAReceber = false
                filtroCliente = ""
                abrirListaVendas()
            })
            content.addView(texto("Filtro: ${filtroInicio ?: "..."} até ${filtroFim ?: "..."}", 13f, false))
        }

        if (lista.isEmpty()) {
            content.addView(texto("Nenhuma Venda Encontrada.", 16f, false))
            return
        }

        lista.forEach { adicionarCardVenda(it) }
    }

    private fun abrirFiltroPeriodoVendas() {
        abrirDialogPeriodo("Filtrar Por Data Da Venda", filtroInicio, filtroFim) { inicio, fim ->
            filtroInicio = inicio
            filtroFim = fim
            abrirListaVendas()
        }
    }

    private fun abrirDialogPeriodo(titulo: String, inicioAtual: String?, fimAtual: String?, onAplicar: (String?, String?) -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(8), dp(28), dp(8))
        }

        val dataInicio = campo("Data Inicial").apply {
            setText(inicioAtual ?: "")
            isFocusable = false
            isClickable = true
            setOnClickListener { abrirCalendario(this) }
        }

        val dataFim = campo("Data Final").apply {
            setText(fimAtual ?: "")
            isFocusable = false
            isClickable = true
            setOnClickListener { abrirCalendario(this) }
        }

        layout.addView(campoRotulado("Data inicial:", dataInicio))
        layout.addView(campoRotulado("Data final:", dataFim))

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(layout)
            .setPositiveButton("Aplicar") { _, _ ->
                onAplicar(
                    dataInicio.text.toString().takeIf { it.isNotBlank() },
                    dataFim.text.toString().takeIf { it.isNotBlank() }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun adicionarCardVenda(venda: VendaRelatorio) {
        val nome = venda.nome_cliente?.takeIf { it.isNotBlank() } ?: "Cliente Não Informado"
        val quitado = venda.total_pago >= venda.valor_total
        val vencido = estaVencida(venda)

        val corCard = when {
            quitado -> corQuitado
            vencido -> corVencido
            else -> corSuperficie
        }

        val parcelaInfo = if ((venda.parcelas ?: 1) > 1) {
            "Parcela ${venda.parcela_atual ?: 1}/${venda.parcelas ?: 1}"
        } else {
            "À Vista"
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = fundoArredondadoComBorda(corCard, 20f, corBorda)
            isClickable = true
            setOnClickListener { abrirOpcoesCard(venda) }
        }

        card.addView(texto(nome, 17f, true))
        card.addView(etiquetaStatus(venda))
        card.addView(TextView(this).apply {
            text = "$parcelaInfo\n" +
                    "Compra: ${venda.data_venda ?: "-"}\n" +
                    "Vencimento: ${venda.data_vencimento ?: "-"}\n" +
                    "Valor: ${moeda.format(venda.valor_total)}\n" +
                    "Pago: ${moeda.format(venda.total_pago)}\n" +
                    (if (venda.total_pago > 0.0) "Data Pagamento: ${dataPagamentoLocal(venda)}\n" else "") +
                    "Faltante: ${moeda.format(venda.saldo)}"
            textSize = 13f
            setTextColor(corTextoSecundario)
            setPadding(0, dp(6), 0, 0)
        })

        content.addView(card, margemCard())
    }

    private fun estaVencida(venda: VendaRelatorio): Boolean {
        val vencimento = venda.data_vencimento ?: return false
        val quitado = venda.total_pago >= venda.valor_total
        return !quitado && vencimento < hoje
    }

    private fun etiquetaStatus(venda: VendaRelatorio): TextView {
        val quitado = venda.total_pago >= venda.valor_total
        val vencido = estaVencida(venda)
        return TextView(this).apply {
            text = when {
                quitado -> "QUITADO"
                vencido -> "VENCIDO"
                venda.total_pago > 0.0 -> "PAGO PARCIALMENTE"
                else -> "EM ABERTO"
            }
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(
                when {
                    quitado -> corPrimariaEscura
                    vencido -> Color.rgb(153, 55, 47)
                    else -> Color.rgb(125, 87, 32)
                }
            )
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(4), dp(10), dp(4))
            background = fundoArredondado(
                when {
                    quitado -> Color.rgb(197, 231, 214)
                    vencido -> Color.rgb(248, 205, 199)
                    else -> Color.rgb(244, 229, 197)
                },
                18f
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp(7), 0, dp(2)) }
        }
    }

    private fun abrirOpcoesCard(venda: VendaRelatorio) {
        lateinit var dialog: AlertDialog
        val opcoes = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(8), dp(14), dp(8))
        }

        fun adicionarOpcao(texto: String, destrutiva: Boolean = false, acao: () -> Unit) {
            opcoes.addView(botaoOpcaoMenu(texto, destrutiva) {
                dialog.dismiss()
                acao()
            })
        }

        adicionarOpcao("Ver detalhes") { abrirDetalhesVenda(venda) }
        adicionarOpcao("Editar card") { abrirDialogVenda(venda) }
        adicionarOpcao("Registrar pagamento") { abrirDialogPagamento(venda) }
        adicionarOpcao("Cobrar via WhatsApp") { cobrarViaWhatsApp(venda) }
        adicionarOpcao("Corrigir valor pago") { abrirDialogCorrigirPagamento(venda) }
        adicionarOpcao("Deletar card", destrutiva = true) { confirmarDeletarCard(venda) }

        dialog = AlertDialog.Builder(this)
            .setTitle(venda.nome_cliente ?: "Venda")
            .setView(opcoes)
            .setNegativeButton("Fechar", null)
            .create()
        dialog.show()
    }

    private fun abrirDetalhesVenda(venda: VendaRelatorio) {
        val painel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(10))
            background = fundoArredondadoComBorda(corSuperficie, 22f, corBorda)
        }

        painel.addView(texto(venda.nome_cliente ?: "Cliente não informado", 20f, true).apply {
            setTextColor(corPrimariaEscura)
        })
        painel.addView(etiquetaStatus(venda))
        painel.addView(linhaDetalhe("Descrição", venda.descricao ?: "-"))
        painel.addView(linhaDetalhe("Data da compra", venda.data_venda ?: "-"))
        painel.addView(linhaDetalhe("Vencimento", venda.data_vencimento ?: "-"))
        painel.addView(linhaDetalhe("Parcela", "${venda.parcela_atual ?: 1}/${venda.parcelas ?: 1}"))
        painel.addView(linhaDetalhe("Valor da venda", moeda.format(venda.valor_total), true))
        painel.addView(linhaDetalhe("Valor pago", moeda.format(venda.total_pago), true))
        painel.addView(linhaDetalhe("Saldo faltante", moeda.format(venda.saldo), true))

        if (venda.total_pago > 0.0) {
            painel.addView(linhaDetalhe("Data do pagamento", dataPagamentoLocal(venda)))
        }

        AlertDialog.Builder(this)
            .setTitle("Detalhes Da Venda")
            .setView(painel)
            .setPositiveButton("Registrar Pagamento") { _, _ -> abrirDialogPagamento(venda) }
            .setNegativeButton("Fechar", null)
            .show()
    }

    private fun linhaDetalhe(rotulo: String, valor: String, destaque: Boolean = false): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(9), 0, dp(8))

            addView(texto("$rotulo:", 12f, true).apply {
                setTextColor(corTextoSecundario)
            })
            addView(texto(valor, if (destaque) 17f else 15f, destaque).apply {
                setTextColor(if (destaque) corPrimariaEscura else corTexto)
            })

            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(0, Color.TRANSPARENT)
            }
        }

    private fun painelMensagem(mensagem: String, aviso: Boolean = false): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
            background = fundoArredondadoComBorda(
                if (aviso) corVencido else corSuperficie,
                20f,
                if (aviso) Color.rgb(230, 181, 174) else corBorda
            )
            addView(texto(mensagem, 14f, false).apply {
                setTextColor(if (aviso) Color.rgb(117, 48, 43) else corTexto)
                setLineSpacing(0f, 1.15f)
            })
        }

    
private fun abrirDashboardFinanceiro() {
        telaAtual = "dashboard"
        content.removeAllViews()
        val mesSelecionado = mesDashboardSelecionado ?: mesAtual
        val vendasDashboard = vendasCache.filter { (it.data_venda ?: "").startsWith(mesSelecionado) }
        statusText.text = "Dashboard Financeiro | $mesSelecionado"

        content.addView(botaoVoltar("Voltar Ao Menu") { abrirMenuPrincipal() })
        content.addView(botaoVoltar("Selecionar Outro Mês") { abrirSelecionarMesDashboard() })
        adicionarCardResumo("Mês Selecionado", mesSelecionado)

        val totalVendido = vendasDashboard.sumOf { it.valor_total }
        val totalRecebido = vendasDashboard.sumOf { it.total_pago }
        val totalReceber = vendasDashboard.sumOf { it.saldo }
        val clientesDebito = vendasDashboard
            .filter { it.saldo > 0.0 }
            .mapNotNull { it.nome_cliente }
            .distinct()
            .size
        val vendasVencidas = vendasDashboard.count { estaVencida(it) }

        val linha1 = linhaBotoes()
        linha1.addView(cardDashboard("Total Vendido", moeda.format(totalVendido), 1f))
        linha1.addView(cardDashboard("Total Recebido", moeda.format(totalRecebido), 1f))
        content.addView(linha1)

        val linha2 = linhaBotoes()
        linha2.addView(cardDashboard("A Receber", moeda.format(totalReceber), 1f))
        linha2.addView(cardDashboard("Clientes Débito", clientesDebito.toString(), 1f))
        content.addView(linha2)

        val linha3 = linhaBotoes()
        linha3.addView(cardDashboard("Vencidas", vendasVencidas.toString(), 1f))
        linha3.addView(cardDashboard("Cards Do Mês", vendasDashboard.size.toString(), 1f))
        content.addView(linha3)

        adicionarCardResumo("Vendido No Mês", moeda.format(totalVendido))
        adicionarCardResumo("Recebido No Mês", moeda.format(totalRecebido))
        adicionarCardResumo("Faltante No Mês", moeda.format(totalReceber))

        adicionarGraficoDashboard(totalVendido, totalRecebido, totalReceber)
    }

    private fun abrirSelecionarMesDashboard() {
        val mesesDisponiveis = vendasCache
            .mapNotNull { it.data_venda }
            .filter { it.length >= 7 }
            .map { it.substring(0, 7) }
            .distinct()
            .sortedDescending()

        if (mesesDisponiveis.isEmpty()) {
            Toast.makeText(this, "Nenhum mês encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Mês Do Dashboard")
            .setItems(mesesDisponiveis.toTypedArray()) { _, which ->
                mesDashboardSelecionado = mesesDisponiveis[which]
                abrirDashboardFinanceiro()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cardDashboard(titulo: String, valor: String, peso: Float): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(12), dp(10), dp(12))
            background = fundoArredondadoComBorda(corSuperficie, 24f, corBorda)
        }

        card.addView(texto(titulo, 12f, false).apply {
            gravity = Gravity.CENTER
            setTextColor(corTextoSecundario)
        })

        card.addView(texto(valor, if (resources.displayMetrics.widthPixels < 900) 14f else 16f, true).apply {
            gravity = Gravity.CENTER
        })

        val params = LinearLayout.LayoutParams(0, dp(86), peso)
        params.setMargins(dp(5), dp(5), dp(5), dp(5))
        card.layoutParams = params
        return card
    }



    private fun abrirResumo() {
        telaAtual = "resumo"
        content.removeAllViews()
        statusText.text = "Resumo Financeiro"

        content.addView(botaoVoltar("Voltar Ao Menu") { abrirMenuPrincipal() })

        val linha = linhaBotoes()
        linha.addView(botaoQuadrado("Resumo", "Período", 1f) { abrirFiltroResumoPeriodo() })
        linha.addView(botaoQuadrado("Resumo", "Clientes", 1f) { abrirResumoClientes() })
        content.addView(linha)

        adicionarCardResumo("Quantidade De Cards", vendasCache.size.toString())
        adicionarCardResumo("Cards Em Aberto", vendasCache.count { it.saldo > 0.0 }.toString())
        adicionarCardResumo("Cards Vencidos", vendasCache.count { estaVencida(it) }.toString())
        adicionarCardResumo("Cards Quitados", vendasCache.count { it.total_pago >= it.valor_total && it.valor_total > 0.0 }.toString())
        adicionarCardResumo("Total Vendido", moeda.format(vendasCache.sumOf { it.valor_total }))
        adicionarCardResumo("Total Recebido", moeda.format(vendasCache.sumOf { it.total_pago }))
        adicionarCardResumo("Saldo Faltante Total", moeda.format(vendasCache.sumOf { it.saldo }))

        content.addView(botaoVoltar("Ver Resumo Por Mês") { abrirResumoMes() })
        content.addView(botaoVoltar("Exportar Excel/CSV") { exportarCsvResumo(vendasCache, "resumo_alejoias.csv") })
        content.addView(botaoVoltar("Gerar PDF Do Resumo") { gerarPdfResumo(vendasCache, "resumo_alejoias.pdf") })
    }

    private fun abrirFiltroResumoPeriodo() {
        abrirDialogPeriodo("Relatório Por Período", relatorioInicio, relatorioFim) { inicio, fim ->
            relatorioInicio = inicio
            relatorioFim = fim
            abrirResumoPeriodo()
        }
    }

    private fun abrirResumoPeriodo() {
        telaAtual = "resumo_periodo"
        content.removeAllViews()
        statusText.text = "Resumo Por Período"

        content.addView(botaoVoltar("Voltar Ao Resumo") { abrirResumo() })
        content.addView(botaoVoltar("Alterar Período") { abrirFiltroResumoPeriodo() })

        val lista = vendasCache.filter { venda ->
            val data = venda.data_venda ?: ""
            val okInicio = relatorioInicio?.let { data >= it } ?: true
            val okFim = relatorioFim?.let { data <= it } ?: true
            okInicio && okFim
        }

        content.addView(botaoVoltar("Enviar Imagem Pelo WhatsApp") {
            compartilharResumoPeriodoWhatsApp(lista)
        })

        content.addView(texto("Período: ${relatorioInicio ?: "..."} até ${relatorioFim ?: "..."}", 15f, true))
        adicionarCardResumo("Quantidade De Cards", lista.size.toString())
        adicionarCardResumo("Cards Vencidos", lista.count { estaVencida(it) }.toString())
        adicionarCardResumo("Total Vendido", moeda.format(lista.sumOf { it.valor_total }))
        adicionarCardResumo("Total Recebido", moeda.format(lista.sumOf { it.total_pago }))
        adicionarCardResumo("Saldo Faltante", moeda.format(lista.sumOf { it.saldo }))
    }

    
private fun abrirResumoMes() {
        telaAtual = "resumo_mes"
        content.removeAllViews()
        statusText.text = "Resumo Por Mês"

        content.addView(botaoVoltar("Voltar Ao Resumo") { abrirResumo() })

        val mesSelecionado = mesResumoSelecionado ?: mesAtual
        val vendasMes = vendasCache.filter { (it.data_venda ?: "").startsWith(mesSelecionado) }

        content.addView(botaoVoltar("Selecionar Outro Mês") { abrirSelecionarMesResumo() })

        adicionarCardResumo("Mês Selecionado", mesSelecionado)
        adicionarCardResumo("Quantidade De Cards", vendasMes.size.toString())
        adicionarCardResumo("Cards Em Aberto", vendasMes.count { it.saldo > 0.0 }.toString())
        adicionarCardResumo("Cards Vencidos", vendasMes.count { estaVencida(it) }.toString())
        adicionarCardResumo("Cards Quitados", vendasMes.count { it.total_pago >= it.valor_total && it.valor_total > 0.0 }.toString())
        adicionarCardResumo("Total Vendido No Mês", moeda.format(vendasMes.sumOf { it.valor_total }))
        adicionarCardResumo("Total Recebido No Mês", moeda.format(vendasMes.sumOf { it.total_pago }))
        adicionarCardResumo("Faltante Do Mês", moeda.format(vendasMes.sumOf { it.saldo }))

        content.addView(botaoVoltar("Gerar PDF Deste Mês") {
            gerarPdfResumo(vendasMes, "resumo_${mesSelecionado}.pdf")
        })
        content.addView(botaoVoltar("Exportar CSV Deste Mês") {
            exportarCsvResumo(vendasMes, "resumo_${mesSelecionado}.csv")
        })
    }

    private fun abrirSelecionarMesResumo() {
        val mesesDisponiveis = vendasCache
            .mapNotNull { it.data_venda }
            .filter { it.length >= 7 }
            .map { it.substring(0, 7) }
            .distinct()
            .sortedDescending()

        if (mesesDisponiveis.isEmpty()) {
            Toast.makeText(this, "Nenhum mês encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Selecionar Mês")
            .setItems(mesesDisponiveis.toTypedArray()) { _, which ->
                mesResumoSelecionado = mesesDisponiveis[which]
                abrirResumoMes()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun abrirResumoClientes() {
        telaAtual = "resumo_clientes"
        content.removeAllViews()
        statusText.text = "Resumo Por Cliente"

        content.addView(botaoVoltar("Voltar Ao Resumo") { abrirResumo() })

        val pesquisaCliente = campo("Pesquisar Cliente").apply {
            setText(filtroClienteResumo)
            setSingleLine(true)
            setOnEditorActionListener { _, _, _ ->
                filtroClienteResumo = text.toString()
                abrirResumoClientes()
                true
            }
        }
        content.addView(campoRotulado("Cliente:", pesquisaCliente), margemCard())

        content.addView(botaoVoltar("Aplicar Pesquisa") {
            filtroClienteResumo = pesquisaCliente.text.toString()
            abrirResumoClientes()
        })

        if (filtroClienteResumo.isNotBlank()) {
            content.addView(botaoVoltar("Limpar Pesquisa") {
                filtroClienteResumo = ""
                abrirResumoClientes()
            })
        }

        val agrupado = vendasCache
            .filter {
                filtroClienteResumo.isBlank() ||
                        (it.nome_cliente ?: "").contains(filtroClienteResumo, ignoreCase = true)
            }
            .groupBy {
                it.nome_cliente?.takeIf { nome -> nome.isNotBlank() } ?: "Cliente Não Informado"
            }
            .toSortedMap()

        if (agrupado.isEmpty()) {
            content.addView(texto("Nenhum Cliente Encontrado.", 16f, false))
            return
        }

        agrupado.forEach { (cliente, vendas) ->
            val vendido = vendas.sumOf { it.valor_total }
            val recebido = vendas.sumOf { it.total_pago }
            val faltante = vendas.sumOf { it.saldo }
            val temVencida = vendas.any { estaVencida(it) }
            val tudoQuitado = vendas.all { it.total_pago >= it.valor_total }
            val cor = when {
                temVencida -> corVencido
                tudoQuitado -> corQuitado
                else -> corSuperficie
            }

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(22), dp(18), dp(22), dp(18))
                background = fundoArredondadoComBorda(cor, 26f, corBorda)
                isClickable = true
                setOnClickListener { abrirHistoricoCliente(cliente, vendas) }
            }

            card.addView(texto(cliente, 19f, true))
            card.addView(texto("Cards: ${vendas.size}", 14f, false))
            card.addView(texto("Vendido: ${moeda.format(vendido)}", 15f, false))
            card.addView(texto("Recebido: ${moeda.format(recebido)}", 15f, false))
            card.addView(texto("Faltante: ${moeda.format(faltante)}", 16f, true))
            content.addView(card, margemCard())
        }
    }

    

private fun abrirHistoricoCliente(cliente: String, vendas: List<VendaRelatorio>) {
        telaAtual = "resumo_clientes"
        content.removeAllViews()
        statusText.text = "Histórico Do Cliente"

        content.addView(botaoVoltar("Voltar Ao Resumo Por Cliente") { abrirResumoClientes() })

        val totalVendido = vendas.sumOf { it.valor_total }
        val totalRecebido = vendas.sumOf { it.total_pago }
        val totalFaltante = vendas.sumOf { it.saldo }

        val resumo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
            background = fundoArredondadoComBorda(corSuperficie, 24f, corBorda)
        }

        resumo.addView(texto(cliente, 20f, true))
        resumo.addView(texto("Total Vendido: ${moeda.format(totalVendido)}", 15f, false))
        resumo.addView(texto("Total Recebido: ${moeda.format(totalRecebido)}", 15f, false))
        resumo.addView(texto("Faltante: ${moeda.format(totalFaltante)}", 16f, true))
        content.addView(resumo, margemCard())

        content.addView(botaoVoltar("Gerar Extrato PDF") {
            gerarPdfCliente(cliente, vendas)
        })
        content.addView(botaoVoltar("Exportar Extrato CSV") {
            exportarCsvResumo(vendas, "extrato_${cliente.replace(" ", "_")}.csv")
        })

        vendas.sortedWith(compareBy<VendaRelatorio> { it.data_venda ?: "" }.thenBy { it.data_vencimento ?: "" })
            .forEach { venda ->
                adicionarCardHistoricoCliente(venda)
            }
    }

    private fun adicionarCardHistoricoCliente(venda: VendaRelatorio) {
        val quitado = venda.total_pago >= venda.valor_total
        val vencido = estaVencida(venda)

        val corCard = when {
            quitado -> corQuitado
            vencido -> corVencido
            else -> corSuperficie
        }

        val parcelaInfo = if ((venda.parcelas ?: 1) > 1) {
            "Parcela ${venda.parcela_atual ?: 1}/${venda.parcelas ?: 1}"
        } else {
            "À Vista"
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
            background = fundoArredondadoComBorda(corCard, 24f, corBorda)
            isClickable = true
            setOnClickListener { abrirDetalhesVenda(venda) }
        }

        val descricao = venda.descricao?.takeIf { it.isNotBlank() } ?: "Venda"
        val compra = venda.data_venda ?: "-"
        val vencimento = venda.data_vencimento ?: "-"

        card.addView(texto(descricao, 18f, true))
        card.addView(etiquetaStatus(venda))

        val detalhes = TextView(this).apply {
            text = parcelaInfo +
                    "\nCompra: " + compra +
                    "\nVencimento: " + vencimento +
                    "\nValor: " + moeda.format(venda.valor_total) +
                    "\nPago: " + moeda.format(venda.total_pago) +
                    "\nFaltante: " + moeda.format(venda.saldo)
            textSize = 15f
            setTextColor(corTextoSecundario)
            setPadding(0, dp(6), 0, 0)
        }

        card.addView(detalhes)
        content.addView(card, margemCard())
    }

    
private fun cobrarViaWhatsApp(venda: VendaRelatorio) {
        val cliente = venda.nome_cliente?.takeIf { it.isNotBlank() } ?: "Cliente"
        val vencimento = venda.data_vencimento ?: "-"
        val saldo = venda.saldo

        if (saldo <= 0.0) {
            Toast.makeText(this, "Venda já está quitada.", Toast.LENGTH_SHORT).show()
            return
        }

        val mensagem = "Olá, $cliente.\n\n" +
                "Identificamos um valor pendente referente à sua compra na AleJoias.\n\n" +
                "Valor em aberto: ${moeda.format(saldo)}\n" +
                "Vencimento: $vencimento\n\n" +
                "Caso já tenha efetuado o pagamento, por favor desconsidere esta mensagem.\n\n" +
                "Obrigado!\nAleJoias"

        val uri = Uri.parse("https://wa.me/?text=" + Uri.encode(mensagem))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }



    private fun adicionarGraficoDashboard(totalVendido: Double, totalRecebido: Double, totalReceber: Double) {
        val maxValor = maxOf(totalVendido, totalRecebido, totalReceber, 1.0)

        fun barra(label: String, valor: Double): LinearLayout {
            val wrapper = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(8), dp(12), dp(8))
                background = fundoArredondadoComBorda(corSuperficie, 22f, corBorda)
            }

            wrapper.addView(texto("$label: ${moeda.format(valor)}", 13f, true))

            val fundo = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                background = fundoArredondado(Color.rgb(232, 226, 214), 12f)
            }

            val largura = ((resources.displayMetrics.widthPixels - dp(80)) * (valor / maxValor)).toInt().coerceAtLeast(dp(12))
            val barra = LinearLayout(this).apply {
                background = fundoArredondado(corPrimaria, 12f)
            }

            fundo.addView(barra, LinearLayout.LayoutParams(largura, dp(16)))
            wrapper.addView(fundo, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(16)).apply {
                setMargins(0, dp(6), 0, 0)
            })

            return wrapper
        }

        content.addView(texto("Gráfico Financeiro", 18f, true).apply {
            setPadding(0, dp(12), 0, dp(4))
        })
        content.addView(barra("Vendido", totalVendido), margemCard())
        content.addView(barra("Recebido", totalRecebido), margemCard())
        content.addView(barra("A Receber", totalReceber), margemCard())
    }

    private fun abrirBuscaGlobal() {
        telaAtual = "busca_global"
        content.removeAllViews()
        statusText.text = "Busca Global"

        content.addView(botaoVoltar("Voltar Ao Menu") { abrirMenuPrincipal() })

        val campoBusca = campo("Pesquisar Cliente, Descrição, Data Ou Valor").apply {
            setText(pesquisaGlobal)
            setSingleLine(true)
            setOnEditorActionListener { _, _, _ ->
                pesquisaGlobal = text.toString()
                abrirBuscaGlobal()
                true
            }
        }

        content.addView(campoRotulado("Pesquisa:", campoBusca), margemCard())
        content.addView(botaoVoltar("Pesquisar") {
            pesquisaGlobal = campoBusca.text.toString()
            abrirBuscaGlobal()
        })

        if (pesquisaGlobal.isBlank()) {
            content.addView(texto("Digite algo para pesquisar.", 16f, false))
            return
        }

        val termo = pesquisaGlobal.trim()
        val resultados = vendasCache.filter { venda ->
            (venda.nome_cliente ?: "").contains(termo, ignoreCase = true) ||
                    (venda.descricao ?: "").contains(termo, ignoreCase = true) ||
                    (venda.data_venda ?: "").contains(termo, ignoreCase = true) ||
                    (venda.data_vencimento ?: "").contains(termo, ignoreCase = true) ||
                    moeda.format(venda.valor_total).contains(termo, ignoreCase = true)
        }.sortedByDescending { it.data_vencimento ?: "" }

        content.addView(texto("Resultado: ${resultados.size} cards", 15f, true))

        if (resultados.isEmpty()) {
            content.addView(texto("Nenhum resultado encontrado.", 16f, false))
            return
        }

        resultados.forEach { adicionarCardVenda(it) }
    }

    private fun abrirBackupLocal() {
        telaAtual = "backup"
        content.removeAllViews()
        statusText.text = "Dados Locais E Sincronização"

        content.addView(botaoVoltar("Voltar Ao Menu") { abrirMenuPrincipal() })
        adicionarCardResumo("Cards No SQLite", vendasCache.size.toString())
        adicionarCardResumo("Alterações Pendentes", localDb.pendingSyncCount().toString())

        content.addView(botaoVoltar("Importar Planilha → SQLite") {
            confirmarImportacaoPlanilha()
        })

        content.addView(botaoVoltar("Enviar SQLite → Planilha") {
            confirmarEnvioPlanilha()
        })

        content.addView(botaoVoltar("Sincronizar / Mesclar") {
            confirmarSincronizacao()
        })

        content.addView(botaoVoltar("Exportar CSV Para Excel") {
            exportarCsvResumo(vendasCache, "alejoias_sqlite.csv")
        })

        content.addView(botaoVoltar("Fazer Backup Do SQLite") {
            fazerBackupBancoSqlite()
        })

        content.addView(botaoVoltar("Compartilhar Backup SQLite") {
            compartilharBackupSqlite()
        })

        content.addView(texto(
            "O SQLite é a base principal do aplicativo. Importar substitui o banco local pelos dados da planilha. " +
                    "Enviar substitui a planilha pela cópia atual do SQLite. Sincronizar mescla registros por ID e, em conflitos, mantém a alteração local.",
            14f,
            false
        ))
    }

    private fun sugerirImportacaoInicial() {
        AlertDialog.Builder(this)
            .setTitle("Banco SQLite Vazio")
            .setView(painelMensagem("Este aparelho ainda não possui dados locais. Deseja importar agora todos os clientes, vendas e pagamentos da planilha atual?"))
            .setPositiveButton("Importar") { _, _ -> importarPlanilhaParaSqlite(false) }
            .setNegativeButton("Depois", null)
            .show()
    }

    private fun confirmarImportacaoPlanilha() {
        AlertDialog.Builder(this)
            .setTitle("Importar Da Planilha")
            .setView(painelMensagem("A importação substituirá os dados atuais do SQLite pelos dados da planilha. Faça um backup local antes se tiver alterações ainda não enviadas."))
            .setPositiveButton("Importar") { _, _ -> importarPlanilhaParaSqlite(true) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEnvioPlanilha() {
        AlertDialog.Builder(this)
            .setTitle("Enviar Para Planilha")
            .setView(painelMensagem("A planilha será substituída pela cópia atual do SQLite, mantendo os mesmos IDs de clientes, vendas e pagamentos. Continuar?"))
            .setPositiveButton("Enviar") { _, _ -> enviarSqliteParaPlanilha() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarSincronizacao() {
        AlertDialog.Builder(this)
            .setTitle("Sincronizar Dados")
            .setView(painelMensagem("O app buscará a planilha, mesclará os registros por ID sem sobrescrever alterações locais pendentes e depois enviará a base mesclada de volta à planilha."))
            .setPositiveButton("Sincronizar") { _, _ -> sincronizarBidirecional() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun importarPlanilhaParaSqlite(mostrarToast: Boolean) {
        statusText.text = "Importando Planilha Para SQLite..."
        RetrofitClient.api.getSyncData().enqueue(object : Callback<SyncExportResponse> {
            override fun onResponse(call: Call<SyncExportResponse>, response: Response<SyncExportResponse>) {
                val body = response.body()
                if (!response.isSuccessful || body == null || !body.ok) {
                    statusText.text = "Falha Ao Importar Da Planilha"
                    Toast.makeText(this@MainActivity, body?.erro ?: "Erro HTTP ${response.code()}", Toast.LENGTH_LONG).show()
                    return
                }
                try {
                    localDb.replaceFromRemote(body)
                    carregarRelatorio { abrirBackupLocal() }
                    if (mostrarToast) Toast.makeText(this@MainActivity, "Planilha importada para o SQLite.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Erro ao gravar SQLite: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SyncExportResponse>, t: Throwable) {
                statusText.text = "Erro De Conexão Na Importação"
                Toast.makeText(this@MainActivity, "Não foi possível importar: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun enviarSqliteParaPlanilha() {
        val local = localDb.exportSyncData()
        val request = SyncUploadRequest(clientes = local.clientes, vendas = local.vendas, pagamentos = local.pagamentos)
        statusText.text = "Enviando SQLite Para Planilha..."
        RetrofitClient.api.uploadSyncData(request).enqueue(object : Callback<SyncUploadResponse> {
            override fun onResponse(call: Call<SyncUploadResponse>, response: Response<SyncUploadResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.ok == true) {
                    localDb.markAllSynced()
                    Toast.makeText(this@MainActivity, "Planilha atualizada com sucesso.", Toast.LENGTH_SHORT).show()
                    carregarRelatorio { abrirBackupLocal() }
                } else {
                    statusText.text = "Falha Ao Enviar Para Planilha"
                    Toast.makeText(this@MainActivity, body?.erro ?: "Erro HTTP ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SyncUploadResponse>, t: Throwable) {
                statusText.text = "Erro De Conexão No Envio"
                Toast.makeText(this@MainActivity, "Não foi possível enviar: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun sincronizarBidirecional() {
        statusText.text = "1/2 Buscando Dados Da Planilha..."
        RetrofitClient.api.getSyncData().enqueue(object : Callback<SyncExportResponse> {
            override fun onResponse(call: Call<SyncExportResponse>, response: Response<SyncExportResponse>) {
                val remoto = response.body()
                if (!response.isSuccessful || remoto == null || !remoto.ok) {
                    statusText.text = "Falha Ao Buscar Planilha"
                    Toast.makeText(this@MainActivity, remoto?.erro ?: "Erro HTTP ${response.code()}", Toast.LENGTH_LONG).show()
                    return
                }

                try {
                    localDb.mergeFromRemote(remoto)
                    val mesclado = localDb.exportSyncData()
                    val request = SyncUploadRequest(
                        clientes = mesclado.clientes,
                        vendas = mesclado.vendas,
                        pagamentos = mesclado.pagamentos
                    )
                    statusText.text = "2/2 Enviando Base Mesclada..."
                    RetrofitClient.api.uploadSyncData(request).enqueue(object : Callback<SyncUploadResponse> {
                        override fun onResponse(call: Call<SyncUploadResponse>, response: Response<SyncUploadResponse>) {
                            val body = response.body()
                            if (response.isSuccessful && body?.ok == true) {
                                localDb.markAllSynced()
                                Toast.makeText(this@MainActivity, "Sincronização concluída.", Toast.LENGTH_SHORT).show()
                                carregarRelatorio { abrirBackupLocal() }
                            } else {
                                Toast.makeText(this@MainActivity, body?.erro ?: "Falha no envio final", Toast.LENGTH_LONG).show()
                                carregarRelatorio { abrirBackupLocal() }
                            }
                        }

                        override fun onFailure(call: Call<SyncUploadResponse>, t: Throwable) {
                            Toast.makeText(this@MainActivity, "Mesclado localmente, mas falhou ao enviar: ${t.message}", Toast.LENGTH_LONG).show()
                            carregarRelatorio { abrirBackupLocal() }
                        }
                    })
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Erro ao mesclar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SyncExportResponse>, t: Throwable) {
                statusText.text = "Erro De Conexão Na Sincronização"
                Toast.makeText(this@MainActivity, "Não foi possível sincronizar: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fazerBackupBancoSqlite(): File? {
        return try {
            localDb.close()
            val origem = getDatabasePath(LocalDatabase.DB_NAME)
            val destino = File(filesDir, "backup_alejoias_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.db")
            origem.copyTo(destino, overwrite = true)
            localDb = LocalDatabase(this)
            Toast.makeText(this, "Backup SQLite salvo.", Toast.LENGTH_SHORT).show()
            destino
        } catch (e: Exception) {
            localDb = LocalDatabase(this)
            Toast.makeText(this, "Erro ao fazer backup: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun compartilharBackupSqlite() {
        try {
            val file = fazerBackupBancoSqlite() ?: return
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Compartilhar backup SQLite"))
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao compartilhar backup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportarCsvResumo(lista: List<VendaRelatorio>, nomeArquivo: String) {
        try {
            val header = "Cliente;Descrição;Data Compra;Vencimento;Valor;Pago;Faltante;Parcela\n"
            val linhas = lista.joinToString(separator = "\n") { venda ->
                val parcela = "${venda.parcela_atual ?: 1}/${venda.parcelas ?: 1}"
                listOf(
                    venda.nome_cliente ?: "",
                    venda.descricao ?: "",
                    venda.data_venda ?: "",
                    venda.data_vencimento ?: "",
                    venda.valor_total.toString(),
                    venda.total_pago.toString(),
                    venda.saldo.toString(),
                    parcela
                ).joinToString(";")
            }

            val file = File(cacheDir, nomeArquivo)
            file.writeText(header + linhas)

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Exportar/Compartilhar CSV"))
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao exportar CSV.", Toast.LENGTH_LONG).show()
        }
    }

    private fun gerarPdfResumo(lista: List<VendaRelatorio>, nomeArquivo: String) {
        try {
            val file = File(cacheDir, nomeArquivo)
            gerarPdfGenerico(file, "Resumo AleJoias Vendas", lista)
            compartilharPdf(file)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar PDF.", Toast.LENGTH_LONG).show()
        }
    }

    private fun gerarPdfCliente(cliente: String, lista: List<VendaRelatorio>) {
        try {
            val nomeSeguro = cliente.replace(" ", "_").replace("/", "_")
            val file = File(cacheDir, "extrato_${nomeSeguro}.pdf")
            gerarPdfGenerico(file, "Extrato Do Cliente: $cliente", lista)
            compartilharPdf(file)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar extrato PDF.", Toast.LENGTH_LONG).show()
        }
    }

    private fun gerarPdfGenerico(file: File, titulo: String, lista: List<VendaRelatorio>) {
        val pdf = PdfDocument()

        val titlePaint = Paint().apply {
            textSize = 19f
            isFakeBoldText = true
            color = corPrimariaEscura
            textAlign = Paint.Align.CENTER
        }
        val subtitlePaint = Paint().apply {
            textSize = 15f
            isFakeBoldText = true
            color = corDestaque
            textAlign = Paint.Align.CENTER
        }
        val sectionPaint = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            color = corPrimariaEscura
        }
        val labelPaint = Paint().apply {
            textSize = 10.5f
            isFakeBoldText = true
            color = corTextoSecundario
        }
        val valuePaint = Paint().apply {
            textSize = 10.5f
            color = corTexto
        }
        val footerPaint = Paint().apply {
            textSize = 9f
            color = corTextoSecundario
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint().apply {
            color = corBorda
            strokeWidth = 1f
        }
        val softLinePaint = Paint().apply {
            color = Color.rgb(238, 231, 220)
            strokeWidth = 1f
        }

        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        var y = 42

        fun rodape() {
            canvas.drawLine(40f, 805f, 555f, 805f, softLinePaint)
            canvas.drawText("Documento gerado automaticamente • AleJoias Vendas ERP • Página $pageNumber", 297f, 822f, footerPaint)
        }

        fun novaPagina() {
            rodape()
            pdf.finishPage(page)
            pageNumber++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
            canvas = page.canvas
            y = 42
        }

        fun garantirEspaco(altura: Int) {
            if (y + altura > 785) novaPagina()
        }

        fun campo(label: String, valor: String) {
            garantirEspaco(16)
            canvas.drawText("$label:", 50f, y.toFloat(), labelPaint)
            canvas.drawText(valor.take(72), 180f, y.toFloat(), valuePaint)
            y += 15
        }

        fun mesPorExtenso(mesAno: String): String {
            if (mesAno.length < 7) return mesAno
            val partes = mesAno.split("-")
            val nomes = listOf("JANEIRO","FEVEREIRO","MARÇO","ABRIL","MAIO","JUNHO","JULHO","AGOSTO","SETEMBRO","OUTUBRO","NOVEMBRO","DEZEMBRO")
            val mes = partes.getOrNull(1)?.toIntOrNull()
            val ano = partes.getOrNull(0) ?: ""
            return if (mes != null && mes in 1..12) "${nomes[mes - 1]} / $ano" else mesAno
        }

        fun periodoRelatorio(): String {
            val datas = lista.mapNotNull { it.data_venda }.filter { it.isNotBlank() }.sorted()
            val inicio = relatorioInicio ?: datas.firstOrNull()
            val fim = relatorioFim ?: datas.lastOrNull()
            return when {
                inicio != null && fim != null -> "$inicio até $fim"
                inicio != null -> inicio
                else -> "Não informado"
            }
        }

        fun mesReferencia(): String {
            val meses = lista.mapNotNull { it.data_venda }.filter { it.length >= 7 }.map { it.substring(0, 7) }.distinct()
            return if (meses.size == 1) mesPorExtenso(meses.first()) else "Múltiplos meses"
        }

        fun statusVenda(venda: VendaRelatorio): String = when {
            venda.total_pago >= venda.valor_total -> "QUITADO"
            estaVencida(venda) -> "VENCIDO"
            else -> "EM ABERTO"
        }

        val totalVendido = lista.sumOf { it.valor_total }
        val totalPago = lista.sumOf { it.total_pago }
        val faltante = lista.sumOf { it.saldo }
        val clientes = lista.mapNotNull { it.nome_cliente }.filter { it.isNotBlank() }.distinct().size
        val vencidas = lista.count { estaVencida(it) }
        val abertas = lista.count { it.saldo > 0.0 }
        val valorMedio = if (lista.isNotEmpty()) totalVendido / lista.size else 0.0
        val tipoRelatorio = if (mesReferencia() != "Múltiplos meses") "RELATÓRIO MENSAL" else "RELATÓRIO FINANCEIRO"

        canvas.drawText("◇ ALEJOIAS", 297f, y.toFloat(), titlePaint)
        y += 24
        canvas.drawText(tipoRelatorio, 297f, y.toFloat(), subtitlePaint)
        y += 26

        campo("Mês De Referência", mesReferencia())
        campo("Período", periodoRelatorio())
        campo("Emitido Em", hoje)
        y += 10
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), linePaint)
        y += 22

        canvas.drawText("Resumo Executivo", 40f, y.toFloat(), sectionPaint)
        y += 18
        campo("Total De Clientes", clientes.toString())
        campo("Total De Vendas/Cards", lista.size.toString())
        campo("Valor Vendido", moeda.format(totalVendido))
        campo("Valor Recebido", moeda.format(totalPago))
        campo("Saldo Em Aberto", moeda.format(faltante))
        campo("Valor Médio Por Venda", moeda.format(valorMedio))
        campo("Parcelas/Cards Em Aberto", abertas.toString())
        campo("Vendas Vencidas", vencidas.toString())
        y += 10
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), linePaint)
        y += 22

        canvas.drawText("Detalhamento", 40f, y.toFloat(), sectionPaint)
        y += 22

        lista.sortedBy { it.data_vencimento ?: "" }.forEachIndexed { index, venda ->
            garantirEspaco(168)
            canvas.drawText("Registro ${index + 1}", 40f, y.toFloat(), sectionPaint)
            y += 18
            campo("Cliente", venda.nome_cliente ?: "-")
            campo("Descrição", venda.descricao ?: "-")
            campo("Data Da Compra", venda.data_venda ?: "-")
            campo("Data De Vencimento", venda.data_vencimento ?: "-")
            campo("Parcela", "${venda.parcela_atual ?: 1}/${venda.parcelas ?: 1}")
            campo("Valor Da Venda", moeda.format(venda.valor_total))
            campo("Valor Pago", moeda.format(venda.total_pago))
            campo("Saldo Faltante", moeda.format(venda.saldo))
            campo("Status", statusVenda(venda))
            y += 8
            canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), softLinePaint)
            y += 18
        }

        rodape()
        pdf.finishPage(page)
        pdf.writeTo(FileOutputStream(file))
        pdf.close()
    }

    private fun compartilharPdf(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Compartilhar PDF"))
    }

    private fun confirmarDeletarCard(venda: VendaRelatorio) {
        val msg = "Deseja deletar este card de venda?\n\n" +
                "Cliente: ${venda.nome_cliente ?: "-"}\n" +
                "Vencimento: ${venda.data_vencimento ?: "-"}\n" +
                "Valor: ${moeda.format(venda.valor_total)}\n\n" +
                "Esta ação remove a venda e os pagamentos vinculados do SQLite. A exclusão será refletida na planilha no próximo envio/sync."

        AlertDialog.Builder(this)
            .setTitle("Deletar Card")
            .setView(painelMensagem(msg, aviso = true))
            .setPositiveButton("Deletar") { _, _ ->
                deletarCardVenda(venda)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deletarCardVenda(venda: VendaRelatorio) {
        try {
            localDb.deletarVenda(venda.id_venda.toLong().toString())
            Toast.makeText(this, "Card deletado do banco local.", Toast.LENGTH_SHORT).show()
            carregarRelatorio { abrirListaVendas() }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao deletar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirDialogVenda(vendaExistente: VendaRelatorio?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(8), dp(28), dp(8))
        }

        val nomes = vendasCache.mapNotNull { it.nome_cliente }.filter { it.isNotBlank() }.distinct().sorted()

        val nomeCliente = AutoCompleteTextView(this).apply {
            hint = "Nome Do Cliente"
            textSize = 16f
            threshold = 0
            setTextColor(corTexto)
            setHintTextColor(corTextoSecundario)
            backgroundTintList = android.content.res.ColorStateList.valueOf(corDestaque)
            setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, nomes))
            setOnClickListener { showDropDown() }
            setText(vendaExistente?.nome_cliente ?: "")
        }

        val valor = campo("Valor Total").apply {
            setText(vendaExistente?.valor_total?.takeIf { it > 0.0 }?.toString() ?: "")
        }

        val parcelas = campo("Parcelas").apply {
            setText((vendaExistente?.parcelas ?: 1).coerceAtLeast(1).toString())
        }

        val data = campo("Data Da Compra").apply {
            setText(vendaExistente?.data_venda ?: hoje)
            isFocusable = false
            isClickable = true
            setOnClickListener { abrirCalendario(this) }
        }

        val descricao = campo("Descrição Da Venda").apply {
            setText(vendaExistente?.descricao ?: "")
            minLines = 3
            maxLines = 3
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        layout.addView(campoRotulado("Nome:", nomeCliente))
        layout.addView(campoRotulado("Valor total:", valor))
        layout.addView(campoRotulado("Parcelas:", parcelas))
        layout.addView(campoRotulado("Data da compra:", data))
        layout.addView(campoRotulado("Descrição:", descricao))

        AlertDialog.Builder(this)
            .setTitle(if (vendaExistente == null) "Nova Venda" else "Editar Card")
            .setView(layout)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val valorVenda = valor.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                        val qtParcelas = (parcelas.text.toString().toIntOrNull() ?: 1).coerceAtLeast(1)

                        if (nomeCliente.text.toString().trim().isEmpty() || data.text.toString().trim().isEmpty() || valorVenda <= 0.0) {
                            Toast.makeText(this@MainActivity, "Preencha Nome, Data E Valor.", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }

                        if (vendaExistente == null) {
                            val request = NovaVendaRequest(
                                nome_cliente = nomeCliente.text.toString(),
                                descricao = descricao.text.toString(),
                                valor_total = valorVenda,
                                parcelas = qtParcelas,
                                data_venda = data.text.toString()
                            )
                            try {
                                localDb.novaVenda(request)
                                Toast.makeText(this@MainActivity, "Venda cadastrada no SQLite.", Toast.LENGTH_SHORT).show()
                                carregarRelatorio { abrirListaVendas() }
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val request = AtualizarVendaRequest(
                                id_venda = vendaExistente.id_venda.toLong().toString(),
                                nome_cliente = nomeCliente.text.toString(),
                                descricao = descricao.text.toString(),
                                valor_total = valorVenda,
                                parcelas = qtParcelas,
                                parcela_atual = vendaExistente.parcela_atual ?: 1,
                                data_venda = data.text.toString(),
                                data_vencimento = vendaExistente.data_vencimento ?: data.text.toString()
                            )
                            try {
                                localDb.atualizarVenda(request)
                                Toast.makeText(this@MainActivity, "Card atualizado no SQLite.", Toast.LENGTH_SHORT).show()
                                carregarRelatorio { abrirListaVendas() }
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Erro ao atualizar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }

                        dismiss()
                    }
                }
            }
            .show()
    }

    private fun dataPagamentoLocal(venda: VendaRelatorio): String {
        return venda.data_pagamento?.takeIf { it.isNotBlank() } ?: "Não informada"
    }

    private fun abrirDialogCorrigirPagamento(venda: VendaRelatorio) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(8), dp(28), dp(8))
        }

        layout.addView(texto("Valor Pago Atual: ${moeda.format(venda.total_pago)}", 15f, false))
        layout.addView(texto("Informe o valor total correto pago. Agora é possível aumentar ou reduzir.", 14f, false))

        val valorCorreto = campo("Valor Correto Pago").apply { setText(venda.total_pago.toString()) }
        val data = campo("Data Da Correção").apply {
            setText(dataPagamentoLocal(venda).takeIf { it != "Não informada" } ?: hoje)
            isFocusable = false
            isClickable = true
            setOnClickListener { abrirCalendario(this) }
        }

        layout.addView(campoRotulado("Valor pago correto:", valorCorreto))
        layout.addView(campoRotulado("Data da correção:", data))

        AlertDialog.Builder(this)
            .setTitle("Corrigir Valor Pago")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val correto = valorCorreto.text.toString().replace(",", ".").toDoubleOrNull() ?: -1.0
                if (correto < 0.0 || correto > venda.valor_total) {
                    Toast.makeText(this, "Informe um valor entre zero e o valor da venda.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                try {
                    localDb.corrigirPagamentoTotal(venda.id_venda.toLong().toString(), data.text.toString(), correto)
                    Toast.makeText(this, "Pagamento corrigido no SQLite.", Toast.LENGTH_SHORT).show()
                    carregarRelatorio { abrirListaVendas() }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao corrigir: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun compartilharResumoPeriodoWhatsApp(lista: List<VendaRelatorio>) {
        compartilharResumoPeriodoImagem()
    }

    private fun compartilharResumoPeriodoImagem() {
        try {
            val bitmap = Bitmap.createBitmap(content.width, content.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            content.draw(canvas)

            val file = File(cacheDir, "resumo_alejoias.png")
            val output = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
            output.close()

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(share, "Compartilhar resumo"))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível gerar a imagem do resumo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirDialogPagamento(venda: VendaRelatorio) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(8), dp(28), dp(8))
        }

        layout.addView(texto("Cliente: ${venda.nome_cliente ?: "-"}\nFaltante Atual: ${moeda.format(venda.saldo)}", 15f, false))

        val valor = campo("Valor Pago")
        val data = campo("Data Do Pagamento").apply {
            setText(hoje)
            isFocusable = false
            isClickable = true
            setOnClickListener { abrirCalendario(this) }
        }

        layout.addView(campoRotulado("Valor pago:", valor))
        layout.addView(campoRotulado("Data do pagamento:", data))

        AlertDialog.Builder(this)
            .setTitle("Registrar Pagamento")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val valorPago = valor.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                if (valorPago <= 0.0 || valorPago > venda.saldo + 0.001) {
                    Toast.makeText(this, "Informe um valor válido de até ${moeda.format(venda.saldo)}.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                try {
                    val request = NovoPagamentoRequest(
                        id_venda = venda.id_venda.toLong().toString(),
                        data_pagamento = data.text.toString(),
                        valor_pago = valorPago
                    )
                    localDb.novoPagamento(request)
                    Toast.makeText(this, "Pagamento registrado no SQLite.", Toast.LENGTH_SHORT).show()
                    carregarRelatorio { abrirListaVendas() }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao registrar pagamento: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    
private fun criarCanalNotificacoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "alejoias_vencimentos",
                "Vencimentos AleJoias",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            canal.description = "Avisos de vendas vencidas ou próximas do vencimento"
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun notificarVencimentos() {
        val vencidas = vendasCache.filter { estaVencida(it) }
        val vencendoHoje = vendasCache.filter {
            (it.data_vencimento ?: "") == hoje && it.saldo > 0.0
        }

        val totalAlertas = vencidas.size + vencendoHoje.size
        if (totalAlertas == 0) return

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val texto = when {
            vencidas.isNotEmpty() -> "${vencidas.size} venda(s) vencida(s) precisam de atenção."
            else -> "${vencendoHoje.size} venda(s) vencem hoje."
        }

        val notification = NotificationCompat.Builder(this, "alejoias_vencimentos")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("AleJoias Vendas")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2026, notification)
    }



    private fun carregarRelatorio(aoFinalizar: (() -> Unit)? = null) {
        try {
            statusText.text = "Carregando Dados Do SQLite..."
            vendasCache = localDb.getRelatorio()
            statusText.text = "SQLite Local | ${vendasCache.size} Cards | ${localDb.pendingSyncCount()} Alteração(ões) Para Sincronizar"
            notificarVencimentos()
            aoFinalizar?.invoke()
        } catch (e: Exception) {
            statusText.text = "Erro No Banco Local: ${e.message}"
        }
    }

    private fun respostaPadrao(msgSucesso: String): Callback<Map<String, Any>> {
        return object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                Toast.makeText(this@MainActivity, msgSucesso, Toast.LENGTH_SHORT).show()
                carregarRelatorio { abrirListaVendas() }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Erro: ${t.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun abrirCalendario(campoData: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                campoData.setText(String.format(Locale.US, "%04d-%02d-%02d", ano, mes + 1, dia))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun linhaBotoes(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, dp(4), 0, dp(4))
    }

    private fun botaoQuadrado(titulo: String, subtitulo: String, peso: Float, acao: () -> Unit): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(16), dp(12), dp(16))
            background = fundoArredondadoComBorda(corSuperficie, 26f, corBorda)
            isClickable = true
            setOnClickListener { acao() }
        }

        card.addView(TextView(this).apply {
            text = titulo
            textSize = if (resources.displayMetrics.widthPixels < 900) 17f else 19f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(corPrimariaEscura)
        })

        card.addView(TextView(this).apply {
            text = subtitulo
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(corDestaque)
        })

        val params = LinearLayout.LayoutParams(0, dp(96), peso)
        params.setMargins(dp(5), dp(5), dp(5), dp(5))
        card.layoutParams = params
        return card
    }

    private fun botaoOpcaoMenu(texto: String, destrutiva: Boolean, acao: () -> Unit): TextView =
        TextView(this).apply {
            text = texto
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(if (destrutiva) Color.rgb(153, 55, 47) else corPrimariaEscura)
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = fundoArredondadoComBorda(
                if (destrutiva) corVencido else corSuperficie,
                18f,
                if (destrutiva) Color.rgb(230, 181, 174) else corBorda
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { acao() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp(4), 0, dp(4)) }
        }

    private fun botaoVoltar(texto: String, acao: () -> Unit): Button = Button(this).apply {
        text = texto
        textSize = 14f
        isAllCaps = false
        val secundaria = texto.startsWith("Voltar", ignoreCase = true) ||
                texto.startsWith("Limpar", ignoreCase = true) ||
                texto.startsWith("Cancelar", ignoreCase = true)
        setTextColor(if (secundaria) corPrimaria else Color.WHITE)
        background = if (secundaria) {
            fundoArredondadoComBorda(corSuperficie, 22f, corBorda)
        } else {
            fundoArredondado(corPrimaria, 22f)
        }
        minHeight = dp(50)
        setPadding(dp(16), dp(8), dp(16), dp(8))
        setOnClickListener { acao() }
    }

    private fun adicionarCardResumo(titulo: String, valor: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = fundoArredondadoComBorda(corSuperficie, 22f, corBorda)
        }
        card.addView(texto(titulo, 13f, false).apply { setTextColor(corTextoSecundario) })
        card.addView(texto(valor, if (resources.displayMetrics.widthPixels < 900) 18f else 21f, true))
        content.addView(card, margemCardResumo())
    }

    private fun margemCardResumo(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, dp(5), 0, dp(5))
        }

    private fun texto(valor: String, tamanho: Float, negrito: Boolean): TextView = TextView(this).apply {
        text = valor
        textSize = tamanho
        setTextColor(corTexto)
        if (negrito) typeface = Typeface.DEFAULT_BOLD
    }

    private fun campoRotulado(rotulo: String, input: EditText): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(9), dp(12), dp(5))
        background = fundoArredondadoComBorda(corSuperficie, 18f, corBorda)

        addView(texto(rotulo, 12f, true).apply {
            setTextColor(corPrimariaEscura)
            setPadding(dp(2), 0, dp(2), 0)
        })

        addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, dp(5), 0, dp(5)) }
    }

    private fun campo(hint: String): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 16f
        setTextColor(corTexto)
        setHintTextColor(corTextoSecundario)
        backgroundTintList = android.content.res.ColorStateList.valueOf(corDestaque)
    }

    private fun margemCard(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, dp(8), 0, dp(8))
        }

    private fun fundoArredondado(cor: Int, raio: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio
        }

    private fun fundoArredondadoComBorda(cor: Int, raio: Float, corBorda: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio
            setStroke(1, corBorda)
        }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
