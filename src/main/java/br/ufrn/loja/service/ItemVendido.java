package br.ufrn.loja.service;

import java.math.BigDecimal;


/** Item de uma venda realizada, com o subtotal e o estoque do produto após a baixa. */
public record ItemVendido(int codigo, int quantidade, BigDecimal subtotal, int estoque) { }
