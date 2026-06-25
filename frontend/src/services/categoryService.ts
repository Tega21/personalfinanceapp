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
 * Shape of the request body for creating a new category.
 */
export interface CategoryRequest {
    name: string;
    type: 'INCOME' | 'EXPENSE';
    description?: string;
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

/**
 * Creates a new custom category.
 *
 * @param data the new category's name, type, and optional description
 * @returns the created category
 */
export const createCategory = async (
    data: CategoryRequest
): Promise<CategoryResponse> => {
    const response = await api.post<CategoryResponse>('/categories', data);
    return response.data;
};

export interface CategoryResponse {
    id: number;
    name: string;
    type: 'INCOME' | 'EXPENSE';
    description: string | null;
    isDefault: boolean;
}

/**
 * Deletes a category.
 *
 * @param id the ID of the category to delete
 */
export const deleteCategory = async (id: number): Promise<void> => {
    await api.delete(`/categories/${id}`);
};