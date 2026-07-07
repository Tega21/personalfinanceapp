import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

/**
 * Persistent navigation bar shown on all authenticated pages
 * (rendered by ProtectedRoute). Provides navigation to Dashboard,
 * Transactions, and Categories, displays the username, and
 * handles logout.
 */
const Navbar = () => {
    const { username, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar">
            <div className="navbar-brand">Personal Finance App</div>
            <div className="navbar-links">
                <Link to="/dashboard">Home</Link>
                <Link to="/transactions">Transactions</Link>
                <Link to="/categories">Categories</Link>
                <Link to="/budgets">Budgets</Link>
                <span className="navbar-username">{username}</span>
                <button onClick={handleLogout} className="navbar-logout">
                    Logout
                </button>
            </div>
        </nav>
    );
};

export default Navbar;