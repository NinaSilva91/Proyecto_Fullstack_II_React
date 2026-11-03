package com.duoc.cursos.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entidad Producto: espejo del catálogo de la tienda (antes estaba solo en el frontend).
 * El campo 'codigo' es el identificador visible en las rutas (ej: /productos/SHI001).
 */
@Entity
@Table(name = "productos")
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false)
    private Integer stock;

    // si el stock baja de este número, el frontend avisa "quedan pocas unidades"
    @Column(name = "stock_critico")
    private Integer stockCritico;

    // la maid dueña del producto, o "grupal"
    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, length = 50)
    private String tipo;

    // ruta pública de la imagen (ej: /imgs/shikku.webp)
    @Column(nullable = false, length = 200)
    private String imagen;

    // si es true aparece en "Productos destacados" del inicio
    @Column(nullable = false)
    private Boolean destacado = false;
}
