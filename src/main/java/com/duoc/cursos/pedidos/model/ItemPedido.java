package com.duoc.cursos.pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Línea de un pedido. Necesito guardar nombre/precio/imágen "congelados" al comprar,
 * porque el catálogo puede cambiar después (precio, o el producto llega a
 * eliminarse) y el pedido debe tener memoria de lo comprado.
 */
@Entity
@Table(name = "pedido_items")
@Data
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(nullable = false, length = 20)
    private String codigoProducto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;
}
