import type { Infer } from "convex/values";
import type { Doc, Id } from "#_generated/dataModel";
import { ERROR_CODES, throwAppError } from "#helpers/constants";
import type { AppMutationCtx, AppQueryCtx } from "#model/common.types";
import type {
	AssessmentComponentListItem,
	PatchAssessmentComponentBody,
} from "../validator/assessmentComponent";
import * as AcademicSchema from "./academicSchema";

const LIST_LIMIT = 100;

export function toListItem(
	component: Doc<"assessmentComponents">,
): AssessmentComponentListItem {
	return {
		_id: component._id,
		name: component.name,
		totalAllotedMarks: component.totalAllotedMarks,
		passingMarks: component.passingMarks,
		orderIdx: component.orderIdx,
		createdAt: component.createdAt,
		updatedAt: component.updatedAt,
	};
}

export async function getById(
	ctx: AppQueryCtx | AppMutationCtx,
	id: Id<"assessmentComponents">,
) {
	return await ctx.db.get("assessmentComponents", id);
}

export async function listOrderedBySchema(
	ctx: AppQueryCtx | AppMutationCtx,
	assessmentSchemaId: Id<"assessmentSchemas">,
): Promise<Doc<"assessmentComponents">[]> {
	return await ctx.db
		.query("assessmentComponents")
		.withIndex("by_assessmentSchema_orderIdx", (q) =>
			q.eq("assessmentSchemaId", assessmentSchemaId),
		)
		.take(LIST_LIMIT);
}

export async function listBySchema(
	ctx: AppQueryCtx,
	assessmentSchemaId: Id<"assessmentSchemas">,
): Promise<AssessmentComponentListItem[]> {
	const rows = await listOrderedBySchema(ctx, assessmentSchemaId);
	return rows.map(toListItem);
}

function assertValidMarks(totalAllotedMarks: number, passingMarks: number) {
	if (passingMarks > totalAllotedMarks) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.INVALID_MARKS);
	}
}

export async function listByProgramSubject(
	ctx: AppQueryCtx,
	programSubjectId: Id<"programSubjects">,
): Promise<
	Array<
		AssessmentComponentListItem & {
			assessmentSchemaId: Id<"assessmentSchemas">;
			assessmentSchemaName: string;
		}
	>
> {
	const schemas = await AcademicSchema.listByProgramSubject(
		ctx,
		programSubjectId,
	);
	const result: Array<
		AssessmentComponentListItem & {
			assessmentSchemaId: Id<"assessmentSchemas">;
			assessmentSchemaName: string;
		}
	> = [];

	for (const schema of schemas) {
		const components = await listBySchema(ctx, schema._id);
		for (const component of components) {
			result.push({
				...component,
				assessmentSchemaId: schema._id,
				assessmentSchemaName: schema.name,
			});
		}
	}

	return result;
}

export async function create(
	ctx: AppMutationCtx,
	args: {
		assessmentSchemaId: Id<"assessmentSchemas">;
		name: string;
		totalAllotedMarks: number;
		passingMarks: number;
	},
): Promise<Id<"assessmentComponents">> {
	assertValidMarks(args.totalAllotedMarks, args.passingMarks);

	const existing = await listOrderedBySchema(ctx, args.assessmentSchemaId);
	const now = Date.now();

	return await ctx.db.insert("assessmentComponents", {
		assessmentSchemaId: args.assessmentSchemaId,
		name: args.name.trim(),
		totalAllotedMarks: args.totalAllotedMarks,
		passingMarks: args.passingMarks,
		orderIdx: existing.length,
		createdAt: now,
		updatedAt: now,
	});
}

export async function patch(
	ctx: AppMutationCtx,
	id: Id<"assessmentComponents">,
	body: Infer<typeof PatchAssessmentComponentBody>,
) {
	const current = await getById(ctx, id);
	if (!current) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	const totalAllotedMarks = body.totalAllotedMarks ?? current.totalAllotedMarks;
	const passingMarks = body.passingMarks ?? current.passingMarks;
	assertValidMarks(totalAllotedMarks, passingMarks);

	const updates: {
		name?: string;
		totalAllotedMarks?: number;
		passingMarks?: number;
		updatedAt: number;
	} = {
		updatedAt: Date.now(),
	};

	if (body.name !== undefined) {
		updates.name = body.name.trim();
	}
	if (body.totalAllotedMarks !== undefined) {
		updates.totalAllotedMarks = body.totalAllotedMarks;
	}
	if (body.passingMarks !== undefined) {
		updates.passingMarks = body.passingMarks;
	}

	await ctx.db.patch("assessmentComponents", id, updates);
}

async function reindex(
	ctx: AppMutationCtx,
	components: Doc<"assessmentComponents">[],
) {
	const now = Date.now();
	for (let i = 0; i < components.length; i++) {
		const component = components[i];
		if (!component) continue;
		if (component.orderIdx === i) continue;
		await ctx.db.patch("assessmentComponents", component._id, {
			orderIdx: i,
			updatedAt: now,
		});
	}
}

/** Move a component to `orderIdx` and shift siblings so order stays contiguous. */
export async function repos(
	ctx: AppMutationCtx,
	id: Id<"assessmentComponents">,
	orderIdx: number,
) {
	const component = await getById(ctx, id);
	if (!component) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	const siblings = await listOrderedBySchema(ctx, component.assessmentSchemaId);

	if (orderIdx < 0 || orderIdx >= siblings.length) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.INVALID_ORDER);
	}

	const fromIdx = siblings.findIndex((row) => row._id === id);
	if (fromIdx < 0) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	if (fromIdx === orderIdx) {
		return;
	}

	const [moved] = siblings.splice(fromIdx, 1);
	if (!moved) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}
	siblings.splice(orderIdx, 0, moved);

	await reindex(ctx, siblings);
}

export async function remove(
	ctx: AppMutationCtx,
	id: Id<"assessmentComponents">,
) {
	const component = await getById(ctx, id);
	if (!component) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	const schemaId = component.assessmentSchemaId;
	await ctx.db.delete("assessmentComponents", id);

	const remaining = await listOrderedBySchema(ctx, schemaId);
	await reindex(ctx, remaining);
}

export async function removeAllBySchema(
	ctx: AppMutationCtx,
	assessmentSchemaId: Id<"assessmentSchemas">,
) {
	const components = await listOrderedBySchema(ctx, assessmentSchemaId);
	for (const component of components) {
		await ctx.db.delete("assessmentComponents", component._id);
	}
}
