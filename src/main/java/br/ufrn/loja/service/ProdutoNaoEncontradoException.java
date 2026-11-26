package br.ufrn.loja.service;


public class ProdutoNaoEncontradoException extends LojaException {
    public ProdutoNaoEncontradoException(int codigo) {
        super("Produto não encontrado: " + codigo);
    }
}
