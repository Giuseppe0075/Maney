// typescript
import { useState, useEffect } from 'react';
import { authService } from '../services/authService';
import '../styles/AuthPage.css';

interface Portfolio {
    id: number;
}

export function PortfolioPage() {
    const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchPortfolio = async () => {
            try {
                const data = await authService.getPortfolio();
                setPortfolio(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchPortfolio();
    }, []);

    if (loading) return <div className="auth-container">Caricamento...</div>;
    if (error) return <div className="auth-container error-message">{error}</div>;

    return (
        <div className="auth-container">
            <h1>Il Tuo Portfolio</h1>
            {portfolio ? (
                <div>
                    <p>ID del Portfolio: {portfolio.id}</p>
                </div>
            ) : (
                <p>Nessun portfolio trovato.</p>
            )}
        </div>
    );
}
