-- RN02 (máximo de unidades por produto no carrinho). As quantidades dependem do valor da regra:
-- carrinho 101 com uma unidade abaixo do máximo, carrinho 102 no máximo.
-- O estoque fica bem acima do máximo, para que a RN03 não interfira.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-020', 'Livro de Testes', NULL, 10.00, 'LIVROS', TRUE, 50, 500, 23, 16, 3);

INSERT INTO carrinho (id) VALUES (101), (102);

INSERT INTO item_carrinho (id, carrinho_id, produto_id, quantidade)
VALUES (201, 101, 1, 9),
       (202, 102, 1, 10);
