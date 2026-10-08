package com.example

import com.example.model.AiGenerationOption
import com.example.model.BrandProfile
import com.example.model.DurationPreset
import com.example.model.VideoAspectRatio
import com.example.service.AiScriptGeneratorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoStudioLogicTest {

    private val scriptService = AiScriptGeneratorService()
    private val brandProfile = BrandProfile()

    @Test
    fun brandProfile_defaultValues_areCorrect() {
        assertEquals("অন্বেষার রসনা বিলাস", brandProfile.businessName)
        assertEquals("৮৫০৯৩৬০৩২৭", brandProfile.contactPhone)
        assertEquals("anweshar-roshona-bilas.ai.studio", brandProfile.website)
        assertTrue(brandProfile.showLogoOverlay)
    }

    @Test
    fun durationPresets_atLeast30Seconds() {
        DurationPreset.values().forEach { preset ->
            if (preset != DurationPreset.CUSTOM) {
                assertTrue("Preset ${preset.name} must be at least 30s", preset.seconds >= 30)
            }
        }
    }

    @Test
    fun localScriptPlan_generatesExpectedScenesAndNarration() {
        val plan = scriptService.generateLocalArtisticPlan(
            userPrompt = "খাসির মাংস ও পোলাও",
            option = AiGenerationOption.OPTION_A,
            durationSeconds = 45,
            brand = brandProfile,
            uploadedMediaCount = 0
        )

        assertNotNull(plan)
        assertTrue(plan.scenes.isNotEmpty())
        assertTrue(plan.narrationCues.isNotEmpty())
        assertTrue(plan.textOverlays.isNotEmpty())
        // Verify Bengali script integrity
        assertTrue(plan.titleBn.contains("মাংস") || plan.titleBn.contains("অন্বেষার"))
        assertEquals(45, plan.totalDurationSeconds)
    }

    @Test
    fun optionC_doesNotGenerateAiVisuals() {
        val plan = scriptService.generateLocalArtisticPlan(
            userPrompt = "রান্নার আসল ভিডিও",
            option = AiGenerationOption.OPTION_C,
            durationSeconds = 40,
            brand = brandProfile,
            uploadedMediaCount = 3
        )

        // For Option C, visual authenticity is preserved: isAiVisual must be false
        plan.scenes.forEach { scene ->
            assertFalse("Option C must not fabricate AI visuals", scene.isAiVisual)
        }
    }

    @Test
    fun videoAspectRatio_valuesAreValid() {
        assertEquals(9f / 16f, VideoAspectRatio.PORTRAIT_9_16.ratioValue, 0.01f)
        assertEquals(16f / 9f, VideoAspectRatio.LANDSCAPE_16_9.ratioValue, 0.01f)
        assertEquals(1.0f, VideoAspectRatio.SQUARE_1_1.ratioValue, 0.01f)
    }
}
