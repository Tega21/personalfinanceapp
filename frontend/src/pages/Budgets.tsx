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
 * creating budgets inline, editing via a modal, and deleting.
 */
const Budgets = () => {
    const now = new Date();
    const [month, setMonth] = useState(now.getMonth() + 1);
    const [year, setYear] = useState(now.getFullYear());

    const [budgets, setBudgets] = useState<BudgetResponse[]>([]);
    const [categories, setCategories] = useState<CategoryResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    // Add form state
    const [showForm, setShowForm] = useState(false);
    const [selectedCategoryId, setSelectedCategoryId] = useState('');
    const [amountLimit, setAmountLimit] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Edit modal state
    const [editingBudget, setEditingBudget] = useState<BudgetResponse | null>(null);
    const [editAmountLimit, setEditAmountLimit] = useState('');

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
        setSelectedCategoryId('');
        setAmountLimit('');
    };

    const handleToggleForm = () => {
        if (showForm) {
            resetForm();
            setShowForm(false);
        } else {
            resetForm();
            setShowForm(true);
        }
    };

    /**
     * Submits the add form to create a new budget, then reloads.
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
            await createBudget(payload);
            resetForm();
            setShowForm(false);
            await loadData();
        } catch (err) {
            setError('Failed to create budget. A budget for this category may already exist.');
        } finally {
            setIsSubmitting(false);
        }
    };

    /**
     * Opens the edit modal pre-filled with the selected budget's limit.
     */
    const openEditModal = (budget: BudgetResponse) => {
        setEditingBudget(budget);
        setEditAmountLimit(budget.amountLimit.toString());
    };

    const closeEditModal = () => {
        setEditingBudget(null);
    };

    /**
     * Submits the edit modal to update the budget's limit, then reloads.
     */
    const handleEditSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!editingBudget) return;
        setIsSubmitting(true);
        setError('');

        const payload = {
            categoryId: editingBudget.categoryId,
            amountLimit: parseFloat(editAmountLimit),
            month,
            year,
        };

        try {
            await updateBudget(editingBudget.budgetId, payload);
            closeEditModal();
            await loadData();
        } catch (err) {
            setError('Failed to update budget.');
        } finally {
            setIsSubmitting(false);
        }
    };

    /**
     * Deletes a budget after a confirmation prompt, then reloads.
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
     * Returns the CSS class for a progress bar based on the budget's status.
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
                    <h2>New Budget</h2>

                    <div className="form-row">
                        <label htmlFor="category">Category</label>
                        <select
                            id="category"
                            value={selectedCategoryId}
                            onChange={(e) => setSelectedCategoryId(e.target.value)}
                            required
                        >
                            <option value="" disabled>Select a category</option>
                            {availableCategories.map((c) => (
                                <option key={c.id} value={c.id}>{c.name}</option>
                            ))}
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
                                    <button className="action-btn edit-btn" onClick={() => openEditModal(budget)}>Edit</button>
                                    <button className="action-btn delete-btn" onClick={() => handleDelete(budget.budgetId)}>Delete</button>
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

            {/* Edit Modal */}
            {editingBudget && (
                <div className="modal-overlay" onClick={closeEditModal}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <h2>Edit Budget</h2>
                        <form onSubmit={handleEditSubmit}>
                            <div className="form-row">
                                <label>Category</label>
                                <input type="text" value={editingBudget.categoryName} disabled />
                            </div>

                            <div className="form-row">
                                <label htmlFor="editAmountLimit">Monthly Limit ($)</label>
                                <input
                                    id="editAmountLimit"
                                    type="number"
                                    step="0.01"
                                    min="0.01"
                                    value={editAmountLimit}
                                    onChange={(e) => setEditAmountLimit(e.target.value)}
                                    required
                                />
                            </div>

                            <div className="modal-actions">
                                <button type="submit" className="primary-btn" disabled={isSubmitting}>
                                    {isSubmitting ? 'Saving...' : 'Save Changes'}
                                </button>
                                <button type="button" className="action-btn" onClick={closeEditModal}>
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Budgets;