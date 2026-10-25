# Security controls and deployment limitations

## Already implemented

- ADMIN vs CONSUMER authorization via Spring Security.
- BCrypt storage of login secrets; public registration creates `CONSUMER` only.
- Server-side invoice ownership check for both HTML view and PDF download.
- CSRF enabled on state-changing requests; session fixation protection and HTTP-only session cookies.
- Strict security response headers including CSP; secure session cookies in `production` behind HTTPS.
- Firebase Admin SDK runs only on the server. Service key must not be exposed to browser or Git.
- Basic `403`, `404` and `500` pages avoid showing stack traces to users.
- Duplicate consumer/meter validation errors are preserved for the form.
- Render deployment must set `firebase,production`, preventing accidental ephemeral Preview mode.
- Public registration disabled by default in production unless `VL_PUBLIC_REGISTRATION_ENABLED=true`.
- `GET /api/live` is a cheap liveness check; `GET /api/health` performs a cached Firestore read no more than once per 60 seconds per instance.

## Important limitations (do not claim production certification)

1. **No login brute-force rate limiting / account lockout** yet. Add IP/user based throttling and monitoring for a public service.
2. **No email verification or password reset** yet; users must use demo accounts or controlled registration.
3. **Scalability:** current Firestore history queries enumerate documents and filter in Java; implement indexed server-side queries and pagination before growing data.
4. **No audit trail** for changes to meter ownership; implement immutable actor/time/event logs.
5. **No formal disaster recovery** or scheduled backups included. Determine Firebase plan requirements before enabling paid backup or storing irreplaceable data.
6. **Service account permissions:** Firebase-generated key may have broad project permissions. Restrict IAM as appropriate and rotate/revoke compromised keys. Do not share screenshots containing secret or account hashes.
7. **Invoicing:** charges/taxes are demonstrations; no legal/payment or public electricity-provider integration.
8. Consider production observability, dependency audit, penetration test and resource-limit load tests before real use.

## Secrets

Never commit: `.env`, Firebase private JSON, any files ending `*-service-account.json`, `*firebase-adminsdk*.json`, MongoDB credentials, local database exports or private keys. Git ignore patterns and `scripts/check-repo-secrets.ps1` provide a basic check, not a guarantee. Render stores Firebase key via Secret File mounted at `/etc/secrets/firebase-service-account.json`. After the first admin is created, remove first-run bootstrap password from Render.

## If credentials leak

- Immediately revoke the affected key/database user in its provider console.
- Generate replacement credentials and update the deployment secret storage.
- Audit provider logs where available and check Firestore records for unexpected activity.
- Re-deploy, invalidate access paths as needed, and never paste the credentials into tickets/chats.

See [docs/DEPLOY_RENDER_FIREBASE.md](DEPLOY_RENDER_FIREBASE.md) and [docs/RELEASE_ACCEPTANCE.md](RELEASE_ACCEPTANCE.md).
