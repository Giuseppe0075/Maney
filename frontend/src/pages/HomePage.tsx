import '../styles/AuthPage.css';

export function HomePage() {
  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-form">
          <div className="form-header">
            <h1>Benvenuto su Maney</h1>
            <p>Gestisci il tuo portfolio in modo semplice e intuitivo.</p>
          </div>
          <p style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>
            Questa è la homepage pubblica. Puoi accedere o registrarti per vedere il tuo portfolio.
          </p>
          <div className="form-footer">
            <p>
              Vai al login per continuare.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

