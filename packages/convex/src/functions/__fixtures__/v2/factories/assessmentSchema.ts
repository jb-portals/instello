import type { Doc, Id } from "#_generated/dataModel";
import type { AppTest } from "../types";

export type CreatedAssessmentSchema = Doc<"assessmentSchemas">;

let schemaSeq = 0;

export async function createAssessmentSchema(
	t: AppTest,
	args: {
		programSubjectId: Id<"programSubjects">;
		name?: string;
		description?: string;
	},
): Promise<CreatedAssessmentSchema> {
	schemaSeq += 1;
	const name = args.name ?? `CIE Schema ${schemaSeq}`;

	const id = await t.run(async (ctx) => {
		const now = Date.now();
		return await ctx.db.insert("assessmentSchemas", {
			programSubjectId: args.programSubjectId,
			name,
			description: args.description,
			createdAt: now,
			updatedAt: now,
		});
	});

	const schema = await t.run(async (ctx) => {
		return await ctx.db.get("assessmentSchemas", id as Id<"assessmentSchemas">);
	});

	if (!schema) {
		throw new Error("Failed to create assessment schema");
	}

	return schema;
}
