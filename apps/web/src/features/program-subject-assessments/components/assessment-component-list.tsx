"use client";

import { api } from "@instello/convex/api";
import type { Id } from "@instello/convex/dataModel";
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from "@instello/ui/components/alert";
import { Button } from "@instello/ui/components/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@instello/ui/components/dropdown-menu";
import {
  Empty,
  EmptyContent,
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
  IconAlertCircle,
  IconArrowDown,
  IconArrowUp,
  IconDots,
  IconListDetails,
  IconPencil,
  IconPlus,
  IconTrash,
} from "@tabler/icons-react";
import { useState } from "react";
import { useInsMutation, useInsQuery } from "@/hooks/convex-react";
import { getConvexErrorMessage } from "@/lib/convex-error";
import { AssessmentComponentDialog } from "./assessment-component-dialog";

export function AssessmentComponentList({
  assessmentSchemaId,
}: {
  assessmentSchemaId: Id<"assessmentSchemas">;
}) {
  const components = useInsQuery(
    api.academicTests.queries.listAcademicComponents,
    {
      assessmentSchemaId,
    },
  );
  const removeComponent = useInsMutation(
    api.academicTests.mutations.removeAssessmentComponent,
  );
  const reposComponent = useInsMutation(
    api.academicTests.mutations.reposAssessmentComponent,
  );

  const [actionError, setActionError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogTarget, setDialogTarget] = useState<
    | { mode: "create"; assessmentSchemaId: Id<"assessmentSchemas"> }
    | {
        mode: "edit";
        componentId: Id<"assessmentComponents">;
        initial: {
          name: string;
          totalAllotedMarks: number;
          passingMarks: number;
        };
      }
    | null
  >(null);

  async function handleRemove(id: Id<"assessmentComponents">) {
    setActionError(null);
    try {
      await removeComponent({ id });
    } catch (error) {
      setActionError(
        getConvexErrorMessage(error, "Failed to remove component"),
      );
    }
  }

  async function handleRepos(id: Id<"assessmentComponents">, orderIdx: number) {
    setActionError(null);
    try {
      await reposComponent({ id, orderIdx });
    } catch (error) {
      setActionError(
        getConvexErrorMessage(error, "Failed to reorder component"),
      );
    }
  }

  if (components === undefined) {
    return (
      <div className="space-y-2">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-10 w-full" />
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between gap-2">
        <p className="text-sm font-medium text-muted-foreground">Components</p>
        <Button
          size="sm"
          variant="outline"
          onClick={() => {
            setDialogTarget({ mode: "create", assessmentSchemaId });
            setDialogOpen(true);
          }}
        >
          <IconPlus />
          Add component
        </Button>
      </div>

      {actionError && (
        <Alert variant="destructive">
          <IconAlertCircle />
          <AlertTitle>{actionError}</AlertTitle>
          <AlertDescription>Please try again.</AlertDescription>
        </Alert>
      )}

      {components.length === 0 ? (
        <Empty className="min-h-40 border border-dashed border-border">
          <EmptyMedia variant="icon">
            <IconListDetails />
          </EmptyMedia>
          <EmptyHeader>
            <EmptyTitle>No components yet</EmptyTitle>
            <EmptyDescription>
              Add IA, quiz, or other parts that make up this schema.
            </EmptyDescription>
          </EmptyHeader>
          <EmptyContent>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setDialogTarget({ mode: "create", assessmentSchemaId });
                setDialogOpen(true);
              }}
            >
              <IconPlus />
              Add component
            </Button>
          </EmptyContent>
        </Empty>
      ) : (
        <ItemGroup className="bg-card rounded-lg border border-border">
          {components.map((component, index) => (
            <Item
              key={component._id}
              className="border-x-0 border-t-0 rounded-none border-border! last:border-b-0"
            >
              <ItemContent>
                <ItemTitle>{component.name}</ItemTitle>
                <ItemDescription>
                  {component.passingMarks} / {component.totalAllotedMarks}{" "}
                  passing
                </ItemDescription>
              </ItemContent>
              <ItemActions>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  disabled={index === 0}
                  aria-label="Move up"
                  onClick={() => handleRepos(component._id, index - 1)}
                >
                  <IconArrowUp className="size-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  disabled={index === components.length - 1}
                  aria-label="Move down"
                  onClick={() => handleRepos(component._id, index + 1)}
                >
                  <IconArrowDown className="size-4" />
                </Button>
                <DropdownMenu>
                  <DropdownMenuTrigger
                    render={<Button variant="ghost" size="icon-sm" />}
                  >
                    <IconDots />
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem
                      onClick={() => {
                        setDialogTarget({
                          mode: "edit",
                          componentId: component._id,
                          initial: {
                            name: component.name,
                            totalAllotedMarks: component.totalAllotedMarks,
                            passingMarks: component.passingMarks,
                          },
                        });
                        setDialogOpen(true);
                      }}
                    >
                      <IconPencil className="size-4" />
                      Edit
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      variant="destructive"
                      onClick={() => handleRemove(component._id)}
                    >
                      <IconTrash className="size-4" />
                      Remove
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </ItemActions>
            </Item>
          ))}
        </ItemGroup>
      )}

      <AssessmentComponentDialog
        open={dialogOpen}
        setOpen={setDialogOpen}
        target={dialogTarget}
      />
    </div>
  );
}
