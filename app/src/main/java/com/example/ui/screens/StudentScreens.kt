package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.components.AppVideoPlayerDialog
import com.example.util.ReceiptPdfGenerator
import com.example.viewmodel.MainViewModel

@Composable
fun StudentModeScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    studentProfile: StudentProfile?,
    isAdmin: Boolean = false,
    onOpenAdmin: () -> Unit = {},
    onOpenPaymentDialog: (Double, String, String, String) -> Unit,
    onOpenReceipt: (Receipt) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Home, 1: Courses & Video, 2: Attendance, 3: Receipts & Fees, 4: Profile

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("tab_student_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.School, contentDescription = "Courses") },
                    label = { Text("Courses") },
                    modifier = Modifier.testTag("tab_student_courses")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = "Attendance") },
                    label = { Text("Attendance") },
                    modifier = Modifier.testTag("tab_student_attendance")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Receipts") },
                    label = { Text("Receipts") },
                    modifier = Modifier.testTag("tab_student_receipts")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("tab_student_profile")
                )
                // Admin Panel Tab without needing desktop mode
                if (isAdmin) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onOpenAdmin,
                        icon = {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = Color(0xFFEF4444)
                            )
                        },
                        label = {
                            Text(
                                "Admin",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_student_admin")
                    )
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
            when (selectedTab) {
                0 -> StudentHomeScreen(
                    viewModel = viewModel,
                    userProfile = userProfile,
                    studentProfile = studentProfile,
                    isAdmin = isAdmin,
                    onOpenAdmin = onOpenAdmin,
                    onNavigateToCourses = { selectedTab = 1 },
                    onNavigateToVideos = { selectedTab = 1 },
                    onNavigateToAttendance = { selectedTab = 2 },
                    onNavigateToReceipts = { selectedTab = 3 },
                    onViewReceipt = onOpenReceipt,
                    onPayFee = { fee ->
                        onOpenPaymentDialog(fee.pendingAmount, "MONTHLY_FEE", fee.id, "Fee ${fee.month}")
                    }
                )
                1 -> StudentCoursesScreen(
                    viewModel = viewModel,
                    studentProfile = studentProfile,
                    onBuyCourse = { course ->
                        onOpenPaymentDialog(course.price, "COURSE_PURCHASE", course.id, course.title)
                    }
                )
                2 -> StudentAttendanceTabScreen(
                    viewModel = viewModel
                )
                3 -> StudentReceiptsAndFeesTabScreen(
                    viewModel = viewModel,
                    userProfile = userProfile,
                    onViewReceipt = onOpenReceipt,
                    onPayFee = { fee ->
                        onOpenPaymentDialog(fee.pendingAmount, "MONTHLY_FEE", fee.id, "Fee ${fee.month}")
                    },
                    onOpenPaymentDialog = onOpenPaymentDialog
                )
                4 -> StudentProfileScreen(
                    viewModel = viewModel,
                    userProfile = userProfile,
                    studentProfile = studentProfile,
                    isAdmin = isAdmin,
                    onOpenAdmin = onOpenAdmin,
                    onViewReceipt = onOpenReceipt
                )
            }
        }
    }
}

@Composable
fun StudentHomeScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    studentProfile: StudentProfile?,
    isAdmin: Boolean = false,
    onOpenAdmin: () -> Unit = {},
    onNavigateToCourses: () -> Unit,
    onNavigateToVideos: () -> Unit,
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToReceipts: () -> Unit = {},
    onViewReceipt: (Receipt) -> Unit = {},
    onPayFee: (FeeRecord) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val videos by viewModel.educationVideos.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val fees by viewModel.studentFees.collectAsState()
    val attendance by viewModel.studentAttendance.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val receipts by viewModel.studentReceipts.collectAsState()
    val payments by viewModel.studentPayments.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Institute Name Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appSettings.customAppIconUrl.isNotEmpty()) {
                        AsyncImage(
                            model = appSettings.customAppIconUrl,
                            contentDescription = "Institute Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appSettings.instituteName.ifEmpty { "NEOL BHARAT INSTITUTE" },
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Admin Portal & Educational Institute • v${appSettings.latestAppVersion}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }

                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onOpenAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Top Student Profile Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userProfile.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Namaste, ${userProfile.displayName}!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Student ID: ${studentProfile?.studentId ?: "NB-STUDENT"} • ${studentProfile?.gradeClass ?: "Grade 10"}",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // App Version Update Notice Card (Features & Direct Access)
        if (appSettings.appVersionFeatures.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF134E4A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Version ${appSettings.latestAppVersion} Available!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                            Badge(containerColor = Color(0xFF2DD4BF)) {
                                Text("New Features", color = Color(0xFF134E4A), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = appSettings.appVersionFeatures,
                            color = Color(0xFFCCFBF1),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Announcement / Notice Banner
        if (announcements.isNotEmpty() || appSettings.announcementBanner.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = announcements.firstOrNull()?.content ?: appSettings.announcementBanner,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Education Quick Stats: Attendance & Fee Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Attendance Card
                val presentCount = attendance.count { it.status == "PRESENT" }
                val totalDays = attendance.size
                val attendancePercent = if (totalDays > 0) (presentCount * 100) / totalDays else 100
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Attendance", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$attendancePercent%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "$presentCount / $totalDays Days",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onNavigateToAttendance,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Check Log (हाजिरी)", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Fee Status Card (Total & Pending Summary)
                val totalFee = fees.sumOf { it.totalAmount }
                val pendingFee = fees.sumOf { it.pendingAmount }
                val paidFee = fees.sumOf { it.paidAmount }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fee Due", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹ ${pendingFee.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pendingFee > 0) Color(0xFFF43F5E) else Color(0xFF10B981)
                        )
                        Text(
                            text = "Total: ₹${totalFee.toInt()} | Paid: ₹${paidFee.toInt()}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        if (pendingFee > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val consolidatedFee = FeeRecord(
                                        id = "TOTAL_DUE_${System.currentTimeMillis()}",
                                        userId = userProfile.uid,
                                        studentName = userProfile.displayName,
                                        month = "Consolidated All Months",
                                        feeType = "TOTAL_DUE",
                                        feeDescription = "Total Outstanding Dues (${fees.filter { it.pendingAmount > 0 }.size} Bills)",
                                        totalAmount = pendingFee,
                                        paidAmount = 0.0,
                                        pendingAmount = pendingFee,
                                        status = "DUE"
                                    )
                                    onPayFee(consolidatedFee)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text("Pay Total Due", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Student Month-wise & Category-wise Fees Ledger on Home Screen
        if (fees.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Month-Wise Fee Status / शुल्क विवरण",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Pending: ₹${fees.sumOf { it.pendingAmount }.toInt()} • Total: ₹${fees.sumOf { it.totalAmount }.toInt()}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                            }
                            if (fees.any { it.pendingAmount > 0 }) {
                                Button(
                                    onClick = {
                                        val totalPending = fees.sumOf { it.pendingAmount }
                                        val consolidatedFee = FeeRecord(
                                            id = "TOTAL_DUE_${System.currentTimeMillis()}",
                                            userId = userProfile.uid,
                                            studentName = userProfile.displayName,
                                            month = "All Pending Months",
                                            feeType = "CONSOLIDATED",
                                            feeDescription = "Pay All Outstanding Fees",
                                            totalAmount = totalPending,
                                            paidAmount = 0.0,
                                            pendingAmount = totalPending,
                                            status = "DUE"
                                        )
                                        onPayFee(consolidatedFee)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Pay Total Fee", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        fees.forEach { feeItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (feeItem.status == "PAID") Color(0xFF1E293B) else Color(0xFF2D1B28)
                                ),
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
                                        Text(
                                            text = "${feeItem.month} (${feeItem.feeType})",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = feeItem.feeDescription.ifEmpty { "Tuition / Educational Fee" },
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Due: ₹${feeItem.pendingAmount.toInt()} / ₹${feeItem.totalAmount.toInt()} (Paid: ₹${feeItem.paidAmount.toInt()}) • Due by ${feeItem.dueDate.ifEmpty { "10th" }}",
                                            color = if (feeItem.pendingAmount > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (feeItem.pendingAmount > 0) {
                                        Button(
                                            onClick = { onPayFee(feeItem) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Pay Fee", fontSize = 12.sp)
                                        }
                                    } else {
                                        Badge(containerColor = Color(0xFF16A34A)) {
                                            Text("PAID", color = Color.White, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Verified Payment Receipts & Risipt Card on Home Screen
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Payment Receipts & Risipt (रसीदें)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Verified fee & course transaction receipts (A4 PDF)",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp
                            )
                        }
                        TextButton(onClick = onNavigateToReceipts) {
                            Text("All (${receipts.size})", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (receipts.isEmpty()) {
                        Text(
                            text = "No verified receipts yet. Generated automatically once admin verifies payment.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        receipts.take(3).forEach { receipt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
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
                                        Text(receipt.receiptNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${receipt.purpose} • ₹${receipt.amount.toInt()}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                        Text("Ref: ${receipt.transactionReference.ifEmpty { "VERIFIED" }}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { onViewReceipt(receipt) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("A4 PDF", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Free Courses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Free Courses / मुफ़्त कोर्स",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                TextButton(onClick = onNavigateToCourses) {
                    Text("View All", color = Color(0xFF38BDF8))
                }
            }
            val freeCourses = courses.filter { it.isFree }
            if (freeCourses.isEmpty()) {
                Text(
                    text = "No free courses currently uploaded.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(freeCourses) { course ->
                        CourseHorizontalCard(course = course)
                    }
                }
            }
        }

        // Paid Courses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Paid Courses",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                TextButton(onClick = onNavigateToCourses) {
                    Text("Explore", color = Color(0xFF38BDF8))
                }
            }
            val paidCourses = courses.filter { !it.isFree }
            if (paidCourses.isEmpty()) {
                Text(
                    text = "No paid courses available yet.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(paidCourses) { course ->
                        CourseHorizontalCard(course = course)
                    }
                }
            }
        }

        // Video Lectures Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Video Lectures / वीडियो कक्षाएं",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                TextButton(onClick = onNavigateToVideos) {
                    Text("More Videos", color = Color(0xFF38BDF8))
                }
            }
            if (videos.isEmpty()) {
                Text(
                    text = "No video lectures available.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(videos) { video ->
                        VideoLectureCard(video = video)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun CourseHorizontalCard(course: Course) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                if (course.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = course.thumbnailUrl,
                        contentDescription = course.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                // Free/Paid Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (course.isFree) Color(0xFF16A34A) else Color(0xFF0284C7))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (course.isFree) "FREE" else "₹${course.price.toInt()}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                Text(
                    text = course.category,
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${course.lessonsCount} Lessons • ${course.instructorName}",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VideoLectureCard(video: VideoItem, onPlay: ((VideoItem) -> Unit)? = null) {
    val context = LocalContext.current
    var showPlayerDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp),
        onClick = {
            if (onPlay != null) {
                onPlay(video)
            } else if (video.videoUrl.isNotEmpty()) {
                showPlayerDialog = true
            }
        },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (video.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(36.dp)
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${video.views} views",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }
    }

    if (showPlayerDialog) {
        AppVideoPlayerDialog(
            videoUrl = video.videoUrl,
            videoTitle = video.title,
            onDismiss = { showPlayerDialog = false }
        )
    }
}

@Composable
fun StudentVideosScreen(
    viewModel: MainViewModel,
    studentProfile: StudentProfile?,
    onBuyVideo: (VideoItem) -> Unit
) {
    val videos by viewModel.educationVideos.collectAsState()
    val context = LocalContext.current
    var activePlayingVideo by remember { mutableStateOf<VideoItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Educational Video Lectures",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No videos uploaded by admin yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(videos) { video ->
                    val isUnlocked = !video.isPaid ||
                            (studentProfile?.unlockedVideos?.contains(video.id) == true) ||
                            (video.requiredCourseId.isNotEmpty() && studentProfile?.enrolledCourses?.contains(video.requiredCourseId) == true)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (isUnlocked) {
                                if (video.videoUrl.isNotEmpty()) {
                                    activePlayingVideo = video
                                }
                            } else {
                                onBuyVideo(video)
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (video.thumbnailUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUnlocked) Icons.Default.PlayArrow else Icons.Default.Lock,
                                        contentDescription = if (isUnlocked) "Play" else "Locked",
                                        tint = if (isUnlocked) Color.White else Color(0xFFFBBF24),
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                // Paid / Free Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isUnlocked) Color(0xFF16A34A) else Color(0xFFDC2626))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isUnlocked) (if (video.isPaid) "UNLOCKED" else "FREE") else "₹${video.price.toInt()} PAY TO UNLOCK",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = video.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = video.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Category: ${video.category}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp
                                    )
                                    if (!isUnlocked) {
                                        Button(
                                            onClick = { onBuyVideo(video) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Unlock ₹${video.price.toInt()}", fontSize = 11.sp)
                                        }
                                    } else {
                                        Text(
                                            text = "${video.views} views • ${video.likes} likes",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
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

    activePlayingVideo?.let { vid ->
        AppVideoPlayerDialog(
            videoUrl = vid.videoUrl,
            videoTitle = vid.title,
            onDismiss = { activePlayingVideo = null }
        )
    }
}

@Composable
fun StudentCoursesScreen(
    viewModel: MainViewModel,
    studentProfile: StudentProfile?,
    onBuyCourse: (Course) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Enrolled/Free, 2: Premium

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Courses & Curriculum",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All Courses") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Free / Enrolled") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Premium") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        val enrolledList = studentProfile?.enrolledCourses ?: emptyList()
        val filtered = when (selectedTab) {
            1 -> courses.filter { it.isFree || enrolledList.contains(it.id) }
            2 -> courses.filter { !it.isFree }
            else -> courses
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No courses found in this category.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filtered) { course ->
                    val isUnlocked = course.isFree || enrolledList.contains(course.id)
                    CourseDetailedCard(
                        course = course,
                        isUnlocked = isUnlocked,
                        onBuy = { onBuyCourse(course) }
                    )
                }
            }
        }
    }
}

@Composable
fun CourseDetailedCard(
    course: Course,
    isUnlocked: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                if (course.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = course.thumbnailUrl,
                        contentDescription = course.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUnlocked) Color(0xFF16A34A) else Color(0xFFEA580C))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "UNLOCKED" else "LOCKED (₹${course.price.toInt()})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${course.lessonsCount} Lessons • ${course.instructorName}",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp
                    )
                    if (!isUnlocked) {
                        Button(
                            onClick = onBuy,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unlock Course")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { /* Open lessons */ },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start Learning", color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    studentProfile: StudentProfile?,
    isAdmin: Boolean = false,
    onOpenAdmin: () -> Unit = {},
    onViewReceipt: (Receipt) -> Unit
) {
    val context = LocalContext.current
    val receipts by viewModel.studentReceipts.collectAsState()
    val attendance by viewModel.studentAttendance.collectAsState()
    val fees by viewModel.studentFees.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (userProfile.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 32.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile.displayName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = userProfile.email,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Badge(containerColor = Color(0xFF0284C7)) {
                        Text(
                            text = "Student ID: ${studentProfile?.studentId ?: "NB-STUDENT"}",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Attendance Record Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "My Attendance History",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (attendance.isEmpty()) {
                        Text("No attendance recorded yet.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        attendance.take(5).forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(rec.date, color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                val statusColor = when (rec.status) {
                                    "PRESENT" -> Color(0xFF10B981)
                                    "ABSENT" -> Color(0xFFEF4444)
                                    else -> Color(0xFFF59E0B)
                                }
                                Text(rec.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Fee History
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Fees Ledger",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (fees.isEmpty()) {
                        Text("No fee records registered.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        fees.forEach { fee ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(fee.month, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Total: ₹${fee.totalAmount.toInt()} | Paid: ₹${fee.paidAmount.toInt()}", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(
                                    text = fee.status,
                                    color = if (fee.status == "PAID") Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Receipts Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verified Payment Receipts / रसीदें",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (receipts.isEmpty()) {
                        Text("No verified receipts yet. Generated automatically after admin payment confirmation.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        receipts.forEach { receipt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(receipt.receiptNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${receipt.purpose}: ₹${receipt.amount.toInt()}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { onViewReceipt(receipt) }) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Download Receipt PDF",
                                            tint = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Switch Mode or Logout Options
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferences & Account",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isAdmin) {
                        Button(
                            onClick = onOpenAdmin,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Admin Panel (एडमिन पैनल)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedButton(
                        onClick = { viewModel.selectExperienceMode("ENTERTAINMENT") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch to Entertainment Mode")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.signOut(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StudentAttendanceTabScreen(viewModel: MainViewModel) {
    val attendance by viewModel.studentAttendance.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PRESENT, ABSENT, LEAVE
    var searchQuery by remember { mutableStateOf("") }

    val totalDays = attendance.size
    val presentCount = attendance.count { it.status == "PRESENT" }
    val absentCount = attendance.count { it.status == "ABSENT" }
    val leaveCount = attendance.count { it.status == "LEAVE" }
    val attendancePct = if (totalDays > 0) (presentCount * 100) / totalDays else 100

    val filteredList = remember(attendance, selectedFilter, searchQuery) {
        attendance.filter { rec ->
            val matchesFilter = when (selectedFilter) {
                "PRESENT" -> rec.status == "PRESENT"
                "ABSENT" -> rec.status == "ABSENT"
                "LEAVE" -> rec.status == "LEAVE"
                else -> true
            }
            val matchesSearch = searchQuery.isEmpty() ||
                rec.date.contains(searchQuery, ignoreCase = true) ||
                rec.notes.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }.sortedByDescending { it.date }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Day-by-Day Attendance (दैनिक छात्र हाजिरी)",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "Real-time records synced directly from institute administration",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp
            )
        }

        // Summary Metric Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Overall Attendance Score", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(
                                text = "$attendancePct%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (attendancePct >= 75) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                        Surface(
                            color = if (attendancePct >= 75) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (attendancePct >= 75) "✓ Eligible / Regular" else "⚠ Needs Attention",
                                color = if (attendancePct >= 75) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Present", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("$presentCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Absent", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("$absentCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Leave", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("$leaveCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        Surface(
                            color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("$totalDays", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        // Filters and search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter date or remarks (e.g. 2026-10)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "ALL" to "All ($totalDays)",
                        "PRESENT" to "Present ($presentCount)",
                        "ABSENT" to "Absent ($absentCount)",
                        "LEAVE" to "Leave ($leaveCount)"
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

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(modifier = Modifier.padding(28.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No attendance records found for this filter.", color = Color.Gray, fontSize = 13.sp)
                            Text("Your admin marks attendance daily.", color = Color(0xFF64748B), fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredList) { rec ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = rec.date,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            if (rec.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rec.notes,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Status Badge
                        Surface(
                            color = when (rec.status) {
                                "PRESENT" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                "ABSENT" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                "LEAVE" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else -> Color(0xFF64748B).copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (rec.status) {
                                    "PRESENT" -> "✓ PRESENT"
                                    "ABSENT" -> "✗ ABSENT"
                                    "LEAVE" -> "⏸ LEAVE"
                                    else -> rec.status
                                },
                                color = when (rec.status) {
                                    "PRESENT" -> Color(0xFF10B981)
                                    "ABSENT" -> Color(0xFFEF4444)
                                    "LEAVE" -> Color(0xFFF59E0B)
                                    else -> Color.White
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentReceiptsAndFeesTabScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    onViewReceipt: (Receipt) -> Unit,
    onPayFee: (FeeRecord) -> Unit,
    onOpenPaymentDialog: (Double, String, String, String) -> Unit
) {
    val fees by viewModel.studentFees.collectAsState()
    val receipts by viewModel.studentReceipts.collectAsState()
    val payments by viewModel.studentPayments.collectAsState()

    var subTab by remember { mutableIntStateOf(0) } // 0: Verified Receipts (रसीदें), 1: Fee Ledger, 2: Payment History

    val totalFee = fees.sumOf { it.totalAmount }
    val paidFee = fees.sumOf { it.paidAmount }
    val pendingFee = fees.sumOf { it.pendingAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Course & Fee Payment History & Receipts",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "Instant payment receipts (PDF), course transaction history & fee ledger",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp
            )
        }

        // Dues & Payment Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pending Outstanding Dues", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(
                                text = "₹${pendingFee.toInt()}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (pendingFee > 0) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                        }
                        if (pendingFee > 0) {
                            Button(
                                onClick = {
                                    val consolidated = FeeRecord(
                                        id = "TOTAL_DUE_${System.currentTimeMillis()}",
                                        userId = userProfile.uid,
                                        studentName = userProfile.displayName,
                                        month = "Consolidated All Months",
                                        feeType = "TOTAL_DUE",
                                        feeDescription = "Total Outstanding Dues (${fees.filter { it.pendingAmount > 0 }.size} Bills)",
                                        totalAmount = pendingFee,
                                        paidAmount = 0.0,
                                        pendingAmount = pendingFee,
                                        status = "DUE"
                                    )
                                    onPayFee(consolidated)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay Dues Now")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Total Registered Fees: ₹${totalFee.toInt()} • Cleared: ₹${paidFee.toInt()} • Receipts: ${receipts.size}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Sub Tabs
        item {
            TabRow(
                selectedTabIndex = subTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Receipts (${receipts.size})") })
                Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Fees Ledger") })
                Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Payment Log (${payments.size})") })
            }
        }

        if (subTab == 0) {
            // OFFICIAL RECEIPTS (PDF DOWNLOAD)
            if (receipts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No verified receipts yet. Generated automatically when your payment is approved by admin.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(receipts) { receipt ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = receipt.receiptNumber,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${receipt.purpose} • ₹${receipt.amount.toInt()}",
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Ref: ${receipt.transactionReference.ifEmpty { "VERIFIED" }} • Status: ${receipt.status}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { onViewReceipt(receipt) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("A4 Receipt PDF", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else if (subTab == 1) {
            // MONTHLY FEES LEDGER
            if (fees.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No fee bills issued to your account.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(fees) { feeItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (feeItem.status == "PAID") Color(0xFF0F172A) else Color(0xFF2D1B28)
                        ),
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
                                Text(
                                    text = "${feeItem.month} (${feeItem.feeType})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = feeItem.feeDescription.ifEmpty { "Tuition / Institute Fee" },
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Due: ₹${feeItem.pendingAmount.toInt()} / ₹${feeItem.totalAmount.toInt()} (Paid: ₹${feeItem.paidAmount.toInt()})",
                                    color = if (feeItem.pendingAmount > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                                    fontSize = 11.sp
                                )
                            }
                            if (feeItem.pendingAmount > 0) {
                                Button(
                                    onClick = { onPayFee(feeItem) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Pay ₹${feeItem.pendingAmount.toInt()}", fontSize = 11.sp)
                                }
                            } else {
                                Badge(containerColor = Color(0xFF16A34A)) {
                                    Text("PAID", color = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // PAYMENT HISTORY LOG (UTR, SCREENSHOT, STATUS)
            if (payments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No payments submitted yet.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(payments) { p ->
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
                                    Text(p.purpose, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("Amount: ₹${p.amount.toInt()} • UTR: ${p.transactionReference}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                }
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
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                            if (p.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Remarks: ${p.notes}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
