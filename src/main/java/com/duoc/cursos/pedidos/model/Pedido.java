package com.duoc.cursos.pedidos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido generado al finalizar la compra desde el carrito.
 * usuarioId es el 'sub' de Cognito (mismo id que guarda la tabla usuarios).
 */
@Entity
@Table(name = "pedidos")
@Data
public class Pedido {

    public static final String ESTADO_CREADO = "CREADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, length = 64)
    private String usuarioId;

    @Column(length = 100)
    private String usuarioEmail;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Column(nullable = false, length = 20)
    private String estado = ESTADO_CREADO;

    @Column(nullable = false)
    private Integer total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> lineas = new ArrayList<>();
}
