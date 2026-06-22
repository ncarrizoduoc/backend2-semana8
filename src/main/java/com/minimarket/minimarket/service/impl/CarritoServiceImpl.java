package com.minimarket.minimarket.service.impl;

import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.CarritoRepository;
import com.minimarket.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Carrito findById(Long id) {
        return carritoRepository.findById(id).orElse(null);
    }

    @Override
    public Carrito save(Carrito carrito) {
        validarStock(carrito);
        return carritoRepository.save(carrito);
    }

    @Override
    public void deleteById(Long id) {
        carritoRepository.deleteById(id);
    }

    @Override
    public List<Carrito> findByUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    // Metodo que valida que haya stock suficiente de un producto agregado al carrito
    private void validarStock(Carrito carrito){
        Producto producto = carrito.getProducto();
        // Si no hay stock suficiente, se lanza una excepcion
        if (producto.getStock() < carrito.getCantidad()){
            throw new StockInsuficienteException("Error al agregar al carrito: No hay stock suficiente del producto: "
                + producto.getNombre());
        }
    }
}
