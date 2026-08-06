import type { Doc, Id } from "#_generated/dataModel";
import type { AppTest } from "../types";

export type CreatedSubject = Doc<"subjects">;

let subjectSeq = 0;

export async function createSubject(
	t: AppTest,
	args: {
		institutionId: string;
		name?: string;
		code?: string;
		alias?: string;
		color?: string;
	},
): Promise<CreatedSubject> {
	subjectSeq += 1;
	const name = args.name ?? `Subject ${subjectSeq}`;
	const code = args.code ?? `SUB${String(subjectSeq).padStart(2, "0")}T`;
	const alias = args.alias ?? `subject-${subjectSeq}`;
	const color = args.color ?? "#3B82F6";

	const subjectId = await t.run(async (ctx) => {
		const now = Date.now();
		return await ctx.db.insert("subjects", {
			name,
			code,
			alias,
			color,
			institutionId: args.institutionId,
			status: "active",
			createdAt: now,
			updatedAt: now,
		});
	});

	const subject = await t.run(async (ctx) => {
		return await ctx.db.get("subjects", subjectId as Id<"subjects">);
	});

	if (!subject) {
		throw new Error("Failed to create subject");
	}

	return subject;
}
