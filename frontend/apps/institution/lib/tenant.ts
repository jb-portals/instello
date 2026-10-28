export const ROOT_DOMAIN = process.env.NEXT_PUBLIC_ROOT_DOMAIN ?? "localhost:3002"

const RESERVED_SUBDOMAINS = new Set(["www"])
const SLUG_PATTERN = /^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$/

export function isValidSlug(slug: string): boolean {
  return !RESERVED_SUBDOMAINS.has(slug) && SLUG_PATTERN.test(slug)
}

/**
 * Extracts the institution slug from a host such as `acme.localhost:3002`.
 * Returns null for the bare root domain, reserved subdomains and anything that is
 * not a single valid DNS label below the root domain.
 */
export function getInstitutionSlug(host: string | null, rootDomain = ROOT_DOMAIN): string | null {
  if (!host) return null

  const suffix = `.${rootDomain.toLowerCase()}`
  const normalizedHost = host.toLowerCase()
  if (!normalizedHost.endsWith(suffix)) return null

  const slug = normalizedHost.slice(0, -suffix.length)
  return isValidSlug(slug) ? slug : null
}

export function institutionUrl(slug: string, path = "/", protocol = "http:"): string {
  return `${protocol}//${slug}.${ROOT_DOMAIN}${path}`
}
