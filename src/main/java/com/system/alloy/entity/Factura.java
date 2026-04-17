package com.system.alloy.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    @Column(name = "monto_subtotal", precision = 15, scale = 2)
    private BigDecimal montoSubtotal;

    @Column(name = "monto_itbis", precision = 15, scale = 2)
    private BigDecimal montoItbis;

    @Column(name = "monto_total", precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fecha_facturacion", insertable = false, updatable = false)
    private LocalDateTime fechaFacturacion;
}
