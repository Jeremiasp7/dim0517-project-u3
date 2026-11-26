package br.ufrn.loja.service;


public class EstoqueInsuficienteException extends DadosInvalidosException {
    public EstoqueInsuficienteException(int codigo, int disponivel, int solicitado) {
        super("Estoque insuficiente para o produto " + codigo
                + ": disponível " + disponivel + ", solicitado " + solicitado);
    }
}
