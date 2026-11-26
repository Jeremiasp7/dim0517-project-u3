package br.ufrn.loja.api;

import br.ufrn.loja.service.DadosInvalidosException;
import br.ufrn.loja.service.ItemVenda;
import br.ufrn.loja.service.ItemVendido;
import br.ufrn.loja.service.LojaService;
import br.ufrn.loja.service.Produto;
import br.ufrn.loja.service.ProdutoJaExisteException;
import br.ufrn.loja.service.ProdutoNaoEncontradoException;
import br.ufrn.loja.service.Venda;
import br.ufrn.loja.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LojaController implements HttpHandler {
    private static final Pattern POST_PRODUTO = Pattern.compile("^/loja/produto/?$");
    private static final Pattern ESTOQUE = Pattern.compile("^/loja/produto/([^/]+)/estoque/?$");
    private static final Pattern ENTRADA = Pattern.compile("^/loja/produto/([^/]+)/entrada/?$");
    private static final Pattern SAIDA = Pattern.compile("^/loja/produto/([^/]+)/saida/?$");
    private static final Pattern VENDA = Pattern.compile("^/loja/venda/?$");

    private final LojaService service;

    public LojaController(LojaService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        int status;
        Object corpo;
        try {
            String path = ex.getRequestURI().getPath();
            String metodo = ex.getRequestMethod();
            Matcher m;

            if (POST_PRODUTO.matcher(path).matches()) {
                exigirMetodo(metodo, "POST");
                Map<String, Object> req = lerCorpo(ex);
                int codigo = inteiro(req, "codigo");
                BigDecimal preco = decimal(req, "preco");
                Produto p = service.cadastrarProduto(codigo, preco);
                status = 201;
                corpo = mapa("codigo", p.getCodigo(), "preco", p.getPreco(), "estoque", p.getEstoque());

            } else if ((m = ESTOQUE.matcher(path)).matches()) {
                exigirMetodo(metodo, "GET");
                int codigo = codigoDaUrl(m.group(1));
                status = 200;
                corpo = mapa("codigo", codigo, "estoque", service.consultarEstoque(codigo));

            } else if ((m = ENTRADA.matcher(path)).matches()) {
                exigirMetodo(metodo, "PUT");
                int codigo = codigoDaUrl(m.group(1));
                int quantidade = inteiro(lerCorpo(ex), "quantidade");
                Produto p = service.entradaEstoque(codigo, quantidade);
                status = 200;
                corpo = mapa("codigo", p.getCodigo(), "estoque", p.getEstoque());

            } else if ((m = SAIDA.matcher(path)).matches()) {
                exigirMetodo(metodo, "PUT");
                int codigo = codigoDaUrl(m.group(1));
                int quantidade = inteiro(lerCorpo(ex), "quantidade");
                Produto p = service.saidaEstoque(codigo, quantidade);
                status = 200;
                corpo = mapa("codigo", p.getCodigo(), "estoque", p.getEstoque());

            } else if (VENDA.matcher(path).matches()) {
                exigirMetodo(metodo, "POST");
                List<ItemVenda> itens = itensDaVenda(lerCorpo(ex));
                Venda venda = service.realizarVenda(itens);
                status = 201;
                corpo = corpoDaVenda(venda);

            } else {
                status = 404;
                corpo = mapa("erro", "Recurso não encontrado");
            }
        } catch (MetodoNaoPermitidoException e) {
            status = 405;
            corpo = mapa("erro", "Método não permitido");
        } catch (RequisicaoInvalidaException | DadosInvalidosException | Json.JsonException e) {
            status = 400;
            corpo = mapa("erro", e.getMessage());
        } catch (ProdutoNaoEncontradoException e) {
            status = 404;
            corpo = mapa("erro", e.getMessage());
        } catch (ProdutoJaExisteException e) {
            status = 409;
            corpo = mapa("erro", e.getMessage());
        } catch (RuntimeException e) {
            status = 500;
            corpo = mapa("erro", "Erro interno");
        }
        responder(ex, status, corpo);
    }


    private static class MetodoNaoPermitidoException extends RuntimeException { }

    private void exigirMetodo(String atual, String esperado) {
        if (!esperado.equals(atual)) throw new MetodoNaoPermitidoException();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> lerCorpo(HttpExchange ex) throws IOException {
        String texto = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Object json = Json.parse(texto);
        if (!(json instanceof Map)) throw new RequisicaoInvalidaException("O corpo deve ser um objeto JSON");
        return (Map<String, Object>) json;
    }

    private int codigoDaUrl(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new RequisicaoInvalidaException("Código inválido: " + texto);
        }
    }

    private BigDecimal numero(Map<String, Object> req, String campo) {
        Object v = req.get(campo);
        if (v == null) throw new RequisicaoInvalidaException("Campo obrigatório ausente: " + campo);
        if (!(v instanceof BigDecimal)) throw new RequisicaoInvalidaException("Campo '" + campo + "' deve ser numérico");
        return (BigDecimal) v;
    }

    private int inteiro(Map<String, Object> req, String campo) {
        BigDecimal v = numero(req, campo);
        try {
            if (v.scale() > 0) throw new ArithmeticException();
            return v.intValueExact();
        } catch (ArithmeticException e) {
            throw new RequisicaoInvalidaException("Campo '" + campo + "' deve ser um inteiro");
        }
    }

    private BigDecimal decimal(Map<String, Object> req, String campo) {
        return numero(req, campo);
    }

    @SuppressWarnings("unchecked")
    private List<ItemVenda> itensDaVenda(Map<String, Object> req) {
        Object valor = req.get("itens");
        if (valor == null) throw new RequisicaoInvalidaException("Campo obrigatório ausente: itens");
        if (!(valor instanceof List)) throw new RequisicaoInvalidaException("Campo 'itens' deve ser uma lista");
        List<ItemVenda> itens = new ArrayList<>();
        for (Object elemento : (List<Object>) valor) {
            if (!(elemento instanceof Map)) throw new RequisicaoInvalidaException("Cada item deve ser um objeto JSON");
            Map<String, Object> item = (Map<String, Object>) elemento;
            itens.add(new ItemVenda(inteiro(item, "codigo"), inteiro(item, "quantidade")));
        }
        return itens;
    }

    private Map<String, Object> corpoDaVenda(Venda venda) {
        List<Object> itens = new ArrayList<>();
        for (ItemVendido item : venda.itens()) {
            itens.add(mapa("codigo", item.codigo(), "quantidade", item.quantidade(),
                    "subtotal", item.subtotal(), "estoque", item.estoque()));
        }
        return mapa("total", venda.total(), "itens", itens);
    }

    private Map<String, Object> mapa(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private void responder(HttpExchange ex, int status, Object corpo) throws IOException {
        byte[] bytes = Json.stringify(corpo).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
