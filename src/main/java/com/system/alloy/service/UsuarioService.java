package com.system.alloy.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.alloy.entity.Usuario;
import com.system.alloy.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        if (usuario.getPasswordHash() != null 
                && !usuario.getPasswordHash().startsWith("$2a$") 
                && !usuario.getPasswordHash().startsWith("$2b$")) {
            
            String passwordEncriptada = passwordEncoder.encode(usuario.getPasswordHash());
            usuario.setPasswordHash(passwordEncriptada);
        }
        
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deleteById(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
