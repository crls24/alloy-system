DROP DATABASE alloy_system_db;

CREATE DATABASE alloy_system_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE alloy_system_db;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    rol ENUM('Administrador', 'Cajero', 'Dependiente') NOT NULL
);

CREATE TABLE articulos (
    codigo_identificador VARCHAR(50) PRIMARY KEY,
    nombre_descriptivo VARCHAR(150) NOT NULL,
    precio_venta DECIMAL(12, 2) NOT NULL CHECK (precio_venta >= 0),
    cantidad_disponible DECIMAL(10, 2) NOT NULL CHECK (cantidad_disponible >= 0),
    limite_minimo DECIMAL(10, 2) NOT NULL CHECK (limite_minimo >= 0)
);

CREATE TABLE facturas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_cliente VARCHAR(150) NOT NULL,
    monto_subtotal DECIMAL(15, 2) DEFAULT 0.00,
    monto_itbis DECIMAL(15, 2) DEFAULT 0.00, 
    monto_total DECIMAL(15, 2) DEFAULT 0.00,
    fecha_facturacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ordenes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_cliente VARCHAR(150) NOT NULL,
    estado ENUM('Pendiente', 'Procesado') DEFAULT 'Pendiente',
    factura_id INT DEFAULT NULL, 
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE SET NULL
);

CREATE TABLE detalles_orden (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orden_id INT NOT NULL,
    articulo_codigo VARCHAR(50) NOT NULL,
    cantidad DECIMAL(10, 2) NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (orden_id) REFERENCES ordenes(id) ON DELETE CASCADE,
    FOREIGN KEY (articulo_codigo) REFERENCES articulos(codigo_identificador) ON DELETE RESTRICT,
    UNIQUE(orden_id, articulo_codigo) 
);

CREATE INDEX idx_nombre_cliente_ordenes ON ordenes(nombre_cliente);