# API response and error standard

Instello REST APIs follow GitHub / Stripe / Microsoft conventions:

- **Success** — return the resource (or list page) directly. HTTP status is the success signal.
- **Errors** — always a nested Microsoft-style `error` object. Never reuse the success DTO.

This is a **breaking change** for early `v0.1` clients that read `response.data`, `response.message`, or a body `code` / `status`. Prefer reading the HTTP status line and the bare resource / `error` object.

## Success

### Single resource — `200 OK`

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "…",
  "email": "admin@example.com"
}
```

### Create — `201 Created` (+ `Location` when a resource URI exists)

```http
HTTP/1.1 201 Created
Location: /v0.1/access-requests/{id}
Content-Type: application/json

{
  "id": "…",
  "status": "PENDING"
}
```

### No body — `204 No Content`

Use for logout, fire-and-forget actions (e.g. request OTP), and stubs that have no resource to return.

### Collections — `PageResponse` as the body

Do **not** wrap again in `{ data: … }`.

```json
{
  "content": [ /* items */ ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

## Errors

Always:

```json
{
  "error": {
    "code": "validation_failed",
    "message": "One or more fields are invalid",
    "target": null,
    "details": [
      {
        "code": "NotBlank",
        "target": "email",
        "message": "must not be blank"
      }
    ]
  }
}
```

- `error.code` — stable snake_case machine code (see below)
- `error.message` — human-readable summary
- `error.target` — optional primary field/path
- `error.details` — optional list of field-level problems (validation collects **all** field errors)
- HTTP status stays on the response line only — do **not** mirror it as a JSON int/`status` string

Security `401` / `403` and the global exception handler use the **same** JSON shape.

### Stable error codes

| Code | Typical HTTP |
|------|----------------|
| `not_found` | 404 |
| `conflict` | 409 |
| `validation_failed` | 400 |
| `unauthorized` | 401 |
| `forbidden` | 403 |
| `email_not_verified` | 403 (login with a correct password before the email OTP was verified) |
| `rate_limited` | 429 (+ `Retry-After` when known) |
| `bad_request` | 400 |
| `internal_error` | 500 |

## Owner onboarding and login

All endpoints below are public, served under `/v0.1`, and (like every `POST`) need the CSRF token from `GET /auth/csrf-token`.

| Step | Endpoint | Body | Success |
|------|----------|------|---------|
| 1. Preview invitation | `GET /organizations/invitations?token=` | – | `200 { email, organizationName, expiresAt }` (the email is locked, show it read-only) |
| 2. Accept invitation | `POST /organizations/invitations/accept` | `{ token, password }` (8–72 chars) | `200 { email }`; an unverified account is created and a 6-digit OTP is emailed |
| 2b. Resend code | `POST /auth/email-verification/otp` | `{ email }` | `204` (always, to avoid account probing; `429` when rate limited) |
| 3. Verify email | `POST /auth/email-verification/verify` | `{ email, code }` | `204`; grants `ROLE_OWNER` and marks the invitation accepted |
| 4. Log in | `POST /auth/login` | `{ email, password }` | `204` + session cookie |

Notes:

- The account email comes from the invitation `metadata.email`, never from the request. It must match `invitation.email` or accept fails with `409`.
- Invalid, expired, revoked or already accepted tokens return `404`. A verified account for the email already existing returns `409`.
- Login failures: `401 unauthorized` for bad credentials (including admins, who use OTP only), `403 email_not_verified` for a correct password with an unverified email, `429 rate_limited` after 10 attempts per 15 minutes per email + IP.
- Roles are rows in the `authorities` table (`ROLE_ADMIN`, `ROLE_OWNER`, `ROLE_USER`). `GET /users/me` returns them as `authorities` (previously `roles`).

## Do / don’t for new endpoints

**Do**

- Return `ResponseEntity<T>`, `ResponseEntity<PageResponse<T>>`, or `ResponseEntity<Void>`
- Use `201` + `Location` for creates when a resource URI exists
- Throw domain exceptions (`ResourceNotFoundException`, `ConflictException`, `BadRequestException`, …); the advice maps them to `ApiErrorResponse`
- Keep success bodies free of human “success message” strings

**Don’t**

- Wrap success in `{ code, status, message, data }`
- Return ad-hoc `Map` bodies for resources
- Put stack traces or internal exception types in the error body
- Invent one-off error JSON shapes for controllers or security filters
