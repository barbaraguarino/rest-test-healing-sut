-- RN08 (cupom). Os preços dependem do subtotal mínimo do cupom:
-- carrinho 101 com subtotal exatamente no mínimo, carrinho 102 um centavo abaixo.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-080', 'Livro no Minimo', NULL, 100.00, 'LIVROS', TRUE, 10, 500, 23, 16, 3),
       (2, 'LIV-081', 'Livro Abaixo do Minimo', NULL, 99.99, 'LIVROS', TRUE, 10, 500, 23, 16, 3);

INSERT INTO carrinho (id) VALUES (101), (102);

INSERT INTO item_carrinho (id, carrinho_id, produto_id, quantidade)
VALUES (201, 101, 1, 1),
       (202, 102, 2, 1);
