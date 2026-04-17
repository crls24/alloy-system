package com.system.alloy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.system.alloy.entity.Factura;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {
}
