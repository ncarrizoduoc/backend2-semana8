package com.minimarket.minimarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.entity.Usuario;

@SpringBootTest
public class CarritoTest {

    @Test
    public void usuarioAsociadoACarritoEsCorrectoTest(){
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(Long.valueOf(99));
        usuario.setUsername("username");
        usuario.setPassword("password");

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);

        // Act
        Usuario usuarioCarrito = carrito.getUsuario();

        // Assert
        assertNotNull(usuarioCarrito); // Se verifica que el usuario no sea Null
        assertEquals(usuario, usuarioCarrito); // Se verifica que el usuario guardado sea igual al retornado
        assertEquals(usuario.getId(), Long.valueOf(99)); // Se verifican los datos del usuario
        assertEquals(usuario.getUsername(), "username");
        assertEquals(usuario.getPassword(), "password");


    }

}
