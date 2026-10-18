package br.edu.resttesthealingsut.pedido;

import br.edu.resttesthealingsut.pedido.dto.PedidoRequest;
import br.edu.resttesthealingsut.pedido.dto.PedidoResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de pedido. */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    /** Cria um pedido: 201 com cabeçalho {@code Location} relativo ({@code /pedidos/{id}}). */
    @PostMapping
    // Repete o 201 do ResponseEntity.created: sem a anotação, o springdoc publica 200 no contrato.
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/pedidos/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** Cancela o pedido; não recebe corpo. */
    @PostMapping("/{id}/cancelamento")
    public PedidoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
