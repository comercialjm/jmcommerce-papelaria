-- V5: Suporte a bloqueio de login (RN-29 a RN-32) e criação do administrador único do MVP.

ALTER TABLE administrador
    ADD COLUMN tentativas_login_falhas INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_ate TIMESTAMP;

-- RN-32: 1 usuário admin no MVP, criado via seed. Sem tela de cadastro.
-- Senha em texto plano: JmC0deStudi0 (hash BCrypt, custo 12 — nunca fica armazenada em texto puro)
INSERT INTO administrador (email, senha_hash) VALUES (
    'comercial@jmcodestudio.com.br',
    '$2b$12$U9kddntxgEtwc5pom.5ituiQsZOOGupQfnrI.3epufm119eqnHvDq'
);
