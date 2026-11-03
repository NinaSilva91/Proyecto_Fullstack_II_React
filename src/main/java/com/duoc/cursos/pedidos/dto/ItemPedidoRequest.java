package com.duoc.cursos.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Un ítem del pedido enviado desde el carrito: solo código y cantidad.
 * Nombre, precio e imagen los llena el backend con el catálogo actual
 * (así el total nunca se calcula desde el lado del cliente).
 */
public record ItemPedidoRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotNull Integer cantidad
) {
}
