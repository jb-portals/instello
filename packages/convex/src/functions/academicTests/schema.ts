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
};
