import { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';
import type { AuthResponse } from '../services/authService';

/**
 * Shape of the authentication state and actions available via useAuth().
 */
interface AuthContextType {
    token: string | null;
    username: string | null;
    isAuthenticated: boolean;
    login: (data: AuthResponse) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Provides authentication state to the entire app. Wraps the app in
 * main.tsx so any component can call useAuth() to read the current
 * user's login state or trigger login/logout. State is initialized
 * from localStorage so that a page refresh won't log the user out.
 *
 * @param children the app components nested inside this provider
 */
export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
    const [username, setUsername] = useState<string | null>(localStorage.getItem('username'));

    /**
     * Stores the JWT and username in both localStorage and component
     * state, called after a successful login or registration.
     *
     * @param data the auth response containing the JWT and username
     */
    const login = (data: AuthResponse) => {
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        setToken(data.token);
        setUsername(data.username);
    };

    /**
     * Clears the JWT and username from both localStorage and component
     * state, logging the user out.
     */
    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        setToken(null);
        setUsername(null);
    };

    return (
        <AuthContext.Provider
            value={{ token, username, isAuthenticated: !!token, login, logout }}
        >
            {children}
        </AuthContext.Provider>
    );
};

/**
 * Hook for accessing authentication state and actions from any
 * component nested inside AuthProvider.
 *
 * @returns the current auth state and login/logout functions
 * @throws Error if called outside of an AuthProvider
 */
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};