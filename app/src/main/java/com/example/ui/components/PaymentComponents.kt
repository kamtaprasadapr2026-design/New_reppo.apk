package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.model.AppSettings
import com.example.model.Receipt
import com.example.model.UpiQrCode
import com.example.util.ReceiptPdfGenerator
import com.example.viewmodel.MainViewModel
import java.io.ByteArrayOutputStream
import java.util.Locale

@Composable
fun OnlinePaymentDialog(
    amount: Double,
    purpose: String,
    targetId: String,
    targetName: String,
    appSettings: AppSettings,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }
    val upiQrCodes by viewModel.upiQrCodes.collectAsState()

    // Apps options: Google Pay, PhonePe, Paytm, BHIM, All
    val appOptions = listOf("Google Pay", "PhonePe", "Paytm", "BHIM UPI", "Other UPI")
    var selectedAppName by remember { mutableStateOf(appOptions[0]) }

    // User entered reference & notes
    var userUtrRef by remember { mutableStateOf("") }
    var userRemarks by remember { mutableStateOf("") }

    // Screenshot/Photo/PDF Attachment states
    var attachmentBase64 by remember { mutableStateOf("") }
    var attachmentType by remember { mutableStateOf("IMAGE") }
    var attachmentFileName by remember { mutableStateOf("") }

    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            if (mimeType.contains("pdf", ignoreCase = true) || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                attachmentType = "PDF"
                attachmentFileName = "Payment_Proof_${System.currentTimeMillis() % 10000}.pdf"
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null && bytes.size < 700000) {
                        attachmentBase64 = "data:application/pdf;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                        Toast.makeText(context, "PDF proof attached successfully!", Toast.LENGTH_SHORT).show()
                    } else if (bytes != null) {
                        Toast.makeText(context, "PDF too large. Please select a file under 700KB or a photo screenshot.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed reading PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                attachmentType = "IMAGE"
                attachmentFileName = "Payment_Screenshot.png"
                val b64 = imageUriToCompactBase64(context, uri, 400)
                if (b64 != null) {
                    attachmentBase64 = b64
                    Toast.makeText(context, "Payment screenshot attached successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed processing image screenshot", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Find custom QR code for selected app or default
    val activeUpiItem: UpiQrCode? = remember(upiQrCodes, selectedAppName) {
        upiQrCodes.firstOrNull { it.appName.equals(selectedAppName, ignoreCase = true) }
            ?: upiQrCodes.firstOrNull { it.isPrimary }
            ?: upiQrCodes.firstOrNull()
    }

    val activeUpiId = activeUpiItem?.upiId?.ifEmpty { appSettings.upiId } ?: appSettings.upiId
    val activePayeeName = activeUpiItem?.payeeName?.ifEmpty { appSettings.upiPayeeName } ?: appSettings.upiPayeeName
    val activeQrUrl = activeUpiItem?.qrImageUrl?.ifEmpty { appSettings.qrImageUrl } ?: appSettings.qrImageUrl
    val activeBarcode = activeUpiItem?.barcodeNumber.orEmpty()

    fun launchUpiPayment(specificPackage: String? = null) {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val cleanNote = Uri.encode("Fee Payment $targetName")
        val upiUri = Uri.parse(
            "upi://pay?pa=$activeUpiId&pn=${Uri.encode(activePayeeName)}&am=$formattedAmount&cu=INR&tn=$cleanNote"
        )
        val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
            if (!specificPackage.isNullOrEmpty()) {
                setPackage(specificPackage)
            }
        }
        try {
            context.startActivity(intent)
            Toast.makeText(context, "Opening UPI App for payment of ₹${amount.toInt()}...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Fallback to generic chooser
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW, upiUri)
                context.startActivity(Intent.createChooser(genericIntent, "Pay via UPI App"))
            } catch (e2: Exception) {
                Toast.makeText(context, "Please install Google Pay, PhonePe or Paytm to pay automatically, or scan QR code.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("payment_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Instant UPI Fee Payment",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = appSettings.instituteName,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Summary Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payable:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(
                                text = "₹ ${amount.toInt()}",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                        }
                        Text("For: $targetName ($purpose)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payee: $activePayeeName", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", activeUpiId))
                                    Toast.makeText(context, "UPI ID copied: $activeUpiId", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy UPI ID", fontSize = 10.sp, color = Color(0xFF38BDF8))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // UPI App Tabs Selector (Google Pay, PhonePe, Paytm, BHIM, etc.)
                Text("Select Your UPI App / ऐप चुनें:", color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(appOptions) { app ->
                        val isSelected = selectedAppName == app
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAppName = app },
                            label = { Text(app, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct One-Click UPI App Launchers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Google Pay Button
                    Button(
                        onClick = { launchUpiPayment("com.google.android.apps.nbu.paisa.user") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // PhonePe Button
                    Button(
                        onClick = { launchUpiPayment("com.phonepe.app") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5F259F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PhonePe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Paytm Button
                    Button(
                        onClick = { launchUpiPayment("net.one97.paytm") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B9F1)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paytm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Generic Pay Button
                OutlinedButton(
                    onClick = { launchUpiPayment(null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Other Installed UPI App (BHIM, CRED, Bank)", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Official Admin QR Code & Barcode Display (renders via AppAsyncImage to avoid blank white cards!)
                if (activeQrUrl.isNotEmpty() || appSettings.qrImageUrl.isNotEmpty()) {
                    val displayQr = activeQrUrl.ifEmpty { appSettings.qrImageUrl }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Admin Official $selectedAppName QR Code",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.size(180.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                AppAsyncImage(
                                    model = displayQr,
                                    contentDescription = "Official UPI Payment QR",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "UPI ID: $activeUpiId",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Barcode Display
                if (activeBarcode.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("||| |||| || ||||| ||| |||| ||| |||||||", fontFamily = FontFamily.Monospace, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            Text("Barcode Number: $activeBarcode", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Proof / Screenshot Attachment Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Payment Proof Attachment / स्क्रीनशॉट:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (attachmentBase64.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                if (attachmentType == "IMAGE") {
                                    Card(
                                        modifier = Modifier.size(54.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        AppAsyncImage(
                                            model = attachmentBase64,
                                            contentDescription = "Screenshot Preview",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = attachmentFileName,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Proof attached successfully",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp
                                    )
                                }
                                IconButton(onClick = {
                                    attachmentBase64 = ""
                                    attachmentFileName = ""
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { attachmentPickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (attachmentBase64.isEmpty()) "Attach Payment Screenshot / Receipt (Photo or PDF)" else "Replace Attachment",
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = userUtrRef,
                            onValueChange = { userUtrRef = it },
                            label = { Text("UTR / Transaction Ref (12-digit UPI reference)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = userRemarks,
                            onValueChange = { userRemarks = it },
                            label = { Text("Student Note / Sender Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instant Confirm & Auto-Generate A4 Receipt Button
                Button(
                    onClick = {
                        isSubmitting = true
                        val autoRef = userUtrRef.trim().ifEmpty { "UPI-${System.currentTimeMillis() % 100000000}" }
                        val autoReceiptNumber = "REC-${System.currentTimeMillis() % 1000000}"

                        // Create receipt object for immediate PDF generation
                        val localReceipt = Receipt(
                            id = "rec_${System.currentTimeMillis()}",
                            receiptNumber = autoReceiptNumber,
                            paymentId = autoRef,
                            userId = viewModel.currentUser.value?.uid.orEmpty(),
                            studentName = viewModel.userProfile.value?.displayName ?: "Student",
                            studentId = viewModel.studentProfile.value?.studentId ?: "NB-STD",
                            userEmail = viewModel.currentUser.value?.email.orEmpty(),
                            purpose = purpose,
                            targetName = targetName,
                            amount = amount,
                            status = "VERIFIED",
                            transactionReference = autoRef
                        )

                        // 1. Submit payment record to Firestore with attachment
                        viewModel.submitPayment(
                            amount = amount,
                            purpose = purpose,
                            targetId = targetId,
                            targetName = targetName,
                            utr = autoRef,
                            attachmentUrl = attachmentBase64,
                            attachmentType = attachmentType,
                            notes = userRemarks.trim()
                        ) { success, msg ->
                            isSubmitting = false
                            if (success) {
                                // 2. Automatically generate A4 Size PDF Receipt!
                                val pdfFile = ReceiptPdfGenerator.generateA4ReceiptPdf(
                                    context = context,
                                    receipt = localReceipt,
                                    instituteName = appSettings.instituteName
                                )

                                if (pdfFile != null && pdfFile.exists()) {
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            pdfFile
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Open Generated A4 PDF Receipt"))
                                        Toast.makeText(context, "Payment Submitted! A4 Size PDF Receipt Generated!", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "A4 Receipt saved to ${pdfFile.name}", Toast.LENGTH_LONG).show()
                                    }
                                }
                                onDismiss()
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submitting Payment...")
                    } else {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Payment & Get A4 Receipt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Compact Base64 encoder for image screenshots
 */
fun imageUriToCompactBase64(context: Context, uri: Uri, maxDimension: Int = 400): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
                decoder.isMutableRequired = true
                val width = info.size.width
                val height = info.size.height
                val scale = if (width > height) maxDimension.toFloat() / width else maxDimension.toFloat() / height
                if (scale < 1.0f) {
                    decoder.setTargetSize((width * scale).toInt(), (height * scale).toInt())
                }
            }
        } else {
            val stream = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(stream)
            if (original != null) {
                val scale = maxDimension.toFloat() / original.width.coerceAtLeast(original.height)
                if (scale < 1.0f) {
                    Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
                } else {
                    original
                }
            } else null
        } ?: return null

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val bytes = baos.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}
