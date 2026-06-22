package com.minimarket.minimarket.service;

import com.minimarket.minimarket.entity.Producto;

import java.util.List;

public interface ProductoService {
    List<Producto> findAll();
    Producto findById(Long id);
    Producto save(Producto producto);
    void deleteById(Long id);
    List<Producto> findByCategoriaId(Long categoriaId);
}
