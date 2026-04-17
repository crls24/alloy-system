package com.system.alloy.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.system.alloy.entity.RolUsuario;
import com.system.alloy.entity.Usuario;
import com.system.alloy.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.setRol(RolUsuario.Administrador);
            usuarioRepository.save(admin);

            Usuario cajero = new Usuario();
            cajero.setUsername("cajero");
            cajero.setPasswordHash(passwordEncoder.encode("cajero"));
            cajero.setRol(RolUsuario.Cajero);
            usuarioRepository.save(cajero);

            Usuario dependiente = new Usuario();
            dependiente.setUsername("dependiente");
            dependiente.setPasswordHash(passwordEncoder.encode("dependiente"));
            dependiente.setRol(RolUsuario.Dependiente);
            usuarioRepository.save(dependiente);
        }
    }
}
