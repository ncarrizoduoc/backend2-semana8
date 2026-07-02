package com.minimarket.minimarket.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Formato de error para Bad Request")
public class BadRequestResponse {
    @Schema(description = "Mensaje explicando la causa del error")
    private String detail;
    @Schema(description = "Endpoint que generó el error")
    private String instance;
    @Schema(description = "Código de status HTTP")
    private int status;
    @Schema(description = "Nombre del status HTTP")
    private String title;

}
