-- V2: Dados de exemplo para testar o catálogo e a API durante o desenvolvimento.
-- Remover ou substituir por dados reais antes do lançamento (M10).

INSERT INTO categoria (nome, parent_id, ativa) VALUES
                                                   ('Cadernos', NULL, true),
                                                   ('Agendas', NULL, true),
                                                   ('Canetas', NULL, true);

INSERT INTO categoria (nome, parent_id, ativa)
SELECT 'Espiral', id, true FROM categoria WHERE nome = 'Cadernos';

INSERT INTO produto (nome, descricao, preco, estoque, categoria_id, peso_gramas)
SELECT 'Caderno floral A5', 'Caderno capa dura com estampa floral, 80 folhas pautadas.', 39.90, 12,
       id, 350
FROM categoria WHERE nome = 'Cadernos';

INSERT INTO produto (nome, descricao, preco, estoque, categoria_id, peso_gramas)
SELECT 'Agenda 2027 semanal', 'Agenda semanal com elástico e marcador de página.', 54.90, 8,
       id, 400
FROM categoria WHERE nome = 'Agendas';

INSERT INTO produto (nome, descricao, preco, estoque, categoria_id, peso_gramas)
SELECT 'Caneta gel pastel (kit 5 cores)', 'Kit com 5 canetas gel em tons pastel, ponta 0.7mm.', 24.90, 0,
       id, 80
FROM categoria WHERE nome = 'Canetas';

-- Imagens geradas como SVG embutido (data URI) — nenhuma chamada de rede externa,
-- portanto nunca quebram por instabilidade ou bloqueio de terceiros (ex: via.placeholder.com).
INSERT INTO produto_imagem (produto_id, url, ordem)
SELECT id,
       'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=''http://www.w3.org/2000/svg'' width=''600'' height=''600''%3E%3Crect width=''100%25'' height=''100%25'' fill=''%23C7B299''/%3E%3Ctext x=''50%25'' y=''50%25'' font-family=''sans-serif'' font-size=''32'' fill=''%232B2620'' text-anchor=''middle'' dominant-baseline=''middle''%3ECaderno Floral%3C/text%3E%3C/svg%3E',
       0
FROM produto WHERE nome = 'Caderno floral A5';

INSERT INTO produto_imagem (produto_id, url, ordem)
SELECT id,
       'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=''http://www.w3.org/2000/svg'' width=''600'' height=''600''%3E%3Crect width=''100%25'' height=''100%25'' fill=''%23C7B299''/%3E%3Ctext x=''50%25'' y=''50%25'' font-family=''sans-serif'' font-size=''32'' fill=''%232B2620'' text-anchor=''middle'' dominant-baseline=''middle''%3EAgenda 2027%3C/text%3E%3C/svg%3E',
       0
FROM produto WHERE nome = 'Agenda 2027 semanal';

INSERT INTO produto_imagem (produto_id, url, ordem)
SELECT id,
       'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=''http://www.w3.org/2000/svg'' width=''600'' height=''600''%3E%3Crect width=''100%25'' height=''100%25'' fill=''%23C7B299''/%3E%3Ctext x=''50%25'' y=''50%25'' font-family=''sans-serif'' font-size=''32'' fill=''%232B2620'' text-anchor=''middle'' dominant-baseline=''middle''%3ECaneta Gel%3C/text%3E%3C/svg%3E',
       0
FROM produto WHERE nome = 'Caneta gel pastel (kit 5 cores)';
