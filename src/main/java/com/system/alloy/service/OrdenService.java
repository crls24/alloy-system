package com.system.alloy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.alloy.entity.Articulo;
import com.system.alloy.entity.DetalleOrden;
import com.system.alloy.entity.EstadoOrden;
import com.system.alloy.entity.Orden;
import com.system.alloy.repository.ArticuloRepository;
import com.system.alloy.repository.DetalleOrdenRepository;
import com.system.alloy.repository.OrdenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final ArticuloRepository articuloRepository;

    @Transactional
    public Orden crearOrdenPendiente(String nombreCliente, List<DetalleOrden> detalles) {
        Orden nuevaOrden = new Orden();
        nuevaOrden.setNombreCliente(nombreCliente);
        nuevaOrden.setEstado(EstadoOrden.Pendiente);
        
        Orden ordenGuardada = ordenRepository.save(nuevaOrden);

        for (DetalleOrden detalle : detalles) {
            detalle.setOrden(ordenGuardada);
            detalleOrdenRepository.save(detalle);

            Articulo articulo = detalle.getArticulo();
            BigDecimal nuevoStock = articulo.getCantidadDisponible().subtract(detalle.getCantidad());
            
            if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Stock agotado durante transacción para: " + articulo.getNombreDescriptivo());
            }
            
            articulo.setCantidadDisponible(nuevoStock);
            articuloRepository.save(articulo);
        }

        return ordenGuardada;
    }

    @Transactional
    public void cancelarOrden(Integer ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (orden.getEstado() != EstadoOrden.Pendiente) {
            throw new RuntimeException("Solo se pueden cancelar órdenes pendientes");
        }

        List<DetalleOrden> detalles = detalleOrdenRepository.findByOrden(orden);
        
        for (DetalleOrden detalle : detalles) {
            Articulo articulo = detalle.getArticulo();
            articulo.setCantidadDisponible(articulo.getCantidadDisponible().add(detalle.getCantidad()));
            articuloRepository.save(articulo);
        }

        detalleOrdenRepository.deleteAll(detalles);
        ordenRepository.delete(orden);
    }

    public List<Orden> buscarOrdenesPendientesPorCliente(String nombreCliente) {
        return ordenRepository.findByNombreClienteAndEstado(nombreCliente, EstadoOrden.Pendiente);
    }
}
