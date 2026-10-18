package br.edu.resttesthealingsut.frete.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.PELO_MENOS_UM_ITEM;

import br.edu.resttesthealingsut.frete.ModalidadeFrete;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Corpo da requisição de cotação de frete. {@code @NotEmpty} cobre a lista ausente e a vazia, com
 * uma única violação em cada caso.
 */
public record CotacaoFreteRequest(
        @NotNull(message = OBRIGATORIO) @Valid DestinoRequest destino,
        @NotNull(message = OBRIGATORIO) ModalidadeFrete modalidade,
        @NotEmpty(message = PELO_MENOS_UM_ITEM) List<@NotNull(message = OBRIGATORIO) @Valid ItemCotacaoRequest> itens) {
}
