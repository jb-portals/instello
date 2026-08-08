import type { Doc, Id } from "#_generated/dataModel";
import type { AppTest } from "../types.setup";

export type CreatedClass = Doc<"classes">;

let classSeq = 0;

export async function createClass(
	t: AppTest,
	args: {
		programId: Id<"programs">;
		currentHeadStageId: Id<"academicStages">;
		name?: string;
		slug?: string;
		status?: "active" | "inactive";
	},
): Promise<CreatedClass> {
	classSeq += 1;
	const name = args.name ?? `Class ${classSeq}`;
	const slug = args.slug ?? `class-${classSeq}`;

	const id = await t.run(async (ctx) => {
		const now = Date.now();
		return await ctx.db.insert("classes", {
			programId: args.programId,
			name,
			slug,
			currentHeadStageId: args.currentHeadStageId,
			status: args.status ?? "active",
			isGroupsEnabled: false,
			createdAt: now,
			updatedAt: now,
		});
	});

	const cls = await t.run(async (ctx) => {
		return await ctx.db.get("classes", id as Id<"classes">);
	});

	if (!cls) {
		throw new Error("Failed to create class");
	}

	return cls;
}

/** Stores a tiny PDF blob and returns its storage id for sitting tests. */
export async function storeQuestionPaperPdf(
	t: AppTest,
): Promise<Id<"_storage">> {
	return await t.run(async (ctx) => {
		return await ctx.storage.store(
			new Blob(["%PDF-1.4 test question paper"], {
				type: "application/pdf",
			}),
		);
	});
}
