package com.example.model

import android.net.Uri

/**
 * Brand details for "অন্বেষার রসনা বিলাস"
 */
data class BrandProfile(
    val businessName: String = "অন্বেষার রসনা বিলাস",
    val tagline: String = "বাঙালিয়ানার চেনা স্বাদ, ঘরের রান্নায় সেরা স্বাদ।",
    val contactPhone: String = "৮৫০৯৩৬০৩২৭",
    val website: String = "anweshar-roshona-bilas.ai.studio",
    val logoUri: String? = null,
    val showLogoOverlay: Boolean = true,
    val showTaglineOverlay: Boolean = true,
    val logoPosition: LogoPosition = LogoPosition.TOP_RIGHT,
    val logoSizePercent: Float = 16f
)

enum class LogoPosition(val labelBn: String) {
    TOP_LEFT("উপরে বামে"),
    TOP_RIGHT("উপরে ডানে"),
    BOTTOM_LEFT("নিচে বামে"),
    BOTTOM_RIGHT("নিচে ডানে")
}

/**
 * Aspect Ratio choices for final video export
 */
enum class VideoAspectRatio(val labelBn: String, val ratioValue: Float, val ratioStr: String) {
    PORTRAIT_9_16("৯:১৬ (রিলস/শর্টস)", 9f / 16f, "9:16"),
    LANDSCAPE_16_9("১৬:৯ (ইউটিউব/টিভি)", 16f / 9f, "16:9"),
    SQUARE_1_1("১:১ (ইনস্টাগ্রাম/ফেসবুক)", 1f, "1:1")
}

/**
 * Duration Presets in Seconds
 */
enum class DurationPreset(val labelBn: String, val seconds: Int) {
    SEC_30("৩০ সেকেন্ড", 30),
    SEC_45("৪৫ সেকেন্ড", 45),
    SEC_60("৬০ সেকেন্ড (১ মিনিট)", 60),
    SEC_90("৯০ সেকেন্ড (১.৫ মিনিট)", 90),
    SEC_120("১২০ সেকেন্ড (২ মিনিট)", 120),
    CUSTOM("কাস্টম সময়", 0)
}

/**
 * AI Generation Modes
 */
enum class AiGenerationOption(val code: String, val titleBn: String, val subtitleBn: String) {
    OPTION_A(
        "A",
        "A. শুধু AI দিয়ে তৈরি করুন",
        "কোনো মিডিয়ার প্রয়োজন নেই। AI প্রম্পট থেকে সরাসরি সম্পূর্ণ ভিডিও, ভয়েস-ওভার ও মিউজিক তৈরি করবে।"
    ),
    OPTION_B(
        "B",
        "B. নিজের ছবি/ভিডিও + AI ব্যবহার করুন",
        "নিজের আপলোড করা খাবারের আসল ছবি/ভিডিওর সাথে AI-এর দৃশ্য ও আবহ তৈরি হবে।"
    ),
    OPTION_C(
        "C",
        "C. শুধু নিজের ছবি/ভিডিও ব্যবহার করুন",
        "শুধুমাত্র আপনার খাঁটি রান্না ও আউটলেটের আসল মিডিয়া ব্যবহৃত হবে। এআই কোনো নকল দৃশ্য যোগ করবে না।"
    )
}

/**
 * Individual clip / media segment in Full Editor
 */
data class MediaClip(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uriString: String,
    val displayName: String,
    val isVideo: Boolean = true,
    val durationMs: Long = 5000L,
    val orderIndex: Int = 0
)

/**
 * Script & Scene element in AI Video pipeline & Timeline
 */
data class SceneItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sceneNumber: Int,
    val titleBn: String,
    val visualDescription: String,
    val narrationScriptBn: String,
    val onScreenTextBn: String,
    val durationSeconds: Int = 8,
    val assignedMediaUri: String? = null,
    val assignedMediaName: String? = null,
    val isAiVisual: Boolean = false,
    val panZoomEffect: String = "Pan & Gentle Zoom"
)

/**
 * Voice-over narration cue for the audio timeline
 */
data class NarrationCue(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startSeconds: Float,
    val endSeconds: Float,
    val textBn: String,
    val voiceSpeaker: String = "পৌলমী (পশ্চিমবঙ্গীয় প্রমিত বাংলা)",
    val speedMultiplier: Float = 1.0f,
    val pitch: Float = 1.0f,
    val isGenerated: Boolean = true
)

/**
 * On-screen text typography overlay
 */
data class TextOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val textBn: String,
    val startSeconds: Float,
    val endSeconds: Float,
    val positionYPercent: Float = 80f, // 80% is bottom subtitle area
    val fontSizeSp: Float = 20f,
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#99000000" // semi-transparent black pill
)

/**
 * Background Music Track
 */
data class MusicTrack(
    val id: String,
    val titleBn: String,
    val descriptionBn: String,
    val genre: String,
    val tempo: String,
    val volume: Float = 0.45f,
    val duckingAmount: Float = 0.15f // volume when voice-over is active
)
