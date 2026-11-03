package com.duoc.cursos.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class UsuarioResponse {
    private String id;
    private String email;
    private String nombre;
    private Set<String> roles;
    private String run;
    private String apellidos;
    private String fechaNacimiento;
    private String tipoUsuario;
    private String region;
    private String comuna;
    private String direccion;
    // true cuando Cognito pidió verificación del email (el frontend debe pedir el código)
    private Boolean requiereVerificacion;
}
