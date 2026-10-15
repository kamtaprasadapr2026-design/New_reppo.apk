package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Robust image loader that automatically handles:
 * 1. Base64 Data URLs (e.g. "data:image/png;base64,..." or raw base64 strings)
 * 2. Standard HTTP/HTTPS Web URLs
 * 3. Local content:// and file:// URIs
 * Never renders as an empty white box on failure; displays a graceful placeholder.
 */
@Composable
fun AppAsyncImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholderColor: Color = Color(0xFF1E293B),
    fallbackIcon: @Composable (() -> Unit)? = null
) {
    if (model.isBlank()) {
        Box(
            modifier = modifier.background(placeholderColor),
            contentAlignment = Alignment.Center
        ) {
            if (fallbackIcon != null) {
                fallbackIcon()
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        return
    }

    val isBase64 = remember(model) {
        model.startsWith("data:image", ignoreCase = true) ||
                (model.length > 100 && !model.startsWith("http", ignoreCase = true) && !model.startsWith("content:", ignoreCase = true) && !model.startsWith("file:", ignoreCase = true))
    }

    if (isBase64) {
        val bitmap = remember(model) {
            try {
                val cleanBase64 = if (model.contains(",")) {
                    model.substringAfter(",")
                } else {
                    model
                }
                val bytes = Base64.decode(cleanBase64.trim(), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = modifier.background(placeholderColor),
                contentAlignment = Alignment.Center
            ) {
                if (fallbackIcon != null) {
                    fallbackIcon()
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    } else {
        val context = LocalContext.current
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFF38BDF8)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (fallbackIcon != null) {
                        fallbackIcon()
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        )
    }
}
