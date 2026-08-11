"use client";

import { api } from "@instello/convex/api";
import type { Id } from "@instello/convex/dataModel";
import {
	Alert,
	AlertDescription,
	AlertTitle,
} from "@instello/ui/components/alert";
import { Badge } from "@instello/ui/components/badge";
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
	IconCalendarEvent,
	IconCheck,
	IconDots,
	IconFileTypePdf,
	IconPlus,
	IconTrash,
} from "@tabler/icons-react";
import { useState } from "react";
import { useInsMutation, useInsQuery } from "@/hooks/convex-react";
import { getConvexErrorMessage } from "@/lib/convex-error";
import { ScheduleSittingDialog } from "./schedule-sitting-dialog";

function formatSessionDate(sessionDate: string) {
	const [year, month, day] = sessionDate.split("-").map(Number);
	if (!year || !month || !day) return sessionDate;
	return new Date(Date.UTC(year, month - 1, day)).toLocaleDateString(
		undefined,
		{
			year: "numeric",
			month: "short",
			day: "numeric",
			timeZone: "UTC",
		},
	);
}

function formatSessionTime(time: string) {
	const [hours, minutes] = time.split(":").map(Number);
	if (hours === undefined || minutes === undefined) return time;
	const date = new Date();
	date.setHours(hours, minutes, 0, 0);
	return date.toLocaleTimeString(undefined, {
		hour: "numeric",
		minute: "2-digit",
	});
}

function formatSessionWindow(
	sessionDate: string,
	sessionStartTime: string,
	sessionEndTime: string,
) {
	return `${formatSessionDate(sessionDate)} · ${formatSessionTime(sessionStartTime)}–${formatSessionTime(sessionEndTime)}`;
}

export function AssessmentSittingList({
	programSubjectId,
}: {
	programSubjectId: Id<"programSubjects">;
}) {
	const sittings = useInsQuery(
		api.academicTests.queries.listAssessmentSittings,
		{ programSubjectId },
	);
	const markConducted = useInsMutation(
		api.academicTests.mutations.markAssessmentSittingConducted,
	);
	const removeSitting = useInsMutation(
		api.academicTests.mutations.removeAssessmentSitting,
	);

	const [dialogOpen, setDialogOpen] = useState(false);
	const [actionError, setActionError] = useState<string | null>(null);

	async function handleMarkConducted(id: Id<"assessmentSittings">) {
		setActionError(null);
		try {
			await markConducted({ id });
		} catch (error) {
			setActionError(
				getConvexErrorMessage(error, "Failed to mark sitting as conducted"),
			);
		}
	}

	async function handleRemove(id: Id<"assessmentSittings">) {
		setActionError(null);
		try {
			await removeSitting({ id });
		} catch (error) {
			setActionError(getConvexErrorMessage(error, "Failed to remove sitting"));
		}
	}

	if (sittings === undefined) {
		return (
			<div className="space-y-3">
				<Skeleton className="h-10 w-40" />
				<Skeleton className="h-24 w-full" />
				<Skeleton className="h-24 w-full" />
			</div>
		);
	}

	return (
		<div className="space-y-4">
			<div className="flex items-center justify-between gap-2">
				<div>
					<h2 className="text-lg font-semibold tracking-tight">
						Exam sittings
					</h2>
					<p className="text-sm text-muted-foreground">
						Schedule each assessment schema per class with a PDF question paper.
						Mark conducted after the test so staff can enter marks by component.
					</p>
				</div>
				<Button onClick={() => setDialogOpen(true)}>
					<IconPlus />
					Schedule sitting
				</Button>
			</div>

			{actionError && (
				<Alert variant="destructive">
					<IconAlertCircle />
					<AlertTitle>Action failed</AlertTitle>
					<AlertDescription>{actionError}</AlertDescription>
				</Alert>
			)}

			{sittings.length === 0 ? (
				<Empty className="min-h-56 border border-dashed border-border">
					<EmptyMedia variant="icon">
						<IconCalendarEvent />
					</EmptyMedia>
					<EmptyHeader>
						<EmptyTitle>No sittings scheduled</EmptyTitle>
						<EmptyDescription>
							Schedule the first sitting for a class when you are ready to
							conduct the test.
						</EmptyDescription>
					</EmptyHeader>
					<EmptyContent>
						<Button onClick={() => setDialogOpen(true)}>
							<IconPlus />
							Schedule sitting
						</Button>
					</EmptyContent>
				</Empty>
			) : (
				<ItemGroup className="gap-2">
					{sittings.map((sitting) => (
						<Item key={sitting._id} variant="outline" className="px-4 py-3">
							<ItemContent>
								<ItemTitle className="flex flex-wrap items-center gap-2">
									<span>{sitting.assessmentSchemaName}</span>
									<Badge
										variant={
											sitting.status === "conducted" ? "default" : "secondary"
										}
									>
										{sitting.status === "conducted" ? "Conducted" : "Scheduled"}
									</Badge>
								</ItemTitle>
								<ItemDescription>
									{sitting.className} ·{" "}
									{formatSessionWindow(
										sitting.sessionDate,
										sitting.sessionStartTime,
										sitting.sessionEndTime,
									)}
								</ItemDescription>
							</ItemContent>
							<ItemActions>
								{sitting.questionPaperUrl && (
									<Button
										variant="ghost"
										size="sm"
										nativeButton={false}
										render={
											// biome-ignore lint/a11y/useAnchorContent: label text is provided via Button children
											<a
												href={sitting.questionPaperUrl}
												target="_blank"
												rel="noreferrer"
											/>
										}
									>
										<IconFileTypePdf className="size-4" />
										Paper
									</Button>
								)}
								{sitting.status === "scheduled" && (
									<DropdownMenu>
										<DropdownMenuTrigger
											render={<Button variant="ghost" size="icon-sm" />}
										>
											<IconDots />
										</DropdownMenuTrigger>
										<DropdownMenuContent align="end">
											<DropdownMenuItem
												onClick={() => void handleMarkConducted(sitting._id)}
											>
												<IconCheck />
												Mark conducted
											</DropdownMenuItem>
											<DropdownMenuItem
												variant="destructive"
												onClick={() => void handleRemove(sitting._id)}
											>
												<IconTrash />
												Remove
											</DropdownMenuItem>
										</DropdownMenuContent>
									</DropdownMenu>
								)}
							</ItemActions>
						</Item>
					))}
				</ItemGroup>
			)}

			<ScheduleSittingDialog
				open={dialogOpen}
				setOpen={setDialogOpen}
				programSubjectId={programSubjectId}
			/>
		</div>
	);
}
