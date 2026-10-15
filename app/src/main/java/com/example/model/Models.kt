package com.example.model

import com.google.firebase.Timestamp

enum class UserRole {
    ADMIN, STUDENT, ENTERTAINMENT
}

enum class UserMode {
    STUDENT, ENTERTAINMENT
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: String = "STUDENT",
    val selectedMode: String = "STUDENT",
    val isBlocked: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class StudentProfile(
    val userId: String = "",
    val studentId: String = "",
    val fullName: String = "",
    val email: String = "",
    val gradeClass: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val enrolledCourses: List<String> = emptyList(),
    val unlockedVideos: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)

data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "General",
    val thumbnailUrl: String = "",
    val price: Double = 0.0,
    val isFree: Boolean = false,
    val isPublished: Boolean = true,
    val instructorName: String = "Neol Bharat",
    val lessonsCount: Int = 0,
    val createdAt: Timestamp? = null
)

data class Lesson(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val pdfUrl: String = "",
    val durationMinutes: Int = 10,
    val orderIndex: Int = 1,
    val createdAt: Timestamp? = null
)

data class VideoItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val category: String = "General",
    val section: String = "EDUCATION",
    val isPaid: Boolean = false,
    val price: Double = 0.0,
    val requiredCourseId: String = "",
    val views: Long = 0,
    val likes: Long = 0,
    val isPublished: Boolean = true,
    val createdAt: Timestamp? = null
)

data class ShortItem(
    val id: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val creatorName: String = "Neol Bharat",
    val section: String = "ENTERTAINMENT",
    val likesCount: Long = 0,
    val isPublished: Boolean = true,
    val createdAt: Timestamp? = null
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val targetAudience: String = "ALL",
    val targetStudentEmail: String = "",
    val targetUserId: String = "",
    val targetUserName: String = "",
    val priority: String = "NORMAL",
    val scheduledDate: String = "",
    val createdAt: Timestamp? = null
)

data class AttendanceRecord(
    val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val date: String = "",
    val status: String = "PRESENT",
    val notes: String = "",
    val recordedAt: Timestamp? = null
)

data class FeeRecord(
    val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val month: String = "",
    val feeType: String = "Tuition",
    val feeDescription: String = "",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val status: String = "PENDING",
    val dueDate: String = "",
    val updatedAt: Timestamp? = null
)

data class PaymentRecord(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val amount: Double = 0.0,
    val purpose: String = "Course Fee",
    val targetId: String = "",
    val targetName: String = "",
    val transactionReference: String = "",
    val paymentMethod: String = "UPI",
    val status: String = "PENDING",
    val verifiedBy: String = "",
    val attachmentUrl: String = "",
    val attachmentType: String = "SCREENSHOT",
    val notes: String = "",
    val submittedAt: Timestamp? = null,
    val reviewedAt: Timestamp? = null
)

data class Receipt(
    val id: String = "",
    val receiptNumber: String = "",
    val paymentId: String = "",
    val userId: String = "",
    val studentName: String = "",
    val studentId: String = "",
    val userEmail: String = "",
    val purpose: String = "Course Fee",
    val targetName: String = "",
    val amount: Double = 0.0,
    val paymentDate: String = "",
    val issuedAt: Timestamp? = null
)

data class UpiQrCode(
    val id: String = "",
    val appName: String = "Neol Bharat",
    val upiId: String = "",
    val payeeName: String = "",
    val qrImageUrl: String = "",
    val barcodeNumber: String = "",
    val isPrimary: Boolean = true,
    val createdAt: Timestamp? = null
)

data class AppSettings(
    val instituteName: String = "Neol Bharat Institute",
    val upiId: String = "",
    val upiPayeeName: String = "",
    val qrImageUrl: String = "",
    val paymentInstructions: String = "",
    val announcementBanner: String = "",
    val supportPhone: String = "",
    val supportEmail: String = "",
    val customAppIconUrl: String = "",
    val latestAppVersion: String = "1.0",
    val appVersionFeatures: String = "",
    val appUpdateUrl: String = "",
    val updatedAt: Timestamp? = null
)
