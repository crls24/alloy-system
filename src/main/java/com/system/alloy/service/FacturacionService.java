package com.system.alloy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.alloy.entity.DetalleOrden;
import com.system.alloy.entity.EstadoOrden;
import com.system.alloy.entity.Factura;
import com.system.alloy.entity.Orden;
import com.system.alloy.repository.DetalleOrdenRepository;
import com.system.alloy.repository.FacturaRepository;
import com.system.alloy.repository.OrdenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FacturacionService {

    private final OrdenRepository ordenRepository;
    private final FacturaRepository facturaRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;

    private static final BigDecimal TASA_ITBIS = new BigDecimal("0.18");

    @Transactional 
    public Factura consolidarYFacturar(String nombreCliente) {
        List<Orden> ordenesPendientes = ordenRepository.findByNombreClienteAndEstado(nombreCliente, EstadoOrden.Pendiente);
        
        if (ordenesPendientes.isEmpty()) {
            throw new RuntimeException("No hay órdenes pendientes para el cliente: " + nombreCliente);
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Orden orden : ordenesPendientes) {
            List<DetalleOrden> detalles = detalleOrdenRepository.findByOrden(orden);
            
            for (DetalleOrden detalle : detalles) {
                BigDecimal totalLinea = detalle.getCantidad().multiply(detalle.getPrecioUnitario());
                subtotal = subtotal.add(totalLinea);
            }
        }
        
        BigDecimal montoItbis = subtotal.multiply(TASA_ITBIS);
        BigDecimal montoTotal = subtotal.add(montoItbis);

        Factura factura = new Factura();
        factura.setNombreCliente(nombreCliente);
        factura.setMontoSubtotal(subtotal);
        factura.setMontoItbis(montoItbis);
        factura.setMontoTotal(montoTotal);
        
        Factura facturaGuardada = facturaRepository.save(factura);

        for (Orden orden : ordenesPendientes) {
            orden.setEstado(EstadoOrden.Procesado);
            orden.setFactura(facturaGuardada);
            ordenRepository.save(orden);
        }

        return facturaGuardada;
    }
}
