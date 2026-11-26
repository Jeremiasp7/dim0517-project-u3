package br.ufrn.loja.service;

import java.math.BigDecimal;


public class Produto {
    private final int codigo;
    private final BigDecimal preco;
    private int estoque;

    public Produto(int codigo, BigDecimal preco) {
        this.codigo = codigo;
        this.preco = preco;
        this.estoque = 0;
    }

    public int getCodigo() { return codigo; }
    public BigDecimal getPreco() { return preco; }
    public int getEstoque() { return estoque; }
}
