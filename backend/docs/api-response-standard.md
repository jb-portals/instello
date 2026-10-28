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
| `rate_limited` | 429 (+ `Retry-After` when known) |
| `bad_request` | 400 |
| `internal_error` | 500 |

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
