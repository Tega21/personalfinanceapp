import axios from 'axios';

const API_URL = 'http://localhost:8080/api/accounts';

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
};

export interface AccountRequest {
    name: string;
    type: 'CHECKING' | 'SAVINGS' | 'OTHER';
    balance: number;
    institution?: string;
}

export interface AccountResponse {
    accountId: number;
    name: string;
    type: 'CHECKING' | 'SAVINGS' | 'OTHER';
    balance: number;
    institution?: string;
}

/** Fetches all accounts for the authenticated user. */
export const getAccounts = async (): Promise<AccountResponse[]> => {
    const response = await axios.get(API_URL, { headers: getAuthHeader() });
    return response.data;
};

/** Creates a new account. */
export const createAccount = async (request: AccountRequest): Promise<AccountResponse> => {
    const response = await axios.post(API_URL, request, { headers: getAuthHeader() });
    return response.data;
};

/** Updates an existing account by ID. */
export const updateAccount = async (
    accountId: number,
    request: AccountRequest
): Promise<AccountResponse> => {
    const response = await axios.put(`${API_URL}/${accountId}`, request, {
        headers: getAuthHeader(),
    });
    return response.data;
};

/** Deletes an account by ID. */
export const deleteAccount = async (accountId: number): Promise<void> => {
    await axios.delete(`${API_URL}/${accountId}`, { headers: getAuthHeader() });
};

/** Fetches the total net worth for the authenticated user. */
export const getNetWorth = async (): Promise<number> => {
    const response = await axios.get(`${API_URL}/net-worth`, { headers: getAuthHeader() });
    return response.data;
};