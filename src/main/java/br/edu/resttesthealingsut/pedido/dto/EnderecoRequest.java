package br.edu.resttesthealingsut.pedido.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OITO_DIGITOS;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.PADRAO_CEP;

import br.edu.resttesthealingsut.frete.Uf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Endereço de entrega na requisição de pedido. {@code uf} e {@code cep} seguem o mesmo formato do
 * destino da cotação de frete.
 */
public record EnderecoRequest(
        @NotBlank(message = OBRIGATORIO) String logradouro,
        @NotBlank(message = OBRIGATORIO) String numero,
        String complemento,
        @NotBlank(message = OBRIGATORIO) String cidade,
        @NotNull(message = OBRIGATORIO) Uf uf,
        @NotNull(message = OBRIGATORIO) @Pattern(regexp = PADRAO_CEP, message = OITO_DIGITOS) String cep) {
}
