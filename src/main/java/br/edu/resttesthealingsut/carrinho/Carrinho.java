package br.edu.resttesthealingsut.carrinho;

import br.edu.resttesthealingsut.produto.Produto;
import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import br.edu.resttesthealingsut.shared.erro.RegraDeNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Carrinho de compras.
 *
 * <p>Concentra as regras de inclusão de item: RN01 (produto ativo) e RN02 (máximo por produto).
 * A RN03 (estoque) é conferida aqui, mas definida em {@link Produto}.
 */
@Entity
@Table(name = "carrinho")
public class Carrinho {

    /** RN02: cada produto pode ter no máximo 10 unidades no carrinho. */
    public static final int MAXIMO_UNIDADES_POR_PRODUTO = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Itens na ordem de inclusão: o id é gerado em ordem crescente, então ordenar por id basta,
    // e as fixtures não precisam de uma coluna de posição.
    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemCarrinho> itens = new ArrayList<>();

    /**
     * Inclui um produto no carrinho. Se o produto já estiver no carrinho, soma a quantidade no item
     * existente, que mantém sua posição; RN02 e RN03 valem sobre a soma.
     *
     * @throws RegraDeNegocioException se o produto estiver inativo (RN01), se a soma passar do
     *                                 máximo por produto (RN02) ou do estoque (RN03)
     */
    public void adicionar(Produto produto, int quantidade) {
        // RN01: produto inativo não pode ser incluído no carrinho.
        if (!produto.isAtivo()) {
            throw new RegraDeNegocioException("Produto inativo não pode ser incluído no carrinho");
        }
        Optional<ItemCarrinho> existente = buscarItem(produto.getId());
        int quantidadeTotal = existente.map(ItemCarrinho::getQuantidade).orElse(0) + quantidade;

        // RN02: máximo de unidades por produto, sobre a quantidade total no carrinho.
        if (quantidadeTotal > MAXIMO_UNIDADES_POR_PRODUTO) {
            throw new RegraDeNegocioException(
                    "Cada produto pode ter no máximo " + MAXIMO_UNIDADES_POR_PRODUTO + " unidades no carrinho");
        }
        // RN03: a quantidade total não pode passar do estoque.
        produto.garantirEstoquePara(quantidadeTotal);

        if (existente.isPresent()) {
            existente.get().alterarQuantidade(quantidadeTotal);
        } else {
            itens.add(new ItemCarrinho(this, produto, quantidade));
        }
    }

    /** Remove todos os itens. Chamado quando o carrinho vira pedido, para que não gere outro. */
    public void esvaziar() {
        itens.clear();
    }

    public boolean estaVazio() {
        return itens.isEmpty();
    }

    /** RN09: soma dos subtotais dos itens, calculados com o preço atual de cada produto. */
    public BigDecimal calcularSubtotal() {
        return Dinheiro.somar(itens.stream().map(ItemCarrinho::calcularSubtotal).toList());
    }

    private Optional<ItemCarrinho> buscarItem(Long produtoId) {
        return itens.stream().filter(item -> item.getProduto().getId().equals(produtoId)).findFirst();
    }

    public Long getId() {
        return id;
    }

    public List<ItemCarrinho> getItens() {
        return List.copyOf(itens);
    }
}
