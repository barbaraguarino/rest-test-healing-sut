package br.edu.resttesthealingsut.carrinho;

import br.edu.resttesthealingsut.carrinho.dto.CarrinhoResponse;
import br.edu.resttesthealingsut.carrinho.dto.ItemCarrinhoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de carrinho. */
@RestController
@RequestMapping("/carrinhos")
public class CarrinhoController {

    private final CarrinhoService service;

    public CarrinhoController(CarrinhoService service) {
        this.service = service;
    }

    /** Cria um carrinho vazio; não recebe corpo. A especificação não pede {@code Location} aqui. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarrinhoResponse criar() {
        return service.criar();
    }

    @PostMapping("/{id}/itens")
    public CarrinhoResponse adicionarItem(@PathVariable Long id, @Valid @RequestBody ItemCarrinhoRequest request) {
        return service.adicionarItem(id, request);
    }

    @GetMapping("/{id}")
    public CarrinhoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
