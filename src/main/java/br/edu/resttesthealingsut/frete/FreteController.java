package br.edu.resttesthealingsut.frete;

import br.edu.resttesthealingsut.frete.dto.CotacaoFreteRequest;
import br.edu.resttesthealingsut.frete.dto.CotacaoFreteResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint de cotação de frete. */
@RestController
@RequestMapping("/fretes")
public class FreteController {

    private final FreteService service;

    public FreteController(FreteService service) {
        this.service = service;
    }

    @PostMapping("/cotacao")
    public CotacaoFreteResponse cotar(@Valid @RequestBody CotacaoFreteRequest request) {
        return service.cotar(request);
    }
}
