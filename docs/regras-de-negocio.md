# Regras de negócio

Os valores abaixo são fictícios e não representam um negócio real. Cada regra fica num único lugar do código de domínio, como constante, e não como anotação de validação, por isso não aparece no contrato.

| ID | Regra | Violação |
|---|---|---|
| RN01 | Produto inativo não pode ser incluído no carrinho | 422 |
| RN02 | Cada produto pode ter no máximo 10 unidades no carrinho | 422 |
| RN03 | A quantidade de um produto no carrinho não pode passar do estoque. A criação do pedido confere o estoque de novo e o reduz | 422 |
| RN04 | Frete base pela região da UF de destino: Sudeste 15,00; Sul 18,00; Centro-Oeste 22,00; Nordeste 25,00; Norte 30,00 | — |
| RN05 | Adicional de 2,00 por quilo iniciado do peso total (peso total = soma de `pesoGramas` × `quantidade`) | — |
| RN06 | Prazo padrão por região: Sudeste 3, Sul 4, Centro-Oeste 5, Nordeste 6, Norte 8 dias. Na modalidade `EXPRESSA`, o valor do frete é multiplicado por 1,5 e o prazo diminui 2 dias, com mínimo de 1 dia | — |
| RN07 | Frete grátis quando o subtotal é maior ou igual a 200,00 e a modalidade é `PADRAO` | — |
| RN08 | O cupom `BEMVINDO10` dá 10% de desconto sobre o subtotal, desde que o subtotal seja maior ou igual a 100,00. Cupom inexistente ou fora da condição é recusado | 422 |
| RN09 | Total = subtotal − desconto + frete. O subtotal de um item é preço unitário × quantidade, com o preço do produto no momento da criação do pedido. Valores monetários em `BigDecimal`, com 2 casas e arredondamento `HALF_UP` em cada cálculo | — |
| RN10 | Não é possível criar pedido a partir de carrinho vazio | 422 |
| RN11 | Só é possível cancelar pedido com status `AGUARDANDO_PAGAMENTO`. O cancelamento devolve as quantidades ao estoque | 422 |

Detalhamento das regras:

- Incluir no carrinho um produto que já está nele soma a quantidade no item existente, que mantém sua posição. RN02 e RN03 valem sobre a soma.
- O carrinho mostra o preço atual de cada produto, lido a cada consulta; o preço só é congelado no pedido (RN09).
- Ao criar o pedido, o carrinho é esvaziado. Ele pode receber novos itens, e um novo pedido do mesmo carrinho sem itens cai na RN10.
- Cadastrar um SKU já existente é recusado com 422.
- A RN01 vale só para a inclusão no carrinho; a criação do pedido não confere de novo se o produto está ativo.
- A cotação de frete é só uma simulação: produto inexistente gera 404, mas RN01 e RN03 não se aplicam. Itens com o mesmo `produtoId` são somados.
- No pedido, o frete usa a UF do endereço de entrega, e a RN07 usa o subtotal antes do desconto.
- RN05: "quilo iniciado" é o peso total em quilos arredondado para cima (1.001 g contam como 2 kg). RN06: o fator 1,5 incide sobre a soma da base com o adicional. RN07: com frete grátis, `valor` é 0,00 e o prazo não muda.
- RN08: o código do cupom precisa ser exato, diferenciando maiúsculas de minúsculas.
- Preço com mais de 2 casas decimais no cadastro é arredondado com `HALF_UP` (RN09), em vez de recusado.
