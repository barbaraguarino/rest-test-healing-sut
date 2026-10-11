-- RN01: produto inativo, com estoque, para que só a regra de produto ativo possa recusar a inclusão.
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'ELE-020', 'Fone Inativo', NULL, 50.00, 'ELETRONICOS', FALSE, 10, 300, 5, 5, 5);

INSERT INTO carrinho (id) VALUES (101);
