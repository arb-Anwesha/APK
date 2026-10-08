package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogoPosition
import com.example.viewmodel.VideoStudioUiState
import com.example.viewmodel.VideoStudioViewModel

/**
 * Screen 3: ব্র্যান্ড সেটিংস (Brand Settings)
 * Dedicated for "অন্বেষার রসনা বিলাস"
 * - Business name, tagline, phone, website persistence
 * - Original logo upload & position controls
 * - Business authenticity constraints (no fabricated menu/prices/facilities)
 */
@Composable
fun BrandSettingsScreen(
    viewModel: VideoStudioViewModel,
    uiState: VideoStudioUiState,
    modifier: Modifier = Modifier
) {
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateBrandProfile(logoUri = uri.toString())
        }
    }

    val brand = uiState.brandProfile

    var name by remember { mutableStateOf(brand.businessName) }
    var tagline by remember { mutableStateOf(brand.tagline) }
    var phone by remember { mutableStateOf(brand.contactPhone) }
    var website by remember { mutableStateOf(brand.website) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("brand_settings_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Brand Profile Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261614)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD47A00))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B1E0F))
                            .border(2.dp, Color(0xFFFFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "অ",
                            color = Color(0xFFFFD54F),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = brand.businessName,
                        color = Color(0xFFFFD54F),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "“${brand.tagline}”",
                        color = Color(0xFFFFE082),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📞 ${brand.contactPhone}", color = Color.White, fontSize = 12.sp)
                        Text("🌐 ${brand.website}", color = Color(0xFF81C784), fontSize = 12.sp)
                    }
                }
            }
        }

        // Business Information Edit Fields
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1514)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E2825))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ব্যবসার মূল পরিচয়",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.updateBrandProfile(businessName = it)
                        },
                        label = { Text("প্রতিষ্ঠানের নাম") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFFFB703)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB703)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = {
                            tagline = it
                            viewModel.updateBrandProfile(tagline = it)
                        },
                        label = { Text("ট্যাগলাইন / স্লোগান") },
                        leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color(0xFFFFB703)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB703)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            viewModel.updateBrandProfile(phone = it)
                        },
                        label = { Text("যোগাযোগ নম্বর") },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFFFFB703)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB703)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = website,
                        onValueChange = {
                            website = it
                            viewModel.updateBrandProfile(website = it)
                        },
                        label = { Text("ওয়েবসাইট") },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFFFB703)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB703)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Logo Upload & Display Customization
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
                        Column {
                            Text(
                                text = "আসল লোগো সংরক্ষণ ও পুনঃব্যবহার",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "লোগো বিকৃতি ছাড়াই সকল ভিডিওতে স্বয়ংক্রিয়ভাবে বসবে",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("লোগো আপলোড", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Logo Overlay Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ভিডিওতে লোগো সিলমোহর দেখান",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Switch(
                            checked = brand.showLogoOverlay,
                            onCheckedChange = { viewModel.updateBrandProfile(showLogoOverlay = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB703),
                                checkedTrackColor = Color(0xFF8B1E0F)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Logo Placement Position
                    Text(
                        text = "লোগো স্থাপনের স্থান:",
                        color = Color(0xFFFFD54F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LogoPosition.values().forEach { pos ->
                            val isSelected = pos == brand.logoPosition
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF8B1E0F) else Color(0xFF261917),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF3E2825)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.updateBrandProfile(logoPosition = pos) }
                            ) {
                                Text(
                                    text = pos.labelBn,
                                    color = if (isSelected) Color.White else Color(0xFFAAAAAA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Authenticity Safeguards Notice
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B231C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            text = "ব্যবসায়িক সত্যতা ও সুরক্ষা নীতি",
                            color = Color(0xFF81C784),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "১. এআই আপনার প্রতিষ্ঠান বা প্রপার্টির কোনো কাল্পনিক ছবি/ভিডিও নিজে থেকে তৈরি করবে না (যদি না আপনি রেফারেন্স দেন)।\n" +
                                   "২. কোনো কাল্পনিক মেনু, মূল্যতালিকা বা ভুয়ো অফার উদ্ভাবন করা হবে না।\n" +
                                   "৩. আপলোড করা আসল ছবি ও ভিডিওর বিশুদ্ধতা ১০০% বজায় রাখা হয়।",
                            color = Color(0xFFC8E6C9),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
