package br.edu.resttesthealingsut.shared.erro;

/**
 * Lançada pelo código de domínio quando uma regra de negócio é violada. Vira 422
 * {@code REGRA_DE_NEGOCIO}.
 *
 * <p>A mensagem descreve a regra em texto, sem citar o ID (RN0x): os IDs ficam nos comentários do
 * código, e uma API real não os exporia ao cliente.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String message) {
        super(message);
    }
}
