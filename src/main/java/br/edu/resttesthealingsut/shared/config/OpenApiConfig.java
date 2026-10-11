package br.edu.resttesthealingsut.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados do contrato OpenAPI gerado pelo springdoc em {@code /v3/api-docs}.
 *
 * <p>O contrato declarado do experimento não é este endpoint, e sim o retrato salvo em
 * {@code docs/openapi.json}, que só muda por decisão explícita.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI contratoDaCobaia() {
        return new OpenAPI()
                .info(new Info()
                        .title("API cobaia de e-commerce")
                        .description("Objeto do experimento de manutenção de testes de APIs REST com agentes de IA")
                        .version("0.0.1"))
                // Servidor fixo e relativo. Sem isto, o springdoc escreve no contrato a URL de quem fez
                // a requisição (por exemplo, http://localhost:8080), e o retrato salvo mudaria de uma
                // máquina para outra.
                .servers(List.of(new Server().url("/")));
    }
}
