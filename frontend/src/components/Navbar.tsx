import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

/**
 * Persistent purple navigation bar shown on all authenticated pages.
 * Provides navigation to Home, Transactions, Categories, and Budgets,
 * and a dropdown from the username showing profile access and logout.
 */
const Navbar = () => {
    const { username, logout } = useAuth();
    const navigate = useNavigate();
    const [dropdownOpen, setDropdownOpen] = useState(false);

    /**
     * Clears the authenticated session and redirects to the login page.
     */
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

                <div className="navbar-user-menu">
                    <button
                        className="navbar-avatar-btn"
                        onClick={() => setDropdownOpen((prev) => !prev)}
                    >
                        {username?.charAt(0).toUpperCase()}
                    </button>

                    {dropdownOpen && (
                        <div className="navbar-dropdown">
                            <div className="navbar-dropdown-header">
                                <p className="dropdown-username">{username}</p>
                            </div>
                            <Link
                                to="/profile"
                                className="navbar-dropdown-item"
                                onClick={() => setDropdownOpen(false)}
                            >
                                View Profile
                            </Link>
                            <button
                                className="navbar-dropdown-item navbar-dropdown-logout"
                                onClick={handleLogout}
                            >
                                Logout
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;