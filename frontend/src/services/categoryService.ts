import api from './api';

/**
 * Shape of a category as returned by the backend.
 */
export interface CategoryResponse {
    id: number;
    name: string;
    type: 'INCOME' | 'EXPENSE';
    description: string | null;
}

/**
 * Retrieves all categories belonging to the authenticated user
 * (their 15 seeded defaults plus any custom categories they've created).
 *
 * @returns the user's full category list
 */
export const getCategories = async (): Promise<CategoryResponse[]> => {
    const response = await api.get<CategoryResponse[]>('/categories');
    return response.data;
};