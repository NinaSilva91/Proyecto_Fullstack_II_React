package com.duoc.cursos.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String idToken;
    private String refreshToken;
    private Integer expiresIn;

    /**
     * Nombre del challenge de Cognito si el login no resultó en tokens directo.
     * Ej: "NEW_PASSWORD_REQUIRED" (usuario con contraseña temporal / primer login).
     */
    private String challengeName;

    /**
     * Sesión de Cognito devuelta junto al challenge; debe enviarse tal cual
     * en el endpoint de confirmación de nueva contraseña.
     */
    private String session;

    public boolean requiereNuevoPassword() {
        return "NEW_PASSWORD_REQUIRED".equals(challengeName);
    }
}