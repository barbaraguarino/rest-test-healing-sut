package br.edu.resttesthealingsut.produto.dto;

import br.edu.resttesthealingsut.produto.Categoria;
import br.edu.resttesthealingsut.produto.Produto;
import java.math.BigDecimal;
import java.util.List;

/**
 * Corpo da resposta com os dados de um produto. {@code descricao} pode ser {@code null};
 * {@code tags} é lista vazia quando o produto não tem tags.
 */
public record ProdutoResponse(
        Long id,
        String sku,
        String nome,
        String descricao,
        BigDecimal preco,
        Categoria categoria,
        Boolean ativo,
        Integer estoque,
        Integer pesoGramas,
        DimensoesResponse dimensoes,
        List<String> tags) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria(),
                produto.isAtivo(),
                produto.getEstoque(),
                produto.getPesoGramas(),
                DimensoesResponse.de(produto.getDimensoes()),
                produto.getTags());
    }
}
