# Rest Test Healing (API cobaia)

API REST de e-commerce (produto, carrinho, frete e pedido) em Java com Spring Boot, construída como **API cobaia** do projeto "Manutenção de Testes de APIs REST usando Agentes de IA", da disciplina IA Generativa para Engenharia de Software (2026.2).

A API é o **objeto do experimento**: sobre ela são injetadas mudanças controladas, e os agentes de IA tentam reparar os testes que quebram. Ela não é um sistema de produção.

Este documento descreve como rodar a API, a suíte de testes e as execuções contra testes instáveis, como o contrato OpenAPI foi gerado e o que a API faz: entidades, endpoints e erros. As regras de negócio estão em `docs/regras-de-negocio.md`.

## Guia de reprodução

Os passos partem de um clone limpo e são dados de dois jeitos: pelo IntelliJ IDEA, usado no desenvolvimento, e pelo terminal, com o Maven Wrapper (`./mvnw`), que dispensa IDE e instalação do Maven.

### Pré-requisitos

- **JDK 21.** No Ubuntu 24.04, o Java 21 está disponível como pacote do sistema ([pacote openjdk-21-jdk](https://packages.ubuntu.com/noble/openjdk-21-jdk)):

  ```bash
  sudo apt update
  sudo apt install openjdk-21-jdk
  java -version
  ```

- **Git.**
- **Maven:** não precisa ser instalado. Na primeira execução, o `./mvnw` (ou `mvnw.cmd`, no Windows) baixa o Maven 3.10.0 e as dependências, o que exige acesso à internet.
- **IntelliJ IDEA** (opcional): o projeto foi desenvolvido na versão paga, que traz o HTTP Client usado pelos arquivos de `http/`.

### Versões e ambiente

| Item | Versão | Observação |
|---|---|---|
| Java | 21 | Versão LTS ([Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)). O Spring Boot 4.1.1 aceita do Java 17 ao 26 ([System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)) |
| Spring Boot | 4.1.1 | Lançado em 20/08/2026 ([Spring Blog](https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now/)); usa Jackson 3 |
| springdoc-openapi | 3.1.1 | A linha 3.x acompanha o Spring Boot 4; a 3.1.0 passou a usar o Spring Boot 4.1.0 ([releases](https://github.com/springdoc/springdoc-openapi/releases)) |
| Maven | 3.10.0, via `./mvnw` | Não precisa ser instalado; o Spring Boot exige 3.6.3 ou superior |
| JUnit Jupiter | 6.0.3 | Versão gerenciada pelo Spring Boot 4.1.1 ([`gradle.properties` do Spring Boot v4.1.1](https://github.com/spring-projects/spring-boot/blob/v4.1.1/gradle.properties)), sem versão fixada no `pom.xml` |
| Banco | H2 em memória | Sem servidor de banco |
| Autenticação | nenhuma | Todos os endpoints são públicos |

### Obter o código

```bash
git clone https://github.com/barbaraguarino/rest-test-healing-sut.git
cd rest-test-healing-sut
git checkout linha-de-base-2
```

A tag `linha-de-base-2` marca a versão da API e da suíte usada no experimento pelos dois agentes. Ela tem o mesmo código e a mesma suíte da `linha-de-base-1`; muda a documentação, com as regras de negócio num arquivo próprio.

### Executar a API

No IntelliJ IDEA:

1. **File → Open** e escolha a pasta do projeto. O IntelliJ reconhece o `pom.xml` e importa o projeto como Maven.
2. **File → Project Structure → Project → SDK**: escolha o JDK 21.
3. Na janela **Maven**, clique em **Reload All Maven Projects**. Se alguma classe não reconhecer os pacotes `jakarta.*`, é este passo que resolve.
4. Abra `RestTestHealingSutApplication` e clique no ▶ ao lado da classe.

No terminal:

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`, com o banco H2 em memória vazio. Os dados somem quando a aplicação para.

### Executar a suíte de testes

- No IntelliJ IDEA: botão direito em `src/test/java` → **Run 'All Tests'**.
- No terminal: `./mvnw test`.

Resultado esperado: **65 execuções, todas passando**. São 61 métodos de teste, e o teste parametrizado do frete por região (RN04) roda uma vez para cada uma das 5 regiões. A aplicação não precisa estar rodando: os testes sobem a própria aplicação, sem abrir porta, e limpam e carregam o banco antes de cada teste.

A suíte tem uma classe de teste por controller, no mesmo pacote da classe testada. O arquivo `docs/matriz-de-cobertura.md` liga cada campo do contrato aos testes que o enviam ou verificam e cada regra de negócio aos seus testes.

### Execuções contra testes instáveis

Teste instável é o que passa ou falha sem nenhuma mudança no código. Antes do experimento, a suíte foi executada várias vezes, em ordem fixa e em ordens diferentes, para verificar que nenhum teste oscila.

O arquivo `src/test/resources/junit-platform.properties` faz o JUnit executar classes e métodos em ordem aleatória, com a semente 1. A ordem é embaralhada, o que revela testes que dependem de outros terem rodado antes, mas é a mesma a cada execução, o que mantém as execuções reproduzíveis. A semente pode ser trocada pela propriedade `junit.jupiter.execution.order.random.seed`, que tem precedência sobre o arquivo.

O protocolo tem duas rodadas:

| Rodada | O que detecta | No IntelliJ IDEA | No terminal |
|---|---|---|---|
| A. Ordem fixa (semente 1), 20 repetições | Instabilidade que não depende da ordem | Na configuração de execução dos testes, **Modify options → Repeat → N times**: 20 | `for i in $(seq 1 20); do ./mvnw -q test \|\| break; done` |
| B. 10 ordens diferentes (sementes 2 a 11), uma execução cada | Dependência de ordem entre testes | Em **VM options**: `-ea -Djunit.jupiter.execution.order.random.seed=2`, trocando o número até 11 | `for s in $(seq 2 11); do ./mvnw -q test -Djunit.jupiter.execution.order.random.seed=$s \|\| break; done` |

Resultado em 11/10/2026 (Ubuntu 24.04, OpenJDK 21, IntelliJ IDEA, JUnit Jupiter 6.0.3):

| Rodada | Execuções | Falhas |
|---|---|---|
| A. Ordem fixa, 20 repetições | 1.300 | 0 |
| B. Sementes 2 a 11 | 650 | 0 |

### Verificação manual

Com a aplicação rodando, abra `http/verificacao-manual.http` no IntelliJ IDEA e execute as requisições de cima para baixo. Cada uma tem asserções, e o resultado aparece na aba **Tests** da janela **Services**. O arquivo segue o formato do HTTP Client da JetBrains e não faz parte da suíte automatizada.

### Contrato OpenAPI

O arquivo `docs/openapi.json` é o contrato declarado da API: o que ela promete aos clientes, independente do que o código faz.

**Como foi gerado.** Em 11/10/2026, com a aplicação rodando nesta versão do código, o contrato foi obtido de `GET http://localhost:8080/v3/api-docs` (springdoc-openapi 3.1.1) e salvo no repositório. As propriedades `springdoc.writer-with-order-by-keys` e `springdoc.writer-with-default-pretty-printer` deixam as chaves em ordem alfabética e o JSON indentado, e o servidor fica fixo em `/` (`OpenApiConfig`). Assim, duas gerações a partir do mesmo código produzem o mesmo arquivo. Depois de gerado, o arquivo foi conferido contra a especificação deste documento: campos e tipos de cada schema, campos obrigatórios, enums, restrições de formato, códigos de sucesso, parâmetros e ausência de schemas de erro e de limites de negócio.

**Como regenerar.** O contrato não é regenerado no build. Com a aplicação rodando:

- No IntelliJ IDEA: abra `http/gerar-contrato.http` e execute a requisição. A resposta sobrescreve `docs/openapi.json`.
- No terminal: `curl -s http://localhost:8080/v3/api-docs -o docs/openapi.json`.

### Estrutura do projeto

```
rest-test-healing-sut/
├── docs/
│   ├── openapi.json                 contrato declarado (retrato de /v3/api-docs)
│   ├── regras-de-negocio.md         regras de negócio RN01 a RN11
│   └── matriz-de-cobertura.md       campos do contrato → testes; regras de negócio → testes
├── http/
│   ├── verificacao-manual.http      verificação manual pelo HTTP Client do IntelliJ IDEA
│   └── gerar-contrato.http          regenera docs/openapi.json
├── src/main/java/br/edu/resttesthealingsut/
│   ├── produto/  carrinho/  frete/  pedido/
│   │                                um pacote por recurso: controller, service, entidades com
│   │                                as regras de negócio, repositório e DTOs (dto/)
│   └── shared/                      configuração do OpenAPI, valores monetários, corpo de erro,
│                                    tratador global de erros e mensagens de validação
├── src/main/resources/
│   ├── application.properties       banco, desserialização estrita e saída estável do contrato
│   └── schema.sql                   schema do banco (IDs gerados a partir de 1000)
├── src/test/java/br/edu/resttesthealingsut/
│   ├── produto/  carrinho/  frete/  pedido/
│   │                                testes de cada controller
│   ├── shared/erro/                 erros gerais: rota inexistente, JSON malformado, método e
│   │                                tipo de mídia não aceitos
│   └── suporte/                     verificação de valores numéricos e leitura do ID da resposta
├── src/test/resources/
│   ├── junit-platform.properties    ordem aleatória dos testes, com semente fixa
│   └── fixtures/                    limpeza.sql, cenários por recurso e um arquivo por regra
│                                    de negócio (regras/)
├── pom.xml
└── mvnw, mvnw.cmd, .mvn/            Maven Wrapper
```

## Especificação da cobaia

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

As regras de negócio (RN01 a RN11) e o detalhamento de cada uma estão em `docs/regras-de-negocio.md`. Cada regra fica num único lugar do código de domínio, como constante, e não como anotação de validação, por isso não aparece no contrato.

### Características da suíte de testes

- **Testes caixa-preta com MockMvc** (`@SpringBootTest` + `@AutoConfigureMockMvc`): a requisição passa por todo o Spring MVC, sem abrir porta. O JSON da requisição fica escrito como texto no próprio teste, e a resposta é lida pelo nome dos campos (`jsonPath`). Os testes não usam os DTOs nem as entidades da aplicação.
- **Formato:** uma classe por controller, no mesmo pacote da classe testada, `@Nested` por operação, métodos `shouldReturn…When…` e `@DisplayName` "Deve …".
- **Asserções campo a campo,** nunca comparação do JSON inteiro. Campos numéricos com matcher (`value(is(6))`, `value(decimal("51.00"))`); texto e booleano com `value(…)`.
- **Regras de negócio com limite** testadas nos dois lados do limite, com asserção só do resultado da regra (aceito ou recusado, frete grátis ou não).
- **Cobertura do contrato.** Todo campo da resposta é verificado em pelo menos um teste, e todo campo da requisição é enviado em pelo menos um teste. A correspondência está em `docs/matriz-de-cobertura.md`.
- **Erros verificados por `codigo` e `campos`,** e não pelas mensagens.
- **Isolamento.** Antes de cada teste, um script SQL limpa o banco e outro carrega a fixture do teste (`@Sql`). Os testes não dependem da ordem de execução.
- **Fixtures em arquivos SQL** em `src/test/resources/fixtures/`, separadas do código Java dos testes, organizadas por recurso e, para as regras de negócio, com um arquivo por regra em `regras/`.

### Determinismo

- Nenhum campo de data ou hora. O prazo de entrega é dado em dias.
- Nada aleatório: sem UUID gerado e com cupons fixos.
- Listas com ordem explícita: produtos por `sku`, itens de carrinho e de pedido pela ordem de inclusão.
- Os testes não assumem IDs gerados pelo banco: leem o ID da resposta. As fixtures usam IDs fixos de 1 a 999, e o banco gera IDs a partir de 1000 (`START WITH 1000` no `schema.sql`).
- Os `campos` das respostas de erro saem ordenados pelo caminho do campo, porque o Bean Validation não garante a ordem das violações.
- Mensagens de erro com texto explícito, sem depender do idioma da máquina.
- Valores monetários sempre em `BigDecimal`, nunca em `double`.
- Testes executados em ordem aleatória com semente fixa, e a suíte executada várias vezes antes do experimento, em ordem fixa e em ordens diferentes.
