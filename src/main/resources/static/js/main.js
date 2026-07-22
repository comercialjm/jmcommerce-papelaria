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

    toast.textContent = mensagem;
    toast.classList.add('toast--visivel');

    clearTimeout(toast._timeoutId);
    toast._timeoutId = setTimeout(() => {
        toast.classList.remove('toast--visivel');
    }, 2500);
}

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

document.addEventListener('DOMContentLoaded', () => {
    atualizarContadorCarrinho();
    configurarMenuMobile();
});
