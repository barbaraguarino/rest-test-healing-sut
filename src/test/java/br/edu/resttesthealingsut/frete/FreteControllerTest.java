package br.edu.resttesthealingsut.frete;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static br.edu.resttesthealingsut.suporte.ValoresJson.decimal;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FreteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Cotação de frete")
    class CotarFrete {

        @Test
        @DisplayName("Deve retornar 200 OK com todos os campos quando a cotação for válida (PADRAO para SP, 500 g: 15,00 + 1 × 2,00 = 17,00, prazo 3)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldReturn200WithAllFieldsWhenQuotationIsValid() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 1, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.modalidade").value("PADRAO"))
                    .andExpect(jsonPath("$.valor").value(decimal("17.00")))
                    .andExpect(jsonPath("$.prazoDias").value(is(3)))
                    .andExpect(jsonPath("$.freteGratis").value(false))
                    .andExpect(jsonPath("$.pesoTotalGramas").value(is(500)));
        }

        @ParameterizedTest(name = "{0}: valor {2}, prazo {3}")
        @CsvSource({
                "SP, 01001000, 17.00, 3",
                "PR, 80010000, 20.00, 4",
                "GO, 74003010, 24.00, 5",
                "BA, 40020000, 27.00, 6",
                "AM, 69005000, 32.00, 8"
        })
        @DisplayName("Deve cobrar a base da região mais 1 kg × 2,00 e o prazo da região na PADRAO (RN04 e RN06: Sudeste 15, Sul 18, Centro-Oeste 22, Nordeste 25, Norte 30)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldChargeRegionBaseAndDeadlineWhenModeIsStandard(String uf, String cep, String valorEsperado,
                                                                int prazoEsperado) throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "%s", "uf": "%s"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 1, "quantidade": 1}]
                                    }
                                    """.formatted(cep, uf)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valor").value(decimal(valorEsperado)))
                    .andExpect(jsonPath("$.prazoDias").value(is(prazoEsperado)));
        }

        @Test
        @DisplayName("Deve contar 1 kg quando o peso for exatamente 1.000 g (RN05: 15,00 + 1 × 2,00 = 17,00)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn05.sql"})
        void shouldCountOneKilogramWhenWeightIsExactlyOneKilogram() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 1, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valor").value(decimal("17.00")));
        }

        @Test
        @DisplayName("Deve contar o quilo iniciado quando o peso passar 1 g do quilo (RN05: 1.001 g = 2 kg; 15,00 + 2 × 2,00 = 19,00)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn05.sql"})
        void shouldCountStartedKilogramWhenWeightExceedsKilogramByOneGram() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 2, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valor").value(decimal("19.00")));
        }

        @Test
        @DisplayName("Deve multiplicar o valor e reduzir o prazo na EXPRESSA (RN06, AM com 1.800 g: (30,00 + 2 × 2,00) × 1,5 = 51,00; prazo 8 − 2 = 6)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldMultiplyValueAndReduceDeadlineWhenModeIsExpress() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "69005000", "uf": "AM"},
                                      "modalidade": "EXPRESSA",
                                      "itens": [{"produtoId": 2, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.modalidade").value("EXPRESSA"))
                    .andExpect(jsonPath("$.valor").value(decimal("51.00")))
                    .andExpect(jsonPath("$.prazoDias").value(is(6)))
                    .andExpect(jsonPath("$.freteGratis").value(false))
                    .andExpect(jsonPath("$.pesoTotalGramas").value(is(1800)));
        }

        @Test
        @DisplayName("Deve retornar prazo de 1 dia, o mínimo, na EXPRESSA para o Sudeste (RN06: (15,00 + 2,00) × 1,5 = 25,50; prazo 3 − 2 = 1)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldReturnOneDayDeadlineWhenModeIsExpressInSoutheast() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "EXPRESSA",
                                      "itens": [{"produtoId": 1, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valor").value(decimal("25.50")))
                    .andExpect(jsonPath("$.prazoDias").value(is(1)));
        }

        @Test
        @DisplayName("Deve dar frete grátis, com valor 0,00 e prazo inalterado, quando o subtotal estiver no limite na PADRAO (RN07)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn07.sql"})
        void shouldGiveFreeShippingWhenSubtotalIsAtThreshold() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 1, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.freteGratis").value(true))
                    .andExpect(jsonPath("$.valor").value(decimal("0.00")))
                    .andExpect(jsonPath("$.prazoDias").value(is(3)));
        }

        @Test
        @DisplayName("Deve cobrar o frete quando o subtotal estiver um centavo abaixo do limite (RN07: 15,00 + 1 × 2,00 = 17,00)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn07.sql"})
        void shouldChargeShippingWhenSubtotalIsOneCentBelowThreshold() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 2, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.freteGratis").value(false))
                    .andExpect(jsonPath("$.valor").value(decimal("17.00")));
        }

        @Test
        @DisplayName("Deve cobrar o frete na EXPRESSA mesmo com o subtotal no limite (RN07: (15,00 + 2,00) × 1,5 = 25,50)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn07.sql"})
        void shouldChargeShippingWhenModeIsExpressEvenAtThreshold() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "EXPRESSA",
                                      "itens": [{"produtoId": 1, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.freteGratis").value(false))
                    .andExpect(jsonPath("$.valor").value(decimal("25.50")));
        }

        @Test
        @DisplayName("Deve somar os itens repetidos e ignorar produto inativo e sem estoque (3 × 400 g + 500 g = 1.700 g; 15,00 + 2 × 2,00 = 19,00)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldSumRepeatedItemsAndIgnoreActiveAndStockRules() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [
                                        {"produtoId": 3, "quantidade": 1},
                                        {"produtoId": 1, "quantidade": 1},
                                        {"produtoId": 3, "quantidade": 2}
                                      ]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pesoTotalGramas").value(is(1700)))
                    .andExpect(jsonPath("$.valor").value(decimal("19.00")));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando um produto da cotação não existir")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldReturn404WhenProductDoesNotExist() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 999, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request com os campos ordenados quando o CEP e a quantidade forem inválidos")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldReturn400WithSortedFieldsWhenCepAndQuantityAreInvalid() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "123", "uf": "SP"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 1, "quantidade": 0}]
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("destino.cep", "itens[0].quantidade")));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request apontando destino.uf quando a UF não existir")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/frete/frete.sql"})
        void shouldReturn400WhenUfDoesNotExist() throws Exception {
            mockMvc.perform(post("/fretes/cotacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "destino": {"cep": "01001000", "uf": "XX"},
                                      "modalidade": "PADRAO",
                                      "itens": [{"produtoId": 1, "quantidade": 1}]
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("destino.uf")));
        }
    }
}
