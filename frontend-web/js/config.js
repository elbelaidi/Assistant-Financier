// ================================
// API Configuration
// ================================

const API_BASE_URL = '';

const API_ENDPOINTS = {
    // Auth
    LOGIN: `${API_BASE_URL}/api/auth/login`,
    REGISTER: `${API_BASE_URL}/api/auth/register`,
    ME: `${API_BASE_URL}/api/auth/me`,

    // Financial advice
    CONSEIL: `${API_BASE_URL}/api/conseil`,

    // Future endpoints
    TRANSACTIONS: `${API_BASE_URL}/api/transactions`,
    HISTORY: `${API_BASE_URL}/api/conversations`,
};

// ================================
// Storage Keys
// ================================

const STORAGE_KEYS = {
    TOKEN: 'auth_token',
    USER: 'user_info',
    REMEMBER_ME: 'remember_me',

    // Assistant-only language
    ASSISTANT_LANGUAGE: 'assistant_language',
};

// ================================
// Assistant Language Management
// ================================

const SUPPORTED_LANGUAGES = ['fr', 'en', 'ar'];

/**
 * Returns the language selected inside the assistant UI.
 * No user preference, no browser detection.
 */
function getCurrentLanguage() {
    const lang = localStorage.getItem(STORAGE_KEYS.ASSISTANT_LANGUAGE);
    return SUPPORTED_LANGUAGES.includes(lang) ? lang : 'en'; // neutral default
}

/**
 * Sets assistant language (local to assistant only)
 */
function setCurrentLanguage(lang) {
    if (!SUPPORTED_LANGUAGES.includes(lang)) return;
    currentLanguage = lang;
    localStorage.setItem(STORAGE_KEYS.ASSISTANT_LANGUAGE, lang);
}

// Initialize assistant language
let currentLanguage = getCurrentLanguage();

// If user is already logged in, use their preferred language
const userInfo = getUserInfo();
if (userInfo && userInfo.languePreferee && SUPPORTED_LANGUAGES.includes(userInfo.languePreferee)) {
    currentLanguage = userInfo.languePreferee;
    localStorage.setItem(STORAGE_KEYS.ASSISTANT_LANGUAGE, currentLanguage);
}

// Set default dashboard language to French (but assistant can still switch)
const DASHBOARD_LANGUAGE = 'fr';

// ================================
// Backend URL (standalone / docker-safe)
// ================================

const BACKEND_URL =
    typeof API_BASE_URL !== 'undefined'
        ? `${API_BASE_URL}/api/conseil`
        : 'http://localhost:8081/api/conseil';

// ================================
// Authentication helpers (iframe-safe)
// ================================

function getToken() {
    const TOKEN_KEY = STORAGE_KEYS.TOKEN;

    // Try iframe storage first
    let token =
        localStorage.getItem(TOKEN_KEY) ||
        sessionStorage.getItem(TOKEN_KEY);

    // Try parent window if embedded
    if (!token && window !== window.parent) {
        try {
            token =
                window.parent.localStorage.getItem(TOKEN_KEY) ||
                window.parent.sessionStorage.getItem(TOKEN_KEY);
        } catch (e) {
            console.warn('Cannot access parent window token:', e);
        }
    }

    return token;
}

function getUserInfo() {
    const USER_KEY = STORAGE_KEYS.USER;

    let userStr =
        localStorage.getItem(USER_KEY) ||
        sessionStorage.getItem(USER_KEY);

    if (!userStr && window !== window.parent) {
        try {
            userStr =
                window.parent.localStorage.getItem(USER_KEY) ||
                window.parent.sessionStorage.getItem(USER_KEY);
        } catch (e) {
            console.warn('Cannot access parent window user info:', e);
        }
    }

    return userStr ? JSON.parse(userStr) : null;
}
