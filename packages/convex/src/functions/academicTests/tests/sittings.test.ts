import { describe, expect } from "vitest";
import {
	seedFaculty,
	seedFacultyMember,
} from "#__fixtures__/seeds/faculty.seed";
import {
	arrangeAssessmentSittingReady,
	arrangeOwnerInstitution,
	baseTest,
	createClass,
	expectAppError,
	ownerIdentity,
	storeQuestionPaperPdf,
	withSlug,
} from "#__fixtures__/v2/index.setup";
import { api } from "#_generated/api";
import { ERROR_CODES } from "#helpers/constants";

describe("academicTests.scheduleAssessmentSitting", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, assessmentSchema, cls, questionPaperStorageId } =
			await arrangeAssessmentSittingReady(t);

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.scheduleAssessmentSitting,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					classId: cls._id,
					sessionDate: "2026-08-10",
					sessionStartTime: "09:00",
					sessionEndTime: "12:00",
					questionPaperStorageId,
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("schedules a sitting with question paper for an eligible class", async ({
		t,
	}) => {
		const {
			institution,
			asOwner,
			assessmentSchema,
			cls,
			programSubject,
			questionPaperStorageId,
		} = await arrangeAssessmentSittingReady(t);

		const sittingId = await asOwner().mutation(
			api.academicTests.mutations.scheduleAssessmentSitting,
			withSlug(institution, {
				assessmentSchemaId: assessmentSchema._id,
				classId: cls._id,
				sessionDate: "2026-08-10",
				sessionStartTime: "09:00",
				sessionEndTime: "12:00",
				questionPaperStorageId,
				questionPaperFileName: "cie1.pdf",
			}),
		);

		const sitting = await t.run((ctx) =>
			ctx.db.get("assessmentSittings", sittingId),
		);

		expect(sitting).toMatchObject({
			_id: sittingId,
			assessmentSchemaId: assessmentSchema._id,
			classId: cls._id,
			programSubjectId: programSubject._id,
			sessionDate: "2026-08-10",
			sessionStartTime: "09:00",
			sessionEndTime: "12:00",
			questionPaperStorageId,
			questionPaperFileName: "cie1.pdf",
			status: "scheduled",
		});
	});

	test("rejects duplicate sitting for same class and schema", async ({ t }) => {
		const { institution, asOwner, assessmentSchema, cls } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		const secondPaper = await storeQuestionPaperPdf(t);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.scheduleAssessmentSitting,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					classId: cls._id,
					sessionDate: "2026-08-11",
					sessionStartTime: "09:00",
					sessionEndTime: "12:00",
					questionPaperStorageId: secondPaper,
				}),
			),
			ERROR_CODES.ASSESSMENT_SITTING.ALREADY_EXISTS,
		);
	});

	test("rejects class on a different academic stage", async ({ t }) => {
		const {
			institution,
			asOwner,
			assessmentSchema,
			program,
			secondStage,
			questionPaperStorageId,
		} = await arrangeAssessmentSittingReady(t);

		const wrongStageClass = await createClass(t, {
			programId: program._id,
			currentHeadStageId: secondStage._id,
			name: "Wrong Stage",
			slug: "wrong-stage",
		});

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.scheduleAssessmentSitting,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					classId: wrongStageClass._id,
					sessionDate: "2026-08-10",
					sessionStartTime: "09:00",
					sessionEndTime: "12:00",
					questionPaperStorageId,
				}),
			),
			ERROR_CODES.ASSESSMENT_SITTING.INVALID_CLASS,
		);
	});

	test("rejects invalid session date", async ({ t }) => {
		const {
			institution,
			asOwner,
			assessmentSchema,
			cls,
			questionPaperStorageId,
		} = await arrangeAssessmentSittingReady(t);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.scheduleAssessmentSitting,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					classId: cls._id,
					sessionDate: "10-08-2026",
					sessionStartTime: "09:00",
					sessionEndTime: "12:00",
					questionPaperStorageId,
				}),
			),
			ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_DATE,
		);
	});

	test("rejects invalid session time format", async ({ t }) => {
		const {
			institution,
			asOwner,
			assessmentSchema,
			cls,
			questionPaperStorageId,
		} = await arrangeAssessmentSittingReady(t);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.scheduleAssessmentSitting,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					classId: cls._id,
					sessionDate: "2026-08-10",
					sessionStartTime: "9:00",
					sessionEndTime: "12:00",
					questionPaperStorageId,
				}),
			),
			ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_TIME,
		);
	});

	test("rejects end time before or equal to start time", async ({ t }) => {
		const {
			institution,
			asOwner,
			assessmentSchema,
			cls,
			questionPaperStorageId,
		} = await arrangeAssessmentSittingReady(t);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.scheduleAssessmentSitting,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					classId: cls._id,
					sessionDate: "2026-08-10",
					sessionStartTime: "12:00",
					sessionEndTime: "09:00",
					questionPaperStorageId,
				}),
			),
			ERROR_CODES.ASSESSMENT_SITTING.INVALID_SESSION_TIME,
		);
	});

	test("denies regular faculty from scheduling", async ({ t }) => {
		const ready = await arrangeAssessmentSittingReady(t);
		const facultyUser = await t.run((ctx) =>
			seedFacultyMember(ctx, {
				institutionId: ready.institution._id,
				role: "faculty",
				email: "faculty.schedule+test@resend.dev",
			}),
		);

		await t.run((ctx) =>
			seedFaculty(ctx, {
				institutionId: ready.institution._id,
				createdBy: ready.owner._id,
				overrides: {
					email: "faculty.schedule+test@resend.dev",
					staffId: "STAFF-SCH-FAC",
					status: "active",
					userId: facultyUser._id,
				},
			}),
		);

		await expectAppError(
			t
				.withIdentity(ownerIdentity(facultyUser._id, ready.institution._id))
				.mutation(
					api.academicTests.mutations.scheduleAssessmentSitting,
					withSlug(ready.institution, {
						assessmentSchemaId: ready.assessmentSchema._id,
						classId: ready.cls._id,
						sessionDate: "2026-08-10",
						sessionStartTime: "09:00",
						sessionEndTime: "12:00",
						questionPaperStorageId: ready.questionPaperStorageId,
					}),
				),
			ERROR_CODES.BASE.ACCESS_DENIED,
		);
	});

	test("allows Head of Program to schedule", async ({ t }) => {
		const ready = await arrangeAssessmentSittingReady(t);
		const facultyUser = await t.run((ctx) =>
			seedFacultyMember(ctx, {
				institutionId: ready.institution._id,
				role: "faculty",
				email: "hop.schedule+test@resend.dev",
			}),
		);

		const facultyId = await t.run((ctx) =>
			seedFaculty(ctx, {
				institutionId: ready.institution._id,
				createdBy: ready.owner._id,
				overrides: {
					email: "hop.schedule+test@resend.dev",
					staffId: "STAFF-SCH-HOP",
					status: "active",
					userId: facultyUser._id,
				},
			}),
		);

		await t.run(async (ctx) => {
			const now = Date.now();
			await ctx.db.insert("programFaculty", {
				programId: ready.program._id,
				facultyId,
				isHeadOfProgram: true,
				createdAt: now,
				updatedAt: now,
			});
		});

		const sittingId = await t
			.withIdentity(ownerIdentity(facultyUser._id, ready.institution._id))
			.mutation(
				api.academicTests.mutations.scheduleAssessmentSitting,
				withSlug(ready.institution, {
					assessmentSchemaId: ready.assessmentSchema._id,
					classId: ready.cls._id,
					sessionDate: "2026-08-10",
					sessionStartTime: "09:00",
					sessionEndTime: "12:00",
					questionPaperStorageId: ready.questionPaperStorageId,
				}),
			);

		expect(sittingId).toBeDefined();
	});
});

describe("academicTests.markAssessmentSittingConducted", () => {
	const test = baseTest();

	test("marks a scheduled sitting as conducted", async ({ t }) => {
		const { institution, asOwner, sitting } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		await asOwner().mutation(
			api.academicTests.mutations.markAssessmentSittingConducted,
			withSlug(institution, { id: sitting._id }),
		);

		const updated = await t.run((ctx) =>
			ctx.db.get("assessmentSittings", sitting._id),
		);

		expect(updated).toMatchObject({
			status: "conducted",
		});
		expect(updated?.conductedAt).toBeTypeOf("number");
	});

	test("rejects marking an already conducted sitting", async ({ t }) => {
		const { institution, asOwner, sitting } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		await asOwner().mutation(
			api.academicTests.mutations.markAssessmentSittingConducted,
			withSlug(institution, { id: sitting._id }),
		);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.markAssessmentSittingConducted,
				withSlug(institution, { id: sitting._id }),
			),
			ERROR_CODES.ASSESSMENT_SITTING.ALREADY_CONDUCTED,
		);
	});
});

describe("academicTests.updateAssessmentSitting / removeAssessmentSitting", () => {
	const test = baseTest();

	test("updates session date while scheduled", async ({ t }) => {
		const { institution, asOwner, sitting } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		await asOwner().mutation(
			api.academicTests.mutations.updateAssessmentSitting,
			withSlug(institution, {
				id: sitting._id,
				body: { sessionDate: "2026-09-01" },
			}),
		);

		const updated = await t.run((ctx) =>
			ctx.db.get("assessmentSittings", sitting._id),
		);
		expect(updated?.sessionDate).toBe("2026-09-01");
	});

	test("blocks update after conducted", async ({ t }) => {
		const { institution, asOwner, sitting } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		await asOwner().mutation(
			api.academicTests.mutations.markAssessmentSittingConducted,
			withSlug(institution, { id: sitting._id }),
		);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.updateAssessmentSitting,
				withSlug(institution, {
					id: sitting._id,
					body: { sessionDate: "2026-09-01" },
				}),
			),
			ERROR_CODES.ASSESSMENT_SITTING.NOT_SCHEDULED,
		);
	});

	test("removes a scheduled sitting", async ({ t }) => {
		const { institution, asOwner, sitting } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		await asOwner().mutation(
			api.academicTests.mutations.removeAssessmentSitting,
			withSlug(institution, { id: sitting._id }),
		);

		const gone = await t.run((ctx) =>
			ctx.db.get("assessmentSittings", sitting._id),
		);
		expect(gone).toBeNull();
	});

	test("blocks remove after conducted", async ({ t }) => {
		const { institution, asOwner, sitting } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		await asOwner().mutation(
			api.academicTests.mutations.markAssessmentSittingConducted,
			withSlug(institution, { id: sitting._id }),
		);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.removeAssessmentSitting,
				withSlug(institution, { id: sitting._id }),
			),
			ERROR_CODES.ASSESSMENT_SITTING.NOT_SCHEDULED,
		);
	});

	test("cascades sitting delete when schema is removed", async ({ t }) => {
		const { institution, asOwner, sitting, assessmentSchema } =
			await arrangeAssessmentSittingReady(t, { schedule: true });

		await asOwner().mutation(
			api.academicTests.mutations.removeAssessmentSchema,
			withSlug(institution, { id: assessmentSchema._id }),
		);

		const gone = await t.run((ctx) =>
			ctx.db.get("assessmentSittings", sitting._id),
		);
		expect(gone).toBeNull();
	});
});

describe("academicTests.listAssessmentSittings", () => {
	const test = baseTest();

	test("lists sittings with class and schema names", async ({ t }) => {
		const {
			institution,
			asOwner,
			programSubject,
			sitting,
			cls,
			assessmentSchema,
		} = await arrangeAssessmentSittingReady(t, { schedule: true });

		const rows = await asOwner().query(
			api.academicTests.queries.listAssessmentSittings,
			withSlug(institution, { programSubjectId: programSubject._id }),
		);

		expect(rows).toHaveLength(1);
		expect(rows[0]).toMatchObject({
			_id: sitting._id,
			classId: cls._id,
			className: cls.name,
			assessmentSchemaId: assessmentSchema._id,
			assessmentSchemaName: assessmentSchema.name,
			sessionDate: "2026-08-10",
			sessionStartTime: "09:00",
			sessionEndTime: "12:00",
			status: "scheduled",
		});
		expect(rows[0]?.questionPaperUrl).toBeTruthy();
	});

	test("lists eligible classes for the program subject stage", async ({
		t,
	}) => {
		const { institution, asOwner, programSubject, cls, program, secondStage } =
			await arrangeAssessmentSittingReady(t);

		await createClass(t, {
			programId: program._id,
			currentHeadStageId: secondStage._id,
			name: "Other Stage Class",
			slug: "other-stage",
		});

		const eligible = await asOwner().query(
			api.academicTests.queries.listEligibleClassesForSitting,
			withSlug(institution, { programSubjectId: programSubject._id }),
		);

		expect(eligible.map((c) => c._id)).toEqual([cls._id]);
	});

	test("rejects listing sittings for another institution", async ({ t }) => {
		const owned = await arrangeAssessmentSittingReady(t, { schedule: true });
		const other = await arrangeOwnerInstitution(t);

		await expectAppError(
			other.asOwner().query(
				api.academicTests.queries.listAssessmentSittings,
				withSlug(other.institution, {
					programSubjectId: owned.programSubject._id,
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.INVALID_PROGRAM_SUBJECT,
		);
	});
});

describe("academicTests assessment schema name uniqueness", () => {
	const test = baseTest();

	test("rejects duplicate schema name on create", async ({ t }) => {
		const { institution, asOwner, programSubject, assessmentSchema } =
			await arrangeAssessmentSittingReady(t);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.createAssessmentSchema,
				withSlug(institution, {
					programSubjectId: programSubject._id,
					name: assessmentSchema.name.toUpperCase(),
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NAME_ALREADY_EXISTS,
		);
	});

	test("rejects rename to an existing schema name", async ({ t }) => {
		const { institution, asOwner, programSubject, assessmentSchema } =
			await arrangeAssessmentSittingReady(t);

		const otherId = await asOwner().mutation(
			api.academicTests.mutations.createAssessmentSchema,
			withSlug(institution, {
				programSubjectId: programSubject._id,
				name: "SEE",
			}),
		);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.updateAssessmentSchema,
				withSlug(institution, {
					id: otherId,
					body: { name: assessmentSchema.name },
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NAME_ALREADY_EXISTS,
		);
	});
});
