package com.system.alloy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.system.alloy.entity.Articulo;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, String> {
}
