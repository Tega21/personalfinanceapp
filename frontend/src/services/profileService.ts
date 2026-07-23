import api from './api';

/**
 * Shape of the user's profile as returned by the backend.
 */
export interface ProfileResponse {
    id: number;
    username: string;
    email: string;
    firstName: string | null;
    lastName: string | null;
    createdAt: string;
}

/**
 * Shape of the request body for updating profile information.
 */
export interface UpdateProfileRequest {
    firstName?: string;
    lastName?: string;
    email?: string;
}

/**
 * Shape of the request body for changing the password.
 */
export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

/**
 * Retrieves the authenticated user's profile.
 *
 * @returns the user's profile data
 */
export const getProfile = async (): Promise<ProfileResponse> => {
    const response = await api.get<ProfileResponse>('/profile');
    return response.data;
};

/**
 * Updates the authenticated user's profile information.
 *
 * @param data the updated name and/or email
 * @returns the updated profile
 */
export const updateProfile = async (data: UpdateProfileRequest): Promise<ProfileResponse> => {
    const response = await api.put<ProfileResponse>('/profile/email', data);
    return response.data;
};

/**
 * Changes the authenticated user's password.
 *
 * @param data the current and new passwords
 */
export const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
    await api.put('/profile/password', data);
};