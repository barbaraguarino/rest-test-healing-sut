# Matriz de cobertura do contrato

Para cada campo dos schemas do contrato OpenAPI (`docs/openapi.json`), os testes que **enviam** o campo (requisição) ou o **verificam** (resposta). Comprova o requisito "todo campo da resposta é verificado em pelo menos um teste, e todo campo da requisição é enviado em pelo menos um teste" do README e indica quais testes quebram com uma mudança de contrato num campo.

Como cada categoria de mudança quebra os testes listados:

- **Campo da requisição** (renomeação, remoção ou alteração de tipo): o teste envia o nome antigo ou o tipo antigo e recebe 400 em vez do status esperado. Vale também para os testes de erro listados, porque o 400 por campo desconhecido chega antes do erro que eles verificam.
- **Campo obrigatório novo na requisição:** quebra todos os testes que enviam aquele objeto de requisição, porque nenhum envia o campo novo.
- **Campo da resposta:** a asserção sobre o campo falha.

Os nomes dos testes estão no formato `Classe.metodo`, sem o sufixo `ApiTest`. O teste parametrizado `Frete.rn04FreteBaseEPrazoPorRegiao` conta como um teste com 5 execuções.

Os parâmetros de caminho (`id`) e de consulta (`categoria`) também são exercitados, mas ficam fora das categorias de mudança (ver "Categorias de mudança e onde elas quebram um teste" no README).

## Requisição

### ProdutoRequest e DimensoesRequest (`POST /produtos`)

| Campo | Enviado em |
|---|---|
| `sku`, `preco`, `categoria`, `ativo`, `estoque`, `pesoGramas`, `dimensoes`, `dimensoes.larguraCm`, `dimensoes.comprimentoCm` | Produto.criarProdutoComTodosOsCampos, Produto.criarProdutoSemCamposOpcionais, Produto.rn09PrecoComTresCasasArredondaHalfUp, Produto.skuDuplicadoRecusado, Produto.obrigatoriosAusentesRecusados |
| `nome`, `dimensoes.alturaCm` | Produto.criarProdutoComTodosOsCampos, Produto.criarProdutoSemCamposOpcionais, Produto.rn09PrecoComTresCasasArredondaHalfUp, Produto.skuDuplicadoRecusado |
| `descricao`, `tags` | Produto.criarProdutoComTodosOsCampos |

### ItemCarrinhoRequest (`POST /carrinhos/{id}/itens`)

| Campo | Enviado em |
|---|---|
| `produtoId` | Carrinho.incluirItem, Carrinho.incluirProdutoRepetidoSomaNoItemExistente, Carrinho.rn01ProdutoInativoRecusado, Carrinho.rn02UmaUnidadeAbaixoDoMaximoAceita, Carrinho.rn02NoMaximoRecusaMaisUma, Carrinho.rn03UmaUnidadeAbaixoDoEstoqueAceita, Carrinho.rn03NoEstoqueRecusaMaisUma, Carrinho.campoDesconhecidoRecusado, Carrinho.tipoIncompativelRecusado, Carrinho.quantidadeAusenteRecusada |
| `quantidade` | Os mesmos, exceto Carrinho.quantidadeAusenteRecusada |

### CotacaoFreteRequest, DestinoRequest e ItemCotacaoRequest (`POST /fretes/cotacao`)

| Campo | Enviado em |
|---|---|
| `destino`, `destino.cep`, `destino.uf`, `modalidade`, `itens`, `itens[].produtoId`, `itens[].quantidade` | Todos os 13 testes de FreteApiTest |

### PedidoRequest, ClienteRequest e EnderecoRequest (`POST /pedidos`)

| Campo | Enviado em |
|---|---|
| `carrinhoId`, `cliente`, `cliente.nome`, `cliente.email`, `enderecoEntrega`, `enderecoEntrega.numero`, `enderecoEntrega.cidade`, `enderecoEntrega.uf`, `modalidadeFrete` | Pedido.criarPedidoComTodosOsCampos, Pedido.criarPedidoSemCamposOpcionais, Pedido.rn03CriacaoBaixaEstoque, Pedido.rn03CriacaoConfereEstoqueDeNovo, Pedido.rn07FreteGratisUsaSubtotalAntesDoDesconto, Pedido.rn08SubtotalNoMinimoAceitaCupom, Pedido.rn08SubtotalAbaixoDoMinimoRecusaCupom, Pedido.rn08CupomInexistenteRecusado, Pedido.rn08CupomDiferenciaMaiusculas, Pedido.rn09DescontoArredondaHalfUp, Pedido.rn10CarrinhoVazioRecusado, Pedido.criacaoEsvaziaCarrinho, Pedido.obrigatoriosDoEnderecoAusentesRecusados, Pedido.criarPedidoComCarrinhoInexistente |
| `enderecoEntrega.logradouro`, `enderecoEntrega.cep` | Os mesmos, exceto Pedido.obrigatoriosDoEnderecoAusentesRecusados |
| `cupom` | Pedido.criarPedidoComTodosOsCampos, Pedido.rn07FreteGratisUsaSubtotalAntesDoDesconto, Pedido.rn08SubtotalNoMinimoAceitaCupom, Pedido.rn08SubtotalAbaixoDoMinimoRecusaCupom, Pedido.rn08CupomInexistenteRecusado, Pedido.rn08CupomDiferenciaMaiusculas, Pedido.rn09DescontoArredondaHalfUp |
| `enderecoEntrega.complemento` | Pedido.criarPedidoComTodosOsCampos |

## Resposta

### ProdutoResponse e DimensoesResponse

| Campo | Verificado em |
|---|---|
| `id`, `nome`, `descricao`, `categoria`, `pesoGramas`, `dimensoes.alturaCm`, `dimensoes.larguraCm`, `dimensoes.comprimentoCm` | Produto.criarProdutoComTodosOsCampos, Produto.buscarProdutoPorId |
| `sku` | Produto.criarProdutoComTodosOsCampos, Produto.buscarProdutoPorId, Produto.listarProdutosPorCategoria, Produto.listarTodosOsProdutosIncluiInativos |
| `preco` | Produto.criarProdutoComTodosOsCampos, Produto.buscarProdutoPorId, Produto.rn09PrecoComTresCasasArredondaHalfUp |
| `ativo` | Produto.criarProdutoComTodosOsCampos, Produto.criarProdutoSemCamposOpcionais, Produto.buscarProdutoPorId, Produto.listarTodosOsProdutosIncluiInativos |
| `estoque` | Produto.criarProdutoComTodosOsCampos, Produto.criarProdutoSemCamposOpcionais, Produto.buscarProdutoPorId, Pedido.rn03CriacaoBaixaEstoque, Pedido.rn03CriacaoConfereEstoqueDeNovo, Pedido.rn11CancelarDevolveEstoque, Pedido.rn11PedidoCanceladoNaoCancelaDeNovo |
| `tags` | Produto.criarProdutoComTodosOsCampos, Produto.criarProdutoSemCamposOpcionais, Produto.buscarProdutoPorId |
| `categoria` (na listagem) | também Produto.listarProdutosPorCategoria |

### CarrinhoResponse e ItemCarrinhoResponse

| Campo | Verificado em |
|---|---|
| `id` | Carrinho.criarCarrinhoVazio, Carrinho.incluirItem, Carrinho.buscarCarrinho |
| `itens` | Carrinho.criarCarrinhoVazio, Carrinho.incluirItem, Carrinho.buscarCarrinho, Carrinho.incluirProdutoRepetidoSomaNoItemExistente, Pedido.rn03CriacaoConfereEstoqueDeNovo, Pedido.criacaoEsvaziaCarrinho |
| `subtotal` | Carrinho.criarCarrinhoVazio, Carrinho.incluirItem, Carrinho.incluirProdutoRepetidoSomaNoItemExistente, Carrinho.buscarCarrinho, Pedido.criacaoEsvaziaCarrinho |
| `itens[].produtoId` | Carrinho.incluirItem, Carrinho.incluirProdutoRepetidoSomaNoItemExistente, Carrinho.buscarCarrinho |
| `itens[].quantidade` | Carrinho.incluirItem, Carrinho.incluirProdutoRepetidoSomaNoItemExistente, Carrinho.buscarCarrinho, Pedido.rn03CriacaoConfereEstoqueDeNovo |
| `itens[].precoUnitario`, `itens[].subtotal` | Carrinho.incluirItem, Carrinho.buscarCarrinho |

### CotacaoFreteResponse

| Campo | Verificado em |
|---|---|
| `modalidade` | Frete.cotarComTodosOsCampos, Frete.rn06ExpressaMultiplicaValorEReduzPrazo |
| `valor` | Frete.cotarComTodosOsCampos, Frete.rn04FreteBaseEPrazoPorRegiao, Frete.rn05PesoExatoEmQuilos, Frete.rn05QuiloIniciadoArredondaParaCima, Frete.rn06ExpressaMultiplicaValorEReduzPrazo, Frete.rn06ExpressaRespeitaPrazoMinimo, Frete.rn07SubtotalNoLimiteTemFreteGratis, Frete.rn07SubtotalAbaixoDoLimiteCobraFrete, Frete.rn07ExpressaNaoTemFreteGratis, Frete.cotarSomaItensRepetidosEIgnoraAtivoEEstoque |
| `prazoDias` | Frete.cotarComTodosOsCampos, Frete.rn04FreteBaseEPrazoPorRegiao, Frete.rn06ExpressaMultiplicaValorEReduzPrazo, Frete.rn06ExpressaRespeitaPrazoMinimo, Frete.rn07SubtotalNoLimiteTemFreteGratis |
| `freteGratis` | Frete.cotarComTodosOsCampos, Frete.rn06ExpressaMultiplicaValorEReduzPrazo, Frete.rn07SubtotalNoLimiteTemFreteGratis, Frete.rn07SubtotalAbaixoDoLimiteCobraFrete, Frete.rn07ExpressaNaoTemFreteGratis |
| `pesoTotalGramas` | Frete.cotarComTodosOsCampos, Frete.rn06ExpressaMultiplicaValorEReduzPrazo, Frete.cotarSomaItensRepetidosEIgnoraAtivoEEstoque |

### PedidoResponse, ClienteResponse, EnderecoResponse e ItemPedidoResponse

| Campo | Verificado em |
|---|---|
| `cliente.nome`, `cliente.email`, `enderecoEntrega.logradouro`, `enderecoEntrega.numero`, `enderecoEntrega.cidade`, `enderecoEntrega.uf`, `enderecoEntrega.cep`, `modalidadeFrete`, `itens[].produtoId`, `itens[].nomeProduto`, `itens[].quantidade`, `itens[].precoUnitario`, `itens[].subtotal` | Pedido.criarPedidoComTodosOsCampos, Pedido.buscarPedido |
| `id`, `status` | Pedido.criarPedidoComTodosOsCampos, Pedido.buscarPedido, Pedido.rn11CancelarDevolveEstoque |
| `itens` | Pedido.criarPedidoComTodosOsCampos, Pedido.buscarPedido |
| `enderecoEntrega.complemento` | Pedido.criarPedidoComTodosOsCampos, Pedido.criarPedidoSemCamposOpcionais, Pedido.buscarPedido |
| `subtotal` | Pedido.criarPedidoComTodosOsCampos, Pedido.criarPedidoSemCamposOpcionais, Pedido.buscarPedido |
| `frete` | Pedido.criarPedidoComTodosOsCampos, Pedido.criarPedidoSemCamposOpcionais, Pedido.rn07FreteGratisUsaSubtotalAntesDoDesconto, Pedido.buscarPedido |
| `desconto`, `total` | Pedido.criarPedidoComTodosOsCampos, Pedido.criarPedidoSemCamposOpcionais, Pedido.rn09DescontoArredondaHalfUp, Pedido.buscarPedido |

## Regras de negócio

Testes por regra e onde fica o valor de que cada teste depende. "Fixture" quer dizer que uma mudança no valor da regra desatualiza um dado do arquivo SQL; "asserção" quer dizer que o valor da regra aparece no valor esperado do teste.

| Regra | Testes | Fixture | Valor da regra em |
|---|---|---|---|
| RN01 | Carrinho.rn01ProdutoInativoRecusado | `regra-rn01.sql` | Fixture (produto inativo) |
| RN02 | Carrinho.rn02UmaUnidadeAbaixoDoMaximoAceita, Carrinho.rn02NoMaximoRecusaMaisUma | `regra-rn02.sql` | Fixture (quantidade no carrinho) |
| RN03 | Carrinho.rn03UmaUnidadeAbaixoDoEstoqueAceita, Carrinho.rn03NoEstoqueRecusaMaisUma, Pedido.rn03CriacaoBaixaEstoque, Pedido.rn03CriacaoConfereEstoqueDeNovo | `regra-rn03.sql` | Fixture (estoque e quantidade) |
| RN04 | Frete.rn04FreteBaseEPrazoPorRegiao | `frete.sql` | Asserção |
| RN05 | Frete.rn05PesoExatoEmQuilos, Frete.rn05QuiloIniciadoArredondaParaCima | `regra-rn05.sql` | Fixture (peso no limite do quilo) e asserção (adicional por quilo) |
| RN06 | Frete.rn04FreteBaseEPrazoPorRegiao, Frete.rn06ExpressaMultiplicaValorEReduzPrazo, Frete.rn06ExpressaRespeitaPrazoMinimo | `frete.sql` | Asserção |
| RN07 | Frete.rn07SubtotalNoLimiteTemFreteGratis, Frete.rn07SubtotalAbaixoDoLimiteCobraFrete, Frete.rn07ExpressaNaoTemFreteGratis, Pedido.rn07FreteGratisUsaSubtotalAntesDoDesconto | `regra-rn07.sql` | Fixture (preço no limite) |
| RN08 | Pedido.rn08SubtotalNoMinimoAceitaCupom, Pedido.rn08SubtotalAbaixoDoMinimoRecusaCupom, Pedido.rn08CupomInexistenteRecusado, Pedido.rn08CupomDiferenciaMaiusculas | `regra-rn08.sql` | Fixture (subtotal mínimo). O percentual aparece na asserção de Pedido.criarPedidoComTodosOsCampos e Pedido.rn09DescontoArredondaHalfUp |
| RN09 | Produto.rn09PrecoComTresCasasArredondaHalfUp, Pedido.rn09DescontoArredondaHalfUp, Pedido.criarPedidoComTodosOsCampos | `pedidos.sql` | Asserção |
| RN10 | Pedido.rn10CarrinhoVazioRecusado | `pedidos.sql` | Não tem valor numérico |
| RN11 | Pedido.rn11CancelarDevolveEstoque, Pedido.rn11PedidoCanceladoNaoCancelaDeNovo | `regra-rn11.sql` | Fixture (status do pedido) |

As regras de limite (RN02, RN03, RN07 e os limites da RN05 e da RN08) têm um teste em cada lado do limite: uma mudança no valor, para mais ou para menos, quebra pelo menos um deles. Esses testes verificam só o resultado da regra (aceito ou recusado, frete grátis ou não), e nunca um valor calculado a partir do dado da fixture, para que o reparo de uma mudança de limite fique na fixture.
