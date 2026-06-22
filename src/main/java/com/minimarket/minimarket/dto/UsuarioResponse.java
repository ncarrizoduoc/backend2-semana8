package com.minimarket.minimarket.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.minimarket.minimarket.entity.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
DTO para respuestas que incluyan objetos Usuario. Por seguridad, este DTO omite el 
atributo password, correspondiente a la contrasena del usuario.
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String username;
    private Set<String> roles;

    // Metodo estatico para convertir Usuario a UsuarioResponse
    static public UsuarioResponse toUsuarioResponse(Usuario usuario){
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setRoles(
            usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toSet()));

        return response;
    }
}
