import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import '../styles/AuthPage.css';

interface IlliquidAssetDto {
    id?: number;
    name: string;
    description: string;
    estimatedValue: number;
}

/**
 * Page component for viewing, creating, and editing illiquid assets.
 * - When id is 'new': displays empty form for creating a new asset
 * - When id is a number: displays asset details with edit capability
 * - Edit mode allows modifying existing assets
 */
export function IlliquidAssetPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isNewAsset = id === 'new'; // Check if we're creating a new asset

    // State management
    const [loading, setLoading] = useState<boolean>(!isNewAsset); // Skip loading for new assets
    const [error, setError] = useState<string | null>(null);
    const [isEditing, setIsEditing] = useState<boolean>(isNewAsset); // New assets start in edit mode
    const [isSaving, setIsSaving] = useState<boolean>(false);
    const [isDeleting, setIsDeleting] = useState<boolean>(false);

    // Form data state
    const [formData, setFormData] = useState<IlliquidAssetDto>({
        name: '',
        description: '',
        estimatedValue: 0
    });

    /**
     * Fetches CSRF token from the server for secure requests.
     * @returns The CSRF token string or null if unavailable
     */
    const fetchCsrfToken = async (): Promise<string | null> => {
        try {
            const res = await fetch('http://localhost:8080/api/csrf', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Accept': 'application/json' },
            });
            if (!res.ok) return null;
            const data: any = await res.json();
            return data.token ?? data.csrfToken ?? data._csrf?.token ?? null;
        } catch {
            return null;
        }
    };

    /**
     * Effect to fetch asset data when viewing an existing asset.
     * Skipped when creating a new asset.
     */
    useEffect(() => {
        if (isNewAsset) {
            return; // Skip fetch for new assets
        }

        const fetchAsset = async () => {
            try {
                const response = await fetch(`http://localhost:8080/user/illiquid-asset/${id}`, {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                // Handle different response statuses
                if (!response.ok) {
                    if (response.status === 401) {
                        navigate('/login');
                        return;
                    }
                    if (response.status === 404) {
                        setError('Asset not found');
                        return;
                    }
                    setError('Failed to fetch asset');
                    return;
                }

                const data = await response.json();
                setFormData(data);
            } catch (err: any) {
                setError(err.message ?? 'An error occurred while fetching the asset.');
            } finally {
                setLoading(false);
            }
        };

        fetchAsset();
    }, [id, isNewAsset, navigate]);

    /**
     * Handles input field changes.
     * Parses numeric values for estimatedValue field.
     */
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'estimatedValue' ? parseFloat(value) || 0 : value
        }));
    };

    /**
     * Handles saving the asset (create or update).
     * Fetches CSRF token, sends POST/PUT request, and handles response.
     */
    const handleSave = async () => {
        setIsSaving(true);
        setError(null);

        try {
            // Determine URL and HTTP method based on whether we're creating or updating
            const url = isNewAsset
                ? 'http://localhost:8080/user/illiquid-asset'
                : `http://localhost:8080/user/illiquid-asset/${id}`;

            const method = isNewAsset ? 'POST' : 'PUT';

            // Fetch CSRF token for security
            const csrfToken = await fetchCsrfToken();

            // Prepare headers with CSRF token
            const headers: HeadersInit = {
                'Content-Type': 'application/json',
            };
            if (csrfToken) {
                headers['X-XSRF-TOKEN'] = csrfToken;
            }

            // Send request to backend
            const response = await fetch(url, {
                method,
                credentials: 'include',
                headers,
                body: JSON.stringify(formData),
            });

            // Handle authentication errors
            if (!response.ok) {
                if (response.status === 401) {
                    navigate('/login');
                    return;
                }
                setError('Failed to save asset');
                return;
            }

            const savedAsset = await response.json();

            // For new assets, navigate to the detail page. For updates, update form and exit edit mode
            if (isNewAsset) {
                navigate(`/user/illiquid-asset/${savedAsset.id}`);
            } else {
                setFormData(savedAsset);
                setIsEditing(false);
            }
        } catch (err: any) {
            setError(err.message ?? 'An error occurred while saving the asset.');
        } finally {
            setIsSaving(false);
        }
    };

    /**
     * Handles deleting the asset.
     * Asks for confirmation, fetches CSRF token, sends DELETE request, and navigates back to portfolio.
     */
    const handleDelete = async () => {
        // Confirm deletion
        if (!window.confirm('Are you sure you want to delete this asset? This action cannot be undone.')) {
            return;
        }

        setIsDeleting(true);
        setError(null);

        try {
            // Fetch CSRF token for security
            const csrfToken = await fetchCsrfToken();

            // Prepare headers with CSRF token
            const headers: HeadersInit = {
                'Content-Type': 'application/json',
            };
            if (csrfToken) {
                headers['X-XSRF-TOKEN'] = csrfToken;
            }

            // Send DELETE request to backend
            const response = await fetch(`http://localhost:8080/user/illiquid-asset/${id}`, {
                method: 'DELETE',
                credentials: 'include',
                headers,
            });

            // Handle authentication errors
            if (!response.ok) {
                if (response.status === 401) {
                    navigate('/login');
                    return;
                }
                if (response.status === 404) {
                    setError('Asset not found');
                    return;
                }
                setError('Failed to delete asset');
                return;
            }

            // Navigate back to portfolio after successful deletion
            navigate('/user/portfolio');
        } catch (err: any) {
            setError(err.message ?? 'An error occurred while deleting the asset.');
        } finally {
            setIsDeleting(false);
        }
    };

    if (loading) {
        return <div className="auth-container">Loading...</div>;
    }

    return (
        <div className="auth-container">
            <button
                onClick={() => navigate('/user/portfolio')}
                className="btn btn-primary"
                style={{
                    width: 'auto',
                    marginBottom: '1rem',
                    padding: '0.5rem 1rem',
                    fontSize: '0.875rem'
                }}
            >
                ← Back to Portfolio
            </button>

            <h1>{isNewAsset ? 'New Illiquid Asset' : formData.name || 'Asset Details'}</h1>

            {error && (
                <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
                    {error}
                </div>
            )}

            <div style={{
                border: '1px solid #ccc',
                padding: '1.5rem',
                borderRadius: '8px',
                background: '#fff',
                color: '#1f2937'
            }}>
                {!isNewAsset && formData.id && (
                    <div className="form-group">
                        <label>ID</label>
                        <input
                            type="text"
                            value={formData.id}
                            disabled
                            style={{ background: '#e5e7eb', cursor: 'not-allowed' }}
                        />
                    </div>
                )}

                <div className="form-group">
                    <label htmlFor="name">Name</label>
                    <input
                        id="name"
                        name="name"
                        type="text"
                        value={formData.name}
                        onChange={handleInputChange}
                        disabled={!isEditing}
                        placeholder="Enter asset name"
                        style={!isEditing ? { background: '#e5e7eb', cursor: 'not-allowed' } : {}}
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="description">Description</label>
                    <textarea
                        id="description"
                        name="description"
                        value={formData.description}
                        onChange={handleInputChange}
                        disabled={!isEditing}
                        placeholder="Enter asset description"
                        rows={4}
                        style={{
                            width: '100%',
                            padding: '0.875rem',
                            border: '2px solid var(--border-color)',
                            borderRadius: '10px',
                            fontSize: '1rem',
                            background: !isEditing ? '#e5e7eb' : 'var(--bg-secondary)',
                            color: 'var(--text-primary)',
                            cursor: !isEditing ? 'not-allowed' : 'text',
                            resize: 'vertical',
                            fontFamily: 'inherit'
                        }}
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="estimatedValue">Estimated Value (€)</label>
                    <input
                        id="estimatedValue"
                        name="estimatedValue"
                        type="number"
                        step="0.01"
                        value={formData.estimatedValue}
                        onChange={handleInputChange}
                        disabled={!isEditing}
                        placeholder="0.00"
                        style={!isEditing ? { background: '#e5e7eb', cursor: 'not-allowed' } : {}}
                    />
                </div>

                <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
                    {!isEditing && !isNewAsset && (
                        <>
                            <button
                                onClick={() => setIsEditing(true)}
                                className="btn btn-primary"
                            >
                                Modify
                            </button>

                            <button
                                onClick={handleDelete}
                                disabled={isDeleting}
                                className="btn"
                                style={{
                                    background: '#ef4444',
                                    color: 'white'
                                }}
                            >
                                {isDeleting ? 'Deleting...' : 'Delete'}
                            </button>
                        </>
                    )}

                    {isEditing && (
                        <>
                            <button
                                onClick={handleSave}
                                disabled={isSaving}
                                className="btn btn-primary"
                            >
                                {isSaving ? 'Saving...' : 'Save'}
                            </button>

                            {!isNewAsset && (
                                <button
                                    onClick={() => {
                                        setIsEditing(false);
                                        setError(null);
                                        // Reset form to original values by refetching
                                        window.location.reload();
                                    }}
                                    disabled={isSaving}
                                    className="btn"
                                    style={{ background: '#6b7280' }}
                                >
                                    Cancel
                                </button>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}

