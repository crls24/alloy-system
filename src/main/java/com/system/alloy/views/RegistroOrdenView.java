package com.system.alloy.views;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.system.alloy.entity.Articulo;
import com.system.alloy.entity.DetalleOrden;
import com.system.alloy.service.ArticuloService;
import com.system.alloy.service.OrdenService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "registro-orden", layout = MainLayout.class)
@PageTitle("Registrar Orden | Alloy System")
@RolesAllowed({"Administrador", "Dependiente"})
public class RegistroOrdenView extends VerticalLayout {

    private final ArticuloService articuloService;
    private final OrdenService ordenService;

    private final TextField nombreCliente = new TextField("Nombre del Cliente");
    private final TextField buscador = new TextField();
    private final Grid<Articulo> gridCatalogo = new Grid<>(Articulo.class, false);
    private final Grid<DetalleOrden> gridCarrito = new Grid<>(DetalleOrden.class, false);
    private final Button btnGuardarOrden = new Button("Crear Orden Pendiente", VaadinIcon.CHECK.create());

    private final List<DetalleOrden> carrito = new ArrayList<>();
    
    private static final String FORMATO_MONEDA = "%,.2f";
    private static final DecimalFormat QTY_FORMATTER = new DecimalFormat("#,##0.00");

    public RegistroOrdenView(ArticuloService articuloService, OrdenService ordenService) {
        this.articuloService = articuloService;
        this.ordenService = ordenService;
        
        setSizeFull();
        addClassNames(LumoUtility.Padding.LARGE);

        HorizontalLayout cabecera = configurarCabecera();
        SplitLayout layoutPrincipal = configurarLayoutDividido();

        add(cabecera, layoutPrincipal);
    }

    private HorizontalLayout configurarCabecera() {
        H2 titulo = new H2("Nueva Orden");
        titulo.addClassNames(LumoUtility.Margin.NONE); 

        HorizontalLayout cabecera = new HorizontalLayout(titulo);
        cabecera.setWidthFull();
        
        return cabecera;
    }

    private SplitLayout configurarLayoutDividido() {
        VerticalLayout panelIzquierdo = configurarPanelCatalogo();
        VerticalLayout panelDerecho = configurarPanelCarrito();

        SplitLayout split = new SplitLayout(panelIzquierdo, panelDerecho);
        split.setSplitterPosition(60); 
        split.setSizeFull();
        return split;
    }

    private VerticalLayout configurarPanelCatalogo() {
        buscador.setPlaceholder("Buscar artículo...");
        buscador.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscador.setClearButtonVisible(true);
        buscador.setValueChangeMode(ValueChangeMode.LAZY);
        buscador.addValueChangeListener(e -> actualizarCatalogo());

        gridCatalogo.addColumn(Articulo::getNombreDescriptivo).setHeader("Descripción").setAutoWidth(true);
        
        gridCatalogo.addColumn(articulo -> "$" + String.format(FORMATO_MONEDA, articulo.getPrecioVenta()))
                .setHeader("Precio").setAutoWidth(true);

        gridCatalogo.addColumn(articulo -> {
            BigDecimal enCarrito = carrito.stream()
                    .filter(d -> d.getArticulo().getCodigoIdentificador().equals(articulo.getCodigoIdentificador()))
                    .map(DetalleOrden::getCantidad)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal disponibleVisual = articulo.getCantidadDisponible().subtract(enCarrito);
            return QTY_FORMATTER.format(disponibleVisual);
        }).setHeader("Disp.").setAutoWidth(true);
        
        gridCatalogo.addComponentColumn(articulo -> {
            Button btnAgregar = new Button(VaadinIcon.PLUS.create());
            btnAgregar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnAgregar.addClickListener(e -> pedirCantidadYAgregar(articulo));
            return btnAgregar;
        }).setHeader("Añadir").setFlexGrow(0);

        actualizarCatalogo();

        VerticalLayout layout = new VerticalLayout(new H3("Catálogo"), buscador, gridCatalogo);
        layout.setSizeFull();
        return layout;
    }

    private VerticalLayout configurarPanelCarrito() {
        gridCarrito.addColumn(detalle -> detalle.getArticulo().getNombreDescriptivo()).setHeader("Artículo").setAutoWidth(true);
        
        gridCarrito.addColumn(detalle -> QTY_FORMATTER.format(detalle.getCantidad()))
                .setHeader("Cantidad").setAutoWidth(true);
        
        gridCarrito.addColumn(detalle -> {
            BigDecimal subtotal = detalle.getCantidad().multiply(detalle.getPrecioUnitario());
            return "$" + String.format(FORMATO_MONEDA, subtotal);
        }).setHeader("Subtotal").setAutoWidth(true);

        gridCarrito.addComponentColumn(detalle -> {
            Button btnQuitar = new Button(VaadinIcon.TRASH.create());
            btnQuitar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnQuitar.addClickListener(e -> {
                carrito.remove(detalle);
                actualizarCarrito();
            });
            return btnQuitar;
        }).setHeader("Quitar").setFlexGrow(0);

        nombreCliente.setPlaceholder("Ej. Juan Pérez");
        nombreCliente.setRequiredIndicatorVisible(true);
        nombreCliente.setWidthFull();
        nombreCliente.getStyle().set("margin-top", "var(--lumo-space-m)");

        btnGuardarOrden.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardarOrden.setWidthFull();
        btnGuardarOrden.getStyle().set("margin-top", "var(--lumo-space-m)");
        btnGuardarOrden.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        btnGuardarOrden.addClickListener(e -> guardarOrden());

        VerticalLayout layout = new VerticalLayout(new H3("Carrito"), gridCarrito, nombreCliente, btnGuardarOrden);
        layout.setSizeFull();
        return layout;
    }

    private void pedirCantidadYAgregar(Articulo articulo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Añadir: " + articulo.getNombreDescriptivo());

        BigDecimalField campoCantidad = new BigDecimalField("Cantidad a añadir");
        campoCantidad.setValue(BigDecimal.ONE);
        
        Button btnConfirmar = new Button("Añadir", e -> {
            BigDecimal qty = campoCantidad.getValue();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarNotificacion("Cantidad debe ser mayor a 0", NotificationVariant.LUMO_ERROR);
                return;
            }

            DetalleOrden detalleExistente = carrito.stream()
                    .filter(d -> d.getArticulo().getCodigoIdentificador().equals(articulo.getCodigoIdentificador()))
                    .findFirst()
                    .orElse(null);

            BigDecimal cantidadPrevia = detalleExistente != null ? detalleExistente.getCantidad() : BigDecimal.ZERO;
            BigDecimal totalDeseado = cantidadPrevia.add(qty);

            if (totalDeseado.compareTo(articulo.getCantidadDisponible()) > 0) {
                String errorMsg = "Stock insuficiente.";
                if (cantidadPrevia.compareTo(BigDecimal.ZERO) > 0) {
                    errorMsg += " Ya tienes " + QTY_FORMATTER.format(cantidadPrevia) + " en el carrito.";
                }
                mostrarNotificacion(errorMsg, NotificationVariant.LUMO_ERROR);
                return;
            }

            if (detalleExistente != null) {
                detalleExistente.setCantidad(totalDeseado);
            } else {
                DetalleOrden detalle = new DetalleOrden();
                detalle.setArticulo(articulo);
                detalle.setCantidad(qty);
                detalle.setPrecioUnitario(articulo.getPrecioVenta());
                carrito.add(detalle);
            }

            actualizarCarrito(); 
            dialog.close();
            mostrarNotificacion("Añadido al carrito", NotificationVariant.LUMO_SUCCESS);
        });
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        dialog.add(campoCantidad);
        dialog.getFooter().add(btnCancelar, btnConfirmar);
        dialog.open();
    }

    private void actualizarCatalogo() {
        String filtro = buscador.getValue().toLowerCase();
        if (filtro.isEmpty()) {
            gridCatalogo.setItems(articuloService.findAll());
        } else {
            gridCatalogo.setItems(articuloService.findAll().stream()
                .filter(a -> a.getNombreDescriptivo().toLowerCase().contains(filtro) || 
                             a.getCodigoIdentificador().toLowerCase().contains(filtro))
                .toList());
        }
    }

    private void actualizarCarrito() {
        gridCarrito.setItems(new ArrayList<>(carrito)); 
        gridCatalogo.getDataProvider().refreshAll();
    }

    private void guardarOrden() {
        String cliente = nombreCliente.getValue().trim();
        if (cliente.isEmpty()) {
            mostrarNotificacion("Falta nombre del cliente", NotificationVariant.LUMO_ERROR);
            return;
        }
        if (carrito.isEmpty()) {
            mostrarNotificacion("Carrito vacío", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            ordenService.crearOrdenPendiente(cliente, carrito);
            
            carrito.clear();
            actualizarCarrito();
            nombreCliente.clear();
            actualizarCatalogo(); 
            
            mostrarNotificacion("Orden pendiente creada exitosamente", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            mostrarNotificacion("Error al guardar: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 3000, Notification.Position.BOTTOM_END).addThemeVariants(variante);
    }
}
