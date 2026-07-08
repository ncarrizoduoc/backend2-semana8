package com.minimarket.minimarket.service.impl;

import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.InventarioRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepo;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Inventario save(Inventario inventario) {
        Producto producto = inventario.getProducto();

        // Actualizar stock de producto segun entrada o salida de producto
        if (inventario.getTipoMovimiento().equals("Entrada")){
            producto.setStock(producto.getStock() + inventario.getCantidad());
        } else if (inventario.getTipoMovimiento().equals("Salida")){
            producto.setStock(producto.getStock() - inventario.getCantidad());
            validarStock(producto);
        }
        productoRepo.save(producto);
        return inventarioRepository.save(inventario);
    }

    // Metodo que valida que el stock de un producto no sea negativo
    // Si el stock es negativo, lanza una excepcion
    public void validarStock(Producto producto){
        if (producto.getStock() < 0){
            throw new StockInsuficienteException(
                "Error al registrar movimiento de inventario: No hay stock suficiente del producto: "
                + producto.getNombre());
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("No existe el inventario con el ID ingresado"));
        
        // Revertir el stock del producto
        Producto producto = inventario.getProducto();
        if (inventario.getTipoMovimiento().equals("Entrada")){
            producto.setStock(producto.getStock() - inventario.getCantidad());
            validarStock(producto);
        } else if (inventario.getTipoMovimiento().equals("Salida")){
            producto.setStock(producto.getStock() + inventario.getCantidad());
        }
        productoRepo.save(producto);

        // Eliminar movimiento de inventario
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }
}
