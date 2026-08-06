import type { Doc, Id } from "#_generated/dataModel";
import * as AcademicStage from "#academicPattern/model/academicStage";
import * as InstitutionAcademicPattern from "#institution/model/institutionAcademicPattern";
import type { AppTest } from "../types";

export type AdoptedEngineeringPattern = {
	pattern: Doc<"academicPatterns">;
	firstStage: Doc<"academicStages">;
	secondStage: Doc<"academicStages">;
};

export async function adoptEngineeringPattern(
	t: AppTest,
	args: {
		ownerOrganizationId: Id<"ownerOrganizations">;
		institutionId: string;
	},
): Promise<AdoptedEngineeringPattern> {
	return await t.run(async (ctx) => {
		const patterns = await ctx.db
			.query("academicPatterns")
			.withIndex("by_ownerOrganization", (q) =>
				q.eq("ownerOrganizationId", args.ownerOrganizationId),
			)
			.take(20);

		const pattern = patterns.find((p) => p.templateKey === "engineering");

		if (!pattern) {
			throw new Error("Engineering academic pattern not found for owner org");
		}

		await InstitutionAcademicPattern.adopt(ctx, {
			institutionId: args.institutionId,
			academicPatternId: pattern._id,
			ownerOrganizationId: args.ownerOrganizationId,
		});

		const stages = await AcademicStage.listByPattern(ctx, pattern._id);
		const firstStage = stages[0];
		const secondStage = stages[1];

		if (!firstStage || !secondStage) {
			throw new Error("Expected at least two stages on engineering pattern");
		}

		return { pattern, firstStage, secondStage };
	});
}
