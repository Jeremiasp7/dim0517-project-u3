package br.ufrn.loja.api;


class RequisicaoInvalidaException extends RuntimeException {
    RequisicaoInvalidaException(String msg) { super(msg); }
}
