import { useState, useEffect } from 'react';
import {
    getAccounts,
    createAccount,
    updateAccount,
    deleteAccount,
    getNetWorth,
} from '../services/accountService';
import type { AccountRequest, AccountResponse } from '../services/accountService';
import './Accounts.css';

/**
 * Accounts page — displays all financial accounts with balances,
 * total net worth, and controls to add, edit, and delete accounts.
 */
const Accounts = () => {
    const [accounts, setAccounts] = useState<AccountResponse[]>([]);
    const [netWorth, setNetWorth] = useState<number>(0);
    const [showModal, setShowModal] = useState(false);
    const [editingAccount, setEditingAccount] = useState<AccountResponse | null>(null);
    const [error, setError] = useState('');

    const [form, setForm] = useState<AccountRequest>({
        name: '',
        type: 'CHECKING',
        balance: 0,
        institution: '',
    });

    useEffect(() => {
        fetchData();
    }, []);

    /** Fetches accounts and net worth from the API. */
    const fetchData = async () => {
        try {
            const [accountsData, netWorthData] = await Promise.all([
                getAccounts(),
                getNetWorth(),
            ]);
            setAccounts(accountsData);
            setNetWorth(netWorthData);
        } catch (err) {
            setError('Failed to load accounts.');
        }
    };

    /** Opens the add account modal with a blank form. */
    const handleAdd = () => {
        setEditingAccount(null);
        setForm({ name: '', type: 'CHECKING', balance: 0, institution: '' });
        setShowModal(true);
    };

    /** Opens the edit modal pre-populated with the selected account's data. */
    const handleEdit = (account: AccountResponse) => {
        setEditingAccount(account);
        setForm({
            name: account.name,
            type: account.type,
            balance: account.balance,
            institution: account.institution || '',
        });
        setShowModal(true);
    };

    /** Submits the form to create or update an account. */
    const handleSubmit = async () => {
        try {
            if (editingAccount) {
                await updateAccount(editingAccount.accountId, form);
            } else {
                await createAccount(form);
            }
            setShowModal(false);
            fetchData();
        } catch (err) {
            setError('Failed to save account.');
        }
    };

    /** Deletes an account after confirmation. */
    const handleDelete = async (accountId: number) => {
        if (!window.confirm('Are you sure you want to delete this account?')) return;
        try {
            await deleteAccount(accountId);
            fetchData();
        } catch (err) {
            setError('Failed to delete account.');
        }
    };

    /** Returns a display label for each account type. */
    const getTypeLabel = (type: string) => {
        switch (type) {
            case 'CHECKING': return 'Checking';
            case 'SAVINGS': return 'Savings';
            case 'OTHER': return 'Other';
            default: return type;
        }
    };

    return (
        <div className="accounts-container">
            <div className="accounts-header">
                <h1>Accounts</h1>
                <button className="btn-add" onClick={handleAdd}>+ Add Account</button>
            </div>

            {error && <div className="error-message">{error}</div>}

            {/* Net Worth Banner */}
            <div className="net-worth-banner">
                <span className="net-worth-label">Total Net Worth</span>
                <span className="net-worth-value">
          ${netWorth.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </span>
            </div>

            {/* Account Cards */}
            {accounts.length === 0 ? (
                <div className="no-accounts">
                    <p>No accounts yet. Add one to get started.</p>
                </div>
            ) : (
                <div className="accounts-grid">
                    {accounts.map((account) => (
                        <div key={account.accountId} className="account-card">
                            <div className="account-card-header">
                                <div>
                                    <div className="account-name">{account.name}</div>
                                    {account.institution && (
                                        <div className="account-institution">{account.institution}</div>
                                    )}
                                </div>
                                <span className={`account-type-badge type-${account.type.toLowerCase()}`}>
                  {getTypeLabel(account.type)}
                </span>
                            </div>
                            <div className="account-balance">
                                ${account.balance.toLocaleString('en-US', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                            })}
                            </div>
                            <div className="account-actions">
                                <button className="btn-edit" onClick={() => handleEdit(account)}>Edit</button>
                                <button className="btn-delete" onClick={() => handleDelete(account.accountId)}>
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Add / Edit Modal */}
            {showModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>{editingAccount ? 'Edit Account' : 'Add Account'}</h2>

                        <label>Account Name</label>
                        <input
                            type="text"
                            value={form.name}
                            onChange={(e) => setForm({ ...form, name: e.target.value })}
                            placeholder="e.g. Chase Checking"
                        />

                        <label>Type</label>
                        <select
                            value={form.type}
                            onChange={(e) =>
                                setForm({ ...form, type: e.target.value as AccountRequest['type'] })
                            }
                        >
                            <option value="CHECKING">Checking</option>
                            <option value="SAVINGS">Savings</option>
                            <option value="OTHER">Other</option>
                        </select>

                        <label>Balance</label>
                        <input
                            type="number"
                            step="0.01"
                            value={form.balance}
                            onChange={(e) => setForm({ ...form, balance: parseFloat(e.target.value) })}
                        />

                        <label>Institution (optional)</label>
                        <input
                            type="text"
                            value={form.institution}
                            onChange={(e) => setForm({ ...form, institution: e.target.value })}
                            placeholder="e.g. Chase, Wells Fargo"
                        />

                        <div className="modal-actions">
                            <button className="btn-save" onClick={handleSubmit}>
                                {editingAccount ? 'Save Changes' : 'Add Account'}
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

export default Accounts;