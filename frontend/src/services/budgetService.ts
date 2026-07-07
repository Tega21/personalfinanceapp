import api from './api';

/**
 * Shape of a budget as returned by the backend, including live
 * spent/percentUsed/status calculated from actual transactions.
 */
export interface BudgetResponse {
    budgetId: number;
    categoryId: number;
    categoryName: string;
    amountLimit: number;
    month: number;
    year: number;
    spent: number;
    percentUsed: number;
    status: 'OK' | 'WARNING' | 'EXCEEDED';
}

/**
 * Shape of the request body for creating or updating a budget.
 */
export interface BudgetRequest {
    categoryId: number;
    amountLimit: number;
    month: number;
    year: number;
}

/**
 * Retrieves all budgets for the authenticated user for a given month
 * and year, with live spending calculations.
 *
 * @param month the month to retrieve budgets for (1-12)
 * @param year the year to retrieve budgets for
 * @returns the user's budgets for that month
 */
export const getBudgets = async (month: number, year: number): Promise<BudgetResponse[]> => {
    const response = await api.get<BudgetResponse[]>('/budgets', { params: { month, year } });
    return response.data;
};

/**
 * Creates a new budget.
 *
 * @param data the budget's category, amount limit, month, and year
 * @returns the created budget
 */
export const createBudget = async (data: BudgetRequest): Promise<BudgetResponse> => {
    const response = await api.post<BudgetResponse>('/budgets', data);
    return response.data;
};

/**
 * Updates an existing budget's amount limit.
 *
 * @param id the ID of the budget to update
 * @param data the updated budget details
 * @returns the updated budget with recalculated spending
 */
export const updateBudget = async (id: number, data: BudgetRequest): Promise<BudgetResponse> => {
    const response = await api.put<BudgetResponse>(`/budgets/${id}`, data);
    return response.data;
};

/**
 * Deletes a budget.
 *
 * @param id the ID of the budget to delete
 */
export const deleteBudget = async (id: number): Promise<void> => {
    await api.delete(`/budgets/${id}`);
};