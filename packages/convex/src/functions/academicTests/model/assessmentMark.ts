import type { Doc, Id } from "#_generated/dataModel";
import { ERROR_CODES, throwAppError } from "#helpers/constants";
import type { AppMutationCtx, AppQueryCtx } from "#model/common.types";
import type { MarksSheet, UpsertMarksEntry } from "../validator/assessmentMark";
import * as AcademicComponent from "./academicComponent";
import * as AssessmentMarkActivityLog from "./assessmentMarkActivityLog";
import * as AssessmentSitting from "./assessmentSitting";

const LIST_LIMIT = 300;

export async function getById(
	ctx: AppQueryCtx | AppMutationCtx,
	id: Id<"assessmentMarks">,
) {
	return await ctx.db.get("assessmentMarks", id);
}

export async function findBySittingComponentStudent(
	ctx: AppQueryCtx | AppMutationCtx,
	args: {
		assessmentSittingId: Id<"assessmentSittings">;
		assessmentComponentId: Id<"assessmentComponents">;
		studentId: Id<"students">;
	},
) {
	return await ctx.db
		.query("assessmentMarks")
		.withIndex("by_sitting_component_student", (q) =>
			q
				.eq("assessmentSittingId", args.assessmentSittingId)
				.eq("assessmentComponentId", args.assessmentComponentId)
				.eq("studentId", args.studentId),
		)
		.unique();
}

export async function listBySitting(
	ctx: AppQueryCtx | AppMutationCtx,
	assessmentSittingId: Id<"assessmentSittings">,
): Promise<Doc<"assessmentMarks">[]> {
	return await ctx.db
		.query("assessmentMarks")
		.withIndex("by_sitting", (q) =>
			q.eq("assessmentSittingId", assessmentSittingId),
		)
		.take(LIST_LIMIT);
}

export async function listByComponent(
	ctx: AppQueryCtx | AppMutationCtx,
	assessmentComponentId: Id<"assessmentComponents">,
): Promise<Doc<"assessmentMarks">[]> {
	return await ctx.db
		.query("assessmentMarks")
		.withIndex("by_component", (q) =>
			q.eq("assessmentComponentId", assessmentComponentId),
		)
		.take(LIST_LIMIT);
}

async function listStudentsForClass(
	ctx: AppQueryCtx | AppMutationCtx,
	classId: Id<"classes">,
) {
	return await ctx.db
		.query("students")
		.withIndex("by_class", (q) => q.eq("classId", classId))
		.order("desc")
		.take(LIST_LIMIT);
}

export async function getMarksSheet(
	ctx: AppQueryCtx,
	args: {
		sitting: Doc<"assessmentSittings">;
		canDeleteMarks: boolean;
	},
): Promise<MarksSheet> {
	const sittingItem = await AssessmentSitting.toListItem(ctx, args.sitting);
	const components = await AcademicComponent.listBySchema(
		ctx,
		args.sitting.assessmentSchemaId,
	);
	const students = await listStudentsForClass(ctx, args.sitting.classId);
	const marks = await listBySitting(ctx, args.sitting._id);

	return {
		sitting: sittingItem,
		components: components.map((component) => ({
			_id: component._id,
			name: component.name,
			totalAllotedMarks: component.totalAllotedMarks,
			passingMarks: component.passingMarks,
			orderIdx: component.orderIdx,
		})),
		students: students.map((student) => ({
			_id: student._id,
			firstName: student.firstName,
			lastName: student.lastName,
			usn: student.usn,
		})),
		marks: marks.map((mark) => ({
			_id: mark._id,
			assessmentComponentId: mark.assessmentComponentId,
			studentId: mark.studentId,
			marks: mark.marks,
			createdAt: mark.createdAt,
			updatedAt: mark.updatedAt,
		})),
		canDeleteMarks: args.canDeleteMarks,
	};
}

function assertSittingConducted(sitting: Doc<"assessmentSittings">) {
	if (sitting.status !== "conducted") {
		throwAppError(ERROR_CODES.ASSESSMENT_MARK.SITTING_NOT_CONDUCTED);
	}
}

function assertMarksInRange(marks: number, totalAllotedMarks: number) {
	if (!Number.isFinite(marks) || marks < 0 || marks > totalAllotedMarks) {
		throwAppError(ERROR_CODES.ASSESSMENT_MARK.INVALID_MARKS);
	}
}

export async function upsertMarks(
	ctx: AppMutationCtx,
	args: {
		sitting: Doc<"assessmentSittings">;
		entries: UpsertMarksEntry[];
		performedBy: string;
	},
) {
	assertSittingConducted(args.sitting);

	if (args.entries.length === 0) {
		return;
	}

	const components = await AcademicComponent.listOrderedBySchema(
		ctx,
		args.sitting.assessmentSchemaId,
	);
	const componentById = new Map(
		components.map((component) => [component._id, component]),
	);

	const students = await listStudentsForClass(ctx, args.sitting.classId);
	const studentIds = new Set(students.map((student) => student._id));

	const now = Date.now();
	const createdChanges: Array<{
		studentId: Id<"students">;
		assessmentComponentId: Id<"assessmentComponents">;
		newMarks: number;
	}> = [];
	const updatedChanges: Array<{
		studentId: Id<"students">;
		assessmentComponentId: Id<"assessmentComponents">;
		previousMarks: number;
		newMarks: number;
	}> = [];

	for (const entry of args.entries) {
		const component = componentById.get(entry.assessmentComponentId);
		if (!component) {
			throwAppError(ERROR_CODES.ASSESSMENT_MARK.INVALID_COMPONENT);
		}

		if (!studentIds.has(entry.studentId)) {
			throwAppError(ERROR_CODES.ASSESSMENT_MARK.INVALID_STUDENT);
		}

		assertMarksInRange(entry.marks, component.totalAllotedMarks);

		const existing = await findBySittingComponentStudent(ctx, {
			assessmentSittingId: args.sitting._id,
			assessmentComponentId: entry.assessmentComponentId,
			studentId: entry.studentId,
		});

		if (!existing) {
			await ctx.db.insert("assessmentMarks", {
				assessmentSittingId: args.sitting._id,
				assessmentComponentId: entry.assessmentComponentId,
				studentId: entry.studentId,
				marks: entry.marks,
				createdAt: now,
				updatedAt: now,
				createdBy: args.performedBy,
			});
			createdChanges.push({
				studentId: entry.studentId,
				assessmentComponentId: entry.assessmentComponentId,
				newMarks: entry.marks,
			});
			continue;
		}

		if (existing.marks === entry.marks) {
			continue;
		}

		await ctx.db.patch("assessmentMarks", existing._id, {
			marks: entry.marks,
			updatedAt: now,
			updatedBy: args.performedBy,
		});
		updatedChanges.push({
			studentId: entry.studentId,
			assessmentComponentId: entry.assessmentComponentId,
			previousMarks: existing.marks,
			newMarks: entry.marks,
		});
	}

	await AssessmentMarkActivityLog.appendLog(ctx, {
		assessmentSittingId: args.sitting._id,
		action: "created",
		performedBy: args.performedBy,
		performedAt: now,
		changes: createdChanges,
	});

	await AssessmentMarkActivityLog.appendLog(ctx, {
		assessmentSittingId: args.sitting._id,
		action: "updated",
		performedBy: args.performedBy,
		performedAt: now,
		changes: updatedChanges,
	});
}

export async function remove(
	ctx: AppMutationCtx,
	args: {
		markId: Id<"assessmentMarks">;
		performedBy: string;
	},
) {
	const mark = await getById(ctx, args.markId);
	if (!mark) {
		throwAppError(ERROR_CODES.ASSESSMENT_MARK.NOT_FOUND);
	}

	const sitting = await AssessmentSitting.getById(
		ctx,
		mark.assessmentSittingId,
	);
	if (!sitting) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.NOT_FOUND);
	}

	assertSittingConducted(sitting);

	const now = Date.now();
	await ctx.db.delete("assessmentMarks", mark._id);

	await AssessmentMarkActivityLog.appendLog(ctx, {
		assessmentSittingId: mark.assessmentSittingId,
		action: "deleted",
		performedBy: args.performedBy,
		performedAt: now,
		changes: [
			{
				studentId: mark.studentId,
				assessmentComponentId: mark.assessmentComponentId,
				previousMarks: mark.marks,
			},
		],
	});
}

/** Cascade helper — deletes all marks for a sitting. */
export async function removeAllBySitting(
	ctx: AppMutationCtx,
	assessmentSittingId: Id<"assessmentSittings">,
) {
	const marks = await listBySitting(ctx, assessmentSittingId);
	for (const mark of marks) {
		await ctx.db.delete("assessmentMarks", mark._id);
	}
}

/** Cascade helper — deletes all marks for a component. */
export async function removeAllByComponent(
	ctx: AppMutationCtx,
	assessmentComponentId: Id<"assessmentComponents">,
) {
	const marks = await listByComponent(ctx, assessmentComponentId);
	for (const mark of marks) {
		await ctx.db.delete("assessmentMarks", mark._id);
	}
}
