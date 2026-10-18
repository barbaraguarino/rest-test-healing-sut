package br.edu.resttesthealingsut.frete.dto;

import br.edu.resttesthealingsut.frete.CalculadoraFrete.ResultadoFrete;
import br.edu.resttesthealingsut.frete.ModalidadeFrete;
import java.math.BigDecimal;

/** Corpo da resposta da cotação de frete. */
public record CotacaoFreteResponse(
        ModalidadeFrete modalidade,
        BigDecimal valor,
        Integer prazoDias,
        Boolean freteGratis,
        Integer pesoTotalGramas) {

    public static CotacaoFreteResponse de(ModalidadeFrete modalidade, ResultadoFrete resultado) {
        return new CotacaoFreteResponse(
                modalidade, resultado.valor(), resultado.prazoDias(), resultado.freteGratis(), resultado.pesoTotalGramas());
    }
}
