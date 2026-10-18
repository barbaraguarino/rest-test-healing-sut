package br.edu.resttesthealingsut.produto.dto;

import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.MAIOR_OU_IGUAL_A_ZERO;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.MAIOR_QUE_ZERO;
import static br.edu.resttesthealingsut.shared.validacao.MensagensValidacao.OBRIGATORIO;

import br.edu.resttesthealingsut.produto.Categoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

/**
 * Corpo da requisição de criação de produto.
 *
 * <p>As anotações são só validação de formato e aparecem no contrato ({@code @NotNull} e
 * {@code @NotBlank} viram a lista {@code required}). Limites de negócio não entram aqui.
 * Os números usam tipos objeto ({@code Integer}, {@code Boolean}) para que um campo ausente chegue
 * como {@code null} e seja recusado, em vez de virar 0 ou {@code false}.
 */
public record ProdutoRequest(
        @NotBlank(message = OBRIGATORIO) String sku,
        @NotBlank(message = OBRIGATORIO) String nome,
        String descricao,
        @NotNull(message = OBRIGATORIO) @Positive(message = MAIOR_QUE_ZERO) BigDecimal preco,
        @NotNull(message = OBRIGATORIO) Categoria categoria,
        @NotNull(message = OBRIGATORIO) Boolean ativo,
        @NotNull(message = OBRIGATORIO) @PositiveOrZero(message = MAIOR_OU_IGUAL_A_ZERO) Integer estoque,
        @NotNull(message = OBRIGATORIO) @Positive(message = MAIOR_QUE_ZERO) Integer pesoGramas,
        @NotNull(message = OBRIGATORIO) @Valid DimensoesRequest dimensoes,
        List<@NotBlank(message = OBRIGATORIO) String> tags) {
}
