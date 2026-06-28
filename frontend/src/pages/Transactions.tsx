import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import {
    getTransactions,
    createTransaction,
    updateTransaction,
    deleteTransaction,
} from '../services/transactionService';
import type { TransactionResponse, TransactionRequest } from '../services/transactionService';
import { getCategories } from '../services/categoryService';
import type { CategoryResponse } from '../services/categoryService';
import './Transactions.css';

/**
 * Transactions list screen. Displays all of the user's transactions,
 * sorted by most recent date, with the ability to add, edit, and delete entries.
 */
const Transactions = () => {
    const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
    const [categories, setCategories] = useState<CategoryResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [amount, setAmount] = useState('');
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [transactionDate, setTransactionDate] = useState('');
    const [categoryId, setCategoryId] = useState('');
    const [description, setDescription] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    /**
     * Loads the user's transactions and categories in parallel on mount.
     */
    const loadData = async () => {
        try {
            const [transactionData, categoryData] = await Promise.all([
                getTransactions(),
                getCategories(),
            ]);
            setTransactions(transactionData);
            setCategories(categoryData);
        } catch (err) {
            setError('Failed to load data.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const resetForm = () => {
        setEditingId(null);
        setAmount('');
        setType('EXPENSE');
        setTransactionDate('');
        setCategoryId('');
        setDescription('');
    };

    const openAddForm = () => {
        resetForm();
        setShowForm(true);
    };

    const openEditForm = (t: TransactionResponse) => {
        setEditingId(t.id);
        setAmount(t.amount.toString());
        setType(t.type);
        setTransactionDate(t.transactionDate);
        setCategoryId(t.categoryId.toString());
        setDescription(t.description);
        setShowForm(true);
    };

    /**
     * Submits the add/edit form. Calls createTransaction or
     * updateTransaction depending on whether editingId is set, and
     * updates local state with the result rather than refetching the
     * whole list.
     *
     * @param e the form submit event
     */
    const handleFormSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError('');

        const payload: TransactionRequest = {
            amount: parseFloat(amount),
            type,
            transactionDate,
            categoryId: parseInt(categoryId, 10),
            description,
        };

        try {
            if (editingId !== null) {
                const updated = await updateTransaction(editingId, payload);
                setTransactions((prev) =>
                    prev.map((t) => (t.id === editingId ? updated : t))
                );
            } else {
                const created = await createTransaction(payload);
                setTransactions((prev) => [created, ...prev]);
            }
            resetForm();
            setShowForm(false);
        } catch (err) {
            setError(editingId !== null ? 'Failed to update transaction.' : 'Failed to create transaction.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async (id: number) => {
        const confirmed = window.confirm('Delete this transaction?');
        if (!confirmed) return;

        try {
            await deleteTransaction(id);
            setTransactions((prev) => prev.filter((t) => t.id !== id));
        } catch (err) {
            setError('Failed to delete transaction.');
        }
    };

    const filteredCategories = categories.filter((c) => c.type === type);

    const handleToggleForm = () => {
        if (showForm) {
            resetForm();
            setShowForm(false);
        } else {
            openAddForm();
        }
    };

    if (isLoading) {
        return <p>Loading transactions...</p>;
    }

    return (
        <div className="transactions-page">
            <div className="transactions-header">
                <h1>Transactions</h1>
                <button className="primary-btn" onClick={handleToggleForm}>
                    {showForm ? 'Cancel' : 'Add Transaction'}
                </button>
            </div>

            {error && <p className="error-text">{error}</p>}

            {showForm && (
                <form className="transaction-form" onSubmit={handleFormSubmit}>
                    <h2>{editingId !== null ? 'Edit Transaction' : 'New Transaction'}</h2>

                    <div className="form-row">
                        <label htmlFor="type">Type</label>
                        <select
                            id="type"
                            value={type}
                            onChange={(e) => {
                                setType(e.target.value as 'INCOME' | 'EXPENSE');
                                setCategoryId('');
                            }}
                        >
                            <option value="EXPENSE">Expense</option>
                            <option value="INCOME">Income</option>
                        </select>
                    </div>

                    <div className="form-row">
                        <label htmlFor="amount">Amount</label>
                        <input
                            id="amount"
                            type="number"
                            step="0.01"
                            min="0.01"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-row">
                        <label htmlFor="categoryId">Category</label>
                        <select
                            id="categoryId"
                            value={categoryId}
                            onChange={(e) => setCategoryId(e.target.value)}
                            required
                        >
                            <option value="" disabled>
                                Select a category
                            </option>
                            {filteredCategories.map((c) => (
                                <option key={c.id} value={c.id}>
                                    {c.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-row">
                        <label htmlFor="transactionDate">Date</label>
                        <input
                            id="transactionDate"
                            type="date"
                            value={transactionDate}
                            onChange={(e) => setTransactionDate(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-row">
                        <label htmlFor="description">Description</label>
                        <input
                            id="description"
                            type="text"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    </div>

                    <button type="submit" className="primary-btn" disabled={isSubmitting}>
                        {isSubmitting ? 'Saving...' : 'Save Transaction'}
                    </button>
                </form>
            )}

            {transactions.length === 0 ? (
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
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {transactions.map((t) => (
                        <tr key={t.id}>
                            <td>{t.transactionDate}</td>
                            <td>{t.description}</td>
                            <td>{t.categoryName}</td>
                            <td>{t.type}</td>
                            <td className={t.type === 'INCOME' ? 'amount-positive' : 'amount-negative'}>
                                ${t.amount.toFixed(2)}
                            </td>
                            <td>
                                <button className="action-btn" onClick={() => openEditForm(t)}>
                                    Edit
                                </button>
                                <button className="action-btn" onClick={() => handleDelete(t.id)}>
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default Transactions;