import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:speech_to_text/speech_to_text.dart' as stt;
import 'package:flutter_tts/flutter_tts.dart';
import 'dart:convert';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Assistant Financier Vocal',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.teal,
        scaffoldBackgroundColor: Colors.grey[50],
        fontFamily: 'Roboto',
        cardTheme: CardThemeData(
          elevation: 4,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        ),
      ),
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with SingleTickerProviderStateMixin {
  stt.SpeechToText _speech = stt.SpeechToText();
  FlutterTts _flutterTts = FlutterTts();
  bool _isListening = false;
  bool _isLoading = false;
  String _text = '';
  String _response = '';
  String _selectedLanguage = 'fr';
  late AnimationController _animationController;

  final Map<String, String> _languages = {
    'fr': 'Français',
    'en': 'English',
    'ar': 'العربية',
  };

  final Map<String, Map<String, String>> _translations = {
    'fr': {
      'title': 'Assistant Financier',
      'subtitle': 'Votre conseiller vocal intelligent',
      'howCanHelp': 'Comment puis-je vous aider?',
      'speakOrType': 'Parlez ou tapez votre question financière',
      'conversation': 'Conversation',
      'startMic': 'Appuyez sur le microphone pour commencer',
      'quickActions': 'Actions rapides',
      'loan': 'Prêt',
      'savings': 'Épargne',
      'budget': 'Budget',
      'invest': 'Investir',
      'listen': 'Écouter',
      'type': 'Taper',
      'typeQuestion': 'Tapez votre question',
      'example': 'Ex: Comment épargner de l\'argent?',
      'cancel': 'Annuler',
      'send': 'Envoyer',
      'loanQuery': 'Comment obtenir un prêt?',
      'savingsQuery': 'Conseils d\'épargne',
      'budgetQuery': 'Aide pour mon budget',
      'investQuery': 'Comment investir?',
    },
    'en': {
      'title': 'Financial Assistant',
      'subtitle': 'Your intelligent voice advisor',
      'howCanHelp': 'How can I help you?',
      'speakOrType': 'Speak or type your financial question',
      'conversation': 'Conversation',
      'startMic': 'Press the microphone to start',
      'quickActions': 'Quick Actions',
      'loan': 'Loan',
      'savings': 'Savings',
      'budget': 'Budget',
      'invest': 'Invest',
      'listen': 'Listen',
      'type': 'Type',
      'typeQuestion': 'Type your question',
      'example': 'Ex: How to save money?',
      'cancel': 'Cancel',
      'send': 'Send',
      'loanQuery': 'How to get a loan?',
      'savingsQuery': 'Savings advice',
      'budgetQuery': 'Help with my budget',
      'investQuery': 'How to invest?',
    },
    'ar': {
      'title': 'المساعد المالي',
      'subtitle': 'مستشارك الصوتي الذكي',
      'howCanHelp': 'كيف يمكنني مساعدتك؟',
      'speakOrType': 'تحدث أو اكتب سؤالك المالي',
      'conversation': 'المحادثة',
      'startMic': 'اضغط على الميكروفون للبدء',
      'quickActions': 'إجراءات سريعة',
      'loan': 'قرض',
      'savings': 'مدخرات',
      'budget': 'ميزانية',
      'invest': 'استثمار',
      'listen': 'استمع',
      'type': 'اكتب',
      'typeQuestion': 'اكتب سؤالك',
      'example': 'مثال: كيف أوفر المال؟',
      'cancel': 'إلغاء',
      'send': 'إرسال',
      'loanQuery': 'كيف أحصل على قرض؟',
      'savingsQuery': 'نصائح الادخار',
      'budgetQuery': 'مساعدة في الميزانية',
      'investQuery': 'كيف أستثمر؟',
    },
  };

  String _t(String key) {
    return _translations[_selectedLanguage]?[key] ?? _translations['fr']![key]!;
  }

  @override
  void initState() {
    super.initState();
    _speech.initialize();
    _animationController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 1500),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  void _listen() async {
    if (!_isListening) {
      bool available = await _speech.initialize();
      if (available) {
        setState(() => _isListening = true);
        
        // Get locale based on selected language
        String localeId = 'fr_FR';
        if (_selectedLanguage == 'en') {
          localeId = 'en_US';
        } else if (_selectedLanguage == 'ar') {
          localeId = 'ar_SA';  // Arabic - Saudi Arabia
        }
        
        _speech.listen(
          onResult: (val) {
            setState(() {
              _text = val.recognizedWords;
            });
            if (val.finalResult) {
              _sendToBackend(_text);
            }
          },
          localeId: localeId,
        );
      }
    } else {
      setState(() => _isListening = false);
      _speech.stop();
    }
  }

  void _sendToBackend(String query) async {
    if (query.isEmpty) return;
    
    setState(() => _isLoading = true);
    
    try {
      print('=== DEBUG: Sending request with language: $_selectedLanguage, query: $query');
      final response = await http.post(
        Uri.parse('http://backend:8081/api/conseil'),
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
        },
        body: jsonEncode({
          'query': query,
          'language': _selectedLanguage,
        }),
      );
      print('=== DEBUG: Response status: ${response.statusCode}, body: ${response.body}');
      
      if (response.statusCode == 200) {
        setState(() {
          _response = response.body;
          _isLoading = false;
        });
        _speak(_response);
      } else {
        setState(() {
          _response = 'Erreur: ${response.statusCode}. Vérifiez l\'authentification.';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _response = 'Erreur de connexion: $e';
        _isLoading = false;
      });
    }
  }

  void _speak(String text) async {
    String ttsLanguage = 'fr-FR';
    if (_selectedLanguage == 'en') {
      ttsLanguage = 'en-US';
    } else if (_selectedLanguage == 'ar') {
      ttsLanguage = 'ar-SA';
    }
    await _flutterTts.setLanguage(ttsLanguage);
    await _flutterTts.speak(text);
  }

  void _clearConversation() {
    setState(() {
      _text = '';
      _response = '';
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Colors.teal[400]!, Colors.teal[50]!],
          ),
        ),
        child: SafeArea(
          child: Column(
            children: [
              _buildHeader(),
              Expanded(
                child: SingleChildScrollView(
                  padding: EdgeInsets.all(20),
                  child: Column(
                    children: [
                      _buildWelcomeCard(),
                      SizedBox(height: 20),
                      _buildConversationCard(),
                      SizedBox(height: 20),
                      _buildQuickActions(),
                    ],
                  ),
                ),
              ),
              _buildBottomControls(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: EdgeInsets.all(20),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                _t('title'),
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
              Text(
                _t('subtitle'),
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.white70,
                ),
              ),
            ],
          ),
          DropdownButton<String>(
            value: _selectedLanguage,
            dropdownColor: Colors.teal[700],
            icon: Icon(Icons.language, color: Colors.white),
            underline: Container(),
            style: TextStyle(color: Colors.white, fontSize: 16),
            items: _languages.entries.map((entry) {
              return DropdownMenuItem(
                value: entry.key,
                child: Text(entry.value),
              );
            }).toList(),
            onChanged: (value) {
              setState(() => _selectedLanguage = value!);
            },
          ),
        ],
      ),
    );
  }

  Widget _buildWelcomeCard() {
    return Card(
      child: Padding(
        padding: EdgeInsets.all(20),
        child: Row(
          children: [
            Container(
              padding: EdgeInsets.all(15),
              decoration: BoxDecoration(
                color: Colors.teal[50],
                shape: BoxShape.circle,
              ),
              child: Icon(Icons.mic, size: 30, color: Colors.teal),
            ),
            SizedBox(width: 15),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    _t('howCanHelp'),
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  SizedBox(height: 5),
                  Text(
                    _t('speakOrType'),
                    style: TextStyle(
                      fontSize: 14,
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildConversationCard() {
    return Card(
      child: Padding(
        padding: EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Conversation',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                if (_text.isNotEmpty || _response.isNotEmpty)
                  IconButton(
                    icon: Icon(Icons.clear, color: Colors.grey),
                    onPressed: _clearConversation,
                  ),
              ],
            ),
            SizedBox(height: 15),
            if (_text.isNotEmpty) ...[
              _buildMessageBubble(
                _text,
                isUser: true,
                icon: Icons.person,
              ),
              SizedBox(height: 15),
            ],
            if (_isLoading)
              Center(
                child: Padding(
                  padding: EdgeInsets.all(20),
                  child: CircularProgressIndicator(),
                ),
              )
            else if (_response.isNotEmpty)
              _buildMessageBubble(
                _response,
                isUser: false,
                icon: Icons.assistant,
              )
            else
              Center(
                child: Padding(
                  padding: EdgeInsets.all(30),
                  child: Text(
                    _t('startMic'),
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: Colors.grey[400],
                      fontSize: 16,
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageBubble(String message, {required bool isUser, required IconData icon}) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: isUser ? Colors.teal[100] : Colors.blue[100],
            shape: BoxShape.circle,
          ),
          child: Icon(icon, size: 20, color: isUser ? Colors.teal[700] : Colors.blue[700]),
        ),
        SizedBox(width: 10),
        Expanded(
          child: Container(
            padding: EdgeInsets.all(15),
            decoration: BoxDecoration(
              color: isUser ? Colors.teal[50] : Colors.blue[50],
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              message,
              style: TextStyle(fontSize: 15),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildQuickActions() {
    final actions = [
      {'icon': Icons.account_balance, 'labelKey': 'loan', 'queryKey': 'loanQuery'},
      {'icon': Icons.savings, 'labelKey': 'savings', 'queryKey': 'savingsQuery'},
      {'icon': Icons.calculate, 'labelKey': 'budget', 'queryKey': 'budgetQuery'},
      {'icon': Icons.trending_up, 'labelKey': 'invest', 'queryKey': 'investQuery'},
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: EdgeInsets.symmetric(horizontal: 5, vertical: 10),
          child: Text(
            _t('quickActions'),
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Colors.teal[800],
            ),
          ),
        ),
        GridView.count(
          shrinkWrap: true,
          physics: NeverScrollableScrollPhysics(),
          crossAxisCount: 2,
          mainAxisSpacing: 15,
          crossAxisSpacing: 15,
          childAspectRatio: 1.5,
          children: actions.map((action) {
            final query = _t(action['queryKey'] as String);
            return InkWell(
              onTap: () {
                setState(() => _text = query);
                _sendToBackend(query);
              },
              child: Card(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(
                      action['icon'] as IconData,
                      size: 35,
                      color: Colors.teal,
                    ),
                    SizedBox(height: 8),
                    Text(
                      _t(action['labelKey'] as String),
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildBottomControls() {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black12,
            blurRadius: 10,
            offset: Offset(0, -5),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _buildControlButton(
            icon: Icons.volume_up,
            label: _t('listen'),
            onPressed: _response.isNotEmpty ? () => _speak(_response) : null,
          ),
          ScaleTransition(
            scale: _isListening
                ? Tween(begin: 1.0, end: 1.2).animate(_animationController)
                : AlwaysStoppedAnimation(1.0),
            child: FloatingActionButton.large(
              onPressed: _listen,
              backgroundColor: _isListening ? Colors.red : Colors.teal,
              child: Icon(
                _isListening ? Icons.mic : Icons.mic_none,
                size: 35,
              ),
            ),
          ),
          _buildControlButton(
            icon: Icons.text_fields,
            label: _t('type'),
            onPressed: () => _showTextInputDialog(),
          ),
        ],
      ),
    );
  }

  Widget _buildControlButton({
    required IconData icon,
    required String label,
    required VoidCallback? onPressed,
  }) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        IconButton(
          icon: Icon(icon),
          iconSize: 30,
          color: onPressed != null ? Colors.teal : Colors.grey,
          onPressed: onPressed,
        ),
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            color: onPressed != null ? Colors.teal[700] : Colors.grey,
          ),
        ),
      ],
    );
  }

  void _showTextInputDialog() {
    TextEditingController controller = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(_t('typeQuestion')),
        content: TextField(
          controller: controller,
          decoration: InputDecoration(
            hintText: _t('example'),
            border: OutlineInputBorder(),
          ),
          maxLines: 3,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text(_t('cancel')),
          ),
          ElevatedButton(
            onPressed: () {
              setState(() => _text = controller.text);
              _sendToBackend(controller.text);
              Navigator.pop(context);
            },
            child: Text(_t('send')),
          ),
        ],
      ),
    );
  }
}