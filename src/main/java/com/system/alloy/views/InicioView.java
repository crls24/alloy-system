package com.system.alloy.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Inicio | Alloy System")
@PermitAll
public class InicioView extends VerticalLayout {

    public InicioView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        H2 bienvenida = new H2("Bienvenido a Alloy System");
        Paragraph descripcion = new Paragraph("Sistema de Gestión de Inventario y Ventas - Ferretería La Fuente");
        
        add(bienvenida, descripcion);
    }
}
