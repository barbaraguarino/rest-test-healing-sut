package br.edu.resttesthealingsut.carrinho.dto;

import br.edu.resttesthealingsut.carrinho.ItemCarrinho;
import java.math.BigDecimal;

/** Item na resposta de carrinho. */
public record ItemCarrinhoResponse(Long produtoId, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {

    public static ItemCarrinhoResponse de(ItemCarrinho item) {
        return new ItemCarrinhoResponse(
                item.getProduto().getId(), item.getQuantidade(), item.getPrecoUnitario(), item.calcularSubtotal());
    }
}
