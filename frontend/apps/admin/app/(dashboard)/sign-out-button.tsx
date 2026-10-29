"use client"

import { clearCsrfCache } from "@instello/api-client"
import { useLogout } from "@instello/api-client/query"
import { Button } from "@instello/ui/components/button"
import { useQueryClient } from "@tanstack/react-query"
import { useRouter } from "next/navigation"

export function useSignOut() {
  const logout = useLogout()
  const queryClient = useQueryClient()
  const router = useRouter()

  async function signOut() {
    try {
      await logout.mutateAsync()
    } finally {
      clearCsrfCache()
      queryClient.clear()
      router.replace("/login")
      router.refresh()
    }
  }

  return { signOut, isPending: logout.isPending }
}

export function SignOutButton() {
  const { signOut, isPending } = useSignOut()

  return (
    <Button disabled={isPending} onClick={() => void signOut()}>
      Sign out
    </Button>
  )
}
