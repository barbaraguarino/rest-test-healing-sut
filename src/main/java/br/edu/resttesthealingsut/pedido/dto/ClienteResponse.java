package br.edu.resttesthealingsut.pedido.dto;

import br.edu.resttesthealingsut.pedido.Cliente;

/** Cliente na resposta de pedido. */
public record ClienteResponse(String nome, String email) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getNome(), cliente.getEmail());
    }
}
