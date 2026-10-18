package br.edu.resttesthealingsut.carrinho;

import br.edu.resttesthealingsut.carrinho.dto.CarrinhoResponse;
import br.edu.resttesthealingsut.carrinho.dto.ItemCarrinhoRequest;
import br.edu.resttesthealingsut.produto.Produto;
import br.edu.resttesthealingsut.produto.ProdutoService;
import br.edu.resttesthealingsut.shared.erro.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de carrinho. As regras de inclusão ficam em {@link Carrinho}. */
@Service
public class CarrinhoService {

    private final CarrinhoRepository repository;
    private final ProdutoService produtoService;

    public CarrinhoService(CarrinhoRepository repository, ProdutoService produtoService) {
        this.repository = repository;
        this.produtoService = produtoService;
    }

    @Transactional
    public CarrinhoResponse criar() {
        return CarrinhoResponse.de(repository.save(new Carrinho()));
    }

    /**
     * Inclui um item. A transação garante que o estoque conferido é o do mesmo momento da gravação.
     *
     * @throws RecursoNaoEncontradoException se o carrinho ou o produto não existir
     */
    @Transactional
    public CarrinhoResponse adicionarItem(Long carrinhoId, ItemCarrinhoRequest request) {
        Carrinho carrinho = buscarEntidade(carrinhoId);
        Produto produto = produtoService.buscarEntidade(request.produtoId());
        carrinho.adicionar(produto, request.quantidade());
        // saveAndFlush grava agora e gera o id do item novo antes de montar a resposta.
        return CarrinhoResponse.de(repository.saveAndFlush(carrinho));
    }

    @Transactional(readOnly = true)
    public CarrinhoResponse buscar(Long id) {
        return CarrinhoResponse.de(buscarEntidade(id));
    }

    /**
     * Busca a entidade para uso pelo service de pedido.
     *
     * @throws RecursoNaoEncontradoException se o carrinho não existir
     */
    public Carrinho buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Carrinho", id));
    }
}
