package br.edu.resttesthealingsut.frete;

import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Cálculo do frete: concentra as regras RN04 a RN07. É código puro (sem banco), usado pela cotação
 * e pela criação do pedido, para que os dois calculem o frete do mesmo jeito.
 */
@Component
public class CalculadoraFrete {

    /** RN04: frete base pela região da UF de destino. */
    static final Map<Regiao, BigDecimal> FRETE_BASE_POR_REGIAO = Map.of(
            Regiao.SUDESTE, new BigDecimal("15.00"),
            Regiao.SUL, new BigDecimal("18.00"),
            Regiao.CENTRO_OESTE, new BigDecimal("22.00"),
            Regiao.NORDESTE, new BigDecimal("25.00"),
            Regiao.NORTE, new BigDecimal("30.00"));

    /** RN05: adicional por quilo iniciado do peso total. */
    static final BigDecimal ADICIONAL_POR_QUILO = new BigDecimal("2.00");

    /** RN05: gramas em um quilo, para contar os quilos iniciados. */
    static final int GRAMAS_POR_QUILO = 1000;

    /** RN06: prazo padrão, em dias, por região. */
    static final Map<Regiao, Integer> PRAZO_DIAS_POR_REGIAO = Map.of(
            Regiao.SUDESTE, 3,
            Regiao.SUL, 4,
            Regiao.CENTRO_OESTE, 5,
            Regiao.NORDESTE, 6,
            Regiao.NORTE, 8);

    /** RN06: na modalidade EXPRESSA, o valor do frete é multiplicado por 1,5. */
    static final BigDecimal MULTIPLICADOR_EXPRESSA = new BigDecimal("1.5");

    /** RN06: na modalidade EXPRESSA, o prazo diminui 2 dias. */
    static final int REDUCAO_PRAZO_EXPRESSA_DIAS = 2;

    /** RN06: prazo mínimo de 1 dia. */
    static final int PRAZO_MINIMO_DIAS = 1;

    /** RN07: subtotal a partir do qual o frete é grátis na modalidade PADRAO. */
    static final BigDecimal SUBTOTAL_MINIMO_FRETE_GRATIS = new BigDecimal("200.00");

    /**
     * Calcula o frete.
     *
     * @param uf         UF de destino
     * @param modalidade modalidade de entrega
     * @param itens      peso unitário e quantidade de cada item
     * @param subtotal   subtotal das mercadorias, antes de qualquer desconto (usado pela RN07)
     */
    public ResultadoFrete calcular(Uf uf, ModalidadeFrete modalidade, List<ItemFrete> itens, BigDecimal subtotal) {
        Regiao regiao = uf.getRegiao();

        // RN05: peso total = soma de pesoGramas × quantidade.
        int pesoTotalGramas = itens.stream().mapToInt(item -> item.pesoGramas() * item.quantidade()).sum();
        // RN05: "quilo iniciado" = divisão arredondada para cima (1.001 g contam como 2 kg).
        int quilosIniciados = (pesoTotalGramas + GRAMAS_POR_QUILO - 1) / GRAMAS_POR_QUILO;
        BigDecimal adicionalPeso = Dinheiro.arredondar(ADICIONAL_POR_QUILO.multiply(BigDecimal.valueOf(quilosIniciados)));

        // RN04 + RN05: base da região mais o adicional de peso.
        BigDecimal valor = Dinheiro.arredondar(FRETE_BASE_POR_REGIAO.get(regiao).add(adicionalPeso));
        int prazoDias = PRAZO_DIAS_POR_REGIAO.get(regiao);

        // RN06: a expressa multiplica o valor já somado (base + adicional) e encurta o prazo.
        if (modalidade == ModalidadeFrete.EXPRESSA) {
            valor = Dinheiro.arredondar(valor.multiply(MULTIPLICADOR_EXPRESSA));
            prazoDias = Math.max(PRAZO_MINIMO_DIAS, prazoDias - REDUCAO_PRAZO_EXPRESSA_DIAS);
        }

        // RN07: frete grátis só na PADRAO e com subtotal suficiente; o prazo não muda.
        boolean freteGratis = modalidade == ModalidadeFrete.PADRAO
                && subtotal.compareTo(SUBTOTAL_MINIMO_FRETE_GRATIS) >= 0;
        if (freteGratis) {
            valor = Dinheiro.ZERO;
        }
        return new ResultadoFrete(valor, prazoDias, freteGratis, pesoTotalGramas);
    }

    /** Peso unitário e quantidade de um item, o que o cálculo do peso total precisa. */
    public record ItemFrete(int pesoGramas, int quantidade) {
    }

    /** Resultado do cálculo, já com o valor na escala monetária. */
    public record ResultadoFrete(BigDecimal valor, int prazoDias, boolean freteGratis, int pesoTotalGramas) {
    }
}
