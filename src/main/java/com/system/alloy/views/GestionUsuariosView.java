package com.system.alloy.views;

import java.util.List;

import com.system.alloy.entity.RolUsuario;
import com.system.alloy.entity.Usuario;
import com.system.alloy.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "gestion-usuarios", layout = MainLayout.class)
@PageTitle("Gestión de Usuarios | Alloy System")
@RolesAllowed({"Administrador"})
public class GestionUsuariosView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final Grid<Usuario> gridUsuarios = new Grid<>(Usuario.class, false);
    
    public GestionUsuariosView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        
        setSizeFull();
        addClassNames(LumoUtility.Padding.LARGE);

        HorizontalLayout cabecera = configurarCabecera();
        configurarGrid();

        add(cabecera, gridUsuarios);
        actualizarGrid();
    }

    private HorizontalLayout configurarCabecera() {
        H2 titulo = new H2("Gestión de Usuarios");
        titulo.addClassNames(LumoUtility.Margin.NONE);

        Button btnNuevo = new Button("Nuevo Usuario", VaadinIcon.PLUS.create());
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> abrirFormulario(new Usuario()));

        HorizontalLayout cabecera = new HorizontalLayout(titulo, btnNuevo);
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cabecera.setAlignItems(Alignment.CENTER);
        cabecera.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        
        return cabecera;
    }

    private void configurarGrid() {
        gridUsuarios.addColumn(Usuario::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        gridUsuarios.addColumn(Usuario::getUsername).setHeader("Nombre de Usuario").setAutoWidth(true);
        
        gridUsuarios.addColumn(Usuario::getRol).setHeader("Rol Asignado").setAutoWidth(true);

        gridUsuarios.addComponentColumn(usuario -> {
            Button btnEditar = new Button(VaadinIcon.EDIT.create());
            btnEditar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnEditar.addClickListener(e -> abrirFormulario(usuario));

            Button btnEliminar = new Button(VaadinIcon.TRASH.create());
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            btnEliminar.addClickListener(e -> confirmarEliminacion(usuario));

            return new HorizontalLayout(btnEditar, btnEliminar);
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
    }

    private void actualizarGrid() {
        List<Usuario> usuarios = usuarioService.findAll();
        gridUsuarios.setItems(usuarios);
    }

    private void abrirFormulario(Usuario usuario) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        boolean esNuevo = usuario.getId() == null;
        dialog.setHeaderTitle(esNuevo ? "Crear Nuevo Usuario" : "Editar Usuario: " + usuario.getUsername());

        TextField usernameField = new TextField("Nombre de Usuario");
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setWidthFull();
        
        PasswordField passwordField = new PasswordField(esNuevo ? "Contraseña" : "Nueva Contraseña");
        passwordField.setPlaceholder(esNuevo ? "" : "Dejar en blanco para mantener");
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(esNuevo);
        
        ComboBox<RolUsuario> rolCombo = new ComboBox<>("Rol");
        rolCombo.setItems(RolUsuario.values());
        rolCombo.setRequiredIndicatorVisible(true);
        rolCombo.setWidthFull();

        if (!esNuevo) {
            usernameField.setValue(usuario.getUsername() != null ? usuario.getUsername() : "");
            rolCombo.setValue(usuario.getRol());
        }

        VerticalLayout layout = new VerticalLayout(usernameField, passwordField, rolCombo);
        layout.setPadding(false);

        Button btnGuardar = new Button("Guardar", e -> {
            String user = usernameField.getValue().trim();
            String pass = passwordField.getValue();
            RolUsuario rol = rolCombo.getValue();

            if (user.isEmpty() || rol == null || (esNuevo && pass.isEmpty())) {
                mostrarNotificacion("Por favor complete todos los campos obligatorios.", NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                usuario.setUsername(user);
                usuario.setRol(rol);
                
                if (!pass.isEmpty()) {
                    usuario.setPasswordHash(pass); 
                }

                usuarioService.save(usuario); 
                
                actualizarGrid();
                dialog.close();
                mostrarNotificacion("Usuario guardado exitosamente.", NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                mostrarNotificacion("Error al guardar: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(btnCancelar, btnGuardar);
        dialog.open();
    }

    private void confirmarEliminacion(Usuario usuario) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar Usuario");
        dialog.setText("¿Estás seguro de que deseas eliminar permanentemente a '" + usuario.getUsername() + "'? Esta acción no se puede deshacer.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Sí, Eliminar");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            try {
                usuarioService.deleteById(usuario.getId()); 
                actualizarGrid();
                mostrarNotificacion("Usuario eliminado.", NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                mostrarNotificacion("Error al eliminar usuario.", NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 3000, Notification.Position.BOTTOM_END).addThemeVariants(variante);
    }
}