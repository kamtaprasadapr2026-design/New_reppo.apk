package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppVideoPlayerDialog(
    videoUrl: String,
    videoTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = videoTitle.ifEmpty { "Video Playback" },
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Neol Bharat Video Player",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Video Surface Box
                val isYouTube = videoUrl.contains("youtube.com", ignoreCase = true) || videoUrl.contains("youtu.be", ignoreCase = true)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isYouTube) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "YouTube Video Stream",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap below to stream this video directly on YouTube or your browser.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open YouTube link", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play on YouTube App", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (videoUrl.isNotBlank() && !hasError) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val mediaController = MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        hasError = true
                                        true
                                    }
                                    try {
                                        setVideoURI(Uri.parse(videoUrl))
                                    } catch (e: Exception) {
                                        hasError = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (!isYouTube && (hasError || videoUrl.isBlank())) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = if (videoUrl.isBlank()) "No video URL specified." else "In-app playback unavailable for this stream.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (videoUrl.isNotBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(videoUrl), "video/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Open with Video Player"))
                                        } catch (e: Exception) {
                                            try {
                                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                                context.startActivity(webIntent)
                                            } catch (e2: Exception) {
                                                Toast.makeText(context, "Cannot open video stream.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play in External Player / Browser")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (videoUrl.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(videoUrl), "video/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open in External Video App"))
                                } catch (e: Exception) {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
                                    } catch (e2: Exception) {
                                        Toast.makeText(context, "No external video app found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open External App", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
