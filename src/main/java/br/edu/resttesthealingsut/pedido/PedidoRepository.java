package br.edu.resttesthealingsut.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos pedidos no banco. Os itens são salvos junto com o pedido (cascade). */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
