import type {LoginRequest} from '../types/auth';

const API_URL = '/api';

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

    logout: async () => {
        await fetch(`${API_URL}/users/logout`, {
            method: 'POST',
            credentials: 'include',
        });
    },
};
