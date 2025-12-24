// Speech Recognition and Text-to-Speech
let recognition = null;
let synthesis = window.speechSynthesis;
let isListening = false;
let voicesLoaded = false;

// Load voices
function loadVoices() {
    const voices = synthesis.getVoices();
    if (voices.length > 0) {
        voicesLoaded = true;
        console.log('Voices loaded:', voices.length);
        voices.forEach(voice => {
            console.log('- Voice:', voice.name, voice.lang, voice.localService ? '(local)' : '(remote)');
        });

        // Log Arabic-specific voices for debugging
        const arabicVoices = voices.filter(v => v.lang.includes('ar'));
        if (arabicVoices.length > 0) {
            console.log('Arabic voices available:', arabicVoices.map(v => `${v.name} (${v.lang})`));
        } else {
            console.warn('No Arabic voices found! This may cause issues with Arabic TTS.');
            console.log('All available voices:', voices.map(v => `${v.name} (${v.lang})`).join(', '));
        }
    }
}

// Load voices on startup and when they change
loadVoices();
if (synthesis.onvoiceschanged !== undefined) {
    synthesis.onvoiceschanged = loadVoices;
}

// Check speech synthesis support
if (!('speechSynthesis' in window)) {
    console.warn('Speech synthesis not supported in this browser');
    const statusEl = document.getElementById('speechStatus');
    if (statusEl) {
        statusEl.textContent = '⚠️ Speech synthesis not supported in this browser';
        statusEl.style.color = '#e74c3c';
    }
}

// Initialize Speech Recognition
function initSpeechRecognition() {
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        recognition = new SpeechRecognition();
        recognition.continuous = false;
        recognition.interimResults = false;
        
        recognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            console.log('Speech recognized:', transcript);
            sendQueryToBackend(transcript);
        };
        
        recognition.onerror = (event) => {
            console.error('Speech recognition error:', event.error);
            stopListening();
            const t = translations[currentLanguage];
            showMessage(t.errorMicrophone, 'assistant');
        };
        
        recognition.onend = () => {
            stopListening();
        };
        
        return true;
    }
    return false;
}

// Toggle Voice Input
function toggleVoiceInput() {
    if (!recognition) {
        if (!initSpeechRecognition()) {
            alert('Speech recognition not supported in this browser');
            return;
        }
    }
    
    if (isListening) {
        stopListening();
    } else {
        startListening();
    }
}

// Start Listening
function startListening() {
    const t = translations[currentLanguage];
    
    // Set language for speech recognition
    const locales = {
        'fr': 'fr-FR',
        'en': 'en-US',
        'ar': 'ar-SA'
    };
    
    recognition.lang = locales[currentLanguage];
    
    try {
        recognition.start();
        isListening = true;
        
        const micBtn = document.getElementById('micBtn');
        const micLabel = document.getElementById('micLabel');
        micBtn.classList.add('listening');
        micLabel.textContent = t.listeningText;
        
        console.log('Started listening in', currentLanguage);
    } catch (error) {
        console.error('Error starting speech recognition:', error);
    }
}

// Stop Listening
function stopListening() {
    if (recognition && isListening) {
        recognition.stop();
        isListening = false;
        
        const t = translations[currentLanguage];
        const micBtn = document.getElementById('micBtn');
        const micLabel = document.getElementById('micLabel');
        micBtn.classList.remove('listening');
        micLabel.textContent = t.micLabel;
    }
}

// Text to Speech
function speak(text) {
    console.log('speak() called with text:', text);
    console.log('Current language:', currentLanguage);

    // Check if speech synthesis is supported
    if (!('speechSynthesis' in window)) {
        console.error('Speech synthesis not supported in this browser');
        return;
    }

    // Cancel any ongoing speech
    synthesis.cancel();

    // Enable stop button
    const stopBtn = document.getElementById('stopBtn');
    if (stopBtn) {
        stopBtn.disabled = false;
        stopBtn.style.opacity = '1';
    }
    
    // Clean text for Arabic - remove problematic content for TTS
    if (currentLanguage === 'ar') {
        // Remove common French words
        text = text.replace(/voici/gi, '');

        // For Arabic, try to transliterate to make it easier for non-Arabic voices
        // This is a simple transliteration to help pronunciation
        const arabicToEnglish = {
            'ا': 'a', 'ب': 'b', 'ت': 't', 'ث': 'th', 'ج': 'j', 'ح': 'h', 'خ': 'kh',
            'د': 'd', 'ذ': 'dh', 'ر': 'r', 'ز': 'z', 'س': 's', 'ش': 'sh', 'ص': 's',
            'ض': 'd', 'ط': 't', 'ظ': 'z', 'ع': 'a', 'غ': 'gh', 'ف': 'f', 'ق': 'q',
            'ك': 'k', 'ل': 'l', 'م': 'm', 'ن': 'n', 'ه': 'h', 'و': 'w', 'ي': 'y'
        };

        // Simple transliteration for basic Arabic words
        let transliterated = '';
        for (let char of text) {
            transliterated += arabicToEnglish[char] || char;
        }

        console.log('Original Arabic text:', text);
        console.log('Transliterated text:', transliterated);

        // Use transliterated text for better pronunciation with non-Arabic voices
        text = transliterated;

        // Convert Latin numbers to words for better pronunciation
        text = text.replace(/\d/g, (digit) => {
            const numbers = ['zero', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine'];
            return numbers[parseInt(digit)] || digit;
        });

        // Clean up extra spaces
        text = text.replace(/\s+/g, ' ').trim();
        console.log('Final text for speech:', text);
    }
    
    // Wait for voices to load if not loaded yet
    if (!voicesLoaded) {
        console.log('Voices not loaded yet, waiting...');
        setTimeout(() => speak(text), 1000);
        return;
    }

    // Wait longer to ensure cancellation is complete and avoid interruption errors
    setTimeout(() => {
        const utterance = new SpeechSynthesisUtterance(text);
        
        // Set language
        const locales = {
            'fr': 'fr-FR',
            'en': 'en-US',
            'ar': 'ar-SA'
        };
        utterance.lang = locales[currentLanguage];
        
        // Get available voices and select the best one for the language
        const voices = synthesis.getVoices();
        let selectedVoice = null;
        
        console.log('Available voices:', voices.length);
        
        // Find a voice that matches the current language
        // For Arabic, try multiple locale variations
        const arabicLocales = ['ar-SA', 'ar-EG', 'ar-AE', 'ar'];
        
        if (currentLanguage === 'ar') {
            // Filter Arabic voices
            const arabicVoices = voices.filter(v => 
                v.lang.includes('ar') || 
                arabicLocales.some(locale => v.lang.startsWith(locale))
            );
            
            console.log('Found Arabic voices:', arabicVoices.map(v => `${v.name} (${v.lang})`));
            
            // Priority order: Google > Natural > Online > avoid Microsoft Edge (reads numbers in French)
            selectedVoice = arabicVoices.find(v => v.name.includes('Google')) ||
                           arabicVoices.find(v => v.name.includes('Natural') && !v.name.includes('Microsoft')) ||
                           arabicVoices.find(v => !v.localService && !v.name.includes('Microsoft')) ||
                           arabicVoices.find(v => !v.name.includes('Microsoft')) ||
                           arabicVoices[0];
            
            if (selectedVoice) {
                console.log('Selected Arabic voice:', selectedVoice.name, selectedVoice.lang);
            } else {
                console.warn('No Arabic voice found! Available voices:',
                    voices.map(v => `${v.name} (${v.lang})`).join(', '));

                // Fallback: Try to use any voice that might work with Arabic text
                // Some voices can handle Arabic even if not specifically Arabic
                const fallbackVoices = voices.filter(v =>
                    v.lang.includes('en') || v.lang.includes('fr') ||
                    v.name.toLowerCase().includes('female') ||
                    v.name.toLowerCase().includes('male')
                );

                if (fallbackVoices.length > 0) {
                    selectedVoice = fallbackVoices[0];
                    console.log('Using fallback voice for Arabic:', selectedVoice.name, selectedVoice.lang);
                } else {
                    // Last resort: use the first available voice
                    selectedVoice = voices[0];
                    console.log('Using first available voice for Arabic:', selectedVoice ? selectedVoice.name : 'none');
                }

                // If still no voice selected, force use the first voice
                if (!selectedVoice && voices.length > 0) {
                    selectedVoice = voices[0];
                    console.log('Forced to use first voice:', selectedVoice.name, selectedVoice.lang);
                }
            }
        } else {
            // For French and English
            for (let voice of voices) {
                if (voice.lang.startsWith(currentLanguage) || voice.lang === locales[currentLanguage]) {
                    selectedVoice = voice;
                    // Prefer native/local voices
                    if (voice.localService) {
                        break;
                    }
                }
            }
        }
        
        if (selectedVoice) {
            utterance.voice = selectedVoice;
            console.log('Using voice:', selectedVoice.name, selectedVoice.lang, selectedVoice.localService ? '(local)' : '(remote)');
        } else {
            console.warn('No specific voice found for', currentLanguage, 'using default voice');
            // Log available voices for debugging
            voices.forEach(v => console.log('- Available:', v.name, v.lang));

            // For Arabic, if no voice is found, try to force speak without a specific voice
            if (currentLanguage === 'ar') {
                console.log('Attempting to speak Arabic without specific voice');
            }
        }
        
        // Set voice parameters - slower for Arabic for better clarity
        utterance.rate = currentLanguage === 'ar' ? 0.75 : 0.85;
        utterance.pitch = 1.0;
        utterance.volume = 1.0;
        
        // Handle errors
        utterance.onerror = (event) => {
            console.error('Speech synthesis error:', event);
            console.error('Error details:', {
                error: event.error,
                utterance: {
                    text: utterance.text,
                    lang: utterance.lang,
                    voice: utterance.voice ? utterance.voice.name : 'none'
                }
            });
            // Disable stop button on error
            const stopBtn = document.getElementById('stopBtn');
            if (stopBtn) {
                stopBtn.disabled = true;
                stopBtn.style.opacity = '0.5';
            }
        };
        
        utterance.onstart = () => {
            console.log('Speech started successfully');
        };

        utterance.onend = () => {
            console.log('Speech finished normally');
            // Disable stop button
            const stopBtn = document.getElementById('stopBtn');
            if (stopBtn) {
                stopBtn.disabled = true;
                stopBtn.style.opacity = '0.5';
            }
        };
        
        console.log('Speaking in', currentLanguage + ':', text.substring(0, 50) + '...');
        console.log('Final utterance details:', {
            text: utterance.text,
            lang: utterance.lang,
            voice: utterance.voice ? utterance.voice.name : 'default',
            rate: utterance.rate,
            pitch: utterance.pitch,
            volume: utterance.volume
        });

        try {
            synthesis.speak(utterance);
            console.log('synthesis.speak() called successfully');
            console.log('Currently speaking:', synthesis.speaking);
            console.log('Pending utterances:', synthesis.pending);
        } catch (error) {
            console.error('Error calling synthesis.speak():', error);
        }
    }, 250);
}

// Stop speaking
function stopSpeaking() {
    synthesis.cancel();
    // Disable stop button
    const stopBtn = document.getElementById('stopBtn');
    if (stopBtn) {
        stopBtn.disabled = true;
        stopBtn.style.opacity = '0.5';
    }
}

// Test speech synthesis - call this from browser console
function testSpeech(text = "Hello world", lang = 'en-US') {
    console.log('=== TESTING SPEECH SYNTHESIS ===');
    console.log('Text:', text, 'Language:', lang);
    console.log('Testing speech synthesis...');

    if (!('speechSynthesis' in window)) {
        console.error('Speech synthesis not supported');
        return false;
    }

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang;

    const voices = synthesis.getVoices();
    console.log('Available voices:', voices.length);
    voices.forEach(v => console.log('- Voice:', v.name, v.lang));

    if (voices.length > 0) {
        const testVoice = voices.find(v => v.lang.startsWith(lang)) || voices[0];
        if (testVoice) {
            utterance.voice = testVoice;
            console.log('Using voice:', testVoice.name, testVoice.lang);
        } else {
            console.log('No suitable voice found, using default');
        }
    }

    utterance.onstart = () => console.log('Speech started');
    utterance.onend = () => console.log('Speech ended');
    utterance.onerror = (e) => console.error('Speech error:', e);

    try {
        synthesis.speak(utterance);
        console.log('speak() called successfully');
        return true;
    } catch (error) {
        console.error('Error calling speak():', error);
        return false;
    }
}
