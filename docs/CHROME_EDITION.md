# VOLTLEDGER — Chrome Edition v1.1.0

## Design reference

The user-supplied 22.5-second cinematic video and `ui_generator_prompt.md` informed the visual language: nearly black background, high-contrast white type, glossy liquid-chrome shapes, full-height scroll sections, roadmap, system overview, and contact section. The reference's generic marketing content has been translated into actual VOLTLEDGER features; no invented market statistics or contact details are used.

## Implementation decision

The source prompt suggested React, Tailwind, Framer Motion and React Three Fiber. VOLTLEDGER already uses a secure Spring Boot / Thymeleaf UI, and replacing it with React would require new authentication/session integration and build/deployment architecture. This edition keeps the functional Thymeleaf frontend. Style and interactive behavior are recreated with **self-hosted CSS, vanilla JavaScript and native WebGL**. There are no CDN dependencies; the existing strict Content Security Policy remains intact.

## Updated pages

- Home `/`: seven cinematic sections plus a final thank-you screen, platform actions, metallic forms and Sign In.
- Login `/login`: redesigned with dark metallic artwork and unchanged Spring Security form/CSRF inputs.
- Registration `/register`: updated visual style; production setting can still disable public registration.
- Calculator `/calculator`: black/silver forms and Java-calculated results.
- Admin `/admin`, consumer `/account`, history and invoice pages: coordinated dark panels/charts/tables.
- Project `/project` and custom errors: coordinated dark theme.
- Print view: white background to keep printed bills clear.

## No backend changes

`BillingService`, Spring Security, Firebase Admin SDK, data repositories, routes and PDF exports are unchanged. All Javascript is decorative or navigational. Data never flows from WebGL to the server.

## Run locally (PowerShell)

1. Keep your existing Firebase credential JSON **outside the Git repository**.
2. Unzip the project and open the folder containing `pom.xml` in VS Code.
3. Run `mvn clean verify`, then configure `SPRING_PROFILES_ACTIVE`, `FIREBASE_PROJECT_ID`, and `GOOGLE_APPLICATION_CREDENTIALS` as in `docs/FIREBASE_SETUP.md`.
4. Run `mvn spring-boot:run` and open `http://localhost:8080/`.
5. HTML/CSS/JS are served from the same Java process. No `npm install` is necessary.
6. Check `/login`, `/dashboard`, `/calculator`, a PDF and the mobile menu.

## Push/deploy without overwriting data

This is a source-code-only UI update; **do not** create a new Firebase database or secret key. Preserve the existing `.git` folder if updating an existing checkout. Copy changed files into the checkout, run Maven verification, then commit/push. Render's Docker build will redeploy. If you still see old styles, hard refresh (`Ctrl+Shift+R`) and confirm `chrome-theme.css` is accessible.

## Accessibility/performance

- System font stack: no remote font-loading requirements.
- WebGL targets 30 FPS maximum and caps device pixel ratio; animations pause when tab hidden.
- `prefers-reduced-motion` disables WebGL and scroll animations, leaving a static metallic backdrop.
- Form labels, CSRF tokens, server validation and keyboard-accessible buttons are preserved.
- Statistics are platform capabilities, **not** external market claims.
