# Firebase Setup for VOLTLEDGER — Windows / VS Code

This is the **next step**, after testing the clean project in default preview mode.

## 1. Create a Firebase project

Open https://console.firebase.google.com/ and create/select your `VOLTLEDGER` project. Under **Build → Firestore Database**, create the **default Cloud Firestore database (Standard edition)**. Choose production security rules: the browser does not access Firestore directly.

## 2. Get a server-side credential

Go to **Project settings (gear) → Service accounts → Firebase Admin SDK → Generate new private key**. Save the downloaded JSON OUTSIDE your Git repository, for example `C:\Users\YOUR_USER\firebase-secrets\voltledger-admin.json`. Do not screenshot, share, or commit the JSON. Treat it like a password. Do not place it under `src/main/resources`.

The app uses **Firebase Admin SDK inside Java**, not Firebase browser JavaScript and not Firebase Authentication. User passwords are BCrypt-hashed within the Java backend and stored in Firestore. Browser authorization uses Spring Security sessions.

## 3. Activate Firestore locally

Stop the running server. You may run `./scripts/start-firebase.ps1` as a guided helper, or follow the manual commands below. In the **same** VS Code PowerShell terminal:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'firebase'
$env:FIREBASE_PROJECT_ID = 'YOUR_ACTUAL_FIREBASE_PROJECT_ID'
$env:GOOGLE_APPLICATION_CREDENTIALS = 'C:\Users\YOUR_USER\firebase-secrets\voltledger-admin.json'
Test-Path $env:GOOGLE_APPLICATION_CREDENTIALS
```

`Test-Path` must return `True`. Set your own project ID from Firebase Project settings → General. Do not put these secrets in source code. The file path need not be secret but its JSON contents are highly sensitive.

Set the first-run admin password:

```powershell
$env:VL_BOOTSTRAP_ADMIN_USERNAME = 'admin'
$secure = Read-Host 'Choose an admin password (12+ characters)' -AsSecureString
$env:VL_BOOTSTRAP_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $secure).Password
Remove-Variable secure
```

Then start:

```powershell
mvn clean test
mvn spring-boot:run
```

Open http://localhost:8080/login. With first-time credentials, login should work and `/api/health` should report `UP` if Firestore is reachable. A new Firestore database may create the `users` collection only after the admin bootstrap writes the first account.

## 4. What to verify

- Log in as admin.
- Register a **consumer** account in another browser/private window.
- Admin creates a consumer/meter record and assigns it to that registered account.
- Admin saves a 275-unit residential bill; demo total ₹1,758.75.
- The consumer sees only their bill, can view it, and can download its PDF.
- Restart the app: Firestore records remain.
- Another account must not be able to open that bill.

## 5. Common errors

- `FileNotFoundException` / missing credentials → check JSON path with `Test-Path`.
- `PERMISSION_DENIED` → check project ID, Firestore enabled, credential's service account IAM permissions.
- `UNAUTHENTICATED` → key/credential issue; generate and safely store a valid service credential.
- `No admin` on first Firestore startup → set `VL_BOOTSTRAP_ADMIN_PASSWORD` to at least 12 characters.
- Preview works but shows no saved Firestore records → ensure `SPRING_PROFILES_ACTIVE=firebase`.

**Do not paste the JSON or any private key into chat, source files or GitHub.** If exposed, revoke that key and generate a new one.

## 6. Hosted deployment

Set `SPRING_PROFILES_ACTIVE=firebase,production`, `FIREBASE_PROJECT_ID`, and the secret `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64` in your host's private environment settings. This package includes a Java Firebase configuration that supports this variable; don't commit the Base64 string, as it is just another form of the private key. Never activate a hosted service using `preview` mode.
