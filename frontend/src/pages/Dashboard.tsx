import { useState, useEffect } from 'react';
import { getDashboardSummary } from '../services/dashboardService';
import type { DashboardSummary } from '../services/dashboardService';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';
import './Dashboard.css';

const COLORS = ['#6B21A8', '#9333EA', '#C084FC', '#A855F7', '#7E22CE', '#D8B4FE'];

const Dashboard = () => {
    const [summary, setSummary] = useState<DashboardSummary | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchSummary = async () => {
            try {
                const data = await getDashboardSummary();
                setSummary(data);
            } catch (err) {
                setError('Failed to load dashboard data.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchSummary();
    }, []);

    if (isLoading) {
        return <p>Loading dashboard...</p>;
    }

    if (error || !summary) {
        return <p className="error-text">{error || 'No data available.'}</p>;
    }

    return (
        <div className="dashboard-page">
            <h1>Dashboard</h1>

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
                                // label={(entry) => entry.categoryName}
                                label={(entry: any) => (entry as {categoryName: string}).categoryName}
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
        </div>
    );
};

export default Dashboard;