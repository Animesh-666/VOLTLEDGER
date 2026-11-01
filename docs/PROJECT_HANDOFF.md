# VOLTLEDGER v1.0.0 — Final Project Handoff

## Project links

- Live website: https://voltledger-java.onrender.com/
- Source repository: https://github.com/Animesh-666/VOLTLEDGER
- GitHub Actions: https://github.com/Animesh-666/VOLTLEDGER/actions

## Local build

Requires Java 21 and Maven. From the folder containing `pom.xml`:

```powershell
mvn clean verify
mvn spring-boot:run
```

Default `preview` profile stores temporary records. `firebase` profile uses the Firebase Admin SDK and privately stored service-account credentials. Do not commit credentials.

## Complete the upstream GitHub release

The archive contains the final v1.0.0 source and release notes. Import its files into your **existing** VOLTLEDGER Git repository while preserving `.git` and secret files outside it. Then:

```powershell
mvn clean verify
powershell -ExecutionPolicy Bypass -File .\scripts\check-repo-secrets.ps1
git status --short
git add .
git commit -m "release: finalize VOLTLEDGER v1.0.0"
git push origin main
```

Wait for the new GitHub Actions build to pass. Then:

```powershell
git tag -a v1.0.0 -m "VOLTLEDGER stable v1.0.0"
git push origin v1.0.0
```

Create a GitHub Release from that tag, using `docs/RELEASE_NOTES.md` as the description. Do not tag a failed build. If a tag already exists, inspect it rather than forcibly replacing it.

## Operational checklist

- Render profiles: `firebase,production` (never `preview`).
- Keep the Firebase JSON in Render Secret Files only.
- Public consumer signup remains disabled without safeguards.
- Validate `/api/live` and `/api/health` after deploys.
- Confirm old exposed MongoDB credentials have been revoked.
- Monitor Firestore quota, logging and Render resource usage.
- Maintain a backup/export and rollback plan before storing irreplaceable information.

## College submission

Include `docs/COLLEGE_REPORT.md`, `docs/VIVA_PREPARATION.md`, the Java source, UML/architecture, redacted UI screenshots, sample PDF, GitHub CI evidence and the live demo URL. Use synthetic demonstration data.

VOLTLEDGER v1.0.0 is an educational billing project with illustrative rates, not a certified electricity utility or payment service.
