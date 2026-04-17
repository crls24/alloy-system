package com.system.alloy.views;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.system.alloy.entity.DetalleOrden;
import com.system.alloy.entity.EstadoOrden;
import com.system.alloy.entity.Factura;
import com.system.alloy.entity.Orden;
import com.system.alloy.repository.DetalleOrdenRepository;
import com.system.alloy.repository.OrdenRepository;
import com.system.alloy.service.FacturacionService;
import com.system.alloy.service.OrdenService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "caja", layout = MainLayout.class)
@PageTitle("Módulo de Caja | Alloy System")
@RolesAllowed({"Administrador", "Cajero"})
public class CajaView extends VerticalLayout {

    private final OrdenService ordenService;
    private final FacturacionService facturacionService;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final OrdenRepository ordenRepository;

    private final ComboBox<String> buscadorCliente = new ComboBox<>("Seleccionar Cliente");
    private final Button btnBuscar = new Button("Buscar Órdenes", VaadinIcon.SEARCH.create());
    
    private final Grid<Orden> gridOrdenes = new Grid<>(Orden.class, false);
    
    private final Paragraph txtSubtotal = new Paragraph("Subtotal: $0.00");
    private final Paragraph txtItbis = new Paragraph("ITBIS (18%): $0.00");
    private final Paragraph txtTotal = new Paragraph("TOTAL: $0.00");
    private final Button btnFacturar = new Button("Consolidar y Cobrar", VaadinIcon.MONEY.create());

    private String clienteActual = "";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private static final String FORMATO_MONEDA = "%,.2f";

    private static final DecimalFormat QTY_FORMATTER = new DecimalFormat("#,###.##");

    public CajaView(OrdenService ordenService, 
                    FacturacionService facturacionService, 
                    DetalleOrdenRepository detalleOrdenRepository,
                    OrdenRepository ordenRepository) {
        this.ordenService = ordenService;
        this.facturacionService = facturacionService;
        this.detalleOrdenRepository = detalleOrdenRepository;
        this.ordenRepository = ordenRepository;
        
        setSizeFull();
        addClassNames(LumoUtility.Padding.LARGE);

        H2 titulo = new H2("Facturación y Cobro");
        titulo.addClassNames(LumoUtility.Margin.NONE);

        HorizontalLayout barraBusqueda = configurarBuscador();
        VerticalLayout panelResultados = configurarTablaOrdenes();
        VerticalLayout panelTotales = configurarTotales();

        HorizontalLayout contenidoPrincipal = new HorizontalLayout(panelResultados, panelTotales);
        contenidoPrincipal.setSizeFull();
        contenidoPrincipal.setFlexGrow(1, panelResultados);
        contenidoPrincipal.setFlexGrow(0, panelTotales);

        add(titulo, barraBusqueda, contenidoPrincipal);
        
        actualizarClientesEnCombo();
    }

    private HorizontalLayout configurarBuscador() {
        buscadorCliente.setPlaceholder("Clientes con órdenes pendientes...");
        buscadorCliente.setWidth("400px");
        buscadorCliente.setAllowCustomValue(false);

        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnBuscar.addClickListener(e -> buscarOrdenesAccionManual());

        HorizontalLayout layout = new HorizontalLayout(buscadorCliente, btnBuscar);
        layout.setAlignItems(Alignment.END);
        layout.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);
        return layout;
    }

    private void actualizarClientesEnCombo() {
        List<String> clientes = ordenRepository.findAll().stream()
                .filter(o -> o.getEstado() == EstadoOrden.Pendiente)
                .map(Orden::getNombreCliente)
                .distinct()
                .collect(Collectors.toList());
        
        buscadorCliente.setItems(clientes);
    }

    private VerticalLayout configurarTablaOrdenes() {
        gridOrdenes.addColumn(Orden::getId).setHeader("ID").setAutoWidth(true);
        
        gridOrdenes.addColumn(orden -> 
            orden.getFechaCreacion() != null ? orden.getFechaCreacion().format(DATE_FORMATTER) : ""
        ).setHeader("Fecha").setAutoWidth(true);
        
        gridOrdenes.addColumn(orden -> {
            List<DetalleOrden> detalles = detalleOrdenRepository.findByOrden(orden);
            return detalles.stream()
                    .map(d -> QTY_FORMATTER.format(d.getCantidad()) + " x " + d.getArticulo().getNombreDescriptivo())
                    .collect(Collectors.joining(", "));
        }).setHeader("Artículos").setAutoWidth(true);

        gridOrdenes.addColumn(orden -> {
            BigDecimal total = BigDecimal.ZERO;
            List<DetalleOrden> detalles = detalleOrdenRepository.findByOrden(orden);
            for (DetalleOrden d : detalles) {
                total = total.add(d.getCantidad().multiply(d.getPrecioUnitario()));
            }
            return "$" + String.format(FORMATO_MONEDA, total);
        }).setHeader("Monto").setAutoWidth(true);

        gridOrdenes.addComponentColumn(orden -> {
            Button btnCancelar = new Button(VaadinIcon.CLOSE.create());
            btnCancelar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnCancelar.setTooltipText("Cancelar Orden (Devuelve Stock)");
            btnCancelar.addClickListener(e -> confirmarCancelacion(orden));
            return btnCancelar;
        }).setHeader("Anular").setFlexGrow(0);

        VerticalLayout layout = new VerticalLayout(new H3("Órdenes Pendientes"), gridOrdenes);
        layout.setPadding(false);
        layout.setSizeFull();
        return layout;
    }

    private VerticalLayout configurarTotales() {
        txtSubtotal.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        txtItbis.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        txtTotal.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD, LumoUtility.Margin.Top.SMALL, LumoUtility.Margin.Bottom.MEDIUM);

        btnFacturar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        btnFacturar.setEnabled(false);
        btnFacturar.setWidthFull();
        btnFacturar.addClickListener(e -> facturar());

        VerticalLayout layout = new VerticalLayout(new H3("Resumen"), txtSubtotal, txtItbis, txtTotal, btnFacturar);
        layout.setWidth("350px");
        layout.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM);
        layout.setAlignItems(Alignment.START);
        return layout;
    }

    private void buscarOrdenesAccionManual() {
        if (buscadorCliente.getValue() == null) {
            mostrarNotificacion("Seleccione un cliente", NotificationVariant.LUMO_WARNING);
            return;
        }
        ejecutarActualizacionUI(true);
    }

    private void ejecutarActualizacionUI(boolean mostrarMensajeVacio) {
        clienteActual = buscadorCliente.getValue();
        if (clienteActual == null) {
            resetTotales();
            gridOrdenes.setItems(List.of());
            return;
        }

        List<Orden> ordenes = ordenService.buscarOrdenesPendientesPorCliente(clienteActual);
        gridOrdenes.setItems(ordenes);

        if (ordenes.isEmpty()) {
            if (mostrarMensajeVacio) {
                mostrarNotificacion("Sin órdenes pendientes para " + clienteActual, NotificationVariant.LUMO_CONTRAST);
            }
            resetTotales();
            actualizarClientesEnCombo();
            buscadorCliente.clear();
        } else {
            calcularTotalesVisuales(ordenes);
            btnFacturar.setEnabled(true);
        }
    }

    private void calcularTotalesVisuales(List<Orden> ordenes) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Orden orden : ordenes) {
            List<DetalleOrden> detalles = detalleOrdenRepository.findByOrden(orden);
            for (DetalleOrden d : detalles) {
                subtotal = subtotal.add(d.getCantidad().multiply(d.getPrecioUnitario()));
            }
        }

        BigDecimal itbis = subtotal.multiply(new BigDecimal("0.18"));
        BigDecimal total = subtotal.add(itbis);

        txtSubtotal.setText(String.format("Subtotal: $" + FORMATO_MONEDA, subtotal));
        txtItbis.setText(String.format("ITBIS (18%%): $" + FORMATO_MONEDA, itbis));
        txtTotal.setText(String.format("TOTAL: $" + FORMATO_MONEDA, total));
    }

    private void resetTotales() {
        txtSubtotal.setText("Subtotal: $0.00");
        txtItbis.setText("ITBIS (18%): $0.00");
        txtTotal.setText("TOTAL: $0.00");
        btnFacturar.setEnabled(false);
    }

    private void confirmarCancelacion(Orden orden) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Cancelar Orden #" + orden.getId());
        dialog.setText("¿Anular orden y devolver stock?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Sí");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            try {
                ordenService.cancelarOrden(orden.getId());
                mostrarNotificacion("Anulada exitosamente", NotificationVariant.LUMO_SUCCESS);
                ejecutarActualizacionUI(false); 
                actualizarClientesEnCombo();
            } catch (Exception ex) {
                mostrarNotificacion("Error al anular", NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    private void facturar() {
        try {
            Factura factura = facturacionService.consolidarYFacturar(clienteActual);
            mostrarNotificacion("Éxito. Factura #" + factura.getId(), NotificationVariant.LUMO_SUCCESS);
            
            actualizarClientesEnCombo();
            gridOrdenes.setItems(List.of()); 
            resetTotales();
            buscadorCliente.clear();
        } catch (Exception e) {
            mostrarNotificacion("Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification.show(mensaje, 3000, Notification.Position.BOTTOM_END).addThemeVariants(variante);
    }
}