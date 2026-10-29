import { ApiError } from "@instello/api-client"

export type SessionUser = {
  email?: string
  authorities?: readonly string[]
  profile?: { firstName?: string; lastName?: string }
}

export function isAdmin(authorities: readonly string[] | undefined): boolean {
  return authorities?.includes("ROLE_ADMIN") ?? false
}

export function displayName(user: {
  email?: string
  profile?: { firstName?: string; lastName?: string }
}): string {
  const name = [user.profile?.firstName, user.profile?.lastName].filter(Boolean).join(" ")
  return name || user.email || "Admin"
}

export function initials(name: string): string {
  const parts = name.split(/\s+/).filter(Boolean)
  const first = parts[0]
  if (!first) return "A"
  const second = parts[1]
  if (!second) return first.slice(0, 2).toUpperCase()
  return `${first[0] ?? ""}${second[0] ?? ""}`.toUpperCase()
}

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError && typeof error.body === "object" && error.body !== null) {
    const message = (error.body as { error?: { message?: string } }).error?.message
    if (message) return message
  }
  return "Something went wrong. Try again."
}
