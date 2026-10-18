-- RN03 (estoque).
-- Produto 1 (estoque 5): carrinho 101 com uma unidade abaixo do estoque, carrinho 102 no estoque.
-- Produto 2 (estoque 5): carrinho 103 com 2 unidades, para a baixa de estoque na criação do pedido.
-- Produto 3 (estoque 2): carrinho 104 com 3 unidades, situação de um estoque que caiu depois da inclusão
-- no carrinho; a criação do pedido confere o estoque de novo.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-030', 'Livro de Testes', NULL, 10.00, 'LIVROS', TRUE, 5, 500, 23, 16, 3),
       (2, 'CAS-030', 'Caneca', NULL, 20.00, 'CASA', TRUE, 5, 500, 10, 8, 8),
       (3, 'VES-030', 'Meia', NULL, 15.00, 'VESTUARIO', TRUE, 2, 300, 2, 10, 20);

INSERT INTO carrinho (id) VALUES (101), (102), (103), (104);

INSERT INTO item_carrinho (id, carrinho_id, produto_id, quantidade)
VALUES (201, 101, 1, 4),
       (202, 102, 1, 5),
       (203, 103, 2, 2),
       (204, 104, 3, 3);
