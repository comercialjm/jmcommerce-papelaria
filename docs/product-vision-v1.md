# Documento de Visão do Produto — v1

**Projeto:** Loja Online de Papelaria — JM Code Studio  
**Data:** 20/07/2026  
**Autor:** Rafa (mentor) + Dev (proprietário)  
**Status:** Rascunho para revisão

---

## 1. Resumo Executivo

Uma loja virtual de papelaria com foco em experiência visual premium, servindo como **projeto vitrine** da JM Code Studio. O sistema deve demonstrar competência técnica em desenvolvimento fullstack, design responsivo e integração com serviços de pagamento e logística. A partir deste projeto, a empresa pretende oferecer desenvolvimento de lojas online customizadas para outros clientes.

## 2. Público-Alvo

**Primário (compradores):** Pessoas que buscam produtos de papelaria com apelo visual — cadernos, agendas, acessórios de mesa. Perfil predominantemente feminino, 18-35 anos, que valoriza estética e experiência de compra.

**Secundário (potenciais clientes da JM Code Studio):** Pequenos e médios lojistas que visitam o site e pensam "quero algo assim pra minha loja". Esse público avalia qualidade visual, responsividade, velocidade e profissionalismo.

## 3. Problema que Resolve

Para compradores: oferecer uma experiência de compra agradável e confiável para produtos de papelaria online.

Para a JM Code Studio: demonstrar capacidade de entrega de um produto web completo, funcional e visualmente atraente, servindo como portfólio vivo e gerador de leads.

## 4. Funcionalidades do MVP

### 4.1 Incluídas no MVP

- **Catálogo de produtos** — Grid responsivo com fotos, nome, preço. Filtro por categoria.
- **Página de produto** — Galeria de imagens, descrição, variações (cor/tamanho se aplicável), botão "Adicionar ao carrinho".
- **Carrinho de compras** — Adicionar, remover, alterar quantidade. Persistência (localStorage ou sessão).
- **Cálculo de frete por CEP** — Integração com API dos Correios ou similar (ex: Melhor Envio).
- **Checkout com pagamento** — Integração Mercado Pago Checkout Pro (ou Stripe). Sem tocar em dados de cartão.
- **Página institucional** — "Sobre a JM Code Studio" com link para contato/portfólio.
- **Design responsivo** — Mobile-first. O site precisa ser impecável em celular.
- **Painel administrativo básico** — CRUD de produtos (nome, descrição, preço, imagens, categoria, estoque).

### 4.2 Explicitamente FORA do MVP

- Nota fiscal eletrônica (NF-e)
- Sistema antifraude próprio
- Sistema de avaliações/reviews
- Wishlist / lista de desejos
- Programa de fidelidade / cupons
- Chat ao vivo / chatbot
- Integração com marketplaces (Mercado Livre, Amazon, etc.)
- Multi-idioma
- Blog integrado
- Sistema de busca avançada (Elasticsearch, Algolia)
- Login social (Google, Facebook)
- Notificações por e-mail transacional (confirmação de pedido, rastreio)
- Múltiplos métodos de envio
- Relatórios e dashboards de vendas

> **Nota:** Cada item acima pode virar uma fase futura. A lista existe para evitar scope creep.

## 5. Requisitos Não-Funcionais

- **Performance:** First Contentful Paint < 1.5s em 4G. Imagens otimizadas (WebP, lazy loading).
- **SEO:** SSR ou SSG nas páginas de produto e catálogo. Meta tags, Open Graph, sitemap.
- **Segurança:** HTTPS obrigatório. Nenhum dado de cartão no servidor. Sanitização de inputs. CORS configurado. Headers de segurança (CSP, X-Frame-Options, etc.).
- **Acessibilidade:** Nível mínimo WCAG 2.1 AA — contraste, navegação por teclado, alt text em imagens.
- **Responsividade:** Breakpoints: mobile (< 768px), tablet (768-1024px), desktop (> 1024px).

## 6. Stack Técnica (Proposta — Pendente Aprovação)

| Camada       | Tecnologia                      | Justificativa                                      |
|--------------|----------------------------------|-----------------------------------------------------|
| Backend      | Java 21 + Spring Boot 3         | Domínio do dev. Ecossistema maduro. API REST.       |
| Frontend     | Next.js 14+ (React)             | SSR/SSG pra SEO. Componentes reativos. Mercado.     |
| Banco        | PostgreSQL                      | Robusto, gratuito, bom pra e-commerce.              |
| Pagamento    | Mercado Pago Checkout Pro       | Sem PCI-DSS. Popular no Brasil. SDK em Java.        |
| Frete        | API Melhor Envio ou ViaCEP+Correios | Cálculo de frete por CEP simplificado.          |
| Hospedagem   | Railway / Render (backend) + Vercel (frontend) | Free tier ou custo mínimo. CI/CD integrado. |
| Imagens      | Cloudinary ou S3                | Upload e otimização automática.                     |
| Versionamento| Git + GitHub                    | Obrigatório. Branch strategy simplificada (trunk-based). |

## 7. Riscos Identificados

| Risco                                    | Probabilidade | Impacto | Mitigação                                                   |
|------------------------------------------|---------------|---------|--------------------------------------------------------------|
| Scope creep (adicionar features demais)  | Alta          | Alto    | Este documento. Dizer "não" ativamente.                      |
| Qualidade visual abaixo das inspirações  | Média         | Alto    | Wireframes antes de código. Usar design system (Shadcn/ui).  |
| Dev solo fica sobrecarregado             | Alta          | Alto    | Sprints de 2 semanas. WIP limit de 2. Kanban.                |
| Integração de pagamento falha            | Baixa         | Alto    | Usar checkout hospedado (redirect). Sandbox primeiro.         |
| Dados de cliente expostos                | Baixa         | Crítico | Nunca armazenar dados de cartão. HTTPS. Sanitização. LGPD básica. |
| Projeto abandonado por falta de motivação| Média         | Alto    | Marcos visíveis. Deploy contínuo. Resultados incrementais.    |

## 8. Marcos (Milestones)

| Marco | Descrição                                   | Entrega estimada   |
|-------|----------------------------------------------|---------------------|
| M0    | Documento de visão aprovado                  | Semana 1            |
| M1    | Wireframes das 6 páginas principais          | Semana 2-3          |
| M2    | Setup do projeto (repo, CI/CD, DB, ambientes)| Semana 3-4          |
| M3    | Backend: CRUD de produtos + API              | Semana 5-7          |
| M4    | Frontend: Catálogo + Página de produto       | Semana 7-9          |
| M5    | Carrinho + Cálculo de frete                  | Semana 9-11         |
| M6    | Checkout + Pagamento (sandbox)               | Semana 11-13        |
| M7    | Painel admin básico                          | Semana 13-15        |
| M8    | Testes, polimento, performance               | Semana 15-17        |
| M9    | Deploy em produção + dados reais             | Semana 17-18        |

> **Total estimado: ~4-5 meses** trabalhando meio período. Ajustável conforme velocidade real.

## 9. Critérios de Sucesso

O MVP é considerado bem-sucedido quando:

1. Um visitante consegue navegar pelo catálogo, ver um produto, adicionar ao carrinho, calcular frete e completar um pagamento (em sandbox ou produção).
2. O site é responsivo e visualmente comparável em qualidade às inspirações (NotebookTherapy / Erin Condren).
3. O painel admin permite cadastrar e editar produtos sem intervenção no banco de dados.
4. O site carrega em menos de 3 segundos em conexão 4G.
5. Um potencial cliente da JM Code Studio olha o site e entende que a empresa é capaz de entregar projetos profissionais.

---

**Próximo passo:** Revisar este documento. Questionar, adicionar, remover. Quando aprovado, partimos para wireframes (M1).
