package br.edu.resttesthealingsut.pedido;

import br.edu.resttesthealingsut.carrinho.Carrinho;
import br.edu.resttesthealingsut.carrinho.CarrinhoService;
import br.edu.resttesthealingsut.frete.CalculadoraFrete;
import br.edu.resttesthealingsut.frete.CalculadoraFrete.ItemFrete;
import br.edu.resttesthealingsut.frete.CalculadoraFrete.ResultadoFrete;
import br.edu.resttesthealingsut.pedido.dto.ClienteRequest;
import br.edu.resttesthealingsut.pedido.dto.EnderecoRequest;
import br.edu.resttesthealingsut.pedido.dto.PedidoRequest;
import br.edu.resttesthealingsut.pedido.dto.PedidoResponse;
import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import br.edu.resttesthealingsut.shared.erro.RecursoNaoEncontradoException;
import br.edu.resttesthealingsut.shared.erro.RegraDeNegocioException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de pedido. Orquestra carrinho, cupom e frete; as regras ficam nas classes de
 * domínio. Criação e cancelamento alteram estoque, por isso são transacionais: se qualquer regra
 * falhar no meio, nenhuma baixa de estoque é gravada.
 */
@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final CarrinhoService carrinhoService;
    private final CalculadoraFrete calculadoraFrete;

    public PedidoService(PedidoRepository repository, CarrinhoService carrinhoService,
                         CalculadoraFrete calculadoraFrete) {
        this.repository = repository;
        this.carrinhoService = carrinhoService;
        this.calculadoraFrete = calculadoraFrete;
    }

    /**
     * Cria um pedido a partir de um carrinho e esvazia o carrinho, para que ele não gere um segundo
     * pedido (uma nova tentativa cai na RN10).
     *
     * @throws RecursoNaoEncontradoException se o carrinho não existir
     * @throws RegraDeNegocioException       se uma regra for violada (RN03, RN08, RN10)
     */
    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Carrinho carrinho = carrinhoService.buscarEntidade(request.carrinhoId());
        ClienteRequest c = request.cliente();
        EnderecoRequest e = request.enderecoEntrega();
        Endereco endereco = new Endereco(e.logradouro(), e.numero(), e.complemento(), e.cidade(), e.uf(), e.cep());

        Pedido pedido = Pedido.criar(
                new Cliente(c.nome(), c.email()), endereco, request.modalidadeFrete(), carrinho.getItens());

        // RN08: cupom opcional; ausente significa desconto zero.
        BigDecimal desconto = request.cupom() == null
                ? Dinheiro.ZERO
                : Cupom.porCodigo(request.cupom()).calcularDesconto(pedido.getSubtotal());

        // RN04–RN07 sobre o subtotal antes do desconto e a UF do endereço de entrega.
        List<ItemFrete> itensFrete = pedido.getItens().stream()
                .map(item -> new ItemFrete(item.getProduto().getPesoGramas(), item.getQuantidade()))
                .toList();
        ResultadoFrete frete = calculadoraFrete.calcular(
                endereco.getUf(), request.modalidadeFrete(), itensFrete, pedido.getSubtotal());

        pedido.registrarValores(desconto, frete.valor());
        carrinho.esvaziar();
        // saveAndFlush gera os ids do pedido e dos itens antes de montar a resposta.
        return PedidoResponse.de(repository.saveAndFlush(pedido));
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id) {
        return PedidoResponse.de(buscarEntidade(id));
    }

    /**
     * Cancela o pedido (RN11).
     *
     * @throws RecursoNaoEncontradoException se o pedido não existir
     * @throws RegraDeNegocioException       se o pedido não estiver aguardando pagamento
     */
    @Transactional
    public PedidoResponse cancelar(Long id) {
        Pedido pedido = buscarEntidade(id);
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }

    private Pedido buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Pedido", id));
    }
}
