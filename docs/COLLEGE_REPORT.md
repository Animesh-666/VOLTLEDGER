# VOLTLEDGER — College Project Report (editable draft)

**Project title:** VOLTLEDGER: Electricity Bill Generator and Energy Analytics using Java OOP

## Abstract

VOLTLEDGER is a Java-based electricity bill generator that calculates consumer charges from consumed electricity units and predefined slab tariffs. Consumers have unique account and meter details, while billing results include slab charges, fixed charges, an illustrative tax, and total payable. The system demonstrates Java classes, objects, constructors, abstraction, interfaces, encapsulation, polymorphism, and Java `BigDecimal` arithmetic. A Spring Boot application provides admin and consumer workflows, saved invoices, searchable history, charts, and PDF exports. A temporary in-memory preview runs without credentials, while Firebase Cloud Firestore is the intended persistent cloud database.

## Objectives

1. Accept and validate consumer ID, name, address, meter number and consumed units.
2. Calculate energy charges by slab, fixed charge, tax and final invoice total in Java.
3. Support separate residential and commercial tariff strategies using OOP.
4. Persist consumer and invoice records with account ownership safeguards.
5. Generate readable bill previews and downloadable PDF invoices.
6. Present usage insights and searchable invoice history in a polished responsive frontend.
7. Demonstrate software engineering fundamentals: tests, version control, secure storage integration and deployment.

## Problem statement

Manual electricity billing can involve arithmetic mistakes, duplicated consumer records, unclear slab calculations, and poor retrieval of past invoices. Students often build console-only calculators which fail to demonstrate real application flows. VOLTLEDGER shows a transparent billing engine with a usable web interface while retaining the OOP emphasis of the course.

## Scope

- **Included:** bill previews, sample tariff slabs, Java calculation, consumer IDs, meter numbers, secure role login, invoice history, analytics, PDF exports.
- **Excluded:** actual utility rates, live meter readings, payment processing, physical energy monitoring, regulatory compliance, predictive forecasting (deferred).

## Methodology

An incremental approach: (1) pure Java domain engine, (2) Spring MVC views, (3) role-based services with temporary preview storage, (4) PDF and analytics, (5) Firestore persistence and deployment preparation. Main bill computation and charge rules are written as Java types, with web MVC and database services separated from the domain layer.

## Billing algorithm (illustrative)

Residential tariff rates in the demo are ₹4 for the first 100 units, ₹6 for the next 100 and ₹8 per unit thereafter. For 275 units, energy charge = (100 × 4) + (100 × 6) + (75 × 8) = ₹1,600. Add illustrative fixed charge ₹75, then a sample 5% tax on ₹1,675, equal to ₹83.75. Final bill = ₹1,758.75. These are **sample rates only**, not any official tariff.

## Java OOP concepts

- **Encapsulation:** consumer attributes with access control and validation.
- **Abstraction:** the abstract class `TariffPlan` and interface `ChargeRule` express shared operations.
- **Polymorphism:** different tariff subclasses override the same methods.
- **Constructors and records:** `Consumer` and immutable record classes.
- **Composition:** `BillingService` uses tariff and charge strategies.
- **Exception handling:** invalid meter/consumer or negative units do not generate a valid bill.

## Modules

- Consumer management — validated consumer and meter information.
- Billing engine — slab and tax computation, `BigDecimal` monetary precision.
- Authentication — BCrypt identity and role-based access.
- Invoice repository — Firestore collections with embedded invoice charge lines; in-memory preview also available.
- PDF export — original Java-generated downloadable documents.
- Analytics — summary statistics and six-month history charts.
- Deployment — Docker and a production-only Firebase profile for deployment.

## Testing plan

Check 0 units, first and second slab boundaries, negative units, invalid forms, persistence after restart, owner-only PDF retrieval, admin-only access, chart/filter correctness and Firebase-backed deployment. **Record actual test results after running Maven locally.** This draft does not claim tests have passed on the developer's computer.

## Limitations and future work

Billing rates are simulated. Public internet deployment needs rate limiting, audit logging, password reset, monitoring, and regular backups before it should be used with sensitive customer data. Later releases could implement rate management, consumption forecasts, email notifications and multilingual invoices.

## Conclusion

VOLTLEDGER meets the core requirement to calculate electricity bills for identified consumers using Java OOP. The additional multi-user web app demonstrates how object-oriented concepts can be adapted into layered, maintainable and testable software.

## References

- Java 21 official documentation: https://docs.oracle.com/en/java/javase/21/
- Spring Boot official reference: https://docs.spring.io/spring-boot/reference/
- Spring Security reference: https://docs.spring.io/spring-security/reference/
- Firebase Admin Java SDK: https://firebase.google.com/docs/admin/setup
- Apache PDFBox: https://pdfbox.apache.org/
- Render deployment docs: https://render.com/docs/
