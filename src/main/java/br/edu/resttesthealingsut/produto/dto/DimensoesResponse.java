package br.edu.resttesthealingsut.produto.dto;

import br.edu.resttesthealingsut.produto.Dimensoes;

/** Dimensões na resposta de produto. */
public record DimensoesResponse(Integer alturaCm, Integer larguraCm, Integer comprimentoCm) {

    public static DimensoesResponse de(Dimensoes dimensoes) {
        return new DimensoesResponse(dimensoes.getAlturaCm(), dimensoes.getLarguraCm(), dimensoes.getComprimentoCm());
    }
}
