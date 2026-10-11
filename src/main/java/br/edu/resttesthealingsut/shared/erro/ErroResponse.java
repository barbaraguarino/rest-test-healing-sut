package br.edu.resttesthealingsut.shared.erro;

import java.util.List;

/**
 * Corpo único de todas as respostas de erro. Não tem data nem hora, para manter o determinismo.
 *
 * @param codigo   categoria do erro
 * @param mensagem descrição em texto explícito
 * @param campos   campos envolvidos; lista vazia quando não se aplica
 */
public record ErroResponse(CodigoErro codigo, String mensagem, List<CampoErro> campos) {

    /** Erro sem campos associados. */
    public static ErroResponse semCampos(CodigoErro codigo, String mensagem) {
        return new ErroResponse(codigo, mensagem, List.of());
    }

    /**
     * Um campo com problema.
     *
     * @param campo    caminho do campo, como {@code dimensoes.alturaCm} ou {@code itens[0].quantidade}
     * @param mensagem o que está errado nele
     */
    public record CampoErro(String campo, String mensagem) {
    }
}
