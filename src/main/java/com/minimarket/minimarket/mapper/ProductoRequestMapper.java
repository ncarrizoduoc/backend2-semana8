package com.minimarket.minimarket.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.dto.ProductoRequest;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.repository.CategoriaRepository;

@Component
public class ProductoRequestMapper {
    @Autowired
    private CategoriaRepository categoriaRepo;

    public Producto toProducto(ProductoRequest request){
        Producto producto = new Producto();
        Long categoriaId = request.getCategoriaId();
        if (categoriaRepo.existsById(categoriaId)){
            Categoria categoria = categoriaRepo.findById(categoriaId).get();
            producto.setCategoria(categoria);
        } else {
            throw new ResourceNotFoundException("No existe la categoria con el ID ingresado");
        }
        producto.setId(request.getId());
        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());

        return producto;
    }

}
