# Instello email templates

> **Warning:** After changing any template in `emails/`, stop the running backend and restart it manually with `./mvnw spring-boot:run`. The Spring app does not pick up template changes on its own. Restarting reruns the Maizzle build, which regenerates `target/classes/templates/email/*.html`.

Email templates written with [Maizzle 6](https://maizzle.com) (Vue + Tailwind CSS). They compile to plain HTML with inlined styles, which Thymeleaf renders at send time through `EmailTemplateService`, using the template name `email/<file>`.

## Layout

| Path | Purpose |
| --- | --- |
| `emails/*.vue` | Templates. `emails/otp.vue` becomes Thymeleaf template `email/otp`. |
| `maizzle.config.ts` | Build config. Writes output to `../target/classes/templates/email`. |

The compiled HTML is generated during the build and is not committed.

## Writing templates

- Style with Tailwind classes only. Maizzle inlines them at build time, so don't add `style=""` by hand.
- Put runtime values in `th:*` attributes and give each one placeholder text for the preview:

  ```vue
  <Text th:text="${code}">000000</Text>
  <Img th:src="${logoUrl}" src="https://instello.in/instello.svg" width="160" alt="Instello" />
  ```

- Don't use Vue `{{ }}` or Thymeleaf `[[...]]` for runtime values. Vue would evaluate `{{ }}` at build time, and `th:*` attributes are the only syntax that passes through untouched.

## Commands

Run these from `backend/emails`:

| Command | What it does |
| --- | --- |
| `bun install` | Installs dependencies (only needed for local preview). |
| `bun run dev` | Live preview at http://localhost:3000. |
| `bun run build` | Builds the templates into `target/classes/templates/email`. |

Maven runs the install and build automatically in the `generate-resources` phase, using its own Bun in `target/bun`, so CI and teammates don't need Bun installed. To skip the email build, run Maven with `-Dskip.installbun -Dskip.bun`.

## Testing

From `backend`:

```bash
./mvnw test -Dtest=EmailTemplateServiceTest
```

This builds the templates, renders `email/otp` with Thymeleaf, and checks that the styles were inlined and that every placeholder was filled in.
