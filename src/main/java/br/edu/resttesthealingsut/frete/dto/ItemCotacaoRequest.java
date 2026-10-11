package br.edu.resttesthealingsut.frete.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.MINIMO_UM;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Item da cotação de frete. */
public record ItemCotacaoRequest(
        @NotNull(message = OBRIGATORIO) Long produtoId,
        @NotNull(message = OBRIGATORIO) @Min(value = 1, message = MINIMO_UM) Integer quantidade) {
}
