import api from './api';

/**
 * Shape of the request body for creating or updating a transaction.
 */
export interface TransactionRequest {
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    transactionDate: string;
    categoryId: number;
    description: string;
}

/**
 * Shape of a transaction as returned by the backend.
 */
export interface TransactionResponse {
    id: number;
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    transactionDate: string;
    description: string;
    categoryName: string;
    categoryId: number;
    createdAt: string;
}

/**
 * Retrieves all transactions belonging to the authenticated user,
 * sorted by most recent date first.
 *
 * @returns the user's full transaction list
 */
/**
 * Retrieves the authenticated user's transactions with optional filters.
 *
 * @param categoryId optional category ID filter
 * @param startDate optional start date filter (yyyy-MM-dd)
 * @param endDate optional end date filter (yyyy-MM-dd)
 * @param keyword optional description keyword filter
 * @returns filtered transactions sorted by most recent date first
 */
export const getTransactions = async (
    categoryId?: number,
    startDate?: string,
    endDate?: string,
    keyword?: string
): Promise<TransactionResponse[]> => {
    const response = await api.get<TransactionResponse[]>('/transactions', {
        params: {
            ...(categoryId !== undefined && { categoryId }),
            ...(startDate && { startDate }),
            ...(endDate && { endDate }),
            ...(keyword && { keyword }),
        },
    });
    return response.data;
};

/**
 * Creates a new transaction.
 *
 * @param data the new transaction's amount, type, date, category, and description
 * @returns the created transaction
 */
export const createTransaction = async (
    data: TransactionRequest
): Promise<TransactionResponse> => {
    const response = await api.post<TransactionResponse>('/transactions', data);
    return response.data;
};

/**
 * Updates an existing transaction.
 *
 * @param id the ID of the transaction to update
 * @param data the updated amount, type, date, category, and description
 * @returns the updated transaction
 */
export const updateTransaction = async (
    id: number,
    data: TransactionRequest
): Promise<TransactionResponse> => {
    const response = await api.put<TransactionResponse>(`/transactions/${id}`, data);
    return response.data;
};

/**
 * Deletes a transaction.
 *
 * @param id the ID of the transaction to delete
 */
export const deleteTransaction = async (id: number): Promise<void> => {
    await api.delete(`/transactions/${id}`);
};