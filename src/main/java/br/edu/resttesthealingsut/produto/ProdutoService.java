package br.edu.resttesthealingsut.produto;

import br.edu.resttesthealingsut.produto.dto.DimensoesRequest;
import br.edu.resttesthealingsut.produto.dto.ProdutoRequest;
import br.edu.resttesthealingsut.produto.dto.ProdutoResponse;
import br.edu.resttesthealingsut.shared.erro.RecursoNaoEncontradoException;
import br.edu.resttesthealingsut.shared.erro.RegraDeNegocioException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de produto. Os métodos devolvem DTOs, montados dentro da transação: com
 * {@code open-in-view} desligado, o acesso ao banco termina quando o service termina.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    /**
     * Cadastra um produto.
     *
     * @throws RegraDeNegocioException se o SKU já estiver cadastrado
     */
    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        // O SKU é único (especificação). A conferência aqui gera um 422 legível; a restrição
        // UNIQUE do schema.sql é a segunda barreira.
        if (repository.existsBySku(request.sku())) {
            throw new RegraDeNegocioException("SKU já cadastrado");
        }
        DimensoesRequest d = request.dimensoes();
        Produto produto = new Produto(
                request.sku(),
                request.nome(),
                request.descricao(),
                request.preco(),
                request.categoria(),
                request.ativo(),
                request.estoque(),
                request.pesoGramas(),
                new Dimensoes(d.alturaCm(), d.larguraCm(), d.comprimentoCm()),
                request.tags());
        return ProdutoResponse.de(repository.save(produto));
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return ProdutoResponse.de(buscarEntidade(id));
    }

    /** Lista os produtos ordenados por SKU, filtrando pela categoria quando ela for informada. */
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(Categoria categoria) {
        List<Produto> produtos = categoria == null
                ? repository.findAllByOrderBySkuAsc()
                : repository.findByCategoriaOrderBySkuAsc(categoria);
        return produtos.stream().map(ProdutoResponse::de).toList();
    }

    /**
     * Busca a entidade para uso por outros services (carrinho, frete, pedido).
     *
     * @throws RecursoNaoEncontradoException se o produto não existir
     */
    public Produto buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Produto", id));
    }
}
