import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

/**
 * Registration page. Creates a new account, which also triggers
 * automatic creation of 15 default categories on the backend. On
 * success, stores the JWT via AuthContext and redirects to /dashboard.
 */
const Register = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const navigate = useNavigate();
    const { login: setAuthState } = useAuth();

    /**
     * Submits the registration form. Calls the backend register
     * endpoint, and on success updates AuthContext and navigates to
     * the dashboard.
     *
     * @param e the form submit event
     */
    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError('');
        setIsSubmitting(true);

        try {
            const response = await register({ username, email, password });
            setAuthState(response);
            navigate('/dashboard');
        } catch (err) {
            setError('Registration failed. Username or email may already be in use.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1>Create Account</h1>

                {error && <p className="error-text">{error}</p>}

                <label htmlFor="username">Username</label>
                <input
                    id="username"
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    minLength={3}
                    maxLength={50}
                    required
                />

                <label htmlFor="email">Email</label>
                <input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    minLength={6}
                    required
                />

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Creating account...' : 'Create Account'}
                </button>

                <p>
                    Already have an account? <Link to="/login">Log in here</Link>
                </p>
            </form>
        </div>
    );
};

export default Register;