import { useState, useEffect } from 'react';
import {
    getRecurring,
    createRecurring,
    updateRecurring,
    deleteRecurring,
} from '../services/recurringService';
import type { RecurringRequest, RecurringResponse } from '../services/recurringService';
import { getCategories } from '../services/categoryService';
import './RecurringTransactions.css';

/**
 * Recurring Transactions page — displays all recurring transaction templates
 * with controls to add, edit, and delete them.
 */
const RecurringTransactions = () => {
    const [recurring, setRecurring] = useState<RecurringResponse[]>([]);
    const [categories, setCategories] = useState<{ id: number; name: string }[]>([]);
    const [showModal, setShowModal] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [error, setError] = useState('');

    const [form, setForm] = useState<RecurringRequest>({
        description: '',
        amount: 0,
        type: 'EXPENSE',
        categoryId: 0,
        dayOfMonth: 1,
    });

    useEffect(() => {
        fetchData();
    }, []);

    /** Fetches recurring templates and categories from the API. */
    const fetchData = async () => {
        try {
            const [recurringData, categoryData] = await Promise.all([
                getRecurring(),
                getCategories(),
            ]);
            setRecurring(recurringData);
            setCategories(categoryData);
            if (categoryData.length > 0 && form.categoryId === 0) {
                setForm((prev) => ({ ...prev, categoryId: categoryData[0].id }));
            }
        } catch (err) {
            setError('Failed to load recurring transactions.');
        }
    };

    /** Opens the add modal with a blank form. */
    const handleAdd = () => {
        setEditingId(null);
        setForm({
            description: '',
            amount: 0,
            type: 'EXPENSE',
            categoryId: categories[0]?.id || 0,
            dayOfMonth: 1,
        });
        setShowModal(true);
    };

    /** Opens the edit modal pre-populated with the selected template's data. */
    const handleEdit = (item: RecurringResponse) => {
        setEditingId(item.id);
        setForm({
            description: item.description,
            amount: item.amount,
            type: item.type,
            categoryId: item.categoryId,
            dayOfMonth: item.dayOfMonth,
        });
        setShowModal(true);
    };

    /** Submits the form to create or update a recurring template. */
    const handleSubmit = async () => {
        try {
            if (editingId !== null) {
                await updateRecurring(editingId, form);
            } else {
                await createRecurring(form);
            }
            setShowModal(false);
            fetchData();
        } catch (err) {
            setError('Failed to save recurring transaction.');
        }
    };

    /** Deletes a recurring template after confirmation. */
    const handleDelete = async (id: number) => {
        if (!window.confirm('Delete this recurring transaction?')) return;
        try {
            await deleteRecurring(id);
            fetchData();
        } catch (err) {
            setError('Failed to delete recurring transaction.');
        }
    };

    /** Returns ordinal suffix for day display (1st, 2nd, 3rd, etc.). */
    const ordinal = (n: number) => {
        const s = ['th', 'st', 'nd', 'rd'];
        const v = n % 100;
        return n + (s[(v - 20) % 10] || s[v] || s[0]);
    };

    return (
        <div className="recurring-container">
            <div className="recurring-header">
                <h1>Recurring Transactions</h1>
                <button className="btn-add" onClick={handleAdd}>+ Add Recurring</button>
            </div>

            {error && <div className="error-message">{error}</div>}

            {recurring.length === 0 ? (
                <div className="no-recurring">
                    <p>No recurring transactions yet. Add bills or income that repeat monthly.</p>
                </div>
            ) : (
                <div className="recurring-list">
                    {recurring.map((item) => (
                        <div key={item.id} className="recurring-card">
                            <div className="recurring-card-left">
                                <div className="recurring-description">{item.description}</div>
                                <div className="recurring-meta">
                                    {item.categoryName} · Every month on the {ordinal(item.dayOfMonth)}
                                </div>
                            </div>
                            <div className="recurring-card-right">
                                <div className={`recurring-amount ${item.type === 'INCOME' ? 'income' : 'expense'}`}>
                                    {item.type === 'INCOME' ? '+' : '-'}$
                                    {item.amount.toLocaleString('en-US', {
                                        minimumFractionDigits: 2,
                                        maximumFractionDigits: 2,
                                    })}
                                </div>
                                <div className="recurring-actions">
                                    <button className="btn-edit" onClick={() => handleEdit(item)}>Edit</button>
                                    <button className="btn-delete" onClick={() => handleDelete(item.id)}>Delete</button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Add / Edit Modal */}
            {showModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>{editingId !== null ? 'Edit Recurring' : 'Add Recurring'}</h2>

                        <label>Description</label>
                        <input
                            type="text"
                            value={form.description}
                            onChange={(e) => setForm({ ...form, description: e.target.value })}
                            placeholder="e.g. Netflix, Rent, Paycheck"
                        />

                        <label>Amount</label>
                        <input
                            type="number"
                            step="0.01"
                            value={form.amount}
                            onChange={(e) => setForm({ ...form, amount: parseFloat(e.target.value) })}
                        />

                        <label>Type</label>
                        <select
                            value={form.type}
                            onChange={(e) =>
                                setForm({ ...form, type: e.target.value as 'INCOME' | 'EXPENSE' })
                            }
                        >
                            <option value="EXPENSE">Expense</option>
                            <option value="INCOME">Income</option>
                        </select>

                        <label>Category</label>
                        <select
                            value={form.categoryId}
                            onChange={(e) => setForm({ ...form, categoryId: parseInt(e.target.value) })}
                        >
                            {categories.map((cat) => (
                                <option key={cat.id} value={cat.id}>
                                    {cat.name}
                                </option>
                            ))}
                        </select>

                        <label>Day of Month</label>
                        <input
                            type="number"
                            min={1}
                            max={31}
                            value={form.dayOfMonth}
                            onChange={(e) => setForm({ ...form, dayOfMonth: parseInt(e.target.value) })}
                        />

                        <div className="modal-actions">
                            <button className="btn-save" onClick={handleSubmit}>
                                {editingId !== null ? 'Save Changes' : 'Add Recurring'}
                            </button>
                            <button className="btn-cancel" onClick={() => setShowModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RecurringTransactions;