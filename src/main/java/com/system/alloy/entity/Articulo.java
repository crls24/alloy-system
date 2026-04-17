package com.system.alloy.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "articulos")
public class Articulo {

    @Id
    @Column(name = "codigo_identificador", length = 50)
    private String codigoIdentificador;

    @Column(name = "nombre_descriptivo", nullable = false, length = 150)
    private String nombreDescriptivo;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "cantidad_disponible", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadDisponible;

    @Column(name = "limite_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteMinimo;

}
