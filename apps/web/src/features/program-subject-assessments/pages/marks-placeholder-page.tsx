"use client";

import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@instello/ui/components/empty";
import { IconReportAnalytics } from "@tabler/icons-react";

export function MarksPlaceholderPage() {
  return (
    <Empty className="min-h-56 border border-dashed border-border">
      <EmptyMedia variant="icon">
        <IconReportAnalytics />
      </EmptyMedia>
      <EmptyHeader>
        <EmptyTitle>Marks entry comes next</EmptyTitle>
        <EmptyDescription>
          Soon you will record student marks against each assessment component
          and track pass/fail thresholds.
        </EmptyDescription>
      </EmptyHeader>
    </Empty>
  );
}
