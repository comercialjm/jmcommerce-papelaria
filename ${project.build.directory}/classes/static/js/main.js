// Base do frontend público — contador do carrinho (RN-09: carrinho em localStorage)
// e alternância do menu mobile. A lógica completa do carrinho (adicionar, remover,
// validar com o backend) chega no M5 — aqui só garantimos que o ícone no header
// já reflete o que estiver salvo no navegador.

const CHAVE_CARRINHO = 'carrinho';

function lerCarrinho() {
    try {
        const bruto = localStorage.getItem(CHAVE_CARRINHO);
        return bruto ? JSON.parse(bruto) : [];
    } catch {
        return [];
    }
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
