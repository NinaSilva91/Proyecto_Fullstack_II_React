package com.duoc.cursos.productos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload para crear o actualizar un producto (lo usa el CRUD del backend).
 */
@Data
public class ProductoRequest {

    @NotBlank @Size(max = 20)
    private String codigo;

    @NotBlank @Size(max = 100)
    private String nombre;

    @NotBlank @Size(max = 500)
    private String descripcion;

    @NotNull @Min(0)
    private Integer precio;

    @NotNull @Min(0)
    private Integer stock;

    @Min(0)
    private Integer stockCritico;

    @NotBlank @Size(max = 50)
    private String categoria;

    @NotBlank @Size(max = 50)
    private String tipo;

    @NotBlank @Size(max = 200)
    private String imagen;

    private Boolean destacado = false;
}
