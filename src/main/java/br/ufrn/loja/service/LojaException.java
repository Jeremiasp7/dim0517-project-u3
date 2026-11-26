package br.ufrn.loja.service;


public abstract class LojaException extends RuntimeException {
    protected LojaException(String mensagem) { super(mensagem); }
}
