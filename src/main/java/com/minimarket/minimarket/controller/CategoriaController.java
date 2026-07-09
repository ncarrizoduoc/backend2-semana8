package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.CategoriaRequest;
import com.minimarket.minimarket.dto.CategoriaResponse;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categoria", description = "API para gestionar categorias en base de datos y realizar consultas.")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(
        summary = "Listar todas las categorias",
        description = "Retorna la lista completa de categorias en la base de datos. El acceso es público."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Lista de categorias obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse[].class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
        }
    )
    public List<CategoriaResponse> listarCategorias() {
        return categoriaService.findAll().stream().map(CategoriaResponse::new).toList();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar categoria por ID",
        description = "Busca una categoria en la base de datos por su ID y retorna sus datos. El acceso es público."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Categoria recuperada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Categoria no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<CategoriaResponse> obtenerCategoriaPorId(
        @Parameter(description = "ID de la categoria buscada", required = true) @PathVariable Long id
    ) {
        Categoria categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(new CategoriaResponse(categoria)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Registrar categoria",
        description = "Crea una categoria y la guarda en la base de datos. El acceso requiere rol ADMIN."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Categoria registrada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public CategoriaResponse guardarCategoria(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Categoria para guardar en base de datos", 
            required = true
        )
        @Valid @RequestBody CategoriaRequest request
    ) {
        sanitizarCategoria(request);
        Categoria categoria = request.toCategoria();
        categoria.setId(null);
        return new CategoriaResponse(categoriaService.save(categoria));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar datos de categoria",
        description = "Modifica los datos de la categoria en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Categoria actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Categoria no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<CategoriaResponse> actualizarCategoria(
        @Parameter(description = "ID de la categoria modificada", required = true) @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Categoria con datos actualizados", 
            required = true
        )
        @Valid @RequestBody CategoriaRequest request
    ) {
        sanitizarCategoria(request);
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            Categoria categoria = request.toCategoria();
            categoria.setId(id);
            categoria.setProductos(categoriaExistente.getProductos());
            return ResponseEntity.ok(new CategoriaResponse(categoriaService.save(categoria)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar categoria",
        description = "Elimina la categoria en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "Categoria eliminada exitosamente (No content)",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Categoria no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<Void> eliminarCategoria(
        @Parameter(description = "ID de la categoria que se desea eliminar", required = true) @PathVariable Long id
    ) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarCategoria(CategoriaRequest categoria){
        categoria.setNombre(sanitizeInput(categoria.getNombre()));
    }
}
