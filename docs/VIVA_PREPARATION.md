# VOLTLEDGER — Java OOP viva quick preparation

**Q1. Why Java, not JavaScript?** The assignment requires OOP with Java. All calculations, strategies and saved-bill processing run in Java. The browser only presents information and uses minimal JavaScript for UI behavior.

**Q2. What is encapsulation here?** `Consumer` and domain objects hold a set of validated properties; external classes use public accessors instead of mutating arbitrary fields.

**Q3. Where is polymorphism?** Residential and commercial tariff classes extend a common abstract `TariffPlan` class, so `BillingService` can execute the selected algorithm uniformly.

**Q4. What is abstraction?** The abstract `TariffPlan` class and `ChargeRule` interface define behavior while hiding how individual tariff calculations operate.

**Q5. Why `BigDecimal`?** Financial amounts should avoid binary floating-point rounding artifacts. Java `BigDecimal` supports deliberate monetary rounding.

**Q6. How are consumer bills private?** Server-side role authorization limits account endpoints; the Firestore repository checks the linked consumer before returning an invoice. HTML and PDF both apply the same ownership rule.

**Q7. What is Cloud Firestore?** Cloud Firestore is a document database. The Java Admin SDK stores accounts, consumers and invoices as documents and enforces access in the backend.

**Q8. Why use an in-memory preview?** It lets the Java website run without cloud credentials for demonstrations. Its records are temporary and will be lost on restart.

**Q9. What is Spring Boot?** A Java framework that simplifies application setup, dependency wiring, web routes and packaging. The OOP billing core is plain Java and independent of Spring MVC.

**Q10. How are bills computed?** Units are distributed into each rate slab. Charges are summed, fixed costs added, illustrative tax applied and the result stored as a Java invoice object.

**Q11. Why store line items?** They explain how each billing slab contributed to the final total and preserve an invoice breakdown even if current tariff rules change.

**Q12. Is this an actual official electricity billing system?** No. Rates are illustrative. It is a transparent educational and portfolio demonstration.

## Live demo order

1. Explain `Consumer`, `BillingService`, `TariffPlan` in Java.
2. Enter 275 kWh in the public calculator, show ₹1,758.75 example.
3. Login as admin, generate a saved bill for a demo consumer.
4. Login as consumer, open history and show analytics.
5. Download the invoice PDF.
6. Explain Java-based Firestore integration and show the Maven tests, only after they actually pass.
