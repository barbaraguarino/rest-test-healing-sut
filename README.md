# rest-test-healing-sut

API REST de e-commerce (produto, carrinho, frete e pedido) em Java com Spring Boot, construída como **API cobaia** do projeto "Manutenção de Testes de APIs REST usando Agentes de IA", da disciplina IA Generativa para Engenharia de Software (2026.2).

A API é o **objeto do experimento**: sobre ela são injetadas mudanças controladas, e os agentes de IA tentam reparar os testes que quebram. Ela não é um sistema de produção.

As instruções de execução, a suíte de testes e o contrato OpenAPI serão incluídos nas próximas etapas. Este documento traz, por enquanto, a especificação a partir da qual a API é implementada.

## Especificação da cobaia

### Papel no experimento

A API atende aos dois agentes do projeto:

- **Agente 1** repara o código de teste quando o contrato OpenAPI muda de forma explícita, em quatro categorias: renomeação de campo, remoção de campo, alteração de tipo de campo e campo obrigatório novo. A API precisa permitir as quatro, e cada mudança precisa quebrar pelo menos um teste.
- **Agente 2** repara fixtures de teste que ficaram desatualizadas em relação a uma mudança de regra de negócio, sem mudança de schema. A API precisa ter regras de negócio com valores que podem mudar sem alterar o contrato.

Daí vêm três exigências, detalhadas nas seções seguintes:

1. **Contrato declarado separado do código.** O arquivo `docs/openapi.json` registra o que a API promete; o código registra o que ela faz. Numa mudança legítima, os dois mudam juntos. Numa regressão, o código muda e o contrato não. Por isso o contrato não é regenerado automaticamente a cada build: ele é gerado uma vez e depois só muda por decisão explícita.
2. **Regras de negócio fora do contrato.** As regras ficam como constantes no código de domínio, nunca como anotações de validação (como `@Max(10)`). O springdoc publica essas anotações no contrato, e mudar a regra viraria uma mudança de schema.
3. **Determinismo.** Um teste que falha sem relação com a mudança injetada contamina o experimento.

### Versões e ambiente

| Item | Versão | Observação |
|---|---|---|
| Java | 25 | Compatível com o Spring Boot 4.1.1, que aceita do Java 17 ao 26 ([System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)) |
| Spring Boot | 4.1.1 | Lançado em 20/08/2026 ([Spring Blog](https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now/)); usa Jackson 3 |
| springdoc-openapi | 3.1.1 | A linha 3.x acompanha o Spring Boot 4; a 3.1.0 passou a usar o Spring Boot 4.1.0 ([releases](https://github.com/springdoc/springdoc-openapi/releases)) |
| Maven | 3.10.0, via `./mvnw` | Não precisa ser instalado; o Spring Boot exige 3.6.3 ou superior |
| Banco | H2 em memória | Sem servidor de banco |

No Ubuntu 24.04, o Java 25 está disponível no repositório `universe` ([pacote openjdk-25-jdk](https://packages.ubuntu.com/search?keywords=openjdk-25-jdk&searchon=names&suite=noble&section=all)):

```bash
sudo apt update
sudo apt install openjdk-25-jdk
java -version
```

### Decisões técnicas

| Decisão | Por quê | Alternativas descartadas |
|---|---|---|
| Sem Spring Security | Com ela, todo endpoint exige autenticação por padrão, e falhas 401 seriam ruído. Mudanças de autenticação não estão entre as categorias estudadas | Manter com todos os endpoints liberados |
| Só H2 em memória | A replicação não depende de um servidor de banco | PostgreSQL |
| Schema explícito em `src/main/resources/schema.sql`, validado pelo Hibernate (`spring.jpa.hibernate.ddl-auto=validate`) | O schema não evolui no experimento: as mudanças de contrato ficam nos DTOs, e as de regra de negócio não mudam o schema. Um arquivo SQL explícito basta, e as fixtures em SQL têm um schema legível como referência | Flyway (versionamento de schema sem uso aqui); schema gerado pelo Hibernate (implícito) |
| Sem Lombok; DTOs como records e entidades JPA com métodos escritos | Os agentes leem o código-fonte, e métodos gerados por anotação não aparecem nele | Lombok |
| DTOs separados para requisição e resposta | Permite mudar o contrato só da requisição ou só da resposta | Um DTO por recurso |
| Contrato gerado pelo springdoc a partir do código, uma vez, e salvo em `docs/openapi.json` como contrato declarado | Gerar evita divergências entre o código e um contrato escrito à mão. Salvar o retrato mantém o contrato independente do código, o que é necessário para injetar regressões | Contrato escrito à mão; contrato regenerado a cada build |
| Desserialização estrita: campo desconhecido no JSON da requisição gera 400 (`spring.jackson.deserialization.fail-on-unknown-properties=true`) | Sem isso, um teste que ainda envia um campo renomeado ou removido continua passando, porque a API ignora o campo | Comportamento tolerante (campo desconhecido ignorado) |
| Contrato só com as respostas de sucesso; o tratador global de erros não usa `@ResponseStatus` | Com `@ResponseStatus`, o springdoc anexaria todas as respostas de erro a todas as operações, inclusive um 404 em `POST /produtos`. As categorias de mudança do Agente 1 atuam só nos corpos de sucesso | Declarar os erros de cada operação com `@ApiResponse` (completo, mas enche os controllers de anotações) |
| Mensagens das anotações de validação escritas no atributo `message` (por exemplo, `@NotNull(message = "é obrigatório")`), a partir de constantes num único arquivo | As mensagens padrão do Bean Validation mudam com o idioma da máquina. Escritas no código, ficam visíveis para os agentes | Arquivo `ValidationMessages.properties` |
| Testes de API com MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`) | A requisição passa por todo o Spring MVC (conversão do JSON, validação e tratamento de erros), então as quebras de contrato aparecem como num servidor real. Não abre porta e é rápido. É uma API de teste consolidada e muito documentada, o que importa porque os agentes vão reparar esse código | RestTestClient (recente, do Spring Framework 7); REST Assured com servidor real (dependência a mais e limpeza manual do banco entre testes) |

### Entidades e campos

Tipos em termos de JSON. Campos marcados com * são obrigatórios na requisição.

**Produto**

| Campo | Tipo | Requisição | Resposta |
|---|---|---|---|
| `id` | número inteiro | — | sim |
| `sku` | texto, único | * | sim |
| `nome` | texto | * | sim |
| `descricao` | texto | opcional | sim (pode ser `null`) |
| `preco` | número decimal, maior que zero | * | sim |
| `categoria` | enum: `ELETRONICOS`, `LIVROS`, `VESTUARIO`, `CASA` | * | sim |
| `ativo` | booleano | * | sim |
| `estoque` | número inteiro, maior ou igual a zero | * | sim |
| `pesoGramas` | número inteiro, maior que zero | * | sim |
| `dimensoes` | objeto: `alturaCm`, `larguraCm`, `comprimentoCm` (números inteiros maiores que zero) | * | sim |
| `tags` | lista de texto | opcional | sim, na ordem enviada (lista vazia se ausente) |

**Carrinho**

| Campo | Tipo | Onde |
|---|---|---|
| `id` | número inteiro | resposta |
| `itens` | lista de objetos: `produtoId`, `quantidade`, `precoUnitario`, `subtotal` | resposta |
| `subtotal` | número decimal | resposta |

Requisição de inclusão de item: `produtoId`* (número inteiro) e `quantidade`* (número inteiro, mínimo 1).

**Cotação de frete**

| Campo | Tipo | Onde |
|---|---|---|
| `destino` | objeto: `cep`* (texto com 8 dígitos), `uf`* (enum com as 27 UFs) | requisição |
| `modalidade` | enum: `PADRAO`, `EXPRESSA` | requisição* e resposta |
| `itens` | lista com pelo menos um objeto: `produtoId`*, `quantidade`* (número inteiro, mínimo 1) | requisição* |
| `valor` | número decimal | resposta |
| `prazoDias` | número inteiro | resposta |
| `freteGratis` | booleano | resposta |
| `pesoTotalGramas` | número inteiro | resposta |

**Pedido**

| Campo | Tipo | Onde |
|---|---|---|
| `carrinhoId` | número inteiro | requisição* |
| `cliente` | objeto: `nome`*, `email`* (texto) | requisição* e resposta |
| `enderecoEntrega` | objeto: `logradouro`*, `numero`*, `complemento` (opcional), `cidade`*, `uf`* (enum com as 27 UFs), `cep`* (texto com 8 dígitos) | requisição* e resposta |
| `modalidadeFrete` | enum: `PADRAO`, `EXPRESSA` | requisição* e resposta |
| `cupom` | texto | requisição (opcional) |
| `id` | número inteiro | resposta |
| `status` | enum: `AGUARDANDO_PAGAMENTO`, `CANCELADO` | resposta |
| `itens` | lista de objetos: `produtoId`, `nomeProduto`, `quantidade`, `precoUnitario`, `subtotal` | resposta |
| `subtotal`, `desconto`, `frete`, `total` | número decimal | resposta |

As restrições de formato (obrigatório, maior que zero, mínimo 1, 8 dígitos) são validação de entrada e aparecem no contrato. Os limites de negócio (estoque, máximo por produto, valores de frete e cupom) ficam fora do contrato, nas regras de negócio.

### Endpoints

| Recurso | Método e caminho | Sucesso |
|---|---|---|
| Produto | `POST /produtos` | 201, com cabeçalho `Location` |
| | `GET /produtos/{id}` | 200 |
| | `GET /produtos?categoria={categoria}` | 200, lista ordenada por `sku`. `categoria` é opcional; sem ela, a lista traz todos os produtos, ativos e inativos |
| Carrinho | `POST /carrinhos` (sem corpo) | 201, carrinho vazio |
| | `POST /carrinhos/{id}/itens` | 200, carrinho atualizado |
| | `GET /carrinhos/{id}` | 200 |
| Frete | `POST /fretes/cotacao` | 200 |
| Pedido | `POST /pedidos` | 201, com cabeçalho `Location` |
| | `GET /pedidos/{id}` | 200 |
| | `POST /pedidos/{id}/cancelamento` | 200, pedido com status `CANCELADO` |

**Erros.** Todas as respostas de erro usam o mesmo corpo, sem data ou hora:

| Campo | Tipo |
|---|---|
| `codigo` | texto: `VALIDACAO`, `JSON_INVALIDO`, `RECURSO_NAO_ENCONTRADO`, `REGRA_DE_NEGOCIO`, `REQUISICAO_NAO_SUPORTADA`, `ERRO_INTERNO` |
| `mensagem` | texto |
| `campos` | lista de objetos `campo`, `mensagem` (vazia quando não se aplica) |

| Situação | Status | `codigo` |
|---|---|---|
| Campo obrigatório ausente ou com formato inválido | 400 | `VALIDACAO` |
| Parâmetro de path ou de query com tipo ou valor inválido (como `/produtos/abc` ou `?categoria=FOO`) | 400 | `VALIDACAO` |
| JSON malformado, campo desconhecido, tipo incompatível (inclusive valor de enum inexistente no corpo) ou corpo ausente | 400 | `JSON_INVALIDO` |
| Recurso ou rota inexistente | 404 | `RECURSO_NAO_ENCONTRADO` |
| Violação de regra de negócio | 422 | `REGRA_DE_NEGOCIO` |
| Método HTTP ou tipo de mídia não aceito pelo endpoint | 405, 406 ou 415 | `REQUISICAO_NAO_SUPORTADA` |
| Erro inesperado (sem detalhes internos no corpo) | 500 | `ERRO_INTERNO` |

Em `VALIDACAO`, `campos` traz um item por violação, com o caminho do campo (`dimensoes.alturaCm`, `itens[0].quantidade`) ou o nome do parâmetro, ordenados pelo caminho. Em `JSON_INVALIDO`, `campos` traz o campo envolvido quando o Jackson o identifica.

### Regras de negócio

Os valores abaixo foram definidos para a cobaia e não representam um negócio real. Cada regra fica num único lugar do código de domínio, para que mudar uma regra seja uma alteração pontual.

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

- Incluir no carrinho um produto que já está nele soma a quantidade no item existente, que mantém sua posição. RN02 e RN03 valem sobre a soma. [A confirmar com o João]
- O carrinho mostra o preço atual de cada produto, lido a cada consulta; o preço só é congelado no pedido (RN09). [A confirmar com o João]
- Ao criar o pedido, o carrinho é esvaziado. Ele pode receber novos itens, e um novo pedido do mesmo carrinho sem itens cai na RN10. [A confirmar com o João]
- Cadastrar um SKU já existente é recusado com 422. [A confirmar com o João]
- A RN01 vale só para a inclusão no carrinho; a criação do pedido não confere de novo se o produto está ativo.
- A cotação de frete é só uma simulação: produto inexistente gera 404, mas RN01 e RN03 não se aplicam. Itens com o mesmo `produtoId` são somados.
- No pedido, o frete usa a UF do endereço de entrega, e a RN07 usa o subtotal antes do desconto.
- RN05: "quilo iniciado" é o peso total em quilos arredondado para cima (1.001 g contam como 2 kg). RN06: o fator 1,5 incide sobre a soma da base com o adicional. RN07: com frete grátis, `valor` é 0,00 e o prazo não muda.
- RN08: o código do cupom precisa ser exato, diferenciando maiúsculas de minúsculas.
- Preço com mais de 2 casas decimais no cadastro é arredondado com `HALF_UP` (RN09), em vez de recusado.

### Categorias de mudança e onde elas quebram um teste

Uma mudança que não quebra nenhum teste fica fora do experimento, porque não há o que reparar. A tabela indica onde aplicar cada categoria para que a quebra seja garantida.

| Categoria | Onde aplicar | Como o teste falha | Onde não quebra (evitar) |
|---|---|---|---|
| Renomeação de campo | Campo da resposta verificado por algum teste | O campo com o nome antigo não existe mais e a asserção falha | Parâmetros de path e de query. Robles et al. (2023) classificam a renomeação de parâmetro como não *breaking*: renomear um parâmetro de path muda só o nome na especificação, e um parâmetro de query desconhecido é ignorado. Por isso a renomeação é aplicada **só a campos do corpo** |
| | Campo da requisição | O teste envia o nome antigo, que é desconhecido: 400 | — |
| Remoção de campo | Campo da resposta verificado por algum teste | A asserção sobre o campo falha | Campo que nenhum teste verifica |
| | Campo da requisição | O teste envia um campo desconhecido: 400 | — |
| Alteração de tipo de campo | Campo da resposta: número para texto, booleano para enum, objeto para texto | A asserção de valor ou de tipo falha | — |
| | Campo da requisição, entre tipos JSON que não se convertem: número e objeto, booleano e enum, texto e lista | 400 | Número para texto na requisição: o Jackson pode converter `10` em `"10"`, e o teste continua passando |
| Campo obrigatório novo | Requisição, com `@NotNull` ou `@NotBlank`, que o springdoc publica como `required` | O teste não envia o campo: 400 | Resposta: um campo novo na resposta não quebra um teste que verifica campo a campo |

### Requisitos para a suíte de testes

- **Testes caixa-preta.** O JSON da requisição fica escrito como texto no próprio teste, e a resposta é lida pelo nome dos campos (`jsonPath`). Os testes não usam os DTOs nem as entidades da aplicação: se usassem, renomear um campo no DTO mudaria o teste junto e a quebra não apareceria.
- **Asserções campo a campo,** nunca comparação do JSON inteiro. A comparação inteira quebraria com qualquer campo novo na resposta, o que não é uma das categorias.
- **Cobertura do contrato.** Todo campo da resposta é verificado em pelo menos um teste, e todo campo da requisição é enviado em pelo menos um teste.
- **Erros verificados por `codigo` e `campos`,** não pelas mensagens padrão do Bean Validation, que mudam com o idioma da máquina.
- **Isolamento.** Antes de cada teste, um script SQL limpa o banco e outro carrega a fixture do teste (`@Sql`). Os testes não dependem da ordem de execução.
- **Fixtures em arquivos SQL** em `src/test/resources/fixtures/`, separadas do código Java dos testes. Assim, o setup não depende do contrato da API, e uma mudança de contrato em `POST /produtos` não quebra todos os testes que precisam de um produto.

### Determinismo

- Nenhum campo de data ou hora. O prazo de entrega é dado em dias. Se uma data se tornar necessária, ela vem de um `java.time.Clock` injetado, fixo nos testes.
- Nada aleatório: sem UUID gerado e com cupons fixos.
- Listas com ordem explícita: produtos por `sku`, itens de carrinho e de pedido pela ordem de inclusão.
- Os testes não assumem IDs gerados pelo banco: leem o ID da resposta. As fixtures usam IDs fixos de 1 a 999, e o banco gera IDs a partir de 1000 (`START WITH 1000` no `schema.sql`). [A confirmar com o João]
- Os `campos` das respostas de erro saem ordenados pelo caminho do campo, porque o Bean Validation não garante a ordem das violações.
- Mensagens de erro com texto explícito, sem depender do idioma da máquina.
- Valores monetários sempre em `BigDecimal`, nunca em `double`.

### Pendências

- Formato das fixtures (arquivos SQL) e valores das regras de negócio. [A confirmar com o João]
- Nome da tag de versão da API usada no experimento. [A decidir]

### Referências

ROBLES, N.; POTES, N.; GARCÉS, K.; CANOVAS IZQUIERDO, J. L.; CABOT, J. Exploratory Analysis of the Structural Evolution of public REST APIs. In: CONGRESSO IBERO-AMERICANO EM ENGENHARIA DE SOFTWARE (CIbSE), 2023. **Anais** [...]. p. 92-106. DOI: https://doi.org/10.5753/cibse.2023.24695.
