package br.edu.resttesthealingsut.produto;

import br.edu.resttesthealingsut.produto.dto.ProdutoRequest;
import br.edu.resttesthealingsut.produto.dto.ProdutoResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de produto. Só traduz HTTP para o service; não contém regra de negócio. */
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    /** Cadastra um produto: 201 com cabeçalho {@code Location} relativo ({@code /produtos/{id}}). */
    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        ProdutoResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/produtos/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** Lista por SKU; sem {@code categoria}, devolve todos os produtos, ativos e inativos. */
    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(required = false) Categoria categoria) {
        return service.listar(categoria);
    }
}
