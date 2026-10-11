package br.edu.resttesthealingsut.api.suporte;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Apoio às asserções sobre o JSON das respostas.
 *
 * <p>Regra da suíte: os valores do JSON são verificados com {@code jsonPath(...).value(matcher)},
 * nunca com {@code jsonPath(...).value(objeto)}. A forma com objeto converte o valor do JSON para o
 * tipo esperado (o texto {@code "6"} vira o número 6), e uma mudança de tipo do campo no contrato
 * deixaria de quebrar o teste.
 */
public final class ValoresJson {

    private ValoresJson() {
    }

    /**
     * Aceita só um número JSON com o mesmo valor de {@code esperado}, sem considerar as casas decimais
     * ({@code 51.0} e {@code 51.00} são iguais). Recusa texto, de modo que a mudança de um campo de
     * número para texto quebra o teste. Não depende do tipo Java que o leitor de JSON entrega
     * ({@code Integer}, {@code Double} ou {@code BigDecimal}).
     *
     * @param esperado valor com ponto decimal, por exemplo {@code "51.00"}
     */
    public static Matcher<Number> decimal(String esperado) {
        BigDecimal valorEsperado = new BigDecimal(esperado);
        return new TypeSafeMatcher<Number>() {
            @Override
            protected boolean matchesSafely(Number atual) {
                return new BigDecimal(atual.toString()).compareTo(valorEsperado) == 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("número igual a ").appendValue(valorEsperado);
            }
        };
    }

    /** Lê o campo {@code id} da resposta, para os testes que criam um recurso com ID gerado pelo banco. */
    public static long lerId(MvcResult resultado) throws Exception {
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
        return id.longValue();
    }
}
