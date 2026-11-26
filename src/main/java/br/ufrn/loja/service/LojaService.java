package br.ufrn.loja.service;

import java.math.BigDecimal;


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

    /** Subtrai a quantidade informada do estoque do produto (perda, avaria ou ajuste). */
    public synchronized Produto saidaEstoque(int codigo, int quantidade) {
        validarQuantidade(quantidade);
        Produto produto = buscar(codigo);
        if (produto.getEstoque() < quantidade) {
            throw new EstoqueInsuficienteException(codigo, produto.getEstoque(), quantidade);
        }
        produto.removerEstoque(quantidade);
        repositorio.salvar(produto);
        return produto;
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
