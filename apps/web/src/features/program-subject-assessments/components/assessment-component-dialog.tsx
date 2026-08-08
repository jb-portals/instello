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
import { IconAlertCircle } from "@tabler/icons-react";
import { revalidateLogic, useForm } from "@tanstack/react-form-nextjs";
import { useEffect, useState } from "react";
import { useInsMutation } from "@/hooks/convex-react";
import { getConvexErrorMessage } from "@/lib/convex-error";
import {
	AssessmentComponentFormSchema,
	type AssessmentComponentFormValues,
} from "../constants";

type ComponentDialogMode =
	| { mode: "create"; assessmentSchemaId: Id<"assessmentSchemas"> }
	| {
			mode: "edit";
			componentId: Id<"assessmentComponents">;
			initial: AssessmentComponentFormValues;
	  };

export function AssessmentComponentDialog({
	open,
	setOpen,
	target,
}: {
	open: boolean;
	setOpen: (open: boolean) => void;
	target: ComponentDialogMode | null;
}) {
	const createComponent = useInsMutation(
		api.academicTests.mutations.createAssessmentComponent,
	);
	const updateComponent = useInsMutation(
		api.academicTests.mutations.updateAssessmentComponent,
	);
	const [globalError, setGlobalError] = useState<string | null>(null);
	const [isSubmitting, setIsSubmitting] = useState(false);

	const form = useForm({
		defaultValues: {
			name: "",
			totalAllotedMarks: 20,
			passingMarks: 8,
		} satisfies AssessmentComponentFormValues,
		validationLogic: revalidateLogic(),
		validators: {
			onSubmit: AssessmentComponentFormSchema,
		},
		onSubmit: async ({ value }) => {
			if (!target) return;
			setGlobalError(null);
			setIsSubmitting(true);

			try {
				if (target.mode === "create") {
					await createComponent({
						assessmentSchemaId: target.assessmentSchemaId,
						name: value.name.trim(),
						totalAllotedMarks: value.totalAllotedMarks,
						passingMarks: value.passingMarks,
					});
				} else {
					await updateComponent({
						id: target.componentId,
						body: {
							name: value.name.trim(),
							totalAllotedMarks: value.totalAllotedMarks,
							passingMarks: value.passingMarks,
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
							? "Failed to create assessment component"
							: "Failed to update assessment component",
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
			form.setFieldValue("totalAllotedMarks", target.initial.totalAllotedMarks);
			form.setFieldValue("passingMarks", target.initial.passingMarks);
		} else {
			form.reset();
		}
	}, [open, target, form]);

	const title =
		target?.mode === "edit"
			? "Edit assessment component"
			: "New assessment component";

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogContent>
				<DialogHeader>
					<DialogTitle>{title}</DialogTitle>
					<DialogDescription>
						Components are parts of an assessment schema such as IA, quiz, or
						lab.
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
											placeholder="IA 1"
											aria-invalid={isInvalid}
										/>
										{isInvalid && (
											<FieldError errors={field.state.meta.errors} />
										)}
									</Field>
								);
							}}
						</form.Field>

						<form.Field name="totalAllotedMarks">
							{(field) => {
								const isInvalid =
									field.state.meta.isTouched && !field.state.meta.isValid;
								return (
									<Field data-invalid={isInvalid}>
										<FieldLabel htmlFor={field.name}>
											Total allotted marks
										</FieldLabel>
										<Input
											id={field.name}
											name={field.name}
											type="number"
											inputMode="numeric"
											min={0}
											value={field.state.value}
											onBlur={field.handleBlur}
											onChange={(event) =>
												field.handleChange(Number(event.target.value))
											}
											aria-invalid={isInvalid}
										/>
										{isInvalid && (
											<FieldError errors={field.state.meta.errors} />
										)}
									</Field>
								);
							}}
						</form.Field>

						<form.Field name="passingMarks">
							{(field) => {
								const isInvalid =
									field.state.meta.isTouched && !field.state.meta.isValid;
								return (
									<Field data-invalid={isInvalid}>
										<FieldLabel htmlFor={field.name}>Passing marks</FieldLabel>
										<Input
											id={field.name}
											name={field.name}
											type="number"
											inputMode="numeric"
											min={0}
											value={field.state.value}
											onBlur={field.handleBlur}
											onChange={(event) =>
												field.handleChange(Number(event.target.value))
											}
											aria-invalid={isInvalid}
										/>
										{isInvalid && (
											<FieldError errors={field.state.meta.errors} />
										)}
									</Field>
								);
							}}
						</form.Field>
					</FieldGroup>

					<DialogFooter className="mt-6">
						<DialogClose render={<Button variant="outline" />}>
							Cancel
						</DialogClose>
						<Button type="submit" disabled={isSubmitting}>
							{target?.mode === "edit" ? "Save changes" : "Add component"}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
