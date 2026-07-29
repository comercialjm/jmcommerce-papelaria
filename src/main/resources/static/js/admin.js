// Compartilhado por todas as páginas do admin. O token vem de meta tags que o
// Spring Security/Thymeleaf injeta automaticamente (ver layout/admin.html) —
// necessário porque reativamos CSRF para /admin/** (RT-03).

function csrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return (token && header) ? { [header]: token } : {};
}

/** Atalho: fetch() com o header CSRF já embutido, para chamadas que alteram dados. */
function fetchAdmin(url, options = {}) {
    return fetch(url, {
        ...options,
        headers: { ...(options.headers || {}), ...csrfHeaders() }
    });
}

// Menu hambúrguer do admin em telas estreitas (mesmo padrão do site público).
function configurarMenuMobileAdmin() {
    const botao = document.querySelector('.cabecalho__menu-mobile');
    const nav = document.querySelector('.cabecalho__nav');
    if (!botao || !nav) return;

    botao.addEventListener('click', () => {
        const aberto = botao.getAttribute('aria-expanded') === 'true';
        botao.setAttribute('aria-expanded', String(!aberto));
        nav.classList.toggle('cabecalho__nav--aberto');
    });
}

document.addEventListener('DOMContentLoaded', configurarMenuMobileAdmin);
