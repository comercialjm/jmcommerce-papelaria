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

INSERT INTO produto_imagem (produto_id, url, ordem)
    SELECT id, 'https://via.placeholder.com/600x600?text=Caderno+Floral', 0 FROM produto WHERE nome = 'Caderno floral A5';

INSERT INTO produto_imagem (produto_id, url, ordem)
    SELECT id, 'https://via.placeholder.com/600x600?text=Agenda+2027', 0 FROM produto WHERE nome = 'Agenda 2027 semanal';

INSERT INTO produto_imagem (produto_id, url, ordem)
    SELECT id, 'https://via.placeholder.com/600x600?text=Caneta+Gel', 0 FROM produto WHERE nome = 'Caneta gel pastel (kit 5 cores)';
