import type { Infer } from "convex/values";
import { vv } from "#schema";
import { AssessmentSittingListItemSchema } from "./assessmentSitting";

export const UpsertMarksEntrySchema = vv.object({
	assessmentComponentId: vv.id("assessmentComponents"),
	studentId: vv.id("students"),
	marks: vv.number(),
});

export const UpsertMarksInput = {
	assessmentSittingId: vv.id("assessmentSittings"),
	entries: vv.array(UpsertMarksEntrySchema),
};

export const MarksSheetComponentSchema = vv.object({
	_id: vv.id("assessmentComponents"),
	name: vv.string(),
	totalAllotedMarks: vv.number(),
	passingMarks: vv.number(),
	orderIdx: vv.number(),
});

export const MarksSheetStudentSchema = vv.object({
	_id: vv.id("students"),
	firstName: vv.string(),
	lastName: vv.string(),
	usn: vv.string(),
});

export const MarksSheetMarkSchema = vv.object({
	_id: vv.id("assessmentMarks"),
	assessmentComponentId: vv.id("assessmentComponents"),
	studentId: vv.id("students"),
	marks: vv.number(),
	createdAt: vv.number(),
	updatedAt: vv.optional(vv.number()),
});

export const MarksSheetSchema = vv.object({
	sitting: AssessmentSittingListItemSchema,
	components: vv.array(MarksSheetComponentSchema),
	students: vv.array(MarksSheetStudentSchema),
	marks: vv.array(MarksSheetMarkSchema),
	canDeleteMarks: vv.boolean(),
});

export const MarkActivityChangeSchema = vv.object({
	studentId: vv.id("students"),
	assessmentComponentId: vv.id("assessmentComponents"),
	previousMarks: vv.optional(vv.number()),
	newMarks: vv.optional(vv.number()),
});

export const MarkActivityLogDtoSchema = vv.object({
	_id: vv.id("assessmentMarkActivityLogs"),
	assessmentSittingId: vv.id("assessmentSittings"),
	action: vv.union(
		vv.literal("created"),
		vv.literal("updated"),
		vv.literal("deleted"),
	),
	performedBy: vv.object({
		_id: vv.string(),
		name: vv.string(),
		image: vv.optional(vv.string()),
	}),
	performedAt: vv.number(),
	changes: vv.array(MarkActivityChangeSchema),
});

export type UpsertMarksEntry = Infer<typeof UpsertMarksEntrySchema>;
export type MarksSheet = Infer<typeof MarksSheetSchema>;
export type MarkActivityLogDto = Infer<typeof MarkActivityLogDtoSchema>;
export type MarkActivityChange = Infer<typeof MarkActivityChangeSchema>;

export const MarksActivitySummarySchema = vv.object({
	actor: vv.object({
		_id: vv.string(),
		name: vv.string(),
		image: vv.optional(vv.string()),
	}),
	description: vv.string(),
	updatedAt: vv.number(),
});

export const ConductedSittingListItemSchema = vv.object({
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
	status: vv.literal("conducted"),
	conductedAt: vv.optional(vv.number()),
	createdAt: vv.number(),
	updatedAt: vv.optional(vv.number()),
	latestActivity: vv.optional(MarksActivitySummarySchema),
});

export type MarksActivitySummary = Infer<typeof MarksActivitySummarySchema>;
export type ConductedSittingListItem = Infer<
	typeof ConductedSittingListItemSchema
>;
