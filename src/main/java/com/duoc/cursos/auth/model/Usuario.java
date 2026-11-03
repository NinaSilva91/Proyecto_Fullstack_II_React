package com.duoc.cursos.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @Column(name = "id", length = 64)
    private String id; // Mapeado directamente al UUID 'sub' de Cognito

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nombre;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "rol", nullable = false)
    private Set<String> roles = new HashSet<>();

    // ----- Datos de perfil de la tienda (formulario de registro de Secret Sweet Maid) -----

    @Column(name = "run", length = 12)
    private String run;

    @Column(name = "apellidos", length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento", length = 10) // "yyyy-MM-dd"
    private String fechaNacimiento;

    @Column(name = "tipo_usuario", length = 20)
    private String tipoUsuario;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "comuna", length = 50)
    private String comuna;

    @Column(name = "direccion", length = 150)
    private String direccion;
}
