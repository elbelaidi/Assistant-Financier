// Translations for all UI elements
const translations = {
    fr: {
        title: 'Assistant Financier',
        subtitle: 'Votre conseiller vocal intelligent',
        conversationTitle: 'Conversation',
        welcomeText: 'Comment puis-je vous aider?',
        welcomeSubtext: 'Parlez ou tapez votre question financière',
        quickActionsTitle: 'Actions rapides',
        loanBtn: 'Prêt',
        savingsBtn: 'Épargne',
        budgetBtn: 'Budget',
        investBtn: 'Investir',
        micLabel: 'Parler',
        typeLabel: 'Taper',
        modalTitle: 'Tapez votre question',
        modalPlaceholder: 'Ex: Comment épargner de l\'argent?',
        cancelBtn: 'Annuler',
        sendBtn: 'Envoyer',
        loadingText: 'Traitement en cours...',
        loanQuery: 'Comment obtenir un prêt?',
        savingsQuery: 'Conseils d\'épargne',
        budgetQuery: 'Aide pour mon budget',
        investQuery: 'Comment investir?',
        listeningText: 'En écoute...',
        stopLabel: 'Arrêter',
        errorConnection: 'Erreur de connexion au serveur',
        errorMicrophone: 'Erreur: Microphone non disponible'
    },
    en: {
        title: 'Financial Assistant',
        subtitle: 'Your intelligent voice advisor',
        conversationTitle: 'Conversation',
        welcomeText: 'How can I help you?',
        welcomeSubtext: 'Speak or type your financial question',
        quickActionsTitle: 'Quick Actions',
        loanBtn: 'Loan',
        savingsBtn: 'Savings',
        budgetBtn: 'Budget',
        investBtn: 'Invest',
        micLabel: 'Speak',
        typeLabel: 'Type',
        modalTitle: 'Type your question',
        modalPlaceholder: 'Ex: How to save money?',
        cancelBtn: 'Cancel',
        sendBtn: 'Send',
        loadingText: 'Processing...',
        loanQuery: 'How to get a loan?',
        savingsQuery: 'Savings advice',
        budgetQuery: 'Help with my budget',
        investQuery: 'How to invest?',
        listeningText: 'Listening...',
        stopLabel: 'Stop',
        errorConnection: 'Connection error to server',
        errorMicrophone: 'Error: Microphone not available'
    },
    ar: {
        title: 'المساعد المالي',
        subtitle: 'مستشارك الصوتي الذكي',
        conversationTitle: 'المحادثة',
        welcomeText: 'كيف يمكنني مساعدتك؟',
        welcomeSubtext: 'تحدث أو اكتب سؤالك المالي',
        quickActionsTitle: 'إجراءات سريعة',
        loanBtn: 'قرض',
        savingsBtn: 'مدخرات',
        budgetBtn: 'ميزانية',
        investBtn: 'استثمار',
        micLabel: 'تحدث',
        typeLabel: 'اكتب',
        modalTitle: 'اكتب سؤالك',
        modalPlaceholder: 'مثال: كيف أوفر المال؟',
        cancelBtn: 'إلغاء',
        sendBtn: 'إرسال',
        loadingText: 'جاري المعالجة...',
        loanQuery: 'كيف أحصل على قرض؟',
        savingsQuery: 'نصائح الادخار',
        budgetQuery: 'مساعدة في الميزانية',
        investQuery: 'كيف أستثمر؟',
        listeningText: 'جاري الاستماع...',
        stopLabel: 'توقف',
        errorConnection: 'خطأ في الاتصال بالخادم',
        errorMicrophone: 'خطأ: الميكروفون غير متاح'
    }
};

function updateUILanguage(lang) {
    const t = translations[lang];
    if (!t) return;

    // Safely update elements with data-i18n attributes
    document.querySelectorAll('[data-i18n]').forEach(element => {
        if (!element) return; // Skip if element is null

        const key = element.getAttribute('data-i18n');
        if (t[key]) {
            try {
                if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
                    element.placeholder = t[key];
                } else {
                    element.textContent = t[key];
                }
            } catch (error) {
                console.warn(`Failed to update element with data-i18n="${key}":`, error);
            }
        }
    });

    // Update RTL direction for Arabic
    try {
        if (lang === 'ar') {
            document.body.classList.add('rtl');
            document.documentElement.setAttribute('dir', 'rtl');
            document.documentElement.setAttribute('lang', 'ar');
        } else {
            document.body.classList.remove('rtl');
            document.documentElement.setAttribute('dir', 'ltr');
            document.documentElement.setAttribute('lang', lang);
        }
    } catch (error) {
        console.warn('Failed to update document direction:', error);
    }
}
