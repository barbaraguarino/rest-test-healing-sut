package br.edu.resttesthealingsut.pedido;

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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Criação de pedido")
    class CriarPedido {

        @Test
        @DisplayName("Deve retornar 201 Created, o cabeçalho Location e todos os campos quando o pedido tiver cupom e complemento (1.400 g = 2 kg; 150,00 − 15,00 + 19,00 = 154,00)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/pedido/pedidos.sql"})
        void shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement() throws Exception {
            MvcResult resultado = mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
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
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                    .andExpect(jsonPath("$.cliente.nome").value("Ana Souza"))
                    .andExpect(jsonPath("$.cliente.email").value("ana@exemplo.com"))
                    .andExpect(jsonPath("$.enderecoEntrega.logradouro").value("Rua A"))
                    .andExpect(jsonPath("$.enderecoEntrega.numero").value("10"))
                    .andExpect(jsonPath("$.enderecoEntrega.complemento").value("Apto 1"))
                    .andExpect(jsonPath("$.enderecoEntrega.cidade").value("Campinas"))
                    .andExpect(jsonPath("$.enderecoEntrega.uf").value("SP"))
                    .andExpect(jsonPath("$.enderecoEntrega.cep").value("13010000"))
                    .andExpect(jsonPath("$.modalidadeFrete").value("PADRAO"))
                    .andExpect(jsonPath("$.itens").value(hasSize(2)))
                    .andExpect(jsonPath("$.itens[0].produtoId").value(is(1)))
                    .andExpect(jsonPath("$.itens[0].nomeProduto").value("Livro de Testes"))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(is(2)))
                    .andExpect(jsonPath("$.itens[0].precoUnitario").value(decimal("50.00")))
                    .andExpect(jsonPath("$.itens[0].subtotal").value(decimal("100.00")))
                    .andExpect(jsonPath("$.itens[1].produtoId").value(is(2)))
                    .andExpect(jsonPath("$.itens[1].nomeProduto").value("Caneca"))
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
        @DisplayName("Deve retornar 201 Created com desconto 0,00 e complemento nulo quando os campos opcionais não forem enviados (50,00 + 17,00 = 67,00)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/pedido/pedidos.sql"})
        void shouldReturn201WithZeroDiscountWhenOptionalFieldsAreMissing() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "carrinhoId": 102,
                                      "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                                      "enderecoEntrega": {
                                        "logradouro": "Rua A", "numero": "10",
                                        "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                                      },
                                      "modalidadeFrete": "PADRAO"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.enderecoEntrega.complemento").value(nullValue()))
                    .andExpect(jsonPath("$.subtotal").value(decimal("50.00")))
                    .andExpect(jsonPath("$.desconto").value(decimal("0.00")))
                    .andExpect(jsonPath("$.frete").value(decimal("17.00")))
                    .andExpect(jsonPath("$.total").value(decimal("67.00")));
        }

        @Test
        @DisplayName("Deve baixar o estoque quando o pedido for criado (RN03: 5 − 2 = 3)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn03.sql"})
        void shouldDecreaseStockWhenOrderIsCreated() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "carrinhoId": 103,
                                      "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                                      "enderecoEntrega": {
                                        "logradouro": "Rua A", "numero": "10",
                                        "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                                      },
                                      "modalidadeFrete": "PADRAO"
                                    }
                                    """))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/produtos/2"))
                    .andExpect(jsonPath("$.estoque").value(is(3)));
        }

        @Test
        @DisplayName("Deve retornar 422 e manter estoque e carrinho quando o estoque não for mais suficiente (RN03: 3 unidades, estoque 2)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn03.sql"})
        void shouldReturn422AndKeepStockAndCartWhenStockIsNoLongerEnough() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "carrinhoId": 104,
                                      "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                                      "enderecoEntrega": {
                                        "logradouro": "Rua A", "numero": "10",
                                        "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                                      },
                                      "modalidadeFrete": "PADRAO"
                                    }
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));

            mockMvc.perform(get("/produtos/3"))
                    .andExpect(jsonPath("$.estoque").value(is(2)));
            mockMvc.perform(get("/carrinhos/104"))
                    .andExpect(jsonPath("$.itens[*].quantidade").value(contains(3)));
        }

        @Test
        @DisplayName("Deve dar frete grátis pelo subtotal antes do desconto quando o subtotal estiver no limite (RN07)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn07.sql"})
        void shouldGiveFreeShippingUsingSubtotalBeforeDiscount() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
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
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.frete").value(decimal("0.00")));
        }

        @Test
        @DisplayName("Deve aceitar o cupom quando o subtotal estiver no mínimo do cupom (RN08)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn08.sql"})
        void shouldAcceptCouponWhenSubtotalIsAtMinimum() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
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
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve retornar 422 quando o subtotal estiver um centavo abaixo do mínimo do cupom (RN08)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn08.sql"})
        void shouldReturn422WhenSubtotalIsOneCentBelowCouponMinimum() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
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
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve retornar 422 quando o cupom não existir, com subtotal acima do mínimo (RN08)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn08.sql"})
        void shouldReturn422WhenCouponDoesNotExist() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
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
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve retornar 422 quando o código do cupom tiver maiúsculas e minúsculas diferentes (RN08)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn08.sql"})
        void shouldReturn422WhenCouponCaseDoesNotMatch() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
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
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve arredondar o desconto com HALF_UP (RN09: 10% de 100,05 = 10,005 → 10,01, HALF_EVEN daria 10,00; 100,05 − 10,01 + 17,00 = 107,04)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/pedido/pedidos.sql"})
        void shouldRoundDiscountHalfUp() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
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
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.desconto").value(decimal("10.01")))
                    .andExpect(jsonPath("$.total").value(decimal("107.04")));
        }

        @Test
        @DisplayName("Deve retornar 422 quando o carrinho estiver vazio (RN10)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/pedido/pedidos.sql"})
        void shouldReturn422WhenCartIsEmpty() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "carrinhoId": 104,
                                      "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                                      "enderecoEntrega": {
                                        "logradouro": "Rua A", "numero": "10",
                                        "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                                      },
                                      "modalidadeFrete": "PADRAO"
                                    }
                                    """))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));
        }

        @Test
        @DisplayName("Deve esvaziar o carrinho quando o pedido for criado")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/pedido/pedidos.sql"})
        void shouldEmptyCartWhenOrderIsCreated() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "carrinhoId": 101,
                                      "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                                      "enderecoEntrega": {
                                        "logradouro": "Rua A", "numero": "10",
                                        "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                                      },
                                      "modalidadeFrete": "PADRAO"
                                    }
                                    """))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/carrinhos/101"))
                    .andExpect(jsonPath("$.itens").value(empty()))
                    .andExpect(jsonPath("$.subtotal").value(decimal("0.00")));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request com os campos ordenados quando faltarem obrigatórios do endereço")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/pedido/pedidos.sql"})
        void shouldReturn400WithSortedFieldsWhenAddressFieldsAreMissing() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "carrinhoId": 102,
                                      "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                                      "enderecoEntrega": {"numero": "10", "cidade": "Campinas", "uf": "SP"},
                                      "modalidadeFrete": "PADRAO"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                    .andExpect(jsonPath("$.campos[*].campo")
                            .value(contains("enderecoEntrega.cep", "enderecoEntrega.logradouro")));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o carrinho não existir")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn404WhenCartDoesNotExist() throws Exception {
            mockMvc.perform(post("/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "carrinhoId": 999,
                                      "cliente": {"nome": "Ana Souza", "email": "ana@exemplo.com"},
                                      "enderecoEntrega": {
                                        "logradouro": "Rua A", "numero": "10",
                                        "cidade": "Campinas", "uf": "SP", "cep": "13010000"
                                      },
                                      "modalidadeFrete": "PADRAO"
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        }
    }

    @Nested
    @DisplayName("Busca de pedido por ID")
    class BuscarPedido {

        @Test
        @DisplayName("Deve retornar 200 OK com todos os campos quando o pedido existir")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/pedido/pedidos.sql"})
        void shouldReturn200WithAllFieldsWhenOrderExists() throws Exception {
            mockMvc.perform(get("/pedidos/301"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(301)))
                    .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                    .andExpect(jsonPath("$.cliente.nome").value("Bruno Lima"))
                    .andExpect(jsonPath("$.cliente.email").value("bruno@exemplo.com"))
                    .andExpect(jsonPath("$.enderecoEntrega.logradouro").value("Rua B"))
                    .andExpect(jsonPath("$.enderecoEntrega.numero").value("20"))
                    .andExpect(jsonPath("$.enderecoEntrega.complemento").value("Casa 2"))
                    .andExpect(jsonPath("$.enderecoEntrega.cidade").value("Campinas"))
                    .andExpect(jsonPath("$.enderecoEntrega.uf").value("SP"))
                    .andExpect(jsonPath("$.enderecoEntrega.cep").value("13010000"))
                    .andExpect(jsonPath("$.modalidadeFrete").value("EXPRESSA"))
                    .andExpect(jsonPath("$.itens[0].produtoId").value(is(1)))
                    .andExpect(jsonPath("$.itens[0].nomeProduto").value("Livro de Testes"))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(is(1)))
                    .andExpect(jsonPath("$.itens[0].precoUnitario").value(decimal("50.00")))
                    .andExpect(jsonPath("$.itens[0].subtotal").value(decimal("50.00")))
                    .andExpect(jsonPath("$.itens[1].produtoId").value(is(2)))
                    .andExpect(jsonPath("$.itens[1].nomeProduto").value("Caneca"))
                    .andExpect(jsonPath("$.itens[1].quantidade").value(is(1)))
                    .andExpect(jsonPath("$.itens[1].precoUnitario").value(decimal("25.00")))
                    .andExpect(jsonPath("$.itens[1].subtotal").value(decimal("25.00")))
                    .andExpect(jsonPath("$.subtotal").value(decimal("75.00")))
                    .andExpect(jsonPath("$.desconto").value(decimal("0.00")))
                    .andExpect(jsonPath("$.frete").value(decimal("25.50")))
                    .andExpect(jsonPath("$.total").value(decimal("100.50")));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o pedido não existir")
        @Sql("/fixtures/limpeza.sql")
        void shouldReturn404WhenOrderDoesNotExist() throws Exception {
            mockMvc.perform(get("/pedidos/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        }
    }

    @Nested
    @DisplayName("Cancelamento de pedido")
    class CancelarPedido {

        @Test
        @DisplayName("Deve retornar 200 OK com status CANCELADO e devolver o estoque quando o pedido aguardar pagamento (RN11: 3 + 2 = 5)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn11.sql"})
        void shouldReturn200AndReturnStockWhenOrderIsAwaitingPayment() throws Exception {
            mockMvc.perform(post("/pedidos/301/cancelamento"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(301)))
                    .andExpect(jsonPath("$.status").value("CANCELADO"));

            mockMvc.perform(get("/produtos/1"))
                    .andExpect(jsonPath("$.estoque").value(is(5)));
        }

        @Test
        @DisplayName("Deve retornar 422 e não devolver estoque de novo quando o pedido já estiver cancelado (RN11)")
        @Sql({"/fixtures/limpeza.sql", "/fixtures/regras/rn11.sql"})
        void shouldReturn422WhenOrderIsAlreadyCancelled() throws Exception {
            mockMvc.perform(post("/pedidos/302/cancelamento"))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                    .andExpect(jsonPath("$.campos").value(empty()));

            mockMvc.perform(get("/produtos/1"))
                    .andExpect(jsonPath("$.estoque").value(is(3)));
        }
    }
}
