import type { Doc, Id } from "#_generated/dataModel";
import type { AppTest } from "../types.setup";

export type CreatedProgramSubject = Doc<"programSubjects">;

export async function createProgramSubject(
	t: AppTest,
	args: {
		programId: Id<"programs">;
		subjectId: Id<"subjects">;
		academicStageId: Id<"academicStages">;
		type?: "theory" | "practical";
	},
): Promise<CreatedProgramSubject> {
	const programSubjectId = await t.run(async (ctx) => {
		const now = Date.now();
		return await ctx.db.insert("programSubjects", {
			programId: args.programId,
			subjectId: args.subjectId,
			academicStageId: args.academicStageId,
			type: args.type ?? "theory",
			createdAt: now,
			updatedAt: now,
		});
	});

	const programSubject = await t.run(async (ctx) => {
		return await ctx.db.get(
			"programSubjects",
			programSubjectId as Id<"programSubjects">,
		);
	});

	if (!programSubject) {
		throw new Error("Failed to create program subject");
	}

	return programSubject;
}
