package com.duoc.cursos.productos.dto;

/**
 * Respuesta del API con un producto del catálogo.
 */
public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer precio,
        Integer stock,
        Integer stockCritico,
        String categoria,
        String tipo,
        String imagen,
        Boolean destacado
) {
}
