import * as v from "valibot";

export const AssessmentSchemaNameSchema = v.pipe(
	v.string(),
	v.trim(),
	v.nonEmpty("Name is required"),
);

export const AssessmentSchemaDescriptionSchema = v.string();

export const NewAssessmentSchemaFormSchema = v.object({
	name: AssessmentSchemaNameSchema,
	description: AssessmentSchemaDescriptionSchema,
});

export const AssessmentComponentNameSchema = v.pipe(
	v.string(),
	v.trim(),
	v.nonEmpty("Name is required"),
);

export const AssessmentComponentFormSchema = v.pipe(
	v.object({
		name: AssessmentComponentNameSchema,
		totalAllotedMarks: v.pipe(
			v.number("Total marks is required"),
			v.minValue(0, "Total marks must be zero or greater"),
		),
		passingMarks: v.pipe(
			v.number("Passing marks is required"),
			v.minValue(0, "Passing marks must be zero or greater"),
		),
	}),
	v.forward(
		v.check(
			(input) => input.passingMarks <= input.totalAllotedMarks,
			"Passing marks cannot exceed total allotted marks",
		),
		["passingMarks"],
	),
);

export type NewAssessmentSchemaFormValues = v.InferInput<
	typeof NewAssessmentSchemaFormSchema
>;

export type AssessmentComponentFormValues = v.InferInput<
	typeof AssessmentComponentFormSchema
>;

const SessionTimeSchema = v.pipe(
	v.string(),
	v.nonEmpty("Time is required"),
	v.regex(/^([01]\d|2[0-3]):([0-5]\d)$/, "Use HH:mm format"),
);

export const ScheduleSittingFormSchema = v.pipe(
	v.object({
		assessmentSchemaId: v.pipe(
			v.string(),
			v.nonEmpty("Select an assessment schema"),
		),
		classId: v.pipe(v.string(), v.nonEmpty("Select a class")),
		sessionDate: v.pipe(
			v.string(),
			v.nonEmpty("Date is required"),
			v.regex(/^\d{4}-\d{2}-\d{2}$/, "Use YYYY-MM-DD format"),
		),
		sessionStartTime: SessionTimeSchema,
		sessionEndTime: SessionTimeSchema,
	}),
	v.forward(
		v.check(
			(input) => input.sessionEndTime > input.sessionStartTime,
			"End time must be after start time",
		),
		["sessionEndTime"],
	),
);

export type ScheduleSittingFormValues = v.InferInput<
	typeof ScheduleSittingFormSchema
>;

export const ASSESSMENT_TABS = [
	{ id: "schemas", label: "Schemas" },
	{ id: "schedule", label: "Schedule" },
	{ id: "marks", label: "Marks" },
] as const;
