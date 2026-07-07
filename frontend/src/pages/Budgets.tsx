import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { getBudgets, createBudget, updateBudget, deleteBudget } from '../services/budgetService';
import type { BudgetResponse } from '../services/budgetService';
import { getCategories } from '../services/categoryService';
import type { CategoryResponse } from '../services/categoryService';
import './Budgets.css';

/**
 * Budget Manager page. Displays all budgets for the selected month
 * with color-coded progress bars showing spending vs limit. Supports
 * creating, editing, and deleting budgets.
 */
const Budgets = () => {
    const now = new Date();
    const [month, setMonth] = useState(now.getMonth() + 1);
    const [year, setYear] = useState(now.getFullYear());

    const [budgets, setBudgets] = useState<BudgetResponse[]>([]);
    const [categories, setCategories] = useState<CategoryResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [selectedCategoryId, setSelectedCategoryId] = useState('');
    const [amountLimit, setAmountLimit] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    /**
     * Loads budgets for the currently selected month/year and the
     * user's full category list.
     */
    const loadData = async () => {
        setIsLoading(true);
        try {
            const [budgetData, categoryData] = await Promise.all([
                getBudgets(month, year),
                getCategories(),
            ]);
            setBudgets(budgetData);
            setCategories(categoryData);
        } catch (err) {
            setError('Failed to load budgets.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [month, year]);

    const resetForm = () => {
        setEditingId(null);
        setSelectedCategoryId('');
        setAmountLimit('');
    };

    const openAddForm = () => {
        resetForm();
        setShowForm(true);
    };

    /**
     * Opens the form pre-filled with an existing budget's values,
     * switching into edit mode.
     *
     * @param budget the budget to edit
     */
    const openEditForm = (budget: BudgetResponse) => {
        setEditingId(budget.budgetId);
        setSelectedCategoryId(budget.categoryId.toString());
        setAmountLimit(budget.amountLimit.toString());
        setShowForm(true);
    };

    const handleToggleForm = () => {
        if (showForm) {
            resetForm();
            setShowForm(false);
        } else {
            openAddForm();
        }
    };

    /**
     * Submits the add/edit form. Calls createBudget or updateBudget
     * depending on whether editingId is set, then reloads the budget
     * list to reflect live spending calculations.
     *
     * @param e the form submit event
     */
    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError('');

        const payload = {
            categoryId: parseInt(selectedCategoryId, 10),
            amountLimit: parseFloat(amountLimit),
            month,
            year,
        };

        try {
            if (editingId !== null) {
                await updateBudget(editingId, payload);
            } else {
                await createBudget(payload);
            }
            resetForm();
            setShowForm(false);
            await loadData();
        } catch (err) {
            setError(editingId !== null
                ? 'Failed to update budget.'
                : 'Failed to create budget. A budget for this category may already exist.');
        } finally {
            setIsSubmitting(false);
        }
    };

    /**
     * Deletes a budget after a confirmation prompt, then reloads
     * the budget list.
     *
     * @param id the ID of the budget to delete
     */
    const handleDelete = async (id: number) => {
        const confirmed = window.confirm('Delete this budget?');
        if (!confirmed) return;

        try {
            await deleteBudget(id);
            await loadData();
        } catch (err) {
            setError('Failed to delete budget.');
        }
    };

    /**
     * Returns the CSS class for a progress bar based on the budget's
     * status — green for OK, yellow for WARNING, red for EXCEEDED.
     *
     * @param status the budget's current status string
     * @returns the CSS class name for that status
     */
    const getProgressClass = (status: string) => {
        if (status === 'EXCEEDED') return 'progress-exceeded';
        if (status === 'WARNING') return 'progress-warning';
        return 'progress-ok';
    };

    const monthName = new Date(year, month - 1).toLocaleString('default', { month: 'long' });

    // Categories that don't already have a budget this month
    const budgetedCategoryIds = new Set(budgets.map((b) => b.categoryId));
    const availableCategories = categories.filter((c) => !budgetedCategoryIds.has(c.id));

    if (isLoading) return <p>Loading budgets...</p>;

    return (
        <div className="budgets-page">
            <div className="budgets-header">
                <h1>Budget Manager</h1>
                <button className="primary-btn" onClick={handleToggleForm}>
                    {showForm ? 'Cancel' : 'Add Budget'}
                </button>
            </div>

            <div className="month-navigator">
                <button onClick={() => {
                    if (month === 1) { setMonth(12); setYear(y => y - 1); }
                    else setMonth(m => m - 1);
                }}>←</button>
                <span>{monthName} {year}</span>
                <button onClick={() => {
                    if (month === 12) { setMonth(1); setYear(y => y + 1); }
                    else setMonth(m => m + 1);
                }}>→</button>
            </div>

            {error && <p className="error-text">{error}</p>}

            {showForm && (
                <form className="budget-form" onSubmit={handleSubmit}>
                    <h2>{editingId !== null ? 'Edit Budget' : 'New Budget'}</h2>

                    <div className="form-row">
                        <label htmlFor="category">Category</label>
                        <select
                            id="category"
                            value={selectedCategoryId}
                            onChange={(e) => setSelectedCategoryId(e.target.value)}
                            required
                            disabled={editingId !== null}
                        >
                            <option value="" disabled>Select a category</option>
                            {editingId !== null
                                ? categories
                                    .filter((c) => c.id === parseInt(selectedCategoryId))
                                    .map((c) => <option key={c.id} value={c.id}>{c.name}</option>)
                                : availableCategories.map((c) => (
                                    <option key={c.id} value={c.id}>{c.name}</option>
                                ))
                            }
                        </select>
                    </div>

                    <div className="form-row">
                        <label htmlFor="amountLimit">Monthly Limit ($)</label>
                        <input
                            id="amountLimit"
                            type="number"
                            step="0.01"
                            min="0.01"
                            value={amountLimit}
                            onChange={(e) => setAmountLimit(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="primary-btn" disabled={isSubmitting}>
                        {isSubmitting ? 'Saving...' : 'Save Budget'}
                    </button>
                </form>
            )}

            {budgets.length === 0 ? (
                <p>No budgets set for {monthName} {year}.</p>
            ) : (
                <div className="budget-list">
                    {budgets.map((budget) => (
                        <div key={budget.budgetId} className="budget-card">
                            <div className="budget-card-header">
                                <span className="budget-category">{budget.categoryName}</span>
                                <div className="budget-actions">
                                    <button className="action-btn" onClick={() => openEditForm(budget)}>Edit</button>
                                    <button className="action-btn" onClick={() => handleDelete(budget.budgetId)}>Delete</button>
                                </div>
                            </div>

                            <div className="budget-amounts">
                                <span>${budget.spent.toFixed(2)} spent</span>
                                <span>of ${budget.amountLimit.toFixed(2)}</span>
                            </div>

                            <div className="progress-bar-track">
                                <div
                                    className={`progress-bar-fill ${getProgressClass(budget.status)}`}
                                    style={{ width: `${Math.min(budget.percentUsed, 100)}%` }}
                                />
                            </div>

                            <div className="budget-footer">
                                <span className={`status-badge status-${budget.status.toLowerCase()}`}>
                                    {budget.status}
                                </span>
                                <span>{budget.percentUsed.toFixed(1)}%</span>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Budgets;