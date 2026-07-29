// Base do frontend público — carrinho (RN-09: localStorage), contador no header,
// toast de feedback e menu mobile. Compartilhado por todas as páginas via layout.

const CHAVE_CARRINHO = 'carrinho';

/**
 * Formato de cada item salvo: { produtoId, quantidade, precoSnapshot }.
 * RN-10: o snapshot de preço é só um registro histórico do momento da adição —
 * a página do carrinho NUNCA confia nele para exibir valores (RN-14). Quem
 * decide o preço e o estoque de verdade é sempre o backend.
 */

function lerCarrinho() {
    try {
        const bruto = localStorage.getItem(CHAVE_CARRINHO);
        return bruto ? JSON.parse(bruto) : [];
    } catch {
        return [];
    }
}

function salvarCarrinho(itens) {
    localStorage.setItem(CHAVE_CARRINHO, JSON.stringify(itens));
    atualizarContadorCarrinho();
}

/** UC-05: adiciona um produto ao carrinho (soma quantidade se já existir — 3a). */
function adicionarAoCarrinho(produtoId, quantidade, precoSnapshot) {
    const itens = lerCarrinho();
    const existente = itens.find(i => i.produtoId === produtoId);

    if (existente) {
        existente.quantidade += quantidade;
    } else {
        itens.push({ produtoId, quantidade, precoSnapshot });
    }

    salvarCarrinho(itens);
}

function atualizarContadorCarrinho() {
    const itens = lerCarrinho();
    const totalItens = itens.reduce((soma, item) => soma + (item.quantidade || 0), 0);
    const badge = document.getElementById('contador-carrinho');
    if (!badge) return;

    if (totalItens > 0) {
        badge.textContent = totalItens;
        badge.hidden = false;
    } else {
        badge.hidden = true;
    }
}

/** UC-05, passo 4: feedback visual de "produto adicionado". */
function mostrarToast(mensagem) {
    let toast = document.getElementById('toast-global');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast-global';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }

    toast.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
            <path d="M20 6L9 17l-5-5"/>
        </svg>
        <span>${mensagem}</span>
    `;
    toast.classList.add('toast--visivel');

    clearTimeout(toast._timeoutId);
    toast._timeoutId = setTimeout(() => {
        toast.classList.remove('toast--visivel');
    }, 3000);
}

function abrirGuiaVitrine() {
    document.getElementById('modal-guia-vitrine').hidden = false;
    document.body.style.overflow = 'hidden';
}

function fecharGuiaVitrine() {
    document.getElementById('modal-guia-vitrine').hidden = true;
    document.body.style.overflow = '';
}

document.addEventListener('keydown', (evento) => {
    if (evento.key === 'Escape') fecharGuiaVitrine();
});

function configurarMenuMobile() {
    const botao = document.querySelector('.cabecalho__menu-mobile');
    const nav = document.querySelector('.cabecalho__nav');
    if (!botao || !nav) return;

    botao.addEventListener('click', () => {
        const aberto = botao.getAttribute('aria-expanded') === 'true';
        botao.setAttribute('aria-expanded', String(!aberto));
        nav.classList.toggle('cabecalho__nav--aberto');
    });
}

// Preenche o menu com as categorias reais, em vez de deixar só "Produtos" sozinho.
// Se houver muitas, agrupa o excedente num "Mais ▾" para não estourar o header.
const MAX_CATEGORIAS_VISIVEIS = 4;

async function popularNavCategorias() {
    const nav = document.getElementById('nav-principal');
    if (!nav) return;

    try {
        const resposta = await fetch('/api/categorias');
        if (!resposta.ok) return;
        const categorias = await resposta.json();

        const visiveis = categorias.slice(0, MAX_CATEGORIAS_VISIVEIS);
        const restantes = categorias.slice(MAX_CATEGORIAS_VISIVEIS);

        visiveis.forEach(cat => {
            const link = document.createElement('a');
            link.href = `/produtos?categoriaId=${cat.id}`;
            link.textContent = cat.nome;
            nav.appendChild(link);
        });

        if (restantes.length > 0) {
            nav.appendChild(criarMenuMais(restantes));
        }
    } catch {
        // Sem categorias no menu não deve quebrar a navegação básica.
    }
}

function criarMenuMais(categorias) {
    const container = document.createElement('div');
    container.style.cssText = 'position:relative; display:inline-block;';

    const botao = document.createElement('button');
    botao.type = 'button';
    botao.textContent = 'Mais ▾';
    botao.style.cssText = 'background:none; border:none; font:inherit; color:inherit; cursor:pointer; padding:0;';

    const dropdown = document.createElement('div');
    dropdown.style.cssText = `
        display:none; position:absolute; top:100%; left:50%; transform:translateX(-50%);
        margin-top:12px; background:var(--cor-papel); border:1px solid var(--cor-borda);
        border-radius:var(--raio-card); box-shadow:0 8px 24px rgba(43,38,32,0.12);
        padding:8px 0; min-width:180px; z-index:20; text-align:left;
    `;
    dropdown.innerHTML = categorias.map(cat =>
        `<a href="/produtos?categoriaId=${cat.id}" style="display:block; padding:8px 16px; white-space:nowrap;">${cat.nome}</a>`
    ).join('');

    botao.addEventListener('click', () => {
        dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
    });
    document.addEventListener('click', (e) => {
        if (!container.contains(e.target)) dropdown.style.display = 'none';
    });

    container.appendChild(botao);
    container.appendChild(dropdown);
    return container;
}

document.addEventListener('DOMContentLoaded', () => {
    atualizarContadorCarrinho();
    configurarMenuMobile();
    popularNavCategorias();
});
