// Authentication Functions

/**
 * Login user
 */
async function login(email, password, rememberMe = false) {
    try {
        const response = await fetch(API_ENDPOINTS.LOGIN, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        const data = await response.json();

        if (response.ok && data.token) {
            // Store token and user info
            const storage = rememberMe ? localStorage : sessionStorage;
            storage.setItem(STORAGE_KEYS.TOKEN, data.token);
            storage.setItem(STORAGE_KEYS.USER, JSON.stringify({
                email: data.email,
                nom: data.nom,
                role: data.role,
                languePreferee: data.languePreferee,
            }));

            if (rememberMe) {
                localStorage.setItem(STORAGE_KEYS.REMEMBER_ME, 'true');
            }

            // Set assistant language from user preference
            if (data.languePreferee && SUPPORTED_LANGUAGES.includes(data.languePreferee)) {
                setCurrentLanguage(data.languePreferee);
            }

            return { success: true, user: data };
        } else {
            return { success: false, error: data.error || 'Login failed' };
        }
    } catch (error) {
        console.error('Login error:', error);
        return { success: false, error: 'Network error' };
    }
}

/**
 * Register new user
 */
async function register(nom, email, password, telephone, languePreferee) {
    try {
        const response = await fetch(API_ENDPOINTS.REGISTER, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                nom,
                email,
                password,
                telephone,
                languePreferee
            }),
        });

        const data = await response.json();

        if (response.ok && data.token) {
            // Store token and user info
            sessionStorage.setItem(STORAGE_KEYS.TOKEN, data.token);
            sessionStorage.setItem(STORAGE_KEYS.USER, JSON.stringify({
                email: data.email,
                nom: data.nom,
                role: data.role,
                languePreferee: data.languePreferee,
            }));

            // Set assistant language from user preference
            if (data.languePreferee && SUPPORTED_LANGUAGES.includes(data.languePreferee)) {
                setCurrentLanguage(data.languePreferee);
            }

            return { success: true, user: data };
        } else {
            return { success: false, error: data.error || 'Registration failed' };
        }
    } catch (error) {
        console.error('Registration error:', error);
        return { success: false, error: 'Network error' };
    }
}

/**
 * Logout user
 */
function logout() {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    localStorage.removeItem(STORAGE_KEYS.REMEMBER_ME);
    sessionStorage.removeItem(STORAGE_KEYS.TOKEN);
    sessionStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = 'login.html';
}

/**
 * Get stored token
 */
function getToken() {
    return localStorage.getItem(STORAGE_KEYS.TOKEN) || 
           sessionStorage.getItem(STORAGE_KEYS.TOKEN);
}

/**
 * Get stored user info
 */
function getUserInfo() {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER) || 
                    sessionStorage.getItem(STORAGE_KEYS.USER);
    return userStr ? JSON.parse(userStr) : null;
}

/**
 * Check if user is authenticated
 */
function isAuthenticated() {
    return getToken() !== null;
}

/**
 * Require authentication - redirect to login if not authenticated
 */
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

/**
 * Get current user from API (validates token)
 */
async function getCurrentUser() {
    const token = getToken();
    if (!token) return null;
    
    try {
        const response = await fetch(API_ENDPOINTS.ME, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });
        
        if (response.ok) {
            return await response.json();
        } else {
            // Token invalid, logout
            logout();
            return null;
        }
    } catch (error) {
        console.error('Get current user error:', error);
        return null;
    }
}

/**
 * Check if user is admin
 */
function isAdmin() {
    const user = getUserInfo();
    return user && user.role === 'ADMIN';
}
