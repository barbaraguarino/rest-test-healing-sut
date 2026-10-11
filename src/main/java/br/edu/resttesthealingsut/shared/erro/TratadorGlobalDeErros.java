package br.edu.resttesthealingsut.shared.erro;

import br.edu.resttesthealingsut.shared.erro.ErroResponse.CampoErro;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Converte toda exceção que sai dos controllers no corpo de erro único da especificação
 * ({@link ErroResponse}).
 *
 * <p>Cada exceção tem um método próprio, em vez de herdar de {@code ResponseEntityExceptionHandler}:
 * aquela classe responde no formato ProblemDetail (RFC 9457), que não é o da especificação, e o
 * mapeamento explícito fica legível para quem lê o código, inclusive os agentes do experimento.
 *
 * <p>Os métodos não usam {@code @ResponseStatus}. Se usassem, o springdoc anexaria todas essas
 * respostas de erro a todas as operações do contrato; o contrato da cobaia traz só as respostas
 * de sucesso.
 */
@RestControllerAdvice
public class TratadorGlobalDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    /**
     * Ordena os campos por nome e depois por mensagem. O Hibernate Validator não garante a ordem
     * das violações, e a especificação exige respostas determinísticas.
     */
    private static final Comparator<CampoErro> ORDEM_DOS_CAMPOS =
            Comparator.comparing(CampoErro::campo).thenComparing(CampoErro::mensagem);

    /** Corpo da requisição falhou no {@code @Valid}: obrigatório ausente ou formato inválido. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErro(erro.getField(), erro.getDefaultMessage()))
                .sorted(ORDEM_DOS_CAMPOS)
                .toList();
        return responder(HttpStatus.BAD_REQUEST,
                new ErroResponse(CodigoErro.VALIDACAO, "Requisição com campos inválidos", campos));
    }

    /**
     * Parâmetro de path ou de query que não converte para o tipo esperado, como
     * {@code /produtos/abc} ou {@code ?categoria=FOO}.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        List<CampoErro> campos = List.of(new CampoErro(ex.getName(), "valor inválido"));
        return responder(HttpStatus.BAD_REQUEST,
                new ErroResponse(CodigoErro.VALIDACAO, "Parâmetro com valor inválido", campos));
    }

    /**
     * O corpo não pôde ser lido: JSON malformado, campo desconhecido (desserialização estrita),
     * tipo incompatível, valor de enum inexistente ou corpo ausente.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getCause();
        String mensagem;
        String mensagemDoCampo;
        // A ordem dos testes importa: UnrecognizedPropertyException é um tipo de MismatchedInputException.
        if (causa instanceof UnrecognizedPropertyException) {
            mensagem = "Campo desconhecido no JSON";
            mensagemDoCampo = "campo desconhecido";
        } else if (causa instanceof MismatchedInputException) {
            mensagem = "Tipo ou valor incompatível no JSON";
            mensagemDoCampo = "tipo ou valor incompatível";
        } else if (causa instanceof JacksonException) {
            mensagem = "JSON malformado";
            mensagemDoCampo = "JSON malformado";
        } else {
            mensagem = "Corpo da requisição ausente ou ilegível";
            mensagemDoCampo = mensagem;
        }
        List<CampoErro> campos = caminhoDoCampo(causa)
                .map(caminho -> List.of(new CampoErro(caminho, mensagemDoCampo)))
                .orElse(List.of());
        return responder(HttpStatus.BAD_REQUEST, new ErroResponse(CodigoErro.JSON_INVALIDO, mensagem, campos));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND,
                ErroResponse.semCampos(CodigoErro.RECURSO_NAO_ENCONTRADO, ex.getMessage()));
    }

    /** Rota inexistente: o Spring MVC procura um recurso estático e não encontra. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND,
                ErroResponse.semCampos(CodigoErro.RECURSO_NAO_ENCONTRADO, "Rota inexistente"));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraDeNegocio(RegraDeNegocioException ex) {
        // 422 pelo número: o nome da constante em HttpStatus mudou entre versões do Spring
        // (UNPROCESSABLE_ENTITY / UNPROCESSABLE_CONTENT), e o número não muda.
        return responder(HttpStatusCode.valueOf(422),
                ErroResponse.semCampos(CodigoErro.REGRA_DE_NEGOCIO, ex.getMessage()));
    }

    /**
     * Método HTTP não aceito (405) ou tipo de mídia não aceito (415) ou não produzível (406).
     * O status vem da própria exceção do Spring, que implementa {@link ErrorResponse}.
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeException.class})
    public ResponseEntity<ErroResponse> tratarRequisicaoNaoSuportada(Exception ex) {
        HttpStatusCode status = ((ErrorResponse) ex).getStatusCode();
        return responder(status,
                ErroResponse.semCampos(CodigoErro.REQUISICAO_NAO_SUPORTADA, "Método ou tipo de mídia não suportado"));
    }

    /**
     * Qualquer outro erro. A mensagem é fixa para não expor detalhes internos; o erro completo vai
     * para o log do servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception ex) {
        LOG.error("Erro inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                ErroResponse.semCampos(CodigoErro.ERRO_INTERNO, "Erro interno no servidor"));
    }

    private static ResponseEntity<ErroResponse> responder(HttpStatusCode status, ErroResponse corpo) {
        return ResponseEntity.status(status).body(corpo);
    }

    /**
     * Monta o caminho do campo a partir do erro do Jackson, no mesmo formato do Bean Validation:
     * nomes separados por ponto e índices de lista entre colchetes ({@code itens[0].quantidade}).
     */
    private static Optional<String> caminhoDoCampo(Throwable causa) {
        if (!(causa instanceof JacksonException erroJackson) || erroJackson.getPath().isEmpty()) {
            return Optional.empty();
        }
        StringBuilder caminho = new StringBuilder();
        for (JacksonException.Reference parte : erroJackson.getPath()) {
            if (parte.getPropertyName() != null) {
                if (!caminho.isEmpty()) {
                    caminho.append('.');
                }
                caminho.append(parte.getPropertyName());
            } else if (parte.getIndex() >= 0) {
                caminho.append('[').append(parte.getIndex()).append(']');
            }
        }
        return caminho.isEmpty() ? Optional.empty() : Optional.of(caminho.toString());
    }
}
