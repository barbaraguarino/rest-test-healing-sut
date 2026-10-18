-- Executado antes de cada teste, antes da fixture do teste. Apaga na ordem das chaves estrangeiras.
-- A contagem dos IDs gerados não volta a 1000; os testes não presumem IDs gerados.
DELETE FROM item_pedido;
DELETE FROM pedido;
DELETE FROM item_carrinho;
DELETE FROM carrinho;
DELETE FROM produto_tag;
DELETE FROM produto;
