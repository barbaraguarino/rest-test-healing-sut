package br.edu.resttesthealingsut.carrinho.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.MINIMO_UM;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisição de inclusão de item no carrinho. O mínimo 1 é validação de formato; o máximo
 * por produto (RN02) é regra de negócio e fica em {@code Carrinho}, fora do contrato.
 */
public record ItemCarrinhoRequest(
        @NotNull(message = OBRIGATORIO) Long produtoId,
        @NotNull(message = OBRIGATORIO) @Min(value = 1, message = MINIMO_UM) Integer quantidade) {
}
