package com.minimarket.minimarket.repository;

import com.minimarket.minimarket.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
