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
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@instello/ui/components/select";
import { IconAlertCircle } from "@tabler/icons-react";
import { revalidateLogic, useForm } from "@tanstack/react-form-nextjs";
import { useEffect, useState } from "react";
import { useInsMutation, useInsQuery } from "@/hooks/convex-react";
import { getConvexErrorMessage } from "@/lib/convex-error";
import {
	ScheduleSittingFormSchema,
	type ScheduleSittingFormValues,
} from "../constants";
import {
	QUESTION_PAPER_MAX_BYTES,
	uploadQuestionPaperPdf,
} from "../lib/upload-question-paper";
import { DatePickerField } from "./date-picker-field";
import { TimeSlotPicker } from "./time-slot-picker";

export function ScheduleSittingDialog({
	open,
	setOpen,
	programSubjectId,
}: {
	open: boolean;
	setOpen: (open: boolean) => void;
	programSubjectId: Id<"programSubjects">;
}) {
	const schemas = useInsQuery(api.academicTests.queries.listAcademicSchemas, {
		programSubjectId,
	});
	const classes = useInsQuery(
		api.academicTests.queries.listEligibleClassesForSitting,
		{ programSubjectId },
	);
	const generateUploadUrl = useInsMutation(
		api.academicTests.mutations.generateQuestionPaperUploadUrl,
	);
	const scheduleSitting = useInsMutation(
		api.academicTests.mutations.scheduleAssessmentSitting,
	);

	const [pdfFile, setPdfFile] = useState<File | null>(null);
	const [globalError, setGlobalError] = useState<string | null>(null);
	const [isSubmitting, setIsSubmitting] = useState(false);

	const form = useForm({
		defaultValues: {
			assessmentSchemaId: "",
			classId: "",
			sessionDate: "",
			sessionStartTime: "",
			sessionEndTime: "",
		} satisfies ScheduleSittingFormValues,
		validationLogic: revalidateLogic(),
		validators: {
			onSubmit: ScheduleSittingFormSchema,
		},
		onSubmit: async ({ value }) => {
			setGlobalError(null);
			if (!pdfFile) {
				setGlobalError("Upload a PDF question paper");
				return;
			}

			setIsSubmitting(true);
			try {
				const questionPaperStorageId = await uploadQuestionPaperPdf(
					() => generateUploadUrl({}),
					pdfFile,
				);

				await scheduleSitting({
					assessmentSchemaId:
						value.assessmentSchemaId as Id<"assessmentSchemas">,
					classId: value.classId as Id<"classes">,
					sessionDate: value.sessionDate,
					sessionStartTime: value.sessionStartTime,
					sessionEndTime: value.sessionEndTime,
					questionPaperStorageId,
					questionPaperFileName: pdfFile.name,
				});

				setOpen(false);
				form.reset();
				setPdfFile(null);
			} catch (error) {
				setGlobalError(
					getConvexErrorMessage(error, "Failed to schedule sitting"),
				);
			} finally {
				setIsSubmitting(false);
			}
		},
	});

	useEffect(() => {
		if (!open) {
			form.reset();
			setPdfFile(null);
			setGlobalError(null);
		}
	}, [open, form]);

	const noSchemas = schemas !== undefined && schemas.length === 0;
	const noClasses = classes !== undefined && classes.length === 0;

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogContent className="sm:max-w-lg">
				<DialogHeader>
					<DialogTitle>Schedule sitting</DialogTitle>
					<DialogDescription>
						Pick a schema, class, date, and time window, then upload the PDF
						question paper.
					</DialogDescription>
				</DialogHeader>

				<form
					onSubmit={(e) => {
						e.preventDefault();
						e.stopPropagation();
						void form.handleSubmit();
					}}
					className="space-y-4"
				>
					{globalError && (
						<Alert variant="destructive">
							<IconAlertCircle />
							<AlertTitle>Could not schedule</AlertTitle>
							<AlertDescription>{globalError}</AlertDescription>
						</Alert>
					)}

					{(noSchemas || noClasses) && (
						<Alert>
							<AlertTitle>Not ready to schedule</AlertTitle>
							<AlertDescription>
								{noSchemas
									? "Add at least one assessment schema under Schemas first."
									: "No active classes are on this subject’s academic stage yet."}
							</AlertDescription>
						</Alert>
					)}

					<FieldGroup>
						<form.Field name="assessmentSchemaId">
							{(field) => {
								const isInvalid =
									field.state.meta.isTouched && !field.state.meta.isValid;
								return (
									<Field data-invalid={isInvalid || undefined}>
										<FieldLabel htmlFor={field.name}>Schema</FieldLabel>
										<Select
											value={field.state.value || undefined}
											onValueChange={(value) => {
												if (value) field.handleChange(value);
											}}
											disabled={!schemas || schemas.length === 0}
										>
											<SelectTrigger id={field.name}>
												<SelectValue placeholder="Select schema" />
											</SelectTrigger>
											<SelectContent>
												{schemas?.map((schema) => (
													<SelectItem key={schema._id} value={schema._id}>
														{schema.name}
													</SelectItem>
												))}
											</SelectContent>
										</Select>
										{isInvalid && (
											<FieldError errors={field.state.meta.errors} />
										)}
									</Field>
								);
							}}
						</form.Field>

						<form.Field name="classId">
							{(field) => {
								const isInvalid =
									field.state.meta.isTouched && !field.state.meta.isValid;
								return (
									<Field data-invalid={isInvalid || undefined}>
										<FieldLabel htmlFor={field.name}>Class</FieldLabel>
										<Select
											value={field.state.value || undefined}
											onValueChange={(value) => {
												if (value) field.handleChange(value);
											}}
											disabled={!classes || classes.length === 0}
										>
											<SelectTrigger id={field.name}>
												<SelectValue placeholder="Select class" />
											</SelectTrigger>
											<SelectContent>
												{classes?.map((cls) => (
													<SelectItem key={cls._id} value={cls._id}>
														{cls.name}
													</SelectItem>
												))}
											</SelectContent>
										</Select>
										{isInvalid && (
											<FieldError errors={field.state.meta.errors} />
										)}
									</Field>
								);
							}}
						</form.Field>

						<form.Field name="sessionDate">
							{(field) => {
								const isInvalid =
									field.state.meta.isTouched && !field.state.meta.isValid;
								return (
									<Field data-invalid={isInvalid || undefined}>
										<FieldLabel htmlFor={field.name}>Date</FieldLabel>
										<DatePickerField
											id={field.name}
											value={field.state.value}
											onBlur={field.handleBlur}
											onChange={field.handleChange}
										/>
										{isInvalid && (
											<FieldError errors={field.state.meta.errors} />
										)}
									</Field>
								);
							}}
						</form.Field>

						<form.Subscribe selector={(state) => state.values.sessionStartTime}>
							{(sessionStartTime) => (
								<div className="grid gap-4 sm:grid-cols-2">
									<form.Field name="sessionStartTime">
										{(field) => {
											const isInvalid =
												field.state.meta.isTouched && !field.state.meta.isValid;
											return (
												<Field data-invalid={isInvalid || undefined}>
													<FieldLabel>Start time</FieldLabel>
													<TimeSlotPicker
														value={field.state.value}
														onChange={(value) => {
															field.handleChange(value);
															const end = form.getFieldValue("sessionEndTime");
															if (end && end <= value) {
																form.setFieldValue("sessionEndTime", "");
															}
														}}
													/>
													{isInvalid && (
														<FieldError errors={field.state.meta.errors} />
													)}
												</Field>
											);
										}}
									</form.Field>

									<form.Field name="sessionEndTime">
										{(field) => {
											const isInvalid =
												field.state.meta.isTouched && !field.state.meta.isValid;
											return (
												<Field data-invalid={isInvalid || undefined}>
													<FieldLabel>End time</FieldLabel>
													<TimeSlotPicker
														value={field.state.value}
														onChange={field.handleChange}
														minTime={sessionStartTime || undefined}
														emptyLabel="Pick a start time first"
													/>
													{isInvalid && (
														<FieldError errors={field.state.meta.errors} />
													)}
												</Field>
											);
										}}
									</form.Field>
								</div>
							)}
						</form.Subscribe>

						<Field>
							<FieldLabel htmlFor="question-paper">Question paper</FieldLabel>
							<Input
								id="question-paper"
								type="file"
								accept="application/pdf,.pdf"
								onChange={(e) => {
									const file = e.target.files?.[0] ?? null;
									setPdfFile(file);
									setGlobalError(null);
								}}
							/>
							<p className="text-xs text-muted-foreground">
								PDF only, up to {QUESTION_PAPER_MAX_BYTES / (1024 * 1024)}MB
								{pdfFile ? ` · ${pdfFile.name}` : ""}
							</p>
						</Field>
					</FieldGroup>

					<DialogFooter>
						<DialogClose render={<Button type="button" variant="outline" />}>
							Cancel
						</DialogClose>
						<Button
							type="submit"
							disabled={isSubmitting || noSchemas || noClasses}
						>
							{isSubmitting ? "Scheduling…" : "Schedule"}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
