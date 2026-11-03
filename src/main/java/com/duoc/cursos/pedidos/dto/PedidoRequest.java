package com.duoc.cursos.pedidos.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PedidoRequest(
        @NotEmpty(message = "El carrito está vacío") List<ItemPedidoRequest> items
) {
}
