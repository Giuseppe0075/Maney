import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import type {LoginRequest} from '../types/auth';
import '../styles/LoginPage.css';

export function LoginPage() {
    const navigate = useNavigate();
    const [credentials, setCredentials] = useState<LoginRequest>({
        username: '',
        password: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setCredentials(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await authService.login(credentials);
            navigate('/dashboard');
        } catch (err) {
            setError('Invalid username or password');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <form onSubmit={handleSubmit} className="login-form">
                <h1>Maney - Login</h1>

                <div className="form-group">
                    <label htmlFor="username">Username</label>
                    <input
                        id="username"
                        type="text"
                        name="username"
                        value={credentials.username}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="password">Password</label>
                    <input
                        id="password"
                        type="password"
                        name="password"
                        value={credentials.password}
                        onChange={handleChange}
                        required
                    />
                </div>

                {error && <div className="error">{error}</div>}

                <button type="submit" disabled={loading}>
                    {loading ? 'Logging in...' : 'Login'}
                </button>

                <p className="register-link">
                    Non hai un account? <a href="/register">Registrati qui</a>
                </p>
            </form>
        </div>
    );
}
