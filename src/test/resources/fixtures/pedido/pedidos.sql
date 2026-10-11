-- Carrinho 101: subtotal 150,00 (1.400 g). Carrinho 102: subtotal 50,00 (400 g).
-- Carrinho 103: subtotal 100,05, para o arredondamento do desconto. Carrinho 104: vazio.
-- Pedido 301: já criado, com valores coerentes com as regras (EXPRESSA para SP, 700 g).
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-090', 'Livro de Testes', NULL, 50.00, 'LIVROS', TRUE, 10, 400, 23, 16, 3),
       (2, 'CAS-090', 'Caneca', NULL, 25.00, 'CASA', TRUE, 10, 300, 10, 8, 8),
       (3, 'VES-090', 'Jaqueta', NULL, 100.05, 'VESTUARIO', TRUE, 10, 500, 5, 30, 40);

INSERT INTO carrinho (id) VALUES (101), (102), (103), (104);

INSERT INTO item_carrinho (id, carrinho_id, produto_id, quantidade)
VALUES (201, 101, 1, 2),
       (202, 101, 2, 2),
       (203, 102, 1, 1),
       (204, 103, 3, 1);

INSERT INTO pedido (id, status, cliente_nome, cliente_email, endereco_logradouro, endereco_numero, endereco_complemento,
                    endereco_cidade, endereco_uf, endereco_cep, modalidade_frete, subtotal, desconto, frete, total)
VALUES (301, 'AGUARDANDO_PAGAMENTO', 'Bruno Lima', 'bruno@exemplo.com', 'Rua B', '20', 'Casa 2',
        'Campinas', 'SP', '13010000', 'EXPRESSA', 75.00, 0.00, 25.50, 100.50);

INSERT INTO item_pedido (id, pedido_id, produto_id, nome_produto, quantidade, preco_unitario, subtotal)
VALUES (401, 301, 1, 'Livro de Testes', 1, 50.00, 50.00),
       (402, 301, 2, 'Caneca', 1, 25.00, 25.00);
