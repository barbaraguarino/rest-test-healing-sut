package br.edu.resttesthealingsut.api;

import static br.edu.resttesthealingsut.api.suporte.ValoresJson.decimal;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class FreteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Cotação PADRAO para SP, 500 g, subtotal 50,00: todos os campos; 15,00 + 1 × 2,00 = 17,00, prazo 3")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void cotarComTodosOsCampos() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modalidade").value(is("PADRAO")))
                .andExpect(jsonPath("$.valor").value(decimal("17.00")))
                .andExpect(jsonPath("$.prazoDias").value(is(3)))
                .andExpect(jsonPath("$.freteGratis").value(is(false)))
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
    @DisplayName("RN04 e prazos da RN06: base da região + 1 kg × 2,00 (Sudeste 15, Sul 18, Centro-Oeste 22, Nordeste 25, Norte 30)")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void rn04FreteBaseEPrazoPorRegiao(String uf, String cep, String valorEsperado, int prazoEsperado) throws Exception {
        cotar("""
                {
                  "destino": {"cep": "%s", "uf": "%s"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """.formatted(cep, uf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(decimal(valorEsperado)))
                .andExpect(jsonPath("$.prazoDias").value(is(prazoEsperado)));
    }

    @Test
    @DisplayName("RN05: 1.000 g contam 1 kg: 15,00 + 1 × 2,00 = 17,00")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn05.sql"})
    void rn05PesoExatoEmQuilos() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(decimal("17.00")));
    }

    @Test
    @DisplayName("RN05: 1.001 g contam 2 kg (quilo iniciado): 15,00 + 2 × 2,00 = 19,00")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn05.sql"})
    void rn05QuiloIniciadoArredondaParaCima() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 2, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(decimal("19.00")));
    }

    @Test
    @DisplayName("RN06: EXPRESSA para AM, 1.800 g: (30,00 + 2 × 2,00) × 1,5 = 51,00; prazo 8 − 2 = 6")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void rn06ExpressaMultiplicaValorEReduzPrazo() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "69005000", "uf": "AM"},
                  "modalidade": "EXPRESSA",
                  "itens": [{"produtoId": 2, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modalidade").value(is("EXPRESSA")))
                .andExpect(jsonPath("$.valor").value(decimal("51.00")))
                .andExpect(jsonPath("$.prazoDias").value(is(6)))
                .andExpect(jsonPath("$.freteGratis").value(is(false)))
                .andExpect(jsonPath("$.pesoTotalGramas").value(is(1800)));
    }

    @Test
    @DisplayName("RN06: EXPRESSA para SP, 500 g: (15,00 + 1 × 2,00) × 1,5 = 25,50; prazo 3 − 2 = 1, o mínimo")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void rn06ExpressaRespeitaPrazoMinimo() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "EXPRESSA",
                  "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(decimal("25.50")))
                .andExpect(jsonPath("$.prazoDias").value(is(1)));
    }

    @Test
    @DisplayName("RN07: subtotal no limite na PADRAO: frete grátis, valor 0,00 e prazo inalterado")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn07.sql"})
    void rn07SubtotalNoLimiteTemFreteGratis() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freteGratis").value(is(true)))
                .andExpect(jsonPath("$.valor").value(decimal("0.00")))
                .andExpect(jsonPath("$.prazoDias").value(is(3)));
    }

    @Test
    @DisplayName("RN07: subtotal um centavo abaixo do limite: frete cobrado, 15,00 + 1 × 2,00 = 17,00")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn07.sql"})
    void rn07SubtotalAbaixoDoLimiteCobraFrete() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 2, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freteGratis").value(is(false)))
                .andExpect(jsonPath("$.valor").value(decimal("17.00")));
    }

    @Test
    @DisplayName("RN07: na EXPRESSA não há frete grátis, mesmo no limite: (15,00 + 2,00) × 1,5 = 25,50")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn07.sql"})
    void rn07ExpressaNaoTemFreteGratis() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "EXPRESSA",
                  "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freteGratis").value(is(false)))
                .andExpect(jsonPath("$.valor").value(decimal("25.50")));
    }

    @Test
    @DisplayName("Cotação soma itens repetidos e não aplica RN01 nem RN03: 3 × 400 g + 500 g = 1.700 g; 15,00 + 2 × 2,00 = 19,00")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void cotarSomaItensRepetidosEIgnoraAtivoEEstoque() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [
                    {"produtoId": 3, "quantidade": 1},
                    {"produtoId": 1, "quantidade": 1},
                    {"produtoId": 3, "quantidade": 2}
                  ]
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesoTotalGramas").value(is(1700)))
                .andExpect(jsonPath("$.valor").value(decimal("19.00")));
    }

    @Test
    @DisplayName("Produto inexistente na cotação: 404 RECURSO_NAO_ENCONTRADO")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void cotarProdutoInexistente() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 999, "quantidade": 1}]
                }
                """)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(is("RECURSO_NAO_ENCONTRADO")));
    }

    @Test
    @DisplayName("CEP sem 8 dígitos e quantidade 0 em item da lista: 400 VALIDACAO com os dois caminhos ordenados")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void cepEQuantidadeInvalidosRecusados() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "123", "uf": "SP"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 1, "quantidade": 0}]
                }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("VALIDACAO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("destino.cep", "itens[0].quantidade")));
    }

    @Test
    @DisplayName("UF inexistente no corpo: 400 JSON_INVALIDO apontando destino.uf")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/frete.sql"})
    void ufInexistenteRecusada() throws Exception {
        cotar("""
                {
                  "destino": {"cep": "01001000", "uf": "XX"},
                  "modalidade": "PADRAO",
                  "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("JSON_INVALIDO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("destino.uf")));
    }

    private ResultActions cotar(String json) throws Exception {
        return mockMvc.perform(post("/fretes/cotacao").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
