# Alloy System - Sistema de Gestión Empresarial / Grupo #9

Alloy System es una aplicación web full-stack diseñada para fines didácticos y de gestión comercial. Permite administrar de manera eficiente el inventario, el registro de órdenes de clientes y la facturación, asegurando un control preciso del stock y un proceso de cobro fluido.

## Características Principales

* **Catálogo de Artículos:** Mantenimiento integral del inventario con capacidad de búsqueda en tiempo real.

* **Punto de Venta:** Soporte para ventas con cantidades fraccionales, ideal para artículos que se venden a la medida como cables por pie o clavos por libra. El sistema reserva el stock de forma visual y dinámica mientras el usuario llena el carrito.

* **Caja y Facturación:** Búsqueda ágil de clientes con órdenes pendientes para consolidar múltiples pedidos en una sola factura. Incluye el cálculo automático de subtotales y la aplicación del ITBIS al 18%. Además, cuenta con una opción para anular órdenes que devuelve los artículos automáticamente al inventario.

* **Seguridad y Accesos:** Autenticación robusta mediante encriptación BCrypt para las contraseñas, respaldada por un esquema de control de acceso estructurado para administradores, cajeros y dependientes.

## Tecnologías Utilizadas

Este proyecto está construido con un enfoque moderno en Java, combinando un backend robusto con una interfaz de usuario reactiva sin necesidad de escribir JavaScript.

* **Backend:** Java, Spring Boot y Spring Data JPA.
* **Seguridad:** Spring Security.
* **Frontend:** Vaadin Flow, aprovechando su diseño responsivo nativo y el sistema visual Lumo.
* **Base de Datos:** H2 Database configurada en memoria para agilizar el desarrollo inicial, con soporte completo para migrar a MySQL o PostgreSQL en producción.

## Requisitos Previos

Para ejecutar este proyecto en tu máquina local, necesitarás tener instalado:

* Java Development Kit (JDK), preferiblemente la versión 17 o superior.
* Maven para gestionar las dependencias del proyecto.
* Git.

## Cómo ejecutar el proyecto en local

1. **Clona este repositorio:**

   git clone https://github.com/crls24/alloy-system.git

2. **Navega al directorio del proyecto:**

   cd alloy-system

3. **Descarga las dependencias y ejecuta la aplicación:**

   mvn spring-boot:run

4. **Accede a la aplicación:**
   Abre tu navegador web y visita: http://localhost:8080

### Credenciales de prueba por defecto

Importante cambiar o eliminar estos usuarios antes de desplegar el sistema en un entorno real.

* **Administrador:** admin / admin
* **Cajero:** cajero / cajero
* **Dependiente:** dependiente / dependiente

## Desarrollo y Contribución

Si deseas contribuir o modificar el código:

1. Crea una rama para tu nueva característica con git checkout -b feature/nueva-caracteristica.
2. Realiza tus cambios y haz commit con git commit -m 'Añadir nueva característica'.
3. Sube los cambios a tu rama con git push origin feature/nueva-caracteristica.
4. Abre un Pull Request.

---
Desarrollado con fines didácticos y de demostración.
