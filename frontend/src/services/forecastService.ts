import axios from 'axios';

const API_URL = 'http://localhost:8080/api/forecast';

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
};

export interface ForecastResponse {
    days: number;
    projectedIncome: number;
    projectedExpenses: number;
    projectedNet: number;
    upcomingTransactions: {
        id: number;
        description: string;
        amount: number;
        type: 'INCOME' | 'EXPENSE';
        categoryId: number;
        categoryName: string;
        dayOfMonth: number;
    }[];
}

/** Fetches a cash flow forecast for the given number of days (30, 60, or 90). */
export const getForecast = async (days: number): Promise<ForecastResponse> => {
    const response = await axios.get(`${API_URL}?days=${days}`, {
        headers: getAuthHeader(),
    });
    return response.data;
};