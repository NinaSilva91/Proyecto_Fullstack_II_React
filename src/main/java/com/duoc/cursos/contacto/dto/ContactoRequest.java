package com.duoc.cursos.contacto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactoRequest {

    @NotBlank @Size(max = 100)
    private String nombre;

    @NotBlank @Email @Size(max = 100)
    private String email;

    @NotBlank @Size(max = 500)
    private String comentario;
}
