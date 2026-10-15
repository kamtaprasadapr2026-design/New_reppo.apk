package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.components.AppVideoPlayerDialog
import com.example.ui.components.PaymentProofViewerDialog
import com.example.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class AdminTabSpec(val index: Int, val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    onCloseAdmin: () -> Unit
) {
    var adminSection by remember { mutableIntStateOf(0) }
    // 0: Dashboard, 1: Students, 2: Education & Videos, 3: Attendance, 4: Fees, 5: Payments, 6: Settings & Branding

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onCloseAdmin) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Admin", tint = Color.White)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "NEOL BHARAT ADMIN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Admin: ${userProfile.email}",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                maxLines = 1
                            )
                        }
                    },
                    actions = {
                        // Quick Switch to Student Mode
                        TextButton(
                            onClick = {
                                viewModel.selectExperienceMode("STUDENT")
                                onCloseAdmin()
                            }
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Student", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        }

                        // Quick Switch to Entertainment Mode
                        TextButton(
                            onClick = {
                                viewModel.selectExperienceMode("ENTERTAINMENT")
                                onCloseAdmin()
                            }
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Media", color = Color(0xFFF59E0B), fontSize = 11.sp)
                        }

                        IconButton(onClick = onCloseAdmin) {
                            Icon(Icons.Default.Close, contentDescription = "Close Admin Panel", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                )

                // Mobile-Friendly Admin Navigation Tabs at Top (Always visible without desktop mode!)
                ScrollableTabRow(
                    selectedTabIndex = adminSection,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        if (adminSection < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[adminSection]),
                                color = Color(0xFF38BDF8),
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    val adminTabs = listOf(
                        AdminTabSpec(0, "Dashboard", Icons.Default.Dashboard),
                        AdminTabSpec(1, "Suchna", Icons.Default.Campaign),
                        AdminTabSpec(2, "Students", Icons.Default.People),
                        AdminTabSpec(3, "Edu & Video", Icons.Default.PlayCircle),
                        AdminTabSpec(4, "Attendance", Icons.Default.DateRange),
                        AdminTabSpec(5, "Fees (शुल्क)", Icons.Default.AttachMoney),
                        AdminTabSpec(6, "Payments", Icons.Default.Payment),
                        AdminTabSpec(7, "Settings", Icons.Default.Settings)
                    )
                    adminTabs.forEach { tabItem ->
                        val isSelected = adminSection == tabItem.index
                        Tab(
                            selected = isSelected,
                            onClick = { adminSection = tabItem.index },
                            icon = {
                                Icon(
                                    tabItem.icon,
                                    contentDescription = tabItem.label,
                                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = tabItem.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF020617))
        ) {
            when (adminSection) {
                0 -> AdminDashboardSection(
                    viewModel = viewModel,
                    onNavigateToSection = { adminSection = it }
                )
                1 -> AdminSuchnaSection(viewModel = viewModel)
                2 -> AdminStudentsSection(viewModel = viewModel)
                3 -> AdminEducationAndVideoSection(viewModel = viewModel)
                4 -> AdminAttendanceSection(viewModel = viewModel)
                5 -> AdminFeesSection(viewModel = viewModel)
                6 -> AdminPaymentsSection(viewModel = viewModel)
                7 -> AdminSettingsAndBrandingSection(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminDashboardSection(
    viewModel: MainViewModel,
    onNavigateToSection: (Int) -> Unit = {}
) {
    val users by viewModel.allUsers.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val videos by viewModel.educationVideos.collectAsState()
    val entertainmentVideos by viewModel.entertainmentVideos.collectAsState()
    val allVideos = videos + entertainmentVideos
    val payments by viewModel.allPayments.collectAsState()
    val fees by viewModel.allFees.collectAsState()

    val totalCollected = payments.filter { it.status == "VERIFIED" }.sumOf { it.amount }
    val pendingFees = fees.sumOf { it.pendingAmount }

    // Dialog state for clicking stat cards
    var activeDetailModal by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operational Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                    text = "Tap any card to view details",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Total Users (Click to view)",
                    value = "${users.size}",
                    icon = Icons.Default.People,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "USERS" }
                )
                AdminStatCard(
                    title = "Total Courses (Click to view)",
                    value = "${courses.size}",
                    icon = Icons.Default.School,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "COURSES" }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Fee Collected (Click to view)",
                    value = "₹${totalCollected.toInt()}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "COLLECTED_FEES" }
                )
                AdminStatCard(
                    title = "Outstanding Fees (Click to view)",
                    value = "₹${pendingFees.toInt()}",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "OUTSTANDING_FEES" }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Total Videos (Click to view)",
                    value = "${allVideos.size}",
                    icon = Icons.Default.PlayCircle,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "VIDEOS" }
                )
                val pendingPayments = payments.count { it.status == "PENDING_VERIFICATION" }
                AdminStatCard(
                    title = "Pending Verifications",
                    value = "$pendingPayments",
                    icon = Icons.Default.PendingActions,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection(6) }
                )
            }
        }
    }

    // Modal dialogs showing one-by-one detail on clicking cards
    activeDetailModal?.let { modal ->
        AlertDialog(
            onDismissRequest = { activeDetailModal = null },
            title = {
                Text(
                    text = when (modal) {
                        "USERS" -> "Total Registered Users (${users.size})"
                        "COURSES" -> "All Courses Details (${courses.size})"
                        "COLLECTED_FEES" -> "Fee Collected Details (₹${totalCollected.toInt()})"
                        "OUTSTANDING_FEES" -> "Outstanding Dues Details (₹${pendingFees.toInt()})"
                        "VIDEOS" -> "All Videos Details (${allVideos.size})"
                        else -> "Details"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    when (modal) {
                        "USERS" -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(users) { u ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(u.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Email: ${u.email}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                            Text("Role: ${u.role} • Mode: ${u.selectedMode} • Status: ${if (u.isBlocked) "Blocked" else "Active"}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        "COURSES" -> {
                            if (courses.isEmpty()) {
                                Text("No courses available.", color = Color.Gray)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(courses) { c ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(c.title, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(c.description, color = Color(0xFF94A3B8), fontSize = 12.sp, maxLines = 2)
                                                Text("Category: ${c.category} • ${if (c.isFree) "Free" else "Price: ₹${c.price.toInt()}"}", color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "COLLECTED_FEES" -> {
                            val verifiedPayments = payments.filter { it.status == "VERIFIED" }
                            if (verifiedPayments.isEmpty()) {
                                Text("No verified fee payments yet.", color = Color.Gray)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(verifiedPayments) { p ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(p.userName, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text("₹${p.amount.toInt()}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                                }
                                                Text("Purpose: ${p.purpose} • Target: ${p.targetName}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                                Text("Ref / Receipt: ${p.receiptId.ifEmpty { p.transactionReference }}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "OUTSTANDING_FEES" -> {
                            val dueFees = fees.filter { it.pendingAmount > 0 }
                            if (dueFees.isEmpty()) {
                                Text("No outstanding fees! All student accounts are clear.", color = Color(0xFF10B981))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(dueFees) { f ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(f.studentName, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text("Due: ₹${f.pendingAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                                                }
                                                Text("Month: ${f.month} • Type: ${f.feeType}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                                Text("Total: ₹${f.totalAmount.toInt()} | Paid: ₹${f.paidAmount.toInt()} | Status: ${f.status}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "VIDEOS" -> {
                            if (allVideos.isEmpty()) {
                                Text("No videos uploaded.", color = Color.Gray)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(allVideos) { v ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(v.title, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("Section: ${v.section} • Category: ${v.category} • ${if (v.isPaid) "₹${v.price.toInt()}" else "Free"}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                                Text(v.videoUrl, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDetailModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(title, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun AdminStudentsSection(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val context = LocalContext.current

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentName by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var studentGrade by remember { mutableStateOf("10th Standard") }
    var studentPhone by remember { mutableStateOf("") }
    var studentPhotoUrl by remember { mutableStateOf("") }

    // Edit student profile state
    var editingUser by remember { mutableStateOf<UserProfile?>(null) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editGrade by remember { mutableStateOf("10th Standard") }
    var editPhone by remember { mutableStateOf("") }
    var editPhotoUrl by remember { mutableStateOf("") }

    var studentToDelete by remember { mutableStateOf<UserProfile?>(null) }

    // Android Photo Picker for Student Profile Photo Upload
    val studentPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageLocally(context, uri, "std_avatar_${System.currentTimeMillis()}.png")
            if (savedPath != null) {
                if (editingUser != null) {
                    editPhotoUrl = savedPath
                } else {
                    studentPhotoUrl = savedPath
                }
                Toast.makeText(context, "Student profile photo selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student & User Records",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Button(
                    onClick = {
                        showAddStudentDialog = true
                        studentPhotoUrl = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Student Profile")
                }
            }
        }

        // Add Student Form Card
        if (showAddStudentDialog) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upload & Create New Student Profile", fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(onClick = { showAddStudentDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Photo Picker Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (studentPhotoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = studentPhotoUrl,
                                    contentDescription = "Profile Preview",
                                    modifier = Modifier.size(52.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            OutlinedButton(
                                onClick = { studentPhotoPicker.launch("image/*") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (studentPhotoUrl.isEmpty()) "Upload Student Photo" else "Change Photo")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = studentEmail,
                            onValueChange = { studentEmail = it },
                            label = { Text("Student Google/Contact Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = studentGrade,
                            onValueChange = { studentGrade = it },
                            label = { Text("Class / Course Grade") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = studentPhone,
                            onValueChange = { studentPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (studentName.isNotEmpty() && studentEmail.isNotEmpty()) {
                                    viewModel.adminCreateStudent(studentName, studentEmail, studentGrade, studentPhone, studentPhotoUrl) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            showAddStudentDialog = false
                                            studentName = ""
                                            studentEmail = ""
                                            studentPhone = ""
                                            studentPhotoUrl = ""
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Name and Email are required", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Create & Sync Student Profile")
                        }
                    }
                }
            }
        }

        items(users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (user.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(42.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(user.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(user.email, color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("Role: ${user.role} • Mode: ${user.selectedMode}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Edit Student Profile Button
                        IconButton(onClick = {
                            editingUser = user
                            editName = user.displayName
                            editEmail = user.email
                            editGrade = "10th Standard"
                            editPhone = ""
                            editPhotoUrl = user.photoUrl
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFF38BDF8))
                        }

                        // Block/Unblock Button
                        IconButton(onClick = {
                            viewModel.adminToggleBlockUser(user.uid, !user.isBlocked) {
                                Toast.makeText(context, if (!user.isBlocked) "User blocked" else "User unblocked", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (user.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                                contentDescription = "Block or Unblock",
                                tint = if (user.isBlocked) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                            )
                        }

                        // Delete Student Button
                        IconButton(onClick = {
                            studentToDelete = user
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Student Profile", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    // Edit Student Profile Dialog
    editingUser?.let { target ->
        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text("Edit Student Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (editPhotoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = editPhotoUrl,
                                contentDescription = "Edit Preview",
                                modifier = Modifier.size(50.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        OutlinedButton(
                            onClick = { studentPhotoPicker.launch("image/*") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Upload/Replace Photo")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editGrade, onValueChange = { editGrade = it }, label = { Text("Class / Grade") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotEmpty() && editEmail.isNotEmpty()) {
                            viewModel.adminUpdateStudentProfile(target.uid, editName, editEmail, editGrade, editPhone, editPhotoUrl) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) editingUser = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save & Sync Profile")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog for deleting student
    studentToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student Profile?") },
            text = { Text("Are you sure you want to delete ${target.displayName} (${target.email})? This permanently deletes their profile from Firestore.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeleteUser(target.uid) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            studentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminSuchnaSection(viewModel: MainViewModel) {
    val announcements by viewModel.announcements.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingAnnouncement by remember { mutableStateOf<Announcement?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("ALL") } // ALL, PERSONAL
    var selectedStudentUser by remember { mutableStateOf<UserProfile?>(null) }
    var selectedStudentEmail by remember { mutableStateOf("") }
    var studentSearchQuery by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") } // NORMAL, URGENT
    var scheduledDate by remember { mutableStateOf("") }

    var announcementToDelete by remember { mutableStateOf<Announcement?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Suchna / Announcements (सूचना)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    Text("Create, schedule, broadcast or send to selected students", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        editingAnnouncement = null
                        title = ""
                        content = ""
                        targetAudience = "ALL"
                        selectedStudentEmail = ""
                        priority = "NORMAL"
                        scheduledDate = ""
                        showCreateDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Suchna")
                }
            }
        }

        if (announcements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No active announcements. Tap '+ Create Suchna' above to post a notice.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(announcements) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (notice.priority == "URGENT") Icons.Default.Warning else Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = if (notice.priority == "URGENT") Color(0xFFEF4444) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(notice.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Edit Announcement
                                IconButton(onClick = {
                                    editingAnnouncement = notice
                                    title = notice.title
                                    content = notice.content
                                    targetAudience = notice.targetAudience
                                    selectedStudentEmail = notice.targetStudentEmail
                                    priority = notice.priority
                                    scheduledDate = notice.scheduledDate
                                    showCreateDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Suchna", tint = Color(0xFF38BDF8))
                                }

                                // Delete Announcement
                                IconButton(onClick = { announcementToDelete = notice }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Suchna", tint = Color(0xFFEF4444))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(notice.content, color = Color(0xFFE2E8F0), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Badge(containerColor = Color(0xFF1E293B)) {
                                    Text("Audience: ${notice.targetAudience}", color = Color(0xFF38BDF8), fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                                }
                                if (notice.targetStudentEmail.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFF1E293B)) {
                                        Text("For: ${notice.targetStudentEmail}", color = Color(0xFFFBBF24), fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }
                            if (notice.scheduledDate.isNotEmpty()) {
                                Text("Schedule: ${notice.scheduledDate}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Announcement Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (editingAnnouncement == null) "Create New Suchna" else "Edit Suchna", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / शीर्षक") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Notice Message / सूचना विवरण") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Target Students / Audience:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = targetAudience == "ALL",
                            onClick = { targetAudience = "ALL" },
                            label = { Text("Broadcast to All") }
                        )
                        FilterChip(
                            selected = targetAudience == "PERSONAL",
                            onClick = { targetAudience = "PERSONAL" },
                            label = { Text("One-by-One Personal Notice") }
                        )
                    }

                    if (targetAudience == "PERSONAL") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Select Target Student (एक छात्र चुनें):", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = studentSearchQuery,
                                    onValueChange = { studentSearchQuery = it },
                                    placeholder = { Text("Search by name/email...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                val matchedUsers = users.filter {
                                    studentSearchQuery.isEmpty() ||
                                    it.displayName.contains(studentSearchQuery, ignoreCase = true) ||
                                    it.email.contains(studentSearchQuery, ignoreCase = true)
                                }.take(6)

                                matchedUsers.forEach { u ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedStudentUser = u
                                                selectedStudentEmail = u.email
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedStudentUser?.uid == u.uid,
                                            onClick = {
                                                selectedStudentUser = u
                                                selectedStudentEmail = u.email
                                            }
                                        )
                                        Column {
                                            Text(u.displayName.ifEmpty { "Student" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Text(u.email, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                    }
                                }

                                if (selectedStudentUser != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Selected: ${selectedStudentUser?.displayName} (${selectedStudentUser?.email})",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Schedule & Timing:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = scheduledDate,
                        onValueChange = { scheduledDate = it },
                        label = { Text("Scheduled Date / Time (e.g. 2026-10-15 or Immediate)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = priority == "URGENT",
                            onCheckedChange = { priority = if (it) "URGENT" else "NORMAL" }
                        )
                        Text("Mark as High Priority / Urgent Notice", color = Color.White, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            if (targetAudience == "PERSONAL" && selectedStudentUser == null && selectedStudentEmail.isEmpty()) {
                                Toast.makeText(context, "Please select a student for personal notice", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val notice = Announcement(
                                id = editingAnnouncement?.id ?: "",
                                title = title,
                                content = content,
                                targetAudience = targetAudience,
                                targetStudentEmail = if (targetAudience == "PERSONAL") (selectedStudentUser?.email ?: selectedStudentEmail) else "",
                                targetUserId = if (targetAudience == "PERSONAL") (selectedStudentUser?.uid ?: "") else "",
                                targetUserName = if (targetAudience == "PERSONAL") (selectedStudentUser?.displayName ?: "") else "",
                                priority = priority,
                                scheduledDate = scheduledDate
                            )
                            viewModel.adminSaveAnnouncement(notice) { success ->
                                if (success) {
                                    showCreateDialog = false
                                    Toast.makeText(context, "Suchna published successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Title and message content are required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text(if (editingAnnouncement == null) "Broadcast Suchna" else "Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation
    announcementToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { announcementToDelete = null },
            title = { Text("Delete Suchna?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete announcement '${target.title}'? This will remove it from all student feeds.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeleteAnnouncement(target.id) { success ->
                            if (success) {
                                Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                                announcementToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { announcementToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminEducationAndVideoSection(viewModel: MainViewModel) {
    val courses by viewModel.courses.collectAsState()
    val educationVideos by viewModel.educationVideos.collectAsState()
    val entertainmentVideos by viewModel.entertainmentVideos.collectAsState()
    val allVideos = educationVideos + entertainmentVideos
    val shorts by viewModel.shorts.collectAsState()
    val context = LocalContext.current

    var subTab by remember { mutableIntStateOf(0) } // 0: Videos, 1: Courses, 2: Shorts
    var previewVideo by remember { mutableStateOf<VideoItem?>(null) }
    var previewShort by remember { mutableStateOf<ShortItem?>(null) }

    // Video creation / editor states
    var showVideoDialog by remember { mutableStateOf(false) }
    var editingVideo by remember { mutableStateOf<VideoItem?>(null) }
    var videoTitle by remember { mutableStateOf("") }
    var videoDescription by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var videoThumbnailUrl by remember { mutableStateOf("") }
    var videoCategory by remember { mutableStateOf("General") }
    var videoSection by remember { mutableStateOf("EDUCATION") }
    var isPaidVideo by remember { mutableStateOf(false) }
    var videoPrice by remember { mutableStateOf("0") }

    // Short creation / editor states
    var showShortDialog by remember { mutableStateOf(false) }
    var editingShort by remember { mutableStateOf<ShortItem?>(null) }
    var shortTitle by remember { mutableStateOf("") }
    var shortVideoUrl by remember { mutableStateOf("") }
    var shortThumbnailUrl by remember { mutableStateOf("") }
    var shortCreator by remember { mutableStateOf("Neol Bharat") }
    var shortSection by remember { mutableStateOf("ENTERTAINMENT") }

    // Filter section for admin list: ALL, EDUCATION, ENTERTAINMENT
    var filterVideoSection by remember { mutableStateOf("ALL") }

    // Course creation state
    var showCreateCourse by remember { mutableStateOf(false) }
    var courseTitle by remember { mutableStateOf("") }
    var courseDesc by remember { mutableStateOf("") }
    var courseCategory by remember { mutableStateOf("Science") }
    var coursePrice by remember { mutableStateOf("0") }
    var isFreeCourse by remember { mutableStateOf(true) }

    // Activity Result Launchers for Video and Thumbnail picking
    val videoFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = saveFileLocally(context, uri, "vid_${System.currentTimeMillis()}.mp4")
            if (saved != null) {
                videoUrl = saved
                Toast.makeText(context, "Video loaded from Gallery/Storage!", Toast.LENGTH_SHORT).show()
            } else {
                videoUrl = uri.toString()
                Toast.makeText(context, "Video URI loaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val videoThumbPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = saveImageLocally(context, uri, "vthumb_${System.currentTimeMillis()}.png")
            if (saved != null) {
                videoThumbnailUrl = saved
                Toast.makeText(context, "Thumbnail selected from Gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val shortFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = saveFileLocally(context, uri, "short_${System.currentTimeMillis()}.mp4")
            if (saved != null) {
                shortVideoUrl = saved
                Toast.makeText(context, "Short video loaded from Storage!", Toast.LENGTH_SHORT).show()
            } else {
                shortVideoUrl = uri.toString()
                Toast.makeText(context, "Short URI loaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val shortThumbPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = saveImageLocally(context, uri, "sthumb_${System.currentTimeMillis()}.png")
            if (saved != null) {
                shortThumbnailUrl = saved
                Toast.makeText(context, "Short thumbnail selected from Gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TabRow(
                selectedTabIndex = subTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Videos") })
                Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Courses") })
                Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Shorts & Reels") })
            }
        }

        if (subTab == 0) {
            // VIDEOS MANAGER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Video Platform Catalog", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                        Text("Manage Education & Entertainment Videos", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            editingVideo = null
                            videoTitle = ""
                            videoDescription = ""
                            videoUrl = ""
                            videoThumbnailUrl = ""
                            videoCategory = if (filterVideoSection == "ENTERTAINMENT") "Entertainment" else "Education"
                            videoSection = if (filterVideoSection == "ENTERTAINMENT") "ENTERTAINMENT" else "EDUCATION"
                            isPaidVideo = false
                            videoPrice = "0"
                            showVideoDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Video")
                    }
                }
            }

            // Section Filter for Videos
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All Videos", "EDUCATION" to "Education Videos", "ENTERTAINMENT" to "Entertainment Videos").forEach { (sec, label) ->
                        FilterChip(
                            selected = filterVideoSection == sec,
                            onClick = { filterVideoSection = sec },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Upload / Edit Video Card
            if (showVideoDialog) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (editingVideo == null) "Upload Video (Gallery, File Manager or YouTube)" else "Edit Video Title & Details",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                IconButton(onClick = { showVideoDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = videoTitle,
                                onValueChange = { videoTitle = it },
                                label = { Text("Video Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoDescription,
                                onValueChange = { videoDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Gallery / File Manager upload buttons for Video
                            Text("Video Source (Gallery / File Manager / YouTube Link):", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { videoFilePickerLauncher.launch("video/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick from Gallery", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { videoThumbPickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick Thumbnail", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoUrl,
                                onValueChange = { videoUrl = it },
                                label = { Text("Video Stream URL / YouTube Link (or File Path)") },
                                placeholder = { Text("https://www.youtube.com/watch?v=... or .mp4 URL") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoThumbnailUrl,
                                onValueChange = { videoThumbnailUrl = it },
                                label = { Text("Thumbnail Image URL or Path") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Test & Play Video button
                            if (videoUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        previewVideo = VideoItem(
                                            id = "preview",
                                            title = videoTitle.ifEmpty { "Video Preview" },
                                            videoUrl = videoUrl,
                                            thumbnailUrl = videoThumbnailUrl
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Preview & Play Video Now")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoCategory,
                                onValueChange = { videoCategory = it },
                                label = { Text("Category (e.g., Mathematics, Culture, News, Comedy)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Target Platform Mode / Section:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FilterChip(
                                    selected = videoSection == "EDUCATION",
                                    onClick = { videoSection = "EDUCATION" },
                                    label = { Text("Education Mode") }
                                )
                                FilterChip(
                                    selected = videoSection == "ENTERTAINMENT",
                                    onClick = { videoSection = "ENTERTAINMENT" },
                                    label = { Text("Entertainment Mode") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isPaidVideo, onCheckedChange = { isPaidVideo = it })
                                Text("Premium / Paid Video (Requires Student Payment)", color = Color.White, fontSize = 13.sp)
                            }
                            if (isPaidVideo) {
                                OutlinedTextField(
                                    value = videoPrice,
                                    onValueChange = { videoPrice = it },
                                    label = { Text("Video Unlock Price (₹)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (videoTitle.isNotEmpty() && videoUrl.isNotEmpty()) {
                                        val newOrUpdated = VideoItem(
                                            id = editingVideo?.id ?: "",
                                            title = videoTitle,
                                            description = videoDescription,
                                            videoUrl = videoUrl,
                                            thumbnailUrl = videoThumbnailUrl,
                                            category = videoCategory,
                                            section = videoSection,
                                            isPaid = isPaidVideo,
                                            price = if (isPaidVideo) videoPrice.toDoubleOrNull() ?: 0.0 else 0.0,
                                            views = editingVideo?.views ?: 0,
                                            likes = editingVideo?.likes ?: 0,
                                            isPublished = true
                                        )
                                        viewModel.adminSaveVideo(newOrUpdated) { success ->
                                            if (success) {
                                                showVideoDialog = false
                                                Toast.makeText(context, "Video synced to Firestore!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Title and Video URL are required", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text(if (editingVideo == null) "Upload Video" else "Save Video Changes")
                            }
                        }
                    }
                }
            }

            val displayVideos = when (filterVideoSection) {
                "EDUCATION" -> allVideos.filter { it.section == "EDUCATION" }
                "ENTERTAINMENT" -> allVideos.filter { it.section == "ENTERTAINMENT" }
                else -> allVideos
            }

            if (displayVideos.isEmpty()) {
                item {
                    Text("No videos uploaded in this section yet. Tap '+ Upload Video' above.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                items(displayVideos) { v ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(v.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Mode: ${v.section} • ${v.category}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                    if (v.isPaid) {
                                        Badge(containerColor = Color(0xFFEF4444)) {
                                            Text("₹${v.price.toInt()}", color = Color.White, fontSize = 9.sp)
                                        }
                                    } else {
                                        Badge(containerColor = Color(0xFF10B981)) {
                                            Text("FREE", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                                Text(v.videoUrl, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        viewModel.adminToggleVideoPaid(v) { success ->
                                            Toast.makeText(context, if (!v.isPaid) "Video changed to Paid (₹99)" else "Video marked FREE", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text(
                                        if (!v.isPaid) "Remove Free" else "Make Free",
                                        fontSize = 11.sp,
                                        color = if (!v.isPaid) Color(0xFFF59E0B) else Color(0xFF10B981)
                                    )
                                }
                                IconButton(onClick = { previewVideo = v }) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = "Play Video", tint = Color(0xFF10B981))
                                }

                                // Edit Video Title & details
                                IconButton(onClick = {
                                    editingVideo = v
                                    videoTitle = v.title
                                    videoDescription = v.description
                                    videoUrl = v.videoUrl
                                    videoThumbnailUrl = v.thumbnailUrl
                                    videoCategory = v.category
                                    videoSection = v.section
                                    isPaidVideo = v.isPaid
                                    videoPrice = v.price.toInt().toString()
                                    showVideoDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Video", tint = Color(0xFF38BDF8))
                                }

                                // Delete Video
                                IconButton(onClick = {
                                    viewModel.adminDeleteVideo(v.id) {
                                        Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Video", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        } else if (subTab == 1) {
            // COURSES CATALOG
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Courses Catalog", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    Button(
                        onClick = { showCreateCourse = !showCreateCourse },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (showCreateCourse) "Cancel" else "+ Add Course")
                    }
                }
            }

            if (showCreateCourse) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("New Course Details", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = courseTitle, onValueChange = { courseTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = courseDesc, onValueChange = { courseDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isFreeCourse, onCheckedChange = { isFreeCourse = it })
                                Text("Free Course", color = Color.White)
                            }
                            if (!isFreeCourse) {
                                OutlinedTextField(value = coursePrice, onValueChange = { coursePrice = it }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (courseTitle.isNotEmpty()) {
                                        val newCourse = Course(
                                            title = courseTitle,
                                            description = courseDesc,
                                            category = courseCategory,
                                            isFree = isFreeCourse,
                                            price = if (isFreeCourse) 0.0 else coursePrice.toDoubleOrNull() ?: 0.0,
                                            isPublished = true
                                        )
                                        viewModel.adminSaveCourse(newCourse) { success ->
                                            if (success) {
                                                showCreateCourse = false
                                                courseTitle = ""
                                                courseDesc = ""
                                                Toast.makeText(context, "Course published!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Publish Course to Firestore")
                            }
                        }
                    }
                }
            }

            items(courses) { course ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(course.title, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(if (course.isFree) "FREE" else "Price: ₹${course.price.toInt()}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    viewModel.adminToggleCourseFree(course) { success ->
                                        Toast.makeText(context, if (course.isFree) "Course changed to Paid (₹499)" else "Course marked FREE", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(
                                    if (course.isFree) "Remove Free" else "Make Free",
                                    fontSize = 11.sp,
                                    color = if (course.isFree) Color(0xFFF59E0B) else Color(0xFF10B981)
                                )
                            }
                            IconButton(onClick = { viewModel.adminDeleteCourse(course.id) {} }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        } else if (subTab == 2) {
            // SHORTS & REELS CATALOG
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Shorts & Reels Catalog", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                        Text("Manage vertical education & entertainment shorts", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            editingShort = null
                            shortTitle = ""
                            shortVideoUrl = ""
                            shortThumbnailUrl = ""
                            shortCreator = "Neol Bharat"
                            shortSection = "ENTERTAINMENT"
                            showShortDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Upload Short")
                    }
                }
            }

            if (showShortDialog) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (editingShort == null) "Upload New Short / Reel" else "Edit Short Details",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                IconButton(onClick = { showShortDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = shortTitle,
                                onValueChange = { shortTitle = it },
                                label = { Text("Short Title / Caption") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Short video file picker buttons
                            Text("Short Video Source (Gallery / File Manager / YouTube Link):", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { shortFilePickerLauncher.launch("video/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick Short Video", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { shortThumbPickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick Thumbnail", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = shortVideoUrl,
                                onValueChange = { shortVideoUrl = it },
                                label = { Text("Video Stream URL / YouTube Short Link / File Path") },
                                placeholder = { Text("https://youtube.com/shorts/... or .mp4 URL") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = shortThumbnailUrl,
                                onValueChange = { shortThumbnailUrl = it },
                                label = { Text("Thumbnail Image URL or Path") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Preview Short Button
                            if (shortVideoUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        previewShort = ShortItem(
                                            id = "preview_short",
                                            title = shortTitle.ifEmpty { "Short Preview" },
                                            videoUrl = shortVideoUrl,
                                            thumbnailUrl = shortThumbnailUrl,
                                            creatorName = shortCreator
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Preview & Play Short Now")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = shortCreator,
                                onValueChange = { shortCreator = it },
                                label = { Text("Creator / Faculty Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Section Category:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("ENTERTAINMENT", "EDUCATION").forEach { sec ->
                                    FilterChip(
                                        selected = shortSection == sec,
                                        onClick = { shortSection = sec },
                                        label = { Text(sec) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (shortTitle.isNotBlank() && shortVideoUrl.isNotBlank()) {
                                        val item = ShortItem(
                                            id = editingShort?.id.orEmpty(),
                                            title = shortTitle.trim(),
                                            videoUrl = shortVideoUrl.trim(),
                                            thumbnailUrl = shortThumbnailUrl.trim(),
                                            creatorName = shortCreator.trim().ifEmpty { "Neol Bharat" },
                                            section = shortSection,
                                            likesCount = editingShort?.likesCount ?: 12
                                        )
                                        viewModel.adminSaveShort(item) { success ->
                                            if (success) {
                                                Toast.makeText(context, "Short saved successfully!", Toast.LENGTH_SHORT).show()
                                                showShortDialog = false
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Title and Video URL required", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text(if (editingShort == null) "Publish Short" else "Save Changes")
                            }
                        }
                    }
                }
            }

            if (shorts.isEmpty()) {
                item {
                    Text("No shorts uploaded yet. Click '+ Upload Short' above.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                items(shorts) { s ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("By ${s.creatorName} • ${s.section} • ${s.likesCount} Likes", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                Text(s.videoUrl, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { previewShort = s }) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = "Play Short", tint = Color(0xFF10B981))
                                }
                                IconButton(onClick = {
                                    editingShort = s
                                    shortTitle = s.title
                                    shortVideoUrl = s.videoUrl
                                    shortThumbnailUrl = s.thumbnailUrl
                                    shortCreator = s.creatorName
                                    shortSection = s.section
                                    showShortDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Short", tint = Color(0xFF38BDF8))
                                }
                                IconButton(onClick = {
                                    viewModel.adminDeleteShort(s.id) {
                                        Toast.makeText(context, "Short deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Short", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    previewVideo?.let { v ->
        AppVideoPlayerDialog(
            videoUrl = v.videoUrl,
            videoTitle = v.title,
            onDismiss = { previewVideo = null }
        )
    }

    previewShort?.let { s ->
        AppVideoPlayerDialog(
            videoUrl = s.videoUrl,
            videoTitle = s.title,
            onDismiss = { previewShort = null }
        )
    }
}

@Composable
fun AdminAttendanceSection(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val context = LocalContext.current

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { sdf.format(Date()) }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var subTab by remember { mutableIntStateOf(0) } // 0: Daily Attendance (Tick), 1: All History
    var studentSearch by remember { mutableStateOf("") }
    var historyFilterStatus by remember { mutableStateOf("ALL") }

    val studentUsers = remember(users, studentSearch) {
        users.filter { u ->
            studentSearch.isEmpty() ||
            u.displayName.contains(studentSearch, ignoreCase = true) ||
            u.email.contains(studentSearch, ignoreCase = true)
        }
    }

    // Attendance map for the currently selected date: studentId -> AttendanceRecord
    val dateAttendanceMap = remember(attendance, selectedDate) {
        attendance.filter { it.date == selectedDate }.associateBy { it.userId }
    }

    val presentCountForDate = dateAttendanceMap.values.count { it.status == "PRESENT" }
    val absentCountForDate = dateAttendanceMap.values.count { it.status == "ABSENT" }
    val leaveCountForDate = dateAttendanceMap.values.count { it.status == "LEAVE" }

    val filteredHistory = remember(attendance, historyFilterStatus, studentSearch) {
        attendance.filter { rec ->
            val matchesStatus = when (historyFilterStatus) {
                "PRESENT" -> rec.status == "PRESENT"
                "ABSENT" -> rec.status == "ABSENT"
                "LEAVE" -> rec.status == "LEAVE"
                else -> true
            }
            val matchesSearch = studentSearch.isEmpty() ||
                    rec.studentName.contains(studentSearch, ignoreCase = true) ||
                    rec.date.contains(studentSearch, ignoreCase = true) ||
                    rec.notes.contains(studentSearch, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Student Attendance Management (दैनिक छात्र हाजिरी)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
            Text("Tick Present, Absent, or Leave for students to update their app in real-time.", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }

        // Sub Tabs
        item {
            TabRow(
                selectedTabIndex = subTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Daily Tick Marking") })
                Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("All Students History (${attendance.size})") })
            }
        }

        if (subTab == 0) {
            // DAILY ATTENDANCE MARKING (ONE-TAP TICK)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Selected Date:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Button(
                                onClick = { selectedDate = todayDate },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Set Today", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Date navigation controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    try {
                                        val cal = java.util.Calendar.getInstance()
                                        cal.time = sdf.parse(selectedDate) ?: Date()
                                        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                                        selectedDate = sdf.format(cal.time)
                                    } catch (e: Exception) {
                                        selectedDate = todayDate
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = Color.White)
                            }

                            OutlinedTextField(
                                value = selectedDate,
                                onValueChange = { selectedDate = it },
                                label = { Text("Date (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            IconButton(
                                onClick = {
                                    try {
                                        val cal = java.util.Calendar.getInstance()
                                        cal.time = sdf.parse(selectedDate) ?: Date()
                                        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                        selectedDate = sdf.format(cal.time)
                                    } catch (e: Exception) {
                                        selectedDate = todayDate
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next Day", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metrics for selected date
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Present", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$presentCountForDate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Absent", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$absentCountForDate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                            Surface(
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Leave", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$leaveCountForDate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast Bulk: Mark All Present
                        Button(
                            onClick = {
                                if (users.isEmpty()) {
                                    Toast.makeText(context, "No registered students found", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                var count = 0
                                users.forEach { u ->
                                    val rec = AttendanceRecord(
                                        userId = u.uid,
                                        studentName = u.displayName.ifEmpty { u.email },
                                        date = selectedDate,
                                        status = "PRESENT",
                                        notes = "Marked present by Admin"
                                    )
                                    viewModel.adminSaveAttendance(rec) { count++ }
                                }
                                Toast.makeText(context, "Marked all ${users.size} students Present for $selectedDate!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark All ${users.size} Students Present Today")
                        }
                    }
                }
            }

            // Student Search
            item {
                OutlinedTextField(
                    value = studentSearch,
                    onValueChange = { studentSearch = it },
                    placeholder = { Text("Search student name or email...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            if (studentUsers.isEmpty()) {
                item {
                    Text("No students found matching search.", color = Color.Gray, fontSize = 13.sp)
                }
            }

            // Student list with 1-tap Tick buttons
            items(studentUsers) { student ->
                val currentRecord = dateAttendanceMap[student.uid]
                val currentStatus = currentRecord?.status ?: "NOT_MARKED"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.displayName.ifEmpty { "Student" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = student.email,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            // Current Status Badge
                            Surface(
                                color = when (currentStatus) {
                                    "PRESENT" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    "ABSENT" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    "LEAVE" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    else -> Color(0xFF64748B).copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = when (currentStatus) {
                                        "PRESENT" -> "✓ PRESENT"
                                        "ABSENT" -> "✗ ABSENT"
                                        "LEAVE" -> "⏸ LEAVE"
                                        else -> "NOT MARKED"
                                    },
                                    color = when (currentStatus) {
                                        "PRESENT" -> Color(0xFF10B981)
                                        "ABSENT" -> Color(0xFFEF4444)
                                        "LEAVE" -> Color(0xFFF59E0B)
                                        else -> Color(0xFF94A3B8)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 One-Tap Quick Tick Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tick Present
                            Button(
                                onClick = {
                                    val rec = AttendanceRecord(
                                        id = currentRecord?.id ?: "",
                                        userId = student.uid,
                                        studentName = student.displayName.ifEmpty { student.email },
                                        date = selectedDate,
                                        status = "PRESENT",
                                        notes = currentRecord?.notes ?: ""
                                    )
                                    viewModel.adminSaveAttendance(rec) { success ->
                                        if (success) Toast.makeText(context, "✓ Marked Present: ${student.displayName}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == "PRESENT") Color(0xFF10B981) else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (currentStatus == "PRESENT") Color.White else Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Present", fontSize = 11.sp, color = if (currentStatus == "PRESENT") Color.White else Color(0xFF10B981))
                            }

                            // Tick Absent
                            Button(
                                onClick = {
                                    val rec = AttendanceRecord(
                                        id = currentRecord?.id ?: "",
                                        userId = student.uid,
                                        studentName = student.displayName.ifEmpty { student.email },
                                        date = selectedDate,
                                        status = "ABSENT",
                                        notes = currentRecord?.notes ?: ""
                                    )
                                    viewModel.adminSaveAttendance(rec) { success ->
                                        if (success) Toast.makeText(context, "✗ Marked Absent: ${student.displayName}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == "ABSENT") Color(0xFFEF4444) else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (currentStatus == "ABSENT") Color.White else Color(0xFFEF4444))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Absent", fontSize = 11.sp, color = if (currentStatus == "ABSENT") Color.White else Color(0xFFEF4444))
                            }

                            // Tick Leave
                            Button(
                                onClick = {
                                    val rec = AttendanceRecord(
                                        id = currentRecord?.id ?: "",
                                        userId = student.uid,
                                        studentName = student.displayName.ifEmpty { student.email },
                                        date = selectedDate,
                                        status = "LEAVE",
                                        notes = currentRecord?.notes ?: ""
                                    )
                                    viewModel.adminSaveAttendance(rec) { success ->
                                        if (success) Toast.makeText(context, "⏸ Marked Leave: ${student.displayName}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == "LEAVE") Color(0xFFF59E0B) else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (currentStatus == "LEAVE") Color.White else Color(0xFFF59E0B))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Leave", fontSize = 11.sp, color = if (currentStatus == "LEAVE") Color.White else Color(0xFFF59E0B))
                            }
                        }
                    }
                }
            }
        } else {
            // ALL STUDENTS HISTORY VIEW
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = studentSearch,
                        onValueChange = { studentSearch = it },
                        placeholder = { Text("Search by student name, date, or note...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "PRESENT", "ABSENT", "LEAVE").forEach { st ->
                            FilterChip(
                                selected = historyFilterStatus == st,
                                onClick = { historyFilterStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
            }

            if (filteredHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No attendance records found matching filters.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            items(filteredHistory) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rec.studentName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Date: ${rec.date}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            if (rec.notes.isNotEmpty()) {
                                Text("Note: ${rec.notes}", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = when (rec.status) {
                                    "PRESENT" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    "ABSENT" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = rec.status,
                                    color = when (rec.status) {
                                        "PRESENT" -> Color(0xFF10B981)
                                        "ABSENT" -> Color(0xFFEF4444)
                                        else -> Color(0xFFF59E0B)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.adminDeleteAttendance(rec.id) { success ->
                                        if (success) Toast.makeText(context, "Attendance record deleted", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFeesSection(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val fees by viewModel.allFees.collectAsState()
    val context = LocalContext.current

    var selectedUser by remember { mutableStateOf("") }
    var applyToAllStudents by remember { mutableStateOf(false) }
    var feeMonth by remember { mutableStateOf("October 2026") }
    var feeType by remember { mutableStateOf("MONTHLY") }
    var feeDescription by remember { mutableStateOf("") }
    var feeAmount by remember { mutableStateOf("1500") }
    var dueDate by remember { mutableStateOf("10th of Month") }

    // State for Editing an existing Fee Record
    var editingFee by remember { mutableStateOf<FeeRecord?>(null) }
    var editFeeAmount by remember { mutableStateOf("") }
    var editPaidAmount by remember { mutableStateOf("") }
    var editMonth by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("DUE") }

    // Dialog state for deleting fee one by one
    var feeToDelete by remember { mutableStateOf<FeeRecord?>(null) }

    // Dialog state for deleting total fee of a student
    var studentToDeleteTotal by remember { mutableStateOf<UserProfile?>(null) }

    // Dialog state for deleting all due fees
    var confirmDeleteAllDue by remember { mutableStateOf(false) }

    // Filter by student / fee status
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, DUE, PAID, OTHER
    var feeSearchQuery by remember { mutableStateOf("") }
    var filterByStudentId by remember { mutableStateOf("") }

    val feeTypeOptions = listOf("MONTHLY", "ADMISSION", "EXAM", "LAB", "LIBRARY", "SPORTS", "TRANSPORT", "OTHER")

    val totalAllFees = fees.sumOf { it.totalAmount }
    val totalPaidFees = fees.sumOf { it.paidAmount }
    val totalPendingFees = fees.sumOf { it.pendingAmount }

    val filteredFees = fees.filter { fee ->
        val matchesStudent = filterByStudentId.isEmpty() || fee.userId == filterByStudentId
        val matchesStatus = when (filterStatus) {
            "DUE" -> fee.pendingAmount > 0 || fee.status == "DUE"
            "PAID" -> fee.status == "PAID"
            "OTHER" -> fee.feeType != "MONTHLY"
            else -> true
        }
        val matchesQuery = feeSearchQuery.isEmpty() ||
                fee.studentName.contains(feeSearchQuery, ignoreCase = true) ||
                fee.month.contains(feeSearchQuery, ignoreCase = true) ||
                fee.feeType.contains(feeSearchQuery, ignoreCase = true) ||
                fee.feeDescription.contains(feeSearchQuery, ignoreCase = true)
        matchesStudent && matchesStatus && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Student Fee Management (शुल्क प्रबंधन)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
            Text("Add fees, waive/remove fees, and delete total or individual fees one by one", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }

        // Summary Metric Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Billed", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalAllFees.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${fees.size} Total Bills", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Paid Fees", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalPaidFees.toInt()}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${fees.count { it.status == "PAID" }} Paid", color = Color(0xFF10B981), fontSize = 10.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pending Due", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalPendingFees.toInt()}", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${fees.count { it.pendingAmount > 0 }} Due", color = Color(0xFFEF4444), fontSize = 10.sp)
                    }
                }
            }
        }

        // Quick Bulk Actions (Clear / Delete All Due Fees)
        if (fees.any { it.pendingAmount > 0 }) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0715)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Pending Fee Dues: ₹${totalPendingFees.toInt()}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Delete or clear total pending fees across all student ledgers", color = Color(0xFFFCA5A5), fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { confirmDeleteAllDue = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete All Due Fees", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Add / Demand Fee Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Fee Bill / Demand Notice (फीस बिल जोड़ें)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Fee Category / Type:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(feeTypeOptions) { ft ->
                            FilterChip(
                                selected = feeType == ft,
                                onClick = { feeType = ft },
                                label = { Text(ft) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feeMonth,
                        onValueChange = { feeMonth = it },
                        label = { Text("Fee Month / Period (e.g. October 2026, Term 1)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feeDescription,
                        onValueChange = { feeDescription = it },
                        label = { Text("Fee Purpose / Description (e.g. Tuition Fee, Exam Fee)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feeAmount,
                        onValueChange = { feeAmount = it },
                        label = { Text("Total Fee Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Fast Amount Preset Buttons
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("500", "1000", "1500", "2000").forEach { preset ->
                            SuggestionChip(
                                onClick = { feeAmount = preset },
                                label = { Text("₹$preset", fontSize = 11.sp) }
                            )
                        }
                        SuggestionChip(
                            onClick = { feeAmount = "0" },
                            label = { Text("Free ₹0 (माफ)", fontSize = 11.sp, color = Color(0xFF10B981)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (e.g. 15th October 2026)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Target Student(s):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { applyToAllStudents = !applyToAllStudents }
                    ) {
                        Checkbox(
                            checked = applyToAllStudents,
                            onCheckedChange = { applyToAllStudents = it }
                        )
                        Text("Add Fee to ALL Students (सभी छात्रों के लिए बिल बनाएं)", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    if (!applyToAllStudents) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            users.forEach { u ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedUser = u.uid }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(selected = selectedUser == u.uid, onClick = { selectedUser = u.uid })
                                    Text(
                                        text = "${u.displayName} (${u.email})",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val amt = feeAmount.toDoubleOrNull() ?: 1000.0
                            val isFree = amt <= 0.0
                            val status = if (isFree) "PAID" else "DUE"
                            val paidAmt = if (isFree) 0.0 else 0.0
                            val pendingAmt = if (isFree) 0.0 else amt

                            if (applyToAllStudents) {
                                if (users.isEmpty()) {
                                    Toast.makeText(context, "No registered students found", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                var createdCount = 0
                                users.forEach { u ->
                                    val fee = FeeRecord(
                                        userId = u.uid,
                                        studentName = u.displayName.ifEmpty { "Student" },
                                        month = feeMonth,
                                        feeType = feeType,
                                        feeDescription = feeDescription.ifEmpty { "$feeType Fee ($feeMonth)" },
                                        totalAmount = amt,
                                        paidAmount = paidAmt,
                                        pendingAmount = pendingAmt,
                                        status = status,
                                        dueDate = dueDate
                                    )
                                    viewModel.adminSaveFee(fee) { success ->
                                        if (success) createdCount++
                                    }
                                }
                                Toast.makeText(context, "Adding fees for ${users.size} students...", Toast.LENGTH_SHORT).show()
                                feeDescription = ""
                            } else {
                                if (selectedUser.isNotEmpty()) {
                                    val student = users.find { it.uid == selectedUser }
                                    val fee = FeeRecord(
                                        userId = selectedUser,
                                        studentName = student?.displayName ?: "Student",
                                        month = feeMonth,
                                        feeType = feeType,
                                        feeDescription = feeDescription.ifEmpty { "$feeType Fee ($feeMonth)" },
                                        totalAmount = amt,
                                        paidAmount = paidAmt,
                                        pendingAmount = pendingAmt,
                                        status = status,
                                        dueDate = dueDate
                                    )
                                    viewModel.adminSaveFee(fee) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Fee bill created for student!", Toast.LENGTH_SHORT).show()
                                            feeDescription = ""
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please select a student or choose All Students", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (applyToAllStudents) "Add Fee to All ${users.size} Students" else "Add Fee Bill in Firestore")
                    }
                }
            }
        }

        // Student-Specific Ledger & Total Fee Deletion Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Filter / Manage by Student (छात्र अनुसार कुल फीस प्रबंधन)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = filterByStudentId.isEmpty(),
                                onClick = { filterByStudentId = "" },
                                label = { Text("All Students") }
                            )
                        }
                        items(users) { u ->
                            val studentBills = fees.filter { it.userId == u.uid }
                            val studentDue = studentBills.sumOf { it.pendingAmount }
                            FilterChip(
                                selected = filterByStudentId == u.uid,
                                onClick = { filterByStudentId = if (filterByStudentId == u.uid) "" else u.uid },
                                label = { Text("${u.displayName} (${studentBills.size})") }
                            )
                        }
                    }

                    // If a student is specifically selected, display Total Fee actions for this student
                    if (filterByStudentId.isNotEmpty()) {
                        val activeStudent = users.find { it.uid == filterByStudentId }
                        val studentBills = fees.filter { it.userId == filterByStudentId }
                        val studentTotalFee = studentBills.sumOf { it.totalAmount }
                        val studentTotalPending = studentBills.sumOf { it.pendingAmount }
                        val studentTotalPaid = studentBills.sumOf { it.paidAmount }

                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Student: ${activeStudent?.displayName ?: "Selected Student"}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Total Fee: ₹${studentTotalFee.toInt()} • Pending: ₹${studentTotalPending.toInt()} • Paid: ₹${studentTotalPaid.toInt()} (${studentBills.size} Records)",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Waive Total Fee / Make Free
                                    OutlinedButton(
                                        onClick = {
                                            studentBills.filter { it.pendingAmount > 0 }.forEach { fee ->
                                                viewModel.adminWaiveFee(fee) {}
                                            }
                                            Toast.makeText(context, "All pending fees waived & marked Free for student!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Waive (Free ₹0)", fontSize = 11.sp)
                                    }

                                    // Delete Total Fee (One by One / Batch for student)
                                    Button(
                                        onClick = { studentToDeleteTotal = activeStudent },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete Total Fee", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search & Status Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Search & Status Filter:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = feeSearchQuery,
                    onValueChange = { feeSearchQuery = it },
                    placeholder = { Text("Search by Student, Month, or Fee Type") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All (${fees.size})", "DUE" to "Due (${fees.count { it.pendingAmount > 0 }})", "PAID" to "Paid (${fees.count { it.status == "PAID" }})", "OTHER" to "Other").forEach { (key, label) ->
                        FilterChip(
                            selected = filterStatus == key,
                            onClick = { filterStatus = key },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        // Fee Records List (With One-By-One Delete and Waive Free Buttons)
        if (filteredFees.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No fee records found matching criteria.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredFees) { fee ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fee.studentName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text(
                                    text = "${fee.feeType}: ${fee.month} • Due: ₹${fee.pendingAmount.toInt()} / ₹${fee.totalAmount.toInt()} (Paid: ₹${fee.paidAmount.toInt()})",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp
                                )
                                if (fee.feeDescription.isNotEmpty()) {
                                    Text(fee.feeDescription, color = Color.LightGray, fontSize = 11.sp)
                                }
                                Text("Due Date: ${fee.dueDate.ifEmpty { "N/A" }}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(
                                    containerColor = if (fee.status == "PAID" || fee.pendingAmount <= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                ) {
                                    Text(
                                        if (fee.pendingAmount <= 0) "PAID" else fee.status,
                                        color = Color.White,
                                        modifier = Modifier.padding(3.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                // Edit Fee Button
                                IconButton(onClick = {
                                    editingFee = fee
                                    editFeeAmount = fee.totalAmount.toInt().toString()
                                    editPaidAmount = fee.paidAmount.toInt().toString()
                                    editMonth = fee.month
                                    editDescription = fee.feeDescription
                                    editStatus = fee.status
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Fee", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }
                                // Delete Fee One-by-One Button
                                IconButton(onClick = {
                                    feeToDelete = fee
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Fee One-by-One", tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Waive Fee / Free (₹0) Button (Admin remove fee / make free)
                            if (fee.pendingAmount > 0) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.adminWaiveFee(fee) { success ->
                                            Toast.makeText(context, if (success) "Fee waived & marked FREE (₹0)!" else "Failed waiving fee", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Waive (Free ₹0)", fontSize = 11.sp)
                                }
                            }

                            // Generate Due/Demand Notice Receipt
                            OutlinedButton(
                                onClick = {
                                    viewModel.adminGenerateFeeReceipt(fee, isDueReceipt = true) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Due Receipt", fontSize = 11.sp)
                            }

                            // Generate Paid Fee Receipt
                            Button(
                                onClick = {
                                    viewModel.adminGenerateFeeReceipt(fee, isDueReceipt = false) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paid Receipt", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for One-By-One Fee Deletion
    feeToDelete?.let { targetFee ->
        AlertDialog(
            onDismissRequest = { feeToDelete = null },
            title = { Text("Delete Fee Record (फीस रिकॉर्ड हटाएं)", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    text = "Are you sure you want to delete this fee record of ₹${targetFee.totalAmount.toInt()} for ${targetFee.studentName} (${targetFee.month})?\n\nThis will be permanently removed from Firestore.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = targetFee.id
                        feeToDelete = null
                        viewModel.adminDeleteFee(idToDelete) { success ->
                            Toast.makeText(context, if (success) "Fee record deleted successfully!" else "Failed deleting fee", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete (हाँ, हटाएं)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { feeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for Student Total Fee Deletion
    studentToDeleteTotal?.let { targetStudent ->
        val studentBills = fees.filter { it.userId == targetStudent.uid }
        val studentTotalAmount = studentBills.sumOf { it.totalAmount }
        AlertDialog(
            onDismissRequest = { studentToDeleteTotal = null },
            title = { Text("Delete Total Fee for Student (छात्र की कुल फीस हटाएं)", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = {
                Text(
                    text = "Are you sure you want to delete ALL ${studentBills.size} fee records for ${targetStudent.displayName}?\n\nTotal fee amount to delete: ₹${studentTotalAmount.toInt()}.\nAll these records will be deleted one by one from Firestore.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ids = studentBills.map { it.id }
                        studentToDeleteTotal = null
                        viewModel.adminDeleteMultipleFees(ids) { success, count ->
                            Toast.makeText(context, if (success) "$count fee records deleted for student!" else "Failed deleting fees", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Total Fee (${studentBills.size} Records)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDeleteTotal = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for All Due Fees Deletion
    if (confirmDeleteAllDue) {
        val dueBills = fees.filter { it.pendingAmount > 0 }
        AlertDialog(
            onDismissRequest = { confirmDeleteAllDue = false },
            title = { Text("Delete All Pending Due Fees (कुल बकाया फीस हटाएं)", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = {
                Text(
                    text = "Are you sure you want to delete all ${dueBills.size} pending fee records across all students?\n\nTotal pending dues: ₹${totalPendingFees.toInt()}.\nAll due records will be removed from Firestore one by one.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ids = dueBills.map { it.id }
                        confirmDeleteAllDue = false
                        viewModel.adminDeleteMultipleFees(ids) { success, count ->
                            Toast.makeText(context, if (success) "$count due fee records deleted!" else "Failed deleting dues", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete All ${dueBills.size} Due Bills")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDeleteAllDue = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for Editing Fee Record
    editingFee?.let { targetFee ->
        AlertDialog(
            onDismissRequest = { editingFee = null },
            title = { Text("Edit Student Fee Record", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student: ${targetFee.studentName}", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editMonth,
                        onValueChange = { editMonth = it },
                        label = { Text("Fee Month/Period") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Fee Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editFeeAmount,
                        onValueChange = { editFeeAmount = it },
                        label = { Text("Total Fee (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPaidAmount,
                        onValueChange = { editPaidAmount = it },
                        label = { Text("Paid Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DUE", "PARTIAL", "PAID").forEach { st ->
                            FilterChip(
                                selected = editStatus == st,
                                onClick = { editStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalAmt = editFeeAmount.toDoubleOrNull() ?: targetFee.totalAmount
                        val paidAmt = editPaidAmount.toDoubleOrNull() ?: targetFee.paidAmount
                        val pendingAmt = (totalAmt - paidAmt).coerceAtLeast(0.0)
                        val updated = targetFee.copy(
                            month = editMonth,
                            feeDescription = editDescription,
                            totalAmount = totalAmt,
                            paidAmount = paidAmt,
                            pendingAmount = pendingAmt,
                            status = if (pendingAmt <= 0) "PAID" else editStatus
                        )
                        viewModel.adminSaveFee(updated) { success ->
                            if (success) {
                                Toast.makeText(context, "Fee record updated!", Toast.LENGTH_SHORT).show()
                                editingFee = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingFee = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminPaymentsSection(viewModel: MainViewModel) {
    val payments by viewModel.allPayments.collectAsState()
    val context = LocalContext.current

    var viewingProofPayment by remember { mutableStateOf<PaymentRecord?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // State for Editing a Payment Record
    var paymentToEdit by remember { mutableStateOf<PaymentRecord?>(null) }
    var editAmount by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("VERIFIED") }
    var editPurpose by remember { mutableStateOf("") }
    var editTargetName by remember { mutableStateOf("") }
    var editUtr by remember { mutableStateOf("") }
    var editStudentName by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    // State for Deleting a Payment Record
    var paymentToDelete by remember { mutableStateOf<PaymentRecord?>(null) }

    val filteredPayments = remember(payments, selectedFilter, searchQuery) {
        payments.filter { p ->
            val matchesFilter = when (selectedFilter) {
                "PENDING" -> p.status == "PENDING_VERIFICATION"
                "VERIFIED" -> p.status == "VERIFIED"
                "REJECTED" -> p.status == "REJECTED"
                else -> true
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    p.userName.contains(searchQuery, ignoreCase = true) ||
                    p.userEmail.contains(searchQuery, ignoreCase = true) ||
                    p.transactionReference.contains(searchQuery, ignoreCase = true) ||
                    p.purpose.contains(searchQuery, ignoreCase = true) ||
                    p.targetName.contains(searchQuery, ignoreCase = true) ||
                    p.notes.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val totalVerifiedAmount = payments.filter { it.status == "VERIFIED" }.sumOf { it.amount }
    val totalPendingAmount = payments.filter { it.status == "PENDING_VERIFICATION" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Payment Approvals & Fee History", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
            Text(
                "Verify student fee/payment proofs, approve/reject transactions, edit ledger details, and delete entries.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        // Summary Metric Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Approved Revenue", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalVerifiedAmount.toInt()}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${payments.count { it.status == "VERIFIED" }} Verified", color = Color(0xFF10B981), fontSize = 10.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pending Approvals", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalPendingAmount.toInt()}", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${payments.count { it.status == "PENDING_VERIFICATION" }} Pending", color = Color(0xFFF59E0B), fontSize = 10.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Logged", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${payments.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${payments.count { it.status == "REJECTED" }} Rejected", color = Color(0xFFEF4444), fontSize = 10.sp)
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Student, UTR, Purpose, or Remarks") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "ALL" to "All (${payments.size})",
                        "PENDING" to "Pending (${payments.count { it.status == "PENDING_VERIFICATION" }})",
                        "VERIFIED" to "Approved (${payments.count { it.status == "VERIFIED" }})",
                        "REJECTED" to "Rejected (${payments.count { it.status == "REJECTED" }})"
                    ).forEach { (f, label) ->
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        if (filteredPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No payment records found matching criteria.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }

        items(filteredPayments) { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.userName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            if (p.userEmail.isNotEmpty()) {
                                Text(p.userEmail, color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(
                                containerColor = when (p.status) {
                                    "VERIFIED" -> Color(0xFF10B981)
                                    "REJECTED" -> Color(0xFFEF4444)
                                    else -> Color(0xFFF59E0B)
                                }
                            ) {
                                Text(
                                    text = when (p.status) {
                                        "VERIFIED" -> "APPROVED"
                                        "REJECTED" -> "REJECTED"
                                        else -> "PENDING"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Edit Fee/Payment Button
                            IconButton(
                                onClick = {
                                    paymentToEdit = p
                                    editAmount = p.amount.toInt().toString()
                                    editStatus = p.status
                                    editPurpose = p.purpose
                                    editTargetName = p.targetName
                                    editUtr = p.transactionReference
                                    editStudentName = p.userName
                                    editNotes = p.notes
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Fee/Payment", tint = Color(0xFF38BDF8), modifier = Modifier.size(19.dp))
                            }

                            // Delete Fee/Payment Button
                            IconButton(
                                onClick = { paymentToDelete = p }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Fee/Payment", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Amount: ₹${p.amount.toInt()} • Purpose: ${p.purpose}", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (p.targetName.isNotEmpty()) {
                        Text("Target: ${p.targetName}", color = Color.LightGray, fontSize = 12.sp)
                    }
                    Text("Transaction Reference / UTR: ${p.transactionReference}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Medium, fontSize = 12.sp)

                    if (p.notes.isNotEmpty()) {
                        Text("Remarks: ${p.notes}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    }

                    // Payment Attachment Proof (Image or PDF)
                    if (p.attachmentUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            onClick = { viewingProofPayment = p },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (p.attachmentType.equals("PDF", ignoreCase = true)) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (p.attachmentType.equals("PDF", ignoreCase = true)) "PDF Payment Receipt Attached" else "Payment Screenshot Proof Attached",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text("Tap to preview, download, or share attachment", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Verification & Status Action Buttons (Approve / Reject / Re-verify)
                    Spacer(modifier = Modifier.height(10.dp))
                    when (p.status) {
                        "PENDING_VERIFICATION" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.adminVerifyPayment(p.id, true) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify & Approve")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.adminVerifyPayment(p.id, false) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reject")
                                }
                            }
                        }
                        "VERIFIED" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.adminVerifyPayment(p.id, false) { success, msg ->
                                            Toast.makeText(context, "Payment marked as Rejected", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change to Rejected", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val updated = p.copy(status = "PENDING_VERIFICATION")
                                        viewModel.adminUpdatePayment(updated) { success ->
                                            if (success) Toast.makeText(context, "Reverted to Pending Verification", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Revert to Pending", fontSize = 11.sp)
                                }
                            }
                        }
                        "REJECTED" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.adminVerifyPayment(p.id, true) { success, msg ->
                                            Toast.makeText(context, "Payment approved and verified!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve & Verify", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val updated = p.copy(status = "PENDING_VERIFICATION")
                                        viewModel.adminUpdatePayment(updated) { success ->
                                            if (success) Toast.makeText(context, "Reverted to Pending Verification", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Revert to Pending", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Editing Payment / Fee History Record
    paymentToEdit?.let { targetPayment ->
        AlertDialog(
            onDismissRequest = { paymentToEdit = null },
            title = { Text("Edit Payment / Fee Record", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Student: ${targetPayment.userName}", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUtr,
                        onValueChange = { editUtr = it },
                        label = { Text("UTR / Transaction Reference") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPurpose,
                        onValueChange = { editPurpose = it },
                        label = { Text("Purpose (e.g. MONTHLY_FEE, COURSE_PURCHASE)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTargetName,
                        onValueChange = { editTargetName = it },
                        label = { Text("Target Item / Month Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Admin Remarks / Student Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Verification Status:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "VERIFIED" to "Approved",
                            "PENDING_VERIFICATION" to "Pending",
                            "REJECTED" to "Rejected"
                        ).forEach { (st, label) ->
                            FilterChip(
                                selected = editStatus == st,
                                onClick = { editStatus = st },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = editAmount.toDoubleOrNull() ?: targetPayment.amount
                        val updated = targetPayment.copy(
                            amount = amt,
                            status = editStatus,
                            purpose = editPurpose.ifEmpty { targetPayment.purpose },
                            targetName = editTargetName,
                            transactionReference = editUtr.ifEmpty { targetPayment.transactionReference },
                            notes = editNotes
                        )
                        viewModel.adminUpdatePayment(updated) { success ->
                            if (success) {
                                Toast.makeText(context, "Payment record updated successfully!", Toast.LENGTH_SHORT).show()
                                paymentToEdit = null
                            } else {
                                Toast.makeText(context, "Failed updating payment record", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { paymentToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for Deleting Payment Record
    paymentToDelete?.let { targetPayment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Delete Payment Record (भुगतान रिकॉर्ड हटाएं)", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = {
                Text(
                    text = "Are you sure you want to delete this payment record of ₹${targetPayment.amount.toInt()} by ${targetPayment.userName}?\n\nPurpose: ${targetPayment.purpose}\nUTR Ref: ${targetPayment.transactionReference}\n\nThis will permanently remove this entry from payment history.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeletePayment(targetPayment.id) { success ->
                            paymentToDelete = null
                            if (success) {
                                Toast.makeText(context, "Payment record deleted successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed deleting payment record", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Record")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    viewingProofPayment?.let { proof ->
        PaymentProofViewerDialog(
            attachmentUrl = proof.attachmentUrl,
            attachmentType = proof.attachmentType,
            studentName = proof.userName,
            utrRef = proof.transactionReference,
            onDismiss = { viewingProofPayment = null }
        )
    }
}

@Composable
fun AdminSettingsAndBrandingSection(viewModel: MainViewModel) {
    val settings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current

    var customAppIcon by remember { mutableStateOf(settings.customAppIconUrl) }
    var instituteName by remember { mutableStateOf(settings.instituteName) }
    var upiId by remember { mutableStateOf(settings.upiId) }
    var payeeName by remember { mutableStateOf(settings.upiPayeeName) }
    var qrImageUrl by remember { mutableStateOf(settings.qrImageUrl) }
    var instructions by remember { mutableStateOf(settings.paymentInstructions) }
    var banner by remember { mutableStateOf(settings.announcementBanner) }
    var supportPhone by remember { mutableStateOf(settings.supportPhone) }
    var supportEmail by remember { mutableStateOf(settings.supportEmail) }
    var latestAppVersion by remember { mutableStateOf(settings.latestAppVersion) }
    var appVersionFeatures by remember { mutableStateOf(settings.appVersionFeatures) }
    var appUpdateUrl by remember { mutableStateOf(settings.appUpdateUrl) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Android Photo Picker for App Logo / Photo
    val appLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageLocally(context, uri, "app_logo_${System.currentTimeMillis()}.png")
            if (savedPath != null) {
                customAppIcon = savedPath
                val newSettings = settings.copy(
                    customAppIconUrl = savedPath,
                    instituteName = instituteName,
                    upiId = upiId,
                    upiPayeeName = payeeName,
                    qrImageUrl = qrImageUrl,
                    paymentInstructions = instructions,
                    announcementBanner = banner,
                    supportPhone = supportPhone,
                    supportEmail = supportEmail,
                    latestAppVersion = latestAppVersion,
                    appVersionFeatures = appVersionFeatures,
                    appUpdateUrl = appUpdateUrl
                )
                viewModel.adminSaveAppSettings(newSettings) { success ->
                    if (success) {
                        Toast.makeText(context, "App logo photo updated & changed in Student & User apps automatically!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Android Photo Picker for Gallery QR Image selection (zero-permission)
    val qrImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageLocally(context, uri, "payment_qr_${System.currentTimeMillis()}.png")
            if (savedPath != null) {
                qrImageUrl = savedPath
                Toast.makeText(context, "QR image selected from gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(settings) {
        customAppIcon = settings.customAppIconUrl
        instituteName = settings.instituteName
        upiId = settings.upiId
        payeeName = settings.upiPayeeName
        qrImageUrl = settings.qrImageUrl
        instructions = settings.paymentInstructions
        banner = settings.announcementBanner
        supportPhone = settings.supportPhone
        supportEmail = settings.supportEmail
        latestAppVersion = settings.latestAppVersion
        appVersionFeatures = settings.appVersionFeatures
        appUpdateUrl = settings.appUpdateUrl
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("App Branding, QR & Global Settings", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)

        // 1. App Photo / Logo Upload, Change, Delete Card (Automatic sync to student and user apps!)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("App Photo / Logo Branding (ऐप फोटो व लोगो)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Upload, change, or delete custom app logo photo. Changes apply automatically to Student, User, and Login screens!", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        modifier = Modifier.size(76.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (customAppIcon.isNotEmpty()) {
                                AsyncImage(
                                    model = customAppIcon,
                                    contentDescription = "Active App Logo",
                                    modifier = Modifier.fillMaxSize().padding(4.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Default Logo",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (customAppIcon.isNotEmpty()) "Custom Logo Photo Active" else "Default Neol Bharat Logo Active",
                            color = if (customAppIcon.isNotEmpty()) Color(0xFF10B981) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (customAppIcon.isNotEmpty()) "Synced with all student & user apps" else "No custom photo uploaded",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        if (customAppIcon.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = {
                                    customAppIcon = ""
                                    val newSettings = settings.copy(customAppIconUrl = "")
                                    viewModel.adminSaveAppSettings(newSettings) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Logo photo deleted. Reset to default logo across all apps!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Photo (Reset Default)", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { appLogoPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (customAppIcon.isEmpty()) "Upload App Logo Photo" else "Change App Logo Photo")
                    }
                }
            }
        }

        // 2. UPI QR Code Gallery Upload Card (Direct URL input option removed per request)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Upload Payment QR Photo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select and upload UPI QR code photo from gallery for student payment checkout.", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                if (qrImageUrl.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            AsyncImage(
                                model = qrImageUrl,
                                contentDescription = "Active Payment QR",
                                modifier = Modifier.fillMaxSize().padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Current Payment QR", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextButton(onClick = { qrImageUrl = "" }) {
                                Text("Remove QR", color = Color(0xFFEF4444))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { qrImagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (qrImageUrl.isEmpty()) "Upload QR from Gallery" else "Replace QR Photo")
                    }
                }
            }
        }

        // 2. UPI Details & Global Sync Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Institute Name & Global App Configuration", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text("Update institute branding, payments, support info, and push new version updates to all users.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = instituteName, onValueChange = { instituteName = it }, label = { Text("Institute Name (Shown on Student Home)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("UPI ID (VPA)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = payeeName, onValueChange = { payeeName = it }, label = { Text("Payee Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Payment Instructions") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = banner, onValueChange = { banner = it }, label = { Text("Announcement Banner") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = supportPhone, onValueChange = { supportPhone = it }, label = { Text("Support Phone Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = supportEmail, onValueChange = { supportEmail = it }, label = { Text("Support Email Address") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))

                // App Version & Feature Broadcast
                Text("Admin Push New App Version & Features", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 14.sp)
                Text("Broadcast latest features to Student Mode and Entertainment Mode users.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = latestAppVersion, onValueChange = { latestAppVersion = it }, label = { Text("App Version (e.g. 2.1.0)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = appVersionFeatures,
                    onValueChange = { appVersionFeatures = it },
                    label = { Text("New Version Features (Broadcast to All Students & Users)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = appUpdateUrl, onValueChange = { appUpdateUrl = it }, label = { Text("App Update Link / GitHub URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val newSettings = settings.copy(
                                customAppIconUrl = customAppIcon,
                                instituteName = instituteName,
                                upiId = upiId,
                                upiPayeeName = payeeName,
                                qrImageUrl = qrImageUrl,
                                paymentInstructions = instructions,
                                announcementBanner = banner,
                                supportPhone = supportPhone,
                                supportEmail = supportEmail,
                                latestAppVersion = latestAppVersion,
                                appVersionFeatures = appVersionFeatures,
                                appUpdateUrl = appUpdateUrl
                            )
                            viewModel.adminSaveAppSettings(newSettings) { success ->
                                if (success) Toast.makeText(context, "All app settings & new version updated globally!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Settings")
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset / Delete")
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset / Delete All Custom App Settings?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear all custom QR photos, reset branding icons, and revert all payment & support information back to default factory settings across the app.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminResetAppSettings { success ->
                            showResetDialog = false
                            if (success) {
                                Toast.makeText(context, "All custom app settings reset & deleted!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Local file save helper for gallery uploads
private fun saveImageLocally(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val file = File(context.filesDir, fileName)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
        fos.flush()
        fos.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Local file save helper for gallery & file manager videos/documents
private fun saveFileLocally(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, fileName)
        val fos = FileOutputStream(file)
        inputStream.copyTo(fos)
        fos.flush()
        fos.close()
        inputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
