package br.edu.resttesthealingsut.produto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static br.edu.resttesthealingsut.suporte.ValoresJson.decimal;
import static br.edu.resttesthealingsut.suporte.ValoresJson.lerId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Cadastro de produto")
    class CriarProduto {

        @Test
        @DisplayName("Deve retornar 201 Created, o cabeçalho Location e todos os campos quando o corpo estiver completo")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn201WithAllFieldsWhenPayloadIsComplete() throws Exception {
            MvcResult resultado = mockMvc.perform(post("/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "sku": "ELE-001",
                                      "nome": "Fone de Ouvido",
                                      "descricao": "Fone sem fio",
                                      "preco": 199.90,
                                      "categoria": "ELETRONICOS",
                                      "ativo": true,
                                      "estoque": 15,
                                      "pesoGramas": 250,
                                      "dimensoes": {"alturaCm": 18, "larguraCm": 16, "comprimentoCm": 8},
                                      "tags": ["promo", "novo"]
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.sku").value("ELE-001"))
                    .andExpect(jsonPath("$.nome").value("Fone de Ouvido"))
                    .andExpect(jsonPath("$.descricao").value("Fone sem fio"))
                    .andExpect(jsonPath("$.preco").value(decimal("199.90")))
                    .andExpect(jsonPath("$.categoria").value("ELETRONICOS"))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.estoque").value(is(15)))
                    .andExpect(jsonPath("$.pesoGramas").value(is(250)))
                    .andExpect(jsonPath("$.dimensoes.alturaCm").value(is(18)))
                    .andExpect(jsonPath("$.dimensoes.larguraCm").value(is(16)))
                    .andExpect(jsonPath("$.dimensoes.comprimentoCm").value(is(8)))
                    .andExpect(jsonPath("$.tags").value(contains("promo", "novo")))
                    .andReturn();

            assertThat(resultado.getResponse().getHeader("Location"), is("/produtos/" + lerId(resultado)));
        }

        @Test
        @DisplayName("Deve retornar 201 Created com descricao nula e tags vazia quando os campos opcionais não forem enviados")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn201WithNullDescriptionAndEmptyTagsWhenOptionalFieldsAreMissing() throws Exception {
            mockMvc.perform(post("/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "sku": "VES-001",
                                      "nome": "Camiseta",
                                      "preco": 49.90,
                                      "categoria": "VESTUARIO",
                                      "ativo": false,
                                      "estoque": 0,
                                      "pesoGramas": 200,
                                      "dimensoes": {"alturaCm": 2, "larguraCm": 30, "comprimentoCm": 40}
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.descricao").value(nullValue()))
                    .andExpect(jsonPath("$.tags").value(empty()))
                    .andExpect(jsonPath("$.ativo").value(false))
                    .andExpect(jsonPath("$.estoque").value(is(0)));
        }

        @Test
        @DisplayName("Deve arredondar o preço 10,005 para 10,01 com HALF_UP (RN09; HALF_EVEN daria 10,00)")
        @Sql("/fixtures/limpeza.sql")
        void shouldRoundPriceHalfUpWhenPriceHasThreeDecimalPlaces() throws Exception {
            mockMvc.perform(post("/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "sku": "CAS-002",
                                      "nome": "Pano de Prato",
                                      "preco": 10.005,
                                      "categoria": "CASA",
                                      "ativo": true,
                                      "estoque": 3,
                                      "pesoGramas": 80,
                                      "dimensoes": {"alturaCm": 1, "larguraCm": 40, "comprimentoCm": 60}
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.preco").value(decimal("10.01")));
        }

        @Test
        @DisplayName("Deve retornar 422 quando o SKU já estiver cadastrado")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/produto/produtos.sql"})
        void shouldReturn422WhenSkuAlreadyExists() throws Exception {
            mockMvc.perform(post("/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "sku": "LIV-001",
                                      "nome": "Livro Repetido",
                                      "preco": 10.00,
                                      "categoria": "LIVROS",
                                      "ativo": true,
                                      "estoque": 1,
                                      "pesoGramas": 100,
                                      "dimensoes": {"alturaCm": 1, "larguraCm": 1, "comprimentoCm": 1}
                                    }
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request com os campos ordenados quando faltarem obrigatórios, inclusive em objeto aninhado")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn400WithSortedFieldsWhenRequiredFieldsAreMissing() throws Exception {
            mockMvc.perform(post("/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "sku": "LIV-003",
                                      "preco": 10.00,
                                      "categoria": "LIVROS",
                                      "ativo": true,
                                      "estoque": 1,
                                      "pesoGramas": 100,
                                      "dimensoes": {"larguraCm": 10, "comprimentoCm": 10}
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("dimensoes.alturaCm", "nome")));
        }
    }

    @Nested
    @DisplayName("Busca de produto por ID")
    class BuscarProduto {

        @Test
        @DisplayName("Deve retornar 200 OK com todos os campos e as tags na ordem de cadastro quando o produto existir")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/produto/produtos.sql"})
        void shouldReturn200WithAllFieldsWhenProductExists() throws Exception {
            mockMvc.perform(get("/produtos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(1)))
                    .andExpect(jsonPath("$.sku").value("LIV-002"))
                    .andExpect(jsonPath("$.nome").value("Livro de Java"))
                    .andExpect(jsonPath("$.descricao").value("Livro sobre Java"))
                    .andExpect(jsonPath("$.preco").value(decimal("89.90")))
                    .andExpect(jsonPath("$.categoria").value("LIVROS"))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.estoque").value(is(7)))
                    .andExpect(jsonPath("$.pesoGramas").value(is(600)))
                    .andExpect(jsonPath("$.dimensoes.alturaCm").value(is(23)))
                    .andExpect(jsonPath("$.dimensoes.larguraCm").value(is(16)))
                    .andExpect(jsonPath("$.dimensoes.comprimentoCm").value(is(3)))
                    .andExpect(jsonPath("$.tags").value(contains("programacao", "java")));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o produto não existir")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn404WhenProductDoesNotExist() throws Exception {
            mockMvc.perform(get("/produtos/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request apontando id quando o ID não for numérico")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn400WhenIdIsNotNumeric() throws Exception {
            mockMvc.perform(get("/produtos/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("id")));
        }
    }

    @Nested
    @DisplayName("Listagem de produtos")
    class ListarProdutos {

        @Test
        @DisplayName("Deve retornar só os produtos da categoria, ordenados por SKU, quando a categoria for informada")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/produto/produtos.sql"})
        void shouldReturnOnlyCategoryProductsSortedBySkuWhenCategoryIsGiven() throws Exception {
            mockMvc.perform(get("/produtos").param("categoria", "LIVROS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].sku").value(contains("LIV-001", "LIV-002")))
                    .andExpect(jsonPath("$[*].categoria").value(contains("LIVROS", "LIVROS")));
        }

        @Test
        @DisplayName("Deve retornar todos os produtos, inclusive os inativos, ordenados por SKU, quando a categoria não for informada")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/produto/produtos.sql"})
        void shouldReturnAllProductsIncludingInactiveWhenCategoryIsMissing() throws Exception {
            mockMvc.perform(get("/produtos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].sku").value(contains("CAS-001", "LIV-001", "LIV-002")))
                    .andExpect(jsonPath("$[*].ativo").value(contains(true, false, true)));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request apontando categoria quando a categoria não existir")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn400WhenCategoryDoesNotExist() throws Exception {
            mockMvc.perform(get("/produtos").param("categoria", "FOO"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                    .andExpect(jsonPath("$.campos[*].campo").value(contains("categoria")));
        }
    }
}
