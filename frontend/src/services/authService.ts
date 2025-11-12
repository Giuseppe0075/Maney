import type {LoginRequest} from '../types/auth';

const API_URL = 'http://localhost:8080/api';

export const authService = {
    login: async (credentials: LoginRequest) => {
        const response = await fetch(`${API_URL}/users/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(credentials),
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        return response.json();
    },

    register: async (data: { username: string; email: string; password: string }) => {
        const response = await fetch(`${API_URL}/users/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            throw new Error('Registration failed');
        }

        return response.json();
    },

    logout: async () => {
        await fetch(`${API_URL}/users/logout`, {
            method: 'POST',
            credentials: 'include',
        });
    },
};
