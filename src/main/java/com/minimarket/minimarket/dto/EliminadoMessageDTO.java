package com.minimarket.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para respuesta con mensaje confirmando eliminacion de objeto")
public class EliminadoMessageDTO {
    @Schema(description = "Mensaje confirmando eliminacion del objeto", example = "Objeto eliminado exitosamente")
    private String message;

}
