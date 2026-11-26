package br.ufrn.loja.service;

import java.math.BigDecimal;
import java.util.List;


/** Resultado de uma venda: valor total e itens vendidos. */
public record Venda(BigDecimal total, List<ItemVendido> itens) {
    public Venda {
        itens = List.copyOf(itens);
    }
}
