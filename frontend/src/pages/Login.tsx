import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

/**
 * Login page. Authenticates the user against the backend and, on
 * success, stores the JWT via AuthContext and redirects to /dashboard.
 */
const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const navigate = useNavigate();
    const { login: setAuthState } = useAuth();

    /**
     * Submits the login form. Calls the backend login endpoint, and on
     * success updates AuthContext and navigates to the dashboard. On
     * failure, displays a generic error message without revealing
     * whether the username or password was incorrect.
     *
     * @param e the form submit event
     */
    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError('');
        setIsSubmitting(true);

        try {
            const response = await login({ username, password });
            setAuthState(response);
            navigate('/dashboard');
        } catch (err) {
            setError('Invalid username or password');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1>Log In</h1>

                {error && <p className="error-text">{error}</p>}

                <label htmlFor="username">Username</label>
                <input
                    id="username"
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />

                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Logging in...' : 'Log In'}
                </button>

                <p>
                    Don't have an account? <Link to="/register">Register here</Link>
                </p>
            </form>
        </div>
    );
};

export default Login;