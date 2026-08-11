"use client";

import { api } from "@instello/convex/api";
import type { Id } from "@instello/convex/dataModel";
import { Button } from "@instello/ui/components/button";
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@instello/ui/components/empty";
import { Input } from "@instello/ui/components/input";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@instello/ui/components/sheet";
import { Skeleton } from "@instello/ui/components/skeleton";
import { IconTrash, IconUsers } from "@tabler/icons-react";
import { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { useInsMutation, useInsQuery } from "@/hooks/convex-react";
import { getConvexErrorMessage } from "@/lib/convex-error";
import { formatSessionWindow } from "../lib/format-session";

function markKey(
  studentId: Id<"students">,
  componentId: Id<"assessmentComponents">,
) {
  return `${studentId}:${componentId}`;
}

export function MarksEntrySheet({
  assessmentSittingId,
  open,
  onOpenChange,
}: {
  assessmentSittingId: Id<"assessmentSittings"> | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const sheet = useInsQuery(
    api.academicTests.queries.getMarksSheet,
    assessmentSittingId && open ? { assessmentSittingId } : "skip",
  );
  const upsertMarks = useInsMutation(api.academicTests.mutations.upsertMarks);
  const deleteMark = useInsMutation(api.academicTests.mutations.deleteMark);

  const [draft, setDraft] = useState<Record<string, string>>({});
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (!sheet) return;
    const next: Record<string, string> = {};
    for (const mark of sheet.marks) {
      next[markKey(mark.studentId, mark.assessmentComponentId)] = String(
        mark.marks,
      );
    }
    setDraft(next);
  }, [sheet]);

  const existingByKey = useMemo(() => {
    const map = new Map<
      string,
      {
        _id: Id<"assessmentMarks">;
        marks: number;
      }
    >();
    if (!sheet) return map;
    for (const mark of sheet.marks) {
      map.set(markKey(mark.studentId, mark.assessmentComponentId), {
        _id: mark._id,
        marks: mark.marks,
      });
    }
    return map;
  }, [sheet]);

  async function handleSave() {
    if (!assessmentSittingId || !sheet) return;

    const entries: Array<{
      assessmentComponentId: Id<"assessmentComponents">;
      studentId: Id<"students">;
      marks: number;
    }> = [];

    for (const student of sheet.students) {
      for (const component of sheet.components) {
        const key = markKey(student._id, component._id);
        const raw = draft[key];
        if (raw === undefined || raw.trim() === "") continue;

        const marks = Number(raw);
        if (!Number.isFinite(marks)) {
          toast.error(`Invalid marks for ${student.usn} / ${component.name}`);
          return;
        }

        const existing = existingByKey.get(key);
        if (existing && existing.marks === marks) continue;

        entries.push({
          assessmentComponentId: component._id,
          studentId: student._id,
          marks,
        });
      }
    }

    if (entries.length === 0) {
      toast.message("No mark changes to save");
      return;
    }

    setIsSaving(true);
    try {
      await upsertMarks({
        assessmentSittingId,
        entries,
      });
      toast.success("Marks saved");
    } catch (error) {
      toast.error(getConvexErrorMessage(error, "Failed to save marks"));
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete(markId: Id<"assessmentMarks">, key: string) {
    try {
      await deleteMark({ id: markId });
      setDraft((prev) => {
        const next = { ...prev };
        delete next[key];
        return next;
      });
      toast.success("Mark deleted");
    } catch (error) {
      toast.error(getConvexErrorMessage(error, "Failed to delete mark"));
    }
  }

  const componentCount = sheet?.components.length ?? 1;
  const sheetMaxWidth = `min(100vw - 1.5rem, ${14 + componentCount * 7.5}rem)`;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        className="flex w-full flex-col sm:max-w-none"
        style={{ maxWidth: sheetMaxWidth }}
      >
        <SheetHeader>
          <SheetTitle>
            {sheet?.sitting.assessmentSchemaName ?? "Enter marks"}
          </SheetTitle>
          <SheetDescription>
            {sheet
              ? `${sheet.sitting.className} · ${formatSessionWindow(
                  sheet.sitting.sessionDate,
                  sheet.sitting.sessionStartTime,
                  sheet.sitting.sessionEndTime,
                )}`
              : "Loading sitting…"}
          </SheetDescription>
        </SheetHeader>

        <div className="min-h-0 flex-1 overflow-y-auto px-1 py-3">
          {sheet === undefined ? (
            <div className="space-y-3">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-24 w-full" />
              <Skeleton className="h-24 w-full" />
            </div>
          ) : sheet.students.length === 0 ? (
            <Empty className="min-h-48 border border-dashed border-border">
              <EmptyMedia variant="icon">
                <IconUsers />
              </EmptyMedia>
              <EmptyHeader>
                <EmptyTitle>No students in this class</EmptyTitle>
                <EmptyDescription>
                  Add students to the class roster before entering marks.
                </EmptyDescription>
              </EmptyHeader>
            </Empty>
          ) : sheet.components.length === 0 ? (
            <Empty className="min-h-48 border border-dashed border-border">
              <EmptyHeader>
                <EmptyTitle>No components on this schema</EmptyTitle>
                <EmptyDescription>
                  Add assessment components before entering marks.
                </EmptyDescription>
              </EmptyHeader>
            </Empty>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-sm">
                <thead>
                  <tr className="border-b border-border text-left">
                    <th className="sticky left-0 bg-background px-2 py-2 font-medium">
                      Student
                    </th>
                    {sheet.components.map((component) => (
                      <th
                        key={component._id}
                        className="px-2 py-2 font-medium whitespace-nowrap"
                      >
                        {component.name}
                        <span className="ml-1 text-muted-foreground">
                          /{component.totalAllotedMarks}
                        </span>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {sheet.students.map((student) => (
                    <tr key={student._id} className="border-b border-border/70">
                      <td className="sticky left-0 bg-background px-2 py-2">
                        <div className="font-medium">
                          {student.firstName} {student.lastName}
                        </div>
                        <div className="text-xs text-muted-foreground">
                          {student.usn}
                        </div>
                      </td>
                      {sheet.components.map((component) => {
                        const key = markKey(student._id, component._id);
                        const existing = existingByKey.get(key);
                        return (
                          <td key={component._id} className="px-2 py-2">
                            <div className="flex items-center gap-1">
                              <Input
                                type="number"
                                min={0}
                                max={component.totalAllotedMarks}
                                step="any"
                                className="w-20"
                                value={draft[key] ?? ""}
                                onChange={(event) => {
                                  const value = event.target.value;
                                  setDraft((prev) => ({
                                    ...prev,
                                    [key]: value,
                                  }));
                                }}
                                aria-label={`Marks for ${student.usn} ${component.name}`}
                              />
                              {sheet.canDeleteMarks && existing ? (
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="icon"
                                  className="size-8 text-muted-foreground"
                                  onClick={() =>
                                    handleDelete(existing._id, key)
                                  }
                                  aria-label="Delete mark"
                                >
                                  <IconTrash className="size-4" />
                                </Button>
                              ) : null}
                            </div>
                          </td>
                        );
                      })}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <SheetFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
          >
            Close
          </Button>
          <Button
            type="button"
            onClick={handleSave}
            disabled={isSaving || sheet === undefined}
          >
            {isSaving ? "Saving…" : "Save marks"}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
