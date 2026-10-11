package br.edu.resttesthealingsut.pedido.dto;

import br.edu.resttesthealingsut.pedido.ItemPedido;
import java.math.BigDecimal;

/** Item na resposta de pedido, com nome e preço congelados na criação. */
public record ItemPedidoResponse(
        Long produtoId, String nomeProduto, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(item.getProduto().getId(), item.getNomeProduto(), item.getQuantidade(),
                item.getPrecoUnitario(), item.getSubtotal());
    }
}
