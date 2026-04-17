package com.system.alloy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.system.alloy.entity.DetalleOrden;
import com.system.alloy.entity.Orden;

@Repository
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Integer> {
    List<DetalleOrden> findByOrden(Orden orden);
}
