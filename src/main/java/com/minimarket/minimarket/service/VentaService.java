package com.minimarket.minimarket.service;

import com.minimarket.minimarket.entity.Venta;

import java.util.List;

public interface VentaService {
    List<Venta> findAll();
    Venta findById(Long id);
    Venta save(Venta venta);
    List<Venta> findByUsuarioId(Long usuarioId);
}
