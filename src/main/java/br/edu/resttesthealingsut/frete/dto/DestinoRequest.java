package br.edu.resttesthealingsut.frete.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OITO_DIGITOS;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.PADRAO_CEP;

import br.edu.resttesthealingsut.frete.Uf;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Destino da cotação. O CEP usa {@code @NotNull} + {@code @Pattern}, e não {@code @NotBlank}: assim
 * um CEP vazio gera uma única violação (8 dígitos), e não duas.
 */
public record DestinoRequest(
        @NotNull(message = OBRIGATORIO) @Pattern(regexp = PADRAO_CEP, message = OITO_DIGITOS) String cep,
        @NotNull(message = OBRIGATORIO) Uf uf) {
}
