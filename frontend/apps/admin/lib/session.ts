import "server-only"

import { ApiError, currentUser } from "@instello/api-client"
import { cookies } from "next/headers"
import type { SessionUser } from "@/lib/auth"

export async function getCurrentUser(): Promise<SessionUser | null> {
  const cookie = (await cookies()).toString()
  if (!cookie.includes("JSESSIONID")) return null

  try {
    const response = await currentUser({
      headers: { cookie },
      cache: "no-store",
    })
    return response.data
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) return null
    throw new Error("Couldn't load your account")
  }
}
