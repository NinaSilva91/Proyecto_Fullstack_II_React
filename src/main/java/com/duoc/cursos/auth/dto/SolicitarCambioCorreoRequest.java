package com.duoc.cursos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Paso 1 del cambio de correo (con sesión activa): se manda el nuevo correo
 * MÁS la CONTRASEÑA ACTUAL (prueba de que es el dueño de la cuenta quien
 * pide el cambio) y Cognito luego envía un código al correo nuevo
 * (prueba de que ese correo existe y le pertenece). Si el User Pool no lo
 * permite, el backend avisa.
 */
@Data
public class SolicitarCambioCorreoRequest {

    @NotBlank(message = "Tu contraseña actual es obligatoria para verificar que eres tú")
    private String contrasenaActual;

    @NotBlank(message = "El nuevo correo es obligatorio")
    @Email(message = "El nuevo correo no es válido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nuevoCorreo;
}
