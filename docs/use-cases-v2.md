# Casos de Uso — Loja Online de Papelaria — v2 (Aprovado)

**Projeto:** JM Code Studio — E-commerce de Papelaria  
**Nome provisório da loja:** Loja de Papelaria (TODO: definir nome/marca final)  
**Data:** 21/07/2026  
**Versão:** 2.0  
**Status:** Aprovado após revisão com PO

---

## Decisões Consolidadas nesta Revisão

| # | Decisão | Resultado |
|---|---------|-----------|
| D1 | Variações de produto (SKU) | Não no MVP. Cada cor/tamanho = produto separado. |
| D2 | Subcategorias | Modelar no banco (parent_id). Navegação plana no MVP. |
| D3 | Carrinho | localStorage no navegador. Backend valida tudo no checkout. |
| D4 | E-mails transacionais | 3 e-mails (confirmação, enviado, cancelado). Serviço: Resend. |
| D5 | Frete grátis | Não no MVP. |
| D6 | Cadastro/login do cliente | Fora do MVP. Arquitetura preparada (e-mail como chave natural). |
| D7 | Página "Sobre" | Eliminada. Rodapé com link externo para jmcodestudio.com.br. |
| D8 | Qtd. produtos no lançamento | ~20 produtos. Paginação simples. |
| D9 | Gateway de pagamento | Stripe (primário). Mercado Pago (fallback). |
| D10 | Stack | Java 21 + Spring Boot 3 + Thymeleaf + HTMX + PostgreSQL. |

---

## Atores do Sistema

| Ator | Descrição |
|------|-----------|
| **Visitante** | Pessoa que acessa o site sem estar autenticada. Pode navegar, ver produtos, adicionar ao carrinho. |
| **Cliente** | Visitante que preenche seus dados no checkout para concluir uma compra. Não exige cadastro/login no MVP. Arquitetura preparada para login futuro (e-mail como chave natural para vincular pedidos a conta futura). |
| **Administrador** | Dono da loja. Responsável pela gestão do catálogo, categorias e pedidos. Acessa o painel admin com login/senha. Único usuário admin no MVP (seed no banco). |
| **Stripe** | Sistema externo de pagamento. Processa transações e retorna status via webhook. |
| **API de Frete** | Sistema externo (Melhor Envio, ViaCEP ou Correios) que calcula custo e prazo de envio. |
| **Resend** | Sistema externo de envio de e-mails transacionais. Dispara notificações de status de pedido ao cliente. |

---

## Jornada 1 — Visitante Navegando

### UC-01: Acessar a Home Page

**Ator:** Visitante  
**Pré-condição:** Nenhuma

**Fluxo Principal:**
1. O visitante acessa a URL principal do site.
2. O sistema exibe a home page contendo:
   - Banner hero com imagem de destaque (imagem e texto configuráveis pelo admin no futuro).
   - Seção de produtos em destaque (ex: "Novidades" ou "Mais Vendidos").
   - Categorias navegáveis com imagens representativas.
   - Rodapé com "Desenvolvido por JM Code Studio" e link para jmcodestudio.com.br.
3. O visitante pode clicar em qualquer produto, categoria ou link de navegação.

**Fluxo Alternativo:**
- 1a. Se o sistema estiver fora do ar, exibe página de erro amigável: "Estamos em manutenção, volte em breve."

**Pós-condição:** Visitante está na home e pode iniciar navegação.

---

### UC-02: Navegar pelo Catálogo de Produtos

**Ator:** Visitante  
**Pré-condição:** Visitante está em qualquer página do site

**Fluxo Principal:**
1. O visitante clica em "Produtos" no menu de navegação ou em uma categoria específica.
2. O sistema exibe uma grade (grid) de produtos contendo: imagem principal, nome, preço (R$), badge de status se aplicável ("Novo", "Esgotado").
3. O visitante pode filtrar por categoria usando menu lateral ou barra de filtros (navegação plana — sem subcategorias na UI do MVP).
4. O visitante pode ordenar por: preço (menor/maior), nome (A-Z), mais recentes.
5. Se houver muitos produtos, o sistema pagina os resultados (12 por página).

**Fluxo Alternativo:**
- 3a. Categoria sem produtos: "Nenhum produto encontrado nesta categoria."
- 5a. Poucos produtos (< 12): sem paginação.

**Regras de Negócio:**
- RN-01: Produtos com estoque = 0 aparecem com badge "Esgotado" e botão de compra desabilitado.
- RN-02: A imagem exibida no grid é a primeira imagem cadastrada para o produto.

**Nota:** Com ~20 produtos no lançamento, a paginação será rara inicialmente mas deve existir para escalabilidade.

---

### UC-03: Visualizar Detalhes do Produto

**Ator:** Visitante  
**Pré-condição:** Visitante está no catálogo ou chegou via link direto

**Fluxo Principal:**
1. O visitante clica em um produto no catálogo.
2. O sistema exibe a página do produto contendo:
   - Galeria de imagens (imagem principal grande + miniaturas navegáveis).
   - Nome do produto.
   - Preço em R$.
   - Descrição detalhada.
   - Categoria do produto.
   - Indicação de disponibilidade ("Em estoque" / "Esgotado").
   - Seletor de quantidade (mínimo 1, máximo = estoque disponível).
   - Botão "Adicionar ao Carrinho".
3. O visitante ajusta a quantidade desejada.
4. O visitante clica em "Adicionar ao Carrinho" (ver UC-05).

**Fluxo Alternativo:**
- 2a. Produto esgotado (estoque = 0): botão desabilitado com texto "Produto Esgotado".
- 2b. Produto não existe (URL inválida): página 404 personalizada.

**Regras de Negócio:**
- RN-03: Galeria suporta de 1 a 5 imagens por produto.
- RN-04: Quantidade máxima selecionável = estoque atual.
- RN-05: Preço exibido no formato brasileiro: R$ 49,90.
- RN-06: Sem variações de produto no MVP. Cada cor/tamanho é um produto separado.

---

### UC-04: Buscar Produto (Busca Simples)

**Ator:** Visitante  
**Pré-condição:** Visitante está em qualquer página do site

**Fluxo Principal:**
1. O visitante digita um termo no campo de busca (ex: "caderno floral").
2. O sistema busca por correspondência no nome e na descrição (ILIKE).
3. O sistema exibe os resultados no formato do catálogo (grid).

**Fluxo Alternativo:**
- 2a. Nenhum resultado: "Nenhum produto encontrado para '[termo]'. Tente outro termo ou navegue pelas categorias."
- 1a. Busca vazia: redireciona para o catálogo completo.

**Regras de Negócio:**
- RN-07: Busca é case-insensitive e ignora acentos.
- RN-08: Mínimo de 2 caracteres para executar a busca.

---

## Jornada 2 — Comprando

### UC-05: Adicionar Produto ao Carrinho

**Ator:** Visitante  
**Pré-condição:** Visitante está na página de detalhes de um produto com estoque > 0

**Fluxo Principal:**
1. O visitante define a quantidade desejada (padrão: 1).
2. O visitante clica em "Adicionar ao Carrinho".
3. O sistema armazena o item no localStorage do navegador (productId, quantidade, preço snapshot).
4. O sistema exibe feedback visual (toast "Produto adicionado ao carrinho!" + contador no ícone do carrinho atualiza).
5. O visitante permanece na página do produto.

**Fluxo Alternativo:**
- 1a. Quantidade maior que estoque: o sistema limita ao máximo e exibe aviso.
- 3a. Produto já no carrinho: soma a quantidade nova à existente (respeitando RN-04).

**Regras de Negócio:**
- RN-09: Carrinho armazenado em localStorage. Persiste mesmo fechando o navegador.
- RN-10: O localStorage guarda: productId, quantidade e um snapshot do preço. O backend SEMPRE revalida preços e estoque no checkout (nunca confia no frontend).
- RN-11: O carrinho não reserva estoque. Verificação final ocorre no checkout.

---

### UC-06: Visualizar e Gerenciar o Carrinho

**Ator:** Visitante  
**Pré-condição:** Visitante tem pelo menos 1 item no carrinho (localStorage)

**Fluxo Principal:**
1. O visitante clica no ícone do carrinho no header.
2. O sistema lê o localStorage e faz uma requisição ao backend para obter dados atualizados dos produtos (nome, imagem, preço atual, estoque atual).
3. O sistema exibe a página do carrinho com, para cada item: imagem miniatura, nome (link para produto), preço unitário atual, seletor de quantidade (editável), subtotal do item, botão "Remover".
4. Abaixo da lista: subtotal geral, seção de cálculo de frete (UC-07), total estimado (subtotal + frete), botão "Finalizar Compra".

**Fluxo Alternativo:**
- 2a. Carrinho vazio: "Seu carrinho está vazio. [Continuar comprando →]".
- 2b. Visitante altera quantidade: localStorage atualiza, HTMX recalcula subtotal e total sem reload.
- 2c. "Remover": item sai do localStorage, recalcula totais.
- 2d. Produto foi desativado ou ficou sem estoque desde a adição: exibe aviso e remove do carrinho automaticamente.

**Regras de Negócio:**
- RN-12: Ao alterar quantidade para 0, o item é removido.
- RN-13: Se o estoque mudou desde a adição, o sistema ajusta e exibe aviso: "A quantidade de [produto] foi ajustada para X unidades (estoque atual)."
- RN-14: O preço exibido no carrinho é sempre o preço ATUAL do banco, não o snapshot do localStorage.

---

### UC-07: Calcular Frete por CEP

**Ator:** Visitante  
**Pré-condição:** Carrinho com pelo menos 1 item

**Fluxo Principal:**
1. Na página do carrinho, o visitante digita o CEP.
2. O visitante clica em "Calcular Frete".
3. O sistema valida o formato (8 dígitos numéricos).
4. O sistema consulta a API de frete passando: CEP de origem (configurável), CEP de destino, peso e dimensões dos itens.
5. O sistema exibe opções de envio: nome do serviço (PAC, SEDEX), preço em R$, prazo em dias úteis.
6. O visitante seleciona uma opção.
7. O sistema atualiza o total (subtotal + frete).

**Fluxo Alternativo:**
- 3a. CEP inválido: "CEP inválido. Informe 8 dígitos (ex: 01001-000)."
- 4a. API indisponível: "Não foi possível calcular o frete. Tente novamente." Botão "Finalizar Compra" desabilitado.
- 5a. CEP não atendido: "Infelizmente não entregamos neste CEP no momento."

**Regras de Negócio:**
- RN-15: CEP de origem configurável no painel admin ou application.properties.
- RN-16: Peso e dimensões são atributos do produto. Se não cadastrados, usar valores padrão (300g, 20×15×5 cm).
- RN-17: Frete calculado tem validade de 30 minutos. Após isso, recalcular.
- RN-18: Sem frete grátis no MVP (decisão D5).

---

### UC-08: Realizar Checkout

**Ator:** Visitante → Cliente  
**Pré-condição:** Carrinho com itens e frete calculado

**Fluxo Principal:**
1. O visitante clica em "Finalizar Compra".
2. O sistema exibe o formulário de checkout:
   - **Dados pessoais:** nome completo, e-mail, telefone (com DDD).
   - **Endereço de entrega:** CEP (pré-preenchido), rua, número, complemento (opcional), bairro, cidade, estado (UF).
   - **Resumo do pedido:** itens, subtotal, frete, total.
3. O visitante preenche os campos obrigatórios.
4. O sistema preenche automaticamente rua, bairro, cidade e estado a partir do CEP (via ViaCEP).
5. O visitante confere o resumo e clica em "Ir para Pagamento".
6. O sistema valida todos os campos obrigatórios.
7. O sistema revalida estoque e preços de todos os itens (backend, não confia no localStorage).
8. O sistema cria um registro de pedido no banco com status "AGUARDANDO_PAGAMENTO".
9. O sistema cria uma Checkout Session no Stripe com os dados do pedido.
10. O sistema redireciona o visitante para a página de pagamento do Stripe.

**Fluxo Alternativo:**
- 6a. Campos obrigatórios não preenchidos: destaca campos com erro.
- 7a. Estoque insuficiente: "O produto [nome] não possui mais estoque suficiente." Retorna ao carrinho.
- 9a. Falha na comunicação com Stripe: "Erro ao processar pagamento. Tente novamente."

**Regras de Negócio:**
- RN-19: Nenhum dado de cartão trafega pelo nosso servidor. Stripe Checkout hospedado.
- RN-20: Pedido tem validade de 30 minutos para pagamento. Após isso, status "EXPIRADO" e estoque liberado.
- RN-21: E-mail é obrigatório (chave natural para futura conta de cliente — decisão D6).
- RN-22: Validação de telefone: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX.

---

### UC-09: Processar Pagamento (Stripe)

**Ator:** Cliente, Stripe  
**Pré-condição:** Cliente redirecionado para Stripe Checkout

**Fluxo Principal:**
1. O cliente está na página de checkout do Stripe.
2. O cliente escolhe método de pagamento (cartão, Pix).
3. O cliente preenche dados e confirma.
4. O Stripe processa o pagamento.
5. O Stripe envia webhook `checkout.session.completed` para nosso servidor.
6. Nosso sistema recebe o webhook e:
   - Atualiza status do pedido para "PAGO".
   - Debita estoque dos produtos comprados.
   - Dispara e-mail de "Pagamento Confirmado" via Resend (UC-E1).
7. O Stripe redireciona o cliente para nossa página de confirmação (success URL).

**Fluxo Alternativo:**
- 4a. Pagamento recusado: Stripe exibe erro. Cliente tenta novamente.
- 4b. Cliente desiste: Stripe redireciona para cancel URL. Pedido permanece "AGUARDANDO_PAGAMENTO" até expirar.
- 5a. Webhook não recebido: Stripe reenvia automaticamente. Endpoint idempotente.
- 6a. Estoque acabou entre checkout e pagamento: cancelar pedido, solicitar reembolso via Stripe API.

**Regras de Negócio:**
- RN-23: Webhook verificado com assinatura (webhook secret).
- RN-24: Estoque só debita APÓS confirmação de pagamento via webhook.
- RN-25: Endpoint de webhook é idempotente (mesmo evento processado uma única vez).

---

### UC-10: Visualizar Confirmação de Pedido

**Ator:** Cliente  
**Pré-condição:** Pagamento confirmado com sucesso

**Fluxo Principal:**
1. Stripe redireciona o cliente para `/pedido/confirmado?session_id=xxx`.
2. O sistema valida a session_id e localiza o pedido.
3. O sistema exibe: número do pedido, status "Pagamento Confirmado", itens comprados, endereço de entrega, frete, total pago, mensagem "Obrigado pela sua compra! Você receberá atualizações por e-mail."
4. Link "Continuar Comprando" → catálogo.
5. O carrinho no localStorage é limpo automaticamente.

**Fluxo Alternativo:**
- 2a. Session_id inválida: "Não encontramos seu pedido. Entre em contato conosco."
- 2b. Pedido com status "AGUARDANDO_PAGAMENTO" (webhook ainda não chegou): "Seu pagamento está sendo processado. Atualize em alguns instantes."

---

### UC-11: Visualizar Página de Cancelamento

**Ator:** Cliente  
**Pré-condição:** Cliente desistiu do pagamento no Stripe

**Fluxo Principal:**
1. Stripe redireciona para `/pedido/cancelado`.
2. O sistema exibe: "Seu pagamento não foi concluído. Seus itens ainda estão no carrinho."
3. Botões: "Voltar ao Carrinho" e "Continuar Comprando".

**Pós-condição:** Carrinho intacto no localStorage. Pedido expira após 30 minutos.

---

## Jornada 3 — E-mails Transacionais

### UC-E1: Enviar E-mail de Pagamento Confirmado

**Ator:** Sistema (automático), Resend  
**Gatilho:** Webhook do Stripe confirma pagamento (UC-09, passo 6)

**Fluxo Principal:**
1. O sistema monta o e-mail com template HTML contendo: logo da loja, número do pedido, lista de itens com quantidades e preços, valor total pago, endereço de entrega, mensagem "Estamos preparando seu pedido!".
2. O sistema envia o e-mail via API do Resend para o e-mail do cliente.
3. O sistema registra log de envio (sucesso/falha).

**Fluxo Alternativo:**
- 2a. Falha no envio: registrar no log. Retry automático 1x após 5 minutos. Se falhar novamente, registrar como "e-mail não entregue" no pedido. Não bloqueia o fluxo de compra.

**Regras de Negócio:**
- RN-26: Falha no e-mail NUNCA impede a conclusão da compra. É fire-and-forget com retry.
- RN-27: E-mail contém link para contato da loja em caso de dúvidas.

---

### UC-E2: Enviar E-mail de Pedido Enviado

**Ator:** Sistema (automático), Resend  
**Gatilho:** Admin atualiza status do pedido para "ENVIADO" (UC-16c)

**Fluxo Principal:**
1. O sistema monta o e-mail com: número do pedido, código de rastreio (se disponível), link para rastreio (se disponível), endereço de entrega, mensagem "Seu pedido foi enviado!".
2. Envia via Resend.
3. Registra log.

**Fluxo Alternativo:**
- 1a. Código de rastreio não informado: e-mail enviado sem rastreio, com mensagem "Em breve enviaremos o código de rastreio."

---

### UC-E3: Enviar E-mail de Pedido Cancelado

**Ator:** Sistema (automático), Resend  
**Gatilho:** Admin cancela pedido pago (UC-16c) ou sistema expira pedido

**Fluxo Principal:**
1. O sistema monta o e-mail com: número do pedido, motivo do cancelamento (se informado pelo admin), informação sobre reembolso (se aplicável), contato da loja.
2. Envia via Resend.
3. Registra log.

**Regras de Negócio:**
- RN-28: E-mail de cancelamento só é enviado se o pedido já estava "PAGO". Pedidos expirados de "AGUARDANDO_PAGAMENTO" não geram e-mail (o cliente desistiu voluntariamente).

---

## Jornada 4 — Administração

### UC-12: Login do Administrador

**Ator:** Administrador  
**Pré-condição:** Credenciais cadastradas (seed)

**Fluxo Principal:**
1. O administrador acessa `/admin/login`.
2. Formulário: e-mail + senha.
3. Preenche e clica em "Entrar".
4. Sistema valida (BCrypt).
5. Redireciona para `/admin/dashboard`.

**Fluxo Alternativo:**
- 4a. Credenciais inválidas: "E-mail ou senha incorretos." (sem especificar qual).
- 4b. 5 tentativas falhas em 15 minutos: bloqueio de 30 minutos.

**Regras de Negócio:**
- RN-29: Senhas com BCrypt (custo mínimo 12).
- RN-30: Sessão expira após 2 horas de inatividade.
- RN-31: `/admin/*` protegido por Spring Security. Acesso não autenticado redireciona para login.
- RN-32: 1 usuário admin no MVP (seed). Sem tela de cadastro de admins.

---

### UC-13: Visualizar Dashboard do Admin

**Ator:** Administrador  
**Pré-condição:** Autenticado

**Fluxo Principal:**
1. Após login, o sistema exibe o dashboard:
   - Total de produtos cadastrados.
   - Total de produtos esgotados (estoque = 0).
   - Total de pedidos por status.
   - **Pedidos pendentes de envio** (status "PAGO" e "EM_PREPARACAO") — destaque visual.
   - Pedidos recentes (últimos 10).
2. Menu lateral: Dashboard, Produtos, Categorias, Pedidos, Configurações, Sair.

**Regras de Negócio:**
- RN-33: Pedidos pendentes de envio devem ter destaque visual (badge, cor, contagem) para que o admin nunca esqueça de enviar.

---

### UC-14: Gerenciar Produtos (CRUD)

**Ator:** Administrador  
**Pré-condição:** Autenticado

#### UC-14a: Listar Produtos

1. Admin acessa "Produtos" no menu.
2. Tabela com: imagem miniatura, nome, categoria, preço, estoque, status (ativo/inativo), ações (editar / desativar).
3. Busca por nome e filtro por categoria.

#### UC-14b: Cadastrar Novo Produto

1. Admin clica em "Novo Produto".
2. Formulário: nome (obrigatório, máx 150 chars), descrição (obrigatório, texto longo), preço (obrigatório, decimal > 0), categoria (obrigatório, seleção de lista), estoque (obrigatório, inteiro >= 0), peso em gramas (obrigatório, para frete), dimensões em cm: largura, altura, comprimento (obrigatório, para frete), imagens (upload 1-5, JPG/PNG/WebP, máx 5MB cada), status (ativo/inativo, padrão: ativo).
3. Admin preenche e faz upload.
4. Admin clica em "Salvar".
5. Sistema valida e salva. Imagens armazenadas no filesystem ou Cloudinary.
6. Redireciona para lista com mensagem de sucesso.

**Fluxo Alternativo:**
- 5a. Validação falha: erros nos campos. Dados preservados.
- 3a. Imagem inválida: rejeita apenas a imagem com erro.

#### UC-14c: Editar Produto

1. Admin clica em "Editar".
2. Formulário preenchido com dados atuais.
3. Admin altera campos desejados.
4. Admin pode remover/adicionar imagens (limite de 5).
5. Salva. Sistema valida e atualiza.

#### UC-14d: Desativar/Ativar Produto

1. Admin clica em "Desativar" (ou "Ativar").
2. Confirmação: "Deseja desativar [nome]? Não aparecerá mais no catálogo."
3. Admin confirma. Status alterado.

**Regras de Negócio:**
- RN-34: Não é possível excluir produto com pedidos associados. Usar "desativar".
- RN-35: Produto inativo não aparece no catálogo público, mas permanece nos pedidos históricos.

---

### UC-15: Gerenciar Categorias

**Ator:** Administrador  
**Pré-condição:** Autenticado

**Fluxo Principal:**
1. Admin acessa "Categorias" no menu.
2. Tabela com: nome da categoria, parent (se subcategoria), quantidade de produtos vinculados, status, ações.
3. Admin pode:
   - Criar nova categoria (nome + imagem representativa opcional + parent_id opcional).
   - Editar nome/imagem de categoria existente.
   - Desativar categoria.

**Regras de Negócio:**
- RN-36: Não é possível excluir categoria com produtos ativos vinculados.
- RN-37: Nome da categoria é único dentro do mesmo nível (pode ter "Espiral" em Cadernos e "Espiral" em Agendas, se um dia subcategorias forem ativadas na UI).
- RN-38: Tabela `categoria` possui campo `parent_id` (FK para si mesma, nullable). No MVP, o admin pode cadastrar subcategorias no banco, mas a navegação pública exibe apenas categorias de nível 1. Preparado para ativação futura.
- RN-39: Ao desativar uma categoria pai, todas as subcategorias são desativadas automaticamente.

---

### UC-16: Gerenciar Pedidos

**Ator:** Administrador  
**Pré-condição:** Autenticado

#### UC-16a: Listar Pedidos

1. Admin acessa "Pedidos" no menu.
2. Tabela: número, data, nome do cliente, total, status, ações.
3. Filtro por status. Busca por número ou nome do cliente.
4. Ordenação por data (mais recente primeiro).

#### UC-16b: Visualizar Detalhes do Pedido

1. Admin clica em um pedido.
2. Exibe: dados do cliente (nome, e-mail, telefone), endereço de entrega, itens (produto, qtd, preço unitário, subtotal), frete (método + custo), total, status atual com histórico de mudanças (timestamp de cada transição), log de e-mails enviados (sucesso/falha), botão "Atualizar Status".

#### UC-16c: Atualizar Status do Pedido

1. Admin clica em "Atualizar Status".
2. Sistema exibe próximos status possíveis (conforme fluxo abaixo).
3. Se novo status = "ENVIADO": campo para código de rastreio (opcional).
4. Admin seleciona, confirma.
5. Sistema registra mudança com timestamp.
6. Sistema dispara e-mail correspondente (UC-E2 ou UC-E3) se aplicável.

**Fluxo de Status do Pedido:**
```
AGUARDANDO_PAGAMENTO → PAGO                  (automático via webhook Stripe)
AGUARDANDO_PAGAMENTO → EXPIRADO              (automático após 30 min)
PAGO → EM_PREPARACAO                          (manual pelo admin)
EM_PREPARACAO → ENVIADO                       (manual pelo admin → dispara UC-E2)
ENVIADO → ENTREGUE                            (manual pelo admin)
PAGO → CANCELADO                              (manual pelo admin → reembolso Stripe → dispara UC-E3)
EM_PREPARACAO → CANCELADO                     (manual pelo admin → reembolso Stripe → dispara UC-E3)
```

**Regras de Negócio:**
- RN-40: Transições respeitam o fluxo acima. Não é possível pular etapas.
- RN-41: Ao cancelar pedido PAGO ou EM_PREPARACAO, o sistema solicita reembolso via Stripe API.
- RN-42: Ao cancelar, estoque é restaurado.
- RN-43: Cada mudança de status grava: status anterior, novo status, timestamp, admin que executou.

---

### UC-17: Configurações da Loja

**Ator:** Administrador  
**Pré-condição:** Autenticado

**Fluxo Principal:**
1. Admin acessa "Configurações".
2. Formulário contendo:
   - **Dados da loja:** nome da loja, CEP de origem (para frete).
   - **Contato:** e-mail, telefone, WhatsApp.
   - **Banner hero (home):** imagem, título, subtítulo, link de destino (opcional).
3. Admin altera os campos desejados e salva.

**Fluxo Alternativo:**
- 2a. Upload de imagem do banner em formato/tamanho inválido: sistema rejeita com mensagem específica (mesma validação de imagem de produto, RN-XX de upload).

**Regras de Negócio:**
- RN-44: Alterações entram em vigor imediatamente.
- RN-45: O link "Desenvolvido por JM Code Studio" no rodapé é fixo no código (constante/variável de ambiente), não editável pelo admin. É a assinatura do estúdio no projeto vitrine, não um dado de configuração da loja.
- RN-46: O banner hero é único no MVP (sem carrossel/múltiplos banners). Se nenhum banner for configurado, a home exibe uma imagem/texto padrão de fallback.
- RN-47: Imagem do banner: formatos JPG/PNG/WebP, máx 5MB, dimensão recomendada exibida na tela de upload (ex: 1600×600px) para evitar distorção.

---

## Requisitos Transversais

### Segurança
- RT-01: HTTPS obrigatório em todas as páginas.
- RT-02: Inputs sanitizados contra XSS e SQL Injection (Thymeleaf escapa por padrão; JPA usa parameterized queries).
- RT-03: CSRF token em todos os formulários (Spring Security).
- RT-04: Headers de segurança: CSP, X-Content-Type-Options, X-Frame-Options, HSTS.
- RT-05: Rate limiting no webhook e login do admin.
- RT-06: Logs de acesso ao painel admin.
- RT-07: Webhook do Stripe verificado com assinatura.
- RT-08: Nenhum dado de cartão no servidor (Stripe Checkout hospedado).

### Performance
- RT-09: Imagens em WebP com fallback JPG/PNG.
- RT-10: Lazy loading de imagens abaixo da dobra.
- RT-11: Cache HTTP para assets estáticos com cache-busting por hash.
- RT-12: Queries com paginação. Nunca SELECT * sem LIMIT.
- RT-13: Índices no banco: produto.nome, produto.categoria_id, pedido.status, pedido.data_criacao.

### Acessibilidade
- RT-14: Todas as imagens com alt text descritivo.
- RT-15: Navegação completa por teclado.
- RT-16: Contraste mínimo AA (4.5:1).
- RT-17: Formulários com labels associados e mensagens de erro acessíveis.

### Responsividade
- RT-18: Layout mobile-first. Grid: 1 coluna (mobile), 2 (tablet), 3-4 (desktop).
- RT-19: Menu hamburguer em mobile. Horizontal em desktop.
- RT-20: Imagens responsivas (srcset/sizes).

### Rodapé (todas as páginas públicas)
- RT-21: Rodapé contém "Desenvolvido por JM Code Studio" com link para jmcodestudio.com.br.
- RT-22: Rodapé contém informações de contato da loja (configuráveis via UC-17).

---

## Mapa de Páginas do MVP (Atualizado)

| # | Página | URL | Ator |
|---|--------|-----|------|
| 1 | Home | / | Visitante |
| 2 | Catálogo (todos/categoria) | /produtos, /produtos?cat=X | Visitante |
| 3 | Busca | /produtos?q=termo | Visitante |
| 4 | Detalhe do produto | /produto/{slug} | Visitante |
| 5 | Carrinho | /carrinho | Visitante |
| 6 | Checkout | /checkout | Visitante |
| 7 | Confirmação de pedido | /pedido/confirmado | Cliente |
| 8 | Cancelamento | /pedido/cancelado | Cliente |
| 9 | Login admin | /admin/login | Admin |
| 10 | Dashboard admin | /admin/dashboard | Admin |
| 11 | Lista de produtos (admin) | /admin/produtos | Admin |
| 12 | Form produto (admin) | /admin/produtos/novo, /admin/produtos/{id}/editar | Admin |
| 13 | Lista de pedidos (admin) | /admin/pedidos | Admin |
| 14 | Detalhe pedido (admin) | /admin/pedidos/{id} | Admin |
| 15 | Categorias (admin) | /admin/categorias | Admin |
| 16 | Configurações (admin) | /admin/configuracoes | Admin |
| 17 | Erro 404 | (qualquer URL inválida) | Todos |
| 18 | Erro 500 | (erro interno) | Todos |

**Total: 18 páginas** (8 públicas + 8 admin + 2 erro). Página "Sobre" eliminada (rodapé com link externo).

---

## Stack Técnica (Aprovada)

| Camada | Tecnologia | Justificativa |
|--------|-----------|---------------|
| Backend | Java 21 + Spring Boot 3 | Domínio do dev. API REST + MVC. |
| Frontend | Thymeleaf + HTMX | Server-side rendering com interações dinâmicas sem React. |
| Banco | PostgreSQL | Robusto, gratuito, bom para e-commerce. |
| Pagamento | Stripe Checkout (primário) / Mercado Pago (fallback) | Sem PCI-DSS. Checkout hospedado. |
| Frete | API Melhor Envio ou ViaCEP + Correios | Cálculo por CEP. |
| E-mail | Resend | Free tier generoso. API simples. SDK Java. |
| Hospedagem | Railway ou Render | Free tier ou custo mínimo. CI/CD integrado. |
| Imagens | Cloudinary ou filesystem local | Upload e otimização. |
| Versionamento | Git + GitHub | Trunk-based development. |

---

## Marcos Atualizados

| Marco | Descrição | Estimativa |
|-------|-----------|------------|
| M0 | Documento de visão + Casos de uso aprovados | ✅ Concluído |
| M1 | Wireframes das 18 páginas | Semana 2-3 |
| M2 | Setup do projeto (repo, CI/CD, DB, ambientes) | Semana 3-4 |
| M3 | Backend: modelo de dados + CRUD produtos/categorias + API | Semana 5-7 |
| M4 | Frontend público: catálogo + detalhe produto + busca | Semana 7-9 |
| M5 | Carrinho (localStorage + HTMX) + cálculo de frete | Semana 9-11 |
| M6 | Checkout + integração Stripe (sandbox) | Semana 11-13 |
| M7 | E-mails transacionais (Resend) | Semana 13-14 |
| M8 | Painel admin completo | Semana 14-17 |
| M9 | Testes, polimento, performance, responsividade | Semana 17-19 |
| M10 | Deploy em produção + dados reais | Semana 19-20 |

**Total estimado: ~5 meses** trabalhando meio período.

---

**Próximo passo:** Wireframes (M1).
