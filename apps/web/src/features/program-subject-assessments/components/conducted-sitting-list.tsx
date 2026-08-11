"use client";

import { api } from "@instello/convex/api";
import type { Id } from "@instello/convex/dataModel";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@instello/ui/components/avatar";
import { Button } from "@instello/ui/components/button";
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@instello/ui/components/empty";
import {
  Item,
  ItemActions,
  ItemContent,
  ItemDescription,
  ItemGroup,
  ItemTitle,
} from "@instello/ui/components/item";
import { Skeleton } from "@instello/ui/components/skeleton";
import {
  IconHistory,
  IconReportAnalytics,
  IconUser,
} from "@tabler/icons-react";
import { formatDistanceToNow } from "date-fns";
import { useState } from "react";
import { useInsQuery } from "@/hooks/convex-react";
import { formatSessionWindow } from "../lib/format-session";
import {
  MarksActivitySheet,
  sessionLabelForSitting,
} from "./marks-activity-sheet";
import { MarksEntrySheet } from "./marks-entry-sheet";

type ConductedSitting = {
  _id: Id<"assessmentSittings">;
  assessmentSchemaName: string;
  className: string;
  sessionDate: string;
  sessionStartTime: string;
  sessionEndTime: string;
  latestActivity?: {
    actor: {
      _id: string;
      name: string;
      image?: string;
    };
    description: string;
    updatedAt: number;
  };
};

function ConductedSittingItems({
  sittings,
  onEnterMarks,
  onOpenActivity,
}: {
  sittings: ConductedSitting[];
  onEnterMarks: (id: Id<"assessmentSittings">) => void;
  onOpenActivity: (id: Id<"assessmentSittings">) => void;
}) {
  return (
    <ItemGroup className="gap-2">
      {sittings.map((sitting) => (
        <Item key={sitting._id} variant="outline" className="px-4 py-3">
          <ItemContent>
            <ItemTitle>{sitting.assessmentSchemaName}</ItemTitle>
            <ItemDescription>
              {sitting.className} ·{" "}
              {formatSessionWindow(
                sitting.sessionDate,
                sitting.sessionStartTime,
                sitting.sessionEndTime,
              )}
            </ItemDescription>
            {sitting.latestActivity ? (
              <div className="mt-1.5 flex items-center gap-1.5">
                <Avatar size="xs">
                  {sitting.latestActivity.actor.image ? (
                    <AvatarImage
                      src={sitting.latestActivity.actor.image}
                      alt={sitting.latestActivity.actor.name}
                    />
                  ) : null}
                  <AvatarFallback>
                    <IconUser className="size-3" />
                  </AvatarFallback>
                </Avatar>
                <strong className="text-xs text-muted-foreground">
                  {sitting.latestActivity.actor.name}
                </strong>
                <span className="font-bold text-muted-foreground">·</span>
                <ItemDescription className="truncate text-muted-foreground">
                  {sitting.latestActivity.description}
                </ItemDescription>
              </div>
            ) : (
              <ItemDescription className="mt-1.5 text-muted-foreground">
                No marks entered yet
              </ItemDescription>
            )}
          </ItemContent>
          <ItemActions className="flex flex-col items-end gap-2 sm:flex-row sm:items-center">
            {sitting.latestActivity ? (
              <span className="text-xs text-muted-foreground">
                last updated{" "}
                {formatDistanceToNow(sitting.latestActivity.updatedAt, {
                  addSuffix: true,
                })}
              </span>
            ) : null}
            <div className="flex items-center gap-2">
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => onOpenActivity(sitting._id)}
              >
                <IconHistory />
                Activity
              </Button>
              <Button
                type="button"
                variant="secondary"
                onClick={() => onEnterMarks(sitting._id)}
              >
                Enter marks
              </Button>
            </div>
          </ItemActions>
        </Item>
      ))}
    </ItemGroup>
  );
}

function ConductedSittingSheets({
  sittings,
  marksSittingId,
  activitySittingId,
  setMarksSittingId,
  setActivitySittingId,
}: {
  sittings: ConductedSitting[];
  marksSittingId: Id<"assessmentSittings"> | null;
  activitySittingId: Id<"assessmentSittings"> | null;
  setMarksSittingId: (id: Id<"assessmentSittings"> | null) => void;
  setActivitySittingId: (id: Id<"assessmentSittings"> | null) => void;
}) {
  const activitySitting =
    activitySittingId === null
      ? undefined
      : sittings.find((sitting) => sitting._id === activitySittingId);

  return (
    <>
      <MarksEntrySheet
        assessmentSittingId={marksSittingId}
        open={marksSittingId !== null}
        onOpenChange={(open) => {
          if (!open) setMarksSittingId(null);
        }}
      />
      <MarksActivitySheet
        assessmentSittingId={activitySittingId}
        sittingLabel={activitySitting?.assessmentSchemaName}
        sessionLabel={
          activitySitting ? sessionLabelForSitting(activitySitting) : undefined
        }
        open={activitySittingId !== null}
        onOpenChange={(open) => {
          if (!open) setActivitySittingId(null);
        }}
      />
    </>
  );
}

export function ConductedSittingListForProgramSubject({
  programSubjectId,
}: {
  programSubjectId: Id<"programSubjects">;
}) {
  const sittings = useInsQuery(
    api.academicTests.queries.listConductedSittingsForProgramSubject,
    { programSubjectId },
  );
  const [marksSittingId, setMarksSittingId] =
    useState<Id<"assessmentSittings"> | null>(null);
  const [activitySittingId, setActivitySittingId] =
    useState<Id<"assessmentSittings"> | null>(null);

  if (sittings === undefined) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-24 w-full" />
        <Skeleton className="h-24 w-full" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-lg font-semibold tracking-tight">Marks entry</h2>
        <p className="text-sm text-muted-foreground">
          Enter student marks by component for sittings that have been marked
          conducted.
        </p>
      </div>

      {sittings.length === 0 ? (
        <Empty className="min-h-56 border border-dashed border-border">
          <EmptyMedia variant="icon">
            <IconReportAnalytics />
          </EmptyMedia>
          <EmptyHeader>
            <EmptyTitle>No conducted sittings yet</EmptyTitle>
            <EmptyDescription>
              Mark a scheduled sitting as conducted on the Schedule tab, then
              return here to enter marks.
            </EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : (
        <ConductedSittingItems
          sittings={sittings}
          onEnterMarks={setMarksSittingId}
          onOpenActivity={setActivitySittingId}
        />
      )}

      <ConductedSittingSheets
        sittings={sittings}
        marksSittingId={marksSittingId}
        activitySittingId={activitySittingId}
        setMarksSittingId={setMarksSittingId}
        setActivitySittingId={setActivitySittingId}
      />
    </div>
  );
}

export function ConductedSittingListForAssigned({
  classId,
  programSubjectId,
}: {
  classId: Id<"classes">;
  programSubjectId: Id<"programSubjects">;
}) {
  const sittings = useInsQuery(
    api.academicTests.queries.listConductedSittingsForAssigned,
    { classId, programSubjectId },
  );
  const [marksSittingId, setMarksSittingId] =
    useState<Id<"assessmentSittings"> | null>(null);
  const [activitySittingId, setActivitySittingId] =
    useState<Id<"assessmentSittings"> | null>(null);

  if (sittings === undefined) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-24 w-full" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-lg font-semibold tracking-tight">Marks entry</h2>
        <p className="text-sm text-muted-foreground">
          Record component marks for conducted sittings in this assigned class
          subject.
        </p>
      </div>

      {sittings.length === 0 ? (
        <Empty className="min-h-56 border border-dashed border-border">
          <EmptyMedia variant="icon">
            <IconReportAnalytics />
          </EmptyMedia>
          <EmptyHeader>
            <EmptyTitle>No conducted sittings yet</EmptyTitle>
            <EmptyDescription>
              Once a sitting for this class is marked conducted, you can enter
              marks here.
            </EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : (
        <ConductedSittingItems
          sittings={sittings}
          onEnterMarks={setMarksSittingId}
          onOpenActivity={setActivitySittingId}
        />
      )}

      <ConductedSittingSheets
        sittings={sittings}
        marksSittingId={marksSittingId}
        activitySittingId={activitySittingId}
        setMarksSittingId={setMarksSittingId}
        setActivitySittingId={setActivitySittingId}
      />
    </div>
  );
}
