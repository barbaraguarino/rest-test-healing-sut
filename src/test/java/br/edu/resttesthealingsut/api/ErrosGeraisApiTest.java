package br.edu.resttesthealingsut.api;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

/** Erros que não dependem de um recurso específico. O corpo de erro não faz parte do contrato OpenAPI. */
@SpringBootTest
@AutoConfigureMockMvc
class ErrosGeraisApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rota inexistente: 404 RECURSO_NAO_ENCONTRADO")
    @Sql("/fixtures/limpeza.sql")
    void rotaInexistente() throws Exception {
        mockMvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(is("RECURSO_NAO_ENCONTRADO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    // Só o código é verificado: se o Jackson aponta ou não um campo num JSON truncado é detalhe dele,
    // e a especificação só promete o campo "quando o Jackson o identifica".
    @Test
    @DisplayName("JSON malformado: 400 JSON_INVALIDO")
    @Sql("/fixtures/limpeza.sql")
    void jsonMalformado() throws Exception {
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content("{\"sku\": \"LIV-001\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("JSON_INVALIDO")));
    }

    @Test
    @DisplayName("Corpo ausente: 400 JSON_INVALIDO sem campos")
    @Sql("/fixtures/limpeza.sql")
    void corpoAusente() throws Exception {
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(is("JSON_INVALIDO")))
                .andExpect(jsonPath("$.campos").value(empty()));
    }

    @Test
    @DisplayName("Método HTTP não aceito: 405 REQUISICAO_NAO_SUPORTADA")
    @Sql("/fixtures/limpeza.sql")
    void metodoNaoSuportado() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value(is("REQUISICAO_NAO_SUPORTADA")));
    }

    @Test
    @DisplayName("Tipo de mídia não aceito: 415 REQUISICAO_NAO_SUPORTADA")
    @Sql("/fixtures/limpeza.sql")
    void tipoDeMidiaNaoSuportado() throws Exception {
        mockMvc.perform(post("/produtos").contentType(MediaType.TEXT_PLAIN).content("produto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value(is("REQUISICAO_NAO_SUPORTADA")));
    }
}
