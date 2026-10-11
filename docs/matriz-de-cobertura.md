# Matriz de cobertura do contrato

Para cada campo dos schemas do contrato OpenAPI (`docs/openapi.json`), os testes que **enviam** o campo (requisição) ou o **verificam** (resposta). Comprova o requisito "todo campo da resposta é verificado em pelo menos um teste, e todo campo da requisição é enviado em pelo menos um teste" do README e indica quais testes quebram com uma mudança de contrato num campo.

Como cada categoria de mudança quebra os testes listados:

- **Campo da requisição** (renomeação, remoção ou alteração de tipo): o teste envia o nome antigo ou o tipo antigo e recebe 400 em vez do status esperado. Vale também para os testes de erro listados, porque o 400 por campo desconhecido chega antes do erro que eles verificam.
- **Campo obrigatório novo na requisição:** quebra todos os testes que enviam aquele objeto de requisição, porque nenhum envia o campo novo.
- **Campo da resposta:** a asserção sobre o campo falha.

Os nomes dos testes estão no formato `Classe.metodo`, sem o sufixo `ControllerTest` (por exemplo, `Carrinho.` é `carrinho/CarrinhoControllerTest`). O teste parametrizado `Frete.shouldChargeRegionBaseAndDeadlineWhenModeIsStandard` conta como um teste com 5 execuções.

Os parâmetros de caminho (`id`) e de consulta (`categoria`) também são exercitados, mas ficam fora das categorias de mudança (ver "Categorias de mudança e onde elas quebram um teste" no README).

## Requisição

### ProdutoRequest e DimensoesRequest (`POST /produtos`)

| Campo | Enviado em |
|---|---|
| `sku`, `preco`, `categoria`, `ativo`, `estoque`, `pesoGramas`, `dimensoes`, `dimensoes.larguraCm`, `dimensoes.comprimentoCm` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn201WithNullDescriptionAndEmptyTagsWhenOptionalFieldsAreMissing, Produto.shouldRoundPriceHalfUpWhenPriceHasThreeDecimalPlaces, Produto.shouldReturn422WhenSkuAlreadyExists, Produto.shouldReturn400WithSortedFieldsWhenRequiredFieldsAreMissing |
| `nome`, `dimensoes.alturaCm` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn201WithNullDescriptionAndEmptyTagsWhenOptionalFieldsAreMissing, Produto.shouldRoundPriceHalfUpWhenPriceHasThreeDecimalPlaces, Produto.shouldReturn422WhenSkuAlreadyExists |
| `descricao`, `tags` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete |

### ItemCarrinhoRequest (`POST /carrinhos/{id}/itens`)

| Campo | Enviado em |
|---|---|
| `produtoId` | Carrinho.shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable, Carrinho.shouldAddQuantityToExistingItemWhenProductIsAlreadyInCart, Carrinho.shouldReturn422WhenProductIsInactive, Carrinho.shouldReturn200WhenCartIsOneUnitBelowMaximum, Carrinho.shouldReturn422WhenCartAlreadyHasMaximumUnits, Carrinho.shouldReturn200WhenCartIsOneUnitBelowStock, Carrinho.shouldReturn422WhenCartAlreadyHasAllUnitsInStock, Carrinho.shouldReturn400WhenBodyHasUnknownField, Carrinho.shouldReturn400WhenQuantityIsNotANumber, Carrinho.shouldReturn400WhenQuantityIsMissing |
| `quantidade` | Os mesmos, exceto Carrinho.shouldReturn400WhenQuantityIsMissing |

### CotacaoFreteRequest, DestinoRequest e ItemCotacaoRequest (`POST /fretes/cotacao`)

| Campo | Enviado em |
|---|---|
| `destino`, `destino.cep`, `destino.uf`, `modalidade`, `itens`, `itens[].produtoId`, `itens[].quantidade` | Todos os 13 testes de FreteControllerTest |

### PedidoRequest, ClienteRequest e EnderecoRequest (`POST /pedidos`)

| Campo | Enviado em |
|---|---|
| `carrinhoId`, `cliente`, `cliente.nome`, `cliente.email`, `enderecoEntrega`, `enderecoEntrega.numero`, `enderecoEntrega.cidade`, `enderecoEntrega.uf`, `modalidadeFrete` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn201WithZeroDiscountWhenOptionalFieldsAreMissing, Pedido.shouldDecreaseStockWhenOrderIsCreated, Pedido.shouldReturn422AndKeepStockAndCartWhenStockIsNoLongerEnough, Pedido.shouldGiveFreeShippingUsingSubtotalBeforeDiscount, Pedido.shouldAcceptCouponWhenSubtotalIsAtMinimum, Pedido.shouldReturn422WhenSubtotalIsOneCentBelowCouponMinimum, Pedido.shouldReturn422WhenCouponDoesNotExist, Pedido.shouldReturn422WhenCouponCaseDoesNotMatch, Pedido.shouldRoundDiscountHalfUp, Pedido.shouldReturn422WhenCartIsEmpty, Pedido.shouldEmptyCartWhenOrderIsCreated, Pedido.shouldReturn400WithSortedFieldsWhenAddressFieldsAreMissing, Pedido.shouldReturn404WhenCartDoesNotExist |
| `enderecoEntrega.logradouro`, `enderecoEntrega.cep` | Os mesmos, exceto Pedido.shouldReturn400WithSortedFieldsWhenAddressFieldsAreMissing |
| `cupom` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldGiveFreeShippingUsingSubtotalBeforeDiscount, Pedido.shouldAcceptCouponWhenSubtotalIsAtMinimum, Pedido.shouldReturn422WhenSubtotalIsOneCentBelowCouponMinimum, Pedido.shouldReturn422WhenCouponDoesNotExist, Pedido.shouldReturn422WhenCouponCaseDoesNotMatch, Pedido.shouldRoundDiscountHalfUp |
| `enderecoEntrega.complemento` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement |

## Resposta

### ProdutoResponse e DimensoesResponse

| Campo | Verificado em |
|---|---|
| `id`, `nome`, `descricao`, `categoria`, `pesoGramas`, `dimensoes.alturaCm`, `dimensoes.larguraCm`, `dimensoes.comprimentoCm` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn200WithAllFieldsWhenProductExists |
| `sku` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn200WithAllFieldsWhenProductExists, Produto.shouldReturnOnlyCategoryProductsSortedBySkuWhenCategoryIsGiven, Produto.shouldReturnAllProductsIncludingInactiveWhenCategoryIsMissing |
| `preco` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn200WithAllFieldsWhenProductExists, Produto.shouldRoundPriceHalfUpWhenPriceHasThreeDecimalPlaces |
| `ativo` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn201WithNullDescriptionAndEmptyTagsWhenOptionalFieldsAreMissing, Produto.shouldReturn200WithAllFieldsWhenProductExists, Produto.shouldReturnAllProductsIncludingInactiveWhenCategoryIsMissing |
| `estoque` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn201WithNullDescriptionAndEmptyTagsWhenOptionalFieldsAreMissing, Produto.shouldReturn200WithAllFieldsWhenProductExists, Pedido.shouldDecreaseStockWhenOrderIsCreated, Pedido.shouldReturn422AndKeepStockAndCartWhenStockIsNoLongerEnough, Pedido.shouldReturn200AndReturnStockWhenOrderIsAwaitingPayment, Pedido.shouldReturn422WhenOrderIsAlreadyCancelled |
| `tags` | Produto.shouldReturn201WithAllFieldsWhenPayloadIsComplete, Produto.shouldReturn201WithNullDescriptionAndEmptyTagsWhenOptionalFieldsAreMissing, Produto.shouldReturn200WithAllFieldsWhenProductExists |
| `categoria` (na listagem) | também Produto.shouldReturnOnlyCategoryProductsSortedBySkuWhenCategoryIsGiven |

### CarrinhoResponse e ItemCarrinhoResponse

| Campo | Verificado em |
|---|---|
| `id` | Carrinho.shouldReturn201WithEmptyCartWhenCartIsCreated, Carrinho.shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable, Carrinho.shouldReturn200WithItemsInInclusionOrderWhenCartExists |
| `itens` | Carrinho.shouldReturn201WithEmptyCartWhenCartIsCreated, Carrinho.shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable, Carrinho.shouldReturn200WithItemsInInclusionOrderWhenCartExists, Carrinho.shouldAddQuantityToExistingItemWhenProductIsAlreadyInCart, Pedido.shouldReturn422AndKeepStockAndCartWhenStockIsNoLongerEnough, Pedido.shouldEmptyCartWhenOrderIsCreated |
| `subtotal` | Carrinho.shouldReturn201WithEmptyCartWhenCartIsCreated, Carrinho.shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable, Carrinho.shouldAddQuantityToExistingItemWhenProductIsAlreadyInCart, Carrinho.shouldReturn200WithItemsInInclusionOrderWhenCartExists, Pedido.shouldEmptyCartWhenOrderIsCreated |
| `itens[].produtoId` | Carrinho.shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable, Carrinho.shouldAddQuantityToExistingItemWhenProductIsAlreadyInCart, Carrinho.shouldReturn200WithItemsInInclusionOrderWhenCartExists |
| `itens[].quantidade` | Carrinho.shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable, Carrinho.shouldAddQuantityToExistingItemWhenProductIsAlreadyInCart, Carrinho.shouldReturn200WithItemsInInclusionOrderWhenCartExists, Pedido.shouldReturn422AndKeepStockAndCartWhenStockIsNoLongerEnough |
| `itens[].precoUnitario`, `itens[].subtotal` | Carrinho.shouldReturn200WithItemAndSubtotalsWhenProductIsAvailable, Carrinho.shouldReturn200WithItemsInInclusionOrderWhenCartExists |

### CotacaoFreteResponse

| Campo | Verificado em |
|---|---|
| `modalidade` | Frete.shouldReturn200WithAllFieldsWhenQuotationIsValid, Frete.shouldMultiplyValueAndReduceDeadlineWhenModeIsExpress |
| `valor` | Frete.shouldReturn200WithAllFieldsWhenQuotationIsValid, Frete.shouldChargeRegionBaseAndDeadlineWhenModeIsStandard, Frete.shouldCountOneKilogramWhenWeightIsExactlyOneKilogram, Frete.shouldCountStartedKilogramWhenWeightExceedsKilogramByOneGram, Frete.shouldMultiplyValueAndReduceDeadlineWhenModeIsExpress, Frete.shouldReturnOneDayDeadlineWhenModeIsExpressInSoutheast, Frete.shouldGiveFreeShippingWhenSubtotalIsAtThreshold, Frete.shouldChargeShippingWhenSubtotalIsOneCentBelowThreshold, Frete.shouldChargeShippingWhenModeIsExpressEvenAtThreshold, Frete.shouldSumRepeatedItemsAndIgnoreActiveAndStockRules |
| `prazoDias` | Frete.shouldReturn200WithAllFieldsWhenQuotationIsValid, Frete.shouldChargeRegionBaseAndDeadlineWhenModeIsStandard, Frete.shouldMultiplyValueAndReduceDeadlineWhenModeIsExpress, Frete.shouldReturnOneDayDeadlineWhenModeIsExpressInSoutheast, Frete.shouldGiveFreeShippingWhenSubtotalIsAtThreshold |
| `freteGratis` | Frete.shouldReturn200WithAllFieldsWhenQuotationIsValid, Frete.shouldMultiplyValueAndReduceDeadlineWhenModeIsExpress, Frete.shouldGiveFreeShippingWhenSubtotalIsAtThreshold, Frete.shouldChargeShippingWhenSubtotalIsOneCentBelowThreshold, Frete.shouldChargeShippingWhenModeIsExpressEvenAtThreshold |
| `pesoTotalGramas` | Frete.shouldReturn200WithAllFieldsWhenQuotationIsValid, Frete.shouldMultiplyValueAndReduceDeadlineWhenModeIsExpress, Frete.shouldSumRepeatedItemsAndIgnoreActiveAndStockRules |

### PedidoResponse, ClienteResponse, EnderecoResponse e ItemPedidoResponse

| Campo | Verificado em |
|---|---|
| `cliente.nome`, `cliente.email`, `enderecoEntrega.logradouro`, `enderecoEntrega.numero`, `enderecoEntrega.cidade`, `enderecoEntrega.uf`, `enderecoEntrega.cep`, `modalidadeFrete`, `itens[].produtoId`, `itens[].nomeProduto`, `itens[].quantidade`, `itens[].precoUnitario`, `itens[].subtotal` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn200WithAllFieldsWhenOrderExists |
| `id`, `status` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn200WithAllFieldsWhenOrderExists, Pedido.shouldReturn200AndReturnStockWhenOrderIsAwaitingPayment |
| `itens` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn200WithAllFieldsWhenOrderExists |
| `enderecoEntrega.complemento` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn201WithZeroDiscountWhenOptionalFieldsAreMissing, Pedido.shouldReturn200WithAllFieldsWhenOrderExists |
| `subtotal` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn201WithZeroDiscountWhenOptionalFieldsAreMissing, Pedido.shouldReturn200WithAllFieldsWhenOrderExists |
| `frete` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn201WithZeroDiscountWhenOptionalFieldsAreMissing, Pedido.shouldGiveFreeShippingUsingSubtotalBeforeDiscount, Pedido.shouldReturn200WithAllFieldsWhenOrderExists |
| `desconto`, `total` | Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement, Pedido.shouldReturn201WithZeroDiscountWhenOptionalFieldsAreMissing, Pedido.shouldRoundDiscountHalfUp, Pedido.shouldReturn200WithAllFieldsWhenOrderExists |

## Regras de negócio

Testes por regra e onde fica o valor de que cada teste depende. "Fixture" quer dizer que uma mudança no valor da regra desatualiza um dado do arquivo SQL; "asserção" quer dizer que o valor da regra aparece no valor esperado do teste.

| Regra | Testes | Fixture | Valor da regra em |
|---|---|---|---|
| RN01 | Carrinho.shouldReturn422WhenProductIsInactive | `regras/rn01.sql` | Fixture (produto inativo) |
| RN02 | Carrinho.shouldReturn200WhenCartIsOneUnitBelowMaximum, Carrinho.shouldReturn422WhenCartAlreadyHasMaximumUnits | `regras/rn02.sql` | Fixture (quantidade no carrinho) |
| RN03 | Carrinho.shouldReturn200WhenCartIsOneUnitBelowStock, Carrinho.shouldReturn422WhenCartAlreadyHasAllUnitsInStock, Pedido.shouldDecreaseStockWhenOrderIsCreated, Pedido.shouldReturn422AndKeepStockAndCartWhenStockIsNoLongerEnough | `regras/rn03.sql` | Fixture (estoque e quantidade) |
| RN04 | Frete.shouldChargeRegionBaseAndDeadlineWhenModeIsStandard | `frete/frete.sql` | Asserção |
| RN05 | Frete.shouldCountOneKilogramWhenWeightIsExactlyOneKilogram, Frete.shouldCountStartedKilogramWhenWeightExceedsKilogramByOneGram | `regras/rn05.sql` | Fixture (peso no limite do quilo) e asserção (adicional por quilo) |
| RN06 | Frete.shouldChargeRegionBaseAndDeadlineWhenModeIsStandard, Frete.shouldMultiplyValueAndReduceDeadlineWhenModeIsExpress, Frete.shouldReturnOneDayDeadlineWhenModeIsExpressInSoutheast | `frete/frete.sql` | Asserção |
| RN07 | Frete.shouldGiveFreeShippingWhenSubtotalIsAtThreshold, Frete.shouldChargeShippingWhenSubtotalIsOneCentBelowThreshold, Frete.shouldChargeShippingWhenModeIsExpressEvenAtThreshold, Pedido.shouldGiveFreeShippingUsingSubtotalBeforeDiscount | `regras/rn07.sql` | Fixture (preço no limite) |
| RN08 | Pedido.shouldAcceptCouponWhenSubtotalIsAtMinimum, Pedido.shouldReturn422WhenSubtotalIsOneCentBelowCouponMinimum, Pedido.shouldReturn422WhenCouponDoesNotExist, Pedido.shouldReturn422WhenCouponCaseDoesNotMatch | `regras/rn08.sql` | Fixture (subtotal mínimo). O percentual aparece na asserção de Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement e Pedido.shouldRoundDiscountHalfUp |
| RN09 | Produto.shouldRoundPriceHalfUpWhenPriceHasThreeDecimalPlaces, Pedido.shouldRoundDiscountHalfUp, Pedido.shouldReturn201WithAllFieldsWhenOrderHasCouponAndComplement | `pedido/pedidos.sql` | Asserção |
| RN10 | Pedido.shouldReturn422WhenCartIsEmpty | `pedido/pedidos.sql` | Não tem valor numérico |
| RN11 | Pedido.shouldReturn200AndReturnStockWhenOrderIsAwaitingPayment, Pedido.shouldReturn422WhenOrderIsAlreadyCancelled | `regras/rn11.sql` | Fixture (status do pedido) |

As regras de limite (RN02, RN03, RN07 e os limites da RN05 e da RN08) têm um teste em cada lado do limite: uma mudança no valor, para mais ou para menos, quebra pelo menos um deles. Esses testes verificam só o resultado da regra (aceito ou recusado, frete grátis ou não), e nunca um valor calculado a partir do dado da fixture, para que o reparo de uma mudança de limite fique na fixture.
