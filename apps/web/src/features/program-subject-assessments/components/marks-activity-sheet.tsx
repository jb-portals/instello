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
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetFooter,
	SheetHeader,
	SheetTitle,
} from "@instello/ui/components/sheet";
import { Skeleton } from "@instello/ui/components/skeleton";
import { IconHistory } from "@tabler/icons-react";
import { useInsQuery } from "@/hooks/convex-react";
import { formatSessionWindow } from "../lib/format-session";

export function MarksActivitySheet({
	assessmentSittingId,
	sittingLabel,
	sessionLabel,
	open,
	onOpenChange,
}: {
	assessmentSittingId: Id<"assessmentSittings"> | null;
	sittingLabel?: string;
	sessionLabel?: string;
	open: boolean;
	onOpenChange: (open: boolean) => void;
}) {
	const activity = useInsQuery(
		api.academicTests.queries.listMarkActivity,
		assessmentSittingId && open ? { assessmentSittingId } : "skip",
	);

	return (
		<Sheet open={open} onOpenChange={onOpenChange}>
			<SheetContent className="flex w-full flex-col sm:max-w-md">
				<SheetHeader>
					<SheetTitle>Mark activity</SheetTitle>
					<SheetDescription>
						{sittingLabel && sessionLabel
							? `${sittingLabel} · ${sessionLabel}`
							: "History of mark create, update, and delete actions."}
					</SheetDescription>
				</SheetHeader>

				<div className="min-h-0 flex-1 overflow-y-auto px-1 py-3">
					{activity === undefined ? (
						<div className="space-y-3">
							<Skeleton className="h-16 w-full" />
							<Skeleton className="h-16 w-full" />
						</div>
					) : activity.length === 0 ? (
						<Empty className="min-h-48 border border-dashed border-border">
							<EmptyMedia variant="icon">
								<IconHistory />
							</EmptyMedia>
							<EmptyHeader>
								<EmptyTitle>No activity yet</EmptyTitle>
								<EmptyDescription>
									Mark saves and edits for this sitting will appear here.
								</EmptyDescription>
							</EmptyHeader>
						</Empty>
					) : (
						<ul className="space-y-2 text-sm">
							{activity.map((log) => (
								<li
									key={log._id}
									className="rounded-lg border border-border px-3 py-2"
								>
									<div className="font-medium">
										{log.performedBy.name} · {log.action}
									</div>
									<div className="text-xs text-muted-foreground">
										{new Date(log.performedAt).toLocaleString()} ·{" "}
										{log.changes.length} change
										{log.changes.length === 1 ? "" : "s"}
									</div>
								</li>
							))}
						</ul>
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
				</SheetFooter>
			</SheetContent>
		</Sheet>
	);
}

export function sessionLabelForSitting(sitting: {
	sessionDate: string;
	sessionStartTime: string;
	sessionEndTime: string;
}) {
	return formatSessionWindow(
		sitting.sessionDate,
		sitting.sessionStartTime,
		sitting.sessionEndTime,
	);
}
