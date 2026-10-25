# VOLTLEDGER Phase 6 — Release Candidate Acceptance

## Local build and smoke testing

- [ ] `java -version` shows Java 21 or compatible compiler configured to release 21
- [ ] `mvn clean verify` succeeds; no failures/errors
- [ ] `mvn spring-boot:run` in Preview works without Firebase credentials
- [ ] Local calculator: 275 residential kWh → ₹1,758.75
- [ ] Preview admin/consumer can be tested with synthetic data
- [ ] Duplicate consumer/meter shows validation error, not HTTP 500
- [ ] Firebase mode uses private credentials OUTSIDE repository
- [ ] Admin/consumer login works and Firebase data survives reboot

## Security and user isolation

- [ ] `/admin/**` denied to consumer and anonymous sessions
- [ ] `/account/**` denied to administrator and anonymous sessions
- [ ] Invoice detail and PDF reject an unrelated consumer's invoice
- [ ] Registration creates CONSUMER accounts only
- [ ] CSRF prevents unsolicited state-changing requests
- [ ] Production sessions use secure / HttpOnly cookies over HTTPS
- [ ] Private Firebase Admin JSON not committed to Git or container image
- [ ] Passwords not shown in logs, screenshots, documentation, or source
- [ ] Previously exposed MongoDB password disabled / revoked

## Release and hosting

- [ ] GitHub Actions green on main branch
- [ ] Render profiles are `firebase,production` (never Preview)
- [ ] Render secret file mounted at `/etc/secrets/firebase-service-account.json`
- [ ] Public registration disabled by default or protected by abuse controls
- [ ] `/api/live` and `/api/health` give expected non-secret statuses
- [ ] Firestore free quota monitored; demo data used
- [ ] Downloaded PDFs contain expected totals without exposing other accounts
- [ ] Hosted URL and installation screenshots added to README/portfolio only after verified
- [ ] Rollback plan and backup plan documented

## Evidence for college submission

Add screenshots to `docs/screenshots/` after masking personal identifiers and sensitive fields: public landing page, tariff calculator, admin dashboard, consumer directory, consumer dashboard, charts, PDF invoice, and Java class/architecture diagram. Attach actual test output and GitHub Actions status.
