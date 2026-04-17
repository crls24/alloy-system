package com.system.alloy.views;

import org.springframework.security.core.userdetails.UserDetails;

import com.system.alloy.security.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout {

    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        
        crearBarraSuperior();
        crearMenuLateral();
    }

    private void crearBarraSuperior() {
        DrawerToggle toggle = new DrawerToggle();

        H1 titulo = new H1("Alloy System");
        titulo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        HorizontalLayout header = new HorizontalLayout(toggle, titulo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(titulo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        UserDetails user = securityService.getAuthenticatedUser();
        if (user != null) {
            Span nombreUsuario = new Span("Hola, " + user.getUsername());
            nombreUsuario.addClassNames(LumoUtility.FontWeight.MEDIUM, LumoUtility.Margin.Right.MEDIUM);
            
            Button logout = new Button("Cerrar Sesión", e -> securityService.logout());
            header.add(nombreUsuario, logout);
        }

        addToNavbar(header);
    }

    private void crearMenuLateral() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Inicio", InicioView.class));

        UserDetails user = securityService.getAuthenticatedUser();
        if (user != null) {
            boolean esAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Administrador"));
            boolean esCajero = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Cajero"));
            boolean esDependiente = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Dependiente"));
            
            if (esAdmin || esDependiente) {
                nav.addItem(new SideNavItem("Catálogo de Artículos", CatalogoView.class));
                nav.addItem(new SideNavItem("Registrar Orden", RegistroOrdenView.class));
            }

            if (esAdmin || esCajero) {
                nav.addItem(new SideNavItem("Módulo de Caja", CajaView.class));
                nav.addItem(new SideNavItem("Gestión de Usuarios", GestionUsuariosView.class));
            }
        }

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        
        addToDrawer(scroller);
    }
}