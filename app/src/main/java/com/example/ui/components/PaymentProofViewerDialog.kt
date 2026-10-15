package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@Composable
fun PaymentProofViewerDialog(
    attachmentUrl: String,
    attachmentType: String,
    studentName: String,
    utrRef: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPdf = attachmentType.equals("PDF", ignoreCase = true) || attachmentUrl.contains("application/pdf")

    val bitmap = remember(attachmentUrl, isPdf) {
        if (!isPdf && attachmentUrl.isNotEmpty()) {
            try {
                val cleanBase64 = if (attachmentUrl.contains(",")) attachmentUrl.substringAfter(",") else attachmentUrl
                val bytes = Base64.decode(cleanBase64.trim(), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun openOrDownloadPdf() {
        try {
            val cleanBase64 = if (attachmentUrl.contains(",")) attachmentUrl.substringAfter(",") else attachmentUrl
            val bytes = Base64.decode(cleanBase64.trim(), Base64.DEFAULT)
            val dir = File(context.cacheDir, "attachments")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Student_Payment_Proof_${System.currentTimeMillis() % 10000}.pdf")
            FileOutputStream(file).use { it.write(bytes) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "View Student Payment PDF Proof"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareOrSaveImage() {
        if (bitmap != null) {
            try {
                val dir = File(context.cacheDir, "attachments")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "Payment_Screenshot_${System.currentTimeMillis() % 10000}.png")
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share/Save Screenshot Proof"))
            } catch (e: Exception) {
                Toast.makeText(context, "Saved to cache: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Payment Proof Attachment",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "$studentName • Ref: $utrRef",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content View Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF020617), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPdf) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Student PDF Document Attached", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Official receipt or bank transfer slip", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { openOrDownloadPdf() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open / Download PDF Document")
                            }
                        }
                    } else if (bitmap != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Payment Proof Screenshot",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else if (attachmentUrl.isNotBlank()) {
                        AppAsyncImage(
                            model = attachmentUrl,
                            contentDescription = "Payment Proof",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("No attachment available for this payment.", color = Color.Gray, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isPdf && bitmap != null) {
                        OutlinedButton(
                            onClick = { shareOrSaveImage() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Save Photo", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    } else if (isPdf) {
                        OutlinedButton(
                            onClick = { openOrDownloadPdf() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View PDF", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
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
