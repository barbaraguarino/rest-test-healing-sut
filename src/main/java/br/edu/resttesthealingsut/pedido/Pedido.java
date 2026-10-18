package br.edu.resttesthealingsut.pedido;

import br.edu.resttesthealingsut.carrinho.ItemCarrinho;
import br.edu.resttesthealingsut.frete.ModalidadeFrete;
import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import br.edu.resttesthealingsut.shared.erro.RegraDeNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pedido criado a partir de um carrinho.
 *
 * <p>Concentra RN10 (carrinho vazio), a fórmula do total (RN09) e RN11 (cancelamento).
 * A criação tem dois passos, porque desconto (RN08) e frete (RN04–RN07) dependem do subtotal:
 * {@link #criar} monta os itens e o subtotal; {@link #registrarValores} completa desconto, frete e
 * total. O service chama os dois dentro da mesma transação.
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // VARCHAR explícito, pelo mesmo motivo de Produto.categoria.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false)
    private StatusPedido status;

    @Embedded
    private Cliente cliente;

    @Embedded
    private Endereco enderecoEntrega;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "modalidade_frete", nullable = false)
    private ModalidadeFrete modalidadeFrete;

    // Itens na ordem de inclusão no carrinho (ver Carrinho.itens).
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "desconto", nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto;

    @Column(name = "frete", nullable = false, precision = 12, scale = 2)
    private BigDecimal frete;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** Exigido pelo JPA. */
    protected Pedido() {
    }

    /**
     * Primeiro passo da criação: copia os itens do carrinho, congelando nome e preço (RN09), e baixa
     * o estoque de cada produto (RN03).
     *
     * @throws RegraDeNegocioException se o carrinho estiver vazio (RN10) ou faltar estoque (RN03)
     */
    public static Pedido criar(Cliente cliente, Endereco enderecoEntrega, ModalidadeFrete modalidadeFrete,
                               List<ItemCarrinho> itensDoCarrinho) {
        // RN10: não é possível criar pedido a partir de carrinho vazio.
        if (itensDoCarrinho.isEmpty()) {
            throw new RegraDeNegocioException("Não é possível criar pedido a partir de carrinho vazio");
        }
        Pedido pedido = new Pedido();
        pedido.status = StatusPedido.AGUARDANDO_PAGAMENTO;
        pedido.cliente = cliente;
        pedido.enderecoEntrega = enderecoEntrega;
        pedido.modalidadeFrete = modalidadeFrete;
        for (ItemCarrinho itemCarrinho : itensDoCarrinho) {
            // RN03: a criação do pedido confere o estoque de novo e o reduz.
            itemCarrinho.getProduto().baixarEstoque(itemCarrinho.getQuantidade());
            pedido.itens.add(new ItemPedido(pedido, itemCarrinho.getProduto(), itemCarrinho.getQuantidade()));
        }
        pedido.subtotal = Dinheiro.somar(pedido.itens.stream().map(ItemPedido::getSubtotal).toList());
        return pedido;
    }

    /** Segundo passo da criação. RN09: total = subtotal − desconto + frete. */
    public void registrarValores(BigDecimal desconto, BigDecimal frete) {
        this.desconto = Dinheiro.arredondar(desconto);
        this.frete = Dinheiro.arredondar(frete);
        this.total = Dinheiro.arredondar(subtotal.subtract(this.desconto).add(this.frete));
    }

    /**
     * Cancela o pedido e devolve as quantidades ao estoque (RN11).
     *
     * @throws RegraDeNegocioException se o pedido não estiver aguardando pagamento
     */
    public void cancelar() {
        // RN11: só é possível cancelar pedido com status AGUARDANDO_PAGAMENTO.
        if (status != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new RegraDeNegocioException("Só é possível cancelar pedido aguardando pagamento");
        }
        for (ItemPedido item : itens) {
            item.getProduto().devolverEstoque(item.getQuantidade());
        }
        status = StatusPedido.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Endereco getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public ModalidadeFrete getModalidadeFrete() {
        return modalidadeFrete;
    }

    public List<ItemPedido> getItens() {
        return List.copyOf(itens);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
