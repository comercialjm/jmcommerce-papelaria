-- V6: Limpeza final antes do lançamento (M10). Remove todos os dados de teste
-- acumulados durante o desenvolvimento (pedidos, produtos, categorias de exemplo),
-- deixando a loja pronta para o cadastro real via painel admin.
--
-- NÃO apaga: administrador (login real já configurado) nem configuracao_loja
-- (editável normalmente pela tela de Configurações, sem necessidade de migration).
--
-- Ordem respeita as chaves estrangeiras: filhos antes dos pais.

DELETE FROM pedido_email_log;
DELETE FROM pedido_status_historico;
DELETE FROM pedido_item;
DELETE FROM pedido;

DELETE FROM produto_imagem;
DELETE FROM produto;

DELETE FROM categoria;
