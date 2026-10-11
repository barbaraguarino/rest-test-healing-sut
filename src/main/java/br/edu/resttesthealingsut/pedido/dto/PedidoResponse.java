package br.edu.resttesthealingsut.pedido.dto;

import br.edu.resttesthealingsut.frete.ModalidadeFrete;
import br.edu.resttesthealingsut.pedido.Pedido;
import br.edu.resttesthealingsut.pedido.StatusPedido;
import java.math.BigDecimal;
import java.util.List;

/** Corpo da resposta com os dados de um pedido. O cupom não aparece na resposta, só o desconto. */
public record PedidoResponse(
        Long id,
        StatusPedido status,
        ClienteResponse cliente,
        EnderecoResponse enderecoEntrega,
        ModalidadeFrete modalidadeFrete,
        List<ItemPedidoResponse> itens,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal frete,
        BigDecimal total) {

    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getStatus(),
                ClienteResponse.de(pedido.getCliente()),
                EnderecoResponse.de(pedido.getEnderecoEntrega()),
                pedido.getModalidadeFrete(),
                pedido.getItens().stream().map(ItemPedidoResponse::de).toList(),
                pedido.getSubtotal(),
                pedido.getDesconto(),
                pedido.getFrete(),
                pedido.getTotal());
    }
}
