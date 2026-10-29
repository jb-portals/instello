const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS"])

type CsrfToken = {
  token: string
  headerName: string
}

const csrfByOriginAndVersion = new Map<string, CsrfToken>()

export function clearCsrfCache(): void {
  csrfByOriginAndVersion.clear()
}

export class ApiError extends Error {
  readonly status: number
  readonly body: unknown

  constructor(status: number, body: unknown) {
    super(`API request failed with status ${status}`)
    this.name = "ApiError"
    this.status = status
    this.body = body
  }
}

export type ErrorType<ErrorBody> = ApiError & { body: ErrorBody }
export type BodyType<Body> = Body

export function apiBaseUrl(): string {
  const configured =
    typeof window === "undefined"
      ? (process.env.API_BASE_URL ?? process.env.NEXT_PUBLIC_API_BASE_URL)
      : process.env.NEXT_PUBLIC_API_BASE_URL
  return (configured ?? "http://localhost:8080").replace(/\/$/, "")
}

function isCsrfToken(body: unknown): body is CsrfToken {
  if (typeof body !== "object" || body === null) return false
  const record = body as Record<string, unknown>
  return typeof record.token === "string" && typeof record.headerName === "string"
}

function versionFromUrl(url: string): string {
  const pathname = new URL(url, apiBaseUrl()).pathname
  const match = /^\/(v[^/]+)/.exec(pathname)
  return match?.[1] ?? "v0.1"
}

async function readBody(response: Response): Promise<unknown> {
  if (response.status === 204) return undefined
  const text = await response.text()
  if (!text) return undefined
  try {
    return JSON.parse(text) as unknown
  } catch {
    return text
  }
}

async function csrfToken(version: string): Promise<CsrfToken> {
  const base = apiBaseUrl()
  const cacheKey = `${base}/${version}`
  const cached = csrfByOriginAndVersion.get(cacheKey)
  if (cached) return cached

  const response = await fetch(`${base}/${version}/auth/csrf-token`, {
    credentials: "include",
  })
  const body = await readBody(response)
  if (!response.ok || !isCsrfToken(body)) throw new ApiError(response.status, body)

  csrfByOriginAndVersion.set(cacheKey, body)
  return body
}

export async function apiFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
  const method = (options.method ?? "GET").toUpperCase()
  const headers = new Headers(options.headers)

  if (!SAFE_METHODS.has(method)) {
    const csrf = await csrfToken(versionFromUrl(url))
    headers.set(csrf.headerName, csrf.token)
  }

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: "include",
  })
  const body = await readBody(response)
  if (!response.ok) throw new ApiError(response.status, body)

  return {
    data: body,
    status: response.status,
    headers: response.headers,
  } as T
}

/** Body-only fetch used by the TanStack Query client. */
export async function apiFetchData<T>(url: string, options?: RequestInit): Promise<T> {
  const result = await apiFetch<{ data: T }>(url, options)
  return result.data
}
