package com.system.alloy.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("login")
@PageTitle("Iniciar Sesión | Alloy System")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassNames(LumoUtility.Background.CONTRAST_5);

        VerticalLayout tarjetaLogin = new VerticalLayout();
        tarjetaLogin.setSizeUndefined();
        tarjetaLogin.setAlignItems(Alignment.CENTER);
        tarjetaLogin.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BoxShadow.MEDIUM,
                LumoUtility.Padding.XLARGE
        );

        Icon iconoHerramientas = VaadinIcon.TOOLS.create();
        iconoHerramientas.setSize("48px");
        iconoHerramientas.setColor("var(--lumo-primary-color)");

        H1 titulo = new H1("Ferretería La Fuente");
        titulo.addClassNames(
                LumoUtility.Margin.Top.MEDIUM, 
                LumoUtility.Margin.Bottom.NONE, 
                LumoUtility.FontSize.XXLARGE
        );
        
        Paragraph subtitulo = new Paragraph("Alloy System - Gestión Empresarial");
        subtitulo.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.XSMALL);

        configurarFormularioLogin();

        tarjetaLogin.add(iconoHerramientas, titulo, subtitulo, login);

        add(tarjetaLogin);
    }

    private void configurarFormularioLogin() {
        LoginI18n i18n = LoginI18n.createDefault();
        
        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("Inicio de sesión");
        i18nForm.setUsername("Usuario");
        i18nForm.setPassword("Contraseña");
        i18nForm.setSubmit("Entrar al sistema");
        i18nForm.setForgotPassword("¿Olvidaste tu contraseña?");
        i18n.setForm(i18nForm);
        
        LoginI18n.ErrorMessage i18nError = i18n.getErrorMessage();
        i18nError.setTitle("Usuario o contraseña incorrectos");
        i18nError.setMessage("Verifica tus credenciales e inténtalo de nuevo.");
        i18n.setErrorMessage(i18nError);
        
        login.setI18n(i18n);

        login.getElement().setAttribute("autocomplete", "off");
        login.getElement().executeJs(
            "setTimeout(() => {" +
            "  const userField = document.querySelector('[name=\"username\"]');" +
            "  const passField = document.querySelector('[name=\"password\"]');" +
            "  if(userField) userField.setAttribute('autocomplete', 'off');" +
            "  if(passField) passField.setAttribute('autocomplete', 'off');" +
            "}, 50);"
        );

        login.setAction("login");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}