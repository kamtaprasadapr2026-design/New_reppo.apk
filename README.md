# Neol Bharat - Android Application

Unified education, entertainment, and administrative management platform for students and users built with Kotlin and Jetpack Compose.

---

## 🚀 How to Push this App to GitHub

To push this entire project to your GitHub repository:

1. **Create a new empty repository** on GitHub (e.g. `neol-bharat-app`). Do not initialize it with a README or .gitignore.
2. In your terminal in this project root directory, run:
```bash
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git branch -M main
git push -u origin main
```
3. Once pushed, the **GitHub Actions Workflow** will automatically trigger!

---

## ⚡ GitHub Actions CI/CD Workflows

The repository includes **two automated GitHub Actions workflows**:

### 1. 📦 Package & Release Project ZIP (`.github/workflows/package-zip.yml`)
- **Fast Execution**: Takes only ~15–20 seconds!
- **Runs on**: Every `push` to `main` branch, or manually via GitHub Actions **Run workflow** button.
- **Outputs**:
  - Uploads `neol-bharat-project-source-zip` artifact for 1-click download.
  - Automatically creates a GitHub Release tagged `source-zip-v<run_number>` with the ready-to-run Android Studio source ZIP attached!

### 2. 📱 Full Android APK Build & Release (`.github/workflows/build-apk.yml`)
- **Runs on**: Every `push` to `main` branch or manual trigger.
- **Outputs**:
  - Compiles installable `app-debug.apk` with Java 17 and Gradle.
  - Generates full project ZIP archive.
  - Publishes a release with direct download links for both the APK and source ZIP.

---

## 📱 Quick Start / Direct APK Install

The repository already includes the pre-compiled debug APK:
- **`app-debug.apk`** (Pre-compiled and verified)

To install directly on your Android phone:
1. Transfer `app-debug.apk` to your phone.
2. Tap the file to install (allow "Install unknown apps" if prompted).
3. Open **Neol Bharat**!

---

## 🛠️ Opening the Project in Android Studio

1. **Extract** the project ZIP file to any folder on your computer.
2. Open **Android Studio** (Koala, Ladybug, or newer recommended).
3. Select **File > Open...** and choose this project folder.
4. Allow Gradle to sync dependencies (Gradle 9.3.1 and AGP 9.1.1 with Java 17).
5. Click **Run (▶)** to run on your device or emulator.

---

## 💻 Building via Command Line

### macOS / Linux:
```bash
./gradlew assembleDebug
```

### Windows:
```cmd
gradlew.bat assembleDebug
```

---

## ✨ Features Overview

### 1. ⚙️ Admin Settings & App Logo Management
- Upload, change, or delete custom institute/app logos directly from device gallery or file manager.
- Automatic real-time synchronization across Student, User, and Login screens.
- Configure institute name, UPI ID, payee name, and dynamic payment QR code.

### 2. 📋 Admin Attendance System
- Comprehensive attendance log for all registered students.
- Filter by date, search student by name/roll number/class.
- One-click Present (उपस्थित), Absent (अनुपस्थित), and Leave (अवकाश) marking with instant student app update.

### 3. 🎬 Education & Video Gallery / File Manager
- Upload long videos and short videos from Gallery, Device File Manager, or YouTube links.
- Work and study materials with thumbnail picker.
- Built-in video player dialog for MP4 and YouTube video playback.

### 4. 📢 Personal & Broadcast Announcements (Suchna)
- Send announcements to All Users, Students only, or individually to a specific student.
- Broadcast priority, badges, and notification alerts.

### 5. 📅 Student Attendance Tracking
- Dedicated "Check Attendance" tab with day-by-day record of Present, Absent, and Leave.
- Attendance percentage indicator, summary chips, and remarks for each date.

### 6. 🎓 Student Video Courses & Fee Receipts
- Explore video courses, study lectures, and shorts.
- Course and fee payment with UPI QR integration and payment proof screenshot upload.
- Fee payment history with instant A4 PDF receipt generation and viewing.

### 7. 💳 Admin Payment Verification
- View submitted payment screenshot proofs with zoom and download.
- Fast Accept (Approve & issue receipt), Reject (with remarks), or Delete payment entries.
