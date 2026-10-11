package br.edu.resttesthealingsut.shared.erro;

/**
 * Lançada quando um recurso buscado pelo ID não existe. Vira 404 {@code RECURSO_NAO_ENCONTRADO}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }

    /** Monta a mensagem padrão, por exemplo "Produto 1000 não encontrado". */
    public static RecursoNaoEncontradoException de(String recurso, Long id) {
        return new RecursoNaoEncontradoException(recurso + " " + id + " não encontrado");
    }
}
