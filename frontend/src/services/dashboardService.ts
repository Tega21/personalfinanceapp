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

export const getDashboardSummary = async (): Promise<DashboardSummary> => {
    const response = await api.get<DashboardSummary>('/dashboard/summary');
    return response.data;
};