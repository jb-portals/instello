# Instello frontend

Turborepo (Bun workspaces) with three Next.js apps sharing one shadcn/ui package.
The backend lives in `../backend`.

| Workspace                    | Name                          | Port | Purpose                                              |
| ---------------------------- | ----------------------------- | ---- | ---------------------------------------------------- |
| `apps/admin`                 | `@instello/admin`             | 3000 | Platform administration                              |
| `apps/organization`          | `@instello/organization`      | 3001 | Organization workspace                               |
| `apps/institution`           | `@instello/institution`       | 3002 | Per-institution app served on `<slug>.<root-domain>` |
| `packages/ui`                | `@instello/ui`                | -    | shadcn components, theme/globals, fonts, `cn()`      |
| `packages/typescript-config` | `@instello/typescript-config` | -    | Shared `tsconfig` bases                              |

## Commands

Run from `frontend/`:

```bash
bun install
bun run dev        # all apps (turbo)
bun run build
bun run typecheck
bun run lint       # Biome check (lint + format + import order)
bun run format     # Biome check --write
bun run check      # lint + typecheck + build
bun run dev --filter=@instello/admin   # a single app
```

## UI components

All components live in `packages/ui/src/components` and are imported by every app:

```tsx
import { Button } from "@instello/ui/components/button"
```

Add a component (installs into `packages/ui`, never into an app):

```bash
bun run ui:add dialog
```

Theme tokens, fonts and Tailwind setup are defined once in `packages/ui/src/styles/globals.css`
and `packages/ui/src/lib/fonts.ts`. Every app's `components.json` points at them, so apps stay visually
identical. Keep `style`, `iconLibrary` and `baseColor` equal in all `components.json` files.

## Institution subdomains

`apps/institution/proxy.ts` reads the request host, extracts the slug below
`NEXT_PUBLIC_ROOT_DOMAIN`, and rewrites `<slug>.<root>/path` to `/<slug>/path`, which is handled by
`app/[institutionSlug]/...`. Paths on the bare root domain such as `/acme/about` redirect to
`acme.<root>/about`, so the slug can only come from the host.

Local development needs no setup because browsers resolve `*.localhost` to loopback:

- `http://localhost:3002` - landing (no institution)
- `http://acme.localhost:3002` - institution `acme`
- `http://acme.localhost:3002/about` - nested route

Copy `apps/institution/.env.example` to `.env.local` to change the root domain. In production set
`NEXT_PUBLIC_ROOT_DOMAIN` to your domain (for example `instello.com`) and add a wildcard DNS record
(`*.instello.com`) and wildcard domain on the hosting platform.

## Adding an app

Copy an existing app folder under `apps/`, rename it in `package.json`, give it a unique dev port, and run
`bun install`. It automatically picks up the shared TypeScript config, UI package and Biome rules.
