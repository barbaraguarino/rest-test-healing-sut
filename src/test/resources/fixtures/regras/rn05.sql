-- RN05 (adicional por quilo iniciado). Pesos nos dois lados do limite de um quilo:
-- produto 1 com exatamente 1 kg, produto 2 com 1 g a mais.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-050', 'Livro de 1 kg', NULL, 50.00, 'LIVROS', TRUE, 10, 1000, 23, 16, 5),
       (2, 'LIV-051', 'Livro de 1001 g', NULL, 50.00, 'LIVROS', TRUE, 10, 1001, 23, 16, 5);
