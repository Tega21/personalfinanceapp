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

/**
 * Shape of a single month's expense total for the trend chart.
 */
export interface TrendDataPoint {
    month: string;
    totalExpenses: number;
}

/**
 * Retrieves spending trend data for the last N months.
 *
 * @param months the number of past months to include (default 6)
 * @returns monthly expense totals in chronological order
 */
export const getSpendingTrends = async (months: number = 6): Promise<TrendDataPoint[]> => {
    const response = await api.get<TrendDataPoint[]>('/dashboard/trends', {
        params: { months }
    });
    return response.data;
};