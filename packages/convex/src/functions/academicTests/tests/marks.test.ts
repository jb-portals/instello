import { describe, expect } from "vitest";
import {
	seedFaculty,
	seedFacultyMember,
} from "#__fixtures__/seeds/faculty.seed";
import {
	arrangeAssessmentSittingReady,
	baseTest,
	expectAppError,
	ownerIdentity,
	withSlug,
} from "#__fixtures__/v2/index.setup";
import type { AppTest } from "#__fixtures__/v2/types.setup";
import { api } from "#_generated/api";
import type { Id } from "#_generated/dataModel";
import { ERROR_CODES } from "#helpers/constants";

async function seedStudentInClass(
	t: AppTest,
	args: {
		institutionId: string;
		classId: Id<"classes">;
		createdBy: string;
		email?: string;
		usn?: string;
	},
) {
	return await t.run(async (ctx) => {
		const now = Date.now();
		const categoryId = await ctx.db.insert("institutionStudentCategories", {
			institutionId: args.institutionId,
			name: "GM",
			createdAt: now,
			updatedAt: now,
		});

		return await ctx.db.insert("students", {
			institutionId: args.institutionId,
			classId: args.classId,
			firstName: "Rahul",
			lastName: "Kumar",
			usn: args.usn ?? "1MS21CS001",
			email: args.email ?? "rahul.marks+test@example.com",
			gender: "male",
			categoryId,
			phoneNumber: "9876543210",
			createdBy: args.createdBy,
			createdAt: now,
			updatedAt: now,
			searchString: "Rahul Kumar 1MS21CS001",
		});
	});
}

async function arrangeConductedSittingWithStudent(t: AppTest) {
	const ready = await arrangeAssessmentSittingReady(t, { schedule: true });

	await ready
		.asOwner()
		.mutation(
			api.academicTests.mutations.markAssessmentSittingConducted,
			withSlug(ready.institution, { id: ready.sitting._id }),
		);

	const studentId = await seedStudentInClass(t, {
		institutionId: ready.institution._id,
		classId: ready.cls._id,
		createdBy: ready.owner._id,
	});

	return { ...ready, studentId };
}

describe("academicTests.upsertMarks", () => {
	const test = baseTest();

	test("rejects marks on a scheduled sitting", async ({ t }) => {
		const ready = await arrangeAssessmentSittingReady(t, { schedule: true });
		const studentId = await seedStudentInClass(t, {
			institutionId: ready.institution._id,
			classId: ready.cls._id,
			createdBy: ready.owner._id,
		});

		await expectAppError(
			ready.asOwner().mutation(
				api.academicTests.mutations.upsertMarks,
				withSlug(ready.institution, {
					assessmentSittingId: ready.sitting._id,
					entries: [
						{
							assessmentComponentId: ready.component._id,
							studentId,
							marks: 12,
						},
					],
				}),
			),
			ERROR_CODES.ASSESSMENT_MARK.SITTING_NOT_CONDUCTED,
		);
	});

	test("creates marks and activity log for a conducted sitting", async ({
		t,
	}) => {
		const { institution, asOwner, sitting, component, studentId } =
			await arrangeConductedSittingWithStudent(t);

		await asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(institution, {
				assessmentSittingId: sitting._id,
				entries: [
					{
						assessmentComponentId: component._id,
						studentId,
						marks: 15,
					},
				],
			}),
		);

		const marks = await t.run((ctx) =>
			ctx.db
				.query("assessmentMarks")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", sitting._id),
				)
				.collect(),
		);
		expect(marks).toHaveLength(1);
		expect(marks[0]).toMatchObject({
			assessmentComponentId: component._id,
			studentId,
			marks: 15,
		});

		const logs = await t.run((ctx) =>
			ctx.db
				.query("assessmentMarkActivityLogs")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", sitting._id),
				)
				.collect(),
		);
		expect(logs).toHaveLength(1);
		expect(logs[0]?.action).toBe("created");
		expect(logs[0]?.changes).toEqual([
			{
				studentId,
				assessmentComponentId: component._id,
				newMarks: 15,
			},
		]);
	});

	test("updates an existing mark and writes an updated audit entry", async ({
		t,
	}) => {
		const { institution, asOwner, sitting, component, studentId } =
			await arrangeConductedSittingWithStudent(t);

		await asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(institution, {
				assessmentSittingId: sitting._id,
				entries: [
					{
						assessmentComponentId: component._id,
						studentId,
						marks: 10,
					},
				],
			}),
		);

		await asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(institution, {
				assessmentSittingId: sitting._id,
				entries: [
					{
						assessmentComponentId: component._id,
						studentId,
						marks: 18,
					},
				],
			}),
		);

		const marks = await t.run((ctx) =>
			ctx.db
				.query("assessmentMarks")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", sitting._id),
				)
				.collect(),
		);
		expect(marks).toHaveLength(1);
		expect(marks[0]?.marks).toBe(18);

		const logs = await t.run((ctx) =>
			ctx.db
				.query("assessmentMarkActivityLogs")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", sitting._id),
				)
				.collect(),
		);
		expect(logs.map((log) => log.action).sort()).toEqual([
			"created",
			"updated",
		]);
	});

	test("rejects marks above the component total", async ({ t }) => {
		const { institution, asOwner, sitting, component, studentId } =
			await arrangeConductedSittingWithStudent(t);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.upsertMarks,
				withSlug(institution, {
					assessmentSittingId: sitting._id,
					entries: [
						{
							assessmentComponentId: component._id,
							studentId,
							marks: component.totalAllotedMarks + 1,
						},
					],
				}),
			),
			ERROR_CODES.ASSESSMENT_MARK.INVALID_MARKS,
		);
	});

	test("allows assigned faculty to upsert marks", async ({ t }) => {
		const ready = await arrangeConductedSittingWithStudent(t);
		const facultyUser = await t.run((ctx) =>
			seedFacultyMember(ctx, {
				institutionId: ready.institution._id,
				role: "faculty",
				email: "faculty.marks+test@resend.dev",
			}),
		);

		const facultyId = await t.run((ctx) =>
			seedFaculty(ctx, {
				institutionId: ready.institution._id,
				createdBy: ready.owner._id,
				overrides: {
					email: "faculty.marks+test@resend.dev",
					staffId: "STAFF-MARKS",
					status: "active",
					userId: facultyUser._id,
				},
			}),
		);

		await t.run(async (ctx) => {
			const now = Date.now();
			await ctx.db.insert("classSubjectFaculty", {
				classId: ready.cls._id,
				programSubjectId: ready.programSubject._id,
				facultyId,
				createdAt: now,
				updatedAt: now,
			});
		});

		await t
			.withIdentity(ownerIdentity(facultyUser._id, ready.institution._id))
			.mutation(
				api.academicTests.mutations.upsertMarks,
				withSlug(ready.institution, {
					assessmentSittingId: ready.sitting._id,
					entries: [
						{
							assessmentComponentId: ready.component._id,
							studentId: ready.studentId,
							marks: 14,
						},
					],
				}),
			);

		const marks = await t.run((ctx) =>
			ctx.db
				.query("assessmentMarks")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", ready.sitting._id),
				)
				.collect(),
		);
		expect(marks).toHaveLength(1);
		expect(marks[0]?.marks).toBe(14);
	});

	test("denies unassigned faculty from upserting marks", async ({ t }) => {
		const ready = await arrangeConductedSittingWithStudent(t);
		const facultyUser = await t.run((ctx) =>
			seedFacultyMember(ctx, {
				institutionId: ready.institution._id,
				role: "faculty",
				email: "faculty.unassigned+test@resend.dev",
			}),
		);

		await t.run((ctx) =>
			seedFaculty(ctx, {
				institutionId: ready.institution._id,
				createdBy: ready.owner._id,
				overrides: {
					email: "faculty.unassigned+test@resend.dev",
					staffId: "STAFF-UNASSIGNED",
					status: "active",
					userId: facultyUser._id,
				},
			}),
		);

		await expectAppError(
			t
				.withIdentity(ownerIdentity(facultyUser._id, ready.institution._id))
				.mutation(
					api.academicTests.mutations.upsertMarks,
					withSlug(ready.institution, {
						assessmentSittingId: ready.sitting._id,
						entries: [
							{
								assessmentComponentId: ready.component._id,
								studentId: ready.studentId,
								marks: 14,
							},
						],
					}),
				),
			ERROR_CODES.BASE.ACCESS_DENIED,
		);
	});
});

describe("academicTests.deleteMark", () => {
	const test = baseTest();

	test("allows owner to delete a mark with audit", async ({ t }) => {
		const { institution, asOwner, sitting, component, studentId } =
			await arrangeConductedSittingWithStudent(t);

		await asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(institution, {
				assessmentSittingId: sitting._id,
				entries: [
					{
						assessmentComponentId: component._id,
						studentId,
						marks: 11,
					},
				],
			}),
		);

		const mark = await t.run(async (ctx) => {
			return await ctx.db
				.query("assessmentMarks")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", sitting._id),
				)
				.unique();
		});
		if (!mark) {
			throw new Error("Expected mark to exist after upsert");
		}

		await asOwner().mutation(
			api.academicTests.mutations.deleteMark,
			withSlug(institution, { id: mark._id }),
		);

		const remaining = await t.run((ctx) =>
			ctx.db
				.query("assessmentMarks")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", sitting._id),
				)
				.collect(),
		);
		expect(remaining).toHaveLength(0);

		const logs = await t.run((ctx) =>
			ctx.db
				.query("assessmentMarkActivityLogs")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", sitting._id),
				)
				.collect(),
		);
		expect(logs.some((log) => log.action === "deleted")).toBe(true);
	});

	test("denies regular faculty from deleting marks", async ({ t }) => {
		const ready = await arrangeConductedSittingWithStudent(t);
		const facultyUser = await t.run((ctx) =>
			seedFacultyMember(ctx, {
				institutionId: ready.institution._id,
				role: "faculty",
				email: "faculty.delete+test@resend.dev",
			}),
		);

		const facultyId = await t.run((ctx) =>
			seedFaculty(ctx, {
				institutionId: ready.institution._id,
				createdBy: ready.owner._id,
				overrides: {
					email: "faculty.delete+test@resend.dev",
					staffId: "STAFF-DELETE",
					status: "active",
					userId: facultyUser._id,
				},
			}),
		);

		await t.run(async (ctx) => {
			const now = Date.now();
			await ctx.db.insert("classSubjectFaculty", {
				classId: ready.cls._id,
				programSubjectId: ready.programSubject._id,
				facultyId,
				createdAt: now,
				updatedAt: now,
			});
		});

		await ready.asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(ready.institution, {
				assessmentSittingId: ready.sitting._id,
				entries: [
					{
						assessmentComponentId: ready.component._id,
						studentId: ready.studentId,
						marks: 9,
					},
				],
			}),
		);

		const mark = await t.run(async (ctx) => {
			return await ctx.db
				.query("assessmentMarks")
				.withIndex("by_sitting", (q) =>
					q.eq("assessmentSittingId", ready.sitting._id),
				)
				.unique();
		});
		if (!mark) {
			throw new Error("Expected mark to exist after upsert");
		}

		await expectAppError(
			t
				.withIdentity(ownerIdentity(facultyUser._id, ready.institution._id))
				.mutation(
					api.academicTests.mutations.deleteMark,
					withSlug(ready.institution, { id: mark._id }),
				),
			ERROR_CODES.BASE.ACCESS_DENIED,
		);
	});
});

describe("academicTests marks cascade", () => {
	const test = baseTest();

	test("deletes marks when the assessment schema is removed", async ({ t }) => {
		const {
			institution,
			asOwner,
			sitting,
			component,
			studentId,
			assessmentSchema,
		} = await arrangeConductedSittingWithStudent(t);

		await asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(institution, {
				assessmentSittingId: sitting._id,
				entries: [
					{
						assessmentComponentId: component._id,
						studentId,
						marks: 13,
					},
				],
			}),
		);

		await asOwner().mutation(
			api.academicTests.mutations.removeAssessmentSchema,
			withSlug(institution, { id: assessmentSchema._id }),
		);

		const marks = await t.run((ctx) =>
			ctx.db.query("assessmentMarks").collect(),
		);
		const logs = await t.run((ctx) =>
			ctx.db.query("assessmentMarkActivityLogs").collect(),
		);
		expect(marks).toHaveLength(0);
		expect(logs).toHaveLength(0);
	});

	test("deletes marks when the assessment component is removed", async ({
		t,
	}) => {
		const { institution, asOwner, sitting, component, studentId } =
			await arrangeConductedSittingWithStudent(t);

		await asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(institution, {
				assessmentSittingId: sitting._id,
				entries: [
					{
						assessmentComponentId: component._id,
						studentId,
						marks: 7,
					},
				],
			}),
		);

		await asOwner().mutation(
			api.academicTests.mutations.removeAssessmentComponent,
			withSlug(institution, { id: component._id }),
		);

		const marks = await t.run((ctx) =>
			ctx.db.query("assessmentMarks").collect(),
		);
		expect(marks).toHaveLength(0);
	});
});

describe("academicTests.getMarksSheet / listConductedSittings", () => {
	const test = baseTest();

	test("lists conducted sittings and returns a marks sheet", async ({ t }) => {
		const {
			institution,
			asOwner,
			sitting,
			component,
			studentId,
			programSubject,
		} = await arrangeConductedSittingWithStudent(t);

		const conductedBefore = await asOwner().query(
			api.academicTests.queries.listConductedSittingsForProgramSubject,
			withSlug(institution, { programSubjectId: programSubject._id }),
		);
		expect(conductedBefore.map((row) => row._id)).toEqual([sitting._id]);
		expect(conductedBefore[0]?.latestActivity).toBeUndefined();

		await asOwner().mutation(
			api.academicTests.mutations.upsertMarks,
			withSlug(institution, {
				assessmentSittingId: sitting._id,
				entries: [
					{
						assessmentComponentId: component._id,
						studentId,
						marks: 16,
					},
				],
			}),
		);

		const conductedAfter = await asOwner().query(
			api.academicTests.queries.listConductedSittingsForProgramSubject,
			withSlug(institution, { programSubjectId: programSubject._id }),
		);
		expect(conductedAfter[0]?.latestActivity).toMatchObject({
			description: "Created 1 mark",
		});
		expect(
			conductedAfter[0]?.latestActivity?.actor.name.length,
		).toBeGreaterThan(0);
		expect(conductedAfter[0]?.latestActivity?.updatedAt).toBeTypeOf("number");

		const sheet = await asOwner().query(
			api.academicTests.queries.getMarksSheet,
			withSlug(institution, { assessmentSittingId: sitting._id }),
		);

		expect(sheet.sitting._id).toBe(sitting._id);
		expect(sheet.components.map((row) => row._id)).toContain(component._id);
		expect(sheet.students.map((row) => row._id)).toContain(studentId);
		expect(sheet.marks).toHaveLength(1);
		expect(sheet.canDeleteMarks).toBe(true);
	});

	test("lists conducted sittings for an assigned class subject", async ({
		t,
	}) => {
		const ready = await arrangeConductedSittingWithStudent(t);
		const facultyUser = await t.run((ctx) =>
			seedFacultyMember(ctx, {
				institutionId: ready.institution._id,
				role: "faculty",
				email: "faculty.list+test@resend.dev",
			}),
		);
		const facultyId = await t.run((ctx) =>
			seedFaculty(ctx, {
				institutionId: ready.institution._id,
				createdBy: ready.owner._id,
				overrides: {
					email: "faculty.list+test@resend.dev",
					staffId: "STAFF-LIST",
					status: "active",
					userId: facultyUser._id,
				},
			}),
		);

		await t.run(async (ctx) => {
			const now = Date.now();
			await ctx.db.insert("classSubjectFaculty", {
				classId: ready.cls._id,
				programSubjectId: ready.programSubject._id,
				facultyId,
				createdAt: now,
				updatedAt: now,
			});
		});

		const sittings = await t
			.withIdentity(ownerIdentity(facultyUser._id, ready.institution._id))
			.query(
				api.academicTests.queries.listConductedSittingsForAssigned,
				withSlug(ready.institution, {
					classId: ready.cls._id,
					programSubjectId: ready.programSubject._id,
				}),
			);

		expect(sittings.map((row) => row._id)).toEqual([ready.sitting._id]);
	});
});
