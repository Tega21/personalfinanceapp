import api from './api';

export interface CategoryBreakdown {
    categoryId: number;
    categoryName: string;
    total: number;
}

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

export interface DashboardSummary {
    totalIncome: number;
    totalExpenses: number;
    netCashFlow: number;
    categoryBreakdown: CategoryBreakdown[];
    recentTransactions: TransactionResponse[];
}

/**
 * Retrieves the dashboard summary for a given month and year.
 * If month/year are omitted, the backend defaults to the current month.
 *
 * @param month optional month (1-12) to retrieve
 * @param year optional year to retrieve
 * @returns the dashboard summary for the requested month
 */
export const getDashboardSummary = async (
    month?: number,
    year?: number
): Promise<DashboardSummary> => {
    const response = await api.get<DashboardSummary>('/dashboard/summary', {
        params: { month, year },
    });
    return response.data;
};