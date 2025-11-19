// typescript
const BASE = 'http://localhost:8080';

type CsrfResponse = Record<string, any>;

async function fetchCsrfToken(): Promise<string | null> {
    try {
        const res = await fetch(`${BASE}/api/csrf`, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Accept': 'application/json' },
        });
        if (!res.ok) return null;
        const data: CsrfResponse = await res.json();
        // Provo diversi possibili nomi di proprietà
        return data.token ?? data.csrfToken ?? data._csrf?.token ?? null;
    } catch {
        return null;
    }
}

export const authService = {
    login: async (credentials: { email: string; password: string }) => {
        const csrf = await fetchCsrfToken();
        const headers: Record<string, string> = { 'Content-Type': 'application/json' };
        if (csrf) headers['X-CSRF-TOKEN'] = csrf;

        const response = await fetch(`${BASE}/user/login`, {
            method: 'POST',
            headers,
            credentials: 'include',
            body: JSON.stringify(credentials),
        });

        if (!response.ok) throw new Error('Login failed');

        const user = await response.json();
        localStorage.setItem('user', JSON.stringify(user));
        return user;
    },

    register: async (data: { username: string; email: string; password: string }) => {
        const csrf = await fetchCsrfToken();
        const headers: Record<string, string> = { 'Content-Type': 'application/json' };
        if (csrf) headers['X-CSRF-TOKEN'] = csrf;

        const response = await fetch(`${BASE}/user/register`, {
            method: 'POST',
            headers,
            credentials: 'include',
            body: JSON.stringify(data),
        });

        if (!response.ok) throw new Error('Registration failed');
        return response.json();
    },

    logout: async () => {
        const csrf = await fetchCsrfToken();
        const headers: Record<string, string> = {};
        if (csrf) headers['X-CSRF-TOKEN'] = csrf;

        await fetch(`${BASE}/user/logout`, {
            method: 'POST',
            credentials: 'include',
            headers,
        });

        localStorage.removeItem('user');
    },

    getPortfolio: async () => {
        const response = await fetch(`${BASE}/user/portfolio`, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Accept': 'application/json' },
        });

        if (response.status === 401 || response.status === 403) {
            throw new Error('Non autenticato');
        }
        if (response.status === 404) {
            throw new Error('Portfolio non trovato');
        }
        if (!response.ok) {
            throw new Error(`Errore nel recupero del portfolio: ${response.status}`);
        }

        return response.json();
    },

    getCurrentUser: () => {
        const raw = localStorage.getItem('user');
        return raw ? JSON.parse(raw) : null;
    },
};
