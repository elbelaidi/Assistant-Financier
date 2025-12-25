package com.example.assistantfinancier.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

@Service
public class AIModelService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    
    // Hugging Face API key - Get free key from https://huggingface.co/settings/tokens
    private final String apiKey = System.getenv("HUGGING_FACE_API_KEY") != null ? System.getenv("HUGGING_FACE_API_KEY") : "hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    
    // Using Mistral-7B-Instruct for multilingual financial advice
    private final String apiUrl = "https://router.huggingface.co/hf-inference/models/mistralai/Mistral-7B-Instruct-v0.2";

    public String processNLP(String requete, String language) {
        System.out.println("=== DEBUG AI: Processing query: " + requete + ", language: " + language);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Create language-specific prompt
        String prompt = buildPrompt(requete, language);
        System.out.println("=== DEBUG AI: Prompt: " + prompt);
        
        String requestBody = String.format(
            "{\"inputs\": \"%s\", \"parameters\": {\"max_new_tokens\": 250, \"temperature\": 0.7, \"return_full_text\": false}}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            String responseBody = response.getBody();
            System.out.println("=== DEBUG AI: Raw response: " + responseBody);
            
            if (responseBody != null) {
                // Parse JSON response
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // Handle array response
                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    String generatedText = jsonNode.get(0).get("generated_text").asText();
                    System.out.println("=== DEBUG AI: Generated text: " + generatedText);
                    return cleanResponse(generatedText);
                }
                
                // Handle object response
                if (jsonNode.has("generated_text")) {
                    String generatedText = jsonNode.get("generated_text").asText();
                    return cleanResponse(generatedText);
                }
            }
            
            System.out.println("=== DEBUG AI: Using fallback advice");
            return generateFallbackAdvice(requete, language);
            
        } catch (Exception e) {
            System.err.println("=== ERROR AI: " + e.getMessage());
            e.printStackTrace();
            return generateFallbackAdvice(requete, language);
        }
    }
    
    private String buildPrompt(String requete, String language) {
        String languageName;
        String instruction;
        
        switch (language) {
            case "en":
                languageName = "English";
                instruction = "You are a financial advisor. Answer this question in English with practical financial advice";
                break;
            case "ar":
                languageName = "Arabic";
                instruction = "أنت مستشار مالي. أجب على هذا السؤال بالعربية مع نصائح مالية عملية";
                break;
            default:
                languageName = "French";
                instruction = "Vous êtes un conseiller financier. Répondez à cette question en français avec des conseils financiers pratiques";
        }
        
        return String.format("[INST] %s: %s [/INST]", instruction, requete);
    }
    
    private String cleanResponse(String response) {
        // Remove any instruction markers or artifacts
        response = response.trim();
        response = response.replaceAll("\\[/INST\\]", "");
        response = response.replaceAll("\\[INST\\]", "");
        return response;
    }
    
    private String generateFallbackAdvice(String requete, String language) {
        String query = requete.toLowerCase();
        
        // Detect topic
        String topic = "general";
        if (query.contains("économiser") || query.contains("épargne") || query.contains("save") || query.contains("saving") || query.contains("توفير") || query.contains("ادخار")) {
            topic = "savings";
        } else if (query.contains("prêt") || query.contains("emprunt") || query.contains("crédit") || query.contains("loan") || query.contains("قرض")) {
            topic = "loan";
        } else if (query.contains("budget") || query.contains("ميزانية")) {
            topic = "budget";
        } else if (query.contains("investir") || query.contains("invest") || query.contains("استثمار")) {
            topic = "invest";
        }
        
        // Return advice in appropriate language
        if ("en".equals(language)) {
            return getEnglishAdvice(topic);
        } else if ("ar".equals(language)) {
            return getArabicAdvice(topic);
        } else {
            return getFrenchAdvice(topic);
        }
    }

    // Text-to-Speech using Hugging Face TTS model
    public String generateSpeech(String text, String language) {
        System.out.println("=== DEBUG TTS: Generating speech for: " + text + ", language: " + language);
        System.out.println("=== DEBUG TTS: API Key available: " + (apiKey != null && !apiKey.contains("xxx")));

        // For Arabic, use a specific Arabic TTS model
        String ttsModel;
        if ("ar".equals(language)) {
            // Try different Arabic TTS models
            ttsModel = "facebook/mms-tts-ara"; // Arabic TTS model
            // Alternative: ttsModel = "microsoft/speecht5_tts"; // Generic model that might work
        } else if ("en".equals(language)) {
            ttsModel = "microsoft/speecht5_tts"; // English TTS
        } else {
            ttsModel = "microsoft/speecht5_tts"; // Default to English for French
        }

        String ttsUrl = "https://router.huggingface.co/hf-inference/models/" + ttsModel;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Prepare the request body for TTS
        String requestBody = String.format(
            "{\"inputs\": \"%s\"}",
            text.replace("\"", "\\\"").replace("\n", " ")
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(ttsUrl, entity, byte[].class);
            byte[] audioData = response.getBody();

            if (audioData != null && audioData.length > 0) {
                // Convert to base64 for transmission
                String base64Audio = Base64.getEncoder().encodeToString(audioData);
                System.out.println("=== DEBUG TTS: Generated audio, size: " + audioData.length + " bytes");
                return base64Audio;
            } else {
                System.out.println("=== DEBUG TTS: No audio data received");
                return null;
            }

        } catch (Exception e) {
            System.err.println("=== ERROR TTS: " + e.getMessage());
            e.printStackTrace();
            // Return a special marker to indicate server TTS failed
            return "SERVER_TTS_FAILED";
        }
    }

    private String getFrenchAdvice(String topic) {
        switch (topic) {
            case "savings":
                return "En tant que conseiller financier, voici mes recommandations pour épargner efficacement:\n\n" +
                       "Premièrement, établissez un budget détaillé en listant toutes vos sources de revenus et dépenses mensuelles. Cela vous permettra d'identifier où va votre argent.\n\n" +
                       "Deuxièmement, appliquez la règle du 50-30-20: allouez 50% de vos revenus aux besoins essentiels, 30% aux envies, et épargnez 20%. Si possible, mettez en place un virement automatique vers votre compte épargne dès réception de votre salaire.\n\n" +
                       "Troisièmement, créez un fonds d'urgence couvrant 3 à 6 mois de dépenses. Commencez par 500 à 1000 euros, puis augmentez progressivement.\n\n" +
                       "Enfin, réduisez les dépenses superflues: renégociez vos abonnements, comparez les prix, évitez les achats impulsifs. Chaque euro économisé est un euro investi dans votre avenir financier.";
            case "loan":
                return "En tant que conseiller financier, voici mes recommandations pour obtenir un prêt dans les meilleures conditions:\n\n" +
                       "Avant tout, vérifiez votre capacité d'emprunt. Vos mensualités ne doivent pas dépasser 33% de vos revenus nets. Calculez précisément ce que vous pouvez vous permettre de rembourser chaque mois.\n\n" +
                       "Ensuite, améliorez votre dossier: un bon historique de crédit est essentiel. Remboursez vos dettes existantes, évitez les découverts, et maintenez un taux d'endettement bas.\n\n" +
                       "Comparez les offres de plusieurs banques et établissements financiers. Les taux d'intérêt peuvent varier significativement. Négociez également les frais de dossier et l'assurance emprunteur.\n\n" +
                       "Préparez un dossier complet: bulletins de salaire des 3 derniers mois, avis d'imposition, relevés bancaires, justificatifs de domicile. Un dossier bien préparé augmente vos chances d'acceptation et améliore les conditions proposées.";
            case "budget":
                return "En tant que conseiller financier, voici comment établir et gérer efficacement votre budget:\n\n" +
                       "Commencez par faire un état des lieux complet. Listez tous vos revenus mensuels (salaires, allocations, revenus complémentaires) et toutes vos dépenses fixes (loyer, assurances, crédits) et variables (alimentation, loisirs, transport).\n\n" +
                       "Catégorisez vos dépenses en trois groupes: essentielles (logement, nourriture, santé), importantes (transport, téléphone), et non-essentielles (sorties, shopping). Cette classification vous aidera à prioriser.\n\n" +
                       "Fixez-vous des objectifs réalistes pour chaque catégorie. Utilisez la méthode des enveloppes ou une application de gestion budgétaire pour suivre vos dépenses en temps réel.\n\n" +
                       "Analysez mensuellement vos dépenses et ajustez votre budget. Identifiez les dépassements, cherchez des économies possibles, et récompensez-vous modérément quand vous respectez votre budget. La clé du succès est la régularité et l'adaptation progressive de vos habitudes.";
            case "invest":
                return "En tant que conseiller financier, voici mes recommandations pour débuter dans l'investissement:\n\n" +
                       "Avant d'investir, constituez impérativement une épargne de précaution équivalente à 3-6 mois de dépenses sur un compte facilement accessible. C'est votre filet de sécurité.\n\n" +
                       "Définissez clairement vos objectifs: court terme (moins de 3 ans), moyen terme (3-10 ans), ou long terme (plus de 10 ans). Votre horizon d'investissement déterminera votre stratégie.\n\n" +
                       "Diversifiez toujours vos placements pour réduire les risques. Ne mettez jamais tous vos œufs dans le même panier. Considérez différents types d'actifs: actions, obligations, immobilier, selon votre profil de risque.\n\n" +
                       "Pour débuter, privilégiez les placements réglementés et peu risqués comme le Livret A, le LDDS, ou l'assurance-vie en euros. Une fois à l'aise, vous pourrez explorer les marchés financiers. N'investissez jamais de l'argent dont vous pourriez avoir besoin à court terme, et n'hésitez pas à consulter un conseiller pour personnaliser votre stratégie.";
            default:
                return "En tant que conseiller financier, permettez-moi de vous guider:\n\n" +
                       "La santé financière repose sur trois piliers fondamentaux. Premièrement, un budget bien géré qui vous permet de contrôler vos dépenses et d'éviter le surendettement. Deuxièmement, une épargne régulière qui constitue votre sécurité financière. Troisièmement, une planification à long terme incluant la retraite et les projets importants.\n\n" +
                       "Je vous encourage à vous fixer des objectifs financiers clairs et réalistes. Commencez par les bases: éliminer les dettes à taux élevé, constituer un fonds d'urgence, puis développer votre patrimoine progressivement.\n\n" +
                       "N'hésitez pas à me poser des questions plus spécifiques sur l'épargne, les prêts, la gestion budgétaire, ou l'investissement. Je suis là pour vous accompagner dans votre réussite financière.";
        }
    }
    
    private String getEnglishAdvice(String topic) {
        switch (topic) {
            case "savings":
                return "As your financial advisor, here are my recommendations for effective saving:\n\n" +
                       "First, establish a detailed budget by listing all your income sources and monthly expenses. This will help you identify where your money goes.\n\n" +
                       "Second, apply the 50-30-20 rule: allocate 50% of your income to essential needs, 30% to wants, and save 20%. If possible, set up automatic transfers to your savings account as soon as you receive your salary.\n\n" +
                       "Third, create an emergency fund covering 3 to 6 months of expenses. Start with 500 to 1000 dollars, then gradually increase.\n\n" +
                       "Finally, reduce unnecessary expenses: renegotiate your subscriptions, compare prices, avoid impulse purchases. Every dollar saved is a dollar invested in your financial future.";
            case "loan":
                return "As your financial advisor, here are my recommendations for obtaining a loan under the best conditions:\n\n" +
                       "First, check your borrowing capacity. Your monthly payments should not exceed 33% of your net income. Calculate precisely what you can afford to repay each month.\n\n" +
                       "Next, improve your file: a good credit history is essential. Pay off existing debts, avoid overdrafts, and maintain a low debt ratio.\n\n" +
                       "Compare offers from multiple banks and financial institutions. Interest rates can vary significantly. Also negotiate processing fees and borrower insurance.\n\n" +
                       "Prepare a complete file: pay slips from the last 3 months, tax notices, bank statements, proof of residence. A well-prepared file increases your chances of acceptance and improves the proposed conditions.";
            case "budget":
                return "As your financial advisor, here's how to establish and manage your budget effectively:\n\n" +
                       "Start with a complete assessment. List all your monthly income (salaries, allowances, additional income) and all your fixed expenses (rent, insurance, credits) and variable expenses (food, leisure, transport).\n\n" +
                       "Categorize your expenses into three groups: essential (housing, food, health), important (transport, phone), and non-essential (outings, shopping). This classification will help you prioritize.\n\n" +
                       "Set realistic goals for each category. Use the envelope method or a budget management app to track your expenses in real time.\n\n" +
                       "Analyze your expenses monthly and adjust your budget. Identify overruns, look for possible savings, and reward yourself moderately when you stick to your budget. The key to success is regularity and gradual adaptation of your habits.";
            case "invest":
                return "As your financial advisor, here are my recommendations for getting started with investing:\n\n" +
                       "Before investing, you must build an emergency savings equivalent to 3-6 months of expenses in an easily accessible account. This is your safety net.\n\n" +
                       "Clearly define your goals: short term (less than 3 years), medium term (3-10 years), or long term (more than 10 years). Your investment horizon will determine your strategy.\n\n" +
                       "Always diversify your investments to reduce risk. Never put all your eggs in one basket. Consider different types of assets: stocks, bonds, real estate, according to your risk profile.\n\n" +
                       "To start, favor regulated and low-risk investments like savings accounts, CDs, or government bonds. Once comfortable, you can explore financial markets. Never invest money you might need in the short term, and don't hesitate to consult an advisor to personalize your strategy.";
            default:
                return "As your financial advisor, let me guide you:\n\n" +
                       "Financial health relies on three fundamental pillars. First, a well-managed budget that allows you to control expenses and avoid over-indebtedness. Second, regular savings that constitute your financial security. Third, long-term planning including retirement and important projects.\n\n" +
                       "I encourage you to set clear and realistic financial goals. Start with the basics: eliminate high-interest debt, build an emergency fund, then develop your wealth gradually.\n\n" +
                       "Feel free to ask me more specific questions about savings, loans, budget management, or investing. I'm here to support you in your financial success.";
        }
    }
    
    private String getArabicAdvice(String topic) {
        switch (topic) {
            case "savings":
                return "بصفتي مستشارك المالي، إليك توصياتي للادخار الفعال:\n\n" +
                       "أولاً، ضع ميزانية مفصلة من خلال سرد جميع مصادر دخلك ونفقاتك الشهرية. سيساعدك هذا على معرفة أين تذهب أموالك.\n\n" +
                       "ثانياً، طبق قاعدة 50-30-20: خصص 50٪ من دخلك للاحتياجات الأساسية، و30٪ للرغبات، وادخر 20٪. إذا أمكن، قم بإعداد تحويلات تلقائية إلى حساب التوفير الخاص بك بمجرد استلام راتبك.\n\n" +
                       "ثالثاً، أنشئ صندوق طوارئ يغطي 3 إلى 6 أشهر من النفقات. ابدأ بمبلغ 500 إلى 1000 دولار، ثم قم بالزيادة تدريجياً.\n\n" +
                       "أخيراً، قلل النفقات غير الضرورية: أعد التفاوض على اشتراكاتك، قارن الأسعار، تجنب المشتريات الاندفاعية. كل دولار تدخره هو دولار مستثمر في مستقبلك المالي.";
            case "loan":
                return "بصفتي مستشارك المالي، إليك توصياتي للحصول على قرض بأفضل الشروط:\n\n" +
                       "أولاً، تحقق من قدرتك على الاقتراض. يجب ألا تتجاوز أقساطك الشهرية 33٪ من دخلك الصافي. احسب بدقة ما يمكنك سداده كل شهر.\n\n" +
                       "بعد ذلك، حسن ملفك: يعد التاريخ الائتماني الجيد أمراً ضرورياً. سدد الديون الحالية، تجنب السحب على المكشوف، وحافظ على نسبة دين منخفضة.\n\n" +
                       "قارن العروض من عدة بنوك ومؤسسات مالية. يمكن أن تختلف أسعار الفائدة بشكل كبير. تفاوض أيضاً على رسوم المعالجة وتأمين المقترض.\n\n" +
                       "جهز ملفاً كاملاً: قسائم رواتب الأشهر الثلاثة الأخيرة، إشعارات الضرائب، كشوف حسابات بنكية، إثبات الإقامة. الملف المعد جيداً يزيد من فرص قبولك ويحسن الشروط المقترحة.";
            case "budget":
                return "بصفتي مستشارك المالي، إليك كيفية إنشاء وإدارة ميزانيتك بفعالية:\n\n" +
                       "ابدأ بتقييم كامل. اسرد جميع دخلك الشهري مثل الرواتب والبدلات والدخل الإضافي، وجميع نفقاتك الثابتة مثل الإيجار والتأمين والقروض، والنفقات المتغيرة مثل الطعام والترفيه والنقل.\n\n" +
                       "صنف نفقاتك إلى ثلاث مجموعات: أساسية مثل السكن والطعام والصحة، مهمة مثل النقل والهاتف، وغير أساسية مثل الخروج والتسوق. سيساعدك هذا التصنيف على تحديد الأولويات.\n\n" +
                       "ضع أهدافاً واقعية لكل فئة. استخدم طريقة المظاريف أو تطبيق إدارة الميزانية لتتبع نفقاتك في الوقت الفعلي.\n\n" +
                       "حلل نفقاتك شهرياً واضبط ميزانيتك. حدد الزيادات، ابحث عن توفيرات محتملة، وكافئ نفسك باعتدال عندما تلتزم بميزانيتك. مفتاح النجاح هو الانتظام والتكيف التدريجي لعاداتك.";
            case "invest":
                return "بصفتي مستشارك المالي، إليك توصياتي للبدء في الاستثمار:\n\n" +
                       "قبل الاستثمار، يجب عليك بناء مدخرات طوارئ تعادل 3 إلى 6 أشهر من النفقات في حساب يسهل الوصول إليه. هذه هي شبكة الأمان الخاصة بك.\n\n" +
                       "حدد أهدافك بوضوح: قصيرة الأجل أي أقل من 3 سنوات، متوسطة الأجل من 3 إلى 10 سنوات، أو طويلة الأجل أكثر من 10 سنوات. سيحدد أفق الاستثمار الخاص بك استراتيجيتك.\n\n" +
                       "قم دائماً بتنويع استثماراتك لتقليل المخاطر. لا تضع كل بيضك في سلة واحدة. ضع في اعتبارك أنواعاً مختلفة من الأصول مثل الأسهم والسندات والعقارات، وفقاً لملف المخاطر الخاص بك.\n\n" +
                       "للبدء، فضل الاستثمارات المنظمة ومنخفضة المخاطر مثل حسابات التوفير أو شهادات الإيداع أو السندات الحكومية. بمجرد أن تشعر بالراحة، يمكنك استكشاف الأسواق المالية. لا تستثمر أبداً أموالاً قد تحتاجها على المدى القصير، ولا تتردد في استشارة مستشار لتخصيص استراتيجيتك.";
            default:
                return "بصفتي مستشارك المالي، دعني أرشدك:\n\n" +
                       "تعتمد الصحة المالية على ثلاثة أعمدة أساسية. أولاً، ميزانية مدارة جيداً تتيح لك التحكم في النفقات وتجنب الإفراط في الديون. ثانياً، مدخرات منتظمة تشكل أمانك المالي. ثالثاً، تخطيط طويل الأجل يشمل التقاعد والمشاريع المهمة.\n\n" +
                       "أشجعك على تحديد أهداف مالية واضحة وواقعية. ابدأ بالأساسيات: التخلص من الديون ذات الفائدة المرتفعة، بناء صندوق طوارئ، ثم تطوير ثروتك تدريجياً.\n\n" +
                       "لا تتردد في طرح أسئلة أكثر تحديداً حول الادخار أو القروض أو إدارة الميزانية أو الاستثمار. أنا هنا لدعمك في نجاحك المالي.";
        }
    }
}