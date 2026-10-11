package br.edu.resttesthealingsut.produto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Dimensões da embalagem do produto, em centímetros. Guardadas nas colunas da própria tabela
 * {@code produto} ({@code @Embeddable}), sem tabela separada.
 */
@Embeddable
public class Dimensoes {

    @Column(name = "altura_cm", nullable = false)
    private Integer alturaCm;

    @Column(name = "largura_cm", nullable = false)
    private Integer larguraCm;

    @Column(name = "comprimento_cm", nullable = false)
    private Integer comprimentoCm;

    /** Exigido pelo JPA. */
    protected Dimensoes() {
    }

    public Dimensoes(Integer alturaCm, Integer larguraCm, Integer comprimentoCm) {
        this.alturaCm = alturaCm;
        this.larguraCm = larguraCm;
        this.comprimentoCm = comprimentoCm;
    }

    public Integer getAlturaCm() {
        return alturaCm;
    }

    public Integer getLarguraCm() {
        return larguraCm;
    }

    public Integer getComprimentoCm() {
        return comprimentoCm;
    }
}
