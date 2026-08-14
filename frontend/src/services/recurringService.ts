import axios from 'axios';

const API_URL = 'http://localhost:8080/api/recurring';

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
};

export interface RecurringRequest {
    description: string;
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    categoryId: number;
    dayOfMonth: number;
}

export interface RecurringResponse {
    id: number;
    description: string;
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    categoryId: number;
    categoryName: string;
    dayOfMonth: number;
}

/** Fetches all recurring transaction templates for the authenticated user. */
export const getRecurring = async (): Promise<RecurringResponse[]> => {
    const response = await axios.get(API_URL, { headers: getAuthHeader() });
    return response.data;
};

/** Creates a new recurring transaction template. */
export const createRecurring = async (
    request: RecurringRequest
): Promise<RecurringResponse> => {
    const response = await axios.post(API_URL, request, { headers: getAuthHeader() });
    return response.data;
};

/** Updates an existing recurring transaction template by ID. */
export const updateRecurring = async (
    id: number,
    request: RecurringRequest
): Promise<RecurringResponse> => {
    const response = await axios.put(`${API_URL}/${id}`, request, {
        headers: getAuthHeader(),
    });
    return response.data;
};

/** Deletes a recurring transaction template by ID. */
export const deleteRecurring = async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/${id}`, { headers: getAuthHeader() });
};