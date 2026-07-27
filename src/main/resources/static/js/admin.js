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
