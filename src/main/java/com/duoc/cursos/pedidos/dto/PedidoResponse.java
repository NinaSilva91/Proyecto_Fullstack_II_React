package com.duoc.cursos.pedidos.dto;

import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        Long id,
        Instant fecha,
        String estado,
        Integer total,
        List<Linea> lineas
) {
    public record Linea(String codigoProducto, String nombre, Integer precioUnitario, Integer cantidad) {
    }
}
