package br.edu.resttesthealingsut.produto.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.MAIOR_QUE_ZERO;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Dimensões na requisição de produto. Separada de {@link DimensoesResponse} para que o contrato da
 * requisição e o da resposta possam mudar de forma independente.
 */
public record DimensoesRequest(
        @NotNull(message = OBRIGATORIO) @Positive(message = MAIOR_QUE_ZERO) Integer alturaCm,
        @NotNull(message = OBRIGATORIO) @Positive(message = MAIOR_QUE_ZERO) Integer larguraCm,
        @NotNull(message = OBRIGATORIO) @Positive(message = MAIOR_QUE_ZERO) Integer comprimentoCm) {
}
