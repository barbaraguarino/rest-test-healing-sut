package br.edu.resttesthealingsut.pedido.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;

import br.edu.resttesthealingsut.frete.ModalidadeFrete;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisição de criação de pedido. {@code cupom} é opcional; quando informado, precisa ser
 * um cupom existente que atenda à sua condição (RN08).
 */
public record PedidoRequest(
        @NotNull(message = OBRIGATORIO) Long carrinhoId,
        @NotNull(message = OBRIGATORIO) @Valid ClienteRequest cliente,
        @NotNull(message = OBRIGATORIO) @Valid EnderecoRequest enderecoEntrega,
        @NotNull(message = OBRIGATORIO) ModalidadeFrete modalidadeFrete,
        String cupom) {
}
