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
	Dialog,
	DialogClose,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@instello/ui/components/dialog";
import {
	Field,
	FieldError,
	FieldGroup,
	FieldLabel,
} from "@instello/ui/components/field";
import { Input } from "@instello/ui/components/input";
import { Textarea } from "@instello/ui/components/textarea";
import { IconAlertCircle } from "@tabler/icons-react";
import { revalidateLogic, useForm } from "@tanstack/react-form-nextjs";
import { useEffect, useState } from "react";
import { useInsMutation } from "@/hooks/convex-react";
import { getConvexErrorMessage } from "@/lib/convex-error";
import {
	NewAssessmentSchemaFormSchema,
	type NewAssessmentSchemaFormValues,
} from "../constants";

type SchemaDialogMode =
	| { mode: "create"; programSubjectId: Id<"programSubjects"> }
	| {
			mode: "edit";
			schemaId: Id<"assessmentSchemas">;
			initial: NewAssessmentSchemaFormValues;
	  };

export function AssessmentSchemaDialog({
	open,
	setOpen,
	target,
}: {
	open: boolean;
	setOpen: (open: boolean) => void;
	target: SchemaDialogMode | null;
}) {
	const createSchema = useInsMutation(
		api.academicTests.mutations.createAssessmentSchema,
	);
	const updateSchema = useInsMutation(
		api.academicTests.mutations.updateAssessmentSchema,
	);
	const [globalError, setGlobalError] = useState<string | null>(null);
	const [isSubmitting, setIsSubmitting] = useState(false);

	const form = useForm({
		defaultValues: {
			name: "",
			description: "",
		} satisfies NewAssessmentSchemaFormValues,
		validationLogic: revalidateLogic(),
		validators: {
			onSubmit: NewAssessmentSchemaFormSchema,
		},
		onSubmit: async ({ value }) => {
			if (!target) return;
			setGlobalError(null);
			setIsSubmitting(true);

			try {
				if (target.mode === "create") {
					await createSchema({
						programSubjectId: target.programSubjectId,
						name: value.name.trim(),
						description: value.description?.trim() || undefined,
					});
				} else {
					await updateSchema({
						id: target.schemaId,
						body: {
							name: value.name.trim(),
							description: value.description?.trim() || undefined,
						},
					});
				}
				form.reset();
				setOpen(false);
			} catch (error) {
				setGlobalError(
					getConvexErrorMessage(
						error,
						target.mode === "create"
							? "Failed to create assessment schema"
							: "Failed to update assessment schema",
					),
				);
			} finally {
				setIsSubmitting(false);
			}
		},
	});

	useEffect(() => {
		if (!open) {
			form.reset();
			setGlobalError(null);
			return;
		}
		if (target?.mode === "edit") {
			form.setFieldValue("name", target.initial.name);
			form.setFieldValue("description", target.initial.description ?? "");
		} else {
			form.reset();
		}
	}, [open, target, form]);

	const title =
		target?.mode === "edit"
			? "Edit assessment schema"
			: "New assessment schema";

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogContent>
				<DialogHeader>
					<DialogTitle>{title}</DialogTitle>
					<DialogDescription>
						Define how this program subject is assessed (for example CIE or
						SEE).
					</DialogDescription>
				</DialogHeader>

				{globalError && (
					<Alert variant="destructive">
						<IconAlertCircle />
						<AlertTitle>{globalError}</AlertTitle>
						<AlertDescription>Please try again.</AlertDescription>
					</Alert>
				)}

				<form
					onSubmit={(event) => {
						event.preventDefault();
						event.stopPropagation();
						void form.handleSubmit();
					}}
				>
					<FieldGroup>
						<form.Field name="name">
							{(field) => {
								const isInvalid =
									field.state.meta.isTouched && !field.state.meta.isValid;
								return (
									<Field data-invalid={isInvalid}>
										<FieldLabel htmlFor={field.name}>Name</FieldLabel>
										<Input
											id={field.name}
											name={field.name}
											value={field.state.value}
											onBlur={field.handleBlur}
											onChange={(event) =>
												field.handleChange(event.target.value)
											}
											placeholder="CIE 1"
											aria-invalid={isInvalid}
										/>
										{isInvalid && (
											<FieldError errors={field.state.meta.errors} />
										)}
									</Field>
								);
							}}
						</form.Field>

						<form.Field name="description">
							{(field) => (
								<Field>
									<FieldLabel htmlFor={field.name}>Description</FieldLabel>
									<Textarea
										id={field.name}
										name={field.name}
										value={field.state.value ?? ""}
										onBlur={field.handleBlur}
										onChange={(event) => field.handleChange(event.target.value)}
										placeholder="Optional notes"
										rows={3}
									/>
								</Field>
							)}
						</form.Field>
					</FieldGroup>

					<DialogFooter className="mt-6">
						<DialogClose render={<Button variant="outline" />}>
							Cancel
						</DialogClose>
						<Button type="submit" disabled={isSubmitting}>
							{target?.mode === "edit" ? "Save changes" : "Create schema"}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
