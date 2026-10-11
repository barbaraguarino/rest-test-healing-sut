package br.edu.resttesthealingsut.frete;

/**
 * As 27 unidades federativas, cada uma com sua região. A associação UF → região é geografia, não
 * regra de negócio; os valores por região ficam em {@link CalculadoraFrete}.
 */
public enum Uf {
    AC(Regiao.NORTE), AP(Regiao.NORTE), AM(Regiao.NORTE), PA(Regiao.NORTE),
    RO(Regiao.NORTE), RR(Regiao.NORTE), TO(Regiao.NORTE),
    AL(Regiao.NORDESTE), BA(Regiao.NORDESTE), CE(Regiao.NORDESTE), MA(Regiao.NORDESTE), PB(Regiao.NORDESTE),
    PE(Regiao.NORDESTE), PI(Regiao.NORDESTE), RN(Regiao.NORDESTE), SE(Regiao.NORDESTE),
    DF(Regiao.CENTRO_OESTE), GO(Regiao.CENTRO_OESTE), MT(Regiao.CENTRO_OESTE), MS(Regiao.CENTRO_OESTE),
    ES(Regiao.SUDESTE), MG(Regiao.SUDESTE), RJ(Regiao.SUDESTE), SP(Regiao.SUDESTE),
    PR(Regiao.SUL), RS(Regiao.SUL), SC(Regiao.SUL);

    private final Regiao regiao;

    Uf(Regiao regiao) {
        this.regiao = regiao;
    }

    public Regiao getRegiao() {
        return regiao;
    }
}
