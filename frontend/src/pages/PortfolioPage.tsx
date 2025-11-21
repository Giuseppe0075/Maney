// typescript
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import '../styles/AuthPage.css';

interface IlliquidAssetDto {
    id: number;
    name: string;
    description: string;
    estimatedValue: number;
}

interface Portfolio {
    id: number;
    illiquidAssets: IlliquidAssetDto[];
}

/**
 * Portfolio page component that displays user's portfolio and illiquid assets.
 * Allows viewing all assets and navigating to individual asset details or creating new ones.
 */
export function PortfolioPage() {
    const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    /**
     * Fetches the user's portfolio data on component mount.
     */
    useEffect(() => {
        const fetchPortfolio = async () => {
            try {
                const data = await authService.getPortfolio();
                setPortfolio(data);
            } catch (err: any) {
                setError(err.message ?? 'An error occurred while fetching the portfolio.');
            } finally {
                setLoading(false);
            }
        };
        fetchPortfolio();
    }, []);

    if (loading) {
        return <div className="auth-container">Loading...</div>;
    }

    if (error) {
        return <div className="auth-container error-message">{error}</div>;
    }

    if (!portfolio) {
        return <div className="auth-container">No portfolio found.</div>;
    }

    return (
        <div className="auth-container">
            <h1>Your Portfolio</h1>

                <div>
                    <p>Portfolio ID: {portfolio.id}</p>
                </div>

            <hr/>

            <section>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    <h2 style={{ margin: 0 }}>Illiquid Assets</h2>
                    <button
                        onClick={() => navigate('/user/illiquid-asset/new')}
                        className="btn btn-primary"
                        style={{
                            width: 'auto',
                            padding: '0.5rem 1.5rem',
                            fontSize: '0.875rem'
                        }}
                    >
                        + Add New Asset
                    </button>
                </div>
                {portfolio.illiquidAssets && portfolio.illiquidAssets.length > 0 && (
                    <div>
                        <ul style={{ listStyle: 'none', padding: 0 }}>
                                {portfolio.illiquidAssets.map(asset => (
                                    <li key={asset.id} style={{marginBottom: '1rem'}}>
                                        <div
                                            role="button"
                                            tabIndex={0}
                                            onClick={() => navigate(`/user/illiquid-asset/${asset.id}`)}
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    navigate(`/user/illiquid-asset/${asset.id}`);
                                                }
                                            }}
                                            style={{
                                                cursor: 'pointer',
                                                border: '1px solid #ccc',
                                                padding: '0.75rem',
                                                borderRadius: '8px',
                                                background: '#fff',
                                                color: '#1f2937',
                                                transition: 'box-shadow 0.2s'
                                            }}
                                            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)'}
                                            onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
                                            aria-label={`View details for ${asset.name}`}
                                        >
                                            <div style={{ marginBottom: '0.5rem' }}><strong>Name:</strong> {asset.name}</div>
                                            <div style={{ marginBottom: '0.5rem' }}><strong>Description:</strong> {asset.description}</div>
                                            <div><strong>Estimated Value:</strong> €{asset.estimatedValue.toLocaleString()}</div>
                                        </div>
                                    </li>
                            ))}
                        </ul>
                    </div>
                )}
                {(!portfolio.illiquidAssets || portfolio.illiquidAssets.length === 0) && (
                    <p style={{ textAlign: 'center', color: 'var(--text-secondary)', marginTop: '2rem' }}>
                        No illiquid assets found. Click "Add New Asset" to create one.
                    </p>
                )}
            </section>
                    </div>
                );
}
