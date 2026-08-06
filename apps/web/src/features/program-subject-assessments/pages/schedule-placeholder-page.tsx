"use client";

import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@instello/ui/components/empty";
import { IconCalendarEvent } from "@tabler/icons-react";

export function SchedulePlaceholderPage() {
  return (
    <Empty className="min-h-56 border border-dashed border-border">
      <EmptyMedia variant="icon">
        <IconCalendarEvent />
      </EmptyMedia>
      <EmptyHeader>
        <EmptyTitle>Scheduling comes next</EmptyTitle>
        <EmptyDescription>
          Soon you will schedule exam sittings for these assessment components
          against your academic calendar.
        </EmptyDescription>
      </EmptyHeader>
    </Empty>
  );
}
