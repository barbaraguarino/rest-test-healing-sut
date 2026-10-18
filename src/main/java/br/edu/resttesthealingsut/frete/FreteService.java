package br.edu.resttesthealingsut.frete;

import br.edu.resttesthealingsut.frete.CalculadoraFrete.ItemFrete;
import br.edu.resttesthealingsut.frete.CalculadoraFrete.ResultadoFrete;
import br.edu.resttesthealingsut.frete.dto.CotacaoFreteRequest;
import br.edu.resttesthealingsut.frete.dto.CotacaoFreteResponse;
import br.edu.resttesthealingsut.frete.dto.ItemCotacaoRequest;
import br.edu.resttesthealingsut.produto.Produto;
import br.edu.resttesthealingsut.produto.ProdutoService;
import br.edu.resttesthealingsut.shared.dominio.Dinheiro;
import br.edu.resttesthealingsut.shared.erro.RecursoNaoEncontradoException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cotação de frete. É só uma simulação: não aplica RN01 (produto ativo) nem RN03 (estoque), que
 * valem para carrinho e pedido.
 */
@Service
public class FreteService {

    private final ProdutoService produtoService;
    private final CalculadoraFrete calculadora;

    public FreteService(ProdutoService produtoService, CalculadoraFrete calculadora) {
        this.produtoService = produtoService;
        this.calculadora = calculadora;
    }

    /**
     * Cota o frete dos itens informados. Itens repetidos com o mesmo {@code produtoId} são somados,
     * como no carrinho. O subtotal (para a RN07) usa o preço atual dos produtos.
     *
     * @throws RecursoNaoEncontradoException se algum produto não existir
     */
    @Transactional(readOnly = true)
    public CotacaoFreteResponse cotar(CotacaoFreteRequest request) {
        // LinkedHashMap mantém a ordem da primeira ocorrência de cada produto.
        Map<Long, Integer> quantidadePorProduto = new LinkedHashMap<>();
        for (ItemCotacaoRequest item : request.itens()) {
            quantidadePorProduto.merge(item.produtoId(), item.quantidade(), Integer::sum);
        }

        List<ItemFrete> itensFrete = new ArrayList<>();
        List<BigDecimal> subtotais = new ArrayList<>();
        quantidadePorProduto.forEach((produtoId, quantidade) -> {
            Produto produto = produtoService.buscarEntidade(produtoId);
            itensFrete.add(new ItemFrete(produto.getPesoGramas(), quantidade));
            subtotais.add(Dinheiro.subtotalDoItem(produto.getPreco(), quantidade));
        });

        ResultadoFrete resultado = calculadora.calcular(
                request.destino().uf(), request.modalidade(), itensFrete, Dinheiro.somar(subtotais));
        return CotacaoFreteResponse.de(request.modalidade(), resultado);
    }
}
