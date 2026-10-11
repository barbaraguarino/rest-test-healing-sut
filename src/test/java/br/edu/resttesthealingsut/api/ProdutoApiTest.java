package br.edu.resttesthealingsut.api;

import static br.edu.resttesthealingsut.api.suporte.ValoresJson.decimal;
import static br.edu.resttesthealingsut.api.suporte.ValoresJson.lerId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /produtos com todos os campos: 201, Location e todos os campos da resposta")
    @Sql("/fixtures/limpeza.sql")
    void criarProdutoComTodosOsCampos() throws Exception {
        MvcResult resultado = criar("""
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
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sku").value(is("ELE-001")))
                .andExpect(jsonPath("$.nome").value(is("Fone de Ouvido")))
                .andExpect(jsonPath("$.descricao").value(is("Fone sem fio")))
                .andExpect(jsonPath("$.preco").value(decimal("199.90")))
                .andExpect(jsonPath("$.categoria").value(is("ELETRONICOS")))
                .andExpect(jsonPath("$.ativo").value(is(true)))
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
    @DisplayName("POST /produtos sem descricao e sem tags: descricao null e tags vazia")
    @Sql("/fixtures/limpeza.sql")
    void criarProdutoSemCamposOpcionais() throws Exception {
        criar("""
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
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value(nullValue()))
                .andExpect(jsonPath("$.tags").value(empty()))
                .andExpect(jsonPath("$.ativo").value(is(false)))
                .andExpect(jsonPath("$.estoque").value(is(0)));
    }

    @Test
    @DisplayName("GET /produtos/{id}: todos os campos do produto da fixture, tags na ordem de cadastro")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/produtos.sql"})
    void buscarProdutoPorId() throws Exception {
        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(is(1)))
                .andExpect(jsonPath("$.sku").value(is("LIV-002")))
                .andExpect(jsonPath("$.nome").value(is("Livro de Java")))
                .andExpect(jsonPath("$.descricao").value(is("Livro sobre Java")))
                .andExpect(jsonPath("$.preco").value(decimal("89.90")))
                .andExpect(jsonPath("$.categoria").value(is("LIVROS")))
                .andExpect(jsonPath("$.ativo").value(is(true)))
                .andExpect(jsonPath("$.estoque").value(is(7)))
                .andExpect(jsonPath("$.pesoGramas").value(is(600)))
                .andExpect(jsonPath("$.dimensoes.alturaCm").value(is(23)))
                .andExpect(jsonPath("$.dimensoes.larguraCm").value(is(16)))
                .andExpect(jsonPath("$.dimensoes.comprimentoCm").value(is(3)))
                .andExpect(jsonPath("$.tags").value(contains("programacao", "java")));
    }

    @Test
    @DisplayName("GET /produtos?categoria=LIVROS: só os livros, ordenados por sku")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/produtos.sql"})
    void listarProdutosPorCategoria() throws Exception {
        mockMvc.perform(get("/produtos").param("categoria", "LIVROS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].sku").value(contains("LIV-001", "LIV-002")))
                .andExpect(jsonPath("$[*].categoria").value(contains("LIVROS", "LIVROS")));
    }

    @Test
    @DisplayName("GET /produtos sem categoria: todos, inclusive o inativo, ordenados por sku")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/produtos.sql"})
    void listarTodosOsProdutosIncluiInativos() throws Exception {
        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].sku").value(contains("CAS-001", "LIV-001", "LIV-002")))
                .andExpect(jsonPath("$[*].ativo").value(contains(true, false, true)));
    }

    @Test
    @DisplayName("RN09: preço 10,005 é arredondado com HALF_UP para 10,01 (HALF_EVEN daria 10,00)")
    @Sql("/fixtures/limpeza.sql")
    void rn09PrecoComTresCasasArredondaHalfUp() throws Exception {
        criar("""
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
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.preco").value(decimal("10.01")));
    }

    @Test
    @DisplayName("SKU já cadastrado: 422 REGRA_DE_NEGOCIO")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/produtos.sql"})
    void skuDuplicadoRecusado() throws Exception {
        criar("""
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
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("Obrigatórios ausentes, inclusive em objeto aninhado: 400 VALIDACAO com campos ordenados")
    @Sql("/fixtures/limpeza.sql")
    void obrigatoriosAusentesRecusados() throws Exception {
        criar("""
                {
                  "sku": "LIV-003",
                  "preco": 10.00,
                  "categoria": "LIVROS",
                  "ativo": true,
                  "estoque": 1,
                  "pesoGramas": 100,
                  "dimensoes": {"larguraCm": 10, "comprimentoCm": 10}
                }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("VALIDACAO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("dimensoes.alturaCm", "nome")));
    }

    @Test
    @DisplayName("Categoria inexistente na query: 400 VALIDACAO apontando categoria")
    @Sql("/fixtures/limpeza.sql")
    void categoriaInvalidaNaQueryRecusada() throws Exception {
        mockMvc.perform(get("/produtos").param("categoria", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("VALIDACAO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("categoria")));
    }

    @Test
    @DisplayName("ID não numérico no caminho: 400 VALIDACAO apontando id")
    @Sql("/fixtures/limpeza.sql")
    void idNaoNumericoRecusado() throws Exception {
        mockMvc.perform(get("/produtos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("VALIDACAO")))
                .andExpect(jsonPath("$.campos[*].campo").value(contains("id")));
    }

    @Test
    @DisplayName("Produto inexistente: 404 RECURSO_NAO_ENCONTRADO")
    @Sql("/fixtures/limpeza.sql")
    void produtoInexistente() throws Exception {
        mockMvc.perform(get("/produtos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(is("RECURSO_NAO_ENCONTRADO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    private ResultActions criar(String json) throws Exception {
        return mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
