package com.example.service

import com.example.BuildConfig
import com.example.model.AiGenerationOption
import com.example.model.BrandProfile
import com.example.model.SceneItem
import com.example.model.NarrationCue
import com.example.model.TextOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Result bundle from the AI script and scene plan generator
 */
data class AiVideoPlan(
    val titleBn: String,
    val conceptBn: String,
    val scenes: List<SceneItem>,
    val narrationCues: List<NarrationCue>,
    val textOverlays: List<TextOverlay>,
    val suggestedMusicGenre: String,
    val totalDurationSeconds: Int
)

class AiScriptGeneratorService {

    /**
     * Generates a culturally rich, authentic Bengali culinary video script and scene timeline.
     * Uses Gemini 3.5 Flash via REST API if GEMINI_API_KEY is available,
     * or produces a rich contextual fallback tailored to "অন্বেষার রসনা বিলাস".
     */
    suspend fun generateScriptAndScenes(
        userPrompt: String,
        option: AiGenerationOption,
        durationSeconds: Int,
        brandProfile: BrandProfile,
        uploadedMediaCount: Int
    ): AiVideoPlan = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidKey = apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")

        if (hasValidKey) {
            try {
                return@withContext callGeminiApi(
                    apiKey = apiKey,
                    prompt = userPrompt,
                    option = option,
                    durationSeconds = durationSeconds,
                    brand = brandProfile,
                    uploadedMediaCount = uploadedMediaCount
                )
            } catch (e: Exception) {
                // If API times out or rate limits, gracefully fall back to domain generator
            }
        }

        // Generate tailored Bengali culinary story plan
        return@withContext generateLocalArtisticPlan(
            userPrompt = userPrompt,
            option = option,
            durationSeconds = durationSeconds,
            brand = brandProfile,
            uploadedMediaCount = uploadedMediaCount
        )
    }

    private fun callGeminiApi(
        apiKey: String,
        prompt: String,
        option: AiGenerationOption,
        durationSeconds: Int,
        brand: BrandProfile,
        uploadedMediaCount: Int
    ): AiVideoPlan {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 30000
            readTimeout = 30000
        }

        val sceneCount = (durationSeconds / 8).coerceIn(3, 12)

        val systemInstruction = """
            You are an expert Bengali culinary film director and advertising scriptwriter for "${brand.businessName}".
            Tagline: "${brand.tagline}".
            Contact: ${brand.contactPhone}, Web: ${brand.website}.
            Generate natural West Bengal Bengali (প্রমিত পশ্চিমবঙ্গীয় বাংলা - কোলকাতা/হুগলী মিষ্ট উচ্চারণ) female voice-over.
            Mode: ${option.name} (${option.titleBn}).
            Target Duration: $durationSeconds seconds across $sceneCount scenes.
            Output ONLY valid JSON without markdown fences.
            JSON structure:
            {
               "titleBn": "...",
               "conceptBn": "...",
               "suggestedMusic": "Traditional Bengali Baul Flute & Sitar / Melodic Sarod",
               "scenes": [
                 {
                   "sceneNumber": 1,
                   "titleBn": "...",
                   "visualDescription": "...",
                   "narrationScriptBn": "...",
                   "onScreenTextBn": "...",
                   "durationSeconds": 8
                 }
               ]
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", "Prompt: $prompt. Duration: $durationSeconds seconds. Mode: ${option.code}.")
                }))
            }))
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", systemInstruction)
                }))
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonRequest.toString()) }

        val responseCode = conn.responseCode
        if (responseCode == 200) {
            val respString = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val root = JSONObject(respString)
            val textContent = root.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val parsedJson = JSONObject(textContent.replace("```json", "").replace("```", "").trim())
            return parsePlanFromJson(parsedJson, durationSeconds, option)
        } else {
            throw RuntimeException("Gemini API HTTP $responseCode")
        }
    }

    private fun parsePlanFromJson(
        json: JSONObject,
        totalDuration: Int,
        option: AiGenerationOption
    ): AiVideoPlan {
        val titleBn = json.optString("titleBn", "বাঙালিয়ানার সেরা রান্না")
        val conceptBn = json.optString("conceptBn", "অন্বেষার রসনা বিলাসের ঘরোয়া খাঁটি স্বাদ")
        val music = json.optString("suggestedMusic", "বাংলার বাঁশি ও সেতারের শান্ত মেলোডি")

        val scenesArray = json.optJSONArray("scenes") ?: JSONArray()
        val scenes = mutableListOf<SceneItem>()
        val cues = mutableListOf<NarrationCue>()
        val overlays = mutableListOf<TextOverlay>()

        var currentSecond = 0f
        for (i in 0 until scenesArray.length()) {
            val scObj = scenesArray.getJSONObject(i)
            val sec = scObj.optInt("durationSeconds", 8)
            val scTitle = scObj.optString("titleBn", "দৃশ্য ${i + 1}")
            val visual = scObj.optString("visualDescription", "আসল খাঁটি বাঙালি পদ পরিবেশন")
            val narration = scObj.optString("narrationScriptBn", "")
            val onScreen = scObj.optString("onScreenTextBn", "")

            scenes.add(
                SceneItem(
                    sceneNumber = i + 1,
                    titleBn = scTitle,
                    visualDescription = visual,
                    narrationScriptBn = narration,
                    onScreenTextBn = onScreen,
                    durationSeconds = sec,
                    isAiVisual = option != AiGenerationOption.OPTION_C
                )
            )

            if (narration.isNotBlank()) {
                cues.add(
                    NarrationCue(
                        startSeconds = currentSecond,
                        endSeconds = currentSecond + sec,
                        textBn = narration
                    )
                )
            }

            if (onScreen.isNotBlank()) {
                overlays.add(
                    TextOverlay(
                        textBn = onScreen,
                        startSeconds = currentSecond,
                        endSeconds = currentSecond + sec
                    )
                )
            }

            currentSecond += sec
        }

        return AiVideoPlan(
            titleBn = titleBn,
            conceptBn = conceptBn,
            scenes = scenes,
            narrationCues = cues,
            textOverlays = overlays,
            suggestedMusicGenre = music,
            totalDurationSeconds = totalDuration
        )
    }

    /**
     * Local artistic generator tailored for "অন্বেষার রসনা বিলাস"
     * Handles Bengali, English and mixed prompts without relying on cloud availability
     */
    fun generateLocalArtisticPlan(
        userPrompt: String,
        option: AiGenerationOption,
        durationSeconds: Int,
        brand: BrandProfile,
        uploadedMediaCount: Int
    ): AiVideoPlan {
        val pLower = userPrompt.lowercase()
        val isFishTheme = pLower.contains("fish") || pLower.contains("মাছ") || pLower.contains("কাতলা") || pLower.contains("ইলিশ")
        val isMuttonTheme = pLower.contains("mutton") || pLower.contains("মাংস") || pLower.contains("কষা") || pLower.contains("খাসি")
        val isThaliTheme = pLower.contains("thali") || pLower.contains("থালা") || pLower.contains("ভাত") || pLower.contains("ভোজ")
        val isSweetTheme = pLower.contains("sweet") || pLower.contains("মিষ্টি") || pLower.contains("রসগোল্লা") || pLower.contains("পায়েস")

        val titleBn: String
        val sceneTemplates: List<Triple<String, String, String>> // Title, Visual, VoiceScript

        when {
            isFishTheme -> {
                titleBn = "ইলিশ ও মাছের ঐতিহ্যের স্বাদ — অন্বেষার রসনা বিলাস"
                sceneTemplates = listOf(
                    Triple(
                        "তাজা মাছের চয়ন ও মশলা প্রস্তুতি",
                        "সকালের খাঁটি সর্ষে বাটা এবং হাতে পেষা সুবাসিত মশলার সান্নিধ্য",
                        "বাঙালির পাতে এক টুকরো খাঁটি তৃপ্তি। অন্বেষার রসনা বিলাসে শুরু হলো আজকের সুস্বাদু আয়োজন।"
                    ),
                    Triple(
                        "কড়াইয়ে মৃদু আঁচে কষা",
                        "মাটির উনুনে পিতলের কড়াইয়ে সোনালী তেলে ফোড়নের সুবাস",
                        "মৃদু আঁচে খাঁটি সর্ষের তেলে ফোড়ন আর মশলার মেলবন্ধন; যে সুবাস ঘরের পুরোনো দিন মনে করিয়ে দেয়।"
                    ),
                    Triple(
                        "রসালো ঝোলের চূড়ান্ত রূপ",
                        "ধোঁয়া ওঠা ভাপ ও কাঁচা লঙ্কার চেরা সুবাসে পরিপূর্ণ মাছের পদ",
                        "না কোনো কৃত্রিম রং, না কোনো আপোষ। শুধু মন ছুঁয়ে যাওয়া খাঁটি ঘরোয়া স্বাদ।"
                    ),
                    Triple(
                        "গরম ভাতে পরিবেশন",
                        "ঝরঝরে গরম চালের ভাত ও গন্ধরাজ লেবু সহযোগে অন্বেষার থালা",
                        "এক লোকমা মুখে নিলেই বুঝবেন—বাঙালিয়ানার আসল চেনা স্বাদ শুধু এখানেই।"
                    ),
                    Triple(
                        "ব্র্যান্ড সমাপ্তি ও আমন্ত্রণ",
                        "অন্বেষার রসনা বিলাসের নিজস্ব সিলমোহর ও যোগাযোগের ঠিকানা",
                        "বাঙালিয়ানার চেনা স্বাদ, ঘরের রান্নায় সেরা স্বাদ। আজই যোগাযোগ করুন অন্বেষার রসনা বিলাসে।"
                    )
                )
            }
            isMuttonTheme -> {
                titleBn = "রবিবারের খাসির মাংসের লাল ঝোল — স্মৃতি ও স্বাদ"
                sceneTemplates = listOf(
                    Triple(
                        "খাসির টুকরো ও দই-মশলার ম্যারিনেশন",
                        "ঘরোয়া খাঁটি মশলা, আদা-রসুন বাটা এবং খাঁটি সর্ষের তেলে ম্যারিনেশন",
                        "রবিবারের সেই চেনা দুপুর, আর রান্নাঘর থেকে ভেসে আসা কষা মাংসের অবিস্মরণীয় সুবাস।"
                    ),
                    Triple(
                        "ধীর আঁচে কষানো মাংস",
                        "কড়াইতে গাঢ় লালচে রঙের সুবাসিত গ্রেভি ও ভাজা সোনালী আলু",
                        "অন্বেষার রসনা বিলাসে প্রতিটি পদ তৈরি হয় নিখুঁত যত্নে, মা-ঠাকুমার চিরাচরিত রন্ধনশৈলীতে।"
                    ),
                    Triple(
                        "গরম মশলার ফোড়ন ও ঘিয়ের সুবাস",
                        "ঘরোয়া ঘিয়ের ফোড়নে ফুটে ওঠা সুস্বাদু ঝোল",
                        "আমাদের লক্ষ্য কেবল পেট ভরানো নয়, বাঙালির জিভের প্রতিটি তৃপ্তি পূরণ করা।"
                    ),
                    Triple(
                        "থালায় সাজানো রাজকীয় পরিবেশন",
                        "বাসমতী পোলাও অথবা গরম ভাতের সাথে কষা মাংসের রাজকীয় আয়োজন",
                        "ঘরের মত তৃপ্তি ও শুদ্ধতা নিয়ে অন্বেষার রসনা বিলাস সর্বদা আপনার পাশে।"
                    ),
                    Triple(
                        "ব্র্যান্ড কল টু অ্যাকশন",
                        "অন্বেষার লোগো এবং অর্ডারের নম্বর ও ওয়েবসাইট প্রদর্শন",
                        "অন্বেষার রসনা বিলাস—৮৫০৯৩৬০৩২৭ নম্বরে আজই অর্ডার করে উপভোগ করুন সেরা স্বাদ।"
                    )
                )
            }
            isThaliTheme -> {
                titleBn = "বাঙালির সম্পূর্ণ রসনা থালি — অন্বেষার রসনা বিলাস"
                sceneTemplates = listOf(
                    Triple(
                        "শাক-ভাজা ও ডালের শুভারম্ভ",
                        "শুকতো, ঝুড়ি আলুভাজা ও সোনা মুগের ডালের সুবাসিত সূচনা",
                        "পাত পেড়ে খাওয়ার সেই আদি ঐতিহ্য। অন্বেষার রসনা বিলাস নিয়ে এলো খাঁটি ভোজের ডালি।"
                    ),
                    Triple(
                        "ঘরোয়া তরকারি ও ছানার মহিমা",
                        "পোস্ত, এঁচোড় ও ধোঁকার ডালনার বিশুদ্ধ ঘরোয়া রন্ধন",
                        "খাঁটি তেলে ভাজা, বিশুদ্ধ উপাদানে প্রস্তুত—প্রতিটি পদে পাবেন মায়ের হাতের মমতাময়ী ছোঁয়া।"
                    ),
                    Triple(
                        "মাছ ও মাংসের রসালো মেলবন্ধন",
                        "রসালো পদগুলির বাটিতে সাজানো রাজকীয় থালি",
                        "কোনো রকম কৃত্রিম উপাদান ছাড়াই স্বাদ আর স্বাস্থ্যের অপূর্ব সমন্বয়।"
                    ),
                    Triple(
                        "চাটনি ও পায়েসের মিষ্ট সমাপ্তি",
                        "আমসত্ত্ব-খেজুরের চাটনি ও পায়েসের মিষ্টি মুখ",
                        "শেষ পাতে মধুর তৃপ্তি ছাড়া বাঙালির ভোজ সম্পূর্ণ হয় না।"
                    ),
                    Triple(
                        "আমাদের ব্র্যান্ডের বিশেষ অঙ্গীকার",
                        "অন্বেষার রসনা বিলাসের ঠিকানা ও খাঁটি অঙ্গীকার",
                        "বাঙালিয়ানার চেনা স্বাদ, ঘরের রান্নায় সেরা স্বাদ। অন্বেষার রসনা বিলাসে আপনাদের স্বাগত।"
                    )
                )
            }
            else -> {
                // Default rich culinary story
                titleBn = if (userPrompt.isNotBlank()) "অন্বেষার রসনা বিলাস — $userPrompt" else "অন্বেষার রসনা বিলাস — খাঁটি ঘরোয়া বাঙালিয়ানা"
                sceneTemplates = listOf(
                    Triple(
                        "রান্নাঘরের শুভ সূচনা",
                        "ভোরবেলার তাজা উপাদান ও খাঁটি মশলার সুগন্ধি আগমন",
                        "রান্না শুধুই কাজ নয়, আমাদের কাছে রান্না হলো এক পরম শিল্প ও ভালোবাসা।"
                    ),
                    Triple(
                        "ঐতিহ্যের নিপুণ রন্ধনশৈলী",
                        "ধীর আঁচে খাঁটি সর্ষের তেলে কষানো সুস্বাদু পদের রূপ",
                        "অন্বেষার রসনা বিলাসের প্রতিটি পদ রাঁধা হয় নিজস্ব পারিবারিক গোপন মশলার জাদুতে।"
                    ),
                    Triple(
                        "বিশুদ্ধতা ও টাটকা স্বাদের অঙ্গীকার",
                        "ধোঁয়া ওঠা গরম তরকারি ও মনোমুগ্ধকর পরিবেশন",
                        "আমরা বিশ্বাস করি খাঁটি মানেই সুস্বাদু—তাই পরিবেশনে কোনো আপোষ নেই।"
                    ),
                    Triple(
                        "রসনার পরম তৃপ্তি",
                        "কাঁসার থালায় সাজানো উষ্ণ ও রাজকীয় পদের সম্ভার",
                        "ঘরের রান্নার তৃপ্তি আর বাঙালিয়ানার পরিচিত আন্তরিকতাই আমাদের অহংকার।"
                    ),
                    Triple(
                        "ব্র্যান্ড বার্তা ও যোগাযোগ",
                        "অন্বেষার লোগো, ফোন নম্বর এবং ওয়েবসাইটের রূপরেখা",
                        "অন্বেষার রসনা বিলাস। যোগাযোগ: ৮৫০৯৩৬০৩২৭। ঘরের রান্নায় সেরা স্বাদ উপভোগ করুন আজই।"
                    )
                )
            }
        }

        // Calculate number of scenes based on requested duration
        val baseCount = (durationSeconds / 7).coerceIn(4, 10)
        val sceneDuration = durationSeconds / baseCount
        val scenes = mutableListOf<SceneItem>()
        val cues = mutableListOf<NarrationCue>()
        val overlays = mutableListOf<TextOverlay>()

        var currentSecond = 0f

        for (i in 0 until baseCount) {
            val template = sceneTemplates[i % sceneTemplates.size]
            val durationSec = if (i == baseCount - 1) durationSeconds - (i * sceneDuration) else sceneDuration
            val scNumber = i + 1

            val onScreenText = when (i % 5) {
                0 -> "অন্বেষার রসনা বিলাস • খাঁটি ঘরোয়া স্বাদ"
                1 -> "তাজা মশলা ও নিজস্ব রন্ধনশৈলী"
                2 -> "স্বাস্থ্যকর ও পরিচ্ছন্ন পরিবেশন"
                3 -> "বাঙালিয়ানার চেনা স্বাদ, ঘরের রান্নায় সেরা স্বাদ"
                else -> "যোগাযোগ: ৮৫০৯৩৬০৩২৭"
            }

            // Assign uploaded media if option B or C and media exists
            val hasUploadedMedia = (option == AiGenerationOption.OPTION_B || option == AiGenerationOption.OPTION_C) && uploadedMediaCount > 0
            val isAiVisual = option == AiGenerationOption.OPTION_A || (option == AiGenerationOption.OPTION_B && i % 2 == 1)

            scenes.add(
                SceneItem(
                    sceneNumber = scNumber,
                    titleBn = template.first,
                    visualDescription = template.second,
                    narrationScriptBn = template.third,
                    onScreenTextBn = onScreenText,
                    durationSeconds = durationSec,
                    isAiVisual = isAiVisual,
                    assignedMediaName = if (hasUploadedMedia) "আপলোড করা ছবি/ভিডিও #${(i % uploadedMediaCount) + 1}" else null,
                    panZoomEffect = if (!isAiVisual) "মসৃণ জুম ও প্যান অ্যানিমেশন" else "সিনেম্যাটিক মুভমেন্ট"
                )
            )

            cues.add(
                NarrationCue(
                    startSeconds = currentSecond,
                    endSeconds = currentSecond + durationSec,
                    textBn = template.third,
                    voiceSpeaker = "পৌলমী (পশ্চিমবঙ্গীয় প্রমিত বাংলা)"
                )
            )

            overlays.add(
                TextOverlay(
                    textBn = onScreenText,
                    startSeconds = currentSecond,
                    endSeconds = currentSecond + durationSec
                )
            )

            currentSecond += durationSec
        }

        return AiVideoPlan(
            titleBn = titleBn,
            conceptBn = "খাঁটি বাঙালি রান্নার স্বাদে ভরপুর ভিডিও উপস্থাপনা",
            scenes = scenes,
            narrationCues = cues,
            textOverlays = overlays,
            suggestedMusicGenre = "শান্ত ঐতিহ্যবাহী সেতার ও বাঁশির সুর (Melodic Sitar & Bansuri)",
            totalDurationSeconds = durationSeconds
        )
    }
}
