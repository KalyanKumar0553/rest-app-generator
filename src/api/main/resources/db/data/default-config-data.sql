-- ================================
-- DATABASE DIALECT (GENERIC)
-- ================================
INSERT INTO config_property (category, label, property_key)
VALUES ('DATABASE', 'Database Dialect', 'database.dialect');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'database.dialect'), 'postgresql', 'PostgreSQL'),
((SELECT id FROM config_property WHERE property_key = 'database.dialect'), 'mysql', 'MySQL'),
((SELECT id FROM config_property WHERE property_key = 'database.dialect'), 'mariadb', 'MariaDB'),
((SELECT id FROM config_property WHERE property_key = 'database.dialect'), 'oracle', 'Oracle'),
((SELECT id FROM config_property WHERE property_key = 'database.dialect'), 'sqlserver', 'SQL Server'),
((SELECT id FROM config_property WHERE property_key = 'database.dialect'), 'h2', 'H2');

-- ================================
-- JPA / HIBERNATE
-- ================================
INSERT INTO config_property (category, label, property_key)
VALUES ('JPA', 'Schema Generation Strategy', 'spring.jpa.hibernate.ddl-auto');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.hibernate.ddl-auto'), 'none', 'None'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.hibernate.ddl-auto'), 'validate', 'Validate'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.hibernate.ddl-auto'), 'update', 'Update'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.hibernate.ddl-auto'), 'create', 'Create'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.hibernate.ddl-auto'), 'create-drop', 'Create Drop');

INSERT INTO config_property (category, label, property_key)
VALUES ('JPA', 'Show SQL', 'spring.jpa.show-sql');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.show-sql'), 'true', 'True'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.show-sql'), 'false', 'False');

INSERT INTO config_property (category, label, property_key)
VALUES ('JPA', 'Format SQL', 'spring.jpa.properties.hibernate.format_sql');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.format_sql'), 'true', 'True'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.format_sql'), 'false', 'False');

INSERT INTO config_property (category, label, property_key)
VALUES ('JPA', 'Highlight SQL', 'spring.jpa.properties.hibernate.highlight_sql');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.highlight_sql'), 'true', 'True'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.highlight_sql'), 'false', 'False');

INSERT INTO config_property (category, label, property_key)
VALUES ('JPA', 'Hibernate Dialect', 'spring.jpa.properties.hibernate.dialect');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.dialect'), 'PostgreSQLDialect', 'PostgreSQL Dialect'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.dialect'), 'MySQLDialect', 'MySQL Dialect'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.dialect'), 'OracleDialect', 'Oracle Dialect'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.dialect'), 'SQLServerDialect', 'SQL Server Dialect'),
((SELECT id FROM config_property WHERE property_key = 'spring.jpa.properties.hibernate.dialect'), 'H2Dialect', 'H2 Dialect');

-- ================================
-- HIKARI POOLING
-- ================================
INSERT INTO config_property (category, label, property_key)
VALUES ('POOLING', 'Max Pool Size', 'spring.datasource.hikari.maximum-pool-size');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.maximum-pool-size'), '5', '5'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.maximum-pool-size'), '10', '10'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.maximum-pool-size'), '20', '20'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.maximum-pool-size'), '30', '30'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.maximum-pool-size'), '50', '50'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.maximum-pool-size'), '100', '100');

INSERT INTO config_property (category, label, property_key)
VALUES ('POOLING', 'Minimum Idle', 'spring.datasource.hikari.minimum-idle');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.minimum-idle'), '0', '0'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.minimum-idle'), '2', '2'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.minimum-idle'), '5', '5'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.minimum-idle'), '10', '10'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.minimum-idle'), '20', '20');

INSERT INTO config_property (category, label, property_key)
VALUES ('POOLING', 'Connection Timeout (ms)', 'spring.datasource.hikari.connection-timeout');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.connection-timeout'), '30000', '30 seconds'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.connection-timeout'), '45000', '45 seconds'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.connection-timeout'), '60000', '60 seconds');

INSERT INTO config_property (category, label, property_key)
VALUES ('POOLING', 'Idle Timeout (ms)', 'spring.datasource.hikari.idle-timeout');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.idle-timeout'), '600000', '10 minutes'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.idle-timeout'), '900000', '15 minutes'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.idle-timeout'), '1200000', '20 minutes');

INSERT INTO config_property (category, label, property_key)
VALUES ('POOLING', 'Max Lifetime (ms)', 'spring.datasource.hikari.max-lifetime');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.max-lifetime'), '1800000', '30 minutes'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.max-lifetime'), '2400000', '40 minutes'),
((SELECT id FROM config_property WHERE property_key = 'spring.datasource.hikari.max-lifetime'), '0', 'Infinite');

-- ================================
-- SQL INIT
-- ================================
INSERT INTO config_property (category, label, property_key)
VALUES ('SQL_INIT', 'SQL Init Mode', 'spring.sql.init.mode');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.sql.init.mode'), 'never', 'Never'),
((SELECT id FROM config_property WHERE property_key = 'spring.sql.init.mode'), 'embedded', 'Embedded'),
((SELECT id FROM config_property WHERE property_key = 'spring.sql.init.mode'), 'always', 'Always');

-- ================================
-- FLYWAY
-- ================================
INSERT INTO config_property (category, label, property_key)
VALUES ('FLYWAY', 'Enable Flyway', 'spring.flyway.enabled');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.flyway.enabled'), 'true', 'True'),
((SELECT id FROM config_property WHERE property_key = 'spring.flyway.enabled'), 'false', 'False');

INSERT INTO config_property (category, label, property_key)
VALUES ('FLYWAY', 'Baseline On Migrate', 'spring.flyway.baseline-on-migrate');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.flyway.baseline-on-migrate'), 'true', 'True'),
((SELECT id FROM config_property WHERE property_key = 'spring.flyway.baseline-on-migrate'), 'false', 'False');

-- ================================
-- LIQUIBASE
-- ================================
INSERT INTO config_property (category, label, property_key)
VALUES ('LIQUIBASE', 'Enable Liquibase', 'spring.liquibase.enabled');

INSERT INTO config_property_values (property_id, value_key, value_label) VALUES
((SELECT id FROM config_property WHERE property_key = 'spring.liquibase.enabled'), 'true', 'True'),
((SELECT id FROM config_property WHERE property_key = 'spring.liquibase.enabled'), 'false', 'False');
