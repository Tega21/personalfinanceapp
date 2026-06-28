import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from './Navbar';

/**
 * Wraps routes that require authentication. Redirects unauthenticated
 * users to /login; otherwise renders the persistent Navbar above the
 * wrapped page content.
 */
const ProtectedRoute = ({ children }: { children: ReactNode }) => {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return (
        <>
            <Navbar />
            <main>{children}</main>
        </>
    );
};

export default ProtectedRoute;