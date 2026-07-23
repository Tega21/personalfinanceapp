import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { getProfile, updateProfile, changePassword } from '../services/profileService';
import type { ProfileResponse } from '../services/profileService';
import './Profile.css';

/**
 * Profile settings page. Displays the authenticated user's account
 * information and allows updating name/email and changing password.
 */
const Profile = () => {
    const [profile, setProfile] = useState<ProfileResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [isProfileSubmitting, setIsProfileSubmitting] = useState(false);

    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [isPasswordSubmitting, setIsPasswordSubmitting] = useState(false);

    /**
     * Loads the user's profile on mount and pre-fills the form fields.
     */
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const data = await getProfile();
                setProfile(data);
                setFirstName(data.firstName ?? '');
                setLastName(data.lastName ?? '');
                setEmail(data.email);
            } catch (err) {
                setError('Failed to load profile.');
            } finally {
                setIsLoading(false);
            }
        };
        fetchProfile();
    }, []);

    /**
     * Submits the profile update form.
     *
     * @param e the form submit event
     */
    const handleProfileSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsProfileSubmitting(true);
        setError('');
        setSuccessMessage('');

        try {
            const updated = await updateProfile({ firstName, lastName, email });
            setProfile(updated);
            setSuccessMessage('Profile updated successfully.');
        } catch (err) {
            setError('Failed to update profile. Email may already be in use.');
        } finally {
            setIsProfileSubmitting(false);
        }
    };

    /**
     * Submits the password change form and clears the fields on success.
     *
     * @param e the form submit event
     */
    const handlePasswordSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsPasswordSubmitting(true);
        setError('');
        setSuccessMessage('');

        try {
            await changePassword({ currentPassword, newPassword });
            setCurrentPassword('');
            setNewPassword('');
            setSuccessMessage('Password changed successfully.');
        } catch (err) {
            setError('Failed to change password. Current password may be incorrect.');
        } finally {
            setIsPasswordSubmitting(false);
        }
    };

    if (isLoading) return <p>Loading profile...</p>;

    return (
        <div className="profile-page">
            <h1>Profile Settings</h1>

            {error && <p className="error-text">{error}</p>}
            {successMessage && <p className="success-text">{successMessage}</p>}

            <div className="profile-section">
                <h2>Account Information</h2>
                <p className="profile-username">Username: <strong>{profile?.username}</strong></p>
                <p className="profile-since">Member since: {profile?.createdAt
                    ? new Date(profile.createdAt).toLocaleDateString()
                    : '—'}
                </p>

                <form className="profile-form" onSubmit={handleProfileSubmit}>
                    <div className="form-row">
                        <label htmlFor="firstName">First Name</label>
                        <input
                            id="firstName"
                            type="text"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                        />
                    </div>

                    <div className="form-row">
                        <label htmlFor="lastName">Last Name</label>
                        <input
                            id="lastName"
                            type="text"
                            value={lastName}
                            onChange={(e) => setLastName(e.target.value)}
                        />
                    </div>

                    <div className="form-row">
                        <label htmlFor="email">Email</label>
                        <input
                            id="email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="primary-btn" disabled={isProfileSubmitting}>
                        {isProfileSubmitting ? 'Saving...' : 'Save Changes'}
                    </button>
                </form>
            </div>

            <div className="profile-section">
                <h2>Change Password</h2>
                <form className="profile-form" onSubmit={handlePasswordSubmit}>
                    <div className="form-row">
                        <label htmlFor="currentPassword">Current Password</label>
                        <input
                            id="currentPassword"
                            type="password"
                            value={currentPassword}
                            onChange={(e) => setCurrentPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-row">
                        <label htmlFor="newPassword">New Password</label>
                        <input
                            id="newPassword"
                            type="password"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="primary-btn" disabled={isPasswordSubmitting}>
                        {isPasswordSubmitting ? 'Changing...' : 'Change Password'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Profile;