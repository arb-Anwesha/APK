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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaClip
import com.example.model.MusicTrack
import com.example.model.VideoAspectRatio
import com.example.ui.components.VideoPlayerStage
import com.example.viewmodel.VideoStudioUiState
import com.example.viewmodel.VideoStudioViewModel
import com.example.viewmodel.defaultMusicTracks

/**
 * Screen 1: পূর্ণ ভিডিও এডিটর (Full Video Editor)
 * - Multiple video upload and join in selected order.
 * - Keeps original videos authentic (no unwanted trimming/cropping/distortion).
 * - Bengali AI female voice-over timeline.
 * - Natural West Bengal Bengali pronunciation.
 * - Bengali on-screen typography text overlays.
 * - Undistorted Brand Logo overlay.
 * - Background music with automatic voice-over volume ducking.
 * - Full video preview & 1080p MP4 export.
 */
@Composable
fun FullEditorScreen(
    viewModel: VideoStudioViewModel,
    uiState: VideoStudioUiState,
    modifier: Modifier = Modifier
) {
    // Video picker launcher using zero-permission Android Photo Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addClipsToEditor(uris, isVideo = true)
        }
    }

    var showAddVoiceDialog by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("full_editor_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Badge
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF241614),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B1E0F))
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
                            .background(Color(0xFF8B1E0F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text(
                            text = "১. পূর্ণ ভিডিও এডিটর",
                            color = Color(0xFFFFD54F),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "আসল ভিডিও অপরিবর্তিত রেখে সহজে জোড়া লাগান, ভয়েস-ওভার ও লোগো যুক্ত করুন",
                            color = Color(0xFFE0E0E0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Complete Live Video Player Preview Stage
        item {
            val previewScene = uiState.scenes.getOrNull(uiState.activePreviewSceneIndex)
            VideoPlayerStage(
                currentScene = previewScene,
                brandProfile = uiState.brandProfile,
                aspectRatio = uiState.fullEditorAspectRatio,
                isPlaying = uiState.isAiPreviewPlaying,
                playbackProgress = uiState.previewPlaybackProgress,
                isVoiceOverActive = uiState.isVoiceOverPlaying,
                onTogglePlay = { viewModel.toggleAiPreviewPlayback() },
                onTriggerVoiceOver = { viewModel.playVoiceOverForCurrentScene() }
            )
        }

        // Section 1: Upload Multiple Videos & Maintain Authenticity
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                            Text(
                                text = "ভিডিও ক্লিপ যোগ ও ক্রম নির্ধারণ (${uiState.uploadedClips.size}টি)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ভিডিও যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notice on keeping videos authentic
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2C1E1B)
                    ) {
                        Text(
                            text = "🛡️ আসল ভিডিও নীতি: ভিডিওগুলোতে কোনো অযাচিত ক্রপিং, স্ট্রেচ বা কৃত্রিম স্পিড পরিবর্তন করা হয় না। আসল রূপ সংরক্ষিত থাকবে।",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.uploadedClips.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF251A18)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "গ্যালারি থেকে এক বা একাধিক ভিডিও ক্লিপ নির্বাচন করুন",
                                color = Color(0xFFAAAAAA),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.uploadedClips.forEachIndexed { index, clip ->
                                ClipRowItem(
                                    clip = clip,
                                    index = index,
                                    totalCount = uiState.uploadedClips.size,
                                    onMoveUp = { viewModel.reorderEditorClips(index, index - 1) },
                                    onMoveDown = { viewModel.reorderEditorClips(index, index + 1) },
                                    onDelete = { viewModel.removeEditorClip(clip.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Bengali Female Voice-Over Timeline Control
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(18.dp))
                            Text(
                                text = "পশ্চিমবঙ্গীয় বাংলা AI ভয়েস-ওভার",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showAddVoiceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ভয়েস কিউ যোগ", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "স্বাভাবিক পশ্চিমবঙ্গীয় উচ্চারণ (কলকাতা/রাঢ় অঞ্চল)। প্রতিটি বর্ণনার শুরু ও শেষের সময় নির্ধারণ করুন।",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.editorNarrationCues.isEmpty()) {
                        Text(
                            text = "কোনো ভয়েস কিউ যুক্ত করা হয়নি। 'ভয়েস কিউ যোগ' বাটনে চাপুন।",
                            color = Color(0xFF888888),
                            fontSize = 12.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.editorNarrationCues.forEachIndexed { idx, cue ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF281C1A)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${cue.startSeconds.toInt()} সে. থেকে ${cue.endSeconds.toInt()} সে.",
                                                color = Color(0xFFFFD54F),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = cue.textBn,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Bengali On-Screen Text & Typography
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = Color(0xFFFFCC80), modifier = Modifier.size(18.dp))
                            Text(
                                text = "বাংলা অন-স্ক্রিন টেক্সট (ইউনিকোড)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showAddTextDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD47A00)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("টেক্সট যোগ", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ভিডিওতে সাবটাইটেল ও ব্র্যান্ড ক্যাপশনের খাঁটি বাংলা লেখা নির্ধারণ করুন।",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.editorTextOverlays.isEmpty()) {
                        Text(
                            text = "কোনো কাস্টম সাবটাইটেল যোগ করা হয়নি।",
                            color = Color(0xFF888888),
                            fontSize = 12.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.editorTextOverlays.forEach { overlay ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF281C1A)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "“${overlay.textBn}” (${overlay.startSeconds.toInt()}-${overlay.endSeconds.toInt()} সে.)",
                                            color = Color(0xFFFFE082),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Background Music with Auto Volume Ducking
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1514)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E2825))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        Text(
                            text = "আবহ সঙ্গীত ও অটো ভলিউম ডাকিং",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track selector chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        defaultMusicTracks.forEach { track ->
                            val isSelected = track.id == uiState.selectedMusicTrack.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF3A1E1A) else Color(0xFF241918),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFB703) else Color(0xFF3D2724)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setMusicTrack(track) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.titleBn, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(track.descriptionBn, color = Color(0xFFB0BEC5), fontSize = 11.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Automatic Ducking Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "স্বয়ংক্রিয় ভলিউম ডাকিং (Volume Ducking)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "ভয়েস-ওভার চলার সময় ব্যাকগ্রাউন্ড মিউজিক নিজে থেকেই নিচু হয়ে যাবে",
                                color = Color(0xFF9E9E9E),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = uiState.isMusicDuckingEnabled,
                            onCheckedChange = { viewModel.toggleMusicDucking(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB703),
                                checkedTrackColor = Color(0xFF8B1E0F)
                            )
                        )
                    }
                }
            }
        }

        // Section 5: Final 1080p MP4 Export Button
        item {
            Button(
                onClick = { viewModel.exportFinalVideoMp4() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("export_full_editor_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সম্পূর্ণ ভিডিও ১০৮০p MP4 হিসেবে এক্সপোর্ট করুন",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog for adding voice-over cue
    if (showAddVoiceDialog) {
        var voiceText by remember { mutableStateOf("অন্বেষার রসনা বিলাসের টাটকা স্বাদে আপনাদের মন ভরবেই।") }
        var startSecText by remember { mutableStateOf("0") }
        var endSecText by remember { mutableStateOf("6") }

        AlertDialog(
            onDismissRequest = { showAddVoiceDialog = false },
            containerColor = Color(0xFF221715),
            title = { Text("নতুন ভয়েস-ওভার কিউ", color = Color(0xFFFFD54F)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = voiceText,
                        onValueChange = { voiceText = it },
                        label = { Text("বাংলা বর্ণনা বাক্য") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startSecText,
                            onValueChange = { startSecText = it.filter { c -> c.isDigit() } },
                            label = { Text("শুরু (সে.)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = endSecText,
                            onValueChange = { endSecText = it.filter { c -> c.isDigit() } },
                            label = { Text("শেষ (সে.)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = startSecText.toFloatOrNull() ?: 0f
                        val e = endSecText.toFloatOrNull() ?: 6f
                        viewModel.addEditorNarrationCue(s, e, voiceText)
                        showAddVoiceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F))
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVoiceDialog = false }) { Text("বাতিল", color = Color.Gray) }
            }
        )
    }

    // Dialog for adding on-screen text
    if (showAddTextDialog) {
        var onScreenText by remember { mutableStateOf("খাঁটি ঘরোয়া রান্নার স্বর্গ — অন্বেষার রসনা বিলাস") }
        var startSecText by remember { mutableStateOf("0") }
        var endSecText by remember { mutableStateOf("8") }

        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            containerColor = Color(0xFF221715),
            title = { Text("অন-স্ক্রিন টেক্সট যোগ", color = Color(0xFFFFD54F)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = onScreenText,
                        onValueChange = { onScreenText = it },
                        label = { Text("বাংলা ক্যাপশন (ইউনিকোড)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startSecText,
                            onValueChange = { startSecText = it.filter { c -> c.isDigit() } },
                            label = { Text("শুরু (সে.)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = endSecText,
                            onValueChange = { endSecText = it.filter { c -> c.isDigit() } },
                            label = { Text("শেষ (সে.)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = startSecText.toFloatOrNull() ?: 0f
                        val e = endSecText.toFloatOrNull() ?: 8f
                        viewModel.addEditorTextOverlay(onScreenText, s, e)
                        showAddTextDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD47A00))
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) { Text("বাতিল", color = Color.Gray) }
            }
        )
    }
}

@Composable
private fun ClipRowItem(
    clip: MediaClip,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF2B1D1B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF422C28))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF8B1E0F)
                ) {
                    Text(
                        text = "#${index + 1}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Column {
                    Text(clip.displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("আসল ভিডিও ফরম্যাট • অপরিবর্তিত", color = Color(0xFF81C784), fontSize = 10.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (index > 0) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "উপরে নিন", tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                    }
                }
                if (index < totalCount - 1) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "নিচে নিন", tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
