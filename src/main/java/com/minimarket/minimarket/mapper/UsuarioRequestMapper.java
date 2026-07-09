package com.minimarket.minimarket.mapper;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.dto.UsuarioRequest;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.repository.RolRepository;

@Component
public class UsuarioRequestMapper {
    
    @Autowired
    private RolRepository rolRepo;

    public Usuario toUsuario(UsuarioRequest request){
        Usuario usuario = Usuario.builder()
            .id(request.getId())
            .username(request.getUsername())
            .password((request.getPassword()))
            .roles(new HashSet<Rol>())
            .build();

        // Se agregan los roles el usuario al set de roles
        for (Long rolId : request.getRolesId()){
            Rol rol = rolRepo.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el rol con el ID ingresado"));
            usuario.getRoles().add(rol);
        }

        return usuario;
    }

}
