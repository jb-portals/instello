import type { Infer } from "convex/values";
import { vv } from "#schema";

export const CreateAssessmentSchemaInput = {
	programSubjectId: vv.id("programSubjects"),
	name: vv.string(),
	description: vv.optional(vv.string()),
};

export const PatchAssessmentSchemaBody = vv.object({
	name: vv.optional(vv.string()),
	description: vv.optional(vv.string()),
});

export const AssessmentSchemaListItemSchema = vv.object({
	_id: vv.id("assessmentSchemas"),
	name: vv.string(),
	description: vv.optional(vv.string()),
	createdAt: vv.number(),
	updatedAt: vv.optional(vv.number()),
});

export type AssessmentSchemaListItem = Infer<
	typeof AssessmentSchemaListItemSchema
>;
