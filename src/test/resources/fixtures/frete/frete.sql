-- Produto 1: 500 g, subtotal abaixo do limite do frete grátis. Produto 2: 1.800 g.
-- Produto 3: inativo e sem estoque, para mostrar que a cotação não aplica RN01 nem RN03.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-040', 'Livro de Testes', NULL, 50.00, 'LIVROS', TRUE, 10, 500, 23, 16, 3),
       (2, 'CAS-040', 'Luminaria', NULL, 80.00, 'CASA', TRUE, 10, 1800, 40, 20, 20),
       (3, 'ELE-040', 'Fone Inativo', NULL, 30.00, 'ELETRONICOS', FALSE, 0, 400, 5, 5, 5);
