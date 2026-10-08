package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrandProfile
import com.example.model.LogoPosition
import com.example.model.SceneItem
import com.example.model.VideoAspectRatio

/**
 * High-fidelity Video Stage Preview player with Aspect Ratio switching (9:16, 16:9, 1:1),
 * brand logo badge, Bengali on-screen text typography, and Ken Burns motion.
 */
@Composable
fun VideoPlayerStage(
    currentScene: SceneItem?,
    brandProfile: BrandProfile,
    aspectRatio: VideoAspectRatio,
    isPlaying: Boolean,
    playbackProgress: Float,
    isVoiceOverActive: Boolean,
    onTogglePlay: () -> Unit,
    onTriggerVoiceOver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ken_burns")
    val zoomScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoom_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_stage"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140C0B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Ratio Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Aspect Ratio Video Canvas Box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(aspectRatio.ratioValue)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3B0E07),
                                    Color(0xFF7A1809),
                                    Color(0xFF2B0703)
                                )
                            )
                        )
                        .border(1.5.dp, Color(0xFFD47A00).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated Background Visual Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(if (isPlaying) zoomScale else 1.0f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Decorative traditional Bengali culinary aura
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB703).copy(alpha = 0.12f))
                        )
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9E00).copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "ঐতিহ্যবাহী রান্না",
                                tint = Color(0xFFFFE082),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    // Top Scene Badge and Tag
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentScene != null) "দৃশ্য #${currentScene.sceneNumber}: ${currentScene.titleBn}" else "ভিডিও স্টুডিও প্রিভিউ",
                            color = Color(0xFFFFD54F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Brand Logo Overlay (User's original business branding)
                    if (brandProfile.showLogoOverlay) {
                        val brandAlignment = when (brandProfile.logoPosition) {
                            LogoPosition.TOP_LEFT -> Alignment.TopStart
                            LogoPosition.TOP_RIGHT -> Alignment.TopEnd
                            LogoPosition.BOTTOM_LEFT -> Alignment.BottomStart
                            LogoPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
                        }
                        Surface(
                            modifier = Modifier
                                .align(brandAlignment)
                                .padding(10.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.8f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD47A00)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("অ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = brandProfile.businessName,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // On-Screen Bengali Text Overlay (Unicode Typography)
                    if (currentScene != null && currentScene.onScreenTextBn.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 12.dp, vertical = 18.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.78f))
                                .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentScene.onScreenTextBn,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Voice-over live waveform indicator
                    if (isVoiceOverActive) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 10.dp, bottom = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.9f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("পশ্চিমবঙ্গীয় কণ্ঠস্বর লাইভ", color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }

            // Timeline Progress Bar
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { playbackProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFFFB703),
                trackColor = Color.White.copy(alpha = 0.2f),
            )

            // Playback and Voiceover Controls
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF251A18),
                        modifier = Modifier.clickable { onTogglePlay() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "থামান" else "চালান",
                                tint = Color(0xFFFFD54F)
                            )
                            Text(
                                text = if (isPlaying) "পজ করুন" else "প্রিভিউ চালান",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF251A18),
                        modifier = Modifier.clickable { onTriggerVoiceOver() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "ভয়েস-ওভার শুনুন",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ভয়েস শুনুন",
                                color = Color(0xFF81C784),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Aspect ratio and resolution tag
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF33201D)
                    ) {
                        Text(
                            text = aspectRatio.ratioStr,
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1B5E20)
                    ) {
                        Text(
                            text = "1080p FHD",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
