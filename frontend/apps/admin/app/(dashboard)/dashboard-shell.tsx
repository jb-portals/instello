"use client"

import { Avatar, AvatarFallback } from "@instello/ui/components/avatar"
import { Badge } from "@instello/ui/components/badge"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@instello/ui/components/dropdown-menu"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
} from "@instello/ui/components/sidebar"
import {
  IconCertificate,
  IconChevronDown,
  IconClipboardText,
  IconLogout,
} from "@tabler/icons-react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import type { ReactNode } from "react"
import type { SessionUser } from "@/lib/auth"
import { displayName, initials } from "@/lib/auth"
import { useSignOut } from "./sign-out-button"

export function DashboardShell({ user, children }: { user: SessionUser; children: ReactNode }) {
  const pathname = usePathname()
  const { signOut, isPending } = useSignOut()
  const name = displayName(user)

  return (
    <SidebarProvider>
      <Sidebar>
        <SidebarHeader>
          <div className="flex items-center gap-2 px-2 py-1.5">
            <img src="/instello.svg" alt="Instello" className="h-5 w-auto" />
            <Badge className="bg-primary/10 text-primary">God Mode</Badge>
          </div>
        </SidebarHeader>
        <SidebarContent>
          <SidebarGroup>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem>
                  <SidebarMenuButton isActive={pathname === "/"} render={<Link href="/" />}>
                    <IconClipboardText />
                    <span>Access Requests</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
                <SidebarMenuItem>
                  <SidebarMenuButton
                    isActive={pathname === "/accreditation"}
                    render={<Link href="/accreditation" />}
                  >
                    <IconCertificate />
                    <span>Accreditation</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </SidebarContent>
        <SidebarFooter>
          <SidebarMenu>
            <SidebarMenuItem>
              <DropdownMenu>
                <DropdownMenuTrigger
                  render={
                    <SidebarMenuButton size="lg" className="aria-expanded:bg-sidebar-accent" />
                  }
                >
                  <Avatar size="sm">
                    <AvatarFallback>{initials(name)}</AvatarFallback>
                  </Avatar>
                  <span className="min-w-0 flex-1 truncate text-left">{name}</span>
                  <IconChevronDown className="ml-auto" />
                </DropdownMenuTrigger>
                <DropdownMenuContent side="top" align="start" className="min-w-56">
                  <DropdownMenuItem disabled={isPending} onClick={() => void signOut()}>
                    <IconLogout />
                    Sign out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarFooter>
      </Sidebar>
      <SidebarInset>
        <div className="flex items-center p-2 md:hidden">
          <SidebarTrigger />
        </div>
        {children}
      </SidebarInset>
    </SidebarProvider>
  )
}
