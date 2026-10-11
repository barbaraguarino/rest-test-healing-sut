package br.edu.resttesthealingsut.carrinho.dto;

import br.edu.resttesthealingsut.carrinho.Carrinho;
import java.math.BigDecimal;
import java.util.List;

/** Corpo da resposta com os dados de um carrinho. Itens na ordem de inclusão. */
public record CarrinhoResponse(Long id, List<ItemCarrinhoResponse> itens, BigDecimal subtotal) {

    public static CarrinhoResponse de(Carrinho carrinho) {
        return new CarrinhoResponse(
                carrinho.getId(),
                carrinho.getItens().stream().map(ItemCarrinhoResponse::de).toList(),
                carrinho.calcularSubtotal());
    }
}
