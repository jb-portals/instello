"use client"

import { ListStatus } from "@instello/api-client"
import { Button } from "@instello/ui/components/button"
import { Card, CardContent } from "@instello/ui/components/card"
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@instello/ui/components/dropdown-menu"
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@instello/ui/components/empty"
import { Input } from "@instello/ui/components/input"
import { IconFilter, IconInbox, IconSearch } from "@tabler/icons-react"
import { useState } from "react"

const STATUS_OPTIONS = [
  { value: ListStatus.PENDING, label: "Pending" },
  { value: ListStatus.CONTACTED, label: "Contacted" },
  { value: ListStatus.APPROVED, label: "Approved" },
  { value: ListStatus.REJECTED, label: "Rejected" },
] as const

export default function AccessRequestsPage() {
  const [search, setSearch] = useState("")
  const [statuses, setStatuses] = useState<ListStatus[]>([])

  function setStatus(status: ListStatus, checked: boolean) {
    setStatuses((current) =>
      checked ? [...current, status] : current.filter((item) => item !== status),
    )
  }

  return (
    <div className="flex flex-1 flex-col gap-6 p-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="space-y-1">
          <h1 className="font-heading text-2xl font-medium tracking-tight">Access Requests</h1>
          <p className="text-sm text-muted-foreground">
            Manage all institutions Access Requests to verify them
          </p>
        </div>
        <div className="flex items-center gap-2">
          <div className="relative w-full sm:w-64">
            <IconSearch className="pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              placeholder="Search..."
              className="pl-8"
              onChange={(event) => setSearch(event.target.value)}
            />
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="outline" />}>
              <IconFilter />
              Status
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-44">
              {STATUS_OPTIONS.map((option) => (
                <DropdownMenuCheckboxItem
                  key={option.value}
                  closeOnClick={false}
                  checked={statuses.includes(option.value)}
                  onCheckedChange={(checked) => setStatus(option.value, checked === true)}
                >
                  {option.label}
                </DropdownMenuCheckboxItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
      <Card className="min-h-80 flex-1">
        <CardContent className="flex flex-1 items-center justify-center">
          <Empty className="border-0">
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <IconInbox />
              </EmptyMedia>
              <EmptyTitle>No access requests</EmptyTitle>
              <EmptyDescription>Access requests will show up here.</EmptyDescription>
            </EmptyHeader>
          </Empty>
        </CardContent>
      </Card>
    </div>
  )
}
