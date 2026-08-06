import type { Infer } from "convex/values";
import type { Doc, Id } from "#_generated/dataModel";
import type { AppMutationCtx, AppQueryCtx } from "#model/common.types";
import type {
	AssessmentSchemaListItem,
	PatchAssessmentSchemaBody,
} from "../validator/assessmentSchema";
import * as AcademicComponent from "./academicComponent";

const LIST_LIMIT = 100;

export function toListItem(
	schema: Doc<"assessmentSchemas">,
): AssessmentSchemaListItem {
	return {
		_id: schema._id,
		name: schema.name,
		description: schema.description,
		createdAt: schema.createdAt,
		updatedAt: schema.updatedAt,
	};
}

export async function getById(
	ctx: AppQueryCtx | AppMutationCtx,
	id: Id<"assessmentSchemas">,
) {
	return await ctx.db.get("assessmentSchemas", id);
}

export async function listByProgramSubject(
	ctx: AppQueryCtx,
	programSubjectId: Id<"programSubjects">,
): Promise<AssessmentSchemaListItem[]> {
	const rows = await ctx.db
		.query("assessmentSchemas")
		.withIndex("by_programSubject", (q) =>
			q.eq("programSubjectId", programSubjectId),
		)
		.take(LIST_LIMIT);

	return rows.map(toListItem);
}

export async function create(
	ctx: AppMutationCtx,
	args: {
		programSubjectId: Id<"programSubjects">;
		name: string;
		description?: string;
	},
): Promise<Id<"assessmentSchemas">> {
	const now = Date.now();
	return await ctx.db.insert("assessmentSchemas", {
		programSubjectId: args.programSubjectId,
		name: args.name.trim(),
		description: args.description,
		createdAt: now,
		updatedAt: now,
	});
}

export async function patch(
	ctx: AppMutationCtx,
	id: Id<"assessmentSchemas">,
	body: Infer<typeof PatchAssessmentSchemaBody>,
) {
	const updates: {
		name?: string;
		description?: string;
		updatedAt: number;
	} = {
		updatedAt: Date.now(),
	};

	if (body.name !== undefined) {
		updates.name = body.name.trim();
	}
	if (body.description !== undefined) {
		updates.description = body.description;
	}

	await ctx.db.patch("assessmentSchemas", id, updates);
}

/** Deletes all components under the schema, then the schema itself. */
export async function remove(ctx: AppMutationCtx, id: Id<"assessmentSchemas">) {
	await AcademicComponent.removeAllBySchema(ctx, id);
	await ctx.db.delete("assessmentSchemas", id);
}
