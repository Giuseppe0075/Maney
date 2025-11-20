// typescript
import { useState, useEffect } from 'react';
import { authService } from '../services/authService';
import '../styles/AuthPage.css';

interface IlliquidAssetDto {
    id: number;
    name: string;
    description: string;
    estimatedValue: number; // oppure string, in base al JSON che ritorna il backend
}

interface Portfolio {
    id: number;
    illiquidAssets: IlliquidAssetDto[];
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
                setError(err.message ?? 'Errore nel caricamento del portfolio');
            } finally {
                setLoading(false);
            }
        };
        fetchPortfolio();
    }, []);

    if (loading) {
        return <div className="auth-container">Caricamento...</div>;
    }

    if (error) {
        return <div className="auth-container error-message">{error}</div>;
    }

    if (!portfolio) {
        return <div className="auth-container">Nessun portfolio trovato.</div>;
    }

    return (
        <div className="auth-container">
            <h1>Il Tuo Portfolio</h1>

            <div>
                <p>\*ID del Portfolio:\* {portfolio.id}</p>
            </div>

            <hr />

            <section>
                <h2>Asset Illiquidi</h2>
                {(!portfolio.illiquidAssets || portfolio.illiquidAssets.length === 0) && (
                    <p>Nessun asset illiquido presente.</p>
                )}

                {portfolio.illiquidAssets && portfolio.illiquidAssets.length > 0 && (
                    <ul>
                        {portfolio.illiquidAssets.map(asset => (
                            <li key={asset.id} style={{ marginBottom: '1rem' }}>
                                <div>\*Nome:\* {asset.name}</div>
                                <div>\*Descrizione:\* {asset.description}</div>
                                <div>
                                    \*Valore stimato:\* {asset.estimatedValue}
                                    {/* qui puoi formattare come valuta, es. Intl.NumberFormat */}
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </section>
        </div>
    );
}
