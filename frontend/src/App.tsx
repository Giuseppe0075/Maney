import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import { Header } from './components/Header';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { PortfolioPage } from './pages/PortfolioPage';
import { HomePage } from './pages/HomePage';
import './App.css';

function App() {
    return (
        <ThemeProvider>
            <BrowserRouter>
                <Header />
                <Routes>
                    <Route path="/homepage" element={<HomePage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/user/portfolio" element={<PortfolioPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    {/* Landing del sito */}
                    <Route path="/" element={<Navigate to="/homepage" replace />} />
                    {/* eventuale fallback 404 in futuro */}
                </Routes>
            </BrowserRouter>
        </ThemeProvider>
    );
}

export default App;
