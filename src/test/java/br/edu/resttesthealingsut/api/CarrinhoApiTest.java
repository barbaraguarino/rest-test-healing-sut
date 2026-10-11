package br.edu.resttesthealingsut.api;

import static br.edu.resttesthealingsut.api.suporte.ValoresJson.decimal;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class CarrinhoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /carrinhos: 201, carrinho vazio com subtotal 0,00")
    @Sql("/fixtures/limpeza.sql")
    void criarCarrinhoVazio() throws Exception {
        mockMvc.perform(post("/carrinhos"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.itens").value(empty()))
                .andExpect(jsonPath("$.subtotal").value(decimal("0.00")));
    }

    @Test
    @DisplayName("Incluir 2 unidades de 25,50: todos os campos; subtotal 2 × 25,50 = 51,00")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinhos.sql"})
    void incluirItem() throws Exception {
        incluir(101, """
                {"produtoId": 1, "quantidade": 2}
                """)
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
    @DisplayName("Incluir produto que já está no carrinho soma no item, que mantém a posição: 3 × 30,00 + 3 × 25,50 = 166,50")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinhos.sql"})
    void incluirProdutoRepetidoSomaNoItemExistente() throws Exception {
        incluir(102, """
                {"produtoId": 2, "quantidade": 2}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].produtoId").value(contains(2, 1)))
                .andExpect(jsonPath("$.itens[*].quantidade").value(contains(3, 3)))
                .andExpect(jsonPath("$.subtotal").value(decimal("166.50")));
    }

    @Test
    @DisplayName("GET /carrinhos/{id}: itens na ordem de inclusão; subtotal 30,00 + 3 × 25,50 = 106,50")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinhos.sql"})
    void buscarCarrinho() throws Exception {
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
    @DisplayName("RN01: produto inativo não entra no carrinho: 422")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn01.sql"})
    void rn01ProdutoInativoRecusado() throws Exception {
        incluir(101, """
                {"produtoId": 1, "quantidade": 1}
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("RN02: carrinho uma unidade abaixo do máximo aceita mais uma")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn02.sql"})
    void rn02UmaUnidadeAbaixoDoMaximoAceita() throws Exception {
        // Nos testes de limite, só o resultado da regra (aceito ou recusado) é verificado. Um valor
        // calculado a partir da fixture, como a quantidade resultante, faria uma mudança da regra
        // exigir correção no teste além da fixture.
        incluir(101, """
                {"produtoId": 1, "quantidade": 1}
                """)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RN02: carrinho no máximo recusa mais uma unidade: 422")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn02.sql"})
    void rn02NoMaximoRecusaMaisUma() throws Exception {
        incluir(102, """
                {"produtoId": 1, "quantidade": 1}
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("RN03: carrinho uma unidade abaixo do estoque aceita mais uma")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn03.sql"})
    void rn03UmaUnidadeAbaixoDoEstoqueAceita() throws Exception {
        incluir(101, """
                {"produtoId": 1, "quantidade": 1}
                """)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RN03: carrinho no estoque recusa mais uma unidade: 422")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn03.sql"})
    void rn03NoEstoqueRecusaMaisUma() throws Exception {
        incluir(102, """
                {"produtoId": 1, "quantidade": 1}
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("Campo desconhecido no corpo: 400 JSON_INVALIDO apontando o campo")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinhos.sql"})
    void campoDesconhecidoRecusado() throws Exception {
        incluir(101, """
                {"produtoId": 1, "quantidade": 1, "campoExtra": 1}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("JSON_INVALIDO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("campoExtra")));
    }

    @Test
    @DisplayName("Texto em campo numérico: 400 JSON_INVALIDO apontando quantidade")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinhos.sql"})
    void tipoIncompativelRecusado() throws Exception {
        incluir(101, """
                {"produtoId": 1, "quantidade": "abc"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("JSON_INVALIDO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("quantidade")));
    }

    @Test
    @DisplayName("Quantidade ausente: 400 VALIDACAO apontando quantidade")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/carrinhos.sql"})
    void quantidadeAusenteRecusada() throws Exception {
        incluir(101, """
                {"produtoId": 1}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("VALIDACAO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("quantidade")));
    }

    @Test
    @DisplayName("Carrinho inexistente: 404 RECURSO_NAO_ENCONTRADO")
    @Sql("/fixtures/limpeza.sql")
    void carrinhoInexistente() throws Exception {
        mockMvc.perform(get("/carrinhos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(is("RECURSO_NAO_ENCONTRADO")));
    }

    private ResultActions incluir(long carrinhoId, String json) throws Exception {
        return mockMvc.perform(post("/carrinhos/{id}/itens", carrinhoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
