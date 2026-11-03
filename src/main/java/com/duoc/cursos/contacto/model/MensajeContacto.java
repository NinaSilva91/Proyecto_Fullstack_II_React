package com.duoc.cursos.contacto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

/**
 * Mensaje enviado desde la página "Contáctanos" de la tienda.
 */
@Entity
@Table(name = "mensajes_contacto")
@Data
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 500)
    private String comentario;

    @Column(nullable = false)
    private Instant fecha = Instant.now();
}
