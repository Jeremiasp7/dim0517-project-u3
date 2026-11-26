package br.ufrn.loja.service;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LojaServiceTest {
    private final LojaService loja = new LojaService();

    @Test
    void cadastraProdutoComCodigoPrecoEEstoqueZero() {
        BigDecimal preco = new BigDecimal("59.90");

        Produto produto = loja.cadastrarProduto(123, preco);

        assertEquals(123, produto.getCodigo());
        assertEquals(preco, produto.getPreco());
        assertEquals(0, produto.getEstoque());
        assertEquals(0, loja.consultarEstoque(123));
    }

    @Test
    void rejeitaCadastroComCodigoDuplicado() {
        loja.cadastrarProduto(123, BigDecimal.ONE);
        loja.entradaEstoque(123, 5);

        assertThrows(ProdutoJaExisteException.class,
                () -> loja.cadastrarProduto(123, BigDecimal.TEN));

        assertEquals(5, loja.consultarEstoque(123));
    }

    @Test
    void consultaEstoqueDoProdutoInformado() {
        loja.cadastrarProduto(123, BigDecimal.ONE);
        loja.cadastrarProduto(456, BigDecimal.ONE);
        loja.entradaEstoque(123, 8);
        loja.entradaEstoque(456, 3);

        assertEquals(8, loja.consultarEstoque(123));
        assertEquals(3, loja.consultarEstoque(456));
    }

    @Test
    void rejeitaConsultaDeProdutoInexistente() {
        assertThrows(ProdutoNaoEncontradoException.class, () -> loja.consultarEstoque(123));
    }

    @Test
    void somaEntradaAoEstoqueExistente() {
        loja.cadastrarProduto(123, BigDecimal.ONE);
        loja.entradaEstoque(123, 4);

        Produto produto = loja.entradaEstoque(123, 6);

        assertEquals(10, produto.getEstoque());
        assertEquals(10, loja.consultarEstoque(123));
    }

    @Test
    void rejeitaEntradaDeProdutoInexistente() {
        assertThrows(ProdutoNaoEncontradoException.class, () -> loja.entradaEstoque(123, 4));
    }

    @Test
    void subtraiSaidaDoEstoqueExistente() {
        loja.cadastrarProduto(123, BigDecimal.ONE);
        loja.entradaEstoque(123, 10);

        Produto produto = loja.saidaEstoque(123, 4);

        assertEquals(6, produto.getEstoque());
        assertEquals(6, loja.consultarEstoque(123));
    }

    @Test
    void rejeitaSaidaDeProdutoInexistente() {
        assertThrows(ProdutoNaoEncontradoException.class, () -> loja.saidaEstoque(123, 4));
    }

    @Test
    void permiteSaidaMaiorQueEstoqueEDeixaSaldoNegativo() {
        loja.cadastrarProduto(123, BigDecimal.ONE);
        loja.entradaEstoque(123, 3);

        Produto produto = loja.saidaEstoque(123, 5);

        assertEquals(-2, produto.getEstoque());
        assertEquals(-2, loja.consultarEstoque(123));
    }

    @Test
    void baixaEstoqueDeTodosOsItensDaVendaECalculaTotal() {
        loja.cadastrarProduto(123, new BigDecimal("59.90"));
        loja.cadastrarProduto(456, new BigDecimal("12.35"));
        loja.entradaEstoque(123, 10);
        loja.entradaEstoque(456, 7);

        Venda venda = loja.realizarVenda(List.of(new ItemVenda(123, 2), new ItemVenda(456, 3)));

        assertEquals(new BigDecimal("156.85"), venda.total());
        assertEquals(8, loja.consultarEstoque(123));
        assertEquals(4, loja.consultarEstoque(456));
    }

    @Test
    void vendaComProdutoInexistentePreservaEstoqueDeTodosOsItens() {
        loja.cadastrarProduto(123, BigDecimal.ONE);
        loja.cadastrarProduto(456, BigDecimal.TEN);
        loja.entradaEstoque(123, 10);
        loja.entradaEstoque(456, 7);

        List<ItemVenda> itens = List.of(
                new ItemVenda(123, 2), new ItemVenda(456, 3), new ItemVenda(789, 1));

        assertThrows(ProdutoNaoEncontradoException.class, () -> loja.realizarVenda(itens));

        assertEquals(10, loja.consultarEstoque(123));
        assertEquals(7, loja.consultarEstoque(456));
    }
}
