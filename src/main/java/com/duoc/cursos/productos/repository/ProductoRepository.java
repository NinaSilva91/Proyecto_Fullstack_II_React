package com.duoc.cursos.productos.repository;

import com.duoc.cursos.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
