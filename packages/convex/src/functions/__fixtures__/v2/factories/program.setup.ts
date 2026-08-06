import type { Doc, Id } from "#_generated/dataModel";
import type { AppTest } from "../types.setup";

export type CreatedProgram = Doc<"programs">;

let programSeq = 0;

export async function createProgram(
	t: AppTest,
	args: {
		institutionId: string;
		createdBy: string;
		name?: string;
		alias?: string;
	},
): Promise<CreatedProgram> {
	programSeq += 1;
	const name = args.name ?? `Program ${programSeq}`;
	const alias = args.alias ?? `P${programSeq}`;

	const programId = await t.run(async (ctx) => {
		const now = Date.now();
		return await ctx.db.insert("programs", {
			name,
			alias,
			createdBy: args.createdBy,
			institutionId: args.institutionId,
			status: "active",
			createdAt: now,
			updatedAt: now,
		});
	});

	const program = await t.run(async (ctx) => {
		return await ctx.db.get("programs", programId as Id<"programs">);
	});

	if (!program) {
		throw new Error("Failed to create program");
	}

	return program;
}
