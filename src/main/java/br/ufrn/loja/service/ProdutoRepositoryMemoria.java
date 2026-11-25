package br.ufrn.loja.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ProdutoRepositoryMemoria implements ProdutoRepository {
    private final Map<Integer, Produto> produtos = new HashMap<>();

    @Override
    public Optional<Produto> buscarPorCodigo(int codigo) {
        return Optional.ofNullable(produtos.get(codigo));
    }

    @Override
    public void salvar(Produto produto) {
        produtos.put(produto.getCodigo(), produto);
    }
}
