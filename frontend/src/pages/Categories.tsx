import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import {
    getCategories,
    createCategory,
    deleteCategory,
} from '../services/categoryService';
import type { CategoryResponse, CategoryRequest } from '../services/categoryService';
import './Categories.css';

/**
 * Categories management screen. Lists the user's categories split into
 * two sections: 15 created defaults and any custom ones the user has
 * created, and supports creating and deleting custom categories.
 */
const Categories = () => {
    const [categories, setCategories] = useState<CategoryResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [showForm, setShowForm] = useState(false);
    const [name, setName] = useState('');
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [description, setDescription] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const loadCategories = async () => {
        try {
            const data = await getCategories();
            setCategories(data);
        } catch (err) {
            setError('Failed to load categories.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadCategories();
    }, []);

    const resetForm = () => {
        setName('');
        setType('EXPENSE');
        setDescription('');
    };

    const handleToggleForm = () => {
        if (showForm) {
            resetForm();
            setShowForm(false);
        } else {
            setShowForm(true);
        }
    };

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError('');

        const payload: CategoryRequest = {
            name,
            type,
            ...(description ? { description } : {}),
        };

        try {
            const created = await createCategory(payload);
            setCategories((prev) => [...prev, created]);
            resetForm();
            setShowForm(false);
        } catch (err) {
            setError('Failed to create category. The name may already be in use.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async (id: number) => {
        const confirmed = window.confirm(
            'Delete this category? This cannot be undone, and any existing transactions using it may be affected.'
        );
        if (!confirmed) return;

        try {
            await deleteCategory(id);
            setCategories((prev) => prev.filter((c) => c.id !== id));
        } catch (err) {
            setError('Failed to delete category. It may still be in use by existing transactions.');
        }
    };

    const renderRow = (c: CategoryResponse) => (
        <tr key={c.id}>
            <td>{c.name}</td>
            <td>{c.type}</td>
            <td>{c.description || '—'}</td>
            <td>
                <button className="action-btn" onClick={() => handleDelete(c.id)}>
                    Delete
                </button>
            </td>
        </tr>
    );

    if (isLoading) {
        return <p>Loading categories...</p>;
    }

    const defaultCategories = categories.filter((c) => c.isDefault);
    const customCategories = categories.filter((c) => !c.isDefault);

    return (
        <div className="categories-page">
            <div className="categories-header">
                <h1>Categories</h1>
                <button className="primary-btn" onClick={handleToggleForm}>
                    {showForm ? 'Cancel' : 'Add Category'}
                </button>
            </div>

            {error && <p className="error-text">{error}</p>}

            {showForm && (
                <form className="category-form" onSubmit={handleSubmit}>
                    <h2>New Category</h2>

                    <div className="form-row">
                        <label htmlFor="name">Name</label>
                        <input
                            id="name"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-row">
                        <label htmlFor="type">Type</label>
                        <select
                            id="type"
                            value={type}
                            onChange={(e) => setType(e.target.value as 'INCOME' | 'EXPENSE')}
                        >
                            <option value="EXPENSE">Expense</option>
                            <option value="INCOME">Income</option>
                        </select>
                    </div>

                    <div className="form-row">
                        <label htmlFor="description">Description (optional)</label>
                        <input
                            id="description"
                            type="text"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    </div>

                    <button type="submit" className="primary-btn" disabled={isSubmitting}>
                        {isSubmitting ? 'Saving...' : 'Save Category'}
                    </button>
                </form>
            )}

            <h2 className="categories-section-heading">Default Categories</h2>
            <table className="categories-table">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>{defaultCategories.map(renderRow)}</tbody>
            </table>

            <h2 className="categories-section-heading">Custom Categories</h2>
            {customCategories.length === 0 ? (
                <p>You haven't created any custom categories yet.</p>
            ) : (
                <table className="categories-table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th>Description</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>{customCategories.map(renderRow)}</tbody>
                </table>
            )}
        </div>
    );
};

export default Categories;