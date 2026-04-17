package com.system.alloy.views;

import com.system.alloy.entity.Articulo;
import com.system.alloy.service.ArticuloService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "catalogo", layout = MainLayout.class)
@PageTitle("Catálogo | Alloy System")
@RolesAllowed({"Administrador", "Dependiente"})
public class CatalogoView extends VerticalLayout {

    private final ArticuloService articuloService;
    private final Grid<Articulo> grid = new Grid<>(Articulo.class, false);

    private final Dialog dialog = new Dialog();
    private final TextField codigoIdentificador = new TextField("Código Identificador");
    private final TextField nombreDescriptivo = new TextField("Descripción del Artículo");
    private final BigDecimalField precioVenta = new BigDecimalField("Precio de Venta ($)");
    private final BigDecimalField cantidadDisponible = new BigDecimalField("Cantidad Inicial / Disponible");
    private final BigDecimalField limiteMinimo = new BigDecimalField("Límite de Alerta Mínima");

    private final Button btnGuardar = new Button("Guardar");
    private final Button btnCancelar = new Button("Cancelar");
    private final Binder<Articulo> binder = new Binder<>(Articulo.class);
    
    private final TextField campoBusqueda = new TextField();

    private Articulo articuloActual;

    public CatalogoView(ArticuloService articuloService) {
        this.articuloService = articuloService;
        setSizeFull();

        H2 titulo = new H2("Catálogo de Artículos");

        campoBusqueda.setPlaceholder("Buscar por nombre o código...");
        campoBusqueda.setPrefixComponent(VaadinIcon.SEARCH.create());
        campoBusqueda.setClearButtonVisible(true);
        campoBusqueda.setValueChangeMode(ValueChangeMode.LAZY);
        campoBusqueda.setWidth("300px");
        campoBusqueda.addValueChangeListener(e -> actualizarLista());

        Button btnNuevo = new Button("Nuevo Artículo", VaadinIcon.PLUS.create());
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> prepararCreacion());

        HorizontalLayout barraHerramientas = new HorizontalLayout(campoBusqueda, btnNuevo);
        barraHerramientas.setWidthFull();
        barraHerramientas.setJustifyContentMode(JustifyContentMode.START);

        configurarGrid();
        configurarFormulario();
        actualizarLista();

        add(titulo, barraHerramientas, grid);
    }

    private void configurarGrid() {
        grid.addColumn(Articulo::getCodigoIdentificador).setHeader("Código");
        grid.addColumn(Articulo::getNombreDescriptivo).setHeader("Descripción");
        grid.addColumn(Articulo::getPrecioVenta).setHeader("Precio de Venta");
        grid.addColumn(Articulo::getCantidadDisponible).setHeader("Cantidad Disponible");
        grid.addColumn(Articulo::getLimiteMinimo).setHeader("Límite Mínimo");

        grid.addComponentColumn(articulo -> {
            Button btnEditar = new Button(VaadinIcon.EDIT.create());
            btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnEditar.setTooltipText("Editar artículo");
            btnEditar.addClickListener(e -> prepararEdicion(articulo));

            Button btnBorrar = new Button(VaadinIcon.TRASH.create());
            btnBorrar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            btnBorrar.setTooltipText("Eliminar artículo");
            btnBorrar.addClickListener(e -> confirmarEliminacion(articulo));

            return new HorizontalLayout(btnEditar, btnBorrar);
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        grid.getColumns().forEach(col -> {
            col.setSortable(true);
            col.setAutoWidth(true);
        });
    }

    private void configurarFormulario() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(codigoIdentificador, nombreDescriptivo, precioVenta, cantidadDisponible, limiteMinimo);
        formLayout.setColspan(codigoIdentificador, 2);
        formLayout.setColspan(nombreDescriptivo, 2);

        binder.bindInstanceFields(this);

        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(formLayout);
        dialog.getFooter().add(btnCancelar, btnGuardar);
        
        btnCancelar.addClickListener(e -> dialog.close());
        btnGuardar.addClickListener(e -> guardarArticulo());
    }

    private void prepararCreacion() {
        articuloActual = new Articulo();
        binder.readBean(articuloActual); 
        codigoIdentificador.setReadOnly(false); 
        dialog.setHeaderTitle("Nuevo Artículo");
        dialog.open();
    }

    private void prepararEdicion(Articulo articulo) {
        articuloActual = articulo;
        binder.readBean(articuloActual); 
        codigoIdentificador.setReadOnly(true);
        dialog.setHeaderTitle("Editar Artículo");
        dialog.open();
    }

    private void guardarArticulo() {
        try {
            binder.writeBean(articuloActual);
            articuloService.save(articuloActual);
            mostrarNotificacion("Artículo guardado exitosamente", NotificationVariant.LUMO_SUCCESS);
            actualizarLista();
            dialog.close();
        } catch (ValidationException e) {
            mostrarNotificacion("Por favor, revise los campos obligatorios", NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            mostrarNotificacion("Error al guardar: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmarEliminacion(Articulo articulo) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Eliminar '" + articulo.getNombreDescriptivo() + "'");
        confirmDialog.setText("¿Está seguro de que desea eliminar este artículo del catálogo? Esta acción no se puede deshacer.");

        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Cancelar");

        confirmDialog.setConfirmText("Eliminar");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            try {
                articuloService.delete(articulo.getCodigoIdentificador());
                mostrarNotificacion("Artículo eliminado", NotificationVariant.LUMO_SUCCESS);
                actualizarLista();
            } catch (Exception e) {
                mostrarNotificacion("No se puede eliminar porque el artículo pertenece a una orden registrada.", NotificationVariant.LUMO_ERROR);
            }
        });

        confirmDialog.open();
    }

    private void actualizarLista() {
        String filtro = campoBusqueda.getValue().toLowerCase();
        
        if (filtro.isEmpty()) {
            grid.setItems(articuloService.findAll());
        } else {
            grid.setItems(articuloService.findAll().stream()
                .filter(a -> a.getNombreDescriptivo().toLowerCase().contains(filtro) || 
                             a.getCodigoIdentificador().toLowerCase().contains(filtro))
                .toList());
        }
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notificacion = Notification.show(mensaje, 3000, Notification.Position.BOTTOM_END);
        notificacion.addThemeVariants(variante);
    }
}
