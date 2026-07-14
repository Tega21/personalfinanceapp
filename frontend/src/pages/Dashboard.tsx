import { useState, useEffect } from 'react';
import { getDashboardSummary, getSpendingTrends } from '../services/dashboardService';
import type { DashboardSummary, TrendDataPoint } from '../services/dashboardService';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    PieChart,
    Pie,
    Cell
} from 'recharts';
import './Dashboard.css';

const COLORS = ['#6B21A8', '#9333EA', '#C084FC', '#A855F7', '#7E22CE', '#D8B4FE'];

const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
];

/**
 * Dashboard screen. Shows the authenticated user's income/expense totals,
 * net cash flow, a category spending pie chart, recent transactions
 * for a selectable month, and a spending trend line chart.
 */
const Dashboard = () => {
    const now = new Date();
    const [selectedMonth, setSelectedMonth] = useState(now.getMonth() + 1);
    const [selectedYear, setSelectedYear] = useState(now.getFullYear());
    const [summary, setSummary] = useState<DashboardSummary | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [trendData, setTrendData] = useState<TrendDataPoint[]>([]);

    useEffect(() => {
        const fetchSummary = async () => {
            setIsLoading(true);
            try {
                const data = await getDashboardSummary(selectedMonth, selectedYear);
                setSummary(data);
                const trends = await getSpendingTrends(6);
                setTrendData(trends);
            } catch (err) {
                setError('Failed to load dashboard data.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchSummary();
    }, [selectedMonth, selectedYear]);

    /**
     * Navigates the dashboard to the previous calendar month.
     */
    const goToPreviousMonth = () => {
        if (selectedMonth === 1) {
            setSelectedMonth(12);
            setSelectedYear(selectedYear - 1);
        } else {
            setSelectedMonth(selectedMonth - 1);
        }
    };

    /**
     * Navigates the dashboard to the next calendar month.
     */
    const goToNextMonth = () => {
        if (selectedMonth === 12) {
            setSelectedMonth(1);
            setSelectedYear(selectedYear + 1);
        } else {
            setSelectedMonth(selectedMonth + 1);
        }
    };

    if (isLoading) {
        return <p>Loading dashboard...</p>;
    }

    if (error || !summary) {
        return <p className="error-text">{error || 'No data available.'}</p>;
    }

    return (
        <div className="dashboard-page">
            <h1>Dashboard</h1>

            <div className="month-navigator">
                <button onClick={goToPreviousMonth} className="nav-arrow">←</button>
                <span className="month-label">
                    {monthNames[selectedMonth - 1]} {selectedYear}
                </span>
                <button onClick={goToNextMonth} className="nav-arrow">→</button>
            </div>

            <div className="summary-cards">
                <div className="summary-card">
                    <h3>Total Income</h3>
                    <p className="amount-positive">${summary.totalIncome.toFixed(2)}</p>
                </div>
                <div className="summary-card">
                    <h3>Total Expenses</h3>
                    <p className="amount-negative">${summary.totalExpenses.toFixed(2)}</p>
                </div>
                <div className="summary-card">
                    <h3>Net Cash Flow</h3>
                    <p className={summary.netCashFlow >= 0 ? 'amount-positive' : 'amount-negative'}>
                        ${summary.netCashFlow.toFixed(2)}
                    </p>
                </div>
            </div>

            <div className="dashboard-section">
                <h2>Spending by Category</h2>
                {summary.categoryBreakdown.length === 0 ? (
                    <p>No expenses logged this month yet.</p>
                ) : (
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={summary.categoryBreakdown}
                                dataKey="total"
                                nameKey="categoryName"
                                cx="50%"
                                cy="50%"
                                outerRadius={100}
                                label={(entry: any) => (entry as { categoryName: string }).categoryName}
                            >
                                {summary.categoryBreakdown.map((_, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip />
                        </PieChart>
                    </ResponsiveContainer>
                )}
            </div>

            <div className="dashboard-section">
                <h2>Recent Transactions</h2>
                {summary.recentTransactions.length === 0 ? (
                    <p>No transactions yet.</p>
                ) : (
                    <table className="transactions-table">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Description</th>
                            <th>Category</th>
                            <th>Type</th>
                            <th>Amount</th>
                        </tr>
                        </thead>
                        <tbody>
                        {summary.recentTransactions.map((t) => (
                            <tr key={t.id}>
                                <td>{t.transactionDate}</td>
                                <td>{t.description}</td>
                                <td>{t.categoryName}</td>
                                <td>{t.type}</td>
                                <td className={t.type === 'INCOME' ? 'amount-positive' : 'amount-negative'}>
                                    ${t.amount.toFixed(2)}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>

            <div className="trend-section">
                <h2>Spending Trend</h2>
                <ResponsiveContainer width="100%" height={250}>
                    <LineChart data={trendData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="month" />
                        <YAxis />
                        <Tooltip formatter={(value) => [`$${Number(value ?? 0).toFixed(2)}`, 'Expenses']} />
                        <Line
                            type="monotone"
                            dataKey="totalExpenses"
                            stroke="#6B21A8"
                            strokeWidth={2}
                            dot={{ r: 4 }}
                            activeDot={{ r: 6 }}
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default Dashboard;