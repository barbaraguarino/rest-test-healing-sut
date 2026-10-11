package br.edu.resttesthealingsut.pedido;

import br.edu.resttesthealingsut.frete.Uf;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Endereço de entrega, guardado nas colunas da tabela {@code pedido}. */
@Embeddable
public class Endereco {

    @Column(name = "endereco_logradouro", nullable = false)
    private String logradouro;

    @Column(name = "endereco_numero", nullable = false)
    private String numero;

    @Column(name = "endereco_complemento")
    private String complemento;

    @Column(name = "endereco_cidade", nullable = false)
    private String cidade;

    // VARCHAR explícito, pelo mesmo motivo de Produto.categoria.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "endereco_uf", nullable = false)
    private Uf uf;

    @Column(name = "endereco_cep", nullable = false)
    private String cep;

    /** Exigido pelo JPA. */
    protected Endereco() {
    }

    public Endereco(String logradouro, String numero, String complemento, String cidade, Uf uf, String cep) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.cidade = cidade;
        this.uf = uf;
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getCidade() {
        return cidade;
    }

    public Uf getUf() {
        return uf;
    }

    public String getCep() {
        return cep;
    }
}
