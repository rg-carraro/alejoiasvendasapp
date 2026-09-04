const SHEET_CLIENTES = "CLIENTES";
const SHEET_VENDAS = "VENDAS";
const SHEET_PAGAMENTOS = "PAGAMENTOS";

const HEADERS_CLIENTES = ["id_cliente", "nome"];
const HEADERS_VENDAS = ["id_venda", "id_cliente", "descricao", "data_venda", "data_vencimento", "valor_total", "parcela_atual", "parcelas", "id_venda_pai"];
const HEADERS_PAGAMENTOS = ["id_pagamento", "id_venda", "data_pagamento", "valor_pago"];

function doGet(e) {
  try {
    const action = e && e.parameter ? e.parameter.action : "";
    if (action === "relatorio") return getRelatorio();
    if (action === "sync_export") return syncExport();
    return jsonResponse({ ok: false, erro: "Ação inválida no GET", action_recebida: action });
  } catch (err) {
    return jsonResponse({ ok: false, erro: String(err), stack: err && err.stack ? String(err.stack) : "" });
  }
}

function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return jsonResponse({ ok: false, erro: "POST sem corpo JSON" });
    }

    const data = JSON.parse(e.postData.contents);
    if (data.action === "nova_venda") return novaVenda(data);
    if (data.action === "atualizar_venda") return atualizarVenda(data);
    if (data.action === "novo_pagamento") return novoPagamento(data);
    if (data.action === "deletar_venda") return deletarVenda(data);
    if (data.action === "sync_upload") return syncUpload(data);

    return jsonResponse({ ok: false, erro: "Ação inválida no POST", action_recebida: data.action || "" });
  } catch (err) {
    return jsonResponse({ ok: false, erro: String(err), stack: err && err.stack ? String(err.stack) : "" });
  }
}

function novaVenda(data) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const clientesSheet = prepararSheet(ss, SHEET_CLIENTES, HEADERS_CLIENTES);
  const vendasSheet = prepararSheet(ss, SHEET_VENDAS, HEADERS_VENDAS);

  const nomeCliente = String(data.nome_cliente || "").trim();
  if (!nomeCliente) return jsonResponse({ ok: false, erro: "Nome do cliente é obrigatório" });

  const valorTotalVenda = normalizarNumero(data.valor_total);
  if (valorTotalVenda <= 0) return jsonResponse({ ok: false, erro: "Valor total é obrigatório" });

  const dataVenda = String(data.data_venda || "").trim();
  if (!dataVenda) return jsonResponse({ ok: false, erro: "Data da compra é obrigatória" });

  const qtdParcelas = Math.max(1, Number(data.parcelas || 1));
  const valorParcela = arredondar(valorTotalVenda / qtdParcelas);

  const idCliente = obterOuCriarCliente(clientesSheet, nomeCliente);
  const idPai = "VEN-" + new Date().getTime();

  for (let i = 1; i <= qtdParcelas; i++) {
    const idVenda = new Date().getTime() + i;
    const vencimento = somarDias(dataVenda, i * 30);

    vendasSheet.appendRow([
      idVenda,
      idCliente,
      String(data.descricao || ""),
      dataVenda,
      vencimento,
      valorParcela,
      i,
      qtdParcelas,
      idPai
    ]);
  }

  return jsonResponse({ ok: true, status: "ok", id_venda_pai: idPai, parcelas: qtdParcelas });
}

function atualizarVenda(data) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const clientesSheet = prepararSheet(ss, SHEET_CLIENTES, HEADERS_CLIENTES);
  const vendasSheet = prepararSheet(ss, SHEET_VENDAS, HEADERS_VENDAS);

  const idVenda = String(data.id_venda || "").trim();
  const nomeCliente = String(data.nome_cliente || "").trim();
  if (!idVenda) return jsonResponse({ ok: false, erro: "id_venda é obrigatório" });
  if (!nomeCliente) return jsonResponse({ ok: false, erro: "Nome do cliente é obrigatório" });

  const valorTotal = normalizarNumero(data.valor_total);
  if (valorTotal <= 0) return jsonResponse({ ok: false, erro: "Valor total é obrigatório" });

  const dataVenda = String(data.data_venda || "").trim();
  if (!dataVenda) return jsonResponse({ ok: false, erro: "Data da compra é obrigatória" });

  const idCliente = obterOuCriarCliente(clientesSheet, nomeCliente);
  const values = vendasSheet.getDataRange().getValues();

  for (let i = 1; i < values.length; i++) {
    if (String(values[i][0] || "").trim() === idVenda) {
      vendasSheet.getRange(i + 1, 1, 1, 9).setValues([[
        Number(idVenda),
        idCliente,
        String(data.descricao || ""),
        dataVenda,
        String(data.data_vencimento || somarDias(dataVenda, 30)),
        valorTotal,
        Number(data.parcela_atual || 1),
        Math.max(1, Number(data.parcelas || 1)),
        String(values[i][8] || idVenda)
      ]]);
      return jsonResponse({ ok: true, status: "ok", id_venda: idVenda });
    }
  }

  return jsonResponse({ ok: false, erro: "Venda não encontrada", id_venda: idVenda });
}

function novoPagamento(data) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const pagamentosSheet = prepararSheet(ss, SHEET_PAGAMENTOS, HEADERS_PAGAMENTOS);

  const idPagamento = new Date().getTime();
  const valorPago = normalizarNumero(data.valor_pago);

  pagamentosSheet.appendRow([
    idPagamento,
    String(data.id_venda || ""),
    String(data.data_pagamento || ""),
    valorPago
  ]);

  return jsonResponse({ ok: true, status: "ok", id_pagamento: idPagamento });
}


function deletarVenda(data) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const vendasSheet = prepararSheet(ss, SHEET_VENDAS, HEADERS_VENDAS);
  const pagamentosSheet = prepararSheet(ss, SHEET_PAGAMENTOS, HEADERS_PAGAMENTOS);

  const idVenda = String(data.id_venda || "").trim();
  if (!idVenda) return jsonResponse({ ok: false, erro: "id_venda é obrigatório" });

  const vendas = vendasSheet.getDataRange().getValues();
  let vendaRemovida = false;

  for (let i = vendas.length - 1; i >= 1; i--) {
    if (String(vendas[i][0] || "").trim() === idVenda) {
      vendasSheet.deleteRow(i + 1);
      vendaRemovida = true;
    }
  }

  const pagamentos = pagamentosSheet.getDataRange().getValues();
  let pagamentosRemovidos = 0;

  for (let j = pagamentos.length - 1; j >= 1; j--) {
    if (String(pagamentos[j][1] || "").trim() === idVenda) {
      pagamentosSheet.deleteRow(j + 1);
      pagamentosRemovidos++;
    }
  }

  return jsonResponse({
    ok: vendaRemovida,
    status: vendaRemovida ? "ok" : "venda_nao_encontrada",
    id_venda: idVenda,
    pagamentos_removidos: pagamentosRemovidos
  });
}


function getRelatorio() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const clientesSheet = prepararSheet(ss, SHEET_CLIENTES, HEADERS_CLIENTES);
  const vendasSheet = prepararSheet(ss, SHEET_VENDAS, HEADERS_VENDAS);
  const pagamentosSheet = prepararSheet(ss, SHEET_PAGAMENTOS, HEADERS_PAGAMENTOS);

  const clientes = clientesSheet.getDataRange().getValues();
  const vendas = vendasSheet.getDataRange().getValues();
  const pagamentos = pagamentosSheet.getDataRange().getValues();

  let mapaClientes = {};
  for (let i = 1; i < clientes.length; i++) {
    const idCliente = String(clientes[i][0] || "").trim();
    const nome = String(clientes[i][1] || "").trim();
    if (idCliente) mapaClientes[idCliente] = nome;
  }

  let resultado = [];
  for (let i = 1; i < vendas.length; i++) {
    const idVenda = String(vendas[i][0] || "").trim();
    if (!idVenda) continue;

    const idCliente = String(vendas[i][1] || "").trim();
    const valorTotal = normalizarNumero(vendas[i][5]);

    let totalPago = 0;
    let dataPagamento = "";
    for (let j = 1; j < pagamentos.length; j++) {
      if (String(pagamentos[j][1] || "").trim() === idVenda) {
        totalPago += normalizarNumero(pagamentos[j][3]);
        const dataPgtoAtual = formatarData(pagamentos[j][2]);
        if (dataPgtoAtual && dataPgtoAtual > dataPagamento) {
          dataPagamento = dataPgtoAtual;
        }
      }
    }

    resultado.push({
      id_venda: Number(idVenda),
      id_cliente: idCliente,
      nome_cliente: mapaClientes[idCliente] || "Cliente não encontrado",
      descricao: String(vendas[i][2] || ""),
      data_venda: formatarData(vendas[i][3]),
      data_vencimento: formatarData(vendas[i][4]),
      data_pagamento: dataPagamento,
      valor_total: valorTotal,
      total_pago: totalPago,
      saldo: valorTotal - totalPago,
      parcela_atual: Number(vendas[i][6] || 1),
      parcelas: Number(vendas[i][7] || 1),
      id_venda_pai: String(vendas[i][8] || "")
    });
  }

  return jsonResponse(resultado);
}


function syncExport() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const clientesSheet = prepararSheet(ss, SHEET_CLIENTES, HEADERS_CLIENTES);
  const vendasSheet = prepararSheet(ss, SHEET_VENDAS, HEADERS_VENDAS);
  const pagamentosSheet = prepararSheet(ss, SHEET_PAGAMENTOS, HEADERS_PAGAMENTOS);

  const clientesValues = clientesSheet.getDataRange().getValues();
  const vendasValues = vendasSheet.getDataRange().getValues();
  const pagamentosValues = pagamentosSheet.getDataRange().getValues();

  const clientes = [];
  for (let i = 1; i < clientesValues.length; i++) {
    const id = String(clientesValues[i][0] || "").trim();
    if (!id) continue;
    clientes.push({ id_cliente: id, nome: String(clientesValues[i][1] || "") });
  }

  const vendas = [];
  for (let i = 1; i < vendasValues.length; i++) {
    const id = String(vendasValues[i][0] || "").trim();
    if (!id) continue;
    vendas.push({
      id_venda: id,
      id_cliente: String(vendasValues[i][1] || "").trim(),
      descricao: String(vendasValues[i][2] || ""),
      data_venda: formatarData(vendasValues[i][3]),
      data_vencimento: formatarData(vendasValues[i][4]),
      valor_total: normalizarNumero(vendasValues[i][5]),
      parcela_atual: Number(vendasValues[i][6] || 1),
      parcelas: Number(vendasValues[i][7] || 1),
      id_venda_pai: String(vendasValues[i][8] || "")
    });
  }

  const pagamentos = [];
  for (let i = 1; i < pagamentosValues.length; i++) {
    const id = String(pagamentosValues[i][0] || "").trim();
    if (!id) continue;
    pagamentos.push({
      id_pagamento: id,
      id_venda: String(pagamentosValues[i][1] || "").trim(),
      data_pagamento: formatarData(pagamentosValues[i][2]),
      valor_pago: normalizarNumero(pagamentosValues[i][3])
    });
  }

  return jsonResponse({ ok: true, clientes: clientes, vendas: vendas, pagamentos: pagamentos });
}

function syncUpload(data) {
  const lock = LockService.getScriptLock();
  lock.waitLock(30000);
  try {
    const clientes = Array.isArray(data.clientes) ? data.clientes : [];
    const vendas = Array.isArray(data.vendas) ? data.vendas : [];
    const pagamentos = Array.isArray(data.pagamentos) ? data.pagamentos : [];

    const ss = SpreadsheetApp.getActiveSpreadsheet();
    const clientesSheet = prepararSheet(ss, SHEET_CLIENTES, HEADERS_CLIENTES);
    const vendasSheet = prepararSheet(ss, SHEET_VENDAS, HEADERS_VENDAS);
    const pagamentosSheet = prepararSheet(ss, SHEET_PAGAMENTOS, HEADERS_PAGAMENTOS);

    substituirConteudo(clientesSheet, HEADERS_CLIENTES, clientes.map(function(c) {
      return [String(c.id_cliente || ""), String(c.nome || "")];
    }).filter(function(r) { return r[0] !== ""; }));

    substituirConteudo(vendasSheet, HEADERS_VENDAS, vendas.map(function(v) {
      return [
        String(v.id_venda || ""),
        String(v.id_cliente || ""),
        String(v.descricao || ""),
        String(v.data_venda || ""),
        String(v.data_vencimento || ""),
        normalizarNumero(v.valor_total),
        Number(v.parcela_atual || 1),
        Math.max(1, Number(v.parcelas || 1)),
        String(v.id_venda_pai || "")
      ];
    }).filter(function(r) { return r[0] !== ""; }));

    substituirConteudo(pagamentosSheet, HEADERS_PAGAMENTOS, pagamentos.map(function(p) {
      return [
        String(p.id_pagamento || ""),
        String(p.id_venda || ""),
        String(p.data_pagamento || ""),
        normalizarNumero(p.valor_pago)
      ];
    }).filter(function(r) { return r[0] !== ""; }));

    SpreadsheetApp.flush();
    return jsonResponse({
      ok: true, status: "ok", clientes: clientes.length, vendas: vendas.length, pagamentos: pagamentos.length
    });
  } finally {
    lock.releaseLock();
  }
}

function substituirConteudo(sheet, headers, rows) {
  sheet.clearContents();
  sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
  if (rows.length > 0) sheet.getRange(2, 1, rows.length, headers.length).setValues(rows);
}

function prepararSheet(ss, nome, headers) {
  let sheet = ss.getSheetByName(nome);
  if (!sheet) sheet = ss.insertSheet(nome);

  if (sheet.getLastRow() === 0) {
    sheet.appendRow(headers);
  } else {
    sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
  }

  return sheet;
}

function obterOuCriarCliente(sheet, nomeCliente) {
  const values = sheet.getDataRange().getValues();
  const nomeNormalizado = normalizarTexto(nomeCliente);

  for (let i = 1; i < values.length; i++) {
    const id = String(values[i][0] || "").trim();
    const nome = String(values[i][1] || "").trim();
    if (id && normalizarTexto(nome) === nomeNormalizado) return id;
  }

  const novoId = "CLI-" + new Date().getTime();
  sheet.appendRow([novoId, nomeCliente]);
  return novoId;
}

function somarDias(dataTexto, dias) {
  const partes = String(dataTexto).split("-");
  const data = new Date(Number(partes[0]), Number(partes[1]) - 1, Number(partes[2]));
  data.setDate(data.getDate() + Number(dias));
  return Utilities.formatDate(data, Session.getScriptTimeZone(), "yyyy-MM-dd");
}

function normalizarNumero(value) {
  if (typeof value === "number") return value;

  let texto = String(value || "0")
    .replace("R$", "")
    .replace(/\s/g, "")
    .replace(/\./g, "")
    .replace(",", ".");

  const numero = Number(texto);
  return isNaN(numero) ? 0 : numero;
}

function arredondar(num) {
  return Math.round(Number(num) * 100) / 100;
}

function normalizarTexto(value) {
  return String(value || "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
}

function formatarData(value) {
  if (Object.prototype.toString.call(value) === "[object Date]") {
    return Utilities.formatDate(value, Session.getScriptTimeZone(), "yyyy-MM-dd");
  }
  return String(value || "");
}

function jsonResponse(data) {
  return ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}