package br.ufrn.loja.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class LojaService {
    private final ProdutoRepository repositorio;

    public LojaService() {
        this(new ProdutoRepositoryMemoria());
    }

    public LojaService(ProdutoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Cria um produto com o código e preço informados e estoque zero. */
    public synchronized Produto cadastrarProduto(int codigo, BigDecimal preco) {
        if (preco == null || preco.signum() < 0) {
            throw new DadosInvalidosException("O preço deve ser maior ou igual a zero");
        }
        if (repositorio.buscarPorCodigo(codigo).isPresent()) {
            throw new ProdutoJaExisteException(codigo);
        }
        Produto produto = new Produto(codigo, preco);
        repositorio.salvar(produto);
        return produto;
    }

    /** Retorna a quantidade em estoque do produto. */
    public synchronized int consultarEstoque(int codigo) {
        return buscar(codigo).getEstoque();
    }

    /** Acrescenta a quantidade informada ao estoque do produto (reposição). */
    public synchronized Produto entradaEstoque(int codigo, int quantidade) {
        validarQuantidade(quantidade);
        Produto produto = buscar(codigo);
        produto.adicionarEstoque(quantidade);
        repositorio.salvar(produto);
        return produto;
    }

    /** Subtrai a quantidade informada do estoque do produto, permitindo saldo negativo. */
    public synchronized Produto saidaEstoque(int codigo, int quantidade) {
        validarQuantidade(quantidade);
        Produto produto = buscar(codigo);
        produto.removerEstoque(quantidade);
        repositorio.salvar(produto);
        return produto;
    }

    /**
     * Realiza a venda dos itens informados e calcula o total (preço x quantidade de cada item).
     * A venda é atômica: todos os itens são validados antes de qualquer baixa de estoque.
     */
    public synchronized Venda realizarVenda(List<ItemVenda> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new DadosInvalidosException("A venda deve ter ao menos um item");
        }
        Map<Integer, Long> quantidadePorProduto = new LinkedHashMap<>();
        for (ItemVenda item : itens) {
            if (item == null) {
                throw new DadosInvalidosException("Item de venda inválido");
            }
            validarQuantidade(item.quantidade());
            quantidadePorProduto.merge(item.codigo(), (long) item.quantidade(), Long::sum);
        }

        Map<Integer, Produto> produtos = new LinkedHashMap<>();
        for (Integer codigo : quantidadePorProduto.keySet()) {
            produtos.put(codigo, buscar(codigo));
        }
        for (Map.Entry<Integer, Long> entrada : quantidadePorProduto.entrySet()) {
            Produto produto = produtos.get(entrada.getKey());
            if (produto.getEstoque() < entrada.getValue()) {
                throw new EstoqueInsuficienteException(
                        entrada.getKey(), produto.getEstoque(), entrada.getValue());
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        List<ItemVendido> vendidos = new ArrayList<>();
        for (ItemVenda item : itens) {
            Produto produto = produtos.get(item.codigo());
            produto.removerEstoque(item.quantidade());
            repositorio.salvar(produto);
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.quantidade()));
            total = total.add(subtotal);
            vendidos.add(new ItemVendido(item.codigo(), item.quantidade(), subtotal, produto.getEstoque()));
        }
        return new Venda(total, vendidos);
    }

    private Produto buscar(int codigo) {
        return repositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(codigo));
    }

    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade deve ser maior que zero");
        }
    }
}
