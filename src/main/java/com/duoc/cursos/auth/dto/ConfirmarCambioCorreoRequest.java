package com.duoc.cursos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Paso 2 del cambio de correo (con sesión activa): el frontend confirma el código
 * que Cognito mandó al correo NUEVO (lo guardamos en estado mientras llega el código).
 * Al confirmar, Cognito hace el cambio oficial y el backend actualiza su copia local.
 */
@Data
public class ConfirmarCambioCorreoRequest {

    @NotBlank(message = "El nuevo correo es obligatorio")
    @Email(message = "El nuevo correo no es válido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nuevoCorreo;

    @NotBlank(message = "El código de confirmación es obligatorio")
    private String codigo;
}
