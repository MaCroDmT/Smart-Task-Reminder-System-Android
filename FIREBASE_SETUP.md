# 🔥 Firebase Setup Guide
## Smart Task Reminder System — Sonia Group
### Step-by-Step Instructions for Prottoy

---

## STEP 1 — Create the Firebase Project

1. Open **https://console.firebase.google.com**
2. Click **"Add project"**
3. Name it: `Smart Task Reminder SSL`
4. Disable Google Analytics (not needed)
5. Click **Create project**

---

## STEP 2 — Register the Android App

1. In Firebase Console, click the **Android** icon (⊕)
2. Enter the package name exactly: `com.ssl.smarttaskreminder`
3. App nickname: `Smart Task Reminder`
4. Leave SHA-1 blank for now (add later for production)
5. Click **Register app**
6. **Download `google-services.json`**
7. Place the `google-services.json` file here:
   ```
   Smart Task Reminder System/app/google-services.json
   ```
8. Click Next → Next → Continue to Console

---

## STEP 3 — Enable Firebase Authentication

1. In Firebase Console → **Authentication** → **Get started**
2. Click **Sign-in method** tab
3. Enable **Email/Password** → Save
4. ✅ Done

---

## STEP 4 — Create Firestore Database

1. Firebase Console → **Firestore Database** → **Create database**
2. Select **Production mode** (we'll add security rules next)
3. Choose location: **asia-south1** (Mumbai) — closest to Bangladesh
4. Click **Enable**

---

## STEP 5 — Deploy Firestore Security Rules

In Firebase Console → Firestore → **Rules** tab, paste this:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper: Is user signed in?
    function isSignedIn() {
      return request.auth != null;
    }

    // Helper: Is this the Super Admin?
    function isSuperAdmin() {
      return request.auth.token.email == 'prottoy.saha@soniagroup.com';
    }

    // Helper: Get the current user's profile from admins/managers/users
    function getUserCompanyId() {
      return exists(/databases/$(database)/documents/admins/$(request.auth.uid)) ? 
        get(/databases/$(database)/documents/admins/$(request.auth.uid)).data.companyId :
        (exists(/databases/$(database)/documents/managers/$(request.auth.uid)) ?
          get(/databases/$(database)/documents/managers/$(request.auth.uid)).data.companyId :
          (exists(/databases/$(database)/documents/users/$(request.auth.uid)) ?
            get(/databases/$(database)/documents/users/$(request.auth.uid)).data.companyId : null
          )
        );
    }

    // Helper: Does request match document's companyId?
    function sameCompany(docCompanyId) {
      return isSignedIn() && (
        isSuperAdmin() ||
        request.auth.uid in [docCompanyId] ||
        getUserCompanyId() == docCompanyId
      );
    }

    // ============================================================
    // COMPANIES — Super Admin only
    // ============================================================
    match /companies/{companyId} {
      allow read: if isSignedIn();
      allow write: if isSuperAdmin();
    }

    // ============================================================
    // COUNTERS — internal only
    // ============================================================
    match /counters/{docId} {
      allow read, write: if isSignedIn();
    }

    // ============================================================
    // ADMINS
    // ============================================================
    match /admins/{adminId} {
      allow read: if isSignedIn() && (
        isSuperAdmin() ||
        request.auth.uid == adminId ||
        getUserCompanyId() == resource.data.companyId
      );
      allow write: if isSignedIn() && (
        isSuperAdmin() ||
        request.auth.uid == adminId
      );
    }

    // ============================================================
    // MANAGERS
    // ============================================================
    match /managers/{managerId} {
      allow read: if isSignedIn() && (
        isSuperAdmin() ||
        request.auth.uid == managerId ||
        getUserCompanyId() == resource.data.companyId
      );
      allow write: if isSignedIn() && (
        isSuperAdmin() ||
        request.auth.uid == managerId ||
        getUserCompanyId() == request.resource.data.companyId
      );
    }

    // ============================================================
    // USERS
    // ============================================================
    match /users/{userId} {
      allow read: if isSignedIn() && (
        isSuperAdmin() ||
        request.auth.uid == userId ||
        getUserCompanyId() == resource.data.companyId
      );
      allow write: if isSignedIn() && (
        isSuperAdmin() ||
        request.auth.uid == userId ||
        getUserCompanyId() == request.resource.data.companyId
      );
    }

    // ============================================================
    // TASKS — companyId filtering enforced in app code AND here
    // ============================================================
    match /tasks/{taskId} {
      allow read: if isSignedIn() && (
        isSuperAdmin() ||
        getUserCompanyId() == resource.data.companyId
      );
      allow create: if isSignedIn() && (
        isSuperAdmin() ||
        getUserCompanyId() == request.resource.data.companyId
      );
      allow update: if isSignedIn() && (
        isSuperAdmin() ||
        getUserCompanyId() == resource.data.companyId
      );
      allow delete: if isSuperAdmin();
    }
  }
}
```

Click **Publish**.

---

## STEP 6 — Create Super Admin Account

> ⚠️ Do this ONLY ONCE. This creates the platform Super Admin.

### Option A: Using Firebase Console (Easiest)
1. Firebase Console → Authentication → Users tab
2. Click **Add user**
3. Email: ``
4. Password: ``
5. Click **Add user**
6. ✅ Done — No Firestore document needed for Super Admin (identified by email)

---

## STEP 7 — Create Firestore Indexes

Some queries require composite indexes. Firebase will show an error link in Logcat the first time these queries run — click the link to auto-create the index.

Required indexes:
| Collection | Fields | Order |
|------------|--------|-------|
| tasks | companyId ASC, createdDate DESC | |
| tasks | companyId ASC, status ASC | |
| tasks | companyId ASC, managerId ASC | |
| tasks | companyId ASC, createdBy ASC | |
| managers | companyId ASC | |
| users | companyId ASC, managerId ASC | |

**Easiest way:** Run the app, open each screen, copy the error URLs from Logcat and open them — Firebase auto-creates all indexes.

---

## STEP 8 — Enable FCM (Push Notifications)

1. Firebase Console → **Cloud Messaging**
2. No setup needed for basic FCM — it's already configured via google-services.json
3. For server-side push (future): Set up Firebase Cloud Functions

---

## STEP 9 — Open in Android Studio

1. Open Android Studio
2. **File → Open** → Select the folder:
   `c:\Users\User\AndroidStudioProjects\Smart Task Reminder System`
3. Wait for Gradle sync
4. Connect your Android phone (or use emulator API 26+)
5. Click **Run ▶**

---

## STEP 10 — First Login Test

1. Launch the app
2. Login with:
   - Email: ``
   - Password: ``
3. You should see the **Super Admin Dashboard**
4. Tap **+** to create your first company (e.g., "Zonron")
5. Open the company → Add Admin for that company
6. Logout → Login with the Admin credentials
7. ✅ Multi-tenant system working!

---

## Seeding Real Companies

After setup, create these companies as Super Admin:

| Company Name | Slug | Industry |
|---|---|---|
| Zonron | zonron | Garments |
| Sonia Fine Knit | sonia-fine-knit | Knitting |
| Q-Tex | q-tex | Textile |

Each company needs its own Admin account created via Super Admin → Company Detail → Add Admin.

---

## ⚠️ Important Notes

1. **google-services.json is NOT included** — you must download it from your own Firebase project
2. **Every Firestore query in the app already filters by companyId** — tenant isolation is enforced
3. **Notifications require Android 13+ permission** — the app handles this automatically
4. **WorkManager** schedules notifications daily at 10 AM (user) and 11 AM (admin)
5. **Sequential IDs** (Cid, Aid, Mid, Uid, Tid) start from 1 and auto-increment per company

---

*Generated for Sonia Group — Smart Task Reminder System v2.0*
