# v1.0.0-rc1 — Phase 6 Candidate (2026-10-25)

## Included

- Existing Firebase Java OOP billing application with clean responsive frontend
- Java server-side progressive tariff calculations and PDF exports
- Firestore-backed accounts, consumers, invoices, analytics and ownership rules
- Corrected `FirestoreCalls` exception unwrapping for duplicate consumer/meter inputs
- Production / Render profile guards, to prevent accidental preview storage deployments
- Configurable public sign-up: disabled by default in `production`
- Brand-matched 403/404/500 HTML error templates and generic server-side error messages
- `/api/live` lightweight host health check; `/api/health` Firestore connectivity cached for 60 seconds
- Docker image with unprivileged user and group 1000 for Render secret files
- Optional `render.yaml`, CI, deployment runbook and release acceptance checklist

## Verification boundaries

Source consistency and ZIP integrity checked in the build environment. `mvn clean verify`, Docker build, Firebase live smoke tests and public Render deployment **must be executed separately**. No live site, tag or repository release is created by this ZIP.

## Known limitations

- Open public registration needs abuse controls for long-term public use.
- Firestore invoice searches currently read collections into memory, suitable for a small college demo but not large-scale production data.
- Managed Firestore export/backup and recovery planning may require a billing-enabled Firebase project.
- Illustrative tariffs are not official real-world electricity rates.
