package com.system.alloy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.alloy.entity.Articulo;
import com.system.alloy.repository.ArticuloRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticuloService {

    private final ArticuloRepository articuloRepository;

    public List<Articulo> findAll() {
        return articuloRepository.findAll();
    }

    @Transactional
    public Articulo save(Articulo articulo) {
        return articuloRepository.save(articulo);
    }

    @Transactional
    public void delete(String codigoIdentificador) {
        articuloRepository.deleteById(codigoIdentificador);
    }

    public List<Articulo> obtenerArticulosBajoMinimo() {
        return articuloRepository.findAll().stream()
                .filter(a -> a.getCantidadDisponible().compareTo(a.getLimiteMinimo()) < 0)
                .collect(Collectors.toList());
    }
}
