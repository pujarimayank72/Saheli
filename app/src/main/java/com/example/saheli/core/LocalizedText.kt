package com.example.saheli.core

object LocalizedText {
    private val values = mapOf(
        "home" to listOf("Home", "होम", "হোম", "முகப்பு", "హోమ్", "होम"),
        "health" to listOf("Health", "स्वास्थ्य", "স্বাস্থ্য", "ஆரோக்கியம்", "ఆరోగ్యం", "आरोग्य"),
        "ask" to listOf("Ask", "पूछें", "জিজ্ঞাসা", "கேள்", "అడగండి", "विचारा"),
        "rights" to listOf("Rights", "अधिकार", "অধিকার", "உரிமைகள்", "హక్కులు", "हक्क"),
        "support" to listOf("Support", "सहायता", "সহায়তা", "ஆதரவு", "సహాయం", "आधार"),
        "settings" to listOf("Settings", "सेटिंग्स", "সেটিংস", "அமைப்புகள்", "సెట్టింగ్స్", "सेटिंग्ज"),
        "hello" to listOf("Namaste", "नमस्ते", "নমস্কার", "வணக்கம்", "నమస్తే", "नमस्कार"),
        "privateReady" to listOf("Private chat is ready", "निजी चैट तैयार है", "প্রাইভেট চ্যাট প্রস্তুত", "தனிப்பட்ட உரையாடல் தயார்", "ప్రైవేట్ చాట్ సిద్ధంగా ఉంది", "खाजगी चॅट तयार आहे"),
        "serverReady" to listOf("Private chat is ready", "निजी चैट तैयार है", "প্রাইভেট চ্যাট প্রস্তুত", "தனிப்பட்ட உரையாடல் தயார்", "ప్రైవేట్ చాట్ సిద్ధంగా ఉంది", "खाजगी चॅट तयार आहे"),
        "preparing" to listOf("Preparing Saheli", "Saheli तैयार हो रही है", "Saheli প্রস্তুত হচ্ছে", "Saheli தயாராகிறது", "Saheli సిద్ధమవుతోంది", "Saheli तयार होत आहे"),
        "autoSetup" to listOf("Saheli is choosing the safest AI mode for this phone.", "Saheli इस फोन के लिए सुरक्षित AI मोड चुन रही है।", "Saheli এই ফোনের জন্য নিরাপদ AI মোড বেছে নিচ্ছে।", "இந்த போனுக்கான பாதுகாப்பான AI முறையை Saheli தேர்வு செய்கிறது.", "ఈ ఫోన్ కోసం Saheli సురక్షిత AI మోడ్ ఎంచుకుంటోంది.", "Saheli या फोनसाठी सुरक्षित AI मोड निवडत आहे."),
        "quickCounselling" to listOf("Quick Counselling", "त्वरित परामर्श", "দ্রুত কাউন্সেলিং", "விரைவு ஆலோசனை", "త్వరిత కౌన్సెలింగ్", "झटपट समुपदेशन"),
        "talk" to listOf("Talk to Saheli", "Saheli से बात करें", "Saheli-র সাথে কথা বলুন", "Saheli-யுடன் பேசுங்கள்", "Saheli తో మాట్లాడండి", "Saheli शी बोला"),
        "askAnything" to listOf("Ask Anything", "कुछ भी पूछें", "যে কোনো প্রশ্ন", "எதையும் கேள்", "ఏదైనా అడగండి", "काहीही विचारा"),
        "chatPlaceholder" to listOf("Ask Saheli privately...", "Saheli से निजी तौर पर पूछें...", "Saheli-কে ব্যক্তিগতভাবে জিজ্ঞাসা করুন...", "தனிப்பட்ட முறையில் Saheli-யிடம் கேளுங்கள்...", "Saheliని ప్రైవేట్‌గా అడగండి...", "Saheli ला खाजगीत विचारा..."),
        "language" to listOf("Language", "भाषा", "ভাষা", "மொழி", "భాష", "भाषा"),
        "profile" to listOf("Profile", "प्रोफाइल", "প্রোফাইল", "சுயவிவரம்", "ప్రొఫైల్", "प्रोफाइल"),
        "modelStatus" to listOf("AI model status", "AI मॉडल स्थिति", "AI মডেলের অবস্থা", "AI மாதிரி நிலை", "AI మోడల్ స్థితి", "AI मॉडेल स्थिती"),
        "changeLanguage" to listOf("Change language", "भाषा बदलें", "ভাষা বদলান", "மொழி மாற்று", "భాష మార్చండి", "भाषा बदला"),
        "importOptional" to listOf("Import local model file", "लोकल मॉडल फाइल जोड़ें", "লোকাল মডেল ফাইল যোগ করুন", "உள்ளூர் மாதிரி கோப்பை சேர்க்கவும்", "లోకల్ మోడల్ ఫైల్ జోడించండి", "लोकल मॉडेल फाइल जोडा"),
        "modelAutomatic" to listOf("Model setup is automatic. High-RAM phones use local Gemma; lower-RAM phones use the secure NGO server.", "मॉडल सेटअप अपने-आप होता है। ज्यादा RAM वाले फोन लोकल Gemma चलाते हैं; कम RAM वाले सुरक्षित NGO सर्वर का उपयोग करते हैं।", "মডেল সেটআপ স্বয়ংক্রিয়। বেশি RAM ফোন লোকাল Gemma ব্যবহার করে; কম RAM ফোন নিরাপদ NGO সার্ভার ব্যবহার করে।", "மாதிரி அமைப்பு தானாக நடக்கும். அதிக RAM போன்கள் உள்ளூர் Gemma-வைப் பயன்படுத்தும்; குறைந்த RAM போன்கள் பாதுகாப்பான NGO சேவையகத்தைப் பயன்படுத்தும்.", "మోడల్ సెటప్ ఆటోమేటిక్. ఎక్కువ RAM ఫోన్లు లోకల్ Gemma వాడతాయి; తక్కువ RAM ఫోన్లు సురక్షిత NGO సర్వర్ వాడతాయి.", "मॉडेल सेटअप आपोआप होतो. जास्त RAM फोन लोकल Gemma वापरतात; कमी RAM फोन सुरक्षित NGO सर्व्हर वापरतात."),
        "helpCounsellors" to listOf("Counsellors & helplines", "काउंसलर और हेल्पलाइन", "কাউন্সেলর ও হেল্পলাইন", "ஆலோசகர்கள் மற்றும் உதவி எண்கள்", "కౌన్సెలర్లు మరియు హెల్ప్‌లైన్‌లు", "समुपदेशक आणि हेल्पलाइन"),
        "helpClinics" to listOf("Nearby health support", "नज़दीकी स्वास्थ्य सहायता", "কাছের স্বাস্থ্য সহায়তা", "அருகிலுள்ள சுகாதார உதவி", "సమీప ఆరోగ్య సహాయం", "जवळची आरोग्य मदत"),
        "helpDocuments" to listOf("Documents checklist", "दस्तावेज़ सूची", "নথির তালিকা", "ஆவண சரிபார்ப்பு பட்டியல்", "పత్రాల జాబితా", "कागदपत्रांची यादी"),
        "helpLegal" to listOf("Emergency & legal aid", "आपातकाल और कानूनी मदद", "জরুরি ও আইনি সহায়তা", "அவசரம் மற்றும் சட்ட உதவி", "అత్యవసర మరియు న్యాయ సహాయం", "आपत्कालीन आणि कायदेशीर मदत"),
        "privacyHelp" to listOf("Saheli uses your saved city/state for this list. No exact location or personal details are sent anywhere.", "Saheli इस सूची के लिए आपका सेव किया हुआ शहर/राज्य इस्तेमाल करती है। आपकी सही लोकेशन या निजी जानकारी कहीं नहीं भेजी जाती।", "Saheli এই তালিকার জন্য আপনার সেভ করা শহর/রাজ্য ব্যবহার করে। সঠিক লোকেশন বা ব্যক্তিগত তথ্য কোথাও পাঠানো হয় না।", "இந்த பட்டியலுக்கு Saheli நீங்கள் சேமித்த நகரம்/மாநிலத்தை மட்டும் பயன்படுத்துகிறது. துல்லிய இடம் அல்லது தனிப்பட்ட தகவல் எங்கும் அனுப்பப்படாது.", "ఈ జాబితా కోసం Saheli మీరు సేవ్ చేసిన నగరం/రాష్ట్రం మాత్రమే ఉపయోగిస్తుంది. ఖచ్చితమైన లొకేషన్ లేదా వ్యక్తిగత వివరాలు ఎక్కడికీ పంపబడవు.", "या यादीसाठी Saheli तुमचे सेव्ह केलेले शहर/राज्य वापरते. अचूक लोकेशन किंवा वैयक्तिक माहिती कुठेही पाठवली जात नाही."),
        "call" to listOf("Call", "कॉल करें", "কল করুন", "அழைக்கவும்", "కాల్ చేయండి", "कॉल करा"),
        "close" to listOf("Close", "बंद करें", "বন্ধ করুন", "மூடு", "మూసివేయండి", "बंद करा")
    )

    fun t(language: Language, key: String): String {
        val index = when (language) {
            Language.English -> 0
            Language.Hindi -> 1
            Language.Bengali -> 2
            Language.Tamil -> 3
            Language.Telugu -> 4
            Language.Marathi -> 5
        }
        return values[key]?.getOrNull(index) ?: values[key]?.firstOrNull() ?: key
    }
}
