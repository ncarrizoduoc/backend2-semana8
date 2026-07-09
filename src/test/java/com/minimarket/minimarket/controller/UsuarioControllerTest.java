package com.minimarket.minimarket.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.minimarket.dto.UsuarioRequest;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.mapper.UsuarioRequestMapper;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.impl.UsuarioServiceImpl;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRequestMapper requestMapper;

    @MockitoBean
    private UsuarioServiceImpl usuarioService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Rol rol;
    private Usuario usuario;
    private UsuarioRequest request;

    @BeforeEach
    void setUp(){
        rol = Rol.builder()
            .id(Long.valueOf(1))
            .nombre("CAJERO")
            .usuarios(new HashSet<Usuario>())
            .build();

        usuario = Usuario.builder()
            .id(Long.valueOf(1))
            .username("prueba")
            .password("prueba123")
            .roles(new HashSet<Rol>(Set.of(rol)))
            .build();

        request = UsuarioRequest.builder()
            .id(Long.valueOf(1))
            .username("prueba")
            .password("prueba123")
            .rolesId(new ArrayList<Long>(List.of(Long.valueOf(1))))
            .build();
    }

    @AfterEach
    void tearDown(){
        rol = null;
        usuario = null;
    }

    // Prueba que verifica que solo un usuario autorizado (rol ADMIN) pueda
    // acceder al endpoint [GET /api/usuarios] para ver los usuarios
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeVerUsuariosTest() throws Exception{
        // Arrange
        List<Usuario> usuarios = new ArrayList<Usuario>(List.of(usuario));
        when(usuarioService.findAll()).thenReturn(usuarios);

        // Assert
        mockMvc.perform(get("/api/usuarios")) // Se llama al endpoint [GET /api/inventario]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$", hasSize(1))) // Se verifica que la lista de inventario retornada tenga 1 elemento
            .andExpect(jsonPath("$[0].id").value(Long.valueOf(1))) // Se verifica que el ID del elemento en la lista sea 1
            .andExpect(jsonPath("$[0].username").value("prueba")); // Se verifica el nombre del usuario
    }

    // Prueba que valida que un usuario no autorizado (sin rol ADMIN) no pueda
    // acceder al endpoint [GET /api/usuarios]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeVerUsuariosTest() throws Exception{
        mockMvc.perform(get("/api/usuarios")) // Llama el endpoint [GET /api/usuarios]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que verifica que solo un usuario autorizado (rol ADMIN) pueda
    // acceder al endpoint [GET /api/usuarios/{id}] para buscar un usuario
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeBuscarUsuarioTest() throws Exception{
        // Arrange
        when(usuarioService.findById(any(Long.class))).thenReturn(Optional.of(usuario));

        // Assert
        mockMvc.perform(get("/api/usuarios/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/inventario]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Se verifica que el ID del elemento en la lista sea 1
            .andExpect(jsonPath("$.username").value("prueba")) // Se verifica el nombre del usuario
            .andExpect(jsonPath("$.roles[0]").value("CAJERO")); // Se verifica el rol del usuario
        verify(usuarioService, times(1)).findById(Long.valueOf(1));
    }

    // Prueba que verifica que si un usuario autorizado (rol ADMIN) busca
    // un usuario con ID que no existe, usando el endpoint [GET /api/usuarios/{id}]
    // recibe como respuesta un status Not Found
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void buscarUsuarioRetornaNotFoundSiNoExisteTest() throws Exception{
        // Arrange
        when(usuarioService.findById(any(Long.class))).thenReturn(Optional.empty());

        // Assert
        mockMvc.perform(get("/api/usuarios/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/inventario]
            .andExpect(status().isNotFound());
        verify(usuarioService, times(1)).findById(Long.valueOf(1));
    }

    // Prueba que valida que un usuario no autorizado (sin rol ADMIN) no pueda
    // acceder al endpoint [GET /api/usuarios/{id}]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeBuscarUsuariosTest() throws Exception{
        mockMvc.perform(get("/api/usuarios/{id}", Long.valueOf(1))) // Llama el endpoint [GET /api/usuarios/1]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que un usuario autorizado pueda acceder al endpoint [POST /api/usuario]
    // para guardar un usuario. Debe retornar el usuario creado
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeGuardarUsuarioTest() throws Exception{
        // Arrange
        when(usuarioService.save(any(Usuario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(requestMapper.toUsuario(any(UsuarioRequest.class))).thenReturn(usuario);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("ContrasenaEncriptada");

        // Act y Assert
        mockMvc.perform(post("/api/usuarios")
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("prueba"));
            
        verify(usuarioService, times(1)).save(usuario);
    }

    // Prueba que valida que un usuario no autorizado no pueda acceder al endpoint [POST /api/usuario]
    // para guardar un usuario.
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeGuardarUsuarioTest() throws Exception{
        mockMvc.perform(post("/api/usuarios")
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    // Prueba que verifica que un usuario autorizado (con rol ADMIN) pueda usar
    // el endpoint [PUT /api/usuarios/{id}] para modificar los datos de un usuario
    // Debe retornar los datos actualizados del usuario
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeActualizarUsuarioTest() throws Exception{
        // Arrange
        when(usuarioService.findById(any(Long.class))).thenReturn(Optional.of(new Usuario()));
        when(usuarioService.save(any(Usuario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(requestMapper.toUsuario(any(UsuarioRequest.class))).thenReturn(usuario);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("ContrasenaEncriptada");

        // Act y Assert
        mockMvc.perform(put("/api/usuarios/{id}", Long.valueOf(1))
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(Long.valueOf(1)))
            .andExpect(jsonPath("$.username").value("prueba"))
            .andExpect(jsonPath("$.roles[0]").value("CAJERO"));
            
        verify(usuarioService, times(1)).findById(Long.valueOf(1));
        verify(usuarioService, times(1)).save(usuario);

    }

    // Prueba que verifica que el endpoint [PUT /api/usuarios/{id}] retorne un status
    // Not Found si el usuario con ID ingresado no existe
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void actualizarUsuarioRetornaNotFoundSiNoExisteTest() throws Exception{
        // Arrange
        when(usuarioService.findById(any(Long.class))).thenReturn(Optional.empty());
        
        // Act y Assert
        mockMvc.perform(put("/api/usuarios/{id}", Long.valueOf(1))
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isNotFound());
            
        verify(usuarioService, times(1)).findById(Long.valueOf(1));

    }

    // Prueba que valida que un usuario no autorizado no pueda acceder al endpoint [POST /api/usuario]
    // para guardar un usuario.
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeActualizarUsuarioTest() throws Exception{
        mockMvc.perform(put("/api/usuarios/{id}", Long.valueOf(1))
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    // Prueba que verifica que el endpoint [DELETE /api/usuarios/{id}] retorne un status
    // Not Found si el usuario con ID ingresado no existe
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeEliminarUsuarioTest() throws Exception{
        // Arrange
        when(usuarioService.findById(any(Long.class))).thenReturn(Optional.of(usuario));
        
        // Act y Assert
        mockMvc.perform(delete("/api/usuarios/{id}", Long.valueOf(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Usuario eliminado exitosamente"));
            
        verify(usuarioService, times(1)).findById(Long.valueOf(1));
        verify(usuarioService, times(1)).deleteById(Long.valueOf(1));

    }

    // Prueba que verifica que el endpoint [DELETE /api/usuarios/{id}] retorne un status
    // Not Found si el usuario con ID ingresado no existe
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void eliminarUsuarioRetornaNotFoundSiNoExisteTest() throws Exception{
        // Arrange
        when(usuarioService.findById(any(Long.class))).thenReturn(Optional.empty());
        
        // Act y Assert
        mockMvc.perform(delete("/api/usuarios/{id}", Long.valueOf(1)))
            .andExpect(status().isNotFound());
            
        verify(usuarioService, times(1)).findById(Long.valueOf(1));

    }


}
