-- V4: Corrige o tipo da coluna endereco_uf de CHAR(2) para VARCHAR(2).
--
-- Motivo: o Hibernate, ao validar o schema (ddl-auto: validate), sempre espera
-- VARCHAR para campos String, independente de columnDefinition. CHAR(2) e
-- VARCHAR(2) são equivalentes na prática para uma sigla de estado (ex: "SP"),
-- então ajustamos o banco em vez de lutar contra o validador do Hibernate.

ALTER TABLE pedido ALTER COLUMN endereco_uf TYPE VARCHAR(2);
