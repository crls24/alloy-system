package com.system.alloy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.system.alloy.entity.EstadoOrden;
import com.system.alloy.entity.Orden;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {
    List<Orden> findByNombreClienteAndEstado(String nombreCliente, EstadoOrden estado);
}
