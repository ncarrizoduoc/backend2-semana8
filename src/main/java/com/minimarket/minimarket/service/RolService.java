package com.minimarket.minimarket.service;

import com.minimarket.minimarket.entity.Rol;

import java.util.Optional;

public interface RolService {
    Optional<Rol> findByNombre(String nombre);
}
