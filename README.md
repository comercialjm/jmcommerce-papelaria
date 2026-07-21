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

## Próximos marcos

Ver `docs/use-cases-v2.md`, seção "Marcos Atualizados", para o roadmap completo (M3 em diante).
