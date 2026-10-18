package br.edu.resttesthealingsut.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Dados do cliente, guardados nas colunas da tabela {@code pedido}. */
@Embeddable
public class Cliente {

    @Column(name = "cliente_nome", nullable = false)
    private String nome;

    @Column(name = "cliente_email", nullable = false)
    private String email;

    /** Exigido pelo JPA. */
    protected Cliente() {
    }

    public Cliente(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
