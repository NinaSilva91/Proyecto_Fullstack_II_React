package com.duoc.cursos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class RegistroRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 64)
    private String password;

    @NotBlank @Size(max = 100)
    private String nombre;

    // Opcional: roles/grupos de Cognito (ej: ["VENDEDOR"]). Si no viene,
    // el usuario se guarda como CLIENTE localmente y sin grupos de Cognito.
    private Set<String> roles;

    // ----- Datos del perfil de la tienda (opcionalmente se registran junto a la cuenta) -----
    @Size(max = 12)
    private String run;

    @Size(max = 100)
    private String apellidos;

    @Size(max = 10)
    private String fechaNacimiento;

    @Size(max = 20)
    private String tipoUsuario;

    @Size(max = 50)
    private String region;

    @Size(max = 50)
    private String comuna;

    @Size(max = 150)
    private String direccion;
}
