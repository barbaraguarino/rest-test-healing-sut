package br.edu.resttesthealingsut.carrinho;

import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos carrinhos no banco. Os itens são salvos junto com o carrinho (cascade). */
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {
}
