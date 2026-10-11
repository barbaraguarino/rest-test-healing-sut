-- RN07 (frete grátis). Os preços dependem do valor da regra:
-- produto 1 com subtotal exatamente no limite, produto 2 um centavo abaixo.
-- Carrinho 101: 2 unidades do produto 3, subtotal no limite, para o pedido com cupom.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-070', 'Livro no Limite', NULL, 200.00, 'LIVROS', TRUE, 10, 500, 23, 16, 3),
       (2, 'LIV-071', 'Livro Abaixo do Limite', NULL, 199.99, 'LIVROS', TRUE, 10, 500, 23, 16, 3),
       (3, 'LIV-072', 'Livro pela Metade do Limite', NULL, 100.00, 'LIVROS', TRUE, 10, 500, 23, 16, 3);

INSERT INTO carrinho (id) VALUES (101);

INSERT INTO item_carrinho (id, carrinho_id, produto_id, quantidade)
VALUES (201, 101, 3, 2);
