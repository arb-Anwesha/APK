package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiGenerationOption
import com.example.model.DurationPreset
import com.example.model.VideoAspectRatio
import com.example.ui.components.SceneTimelineEditor
import com.example.ui.components.VideoPlayerStage
import com.example.viewmodel.VideoStudioUiState
import com.example.viewmodel.VideoStudioViewModel

/**
 * Screen 2: AI ভিডিও জেনারেটর (AI Video Studio)
 * Supports all 3 options:
 * A. শুধু AI দিয়ে তৈরি করুন
 * B. নিজের ছবি/ভিডিও + AI ব্যবহার করুন
 * C. শুধু নিজের ছবি/ভিডিও ব্যবহার করুন
 * - Duration presets: 30s, 45s, 60s, 90s, 120s + Custom (minimum 30s)
 * - Aspect ratios: 9:16, 16:9, 1:1
 * - Multi-scene stitching & character/atmosphere consistency
 * - Complete editable timeline (scripts, narration, on-screen text, durations)
 * - Full preview with Bengali voice-over + 1080p MP4 export
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiVideoGeneratorScreen(
    viewModel: VideoStudioViewModel,
    uiState: VideoStudioUiState,
    modifier: Modifier = Modifier
) {
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addMediaForAiGenerator(uris, isVideo = false)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_video_generator_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Badge
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2B1612),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD47A00))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD47A00)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text(
                            text = "২. AI ভিডিও জেনারেটর",
                            color = Color(0xFFFFD54F),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "প্রম্পট/মিডিয়া → চিত্রনাট্য → সিন প্ল্যান → বাংলা ভয়েস → ভিজ্যুয়াল → মিউজিক → MP4",
                            color = Color(0xFFE0E0E0),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 1. CHOOSE OPTION (A, B, C)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1514)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E2825))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ভিডিও তৈরির মোড নির্বাচন করুন",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AiGenerationOption.values().forEach { option ->
                        val isSelected = option == uiState.selectedAiOption
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF3B1E1A) else Color(0xFF231816),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFB703) else Color(0xFF3D2724)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setAiOption(option) }
                                .testTag("option_${option.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFFFB703) else Color(0xFF888888),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = option.titleBn,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = option.subtitleBn,
                                        color = Color(0xFFB0BEC5),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upload Media for Option B & C
        if (uiState.selectedAiOption != AiGenerationOption.OPTION_A) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1514)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E2825))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "নিজের আসল ছবি / ভিডিও আপলোড (${uiState.aiUploadedMediaList.size}টি)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = {
                                    mediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("মিডিয়া নির্বাচন", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Authenticity Guarantee Notice
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2C1E1B)
                        ) {
                            Text(
                                text = if (uiState.selectedAiOption == AiGenerationOption.OPTION_C)
                                    "🔒 কঠোর নিরাপত্তা: শুধুমাত্র আপনার আপলোড করা আসল ছবি/ভিডিও প্রদর্শিত হবে। কোনো কাল্পনিক AI ফুটেজ তৈরি হবে না। সময় বাড়াতে মসৃণ প্যান ও জুম ব্যবহৃত হবে।"
                                else
                                    "✨ সংমিশ্রণ মোড: আপনার আসল মিডিয়ার সাথে মানানসই আলো ও পরিবেশের ধারাবাহিক AI সিন সিন্থেসিস যুক্ত হবে।",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        if (uiState.aiUploadedMediaList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                uiState.aiUploadedMediaList.forEach { clip ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF2A1C1A),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF422D29))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(clip.displayName, color = Color.White, fontSize = 11.sp)
                                            IconButton(
                                                onClick = { viewModel.removeAiMedia(clip.id) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = Color(0xFFE57373), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. PROMPT INPUT & PRESETS
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1514)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E2825))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "বিজ্ঞাপন বা ভিডিওর প্রম্পট লিখুন",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "বাংলা, ইংরেজি বা মিশ্র ভাষায় লিখতে পারেন (যেমন: 'রবিবারের খাসির মাংসের ঝোল ও পোলাও')",
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.userPrompt,
                        onValueChange = { viewModel.setUserPrompt(it) },
                        placeholder = { Text("খাবারের বর্ণনা, স্পেশাল পদ বা ইভেন্টের অফার লিখুন...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB703),
                            unfocusedBorderColor = Color(0xFF553833),
                            focusedContainerColor = Color(0xFF251A18),
                            unfocusedContainerColor = Color(0xFF251A18)
                        ),
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_text_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Suggested quick culinary prompt chips
                    Text("💡 দ্রুত প্রম্পট আইডিয়া:", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val quickPrompts = listOf(
                            "সর্ষে ইলিশ ও ভাপা চিংড়ির রাজকীয় আয়োজন",
                            "রবিবারের খাসির মাংসের লাল ঝোল ও গরম ভাত",
                            "বাঙালির সম্পূর্ণ রসনা থালি ও শেষ পাতে চাটনি-পায়েস"
                        )
                        quickPrompts.forEach { qp ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF2E1F1D),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF533630)),
                                modifier = Modifier.clickable { viewModel.setUserPrompt(qp) }
                            ) {
                                Text(qp, color = Color(0xFFFFD54F), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // 3. DURATION PRESETS & ASPECT RATIO
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1514)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E2825))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Duration Presets (Min 30s)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        Text(
                            text = "ভিডিওর সময়কাল (ন্যূনতম ৩০ সেকেন্ড)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DurationPreset.values().forEach { preset ->
                            val isSelected = preset == uiState.selectedDurationPreset
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setDurationPreset(preset) },
                                label = { Text(preset.labelBn, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF8B1E0F),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF261917),
                                    labelColor = Color(0xFFB0BEC5)
                                )
                            )
                        }
                    }

                    if (uiState.selectedDurationPreset == DurationPreset.CUSTOM) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.customDurationSeconds.toString(),
                            onValueChange = {
                                val s = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 30
                                viewModel.setCustomDuration(s)
                            },
                            label = { Text("কাস্টম সময় সেকেন্ডে (৩০ - ৩০০)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFB703)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aspect Ratio Choices
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        Text(
                            text = "অ্যাসপেক্ট রেশিও (Aspect Ratio)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VideoAspectRatio.values().forEach { ratio ->
                            val isSelected = ratio == uiState.selectedAspectRatio
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF8B1E0F) else Color(0xFF261917),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF3E2825)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setAspectRatio(ratio) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(ratio.ratioStr, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when (ratio) {
                                            VideoAspectRatio.PORTRAIT_9_16 -> "রিলস / শর্টস"
                                            VideoAspectRatio.LANDSCAPE_16_9 -> "ইউটিউব / টিভি"
                                            VideoAspectRatio.SQUARE_1_1 -> "ইনস্টা / পোস্ট"
                                        },
                                        color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF888888),
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // GENERATE / REGENERATE SCRIPT & SCENE PLAN BUTTON
        item {
            Button(
                onClick = { viewModel.generateAiVideoPlan() },
                enabled = !uiState.isGeneratingScript,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_script_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD47A00))
            ) {
                if (uiState.isGeneratingScript) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI চিত্রনাট্য ও সিন প্ল্যান তৈরি হচ্ছে...", color = Color.White, fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.scenes.isEmpty()) "AI স্ক্রিপ্ট ও সিন প্ল্যান তৈরি করুন" else "নতুন করে সম্পূর্ণ স্ক্রিপ্ট রি-জেনারেট করুন",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. LIVE VIDEO PLAYER STAGE
        item {
            val previewScene = uiState.scenes.getOrNull(uiState.activePreviewSceneIndex)
            VideoPlayerStage(
                currentScene = previewScene,
                brandProfile = uiState.brandProfile,
                aspectRatio = uiState.selectedAspectRatio,
                isPlaying = uiState.isAiPreviewPlaying,
                playbackProgress = uiState.previewPlaybackProgress,
                isVoiceOverActive = uiState.isVoiceOverPlaying,
                onTogglePlay = { viewModel.toggleAiPreviewPlayback() },
                onTriggerVoiceOver = { viewModel.playVoiceOverForCurrentScene() }
            )
        }

        // 5. EDITABLE SCENE TIMELINE & SCRIPT CARDS
        if (uiState.scenes.isNotEmpty()) {
            item {
                SceneTimelineEditor(
                    scenes = uiState.scenes,
                    activeSceneIndex = uiState.activePreviewSceneIndex,
                    onSelectScene = { viewModel.selectPreviewScene(it) },
                    onUpdateTitle = { id, title -> viewModel.updateSceneTitle(id, title) },
                    onUpdateNarration = { id, narr -> viewModel.updateSceneNarration(id, narr) },
                    onUpdateOnScreenText = { id, text -> viewModel.updateSceneOnScreenText(id, text) },
                    onUpdateDuration = { id, sec -> viewModel.updateSceneDuration(id, sec) }
                )
            }
        }

        // 6. FINAL 1080P MP4 EXPORT BUTTON
        item {
            Button(
                onClick = { viewModel.exportFinalVideoMp4() },
                enabled = !uiState.isExporting && uiState.scenes.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("export_ai_video_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "চূড়ান্ত ১০৮০p MP4 ভিডিও এক্সপোর্ট করুন",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
