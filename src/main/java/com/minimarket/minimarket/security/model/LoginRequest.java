package com.minimarket.minimarket.security.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Credenciales de usuario para autenticacion")
public class LoginRequest {
    @Schema(description = "Nombre de usuario", example = "user")
    @NotNull(message = "Debe ingresar un nombre de usuario")
    private String username;

    @Schema(description = "Contrasena del usuario", example = "password")
    @NotNull(message = "Debe ingresar una contrasena")
    private String password;
}
