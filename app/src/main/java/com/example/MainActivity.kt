package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AiVideoGeneratorScreen
import com.example.ui.screens.BrandSettingsScreen
import com.example.ui.screens.FullEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.VideoStudioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VideoStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: VideoStudioViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF100C0B),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B1E0F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("অ", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "অন্বেষার ভিডিও স্টুডিও",
                                color = Color(0xFFFFD54F),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.brandProfile.businessName,
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1210)
                ),
                actions = {
                    // Quick Export Trigger
                    Button(
                        onClick = { viewModel.exportFinalVideoMp4() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এক্সপোর্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1B1210),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.FULL_EDITOR,
                    onClick = { viewModel.setNavTab(AppNavTab.FULL_EDITOR) },
                    icon = { Icon(Icons.Default.Movie, contentDescription = AppNavTab.FULL_EDITOR.titleBn) },
                    label = { Text(AppNavTab.FULL_EDITOR.titleBn, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFD54F),
                        selectedTextColor = Color(0xFFFFD54F),
                        indicatorColor = Color(0xFF3E1F1A),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888)
                    )
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.AI_STUDIO,
                    onClick = { viewModel.setNavTab(AppNavTab.AI_STUDIO) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = AppNavTab.AI_STUDIO.titleBn) },
                    label = { Text(AppNavTab.AI_STUDIO.titleBn, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFD54F),
                        selectedTextColor = Color(0xFFFFD54F),
                        indicatorColor = Color(0xFF3E1F1A),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888)
                    )
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.BRAND_SETTINGS,
                    onClick = { viewModel.setNavTab(AppNavTab.BRAND_SETTINGS) },
                    icon = { Icon(Icons.Default.Business, contentDescription = AppNavTab.BRAND_SETTINGS.titleBn) },
                    label = { Text(AppNavTab.BRAND_SETTINGS.titleBn, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFD54F),
                        selectedTextColor = Color(0xFFFFD54F),
                        indicatorColor = Color(0xFF3E1F1A),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                AppNavTab.FULL_EDITOR -> FullEditorScreen(viewModel = viewModel, uiState = uiState)
                AppNavTab.AI_STUDIO -> AiVideoGeneratorScreen(viewModel = viewModel, uiState = uiState)
                AppNavTab.BRAND_SETTINGS -> BrandSettingsScreen(viewModel = viewModel, uiState = uiState)
            }

            // Exporting Modal Overlay
            if (uiState.isExporting) {
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFB703),
                            modifier = Modifier.size(54.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "1080p MP4 ভিডিও এক্সপোর্ট হচ্ছে...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.exportStatusMessage,
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { uiState.exportProgress },
                            color = Color(0xFFFFB703),
                            trackColor = Color(0xFF3E2825),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(uiState.exportProgress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Export Complete Dialog
            uiState.exportedVideoUri?.let { uri ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissExportDialog() },
                    containerColor = Color(0xFF221614),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784))
                            Text("ভিডিও এক্সপোর্ট সম্পন্ন!", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = "আপনার সম্পূর্ণ ১০৮০p MP4 ভিডিও গ্যালারিতে সংরক্ষিত হয়েছে।",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "সংরক্ষণ স্থান: Movies/AnweshaVideoStudio",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "ভিডিও শেয়ার করুন"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E0F))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("শেয়ার করুন")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissExportDialog() }) {
                            Text("ঠিক আছে", color = Color(0xFFFFD54F))
                        }
                    }
                )
            }

            // Error Dialog
            uiState.errorMessage?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissExportDialog() },
                    containerColor = Color(0xFF2A1513),
                    title = { Text("বিজ্ঞপ্তি", color = Color(0xFFEF5350)) },
                    text = { Text(error, color = Color.White) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissExportDialog() }) {
                            Text("ঠিক আছে", color = Color.White)
                        }
                    }
                )
            }
        }
    }
}
