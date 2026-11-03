package com.duoc.cursos.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cambio de contraseña con sesión activa: Cognito exige la contraseña ACTUAL
 * para autorizar el cambio (la nueva debe cumplir la misma política del registro).
 */
@Data
public class CambiarContrasenaRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La nueva contraseña debe tener entre 8 y 64 caracteres")
    private String nuevaContrasena;
}
