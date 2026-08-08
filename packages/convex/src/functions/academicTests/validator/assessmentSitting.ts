import type { Infer } from "convex/values";
import { v } from "convex/values";
import { vv } from "#schema";

export const AssessmentSittingStatusSchema = vv.union(
	vv.literal("scheduled"),
	vv.literal("conducted"),
);

export const ScheduleAssessmentSittingInput = {
	assessmentSchemaId: vv.id("assessmentSchemas"),
	classId: vv.id("classes"),
	sessionDate: vv.string(),
	sessionStartTime: vv.string(),
	sessionEndTime: vv.string(),
	questionPaperStorageId: v.id("_storage"),
	questionPaperFileName: vv.optional(vv.string()),
};

export const PatchAssessmentSittingBody = vv.object({
	sessionDate: vv.optional(vv.string()),
	sessionStartTime: vv.optional(vv.string()),
	sessionEndTime: vv.optional(vv.string()),
	questionPaperStorageId: v.optional(v.id("_storage")),
	questionPaperFileName: vv.optional(vv.union(vv.string(), vv.null())),
});

export const AssessmentSittingListItemSchema = vv.object({
	_id: vv.id("assessmentSittings"),
	assessmentSchemaId: vv.id("assessmentSchemas"),
	assessmentSchemaName: vv.string(),
	classId: vv.id("classes"),
	className: vv.string(),
	programSubjectId: vv.id("programSubjects"),
	sessionDate: vv.string(),
	sessionStartTime: vv.string(),
	sessionEndTime: vv.string(),
	questionPaperUrl: vv.union(vv.string(), vv.null()),
	questionPaperFileName: vv.optional(vv.string()),
	status: AssessmentSittingStatusSchema,
	conductedAt: vv.optional(vv.number()),
	createdAt: vv.number(),
	updatedAt: vv.optional(vv.number()),
});

export const EligibleClassForSittingSchema = vv.object({
	_id: vv.id("classes"),
	name: vv.string(),
	slug: vv.string(),
});

export type AssessmentSittingListItem = Infer<
	typeof AssessmentSittingListItemSchema
>;

export type EligibleClassForSitting = Infer<
	typeof EligibleClassForSittingSchema
>;
