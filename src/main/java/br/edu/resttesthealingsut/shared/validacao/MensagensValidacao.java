package br.edu.resttesthealingsut.shared.validacao;

/**
 * Mensagens das anotações de validação de entrada (Bean Validation).
 *
 * <p>As mensagens são escritas aqui, e não tiradas dos textos padrão do Hibernate Validator,
 * porque os textos padrão mudam com o idioma da máquina e quebrariam o determinismo exigido pela
 * especificação. Ficam como constantes porque o atributo {@code message} de uma anotação só
 * aceita valores conhecidos em tempo de compilação.
 */
public final class MensagensValidacao {

    public static final String OBRIGATORIO = "é obrigatório";
    public static final String MAIOR_QUE_ZERO = "deve ser maior que zero";
    public static final String MAIOR_OU_IGUAL_A_ZERO = "deve ser maior ou igual a zero";
    public static final String MINIMO_UM = "deve ser no mínimo 1";
    public static final String OITO_DIGITOS = "deve ter 8 dígitos";
    public static final String PELO_MENOS_UM_ITEM = "deve ter pelo menos um item";

    /** Expressão regular do CEP: exatamente 8 dígitos, sem hífen. */
    public static final String PADRAO_CEP = "\\d{8}";

    private MensagensValidacao() {
    }
}
