import {
  Empty,
  EmptyContent,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@instello/ui/components/empty"
import { IconShieldOff } from "@tabler/icons-react"
import { redirect } from "next/navigation"
import type { ReactNode } from "react"
import { isAdmin } from "@/lib/auth"
import { getCurrentUser } from "@/lib/session"
import { DashboardShell } from "./dashboard-shell"
import { SignOutButton } from "./sign-out-button"

export default async function DashboardLayout({ children }: { children: ReactNode }) {
  const user = await getCurrentUser()
  if (!user) redirect("/login")

  if (!isAdmin(user.authorities)) {
    return (
      <main className="flex min-h-svh items-center justify-center p-6">
        <Empty>
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <IconShieldOff />
            </EmptyMedia>
            <EmptyTitle>Admin access required</EmptyTitle>
            <EmptyDescription>This account is not a platform admin.</EmptyDescription>
          </EmptyHeader>
          <EmptyContent>
            <SignOutButton />
          </EmptyContent>
        </Empty>
      </main>
    )
  }

  return <DashboardShell user={user}>{children}</DashboardShell>
}
