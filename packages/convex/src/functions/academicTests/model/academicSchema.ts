import type { Infer } from "convex/values";
import type { Doc, Id } from "#_generated/dataModel";
import { ERROR_CODES, throwAppError } from "#helpers/constants";
import type { AppMutationCtx, AppQueryCtx } from "#model/common.types";
import type {
	AssessmentSchemaListItem,
	PatchAssessmentSchemaBody,
} from "../validator/assessmentSchema";
import * as AcademicComponent from "./academicComponent";
import * as AssessmentSitting from "./assessmentSitting";

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

async function assertNameAvailable(
	ctx: AppMutationCtx,
	args: {
		programSubjectId: Id<"programSubjects">;
		name: string;
		excludeId?: Id<"assessmentSchemas">;
	},
) {
	const normalized = args.name.trim().toLowerCase();
	const rows = await ctx.db
		.query("assessmentSchemas")
		.withIndex("by_programSubject", (q) =>
			q.eq("programSubjectId", args.programSubjectId),
		)
		.take(LIST_LIMIT);

	const conflict = rows.find(
		(row) =>
			row._id !== args.excludeId &&
			row.name.trim().toLowerCase() === normalized,
	);

	if (conflict) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.NAME_ALREADY_EXISTS);
	}
}

export async function create(
	ctx: AppMutationCtx,
	args: {
		programSubjectId: Id<"programSubjects">;
		name: string;
		description?: string;
	},
): Promise<Id<"assessmentSchemas">> {
	const name = args.name.trim();
	await assertNameAvailable(ctx, {
		programSubjectId: args.programSubjectId,
		name,
	});

	const now = Date.now();
	return await ctx.db.insert("assessmentSchemas", {
		programSubjectId: args.programSubjectId,
		name,
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
	const schema = await getById(ctx, id);
	if (!schema) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND);
	}

	const updates: {
		name?: string;
		description?: string;
		updatedAt: number;
	} = {
		updatedAt: Date.now(),
	};

	if (body.name !== undefined) {
		const name = body.name.trim();
		await assertNameAvailable(ctx, {
			programSubjectId: schema.programSubjectId,
			name,
			excludeId: id,
		});
		updates.name = name;
	}
	if (body.description !== undefined) {
		updates.description = body.description;
	}

	await ctx.db.patch("assessmentSchemas", id, updates);
}

/** Deletes sittings and components under the schema, then the schema itself. */
export async function remove(ctx: AppMutationCtx, id: Id<"assessmentSchemas">) {
	await AssessmentSitting.removeAllBySchema(ctx, id);
	await AcademicComponent.removeAllBySchema(ctx, id);
	await ctx.db.delete("assessmentSchemas", id);
}
