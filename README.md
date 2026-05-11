  ---

  📌 Smart Task Reminder & Management System (Android)

  A high-fidelity, Android-based enterprise task management application designed with a modern Glassmorphism UI. It features strict multi-tenant isolation,
  hierarchical roles, advanced deadline rescheduling audits, and automated intelligent notifications using Firebase Cloud Functions.

  ---

  🚀 Key Features

   - Multi-Tenant Architecture: Secure data isolation using companyId. Multiple companies can use the app without ever seeing each other's data.
   - Advanced Role-Based System:
     - Super Admin: Platform owner with "Ghost Mode" impersonation for global support.
     - Admin: Executive oversight with deep analytics and PDF reporting.
     - Manager: Team leaders with assignment and monitored rescheduling powers.
     - User: Staff execution dashboard with one-tap completion.
   - Intelligent Notification Engine (FCM & WorkManager):
     - Instant push notifications for Assignments, Completions, and Reschedules.
     - Automated local daily reminders at 10:00 AM (Users) and 11:00 AM (Admins).
   - Accountability & Reschedule Auditing:
     - Managers can extend deadlines but must provide mandatory remarks.
     - System archives the "Old Deadline" and tracks the "Reschedule Count."
   - Executive Analytics: Live tracking of Best/Worst Performers, Departmental Health Scores, and "Most Rescheduled" leaderboards.
   - Rich Visuals: Android Home Screen Widgets and a consistent "Glass Design" aesthetic with semi-transparent cards and dynamic status badges.

  ---

  🧠 Core Functionality

   - Smart Task Lifecycle: Pending → Overdue → Completed.
   - Completion Tracking: Automatically tags completions as "On-Time" or "Late" based on the deadline timestamp.
   - Sequential IDs: Auto-generating, human-readable IDs (e.g., Task #102, CID-1) for easy offline communication.
   - Ghost Mode: Super Admins can securely jump into any Company's dashboard to troubleshoot and audit locally.
   - Admin Oversight: Transparent visibility into exactly which Manager is responsible for which User on every task.

  ---

  🏗️ Tech Stack

   - Android: Kotlin, XML (MVVM Architecture)
   - Backend/Database: Firebase Firestore (with custom server-side indexing)
   - Authentication: Firebase Auth
   - Push Notifications: Firebase Cloud Messaging (FCM) + Cloud Functions (Node.js v2)
   - Local Scheduling: Android WorkManager / AlarmManager
   - Reporting: Built-in PDF Generator

  ---

  📂 Project Structure

   1 app/
   2 ├── src/main/java/com/ssl/smarttaskreminder/
   3 │   ├── data/          # Firestore Repositories & Data Models
   4 │   ├── notifications/ # FCM Services, Widget Providers, Alarm Receivers
   5 │   ├── ui/            # Glassmorphism Activities & Adapters (Role-based)
   6 │   ├── utils/         # PDF Generators, Constants, Session Management
   7 │   └── viewmodel/     # MVVM ViewModels linking UI to Repositories
   8 └── functions/         # Node.js Firebase Cloud Functions (Triggers)

  ---

  🔐 Roles & Permissions

  ┌─────────────┬──────────────────┬─────────────────────────────────────────────────────────────────────────────────┐
  │ Role        │ Access Level     │ Key Capabilities                                                                │
  ├─────────────┼──────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
  │ Super Admin │ Platform Global  │ Register companies, appoint Admins, use "Ghost Mode", global data deletion.     │
  │ Admin       │ Company Level    │ Manage staff, view analytics, generate PDF reports, audit rescheduled tasks.    │
  │ Manager     │ Department Level │ Assign tasks, view team status, reschedule deadlines (with mandatory remarks).  │
  │ User        │ Personal Level   │ View assigned tasks, mark tasks as complete, receive daily automated reminders. │
  └─────────────┴──────────────────┴─────────────────────────────────────────────────────────────────────────────────┘
  ---

  🔔 Notification Logic

   - Action-Triggered (Cloud Functions):
     - Assignment: "Task Assigned by [Manager Name]" + Task ID & Deadline.
     - Completion: "Task Completed" + Met Deadline (Yes/No) sent to Managers/Admins.
     - Reschedule: "Deadline Updated" sent to User with the Manager's new date.
   - Time-Triggered (Local):
     - Daily at 10:00 AM: User reminder for pending/overdue tasks.
     - Daily at 11:00 AM: Admin summary of overdue company tasks.

  ---

  📊 Task Lifecycle & Auditing

   1 Create Task → (Optional: Reschedule + Mandatory Remark) → Pending / Overdue → Mark Completed

   - Pending: Standard state before deadline.
   - Overdue: Server automatically syncs status if deadline passes. "Mark Complete" changes to "Late Completion."
   - Completed: Stored with exact timestamp and "On-time/Late" tag for analytics.

  ---

  ⚙️ Setup Instructions

   1. Clone repository:

   1    git clone https://github.com/your-username/your-repo.git

   2. Android Studio Setup:
      - Open the project in Android Studio.
      - Add your google-services.json to the app/ directory.

   3. Firebase Setup:
      - Enable Authentication (Email/Password).
      - Enable Firestore and deploy the Security Rules.
      - Deploy Cloud Functions:

   1      cd functions
   2      firebase deploy --only functions

   4. Run the Project:
      - Compile and run on an Android 13+ device to test the notification permission flow.

  ---

  📌 Notes

   * IDs (Tid, Cid, Mid, Uid) are sequentially auto-generated via a custom Firestore counter system to ensure human readability.
   * The system uses sendEachForMulticast in Cloud Functions to efficiently notify entire departments of task completions.
   * Designed and optimized for internal enterprise use.

  ---

  👨‍💻 Author

  Designed, Developed & Maintained by Prottoy Saha for Sonia Group as an internal enterprise solution for advanced task tracking and productivity
  monitoring.
