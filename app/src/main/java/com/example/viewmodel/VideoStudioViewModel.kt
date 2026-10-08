package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AiGenerationOption
import com.example.model.BrandProfile
import com.example.model.DurationPreset
import com.example.model.LogoPosition
import com.example.model.MediaClip
import com.example.model.MusicTrack
import com.example.model.NarrationCue
import com.example.model.SceneItem
import com.example.model.TextOverlay
import com.example.model.VideoAspectRatio
import com.example.service.AiScriptGeneratorService
import com.example.service.AndroidVideoExportEngine
import com.example.service.BengaliAudioVoiceEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppNavTab(val titleBn: String) {
    FULL_EDITOR("১. পূর্ণ ভিডিও এডিটর"),
    AI_STUDIO("২. AI ভিডিও জেনারেটর"),
    BRAND_SETTINGS("ব্র্যান্ড সেটিংস")
}

data class VideoStudioUiState(
    val currentTab: AppNavTab = AppNavTab.AI_STUDIO,
    val brandProfile: BrandProfile = BrandProfile(),
    
    // FULL EDITOR STATE
    val uploadedClips: List<MediaClip> = emptyList(),
    val editorTimelinePositionSeconds: Float = 0f,
    val isEditorPlaying: Boolean = false,
    val editorNarrationCues: List<NarrationCue> = emptyList(),
    val editorTextOverlays: List<TextOverlay> = emptyList(),
    val selectedMusicTrack: MusicTrack = defaultMusicTracks[0],
    val isMusicDuckingEnabled: Boolean = true,
    val fullEditorAspectRatio: VideoAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
    
    // AI VIDEO GENERATOR STATE
    val selectedAiOption: AiGenerationOption = AiGenerationOption.OPTION_B,
    val userPrompt: String = "রবিবারের খাঁটি বাঙালি খাসির মাংসের কষা ও গরম গরম লুচির মনোরম বিজ্ঞাপন",
    val selectedDurationPreset: DurationPreset = DurationPreset.SEC_45,
    val customDurationSeconds: Int = 45,
    val selectedAspectRatio: VideoAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
    val aiUploadedMediaList: List<MediaClip> = emptyList(),
    
    // GENERATED PLAN STATE
    val isGeneratingScript: Boolean = false,
    val generatedTitleBn: String = "",
    val generatedConceptBn: String = "",
    val scenes: List<SceneItem> = emptyList(),
    val narrationCues: List<NarrationCue> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val activePreviewSceneIndex: Int = 0,
    val previewPlaybackProgress: Float = 0f, // 0..1 in current scene
    val isAiPreviewPlaying: Boolean = false,
    val isVoiceOverPlaying: Boolean = false,
    
    // EXPORT STATE
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportStatusMessage: String = "",
    val exportedVideoUri: Uri? = null,
    val errorMessage: String? = null
)

val defaultMusicTracks = listOf(
    MusicTrack(
        id = "track_1",
        titleBn = "বাংলার মাটি ও সেতারের রাগ",
        descriptionBn = "শান্ত ঐতিহ্যবাহী সেতার ও তানপুরার সুর, খাঁটি বাঙালি রান্নার উপযুক্ত",
        genre = "Traditional Classical",
        tempo = "শান্ত (৮৫ bpm)"
    ),
    MusicTrack(
        id = "track_2",
        titleBn = "বাউল বাঁশির মিষ্টি তান",
        descriptionBn = "মাটির গন্ধ মাখা মধুর লোকসঙ্গীতের দোতারা ও বাঁশির মেলোডি",
        genre = "Bengali Folk",
        tempo = "মধ্যম (৯৫ bpm)"
    ),
    MusicTrack(
        id = "track_3",
        titleBn = "উৎসবের সানাই ও ঢোল",
        descriptionBn = "উৎসবমুখর বিশেষ ভোজ ও জমকালো প্রচারের জন্য উদ্যমী আবহ",
        genre = "Festive Celebration",
        tempo = "উদ্যমী (১১০ bpm)"
    )
)

class VideoStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VideoStudioUiState())
    val uiState: StateFlow<VideoStudioUiState> = _uiState.asStateFlow()

    private val scriptGenerator = AiScriptGeneratorService()
    private val voiceEngine = BengaliAudioVoiceEngine(application)
    private val exportEngine = AndroidVideoExportEngine(application)

    private var playbackJob: Job? = null

    init {
        // Pre-generate a default rich script for immediate preview experience
        generateAiVideoPlan()
    }

    fun setNavTab(tab: AppNavTab) {
        pauseAiPreview()
        voiceEngine.stop()
        _uiState.update { it.copy(currentTab = tab) }
    }

    // --- BRAND SETTINGS ---
    fun updateBrandProfile(
        businessName: String? = null,
        tagline: String? = null,
        phone: String? = null,
        website: String? = null,
        logoUri: String? = null,
        showLogoOverlay: Boolean? = null,
        logoPosition: LogoPosition? = null
    ) {
        _uiState.update { state ->
            val curr = state.brandProfile
            state.copy(
                brandProfile = curr.copy(
                    businessName = businessName ?: curr.businessName,
                    tagline = tagline ?: curr.tagline,
                    contactPhone = phone ?: curr.contactPhone,
                    website = website ?: curr.website,
                    logoUri = logoUri ?: curr.logoUri,
                    showLogoOverlay = showLogoOverlay ?: curr.showLogoOverlay,
                    logoPosition = logoPosition ?: curr.logoPosition
                )
            )
        }
    }

    // --- FULL VIDEO EDITOR ACTIONS ---
    fun addClipsToEditor(uris: List<Uri>, isVideo: Boolean = true) {
        val newClips = uris.mapIndexed { index, uri ->
            MediaClip(
                uriString = uri.toString(),
                displayName = "ভিডিও ক্লিপ #${_uiState.value.uploadedClips.size + index + 1}",
                isVideo = isVideo,
                durationMs = 6000L,
                orderIndex = _uiState.value.uploadedClips.size + index
            )
        }
        _uiState.update { state ->
            state.copy(uploadedClips = state.uploadedClips + newClips)
        }
    }

    fun removeEditorClip(clipId: String) {
        _uiState.update { state ->
            state.copy(uploadedClips = state.uploadedClips.filterNot { it.id == clipId })
        }
    }

    fun reorderEditorClips(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.uploadedClips.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            state.copy(uploadedClips = list)
        }
    }

    fun addEditorNarrationCue(startSec: Float, endSec: Float, textBn: String) {
        val cue = NarrationCue(
            startSeconds = startSec,
            endSeconds = endSec,
            textBn = textBn
        )
        _uiState.update { state ->
            state.copy(editorNarrationCues = state.editorNarrationCues + cue)
        }
    }

    fun addEditorTextOverlay(textBn: String, startSec: Float, endSec: Float) {
        val overlay = TextOverlay(
            textBn = textBn,
            startSeconds = startSec,
            endSeconds = endSec
        )
        _uiState.update { state ->
            state.copy(editorTextOverlays = state.editorTextOverlays + overlay)
        }
    }

    fun toggleMusicDucking(enabled: Boolean) {
        _uiState.update { it.copy(isMusicDuckingEnabled = enabled) }
    }

    fun setMusicTrack(track: MusicTrack) {
        _uiState.update { it.copy(selectedMusicTrack = track) }
    }

    // --- AI VIDEO GENERATOR ACTIONS ---
    fun setAiOption(option: AiGenerationOption) {
        _uiState.update { it.copy(selectedAiOption = option) }
    }

    fun setUserPrompt(prompt: String) {
        _uiState.update { it.copy(userPrompt = prompt) }
    }

    fun setDurationPreset(preset: DurationPreset) {
        _uiState.update { it.copy(selectedDurationPreset = preset) }
    }

    fun setCustomDuration(seconds: Int) {
        _uiState.update { it.copy(customDurationSeconds = seconds.coerceIn(30, 300)) }
    }

    fun setAspectRatio(ratio: VideoAspectRatio) {
        _uiState.update { it.copy(selectedAspectRatio = ratio) }
    }

    fun addMediaForAiGenerator(uris: List<Uri>, isVideo: Boolean) {
        val newClips = uris.mapIndexed { index, uri ->
            MediaClip(
                uriString = uri.toString(),
                displayName = if (isVideo) "আসল ভিডিও #${_uiState.value.aiUploadedMediaList.size + index + 1}" else "আসল ফটো #${_uiState.value.aiUploadedMediaList.size + index + 1}",
                isVideo = isVideo,
                durationMs = 6000L
            )
        }
        _uiState.update { state ->
            state.copy(aiUploadedMediaList = state.aiUploadedMediaList + newClips)
        }
    }

    fun removeAiMedia(clipId: String) {
        _uiState.update { state ->
            state.copy(aiUploadedMediaList = state.aiUploadedMediaList.filterNot { it.id == clipId })
        }
    }

    fun generateAiVideoPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingScript = true, errorMessage = null) }
            val state = _uiState.value
            val duration = if (state.selectedDurationPreset == DurationPreset.CUSTOM) {
                state.customDurationSeconds
            } else {
                state.selectedDurationPreset.seconds
            }

            try {
                val plan = scriptGenerator.generateScriptAndScenes(
                    userPrompt = state.userPrompt,
                    option = state.selectedAiOption,
                    durationSeconds = duration,
                    brandProfile = state.brandProfile,
                    uploadedMediaCount = state.aiUploadedMediaList.size
                )

                _uiState.update {
                    it.copy(
                        isGeneratingScript = false,
                        generatedTitleBn = plan.titleBn,
                        generatedConceptBn = plan.conceptBn,
                        scenes = plan.scenes,
                        narrationCues = plan.narrationCues,
                        textOverlays = plan.textOverlays,
                        activePreviewSceneIndex = 0,
                        previewPlaybackProgress = 0f
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingScript = false,
                        errorMessage = "স্ক্রিপ্ট তৈরিতে সমস্যা হয়েছে: ${e.message}"
                    )
                }
            }
        }
    }

    // Editable scenes & narration
    fun updateSceneTitle(sceneId: String, newTitle: String) {
        _uiState.update { state ->
            val updated = state.scenes.map {
                if (it.id == sceneId) it.copy(titleBn = newTitle) else it
            }
            state.copy(scenes = updated)
        }
    }

    fun updateSceneNarration(sceneId: String, newNarration: String) {
        _uiState.update { state ->
            val updated = state.scenes.map {
                if (it.id == sceneId) it.copy(narrationScriptBn = newNarration) else it
            }
            // Sync with narration cues
            val updatedCues = state.narrationCues.mapIndexed { idx, cue ->
                if (idx < updated.size && updated[idx].id == sceneId) {
                    cue.copy(textBn = newNarration)
                } else cue
            }
            state.copy(scenes = updated, narrationCues = updatedCues)
        }
    }

    fun updateSceneOnScreenText(sceneId: String, newText: String) {
        _uiState.update { state ->
            val updated = state.scenes.map {
                if (it.id == sceneId) it.copy(onScreenTextBn = newText) else it
            }
            state.copy(scenes = updated)
        }
    }

    fun updateSceneDuration(sceneId: String, newSec: Int) {
        _uiState.update { state ->
            val updated = state.scenes.map {
                if (it.id == sceneId) it.copy(durationSeconds = newSec.coerceIn(3, 30)) else it
            }
            state.copy(scenes = updated)
        }
    }

    fun assignMediaToScene(sceneId: String, mediaName: String?, mediaUri: String?) {
        _uiState.update { state ->
            val updated = state.scenes.map {
                if (it.id == sceneId) it.copy(assignedMediaName = mediaName, assignedMediaUri = mediaUri) else it
            }
            state.copy(scenes = updated)
        }
    }

    // PREVIEW & SPEECH
    fun playVoiceOverForCurrentScene() {
        val state = _uiState.value
        val scene = state.scenes.getOrNull(state.activePreviewSceneIndex) ?: return
        voiceEngine.speakNarration(scene.narrationScriptBn, onStart = {
            _uiState.update { it.copy(isVoiceOverPlaying = true) }
        }, onDone = {
            _uiState.update { it.copy(isVoiceOverPlaying = false) }
        })
    }

    fun toggleAiPreviewPlayback() {
        if (_uiState.value.isAiPreviewPlaying) {
            pauseAiPreview()
        } else {
            startAiPreview()
        }
    }

    private fun startAiPreview() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isAiPreviewPlaying = true) }

        playbackJob = viewModelScope.launch {
            while (isActive && _uiState.value.isAiPreviewPlaying) {
                val state = _uiState.value
                val scenes = state.scenes
                if (scenes.isEmpty()) break

                val currentIdx = state.activePreviewSceneIndex
                val activeScene = scenes[currentIdx]
                val sceneSec = activeScene.durationSeconds.coerceAtLeast(3)

                // Trigger voice-over when scene starts
                voiceEngine.speakNarration(activeScene.narrationScriptBn)

                // Advance progress smoothly over sceneSec
                val stepCount = sceneSec * 10
                for (s in 0 until stepCount) {
                    if (!isActive || !_uiState.value.isAiPreviewPlaying) break
                    delay(100L)
                    _uiState.update {
                        it.copy(previewPlaybackProgress = (s + 1).toFloat() / stepCount)
                    }
                }

                if (!isActive || !_uiState.value.isAiPreviewPlaying) break

                // Move to next scene
                val nextIdx = (currentIdx + 1) % scenes.size
                _uiState.update {
                    it.copy(
                        activePreviewSceneIndex = nextIdx,
                        previewPlaybackProgress = 0f
                    )
                }
            }
        }
    }

    fun selectPreviewScene(index: Int) {
        voiceEngine.stop()
        _uiState.update {
            it.copy(
                activePreviewSceneIndex = index.coerceIn(0, (it.scenes.size - 1).coerceAtLeast(0)),
                previewPlaybackProgress = 0f
            )
        }
        val scene = _uiState.value.scenes.getOrNull(index)
        if (scene != null && scene.narrationScriptBn.isNotBlank()) {
            voiceEngine.speakNarration(scene.narrationScriptBn)
        }
    }

    fun pauseAiPreview() {
        playbackJob?.cancel()
        voiceEngine.stop()
        _uiState.update { it.copy(isAiPreviewPlaying = false, isVoiceOverPlaying = false) }
    }

    // EXPORT MP4
    fun exportFinalVideoMp4() {
        viewModelScope.launch {
            pauseAiPreview()
            _uiState.update {
                it.copy(
                    isExporting = true,
                    exportProgress = 0.05f,
                    exportStatusMessage = "ভিডিও রেন্ডারিং শুরু হচ্ছে...",
                    errorMessage = null
                )
            }

            val state = _uiState.value
            val scenesToExport = if (state.currentTab == AppNavTab.FULL_EDITOR) {
                // If Full Editor, convert uploaded clips to scenes
                if (state.uploadedClips.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage = "রপ্তানির জন্য প্রথমে কমপক্ষে একটি ভিডিও ক্লিপ যোগ করুন।"
                        )
                    }
                    return@launch
                }
                state.uploadedClips.mapIndexed { idx, clip ->
                    val cue = state.editorNarrationCues.getOrNull(idx)
                    val overlay = state.editorTextOverlays.getOrNull(idx)
                    SceneItem(
                        sceneNumber = idx + 1,
                        titleBn = clip.displayName,
                        visualDescription = "আসল মূল ভিডিও ফুটেজ (কোনো পরিবর্তন ছাড়া)",
                        narrationScriptBn = cue?.textBn ?: "",
                        onScreenTextBn = overlay?.textBn ?: state.brandProfile.tagline,
                        durationSeconds = (clip.durationMs / 1000).toInt().coerceAtLeast(4),
                        assignedMediaUri = clip.uriString,
                        isAiVisual = false
                    )
                }
            } else {
                state.scenes
            }

            val resultUri = exportEngine.exportVideoMp4(
                scenes = scenesToExport,
                narrationCues = state.narrationCues,
                textOverlays = state.textOverlays,
                brandProfile = state.brandProfile,
                aspectRatio = if (state.currentTab == AppNavTab.FULL_EDITOR) state.fullEditorAspectRatio else state.selectedAspectRatio,
                musicTrackTitle = state.selectedMusicTrack.titleBn,
                isMusicMuted = false,
                onProgress = { progress, msg ->
                    _uiState.update { it.copy(exportProgress = progress, exportStatusMessage = msg) }
                }
            )

            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportedVideoUri = resultUri,
                    exportStatusMessage = if (resultUri != null) "রপ্তানি সম্পূর্ণ! গ্যালারিতে সংরক্ষিত।" else "রপ্তানি ব্যর্থ হয়েছে।"
                )
            }
        }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(exportedVideoUri = null, errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}
