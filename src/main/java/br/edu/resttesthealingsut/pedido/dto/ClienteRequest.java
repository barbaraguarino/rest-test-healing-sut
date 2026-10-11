package br.edu.resttesthealingsut.pedido.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;

import jakarta.validation.constraints.NotBlank;

/** Cliente na requisição de pedido. O e-mail é só texto obrigatório, sem validação de formato. */
public record ClienteRequest(
        @NotBlank(message = OBRIGATORIO) String nome,
        @NotBlank(message = OBRIGATORIO) String email) {
}
