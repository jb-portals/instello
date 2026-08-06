import { describe, expect } from "vitest";
import {
	arrangeAssessmentSchemaReady,
	arrangeOwnerInstitution,
	arrangeProgramSubjectReady,
	baseTest,
	createAssessmentComponent,
	createAssessmentSchema,
	expectAppError,
	withSlug,
} from "#__fixtures__/v2/index";
import { api } from "#_generated/api";
import { ERROR_CODES } from "#helpers/constants";

describe("academicTests.listAcademicSchemas", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, programSubject } = await arrangeProgramSubjectReady(t);

		await expectAppError(
			t.query(
				api.academicTests.queries.listAcademicSchemas,
				withSlug(institution, {
					programSubjectId: programSubject._id,
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("returns empty list when no schemas exist", async ({ t }) => {
		const { institution, programSubject, asOwner } =
			await arrangeProgramSubjectReady(t);

		const result = await asOwner().query(
			api.academicTests.queries.listAcademicSchemas,
			withSlug(institution, {
				programSubjectId: programSubject._id,
			}),
		);

		expect(result).toEqual([]);
	});

	test("lists schemas for the given program subject only", async ({ t }) => {
		const scenario = await arrangeProgramSubjectReady(t);
		const otherSubject = await arrangeProgramSubjectReady(t);

		const first = await createAssessmentSchema(t, {
			programSubjectId: scenario.programSubject._id,
			name: "CIE 1",
			description: "First",
		});
		const second = await createAssessmentSchema(t, {
			programSubjectId: scenario.programSubject._id,
			name: "CIE 2",
		});
		await createAssessmentSchema(t, {
			programSubjectId: otherSubject.programSubject._id,
			name: "Other CIE",
		});

		const result = await scenario.asOwner().query(
			api.academicTests.queries.listAcademicSchemas,
			withSlug(scenario.institution, {
				programSubjectId: scenario.programSubject._id,
			}),
		);

		expect(result).toHaveLength(2);
		expect(result).toEqual(
			expect.arrayContaining([
				expect.objectContaining({
					_id: first._id,
					name: "CIE 1",
					description: "First",
				}),
				expect.objectContaining({
					_id: second._id,
					name: "CIE 2",
				}),
			]),
		);
	});

	test("rejects program subject from another institution", async ({ t }) => {
		const owned = await arrangeProgramSubjectReady(t);
		const other = await arrangeOwnerInstitution(t);

		await expectAppError(
			other.asOwner().query(
				api.academicTests.queries.listAcademicSchemas,
				withSlug(other.institution, {
					programSubjectId: owned.programSubject._id,
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.INVALID_PROGRAM_SUBJECT,
		);
	});
});

describe("academicTests.listAcademicComponents", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, assessmentSchema } =
			await arrangeAssessmentSchemaReady(t);

		await expectAppError(
			t.query(
				api.academicTests.queries.listAcademicComponents,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("returns empty list when schema has no components", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		const result = await asOwner().query(
			api.academicTests.queries.listAcademicComponents,
			withSlug(institution, {
				assessmentSchemaId: assessmentSchema._id,
			}),
		);

		expect(result).toEqual([]);
	});

	test("lists components ordered by orderIdx", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		const c2 = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "IA 2",
			orderIdx: 1,
			totalAllotedMarks: 30,
			passingMarks: 12,
		});
		const c1 = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "IA 1",
			orderIdx: 0,
			totalAllotedMarks: 20,
			passingMarks: 8,
		});
		const c3 = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "IA 3",
			orderIdx: 2,
			totalAllotedMarks: 50,
			passingMarks: 20,
		});

		const result = await asOwner().query(
			api.academicTests.queries.listAcademicComponents,
			withSlug(institution, {
				assessmentSchemaId: assessmentSchema._id,
			}),
		);

		expect(result).toEqual([
			expect.objectContaining({
				_id: c1._id,
				name: "IA 1",
				orderIdx: 0,
				totalAllotedMarks: 20,
				passingMarks: 8,
			}),
			expect.objectContaining({
				_id: c2._id,
				name: "IA 2",
				orderIdx: 1,
				totalAllotedMarks: 30,
				passingMarks: 12,
			}),
			expect.objectContaining({
				_id: c3._id,
				name: "IA 3",
				orderIdx: 2,
				totalAllotedMarks: 50,
				passingMarks: 20,
			}),
		]);
	});

	test("rejects unknown assessment schema", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		await t.run((ctx) =>
			ctx.db.delete("assessmentSchemas", assessmentSchema._id),
		);

		await expectAppError(
			asOwner().query(
				api.academicTests.queries.listAcademicComponents,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND,
		);
	});

	test("rejects schema belonging to another institution", async ({ t }) => {
		const owned = await arrangeAssessmentSchemaReady(t);
		const other = await arrangeOwnerInstitution(t);

		await expectAppError(
			other.asOwner().query(
				api.academicTests.queries.listAcademicComponents,
				withSlug(other.institution, {
					assessmentSchemaId: owned.assessmentSchema._id,
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND,
		);
	});
});
