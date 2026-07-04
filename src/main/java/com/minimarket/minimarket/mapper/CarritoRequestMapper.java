package com.minimarket.minimarket.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.dto.CarritoRequest;
import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.repository.UsuarioRepository;

@Component
public class CarritoRequestMapper {
    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ProductoRepository productoRepo;

    public Carrito toCarrito(CarritoRequest request){
        Carrito carrito = new Carrito();

        // Agregar usuario por ID
        Long usuarioId = request.getUsuarioId();
        if (usuarioRepo.existsById(usuarioId)){
            Usuario usuario = usuarioRepo.findById(usuarioId).get();
            carrito.setUsuario(usuario);
        } else {
            throw new ResourceNotFoundException("No existe el usuario con el ID ingresado");
        }

        Long productoId = request.getProductoId();
        if (productoRepo.existsById(productoId)){
            Producto producto = productoRepo.findById(productoId).get();
            carrito.setProducto(producto);
        } else {
            throw new ResourceNotFoundException("No existe el producto con el ID ingresado");
        }

        carrito.setId(request.getId());
        carrito.setCantidad(request.getCantidad());

        return carrito;

    }
}
