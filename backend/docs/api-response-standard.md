# API response standard

Instello APIs use HTTP status codes to describe the outcome. Successful responses return the
requested resource or result directly; errors use the Problem Details format defined by
[RFC 9457](https://www.rfc-editor.org/rfc/rfc9457).

There is no universal success envelope. Do not add `{ "data": ... }`, `{ "success": true }`,
or a success message around a response. The response body is the endpoint's resource/result shape.

## Success responses

### Read or update a resource — `200 OK`

Return the resource itself as JSON:

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "usr_01JABCDEF",
  "email": "admin@example.com",
  "authorities": ["ROLE_ADMIN"]
}
```

### Create a resource — `201 Created`

Return the created resource. Include `Location` when its canonical URI is known:

```http
HTTP/1.1 201 Created
Location: /v0.1/access-requests/ar_01JABCDEF
Content-Type: application/json

{
  "id": "ar_01JABCDEF",
  "status": "PENDING"
}
```

### Collection — `200 OK`

Return a page object directly (do not add another `data` wrapper):

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": [
    { "id": "ar_01JABCDEF", "status": "PENDING" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Successful operation with no representation — `204 No Content`

Return no response body. Use this for operations such as logout, OTP requests, or verification
when there is no result resource to return.

## Error responses: RFC 9457 Problem Details

Return `Content-Type: application/problem+json` and the standard Problem Details members. The
HTTP response status is authoritative and must match the `status` member in the body. Use
`type` as a stable identifier for the problem category; use `about:blank` when no more specific
type URI is defined. `title` is a short summary, `detail` describes this occurrence, and
`instance` identifies the specific occurrence when available.

`about:blank` is the RFC-defined generic problem type. When using it, set `title` to the standard
reason phrase for the HTTP status (for example, `Not Found` for `404`). The application `code`
extension can still provide a more specific machine-readable category, such as
`INVALID_INVITATION`; it does not replace the RFC `type` member. Prefer a stable, specific type
URI when Instello defines one for the problem.

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{
  "type": "https://api.instello.in/problems/validation-failed",
  "title": "Request validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/v0.1/invitations",
  "code": "VALIDATION_FAILED",
  "errors": [
    {
      "code": "NotBlank",
      "target": "email",
      "detail": "must not be blank"
    }
  ]
}
```

`code` and `errors` above are application extension members; RFC 9457 permits extensions but
defines the standard members (`type`, `title`, `status`, `detail`, and `instance`). Use stable,
uppercase application codes such as `VALIDATION_FAILED`, `INVALID_CREDENTIALS`, and
`EMAIL_NOT_VERIFIED`. The HTTP status line is authoritative; the body `status` must match it.

Set `instance` to the request path where the error occurred, without the query string. This
identifies the endpoint in logs without copying query parameters that could contain sensitive
values. Omit `errors` when there are no field-level issues. Do not expose stack traces, internal
exception names, or sensitive data.

`target` is an application extension, not an RFC 9457 member. Use it only on an individual entry
in `errors` to identify the invalid request field (for example, `email`). Clients can use it to
show the validation message next to the relevant input. Omit it when an error is not tied to a
specific field; it is not useful as a general error-level property.

Example without validation extensions:

```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "type": "https://api.instello.in/problems/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "The requested invitation was not found.",
  "instance": "/v0.1/organizations/invitations",
  "code": "NOT_FOUND"
}
```

### Common application error codes

| `code` | HTTP status | Typical use |
|---|---:|---|
| `NOT_FOUND` | 404 | Resource does not exist or should not be disclosed |
| `CONFLICT` | 409 | Request conflicts with current resource state |
| `VALIDATION_FAILED` | 400 | Request fields fail validation |
| `BAD_REQUEST` | 400 | Other malformed or unsupported request |
| `INVALID_CREDENTIALS` | 401 | Submitted credentials are invalid |
| `AUTHENTICATION_REQUIRED` | 401 | Authentication is missing |
| `FORBIDDEN` | 403 | Caller is authenticated but not allowed |
| `EMAIL_NOT_VERIFIED` | 403 | Correct-password login for an unverified email |
| `RATE_LIMITED` | 429 | Request limit exceeded; include `Retry-After` when known |
| `INTERNAL_ERROR` | 500 | Unexpected server failure |

Use the same Problem Details format for errors from controllers, global exception handling,
and security handling. Security errors should not use a separate body schema.

## Do / don't for new endpoints

**Do**

- Return a resource/result directly, `PageResponse<T>` for pages, or no body for `204`.
- Use `201` and `Location` for creates when a canonical resource URI exists.
- Use RFC 9457 Problem Details for every error, including validation and security errors.
- Keep successful response bodies free of generic success messages.

**Don't**

- Wrap success in `{ "code", "status", "message", "data" }`.
- Return ad-hoc `Map` bodies for resources.
- Put stack traces or internal exception types in an error response.
- Invent one-off error JSON shapes for controllers or security filters.
