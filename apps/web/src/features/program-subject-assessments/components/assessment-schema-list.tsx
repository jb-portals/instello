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
	IconChevronDown,
	IconClipboardList,
	IconDots,
	IconPencil,
	IconPlus,
	IconTrash,
} from "@tabler/icons-react";
import { useState } from "react";
import { useInsMutation, useInsQuery } from "@/hooks/convex-react";
import { getConvexErrorMessage } from "@/lib/convex-error";
import { cn } from "@/lib/utils";
import { AssessmentComponentList } from "./assessment-component-list";
import { AssessmentSchemaDialog } from "./assessment-schema-dialog";

export function AssessmentSchemaList({
	programSubjectId,
}: {
	programSubjectId: Id<"programSubjects">;
}) {
	const schemas = useInsQuery(api.academicTests.queries.listAcademicSchemas, {
		programSubjectId,
	});
	const removeSchema = useInsMutation(
		api.academicTests.mutations.removeAssessmentSchema,
	);

	const [actionError, setActionError] = useState<string | null>(null);
	const [openSchemaId, setOpenSchemaId] = useState<string | null>(null);
	const [dialogOpen, setDialogOpen] = useState(false);
	const [dialogTarget, setDialogTarget] = useState<
		| { mode: "create"; programSubjectId: Id<"programSubjects"> }
		| {
				mode: "edit";
				schemaId: Id<"assessmentSchemas">;
				initial: { name: string; description: string };
		  }
		| null
	>(null);

	async function handleRemove(id: Id<"assessmentSchemas">) {
		setActionError(null);
		try {
			await removeSchema({ id });
			if (openSchemaId === id) setOpenSchemaId(null);
		} catch (error) {
			setActionError(getConvexErrorMessage(error, "Failed to remove schema"));
		}
	}

	if (schemas === undefined) {
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
						Assessment schemas
					</h2>
					<p className="text-sm text-muted-foreground">
						Configure CIE/SEE-style schemas and their components for this
						allocation.
					</p>
				</div>
				<Button
					onClick={() => {
						setDialogTarget({ mode: "create", programSubjectId });
						setDialogOpen(true);
					}}
				>
					<IconPlus />
					New schema
				</Button>
			</div>

			{actionError && (
				<Alert variant="destructive">
					<IconAlertCircle />
					<AlertTitle>{actionError}</AlertTitle>
					<AlertDescription>Please try again.</AlertDescription>
				</Alert>
			)}

			{schemas.length === 0 ? (
				<Empty className="min-h-56 border border-dashed border-border">
					<EmptyMedia variant="icon">
						<IconClipboardList />
					</EmptyMedia>
					<EmptyHeader>
						<EmptyTitle>No assessment schemas yet</EmptyTitle>
						<EmptyDescription>
							Create a schema to define how this subject is assessed.
						</EmptyDescription>
					</EmptyHeader>
					<EmptyContent>
						<Button
							variant="outline"
							onClick={() => {
								setDialogTarget({ mode: "create", programSubjectId });
								setDialogOpen(true);
							}}
						>
							<IconPlus />
							New schema
						</Button>
					</EmptyContent>
				</Empty>
			) : (
				<ItemGroup className="space-y-3 border-0">
					{schemas.map((schema) => {
						const isOpen = openSchemaId === schema._id;
						return (
							<div
								key={schema._id}
								className="overflow-hidden rounded-lg border bg-card"
							>
								<Item className="rounded-none border-0">
									<Button
										variant="ghost"
										size="icon-sm"
										className="shrink-0"
										aria-label={isOpen ? "Collapse" : "Expand"}
										aria-expanded={isOpen}
										onClick={() => setOpenSchemaId(isOpen ? null : schema._id)}
									>
										<IconChevronDown
											className={cn(
												"size-4 transition-transform",
												isOpen && "rotate-180",
											)}
										/>
									</Button>
									<ItemContent
										className="cursor-pointer"
										onClick={() => setOpenSchemaId(isOpen ? null : schema._id)}
									>
										<ItemTitle>{schema.name}</ItemTitle>
										{schema.description ? (
											<ItemDescription>{schema.description}</ItemDescription>
										) : null}
									</ItemContent>
									<ItemActions>
										<DropdownMenu>
											<DropdownMenuTrigger
												render={<Button variant="ghost" size="icon-sm" />}
											>
												<IconDots />
											</DropdownMenuTrigger>
											<DropdownMenuContent align="end">
												<DropdownMenuItem
													onClick={() => {
														const description = schema.description ?? "";
														setDialogTarget({
															mode: "edit",
															schemaId: schema._id,
															initial: {
																name: schema.name,
																description,
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
													onClick={() => handleRemove(schema._id)}
												>
													<IconTrash className="size-4" />
													Remove
												</DropdownMenuItem>
											</DropdownMenuContent>
										</DropdownMenu>
									</ItemActions>
								</Item>
								{isOpen ? (
									<div className="border-t border-border p-4">
										<AssessmentComponentList assessmentSchemaId={schema._id} />
									</div>
								) : null}
							</div>
						);
					})}
				</ItemGroup>
			)}

			<AssessmentSchemaDialog
				open={dialogOpen}
				setOpen={setDialogOpen}
				target={dialogTarget}
			/>
		</div>
	);
}
