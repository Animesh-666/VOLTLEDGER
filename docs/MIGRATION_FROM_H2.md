# Preserving earlier H2 data

**No automatic H2 → Firestore migration is included in this clean Firebase-only application.** Do not delete your previous project directory or its `.mv.db` file. Make an offline backup while the old Java server is stopped.

Do NOT copy `.mv.db` files into this new Firebase project. Instead, when ready to transfer historic records:

1. Work from an immutable backup of the old H2 file using the older Phase 5 SQL project separately.
2. Export `app_users`, `consumers`, `invoices`, and `invoice_lines` with their full original identifiers and BCrypt hashes.
3. Validate the export locally without logging plaintext credentials or personal records.
4. Import users and consumers first, maintaining ownership; then import invoices with charge lines embedded.
5. Compare counts and all monetary totals between original and Firestore collections; verify access under each consumer account.
6. Only after verified restoration, consider retiring the old SQL copy. Do not overwrite newly created Firebase documents.

At this stage, Firestore uses a fresh database. Preview data is ephemeral and deliberately not migrated.
