# Loja de Papelaria

Projeto vitrine da **JM Code Studio**: e-commerce de papelaria construído para demonstrar a capacidade da
empresa de entregar sistemas de venda online customizados ("nós fazemos para você"), como alternativa a
plataformas self-service (Nuvemshop, Bagy).

Consulte `docs/use-cases-v2.md` e `docs/product-vision-v1.md` (não incluídos neste repo ainda — copiar da
pasta de planejamento) para o escopo completo aprovado.

## Stack

- Java 21 + Spring Boot 3.3
- Thymeleaf + HTMX (server-side rendering com interações dinâmicas)
- PostgreSQL + Flyway (migrations versionadas, nunca `ddl-auto: update`)
- Stripe Checkout (pagamento) — Mercado Pago como fallback
- Resend (e-mails transacionais)
- Hospedagem: Render

## Rodando localmente

Pré-requisitos: Java 21, Maven, Docker.

```bash
# 1. Sobe o banco PostgreSQL local
docker compose up -d

# 2. Roda a aplicação (perfil "dev" é o padrão)
./mvnw spring-boot:run

# 3. Acesse
http://localhost:8080
```

O Flyway aplica as migrations automaticamente na primeira subida. Não use `ddl-auto: update` do Hibernate —
toda alteração de schema deve virar um novo arquivo `V{N}__descricao.sql` em
`src/main/resources/db/migration`.

## Perfis de ambiente

| Perfil | Uso | Banco |
|---|---|---|
| `dev` | Desenvolvimento local | PostgreSQL via Docker Compose |
| `prod` | Produção (Render) | PostgreSQL gerenciado pelo Render, via variáveis de ambiente |

## Deploy (Render)

1. Conecte este repositório GitHub ao Render (Render detecta o `render.yaml` automaticamente).
2. Configure manualmente no dashboard do Render (nunca commitar no Git):
   - `STRIPE_SECRET_KEY`
   - `STRIPE_WEBHOOK_SECRET`
   - `RESEND_API_KEY`
3. Todo push na branch `main` que passar no CI dispara deploy automático.

## Estratégia de Git

- **Trunk-based development**: uma branch `main` sempre estável.
- Trabalho em features: branch curta `feature/nome-da-feature`, merge via PR após o CI passar.
- Commits em português, no imperativo: `adiciona CRUD de produtos`, não `added products crud`.

## Estrutura de pacotes

```
com.jmcodestudio.papelaria
├── controller   → endpoints HTTP (MVC + REST)
├── service      → regras de negócio
├── repository   → acesso a dados (Spring Data JPA)
├── entity       → entidades JPA (mapeiam as tabelas do Flyway)
├── dto          → objetos de transferência (formulários, respostas)
└── config       → configuração do Spring (segurança, etc.)
```

## Decisões arquiteturais registradas

- Carrinho em `localStorage` no navegador — backend sempre revalida preço e estoque no checkout.
- Sem variações de produto (SKU) no MVP — cada cor/tamanho é um produto separado.
- Categorias com `parent_id` no banco, mas navegação pública plana (subcategorias prontas para ativação
  futura sem migração de schema).
- E-mail do cliente como chave natural — prepara o schema para login de cliente numa fase futura, sem
  quebrar o modelo atual.
- Rodapé "Desenvolvido por JM Code Studio" é fixo no código, não editável pelo admin.

## Frontend — bases (Thymeleaf + HTMX)

- **Layout**: `templates/layout/main.html` é o template mestre (Thymeleaf Layout Dialect). Toda página nova
  deve usar `layout:decorate="~{layout/main}"` e colocar seu conteúdo em `layout:fragment="content"` — veja
  `templates/home.html` como exemplo mínimo.
- **Fragmentos**: `templates/fragments/header.html` e `footer.html` são incluídos automaticamente pelo
  layout. Não duplique cabeçalho/rodapé em outras páginas.
- **Tokens de design**: `static/css/tokens.css` define cores, tipografia e espaçamento nomeados por
  material (verde-ledger, kraft, tinta, etc.) — direção visual aprovada em 22/07/2026, inspirada no universo
  de livro-caixa e papel kraft. Nunca use valores de cor/fonte "soltos" no CSS; sempre referencie uma
  variável de `tokens.css`.
- **Elementos-assinatura**: classes `.regua-ledger` (divisor de seção) e `.selo` (badge circular tipo
  carimbo) são o fio condutor visual do site. Reutilize-os em vez de criar novos estilos de divisor/badge.
- **HTMX**: carregado via CDN no layout. Fragmentos parciais (ex: recalcular carrinho sem reload) serão
  implementados a partir do M5.
- **Carrinho**: `static/js/main.js` já lê o `localStorage` para atualizar o contador no ícone do header
  (RN-09). A lógica completa de adicionar/remover itens entra no M5.



**Deploy falha com `URL must start with 'jdbc'`**: o Render expõe a conexão do Postgres via `connectionString`
no formato `postgres://user:senha@host:porta/banco`, que não é aceito pelo HikariCP/Spring (exige
`jdbc:postgresql://...`). A correção já está aplicada: usamos as propriedades separadas (`host`, `port`,
`database`, `user`, `password`) do banco no `render.yaml` e montamos a URL JDBC manualmente em
`application-prod.yml`. Se você recriar o `render.yaml` do zero, lembre-se dessa pegadinha.

## Regra de ouro do Flyway

## Frete (Melhor Envio)

O cálculo de frete (UC-07) usa a API do Melhor Envio (sandbox por padrão). Para funcionar localmente:

1. Crie uma conta em https://sandbox.melhorenvio.com.br/
2. Vá em **Gerenciar → Tokens → Novo token**, marque a permissão **Cotação de fretes**
   (`shipping-calculate`) e copie o token gerado.
3. Configure a variável de ambiente `MELHOR_ENVIO_TOKEN` com esse valor (nunca no código —
   no IntelliJ, adicione em Run/Debug Configuration → Environment variables).

O token do sandbox expira em 30 dias; será necessário gerar um novo periodicamente durante o
desenvolvimento. Em produção, avaliar se vale a pena implementar o fluxo completo de
`refresh_token` (válido por 45 dias) para não depender de renovação manual.


**Nunca edite uma migration que já foi aplicada em um ambiente compartilhado (Render/produção).**
O Flyway grava um checksum de cada migration aplicada; editar o arquivo depois quebra a validação
(`Migration checksum mismatch`) e trava o deploy. Se precisar corrigir dados ou schema, sempre crie uma
migration **nova** (`V3__descricao.sql`, `V4__...`, etc.) — nunca edite `V1` ou `V2` outra vez.

Em caso de mismatch em ambiente de desenvolvimento local (onde os dados são descartáveis), a solução é
resetar o banco: `docker compose down -v && docker compose up -d`. Em produção, nunca faça isso sem
confirmar antes que não há dados reais a perder — e sempre prefira `flyway repair` ou uma migration nova
a apagar o schema, quando houver dados de verdade em jogo.

## Próximos marcos

Ver `docs/use-cases-v2.md`, seção "Marcos Atualizados", para o roadmap completo (M3 em diante).
