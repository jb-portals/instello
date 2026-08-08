import type { Infer } from "convex/values";
import { vv } from "#schema";

export const CreateAssessmentComponentInput = {
	assessmentSchemaId: vv.id("assessmentSchemas"),
	name: vv.string(),
	totalAllotedMarks: vv.number(),
	passingMarks: vv.number(),
};

export const PatchAssessmentComponentBody = vv.object({
	name: vv.optional(vv.string()),
	totalAllotedMarks: vv.optional(vv.number()),
	passingMarks: vv.optional(vv.number()),
});

export const AssessmentComponentListItemSchema = vv.object({
	_id: vv.id("assessmentComponents"),
	name: vv.string(),
	totalAllotedMarks: vv.number(),
	passingMarks: vv.number(),
	orderIdx: vv.number(),
	createdAt: vv.number(),
	updatedAt: vv.optional(vv.number()),
});

export const AssessmentComponentWithSchemaListItemSchema = vv.object({
	_id: vv.id("assessmentComponents"),
	name: vv.string(),
	totalAllotedMarks: vv.number(),
	passingMarks: vv.number(),
	orderIdx: vv.number(),
	createdAt: vv.number(),
	updatedAt: vv.optional(vv.number()),
	assessmentSchemaId: vv.id("assessmentSchemas"),
	assessmentSchemaName: vv.string(),
});

export type AssessmentComponentListItem = Infer<
	typeof AssessmentComponentListItemSchema
>;
