package br.edu.resttesthealingsut.carrinho;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static br.edu.resttesthealingsut.suporte.ValoresJson.decimal;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CarrinhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Criação de carrinho")
    class CriarCarrinho {

        @Test
        @DisplayName("Deve retornar 201 Created com o carrinho vazio e subtotal 0,00")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn201WithEmptyCartWhenCartIsCreated() throws Exception {
            mockMvc.perform(post("/carrinhos"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.itens").value(empty()))
                    .andExpect(jsonPath("$.subtotal").value(decimal("0.00")));
        }
    }

    @Nested
    @DisplayName("Inclusão de item no carrinho")
    class IncluirItem {

        @Test
        @DisplayName("Deve retornar 200 OK com o item e os subtotais quando o produto estiver disponível (2 × 25,50 = 51,00)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinho/carrinhos.sql"})
        void shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable() throws Exception {
            mockMvc.perform(post("/carrinhos/101/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": 2}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(101)))
                    .andExpect(jsonPath("$.itens").value(hasSize(1)))
                    .andExpect(jsonPath("$.itens[0].produtoId").value(is(1)))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(is(2)))
                    .andExpect(jsonPath("$.itens[0].precoUnitario").value(decimal("25.50")))
                    .andExpect(jsonPath("$.itens[0].subtotal").value(decimal("51.00")))
                    .andExpect(jsonPath("$.subtotal").value(decimal("51.00")));
        }

        @Test
        @DisplayName("Deve somar a quantidade no item existente, que mantém a posição, quando o produto já estiver no carrinho (3 × 30,00 + 3 × 25,50 = 166,50)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinho/carrinhos.sql"})
        void shouldAddQuantityToExistingItemWhenProductIsAlreadyInCart() throws Exception {
            mockMvc.perform(post("/carrinhos/102/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 2, "quantidade": 2}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itens[*].produtoId").value(contains(2, 1)))
                    .andExpect(jsonPath("$.itens[*].quantidade").value(contains(3, 3)))
                    .andExpect(jsonPath("$.subtotal").value(decimal("166.50")));
        }

        @Test
        @DisplayName("Deve retornar 422 quando o produto estiver inativo (RN01)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn01.sql"})
        void shouldReturn422WhenProductIsInactive() throws Exception {
            mockMvc.perform(post("/carrinhos/101/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": 1}
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        // Nos testes de limite, só o resultado da regra (aceito ou recusado) é verificado. Um valor
        // calculado a partir da fixture, como a quantidade resultante, faria uma mudança da regra
        // exigir correção no teste além da fixture.
        @Test
        @DisplayName("Deve retornar 200 OK quando o carrinho estiver uma unidade abaixo do máximo por produto (RN02)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn02.sql"})
        void shouldReturn200WhenCartIsOneUnitBelowMaximum() throws Exception {
            mockMvc.perform(post("/carrinhos/101/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": 1}
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 422 quando o carrinho já tiver o máximo de unidades do produto (RN02)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn02.sql"})
        void shouldReturn422WhenCartAlreadyHasMaximumUnits() throws Exception {
            mockMvc.perform(post("/carrinhos/102/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": 1}
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve retornar 200 OK quando o carrinho estiver uma unidade abaixo do estoque (RN03)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn03.sql"})
        void shouldReturn200WhenCartIsOneUnitBelowStock() throws Exception {
            mockMvc.perform(post("/carrinhos/101/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": 1}
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 422 quando o carrinho já tiver todas as unidades em estoque (RN03)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn03.sql"})
        void shouldReturn422WhenCartAlreadyHasAllUnitsInStock() throws Exception {
            mockMvc.perform(post("/carrinhos/102/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": 1}
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request apontando o campo quando o corpo tiver um campo desconhecido")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinho/carrinhos.sql"})
        void shouldReturn400WhenBodyHasUnknownField() throws Exception {
            mockMvc.perform(post("/carrinhos/101/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": 1, "campoExtra": 1}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("campoExtra")));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request apontando quantidade quando a quantidade não for número")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinho/carrinhos.sql"})
        void shouldReturn400WhenQuantityIsNotANumber() throws Exception {
            mockMvc.perform(post("/carrinhos/101/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1, "quantidade": "abc"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("quantidade")));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request apontando quantidade quando a quantidade não for enviada")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinho/carrinhos.sql"})
        void shouldReturn400WhenQuantityIsMissing() throws Exception {
            mockMvc.perform(post("/carrinhos/101/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"produtoId": 1}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("quantidade")));
        }
    }

    @Nested
    @DisplayName("Busca de carrinho por ID")
    class BuscarCarrinho {

        @Test
        @DisplayName("Deve retornar 200 OK com os itens na ordem de inclusão quando o carrinho existir (30,00 + 3 × 25,50 = 106,50)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinho/carrinhos.sql"})
        void shouldReturn200WithItemsInInclusionOrderWhenCartExists() throws Exception {
            mockMvc.perform(get("/carrinhos/102"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(102)))
                    .andExpect(jsonPath("$.itens[0].produtoId").value(is(2)))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(is(1)))
                    .andExpect(jsonPath("$.itens[0].precoUnitario").value(decimal("30.00")))
                    .andExpect(jsonPath("$.itens[0].subtotal").value(decimal("30.00")))
                    .andExpect(jsonPath("$.itens[1].produtoId").value(is(1)))
                    .andExpect(jsonPath("$.itens[1].quantidade").value(is(3)))
                    .andExpect(jsonPath("$.itens[1].precoUnitario").value(decimal("25.50")))
                    .andExpect(jsonPath("$.itens[1].subtotal").value(decimal("76.50")))
                    .andExpect(jsonPath("$.subtotal").value(decimal("106.50")));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o carrinho não existir")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn404WhenCartDoesNotExist() throws Exception {
            mockMvc.perform(get("/carrinhos/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        }
    }
}
