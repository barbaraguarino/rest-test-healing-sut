package br.edu.resttesthealingsut.pedido.dto;

import br.edu.resttesthealingsut.frete.Uf;
import br.edu.resttesthealingsut.pedido.Endereco;

/** Endereço de entrega na resposta de pedido. {@code complemento} pode ser {@code null}. */
public record EnderecoResponse(String logradouro, String numero, String complemento, String cidade, Uf uf, String cep) {

    public static EnderecoResponse de(Endereco endereco) {
        return new EnderecoResponse(endereco.getLogradouro(), endereco.getNumero(), endereco.getComplemento(),
                endereco.getCidade(), endereco.getUf(), endereco.getCep());
    }
}
