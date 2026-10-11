-- Carrinho 101 vazio. Carrinho 102 com dois itens; o item do produto 2 foi incluído primeiro (id menor).
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-010', 'Livro de Testes', NULL, 25.50, 'LIVROS', TRUE, 20, 500, 23, 16, 3),
       (2, 'CAS-010', 'Caneca', NULL, 30.00, 'CASA', TRUE, 20, 350, 10, 8, 8);

INSERT INTO carrinho (id) VALUES (101), (102);

INSERT INTO item_carrinho (id, carrinho_id, produto_id, quantidade)
VALUES (201, 102, 2, 1),
       (202, 102, 1, 3);
