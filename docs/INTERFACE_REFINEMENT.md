# VOLTLEDGER v1.1.1 — Chrome Interface Refinement

The visual refinements are based on the user's Chrome Edition screenshot and the previously supplied video/style reference.

## Changes included

1. **Glass navigation:** a floating translucent panel with soft silver highlights, border, blur and an improved responsive menu.
2. **Glowing CTAs:** brighter, curved silver buttons with sliding light sweep and clear outline alternatives; Sign In is highly visible.
3. **Hero hierarchy:** smaller typography, tighter heading spacing, balanced supporting copy and two visually distinct actions (Enter platform / Calculator).
4. **Dashboard depth:** frosted metal metric cards with subtle light pools, improved value hierarchy and coherent borders on forms and analytics.

## Local installation (existing GitHub checkout)

The **full ZIP** is a complete project copy. Prefer the **update ZIP** for an existing GitHub checkout, copying the `VOLTLEDGER` directory contents into the existing project without replacing `.git` or local secrets.

```powershell
mvn clean verify
mvn spring-boot:run
```

To run against the existing Firebase database, configure your previously working `FIREBASE_PROJECT_ID`, `SPRING_PROFILES_ACTIVE=firebase`, and external `GOOGLE_APPLICATION_CREDENTIALS` path in the same terminal. Do not add the service-account JSON to the repository.

After copying and testing, commit changed source and push. On automatic Render deployments, pushing to the configured branch triggers a Docker rebuild.

## Acceptance checks

- Landing page at `/` uses smaller hero typography and both curved CTAs.
- Top-right Sign In opens `/login` and authenticated Dashboard routes remain functional.
- Glass navigation and mobile menu remain readable and usable.
- `/calculator`, `/admin`, `/account` and invoice/PDF views still work.
- Check tab navigation: visible focus rings on buttons and header links.
- Check mobile widths (~375px and 768px) and `prefers-reduced-motion` accessibility.
- Check printed invoice PDF or HTML print preview remains legible.
- Hard refresh (`Ctrl+Shift+R`) if CSS appears cached.

## Technical notes

- `src/main/resources/static/css/chrome-theme.css` contains the self-contained enhanced style layer at the end.
- Templates now request `chrome-theme.css(v=2)` to invalidate cached CSS.
- No new JS dependencies, CDN calls, backend controllers, Firebase schema changes or new sensitive config.
- Purely decorative animations do not interfere with authentication or billing.
