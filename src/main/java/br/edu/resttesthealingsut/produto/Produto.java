package br.edu.resttesthealingsut.produto;

import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import br.edu.resttesthealingsut.shared.erro.RegraDeNegocioException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Produto do catálogo.
 *
 * <p>Concentra a RN03 (estoque): o carrinho e o pedido conferem e alteram o estoque só pelos
 * métodos desta classe, para que a regra fique num único lugar.
 */
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "preco", nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    // Força VARCHAR no banco: sem isto, o Hibernate pode esperar um tipo ENUM nativo do H2, e a
    // validação contra o schema.sql falharia.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "estoque", nullable = false)
    private Integer estoque;

    @Column(name = "peso_gramas", nullable = false)
    private Integer pesoGramas;

    @Embedded
    private Dimensoes dimensoes;

    // A coluna "posicao" guarda a ordem em que as tags foram enviadas, que é a ordem da resposta.
    @ElementCollection
    @CollectionTable(name = "produto_tag", joinColumns = @JoinColumn(name = "produto_id"))
    @OrderColumn(name = "posicao")
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    /** Exigido pelo JPA. */
    protected Produto() {
    }

    /**
     * Cria um produto. O preço é ajustado para 2 casas com {@code HALF_UP} (RN09), em vez de recusar
     * preços com mais casas.
     */
    public Produto(String sku, String nome, String descricao, BigDecimal preco, Categoria categoria,
                   Boolean ativo, Integer estoque, Integer pesoGramas, Dimensoes dimensoes, List<String> tags) {
        this.sku = sku;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = Dinheiro.arredondar(preco);
        this.categoria = categoria;
        this.ativo = ativo;
        this.estoque = estoque;
        this.pesoGramas = pesoGramas;
        this.dimensoes = dimensoes;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    /**
     * RN03: a quantidade pedida não pode passar do estoque.
     *
     * @throws RegraDeNegocioException se o estoque não for suficiente
     */
    public void garantirEstoquePara(int quantidade) {
        if (quantidade > estoque) {
            throw new RegraDeNegocioException("Estoque insuficiente para o produto " + id);
        }
    }

    /** RN03: confere o estoque de novo e o reduz, na criação do pedido. */
    public void baixarEstoque(int quantidade) {
        garantirEstoquePara(quantidade);
        estoque -= quantidade;
    }

    /** RN11: o cancelamento do pedido devolve as quantidades ao estoque. */
    public void devolverEstoque(int quantidade) {
        estoque += quantidade;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public Integer getPesoGramas() {
        return pesoGramas;
    }

    public Dimensoes getDimensoes() {
        return dimensoes;
    }

    /** Cópia somente leitura, para que a lista não seja alterada por fora da entidade. */
    public List<String> getTags() {
        return List.copyOf(tags);
    }
}
