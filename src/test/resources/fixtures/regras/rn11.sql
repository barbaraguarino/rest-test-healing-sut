-- RN11 (cancelamento). Produto 1 com estoque 3, depois de já ter baixado as unidades dos pedidos.
-- Pedido 301 aguardando pagamento (2 unidades); pedido 302 já cancelado (1 unidade).
INSERT INTO produto (id, sku, nome, descricao, preco, categoria, ativo, estoque, peso_gramas, altura_cm, largura_cm, comprimento_cm)
VALUES (1, 'LIV-110', 'Livro de Testes', NULL, 50.00, 'LIVROS', TRUE, 3, 500, 23, 16, 3);

INSERT INTO pedido (id, status, cliente_nome, cliente_email, endereco_logradouro, endereco_numero, endereco_complemento,
                    endereco_cidade, endereco_uf, endereco_cep, modalidade_frete, subtotal, desconto, frete, total)
VALUES (301, 'AGUARDANDO_PAGAMENTO', 'Ana Souza', 'ana@exemplo.com', 'Rua A', '10', NULL,
        'Campinas', 'SP', '13010000', 'PADRAO', 100.00, 0.00, 17.00, 117.00),
       (302, 'CANCELADO', 'Ana Souza', 'ana@exemplo.com', 'Rua A', '10', NULL,
        'Campinas', 'SP', '13010000', 'PADRAO', 50.00, 0.00, 17.00, 67.00);

INSERT INTO item_pedido (id, pedido_id, produto_id, nome_produto, quantidade, preco_unitario, subtotal)
VALUES (401, 301, 1, 'Livro de Testes', 2, 50.00, 100.00),
       (402, 302, 1, 'Livro de Testes', 1, 50.00, 50.00);
