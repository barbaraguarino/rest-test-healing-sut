-- Inseridos fora da ordem de SKU, para que a ordenação da listagem seja de fato verificada.
-- O produto 2 é inativo e não tem descrição nem tags.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-002', 'Livro de Java', 'Livro sobre Java', 89.90, 'LIVROS', TRUE, 7, 600, 23, 16, 3),
       (2, 'LIV-001', 'Livro de SQL', NULL, 59.90, 'LIVROS', FALSE, 0, 400, 21, 14, 2),
       (3, 'CAS-001', 'Caneca', 'Caneca de ceramica', 25.00, 'CASA', TRUE, 30, 350, 10, 8, 8);

INSERT INTO produto_tag (produto_id, posicao, tag)
VALUES (1, 0, 'programacao'),
       (1, 1, 'java'),
       (3, 0, 'cozinha');
