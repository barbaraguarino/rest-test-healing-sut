package br.edu.resttesthealingsut.shared.erro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Erros que não dependem de um recurso específico. O corpo de erro não faz parte do contrato OpenAPI. */
@SpringBootTest
@AutoConfigureMockMvc
class TratadorGlobalDeErrosTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 404 Not Found quando a rota não existir")
    @Sql("/fixtures/limpeza.sql")
    void shouldReturn404WhenRouteDoesNotExist() throws Exception {
        mockMvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    // Só o código é verificado: se o Jackson aponta ou não um campo num JSON truncado é detalhe dele,
    // e a especificação só promete o campo "quando o Jackson o identifica".
    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o JSON estiver malformado")
    @Sql("/fixtures/limpeza.sql")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": \"LIV-001\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request sem campos quando a requisição não tiver corpo")
    @Sql("/fixtures/limpeza.sql")
    void shouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("Deve retornar 405 Method Not Allowed quando o método HTTP não for aceito")
    @Sql("/fixtures/limpeza.sql")
    void shouldReturn405WhenMethodIsNotSupported() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_NAO_SUPORTADA"));
    }

    @Test
    @DisplayName("Deve retornar 415 Unsupported Media Type quando o tipo de mídia não for aceito")
    @Sql("/fixtures/limpeza.sql")
    void shouldReturn415WhenMediaTypeIsNotSupported() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("produto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_NAO_SUPORTADA"));
    }
}
