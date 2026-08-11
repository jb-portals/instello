import { defineTable } from "convex/server";
import { v } from "convex/values";

export const academicTestsTables = {
	assessmentSchemas: defineTable({
		programSubjectId: v.id("programSubjects"),
		name: v.string(),
		description: v.optional(v.string()),
		createdAt: v.number(),
		updatedAt: v.optional(v.number()),
	}).index("by_programSubject", ["programSubjectId"]),

	assessmentComponents: defineTable({
		name: v.string(),
		totalAllotedMarks: v.number(),
		passingMarks: v.number(),
		orderIdx: v.number(),
		assessmentSchemaId: v.id("assessmentSchemas"),
		createdAt: v.number(),
		updatedAt: v.optional(v.number()),
	}).index("by_assessmentSchema_orderIdx", {
		fields: ["assessmentSchemaId", "orderIdx"],
	}),

	/** Per-class sitting of an assessment schema on a calendar day */
	assessmentSittings: defineTable({
		assessmentSchemaId: v.id("assessmentSchemas"),
		classId: v.id("classes"),
		programSubjectId: v.id("programSubjects"),
		sessionDate: v.string(),
		sessionStartTime: v.string(),
		sessionEndTime: v.string(),
		questionPaperStorageId: v.id("_storage"),
		questionPaperFileName: v.optional(v.string()),
		status: v.union(v.literal("scheduled"), v.literal("conducted")),
		conductedAt: v.optional(v.number()),
		createdAt: v.number(),
		updatedAt: v.optional(v.number()),
	})
		.index("by_programSubject", ["programSubjectId"])
		.index("by_class_and_schema", ["classId", "assessmentSchemaId"])
		.index("by_assessmentSchema", ["assessmentSchemaId"]),

	/** One score per student per assessment component under a conducted sitting */
	assessmentMarks: defineTable({
		assessmentSittingId: v.id("assessmentSittings"),
		assessmentComponentId: v.id("assessmentComponents"),
		studentId: v.id("students"),
		marks: v.number(),
		createdAt: v.number(),
		updatedAt: v.optional(v.number()),
		createdBy: v.string(),
		updatedBy: v.optional(v.string()),
	})
		.index("by_sitting", ["assessmentSittingId"])
		.index("by_component", ["assessmentComponentId"])
		.index("by_sitting_component_student", [
			"assessmentSittingId",
			"assessmentComponentId",
			"studentId",
		]),

	/** Audit trail for mark create / update / delete actions */
	assessmentMarkActivityLogs: defineTable({
		assessmentSittingId: v.id("assessmentSittings"),
		action: v.union(
			v.literal("created"),
			v.literal("updated"),
			v.literal("deleted"),
		),
		performedBy: v.string(),
		performedAt: v.number(),
		changes: v.array(
			v.object({
				studentId: v.id("students"),
				assessmentComponentId: v.id("assessmentComponents"),
				previousMarks: v.optional(v.number()),
				newMarks: v.optional(v.number()),
			}),
		),
	}).index("by_sitting", ["assessmentSittingId"]),
};
