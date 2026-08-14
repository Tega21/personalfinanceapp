import { useState, useEffect } from 'react';
import { getForecast } from '../services/forecastService';
import type { ForecastResponse } from '../services/forecastService';
import './Forecast.css';

/**
 * Forecast page — displays a 30, 60, or 90-day cash flow projection
 * based on the user's recurring transaction templates.
 */
const Forecast = () => {
    const [forecast, setForecast] = useState<ForecastResponse | null>(null);
    const [days, setDays] = useState(30);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchForecast();
    }, [days]);

    /** Fetches the forecast for the currently selected day window. */
    const fetchForecast = async () => {
        try {
            setLoading(true);
            const data = await getForecast(days);
            setForecast(data);
        } catch (err) {
            setError('Failed to load forecast.');
        } finally {
            setLoading(false);
        }
    };

    /** Returns ordinal suffix for day display (1st, 2nd, 3rd, etc.). */
    const ordinal = (n: number) => {
        const s = ['th', 'st', 'nd', 'rd'];
        const v = n % 100;
        return n + (s[(v - 20) % 10] || s[v] || s[0]);
    };

    const fmt = (amount: number) =>
        amount.toLocaleString('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        });

    return (
        <div className="forecast-container">
            <div className="forecast-header">
                <h1>Cash Flow Forecast</h1>

                {/* Day Window Toggle */}
                <div className="day-toggle">
                    {[30, 60, 90].map((d) => (
                        <button
                            key={d}
                            className={`toggle-btn ${days === d ? 'active' : ''}`}
                            onClick={() => setDays(d)}
                        >
                            {d} Days
                        </button>
                    ))}
                </div>
            </div>

            {error && <div className="error-message">{error}</div>}

            {loading ? (
                <div className="loading">Loading forecast...</div>
            ) : forecast ? (
                <>
                    {/* Summary Cards */}
                    <div className="forecast-summary">
                        <div className="summary-card income">
                            <div className="summary-label">Projected Income</div>
                            <div className="summary-value">+${fmt(forecast.projectedIncome)}</div>
                        </div>
                        <div className="summary-card expenses">
                            <div className="summary-label">Projected Expenses</div>
                            <div className="summary-value">-${fmt(forecast.projectedExpenses)}</div>
                        </div>
                        <div className={`summary-card net ${forecast.projectedNet >= 0 ? 'positive' : 'negative'}`}>
                            <div className="summary-label">Projected Net</div>
                            <div className="summary-value">
                                {forecast.projectedNet >= 0 ? '+' : '-'}$
                                {fmt(Math.abs(forecast.projectedNet))}
                            </div>
                        </div>
                    </div>

                    {/* Upcoming Transactions */}
                    <div className="upcoming-section">
                        <h2>Upcoming Transactions</h2>
                        {forecast.upcomingTransactions.length === 0 ? (
                            <div className="no-upcoming">
                                <p>No recurring transactions fall within this window.</p>
                            </div>
                        ) : (
                            <div className="upcoming-list">
                                {forecast.upcomingTransactions.map((item, index) => (
                                    <div key={index} className="upcoming-card">
                                        <div className="upcoming-left">
                                            <div className="upcoming-description">{item.description}</div>
                                            <div className="upcoming-meta">
                                                {item.categoryName} · Due on the {ordinal(item.dayOfMonth)}
                                            </div>
                                        </div>
                                        <div className={`upcoming-amount ${item.type === 'INCOME' ? 'income' : 'expense'}`}>
                                            {item.type === 'INCOME' ? '+' : '-'}${fmt(item.amount)}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </>
            ) : null}
        </div>
    );
};

export default Forecast;