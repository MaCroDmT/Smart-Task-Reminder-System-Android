# 📌 Smart Task Reminder & Management System (Android)

Android-based smart task management application with hierarchical roles, deadline tracking, and automated reminder notifications using Firebase.

---

## 🚀 Features

- Task creation with deadline & priority  
- Automated reminders at 10:00 AM by using FCM (User + WorkManager)
- Automated Admin OverDue reminders at 11:00 AM by FCM  
- Pending, Overdue, Completed task tracking  
- Late completion & on-time completion handling  
- Role-based system:
  - Super Admin
  - Admin
  - Manager
  - User  
- Team & user performance analytics  
- Real-time data storage using Firestore  

---

## 🧠 Core Functionality

- Smart notification system based on deadline  
- Auto task categorization (Pending / Overdue / Completed)  
- Completion tracking with timestamp  
- Late completion handling for overdue tasks  
- Role-based dashboards & data visibility  

---

## 🏗️ Tech Stack

- **Android:** Kotlin, XML  
- **Backend:** Firebase Firestore  
- **Authentication:** Firebase Auth  
- **Notifications:** Firebase Cloud Messaging (FCM)  
- **Scheduling:** WorkManager  

---

## 📂 Project Structure

```

app/
├── activities/
├── fragments/
├── adapters/
├── models/
├── utils/
└── firebase/

```

---

## 🔐 Roles & Permissions

| Role         | Access Level |
|--------------|-------------|
| Super Admin  | Full control (Admin, Manager, User, Tasks) |
| Admin        | Manage Managers & Users, view analytics |
| Manager      | Manage own users & tasks |
| User         | Manage own tasks |

---

## 🔔 Notification Logic

- Daily at **10:00 AM**
- First reminder: next day after task creation (if deadline close)
- Repeats every **2 days**
- Stops when task is completed

---

## 📊 Task Lifecycle

```

Create → Pending → Overdue → Completed

````

- Pending → "Mark as Completed"  
- Overdue → "Late Completion"  
- Completed → Stored with timestamp  

---

## ⚙️ Setup Instructions

1. Clone repository
```bash
git clone https://github.com/your-username/your-repo.git
````

2. Open in Android Studio

3. Connect Firebase:

   * Add `google-services.json`
   * Enable Firestore, Auth, FCM

4. Run the project

---

## 📌 Notes

* IDs (Tid, Uid, Mid, Aid) are auto-generated
* Follows structured hierarchy for scalability
* Designed for internal enterprise use

---

## 👨‍💻 Author

Developed by **Prottoy Saha** for **Sonia Group** as an internal enterprise solution for task tracking and productivity monitoring.

```


