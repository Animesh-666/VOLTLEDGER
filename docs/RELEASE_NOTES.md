# VOLTLEDGER v1.0.0 — Stable College & Portfolio Release

**Release date:** 25 October 2026  
**Live website:** https://voltledger-java.onrender.com/  
**Repository:** https://github.com/Animesh-666/VOLTLEDGER

## Features delivered

- **Java OOP billing:** residential and commercial progressive tariff strategies, validated consumer and meter details, fixed charges and illustrative tax using `BigDecimal`.
- **Clean Energy frontend:** responsive landing page, calculator, login, administration and consumer pages.
- **Accounts and authorization:** Spring Security, BCrypt, ADMIN/CONSUMER roles and consumer-specific invoice permissions.
- **Firestore data:** server-side Firebase Admin SDK persistence for users, consumers, meter index and invoices.
- **Billing experience:** searchable history, analytics dashboard and Java PDFBox invoice exports.
- **Delivery:** Java 21/Maven build, GitHub Actions CI, Docker and Render with runtime Firebase Secret Files.

## Demonstration calculation

275 units residential: 100 × ₹4 + 100 × ₹6 + 75 × ₹8 = ₹1,600 energy charges; ₹75 fixed charge; ₹83.75 illustrative tax; **₹1,758.75 total**.

## Verification

The release candidate's Maven GitHub Actions CI passed at commit `8b5b9b40fe166d775342e83f35e65698cffb71ec`. The project owner reports completing live checks of the deployed project. Check CI again on the new v1.0.0 source commit before publishing the GitHub Release. This educational product has not had an independent security, compliance or utility certification audit.

## Known limitations

- Tariffs are fictitious examples, not official provider rates; no real payment processing or certified legal invoices.
- Firestore history currently enumerates small demo collections; indexed queries/pagination are required at scale.
- Public registration should remain restricted without account abuse protection.
- Additional monitoring, password recovery, audit trails and backup/restore procedures are needed for real use.
- Render Free may sleep during periods of inactivity.

## Next ideas

Configurable real-world tariff sources, multi-language PDF bills, optional bill forecasting and service notifications.

See [Security](SECURITY.md), [Final handoff](PROJECT_HANDOFF.md), [Architecture](ARCHITECTURE.md) and [College report](COLLEGE_REPORT.md).
