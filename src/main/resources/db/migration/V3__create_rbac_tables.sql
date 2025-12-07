CREATE TABLE IF NOT EXISTS role_permissions (
    id           BIGSERIAL PRIMARY KEY,
    role         VARCHAR(100)  NOT NULL,
    permission   VARCHAR(200)  NOT NULL
);
DELETE FROM role_permissions;
INSERT INTO role_permissions values(1,'USER','project.create');
INSERT INTO role_permissions values(2,'USER','project.read');
INSERT INTO role_permissions values(3,'USER','project.update');
INSERT INTO role_permissions values(4,'USER','project.delete');
INSERT INTO role_permissions values(5,'USER','dashboard.view');