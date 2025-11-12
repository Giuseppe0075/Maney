import { useTheme } from '../context/ThemeContext';
import './Header.css';

export function Header() {
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="header">
      <div className="header-content">
        <h1 className="logo">Maney</h1>
        <button
          className="theme-toggle"
          onClick={toggleTheme}
          aria-label="Toggle theme"
        >
          {theme === 'light' ? '🌙' : '☀️'}
        </button>
      </div>
    </header>
  );
}

