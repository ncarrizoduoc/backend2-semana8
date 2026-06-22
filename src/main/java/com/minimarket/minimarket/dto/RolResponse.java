package com.minimarket.minimarket.dto;

import com.minimarket.minimarket.entity.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolResponse {
    private Long id;
    private String nombre;
    
    public static RolResponse toRolResponse(Rol rol){
        RolResponse response = new RolResponse();
        response.setId(rol.getId());
        response.setNombre(rol.getNombre());
        
        return response;
    }

}
