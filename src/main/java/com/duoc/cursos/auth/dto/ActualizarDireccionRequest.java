package com.duoc.cursos.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Actualiza los datos de ubicación del perfil de la tienda (se guardan en la base
 * del backend, NO son atributos de Cognito: en el registro también quedaron acá).
 * La dirección es obligatoria; región y comuna se actualizan solo si vienen.
 */
@Data
public class ActualizarDireccionRequest {

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String direccion;

    @Size(max = 50, message = "Máximo 50 caracteres")
    private String region;

    @Size(max = 50, message = "Máximo 50 caracteres")
    private String comuna;
}
