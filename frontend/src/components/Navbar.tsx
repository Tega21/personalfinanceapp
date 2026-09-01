import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
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
                <NavLink to="/dashboard" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Home</NavLink>
                <NavLink to="/transactions" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Transactions</NavLink>
                <NavLink to="/categories" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Categories</NavLink>
                <NavLink to="/budgets" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Budgets</NavLink>
                <NavLink to="/accounts" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Accounts</NavLink>
                <NavLink to="/recurring" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Recurring</NavLink>
                <NavLink to="/forecast" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Forecast</NavLink>

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
                            <NavLink
                                to="/profile"
                                className="navbar-dropdown-item"
                                onClick={() => setDropdownOpen(false)}
                            >
                                View Profile
                            </NavLink>
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