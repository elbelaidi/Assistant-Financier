// ================================
// App Initialization
// ================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('Assistant app initialized');
    console.log('Assistant language:', currentLanguage);

    // Language selector (assistant-only)
    const languageSelect = document.getElementById('languageSelect');
    if (languageSelect) {
        languageSelect.value = currentLanguage;

        languageSelect.addEventListener('change', (e) => {
            setCurrentLanguage(e.target.value);
            updateUILanguage(currentLanguage);
            stopSpeaking();
            stopListening();
        });
    }

    // Initialize UI
    updateUILanguage(currentLanguage);

    // Load conversation history only if not embedded
    const urlParams = new URLSearchParams(window.location.search);
    if (!urlParams.get('embedded')) {
        setTimeout(loadConversationHistory, 500);
    }
});

// ================================
// UI Helpers
// ================================

function showLoading(show) {
    const spinner = document.getElementById('loadingSpinner');
    if (!spinner) return;
    spinner.classList.toggle('hidden', !show);
}

function showTextInput() {
    const modal = document.getElementById('textInputModal');
    const textInput = document.getElementById('textInput');
    modal.classList.add('active');
    textInput.value = '';
    textInput.focus();
}

function closeTextInput() {
    document.getElementById('textInputModal').classList.remove('active');
}

// ================================
// Input Handling
// ================================

function sendTextInput() {
    const input = document.getElementById('textInput');
    const query = input.value.trim();
    if (!query) return;
    closeTextInput();
    sendQueryToBackend(query);
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        const modal = document.getElementById('textInputModal');
        if (modal && modal.classList.contains('active')) {
            e.preventDefault();
            sendTextInput();
        }
    }
});

// ================================
// Quick Actions
// ================================

function quickAction(type) {
    const t = translations[currentLanguage];
    if (!t) return;

    const queries = {
        loan: t.loanQuery,
        savings: t.savingsQuery,
        budget: t.budgetQuery,
        invest: t.investQuery,
    };

    if (queries[type]) {
        sendQueryToBackend(queries[type]);
    }
}

// ================================
// Conversation History
// ================================

async function loadConversationHistory() {
    const token = getToken();
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/conversations`, {
            headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) return;

        const conversations = await response.json();
        if (!conversations.length) return;

        document.getElementById('welcomeMessage')?.remove();

        const area = document.getElementById('conversationArea');
        area.querySelectorAll('.message').forEach(m => m.remove());

        conversations.slice(-10).forEach(c => {
            displayConversationMessage(c.query, 'user');
            displayConversationMessage(c.response, 'assistant');
        });

    } catch (err) {
        console.error('History load error:', err);
    }
}

// ================================
// Message Rendering
// ================================

function displayConversationMessage(text, sender) {
    const area = document.getElementById('conversationArea');
    const msg = document.createElement('div');
    msg.className = `message message-${sender}`;

    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';
    bubble.textContent = text;

    msg.appendChild(bubble);
    area.appendChild(msg);
    area.scrollTop = area.scrollHeight;
}

function showMessage(text, sender) {
    document.getElementById('welcomeMessage')?.remove();
    displayConversationMessage(text, sender);
}

// ================================
// Backend Communication
// ================================

async function sendQueryToBackend(query) {
    console.log('Sending query:', query);
    console.log('Assistant language:', currentLanguage);

    showMessage(query, 'user');
    showLoading(true);

    try {
        const headers = { 'Content-Type': 'application/json; charset=utf-8' };
        const token = getToken();
        if (token) headers.Authorization = `Bearer ${token}`;

        const response = await fetch(BACKEND_URL, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                query,
                language: currentLanguage,
            }),
        });

        if (!response.ok) {
            showMessage(`Error ${response.status}`, 'assistant');
            return;
        }

        const answer = await response.text();
        showMessage(answer, 'assistant');

        // Conversation is already saved by the backend with proper categorization

        generateAndPlaySpeech(answer, currentLanguage);

    } catch (err) {
        console.error(err);
        showMessage(translations[currentLanguage].errorConnection, 'assistant');
    } finally {
        showLoading(false);
    }
}

// ================================
// TTS (Server First, Browser Fallback)
// ================================

async function generateAndPlaySpeech(text, language) {
    try {
        const headers = { 'Content-Type': 'application/json; charset=utf-8' };
        const token = getToken();
        if (token) headers.Authorization = `Bearer ${token}`;

        const response = await fetch(`${API_BASE_URL}/api/tts`, {
            method: 'POST',
            headers,
            body: JSON.stringify({ text, language }),
        });

        if (!response.ok) throw new Error('TTS failed');

        const audio = await response.text();
        if (audio === 'SERVER_TTS_FAILED') return speak(text);

        playBase64Audio(audio);

    } catch {
        speak(text);
    }
}

// ================================
// Audio Helpers
// ================================

function playBase64Audio(base64) {
    const bytes = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
    const blob = new Blob([bytes], { type: 'audio/wav' });
    const url = URL.createObjectURL(blob);
    const audio = new Audio(url);
    audio.onended = () => URL.revokeObjectURL(url);
    audio.play();
}

// ================================
// Conversation Persistence
// ================================

// Conversations are now saved by the backend with proper categorization
