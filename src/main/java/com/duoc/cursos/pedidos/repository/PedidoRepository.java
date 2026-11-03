package com.duoc.cursos.pedidos.repository;

import com.duoc.cursos.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "lineas")
    List<Pedido> findByUsuarioIdOrderByFechaDesc(String usuarioId);

    @EntityGraph(attributePaths = "lineas")
    List<Pedido> findAllByOrderByFechaDesc();
}
