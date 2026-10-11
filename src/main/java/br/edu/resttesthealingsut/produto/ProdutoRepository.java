package br.edu.resttesthealingsut.produto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos produtos no banco. As consultas são derivadas do nome do método pelo Spring Data. */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsBySku(String sku);

    /** Todos os produtos, ordenados por SKU (ordem explícita, exigida pelo determinismo). */
    List<Produto> findAllByOrderBySkuAsc();

    List<Produto> findByCategoriaOrderBySkuAsc(Categoria categoria);
}
