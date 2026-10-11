package br.edu.resttesthealingsut.carrinho;

import br.edu.resttesthealingsut.produto.Produto;
import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Item do carrinho. Guarda só o produto e a quantidade: o preço não é congelado, e sim lido do
 * produto a cada consulta, para que carrinho e pedido sempre usem o mesmo preço.
 */
@Entity
@Table(name = "item_carrinho")
public class ItemCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrinho_id", nullable = false)
    private Carrinho carrinho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    /** Exigido pelo JPA. */
    protected ItemCarrinho() {
    }

    /** Usado só por {@link Carrinho}, que aplica as regras antes de criar o item. */
    ItemCarrinho(Carrinho carrinho, Produto produto, int quantidade) {
        this.carrinho = carrinho;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    void alterarQuantidade(int novaQuantidade) {
        this.quantidade = novaQuantidade;
    }

    /** Preço atual do produto. */
    public BigDecimal getPrecoUnitario() {
        return produto.getPreco();
    }

    /** RN09: preço unitário × quantidade. */
    public BigDecimal calcularSubtotal() {
        return Dinheiro.subtotalDoItem(getPrecoUnitario(), quantidade);
    }

    public Produto getProduto() {
        return produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }
}
