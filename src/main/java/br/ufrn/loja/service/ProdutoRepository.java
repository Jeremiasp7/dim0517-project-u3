package br.ufrn.loja.service;

import java.util.Optional;


public interface ProdutoRepository {
    Optional<Produto> buscarPorCodigo(int codigo);
    void salvar(Produto produto);
}
