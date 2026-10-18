package br.edu.resttesthealingsut.pedido;

import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import br.edu.resttesthealingsut.shared.erro.RegraDeNegocioException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Cupons de desconto aceitos (RN08). Os cupons são fixos no código, sem geração aleatória, para
 * manter o determinismo.
 */
public enum Cupom {

    /** RN08: 10% de desconto sobre o subtotal, para subtotal maior ou igual a 100,00. */
    BEMVINDO10(new BigDecimal("0.10"), new BigDecimal("100.00"));

    private final BigDecimal percentualDesconto;
    private final BigDecimal subtotalMinimo;

    Cupom(BigDecimal percentualDesconto, BigDecimal subtotalMinimo) {
        this.percentualDesconto = percentualDesconto;
        this.subtotalMinimo = subtotalMinimo;
    }

    /**
     * RN08: procura o cupom pelo código exato (diferencia maiúsculas de minúsculas).
     *
     * @throws RegraDeNegocioException se o cupom não existir
     */
    public static Cupom porCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(cupom -> cupom.name().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Cupom inexistente"));
    }

    /**
     * RN08: calcula o desconto sobre o subtotal.
     *
     * @throws RegraDeNegocioException se o subtotal não atender à condição do cupom
     */
    public BigDecimal calcularDesconto(BigDecimal subtotal) {
        if (subtotal.compareTo(subtotalMinimo) < 0) {
            throw new RegraDeNegocioException("Subtotal não atende à condição do cupom");
        }
        return Dinheiro.arredondar(subtotal.multiply(percentualDesconto));
    }
}
