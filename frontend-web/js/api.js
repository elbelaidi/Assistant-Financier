// API Client with Authentication

/**
 * Make authenticated API request
 */
async function apiRequest(url, options = {}) {
    const token = getToken();
    
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
        const response = await fetch(url, {
            ...options,
            headers,
        });
        
        // Handle 401 - unauthorized
        if (response.status === 401) {
            logout();
            throw new Error('Unauthorized');
        }
        
        const data = await response.json();
        
        return {
            ok: response.ok,
            status: response.status,
            data,
        };
    } catch (error) {
        console.error('API request error:', error);
        throw error;
    }
}

/**
 * Send financial advice query
 */
async function sendFinancialQuery(query, language) {
    return apiRequest(API_ENDPOINTS.CONSEIL, {
        method: 'POST',
        body: JSON.stringify({ query, language }),
    });
}

/**
 * Get user transactions
 */
async function getTransactions(page = 0, size = 10) {
    return apiRequest(`${API_ENDPOINTS.TRANSACTIONS}?page=${page}&size=${size}`);
}

/**
 * Get conversation history
 */
async function getConversationHistory(page = 0, size = 20) {
    return apiRequest(`${API_ENDPOINTS.HISTORY}?page=${page}&size=${size}`);
}
