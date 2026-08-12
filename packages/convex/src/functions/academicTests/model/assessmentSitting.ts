import type { Infer } from "convex/values";
import type { Doc, Id } from "#_generated/dataModel";
import * as Class from "#class/model/class";
import { ERROR_CODES, throwAppError } from "#helpers/constants";
import type { AppMutationCtx, AppQueryCtx } from "#model/common.types";
import * as ProgramSubject from "#program/model/programSubject";
import type { ConductedSittingListItem } from "../validator/assessmentMark";
import type {
	AssessmentSittingListItem,
	EligibleClassForSitting,
	PatchAssessmentSittingBody,
} from "../validator/assessmentSitting";
import * as AcademicSchema from "./academicSchema";
import * as AssessmentMark from "./assessmentMark";
import * as AssessmentMarkActivityLog from "./assessmentMarkActivityLog";

const LIST_LIMIT = 200;
const SESSION_DATE_RE = /^\d{4}-\d{2}-\d{2}$/;
const SESSION_TIME_RE = /^([01]\d|2[0-3]):([0-5]\d)$/;

export function assertValidSessionDate(sessionDate: string): string {
	const trimmed = sessionDate.trim();
	if (!SESSION_DATE_RE.test(trimmed)) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_DATE);
	}

	const [year, month, day] = trimmed.split("-").map(Number);
	if (year === undefined || month === undefined || day === undefined) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_DATE);
	}

	const parsed = new Date(Date.UTC(year, month - 1, day));
	if (
		parsed.getUTCFullYear() !== year ||
		parsed.getUTCMonth() !== month - 1 ||
		parsed.getUTCDate() !== day
	) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_DATE);
	}

	return trimmed;
}

export function assertValidSessionTime(sessionTime: string): string {
	const trimmed = sessionTime.trim();
	if (!SESSION_TIME_RE.test(trimmed)) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_TIME);
	}
	return trimmed;
}

export function assertValidSessionWindow(
	sessionStartTime: string,
	sessionEndTime: string,
): { sessionStartTime: string; sessionEndTime: string } {
	const start = assertValidSessionTime(sessionStartTime);
	const end = assertValidSessionTime(sessionEndTime);
	if (end <= start) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_TIME);
	}
	return { sessionStartTime: start, sessionEndTime: end };
}

export async function getById(
	ctx: AppQueryCtx | AppMutationCtx,
	id: Id<"assessmentSittings">,
) {
	return await ctx.db.get("assessmentSittings", id);
}

export async function findByClassAndSchema(
	ctx: AppQueryCtx | AppMutationCtx,
	args: {
		classId: Id<"classes">;
		assessmentSchemaId: Id<"assessmentSchemas">;
	},
) {
	return await ctx.db
		.query("assessmentSittings")
		.withIndex("by_class_and_schema", (q) =>
			q
				.eq("classId", args.classId)
				.eq("assessmentSchemaId", args.assessmentSchemaId),
		)
		.unique();
}

export async function listByAssessmentSchema(
	ctx: AppQueryCtx | AppMutationCtx,
	assessmentSchemaId: Id<"assessmentSchemas">,
): Promise<Doc<"assessmentSittings">[]> {
	return await ctx.db
		.query("assessmentSittings")
		.withIndex("by_assessmentSchema", (q) =>
			q.eq("assessmentSchemaId", assessmentSchemaId),
		)
		.take(LIST_LIMIT);
}

/**
 * Active, live classes in the program whose head stage matches the allocation.
 */
export async function listEligibleClasses(
	ctx: AppQueryCtx,
	programSubjectId: Id<"programSubjects">,
): Promise<EligibleClassForSitting[]> {
	const programSubject = await ProgramSubject.getById(ctx, programSubjectId);
	if (!programSubject) {
		return [];
	}

	const classes = await ctx.db
		.query("classes")
		.withIndex("by_program", (q) => q.eq("programId", programSubject.programId))
		.take(LIST_LIMIT);

	return classes
		.filter(
			(cls) =>
				Class.isLive(cls) &&
				cls.status === "active" &&
				cls.currentHeadStageId === programSubject.academicStageId,
		)
		.map((cls) => ({
			_id: cls._id,
			name: cls.name,
			slug: cls.slug,
		}))
		.sort((a, b) => a.name.localeCompare(b.name));
}

export async function assertClassEligibleForProgramSubject(
	ctx: AppQueryCtx | AppMutationCtx,
	args: {
		classId: Id<"classes">;
		programSubjectId: Id<"programSubjects">;
	},
): Promise<Doc<"classes">> {
	const programSubject = await ProgramSubject.getById(
		ctx,
		args.programSubjectId,
	);
	if (!programSubject) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.INVALID_CLASS);
	}

	const cls = await ctx.db.get("classes", args.classId);
	if (
		!cls ||
		!Class.isLive(cls) ||
		cls.status !== "active" ||
		cls.programId !== programSubject.programId ||
		cls.currentHeadStageId !== programSubject.academicStageId
	) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.INVALID_CLASS);
	}

	return cls;
}

export async function toListItem(
	ctx: AppQueryCtx | AppMutationCtx,
	sitting: Doc<"assessmentSittings">,
): Promise<AssessmentSittingListItem> {
	const [schema, cls] = await Promise.all([
		AcademicSchema.getById(ctx, sitting.assessmentSchemaId),
		ctx.db.get("classes", sitting.classId),
	]);

	if (!schema) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND);
	}

	const questionPaperUrl = await ctx.storage.getUrl(
		sitting.questionPaperStorageId,
	);

	return {
		_id: sitting._id,
		assessmentSchemaId: sitting.assessmentSchemaId,
		assessmentSchemaName: schema.name,
		classId: sitting.classId,
		className: cls?.name ?? "Unknown",
		programSubjectId: sitting.programSubjectId,
		sessionDate: sitting.sessionDate,
		sessionStartTime: sitting.sessionStartTime,
		sessionEndTime: sitting.sessionEndTime,
		questionPaperUrl,
		questionPaperFileName: sitting.questionPaperFileName,
		status: sitting.status,
		conductedAt: sitting.conductedAt,
		createdAt: sitting.createdAt,
		updatedAt: sitting.updatedAt,
	};
}

export async function listByProgramSubject(
	ctx: AppQueryCtx,
	programSubjectId: Id<"programSubjects">,
): Promise<AssessmentSittingListItem[]> {
	const rows = await ctx.db
		.query("assessmentSittings")
		.withIndex("by_programSubject", (q) =>
			q.eq("programSubjectId", programSubjectId),
		)
		.take(LIST_LIMIT);

	const items = await Promise.all(rows.map((row) => toListItem(ctx, row)));
	return items.sort((a, b) => {
		const dateCmp = a.sessionDate.localeCompare(b.sessionDate);
		if (dateCmp !== 0) return dateCmp;
		const timeCmp = a.sessionStartTime.localeCompare(b.sessionStartTime);
		if (timeCmp !== 0) return timeCmp;
		return a.className.localeCompare(b.className);
	});
}

/** Conducted sittings only — used for marks entry surfaces. */
export async function listConductedByProgramSubject(
	ctx: AppQueryCtx,
	programSubjectId: Id<"programSubjects">,
): Promise<ConductedSittingListItem[]> {
	const items = await listByProgramSubject(ctx, programSubjectId);
	const conducted = items.filter((item) => item.status === "conducted");

	return await Promise.all(
		conducted.map(async (item) => {
			const latestActivity =
				await AssessmentMarkActivityLog.getLatestActivitySummary(ctx, item._id);
			return {
				...item,
				status: "conducted" as const,
				...(latestActivity ? { latestActivity } : {}),
			};
		}),
	);
}

/** Conducted sittings for a class + program-subject assignment. */
export async function listConductedByClassAndProgramSubject(
	ctx: AppQueryCtx,
	args: {
		classId: Id<"classes">;
		programSubjectId: Id<"programSubjects">;
	},
): Promise<ConductedSittingListItem[]> {
	const items = await listConductedByProgramSubject(ctx, args.programSubjectId);
	return items.filter((item) => item.classId === args.classId);
}

export async function create(
	ctx: AppMutationCtx,
	args: {
		assessmentSchemaId: Id<"assessmentSchemas">;
		classId: Id<"classes">;
		programSubjectId: Id<"programSubjects">;
		sessionDate: string;
		sessionStartTime: string;
		sessionEndTime: string;
		questionPaperStorageId: Id<"_storage">;
		questionPaperFileName?: string;
	},
): Promise<Id<"assessmentSittings">> {
	const sessionDate = assertValidSessionDate(args.sessionDate);
	const { sessionStartTime, sessionEndTime } = assertValidSessionWindow(
		args.sessionStartTime,
		args.sessionEndTime,
	);

	await assertClassEligibleForProgramSubject(ctx, {
		classId: args.classId,
		programSubjectId: args.programSubjectId,
	});

	const existing = await findByClassAndSchema(ctx, {
		classId: args.classId,
		assessmentSchemaId: args.assessmentSchemaId,
	});
	if (existing) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.ALREADY_EXISTS);
	}

	const now = Date.now();
	return await ctx.db.insert("assessmentSittings", {
		assessmentSchemaId: args.assessmentSchemaId,
		classId: args.classId,
		programSubjectId: args.programSubjectId,
		sessionDate,
		sessionStartTime,
		sessionEndTime,
		questionPaperStorageId: args.questionPaperStorageId,
		questionPaperFileName: args.questionPaperFileName?.trim() || undefined,
		status: "scheduled",
		createdAt: now,
		updatedAt: now,
	});
}

export async function patch(
	ctx: AppMutationCtx,
	id: Id<"assessmentSittings">,
	body: Infer<typeof PatchAssessmentSittingBody>,
) {
	const sitting = await getById(ctx, id);
	if (!sitting) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.NOT_FOUND);
	}
	if (sitting.status !== "scheduled") {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.NOT_SCHEDULED);
	}

	const updates: {
		sessionDate?: string;
		sessionStartTime?: string;
		sessionEndTime?: string;
		questionPaperStorageId?: Id<"_storage">;
		questionPaperFileName?: string;
		updatedAt: number;
	} = {
		updatedAt: Date.now(),
	};

	if (body.sessionDate !== undefined) {
		updates.sessionDate = assertValidSessionDate(body.sessionDate);
	}

	const nextStart = body.sessionStartTime ?? sitting.sessionStartTime;
	const nextEnd = body.sessionEndTime ?? sitting.sessionEndTime;
	if (
		body.sessionStartTime !== undefined ||
		body.sessionEndTime !== undefined
	) {
		const window = assertValidSessionWindow(nextStart, nextEnd);
		updates.sessionStartTime = window.sessionStartTime;
		updates.sessionEndTime = window.sessionEndTime;
	}

	if (body.questionPaperStorageId !== undefined) {
		const previous = sitting.questionPaperStorageId;
		updates.questionPaperStorageId = body.questionPaperStorageId;
		if (previous !== body.questionPaperStorageId) {
			try {
				await ctx.storage.delete(previous);
			} catch {
				// Previous file may already be gone
			}
		}
	}

	if (body.questionPaperFileName !== undefined) {
		if (body.questionPaperFileName === null) {
			updates.questionPaperFileName = undefined;
		} else {
			const trimmed = body.questionPaperFileName.trim();
			updates.questionPaperFileName = trimmed.length > 0 ? trimmed : undefined;
		}
	}

	await ctx.db.patch("assessmentSittings", id, updates);
}

export async function markConducted(
	ctx: AppMutationCtx,
	id: Id<"assessmentSittings">,
) {
	const sitting = await getById(ctx, id);
	if (!sitting) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.NOT_FOUND);
	}
	if (sitting.status === "conducted") {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.ALREADY_CONDUCTED);
	}

	const now = Date.now();
	await ctx.db.patch("assessmentSittings", id, {
		status: "conducted",
		conductedAt: now,
		updatedAt: now,
	});
}

async function deleteSittingRow(
	ctx: AppMutationCtx,
	sitting: Doc<"assessmentSittings">,
) {
	await AssessmentMark.removeAllBySitting(ctx, sitting._id);
	await AssessmentMarkActivityLog.removeAllBySitting(ctx, sitting._id);

	try {
		await ctx.storage.delete(sitting.questionPaperStorageId);
	} catch {
		// Storage object may already be gone
	}
	await ctx.db.delete("assessmentSittings", sitting._id);
}

export async function remove(
	ctx: AppMutationCtx,
	id: Id<"assessmentSittings">,
) {
	const sitting = await getById(ctx, id);
	if (!sitting) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.NOT_FOUND);
	}
	if (sitting.status !== "scheduled") {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.NOT_SCHEDULED);
	}

	await deleteSittingRow(ctx, sitting);
}

/** Cascade helper — deletes sittings for a schema regardless of status. */
export async function removeAllBySchema(
	ctx: AppMutationCtx,
	assessmentSchemaId: Id<"assessmentSchemas">,
) {
	const sittings = await listByAssessmentSchema(ctx, assessmentSchemaId);
	for (const sitting of sittings) {
		await deleteSittingRow(ctx, sitting);
	}
}
