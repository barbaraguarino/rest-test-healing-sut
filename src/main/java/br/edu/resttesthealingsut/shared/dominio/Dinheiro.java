package br.edu.resttesthealingsut.shared.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * Operações com valores monetários.
 *
 * <p>Concentra a RN09: todo valor monetário é {@link BigDecimal} com 2 casas decimais e
 * arredondamento {@code HALF_UP}, aplicado em cada cálculo (e não só no resultado final).
 * Nenhuma outra classe chama {@code setScale} diretamente.
 */
public final class Dinheiro {

    /** RN09: valores monetários com 2 casas decimais. */
    public static final int ESCALA = 2;

    /** RN09: arredondamento "meio para cima" (0,125 vira 0,13). */
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    /** Zero já na escala monetária, para que a resposta mostre 0.00 e não 0. */
    public static final BigDecimal ZERO = arredondar(BigDecimal.ZERO);

    private Dinheiro() {
    }

    /** Ajusta um valor para a escala monetária (RN09). */
    public static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(ESCALA, ARREDONDAMENTO);
    }

    /** RN09: subtotal de um item = preço unitário × quantidade. */
    public static BigDecimal subtotalDoItem(BigDecimal precoUnitario, int quantidade) {
        return arredondar(precoUnitario.multiply(BigDecimal.valueOf(quantidade)));
    }

    /** Soma valores monetários, arredondando o resultado (RN09). */
    public static BigDecimal somar(Collection<BigDecimal> valores) {
        return arredondar(valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
