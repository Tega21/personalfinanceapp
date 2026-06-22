import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

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
                <Link to="/dashboard">Dashboard</Link>
                <Link to="/transactions">Transactions</Link>
                <span className="navbar-username">{username}</span>
                <button onClick={handleLogout} className="navbar-logout">
                    Logout
                </button>
            </div>
        </nav>
    );
};

export default Navbar;