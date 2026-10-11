package br.edu.resttesthealingsut.api;

import static br.edu.resttesthealingsut.api.suporte.ValoresJson.decimal;
import static br.edu.resttesthealingsut.api.suporte.ValoresJson.lerId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
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
class PedidoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /pedidos com cupom e complemento: todos os campos; 1.400 g = 2 kg; 150,00 − 15,00 + (15,00 + 2 × 2,00) = 154,00")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/pedidos.sql"})
    void criarPedidoComTodosOsCampos() throws Exception {
        MvcResult resultado = criar("""
                {
                  "carrinhoId": 101,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10", "complemento": "Apto 1",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO",
                  "cupom": "BEMVINDO10"
                }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value(is("AGUARDANDO_PAGAMENTO")))
                .andExpect(jsonPath("$.cliente.nome").value(is("Ana Souza")))
                .andExpect(jsonPath("$.cliente.email").value(is("ana@exemplo.com")))
                .andExpect(jsonPath("$.enderecoEntrega.logradouro").value(is("Rua A")))
                .andExpect(jsonPath("$.enderecoEntrega.numero").value(is("10")))
                .andExpect(jsonPath("$.enderecoEntrega.complemento").value(is("Apto 1")))
                .andExpect(jsonPath("$.enderecoEntrega.cidade").value(is("Campinas")))
                .andExpect(jsonPath("$.enderecoEntrega.uf").value(is("SP")))
                .andExpect(jsonPath("$.enderecoEntrega.cep").value(is("13010000")))
                .andExpect(jsonPath("$.modalidadeFrete").value(is("PADRAO")))
                .andExpect(jsonPath("$.itens").value(hasSize(2)))
                .andExpect(jsonPath("$.itens[0].produtoId").value(is(1)))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value(is("Livro de Testes")))
                .andExpect(jsonPath("$.itens[0].quantidade").value(is(2)))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(decimal("50.00")))
                .andExpect(jsonPath("$.itens[0].subtotal").value(decimal("100.00")))
                .andExpect(jsonPath("$.itens[1].produtoId").value(is(2)))
                .andExpect(jsonPath("$.itens[1].nomeProduto").value(is("Caneca")))
                .andExpect(jsonPath("$.itens[1].quantidade").value(is(2)))
                .andExpect(jsonPath("$.itens[1].precoUnitario").value(decimal("25.00")))
                .andExpect(jsonPath("$.itens[1].subtotal").value(decimal("50.00")))
                .andExpect(jsonPath("$.subtotal").value(decimal("150.00")))
                .andExpect(jsonPath("$.desconto").value(decimal("15.00")))
                .andExpect(jsonPath("$.frete").value(decimal("19.00")))
                .andExpect(jsonPath("$.total").value(decimal("154.00")))
                .andReturn();

        assertThat(resultado.getResponse().getHeader("Location"), is("/pedidos/" + lerId(resultado)));
    }

    @Test
    @DisplayName("POST /pedidos sem cupom e sem complemento: desconto 0,00 e complemento null; 50,00 + 17,00 = 67,00")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/pedidos.sql"})
    void criarPedidoSemCamposOpcionais() throws Exception {
        criar("""
                {
                  "carrinhoId": 102,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO"
                }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enderecoEntrega.complemento").value(nullValue()))
                .andExpect(jsonPath("$.subtotal").value(decimal("50.00")))
                .andExpect(jsonPath("$.desconto").value(decimal("0.00")))
                .andExpect(jsonPath("$.frete").value(decimal("17.00")))
                .andExpect(jsonPath("$.total").value(decimal("67.00")));
    }

    @Test
    @DisplayName("RN03: a criação do pedido baixa o estoque: 5 − 2 = 3")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn03.sql"})
    void rn03CriacaoBaixaEstoque() throws Exception {
        criar("""
                {
                  "carrinhoId": 103,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO"
                }
                """)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/produtos/2"))
                .andExpect(jsonPath("$.estoque").value(is(3)));
    }

    @Test
    @DisplayName("RN03: a criação confere o estoque de novo (3 unidades, estoque 2): 422, sem baixar estoque nem esvaziar o carrinho")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn03.sql"})
    void rn03CriacaoConfereEstoqueDeNovo() throws Exception {
        criar("""
                {
                  "carrinhoId": 104,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO"
                }
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));

        mockMvc.perform(get("/produtos/3"))
                .andExpect(jsonPath("$.estoque").value(is(2)));
        mockMvc.perform(get("/carrinhos/104"))
                .andExpect(jsonPath("$.itens[*].quantidade").value(contains(3)));
    }

    @Test
    @DisplayName("RN07 com cupom: subtotal no limite, abaixo dele depois do desconto, ainda tem frete grátis")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn07.sql"})
    void rn07FreteGratisUsaSubtotalAntesDoDesconto() throws Exception {
        criar("""
                {
                  "carrinhoId": 101,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO",
                  "cupom": "BEMVINDO10"
                }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.frete").value(decimal("0.00")));
    }

    @Test
    @DisplayName("RN08: subtotal no mínimo do cupom: cupom aceito")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn08.sql"})
    void rn08SubtotalNoMinimoAceitaCupom() throws Exception {
        criar("""
                {
                  "carrinhoId": 101,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO",
                  "cupom": "BEMVINDO10"
                }
                """)
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("RN08: subtotal um centavo abaixo do mínimo do cupom: 422")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn08.sql"})
    void rn08SubtotalAbaixoDoMinimoRecusaCupom() throws Exception {
        criar("""
                {
                  "carrinhoId": 102,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO",
                  "cupom": "BEMVINDO10"
                }
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("RN08: cupom inexistente, com subtotal acima do mínimo: 422")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn08.sql"})
    void rn08CupomInexistenteRecusado() throws Exception {
        criar("""
                {
                  "carrinhoId": 101,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO",
                  "cupom": "NAOEXISTE"
                }
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("RN08: o código do cupom diferencia maiúsculas de minúsculas: 422")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn08.sql"})
    void rn08CupomDiferenciaMaiusculas() throws Exception {
        criar("""
                {
                  "carrinhoId": 101,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO",
                  "cupom": "bemvindo10"
                }
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("RN09: 10% de 100,05 = 10,005, arredondado com HALF_UP para 10,01 (HALF_EVEN daria 10,00); 100,05 − 10,01 + 17,00 = 107,04")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/pedidos.sql"})
    void rn09DescontoArredondaHalfUp() throws Exception {
        criar("""
                {
                  "carrinhoId": 103,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO",
                  "cupom": "BEMVINDO10"
                }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.desconto").value(decimal("10.01")))
                .andExpect(jsonPath("$.total").value(decimal("107.04")));
    }

    @Test
    @DisplayName("RN10: pedido de carrinho vazio: 422")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/pedidos.sql"})
    void rn10CarrinhoVazioRecusado() throws Exception {
        criar("""
                {
                  "carrinhoId": 104,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO"
                }
                """)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("A criação do pedido esvazia o carrinho")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/pedidos.sql"})
    void criacaoEsvaziaCarrinho() throws Exception {
        criar("""
                {
                  "carrinhoId": 101,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO"
                }
                """)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/carrinhos/101"))
                .andExpect(jsonPath("$.itens").value(empty()))
                .andExpect(jsonPath("$.subtotal").value(decimal("0.00")));
    }

    @Test
    @DisplayName("GET /pedidos/{id}: todos os campos do pedido da fixture")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/pedidos.sql"})
    void buscarPedido() throws Exception {
        mockMvc.perform(get("/pedidos/301"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(is(301)))
                .andExpect(jsonPath("$.status").value(is("AGUARDANDO_PAGAMENTO")))
                .andExpect(jsonPath("$.cliente.nome").value(is("Bruno Lima")))
                .andExpect(jsonPath("$.cliente.email").value(is("bruno@exemplo.com")))
                .andExpect(jsonPath("$.enderecoEntrega.logradouro").value(is("Rua B")))
                .andExpect(jsonPath("$.enderecoEntrega.numero").value(is("20")))
                .andExpect(jsonPath("$.enderecoEntrega.complemento").value(is("Casa 2")))
                .andExpect(jsonPath("$.enderecoEntrega.cidade").value(is("Campinas")))
                .andExpect(jsonPath("$.enderecoEntrega.uf").value(is("SP")))
                .andExpect(jsonPath("$.enderecoEntrega.cep").value(is("13010000")))
                .andExpect(jsonPath("$.modalidadeFrete").value(is("EXPRESSA")))
                .andExpect(jsonPath("$.itens[0].produtoId").value(is(1)))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value(is("Livro de Testes")))
                .andExpect(jsonPath("$.itens[0].quantidade").value(is(1)))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(decimal("50.00")))
                .andExpect(jsonPath("$.itens[0].subtotal").value(decimal("50.00")))
                .andExpect(jsonPath("$.itens[1].produtoId").value(is(2)))
                .andExpect(jsonPath("$.itens[1].nomeProduto").value(is("Caneca")))
                .andExpect(jsonPath("$.itens[1].quantidade").value(is(1)))
                .andExpect(jsonPath("$.itens[1].precoUnitario").value(decimal("25.00")))
                .andExpect(jsonPath("$.itens[1].subtotal").value(decimal("25.00")))
                .andExpect(jsonPath("$.subtotal").value(decimal("75.00")))
                .andExpect(jsonPath("$.desconto").value(decimal("0.00")))
                .andExpect(jsonPath("$.frete").value(decimal("25.50")))
                .andExpect(jsonPath("$.total").value(decimal("100.50")));
    }

    @Test
    @DisplayName("RN11: cancelar pedido aguardando pagamento: status CANCELADO e estoque devolvido, 3 + 2 = 5")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn11.sql"})
    void rn11CancelarDevolveEstoque() throws Exception {
        mockMvc.perform(post("/pedidos/{id}/cancelamento", 301))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(is(301)))
                .andExpect(jsonPath("$.status").value(is("CANCELADO")));

        mockMvc.perform(get("/produtos/1"))
                .andExpect(jsonPath("$.estoque").value(is(5)));
    }

    @Test
    @DisplayName("RN11: cancelar pedido já cancelado: 422, sem devolver estoque de novo")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/regra-rn11.sql"})
    void rn11PedidoCanceladoNaoCancelaDeNovo() throws Exception {
        mockMvc.perform(post("/pedidos/{id}/cancelamento", 302))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value(is("REGRA_DE_NEGOCIO")))
                .andExpect(jsonPath("$.campos").value(empty()));

        mockMvc.perform(get("/produtos/1"))
                .andExpect(jsonPath("$.estoque").value(is(3)));
    }

    @Test
    @DisplayName("Obrigatórios ausentes no endereço: 400 VALIDACAO com os caminhos ordenados")
    @Sql({"/fixtures/limpeza.sql", "/fixtures/pedidos.sql"})
    void obrigatoriosDoEnderecoAusentesRecusados() throws Exception {
        criar("""
                {
                  "carrinhoId": 102,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {"numero": "10", "cidade": "Campinas", "uf": "SP"},
                  "modalidadeFrete": "PADRAO"
                }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("VALIDACAO")))
                .andExpect(jsonPath("$.campos[*].campo")
                        .value(contains("enderecoEntrega.cep", "enderecoEntrega.logradouro")));
    }

    @Test
    @DisplayName("Carrinho inexistente na criação do pedido: 404 RECURSO_NAO_ENCONTRADO")
    @Sql("/fixtures/limpeza.sql")
    void criarPedidoComCarrinhoInexistente() throws Exception {
        criar("""
                {
                  "carrinhoId": 999,
                  "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                  "enderecoEntrega": {
                    "logradouro": "Rua A", "numero": "10",
                    "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                  },
                  "modalidadeFrete": "PADRAO"
                }
                """)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(is("RECURSO_NAO_ENCONTRADO")));
    }

    @Test
    @DisplayName("Pedido inexistente: 404 RECURSO_NAO_ENCONTRADO")
    @Sql("/fixtures/limpeza.sql")
    void pedidoInexistente() throws Exception {
        mockMvc.perform(get("/pedidos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(is("RECURSO_NAO_ENCONTRADO")));
    }

    private ResultActions criar(String json) throws Exception {
        return mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
