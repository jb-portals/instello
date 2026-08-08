import type { Doc, Id } from "#_generated/dataModel";
import type { CreatedAssessmentComponent } from "../factories/assessmentComponent.setup";
import {
	type CreatedClass,
	createClass,
	storeQuestionPaperPdf,
} from "../factories/class.setup";
import type { AppTest } from "../types.setup";
import {
	type AssessmentSchemaWithComponents,
	arrangeAssessmentSchemaReady,
} from "./assessmentSchemaReady.setup";

type AssessmentSittingReadyBase = AssessmentSchemaWithComponents & {
	cls: CreatedClass;
	questionPaperStorageId: Id<"_storage">;
};

export type AssessmentSittingReady = AssessmentSittingReadyBase & {
	sitting?: Doc<"assessmentSittings">;
};

export type AssessmentSittingScheduled = AssessmentSittingReadyBase & {
	sitting: Doc<"assessmentSittings">;
};

/**
 * Assessment schema + component + eligible class + PDF storage id.
 * Optionally creates a scheduled sitting when `schedule` is true.
 */
export async function arrangeAssessmentSittingReady(
	t: AppTest,
	options: {
		schedule: true;
		sessionDate?: string;
	},
): Promise<AssessmentSittingScheduled>;
export async function arrangeAssessmentSittingReady(
	t: AppTest,
	options?: {
		schedule?: false;
		sessionDate?: string;
	},
): Promise<AssessmentSittingReadyBase>;
export async function arrangeAssessmentSittingReady(
	t: AppTest,
	options?: {
		schedule?: boolean;
		sessionDate?: string;
	},
): Promise<AssessmentSittingReady | AssessmentSittingScheduled> {
	const base = await arrangeAssessmentSchemaReady(t, { componentCount: 1 });
	const cls = await createClass(t, {
		programId: base.program._id,
		currentHeadStageId: base.firstStage._id,
	});
	const questionPaperStorageId = await storeQuestionPaperPdf(t);

	if (!options?.schedule) {
		return {
			...base,
			cls,
			questionPaperStorageId,
		};
	}

	const sessionDate = options.sessionDate ?? "2026-08-10";
	const sittingId = await t.run(async (ctx) => {
		const now = Date.now();
		return await ctx.db.insert("assessmentSittings", {
			assessmentSchemaId: base.assessmentSchema._id,
			classId: cls._id,
			programSubjectId: base.programSubject._id,
			sessionDate,
			sessionStartTime: "09:00",
			sessionEndTime: "12:00",
			questionPaperStorageId,
			questionPaperFileName: "cie1.pdf",
			status: "scheduled",
			createdAt: now,
			updatedAt: now,
		});
	});

	const sitting = await t.run(async (ctx) => {
		return await ctx.db.get(
			"assessmentSittings",
			sittingId as Id<"assessmentSittings">,
		);
	});

	if (!sitting) {
		throw new Error("Failed to create assessment sitting");
	}

	return {
		...base,
		cls,
		questionPaperStorageId,
		sitting,
	};
}

export type { CreatedAssessmentComponent };
