-- Insertar Artículos (Inventario mixto: unidades enteras y fracciones)
INSERT INTO articulos (codigo_identificador, nombre_descriptivo, precio_venta, cantidad_disponible, limite_minimo) VALUES 
('ELEC-001', 'Tomacorriente Doble Leviton', 150.00, 50.00, 10.00),
('ELEC-002', 'Cable THHN #12 AWG (por pie)', 15.00, 1000.00, 100.00),
('PIEZ-001', 'Clavos de Acero 2 pulgadas (por libra)', 80.00, 100.00, 20.00),
('PIEZ-002', 'Tornillo para Madera 1 pulgada (unidad)', 5.00, 500.00, 50.00);

-- El cliente "Juan Pérez" va al área de Electricidad y pide 2 tomacorrientes. 
-- El dependiente crea la cabecera de la orden:
-- INSERT INTO ordenes (nombre_cliente) VALUES ('Juan Pérez');
-- Asumiendo que esta es la orden ID 1, agregamos el detalle:
-- INSERT INTO detalles_orden (orden_id, articulo_codigo, cantidad, precio_unitario) 
-- VALUES (1, 'ELEC-001', 2.00, 150.00);

-- Luego, Juan va al área de Piezas y pide 1.5 libras de clavos a granel.
-- INSERT INTO ordenes (nombre_cliente) VALUES ('Juan Pérez');
-- Asumiendo que esta es la orden ID 2, agregamos el detalle fraccionado:
-- INSERT INTO detalles_orden (orden_id, articulo_codigo, cantidad, precio_unitario) 
-- VALUES (2, 'PIEZ-001', 1.50, 80.00);

-- Juan llega a la caja y da su nombre. El sistema busca rápidamente gracias al índice:
-- SELECT * FROM ordenes WHERE nombre_cliente = 'Juan Pérez' AND estado = 'Pendiente';

-- Para que el cajero vea el desglose en pantalla antes de cobrar (Uniendo las tablas):
-- SELECT o.id AS orden_id, a.nombre_descriptivo, d.cantidad, d.precio_unitario, (d.cantidad * d.precio_unitario) AS total_linea
-- FROM ordenes o
-- JOIN detalles_orden d ON o.id = d.orden_id
-- JOIN articulos a ON d.articulo_codigo = a.codigo_identificador
-- WHERE o.nombre_cliente = 'Juan Pérez' AND o.estado = 'Pendiente';

-- Cálculos que haría el sistema:
-- Subtotal: (2 * 150) + (1.5 * 80) = 300 + 120 = 420.00
-- ITBIS del 18 por ciento: 420 * 0.18 = 75.60
-- Total: 420 + 75.60 = 495.60

-- 1. Se crea el registro de la factura definitiva:
-- INSERT INTO facturas (nombre_cliente, monto_subtotal, monto_itbis, monto_total)
-- VALUES ('Juan Pérez', 420.00, 75.60, 495.60);

-- 2. Se actualizan las órdenes pendientes de Juan (Asumiendo que la factura generada es la ID 1):
-- UPDATE ordenes 
-- SET estado = 'Procesado', factura_id = 1 
-- WHERE nombre_cliente = 'Juan Pérez' AND estado = 'Pendiente';

-- 3. Se descuenta la mercancía del inventario general:
-- UPDATE articulos SET cantidad_disponible = cantidad_disponible - 2.00 WHERE codigo_identificador = 'ELEC-001';
-- UPDATE articulos SET cantidad_disponible = cantidad_disponible - 1.50 WHERE codigo_identificador = 'PIEZ-001';