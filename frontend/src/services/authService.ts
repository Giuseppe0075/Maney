import type { LoginRequest, RegisterRequest, User } from '../types/auth';

const BASE = 'http://localhost:8080';

type CsrfResponse = Record<string, unknown>;

function getStoredUser(): User | null {
    const raw = localStorage.getItem('user');
    return raw ? (JSON.parse(raw) as User) : null;
}

async function handleUnauthorized() {
    const user = getStoredUser();
    if (typeof window !== 'undefined') {
        if (user) {
            window.location.href = '/user/portfolio';
        } else {
            window.location.href = '/login';
        }
    }
}

async function fetchCsrfToken(): Promise<string | null> {
    try {
        const res = await fetch(`${BASE}/api/csrf`, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Accept': 'application/json' },
        });
        if (!res.ok) return null;
        const data: CsrfResponse = await res.json();
        const anyData = data as any;
        return anyData.token ?? anyData.csrfToken ?? anyData._csrf?.token ?? null;
    } catch {
        return null;
    }
}

export const authService = {
    login: async (credentials: LoginRequest): Promise<User> => {
        const csrf = await fetchCsrfToken();
        const headers: Record<string, string> = { 'Content-Type': 'application/json' };
        if (csrf) headers['X-CSRF-TOKEN'] = csrf;

        const response = await fetch(`${BASE}/user/login`, {
            method: 'POST',
            headers,
            credentials: 'include',
            body: JSON.stringify(credentials),
        });

        if (response.status === 401) {
            await handleUnauthorized();
            throw new Error('Non autorizzato');
        }

        if (!response.ok) throw new Error('Login non riuscito');

        const user: User = await response.json();
        localStorage.setItem('user', JSON.stringify(user));
        return user;
    },

    register: async (data: RegisterRequest): Promise<User> => {
        const csrf = await fetchCsrfToken();
        const headers: Record<string, string> = { 'Content-Type': 'application/json' };
        if (csrf) headers['X-CSRF-TOKEN'] = csrf;

        const response = await fetch(`${BASE}/user/register`, {
            method: 'POST',
            headers,
            credentials: 'include',
            body: JSON.stringify(data),
        });

        if (response.status === 401) {
            await handleUnauthorized();
            throw new Error('Non autorizzato');
        }

        if (!response.ok) throw new Error('Registrazione non riuscita');
        return response.json();
    },

    logout: async (): Promise<void> => {
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
            await handleUnauthorized();
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

    getCurrentUser: (): User | null => {
        return getStoredUser();
    },
};
