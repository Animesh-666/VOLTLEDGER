# Phase 6 — GitHub + Render + Firebase: screen-by-screen guide

## Before starting

This is a **release candidate**. Run `mvn clean verify` locally. Use ONLY synthetic/demo consumers, not real utility records. Keep your existing Firebase project and Firestore records: deploying code does not migrate, delete, or reset data.

### Safety prerequisites

- Your Firebase project ID (not a secret) and active Cloud Firestore database.
- An existing Firebase Admin service-account JSON key held privately on your computer. **Never commit or send this key.**
- Access to GitHub and Render, and ability to create services on your plan.
- Confirm MongoDB credentials exposed in earlier screenshots are revoked, even though MongoDB is no longer used.
- Run `mvn clean verify` and fix any failing test. Do not proceed with a broken build.

## 1 — Publish clean source to GitHub

From your **new extracted Phase 6 folder** (the directory containing `pom.xml`):

```powershell
git init
git branch -M main
git status --short
# Inspect tracked candidate files; never commit keys, passwords, database dumps, or .env.
git add .
git status --short
git commit -m "release: VOLTLEDGER Firebase v1.0.0-rc1"
```

In GitHub, create an empty repository named `VOLTLEDGER` (no README/license preselected if the local repo already includes them). Copy **your actual** repository URL and run:

```powershell
git remote add origin https://github.com/YOUR_USERNAME/VOLTLEDGER.git
git push -u origin main
```

If using an existing Git repository, **do not** initialize, rewrite history, or overwrite its branch; use the normal commit/push workflow. Never copy the old `.git` into a new unrelated repository unless that is intentional.

GitHub workflow `.github/workflows/ci.yml` runs `mvn clean verify` on pull requests and main pushes. Verify it passes in GitHub Actions before production deployment.

## 2 — Add Render web service

1. Sign in at https://dashboard.render.com/ and choose **New → Web Service**.
2. Connect GitHub and select your VOLTLEDGER repository.
3. Service name: `voltledger-java` or a unique available name.
4. **Language/Runtime: Docker**; choose repository branch `main` and root `.`.
5. Choose a suitable plan (Free is for demonstrations; memory/idle limitations may apply).
6. In Advanced, set **Health Check Path**: `/api/live`.
7. **Do not deploy until secrets are configured** (below). If Render begins a deployment automatically, a failed first launch is recoverable; configure secrets and redeploy.

Alternatively use `render.yaml` as a Blueprint starting point. It deliberately contains **no secrets**.

## 3 — Configure environment variables

In Render service → **Environment → Environment Variables** set:

| Key | Value | Sensitive? |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `firebase,production` | No |
| `FIREBASE_PROJECT_ID` | exact Firebase project ID | No |
| `GOOGLE_APPLICATION_CREDENTIALS` | `/etc/secrets/firebase-service-account.json` | Path only |
| `VL_PUBLIC_REGISTRATION_ENABLED` | `false` initially | No |
| `VL_BOOTSTRAP_ADMIN_USERNAME` | `admin` or another permitted username | No |
| `VL_BOOTSTRAP_ADMIN_PASSWORD` | **a private, new, unique 12–72-byte secret**, needed only if the target Firestore database has no administrator yet | YES |

If the existing Firestore `users` collection already contains an enabled admin, you normally **do not need** to set `VL_BOOTSTRAP_ADMIN_PASSWORD` again; `AdminBootstrap` returns immediately when `adminCount() > 0`. Never overwrite an existing admin document from deployment.

**Do not commit a filled `.env`, secret JSON, password or base64 credential.**

## 4 — Upload Firebase Admin JSON as a Render Secret File

1. Render service → **Environment → Secret Files → Add Secret File**.
2. Filename: `firebase-service-account.json`.
3. In *Contents* paste the full service-account JSON privately in **Render's secret management form**, not in a code file or public chat.
4. Save the service environment and deploy.
5. Runtime path will be `/etc/secrets/firebase-service-account.json`.

The Dockerfile runs with an unprivileged account in **group 1000** for Render's secret-file access requirements. The JSON is never copied into the Docker image.

## 5 — Confirm Render deployment

Render → your service → **Events / Logs**. Verify a successful build and that Spring Boot started. Copy the assigned **actual** `https://...onrender.com` URL (do not guess it).

Smoke-test in this order:

1. `GET /api/live` → `status: UP` (process liveness only).
2. `GET /api/health` → `status: UP` (cached Firebase connectivity test). This endpoint may use a Firestore document read at most once per 60 seconds for each app instance.
3. Open `/`, `/calculator` and test 275 residential units → **₹1,758.75** (illustrative tariff).
4. Open `/login`, sign in as admin, then `/admin/consumers`.
5. Confirm existing Firestore `users` and `consumers` appear; no database migration is needed because both sites use the same Firestore project.
6. Use *synthetic* data to create one invoice; check amount and PDF.
7. Sign in as consumer and confirm assigned invoice access, and other users' invoice URLs are not readable.
8. Sign out; verify `/admin` redirects to login / blocks access.
9. Confirm HTTP traffic redirects to HTTPS (Render manages TLS) and session cookie has `Secure`.

**Public account registration is OFF by default in production.** If demonstration sign-up is needed, set `VL_PUBLIC_REGISTRATION_ENABLED=true` temporarily; production public sign-up without rate limiting and abuse monitoring should not be left unrestricted.

## 6 — After successful first deployment

- If you used `VL_BOOTSTRAP_ADMIN_PASSWORD`, remove this runtime secret after confirming the admin account exists, then redeploy. Deleting the env value does **not** delete the Firestore admin account.
- Keep the Firebase Admin key rotated/revocable and scoped to the appropriate project. Disable or delete keys no longer used.
- Do not reveal Firestore `passwordHash` values in public screenshots.
- Monitor Firestore Usage; free quota is finite and database reads scale with history searches.
- Store a safe backup/export plan **before** using real data; Firestore managed backup/restore features may require billing.

## 7 — Rollback and incident response

A failed Render release should be rolled back to the last known-good commit/image through Render's Deploys UI. **Do not delete Firestore collections or the service-account key just to fix a startup issue.** Local Preview mode can run without Firebase to isolate UI issues. If a Firebase private key is exposed, revoke it in Google Cloud IAM and issue a new one, then update Render Secret Files and redeploy.

## 8 — Constraints and remaining work

This release candidate demonstrates a secure-by-default **portfolio** pattern, not an audited production utility service. For real users, further work is needed: login/registration rate limiting, audit logs, account recovery, backups, monitoring/alerts, database query pagination/indexes and load testing. Render Free services can spin down, and 512 MB deployments may be memory-constrained depending on dependencies.

Sources:
- https://render.com/docs/web-services
- https://render.com/docs/configure-environment-variables
- https://render.com/docs/docker-secrets
- https://firebase.google.com/docs/admin/setup
- https://firebase.google.com/docs/firestore/quotas
