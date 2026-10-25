# VOLTLEDGER v0.6.1 — Firestore build correction

Fixes the `mvn clean test` compilation error in `FirestoreBillArchiveRepository.java` at the `allInvoiceDocuments()` method.

Firestore`s `QuerySnapshot.getDocuments()` returns `List<QueryDocumentSnapshot>`, which is not assignment-compatible with `List<DocumentSnapshot>` in Java. Since `QueryDocumentSnapshot` extends `DocumentSnapshot`, this method now returns `List<? extends DocumentSnapshot>`.

## Upgrade from v0.6.0

If your current project already contains edits, replace only the method return type at line 98 in `src/main/java/com/voltledger/firebase/FirestoreBillArchiveRepository.java`. Do not replace your entire project or private files.

```java
private List<? extends DocumentSnapshot> allInvoiceDocuments() {
    return FirestoreCalls.await(db.collection("invoices").get()).getDocuments();
}
```

Then run `mvn clean test`. Only after tests pass, use `mvn spring-boot:run`.

This is a source-level correction; complete Maven compilation and Spring Boot runtime tests must be performed on a machine with the dependencies installed.
