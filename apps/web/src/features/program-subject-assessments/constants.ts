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

export const ASSESSMENT_TABS = [
	{ id: "schemas", label: "Schemas" },
	{ id: "schedule", label: "Schedule" },
	{ id: "marks", label: "Marks" },
] as const;
