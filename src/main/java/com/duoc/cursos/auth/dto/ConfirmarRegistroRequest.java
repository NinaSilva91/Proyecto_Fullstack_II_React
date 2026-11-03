package com.duoc.cursos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmarRegistroRequest {

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 4, max = 10)
    private String codigo;
}
