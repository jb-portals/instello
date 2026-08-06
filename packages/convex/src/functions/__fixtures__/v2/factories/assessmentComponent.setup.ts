import type { Doc, Id } from "#_generated/dataModel";
import type { AppTest } from "../types.setup";

export type CreatedAssessmentComponent = Doc<"assessmentComponents">;

let componentSeq = 0;

export async function createAssessmentComponent(
	t: AppTest,
	args: {
		assessmentSchemaId: Id<"assessmentSchemas">;
		name?: string;
		totalAllotedMarks?: number;
		passingMarks?: number;
		orderIdx?: number;
	},
): Promise<CreatedAssessmentComponent> {
	componentSeq += 1;
	const name = args.name ?? `Component ${componentSeq}`;
	const totalAllotedMarks = args.totalAllotedMarks ?? 20;
	const passingMarks = args.passingMarks ?? 8;
	const orderIdx = args.orderIdx ?? componentSeq - 1;

	const id = await t.run(async (ctx) => {
		const now = Date.now();
		return await ctx.db.insert("assessmentComponents", {
			assessmentSchemaId: args.assessmentSchemaId,
			name,
			totalAllotedMarks,
			passingMarks,
			orderIdx,
			createdAt: now,
			updatedAt: now,
		});
	});

	const component = await t.run(async (ctx) => {
		return await ctx.db.get(
			"assessmentComponents",
			id as Id<"assessmentComponents">,
		);
	});

	if (!component) {
		throw new Error("Failed to create assessment component");
	}

	return component;
}
