package br.ufrn.loja.service;


public class ProdutoJaExisteException extends LojaException {
    public ProdutoJaExisteException(int codigo) {
        super("Já existe produto com o código " + codigo);
    }
}
