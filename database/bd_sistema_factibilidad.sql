-- ============================================================================
-- SISTEMA GESTOR DE FACTIBILIDAD CON PLANIFICACIÓN VISUAL
-- Script completo de base de datos (MySQL 8.0+)
-- ============================================================================

DROP DATABASE IF EXISTS sistema_factibilidad;
CREATE DATABASE sistema_factibilidad
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_factibilidad;

-- ----------------------------------------------------------------------------
-- 1. Tabla: usuario
-- Almacena los usuarios del sistema con contraseña cifrada en BCrypt ($2a$)
-- ----------------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('ANALISTA', 'APROBADOR', 'ADMINISTRADOR')),
    CONSTRAINT chk_usuario_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. Tabla: idea_negocio
-- Propuesta inicial registrada por un Analista antes de abrir el proyecto
-- ----------------------------------------------------------------------------
CREATE TABLE idea_negocio (
    id_idea INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_registro DATE NOT NULL,
    CONSTRAINT fk_idea_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. Tabla: proyecto
-- Proyecto de estudio de factibilidad vinculado a una idea y a un analista
-- ----------------------------------------------------------------------------
CREATE TABLE proyecto (
    id_proyecto INT AUTO_INCREMENT PRIMARY KEY,
    id_idea INT NOT NULL,
    id_analista INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    fecha_creacion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'EN_PLANIFICACION',
    CONSTRAINT fk_proyecto_idea FOREIGN KEY (id_idea) REFERENCES idea_negocio(id_idea),
    CONSTRAINT fk_proyecto_analista FOREIGN KEY (id_analista) REFERENCES usuario(id_usuario),
    CONSTRAINT chk_proyecto_estado CHECK (
        estado IN ('EN_PLANIFICACION', 'EN_EJECUCION', 'FINALIZADO', 'CANCELADO')
    )
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. Tabla: criterio
-- Catálogo fijo de los 6 criterios de factibilidad (igual peso)
-- ----------------------------------------------------------------------------
CREATE TABLE criterio (
    id_criterio INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT chk_criterio_tipo CHECK (
        tipo IN ('MERCADO', 'TECNICO', 'OPERATIVO', 'LEGAL', 'FINANCIERO', 'AMBIENTAL')
    )
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. Tabla: proyecto_criterio
-- Relación N:M entre proyecto y sus 6 criterios con el puntaje evaluado (0-100)
-- ----------------------------------------------------------------------------
CREATE TABLE proyecto_criterio (
    id_proyecto_criterio INT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto INT NOT NULL,
    id_criterio INT NOT NULL,
    puntaje DECIMAL(5,2) NULL,
    CONSTRAINT fk_pc_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto),
    CONSTRAINT fk_pc_criterio FOREIGN KEY (id_criterio) REFERENCES criterio(id_criterio),
    CONSTRAINT uq_proyecto_criterio UNIQUE (id_proyecto, id_criterio),
    CONSTRAINT chk_pc_puntaje CHECK (puntaje IS NULL OR (puntaje >= 0.00 AND puntaje <= 100.00))
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 6. Tabla: etapa
-- Etapas secuenciales planificadas dentro de cada criterio de un proyecto
-- ----------------------------------------------------------------------------
CREATE TABLE etapa (
    id_etapa INT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto_criterio INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    orden INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_cierre DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_etapa_pc FOREIGN KEY (id_proyecto_criterio) REFERENCES proyecto_criterio(id_proyecto_criterio),
    CONSTRAINT chk_etapa_fechas CHECK (fecha_cierre >= fecha_inicio),
    CONSTRAINT chk_etapa_orden CHECK (orden >= 1),
    CONSTRAINT chk_etapa_estado CHECK (
        estado IN ('PENDIENTE', 'EN_PROGRESO', 'COMPLETADA', 'BLOQUEADA')
    )
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 7. Tabla: actividad
-- Tareas operativas planificadas dentro de una etapa
-- ----------------------------------------------------------------------------
CREATE TABLE actividad (
    id_actividad INT AUTO_INCREMENT PRIMARY KEY,
    id_etapa INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    responsable VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_cierre DATE NOT NULL,
    terminada TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_actividad_etapa FOREIGN KEY (id_etapa) REFERENCES etapa(id_etapa),
    CONSTRAINT chk_actividad_fechas CHECK (fecha_cierre >= fecha_inicio),
    CONSTRAINT chk_actividad_terminada CHECK (terminada IN (0, 1))
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 8. Tabla: entregable
-- Evidencia asociada a una actividad que debe ser aprobada por un Aprobador
-- ----------------------------------------------------------------------------
CREATE TABLE entregable (
    id_entregable INT AUTO_INCREMENT PRIMARY KEY,
    id_actividad INT NOT NULL,
    id_aprobador INT NULL,
    nombre VARCHAR(150) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo TEXT NULL,
    fecha_revision DATETIME NULL,
    CONSTRAINT fk_entregable_actividad FOREIGN KEY (id_actividad) REFERENCES actividad(id_actividad),
    CONSTRAINT fk_entregable_aprobador FOREIGN KEY (id_aprobador) REFERENCES usuario(id_usuario),
    CONSTRAINT chk_entregable_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 9. Tabla: dato_estudio
-- Información cuantitativa o cualitativa recopilada por etapa
-- ----------------------------------------------------------------------------
CREATE TABLE dato_estudio (
    id_dato INT AUTO_INCREMENT PRIMARY KEY,
    id_etapa INT NOT NULL,
    categoria VARCHAR(20) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    monto DECIMAL(12,2) NULL,
    periodo VARCHAR(20) NULL,
    CONSTRAINT fk_dato_etapa FOREIGN KEY (id_etapa) REFERENCES etapa(id_etapa),
    CONSTRAINT chk_dato_categoria CHECK (
        categoria IN ('MERCADO', 'COSTO', 'INVERSION', 'FLUJO', 'RIESGO', 'SUPUESTO')
    )
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 10. Tabla: recomendacion
-- Resultado final calculado a partir del promedio de los 6 criterios
-- ----------------------------------------------------------------------------
CREATE TABLE recomendacion (
    id_recomendacion INT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto INT NOT NULL UNIQUE,
    promedio DECIMAL(5,2) NOT NULL,
    resultado VARCHAR(25) NOT NULL,
    fecha_generacion DATETIME NOT NULL,
    CONSTRAINT fk_recomendacion_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto),
    CONSTRAINT chk_recomendacion_resultado CHECK (
        resultado IN ('VIABLE', 'VIABLE_CON_AJUSTES', 'NO_VIABLE')
    )
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 11. Tabla: historial
-- Bitácora inmutable de auditoría (RNF7: no se puede editar ni borrar)
-- ----------------------------------------------------------------------------
CREATE TABLE historial (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tabla_afectada VARCHAR(50) NOT NULL,
    id_registro INT NOT NULL,
    accion VARCHAR(20) NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT chk_historial_accion CHECK (
        accion IN ('CREAR', 'MODIFICAR', 'APROBAR', 'RECHAZAR', 'FINALIZAR', 'DESACTIVAR', 'BLOQUEAR')
    )
) ENGINE=InnoDB;

-- ============================================================================
-- ÍNDICES DE RENDIMIENTO (RNF4 / RNF5: pantallas < 3s, calendario < 5s)
-- ============================================================================
CREATE INDEX idx_proyecto_analista ON proyecto(id_analista);
CREATE INDEX idx_proyecto_estado ON proyecto(estado);
CREATE INDEX idx_etapa_pc_orden ON etapa(id_proyecto_criterio, orden);
CREATE INDEX idx_actividad_etapa ON actividad(id_etapa);
CREATE INDEX idx_entregable_actividad ON entregable(id_actividad);
CREATE INDEX idx_entregable_estado ON entregable(estado);
CREATE INDEX idx_dato_etapa ON dato_estudio(id_etapa);
CREATE INDEX idx_historial_tabla_reg ON historial(tabla_afectada, id_registro);
CREATE INDEX idx_historial_fecha ON historial(fecha_hora);

-- ============================================================================
-- TRIGGERS DE INMUTABILIDAD EN HISTORIAL (RNF7)
-- Impiden modificar o eliminar registros de auditoría incluso desde SQL directo
-- ============================================================================
DELIMITER $$

CREATE TRIGGER trg_historial_no_update
BEFORE UPDATE ON historial
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El historial de cambios es inmutable y no puede modificarse.';
END$$

CREATE TRIGGER trg_historial_no_delete
BEFORE DELETE ON historial
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El historial de cambios es inmutable y no puede eliminarse.';
END$$

DELIMITER ;

-- ============================================================================
-- CATÁLOGO INICIAL DE LOS 6 CRITERIOS DE FACTIBILIDAD
-- ============================================================================
INSERT INTO criterio (tipo, nombre) VALUES
('MERCADO', 'Factibilidad de Mercado'),
('TECNICO', 'Factibilidad Técnica'),
('OPERATIVO', 'Factibilidad Operativa'),
('LEGAL', 'Factibilidad Legal'),
('FINANCIERO', 'Factibilidad Financiera'),
('AMBIENTAL', 'Factibilidad Ambiental');