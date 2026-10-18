package br.edu.resttesthealingsut.shared.erro;

/**
 * Códigos do campo {@code codigo} das respostas de erro, conforme a especificação da cobaia.
 */
public enum CodigoErro {
    /** Campo obrigatório ausente ou com formato inválido, no corpo ou em parâmetro de path/query. */
    VALIDACAO,
    /** JSON malformado, campo desconhecido, tipo incompatível ou corpo ausente. */
    JSON_INVALIDO,
    /** Recurso ou rota inexistente. */
    RECURSO_NAO_ENCONTRADO,
    /** Violação de regra de negócio (RN01 a RN11, SKU duplicado). */
    REGRA_DE_NEGOCIO,
    /** Método HTTP ou tipo de mídia que o endpoint não aceita (405, 406, 415). */
    REQUISICAO_NAO_SUPORTADA,
    /** Erro inesperado no servidor. */
    ERRO_INTERNO
}
