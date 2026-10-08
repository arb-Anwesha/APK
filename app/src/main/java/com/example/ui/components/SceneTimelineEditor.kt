package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.model.SceneItem

/**
 * Interactive Timeline & Scene cards.
 * Displays scenes, durations, voice-over text cues, on-screen text,
 * and allows in-place editing of script, durations, and visuals.
 */
@Composable
fun SceneTimelineEditor(
    scenes: List<SceneItem>,
    activeSceneIndex: Int,
    onSelectScene: (Int) -> Unit,
    onUpdateTitle: (String, String) -> Unit,
    onUpdateNarration: (String, String) -> Unit,
    onUpdateOnScreenText: (String, String) -> Unit,
    onUpdateDuration: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingScene by remember { mutableStateOf<SceneItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scene_timeline_editor")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(20.dp))
                Text(
                    text = "দৃশ্য ও স্ক্রিপ্ট টাইমলাইন (${scenes.size}টি দৃশ্য)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "মোট: ${scenes.sumOf { it.durationSeconds }} সেকেন্ড",
                fontSize = 13.sp,
                color = Color(0xFFFFD54F)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal visual timeline strip
        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(scenes) { index, scene ->
                val isSelected = index == activeSceneIndex
                TimelineSceneCard(
                    scene = scene,
                    isSelected = isSelected,
                    onClick = { onSelectScene(index) },
                    onEditClick = { editingScene = scene }
                )
            }
        }
    }

    // Modal Sheet / Dialog to edit selected scene details
    editingScene?.let { scene ->
        SceneEditDialog(
            scene = scene,
            onDismiss = { editingScene = null },
            onSave = { newTitle, newNarration, newText, newSec ->
                onUpdateTitle(scene.id, newTitle)
                onUpdateNarration(scene.id, newNarration)
                onUpdateOnScreenText(scene.id, newText)
                onUpdateDuration(scene.id, newSec)
                editingScene = null
            }
        )
    }
}

@Composable
private fun TimelineSceneCard(
    scene: SceneItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("scene_card_${scene.sceneNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF381A16) else Color(0xFF221614)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFFFB703) else Color(0xFF4A3430)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with Scene Number and Duration badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF8B1E0F)
                ) {
                    Text(
                        text = "দৃশ্য ${scene.sceneNumber}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(13.dp))
                    Text(
                        text = "${scene.durationSeconds} সে.",
                        color = Color(0xFFFFD54F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scene Title
            Text(
                text = scene.titleBn,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Voice-over Bengali script snippet
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(14.dp))
                Text(
                    text = scene.narrationScriptBn.ifBlank { "ভয়েস-ওভার নেই" },
                    color = Color(0xFFCFD8DC),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // On-Screen Text snippet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Subtitles, contentDescription = null, tint = Color(0xFFFFCC80), modifier = Modifier.size(13.dp))
                Text(
                    text = scene.onScreenTextBn.ifBlank { "টেক্সট নেই" },
                    color = Color(0xFFFFCC80),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer with edit button and media status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (scene.isAiVisual) "এআই দৃশ্য" else (scene.assignedMediaName ?: "আসল ফুটেজ"),
                    color = Color(0xFFAAAAAA),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4A3430),
                    modifier = Modifier.clickable { onEditClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                        Text("এডিট", color = Color(0xFFFFD54F), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SceneEditDialog(
    scene: SceneItem,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf(scene.titleBn) }
    var narration by remember { mutableStateOf(scene.narrationScriptBn) }
    var onScreenText by remember { mutableStateOf(scene.onScreenTextBn) }
    var durationText by remember { mutableStateOf(scene.durationSeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF201614),
        title = {
            Text(
                text = "দৃশ্য ${scene.sceneNumber} সম্পাদনা করুন",
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("দৃশ্যের নাম") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFB703),
                        focusedLabelColor = Color(0xFFFFB703)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = narration,
                    onValueChange = { narration = it },
                    label = { Text("পশ্চিমবঙ্গীয় বাংলা ভয়েস-ওভার স্ক্রিপ্ট") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFB703),
                        focusedLabelColor = Color(0xFFFFB703)
                    ),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = onScreenText,
                    onValueChange = { onScreenText = it },
                    label = { Text("অন-স্ক্রিন বাংলা টেক্সট (ইউনিকোড)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFB703),
                        focusedLabelColor = Color(0xFFFFB703)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { char -> char.isDigit() } },
                    label = { Text("সময়কাল (সেকেন্ড)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFB703),
                        focusedLabelColor = Color(0xFFFFB703)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sec = durationText.toIntOrNull() ?: scene.durationSeconds
                    onSave(title, narration, onScreenText, sec)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F))
            ) {
                Text("সংরক্ষণ করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Color(0xFFAAAAAA))
            }
        }
    )
}
