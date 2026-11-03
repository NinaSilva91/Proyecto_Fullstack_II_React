package com.duoc.cursos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request para completar el cambio de contraseña obligatorio
 * (challenge NEW_PASSWORD_REQUIRED de Cognito, primer login con contraseña temporal).
 */
@Data
public class NuevaContrasenaRequest {
    @NotBlank @Email
    private String email;

    /** Sesión entregada por el login, se envía sin modificaciones. */
    @NotBlank
    private String session;

    @NotBlank @Size(min = 8, max = 64)
    private String nuevaContrasena;

    /**
     * Nombre del usuario. Opcional: solo es necesario si Cognito reporta que el
     * atributo "name" falta al resolver el challenge (usuarios creados sin él).
     */
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
}
