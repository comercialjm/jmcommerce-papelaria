-- V1: Schema inicial da Loja de Papelaria
-- Reflete as decisões consolidadas em use-cases-v2.md

CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    imagem_url VARCHAR(500),
    parent_id BIGINT REFERENCES categoria(id), -- D2: modelado no banco, navegação plana no MVP (RN-38)
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_categoria_parent ON categoria(parent_id);

CREATE TABLE produto (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT NOT NULL,
    preco NUMERIC(10,2) NOT NULL CHECK (preco > 0),
    estoque INTEGER NOT NULL DEFAULT 0 CHECK (estoque >= 0),
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    peso_gramas INTEGER NOT NULL DEFAULT 300,       -- RN-16: peso p/ cálculo de frete
    largura_cm NUMERIC(5,2) NOT NULL DEFAULT 20,
    altura_cm NUMERIC(5,2) NOT NULL DEFAULT 15,
    comprimento_cm NUMERIC(5,2) NOT NULL DEFAULT 5,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,             -- RN-35: inativo some do catálogo, mas fica no histórico
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_produto_nome ON produto(nome);
CREATE INDEX idx_produto_categoria ON produto(categoria_id);

CREATE TABLE produto_imagem (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    ordem INTEGER NOT NULL DEFAULT 0                 -- RN-03: até 5 imagens, ordem de exibição
);

CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,

    -- D6: e-mail como chave natural, preparado para login futuro do cliente
    cliente_nome VARCHAR(150) NOT NULL,
    cliente_email VARCHAR(150) NOT NULL,
    cliente_telefone VARCHAR(20) NOT NULL,

    endereco_cep VARCHAR(9) NOT NULL,
    endereco_rua VARCHAR(200) NOT NULL,
    endereco_numero VARCHAR(20) NOT NULL,
    endereco_complemento VARCHAR(100),
    endereco_bairro VARCHAR(100) NOT NULL,
    endereco_cidade VARCHAR(100) NOT NULL,
    endereco_uf CHAR(2) NOT NULL,

    subtotal NUMERIC(10,2) NOT NULL,
    frete_valor NUMERIC(10,2) NOT NULL,
    frete_metodo VARCHAR(50),
    total NUMERIC(10,2) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO', -- fluxo definido em UC-16c
    stripe_session_id VARCHAR(200),
    codigo_rastreio VARCHAR(100),

    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_pedido_status ON pedido(status);
CREATE INDEX idx_pedido_data ON pedido(criado_em);
CREATE INDEX idx_pedido_email ON pedido(cliente_email); -- suporta futura consulta "minhas compras"

CREATE TABLE pedido_item (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    produto_nome VARCHAR(150) NOT NULL, -- snapshot: mantém o nome mesmo se o produto mudar depois
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(10,2) NOT NULL -- snapshot: preço no momento da compra (RN-14)
);

CREATE TABLE pedido_status_historico (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30) NOT NULL,
    alterado_por VARCHAR(100), -- 'sistema' ou o e-mail do admin (RN-43)
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE pedido_email_log (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL, -- CONFIRMACAO, ENVIADO, CANCELADO (UC-E1/E2/E3)
    sucesso BOOLEAN NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 1,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE administrador (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL, -- RN-29: BCrypt custo 12
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE configuracao_loja (
    id BIGSERIAL PRIMARY KEY,
    nome_loja VARCHAR(150) NOT NULL DEFAULT 'Loja de Papelaria', -- TODO: nome final da marca
    cep_origem VARCHAR(9) NOT NULL,
    contato_email VARCHAR(150),
    contato_whatsapp VARCHAR(20),
    banner_imagem_url VARCHAR(500), -- RN-46: banner hero único no MVP
    banner_titulo VARCHAR(200),
    banner_subtitulo VARCHAR(300),
    banner_link VARCHAR(500),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now()
);

-- Linha única de configuração (singleton) para o MVP
INSERT INTO configuracao_loja (cep_origem) VALUES ('00000-000');
