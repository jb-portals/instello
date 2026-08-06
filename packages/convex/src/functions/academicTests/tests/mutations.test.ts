import { describe, expect } from "vitest";
import {
	arrangeAssessmentSchemaReady,
	arrangeOwnerInstitution,
	arrangeProgramSubjectReady,
	baseTest,
	createAssessmentComponent,
	createProgramSubject,
	createSubject,
	expectAppError,
	withSlug,
} from "#__fixtures__/v2/index";
import { api } from "#_generated/api";
import { ERROR_CODES } from "#helpers/constants";

describe("academicTests.createAssessmentSchema", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, programSubject } = await arrangeProgramSubjectReady(t);

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.createAssessmentSchema,
				withSlug(institution, {
					programSubjectId: programSubject._id,
					name: "CIE 1",
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("creates an assessment schema for a program subject", async ({ t }) => {
		const { institution, programSubject, asOwner } =
			await arrangeProgramSubjectReady(t);

		const schemaId = await asOwner().mutation(
			api.academicTests.mutations.createAssessmentSchema,
			withSlug(institution, {
				programSubjectId: programSubject._id,
				name: "CIE 1",
				description: "First internal",
			}),
		);

		expect(schemaId).toBeDefined();

		const rows = await t.run((ctx) =>
			ctx.db.query("assessmentSchemas").collect(),
		);

		expect(rows).toMatchObject([
			{
				_id: schemaId,
				programSubjectId: programSubject._id,
				name: "CIE 1",
				description: "First internal",
			},
		]);
	});

	test("rejects program subject from another institution", async ({ t }) => {
		const owned = await arrangeProgramSubjectReady(t);
		const other = await arrangeOwnerInstitution(t);

		await expectAppError(
			other.asOwner().mutation(
				api.academicTests.mutations.createAssessmentSchema,
				withSlug(other.institution, {
					programSubjectId: owned.programSubject._id,
					name: "Foreign schema",
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.INVALID_PROGRAM_SUBJECT,
		);
	});
});

describe("academicTests.createAssessmentComponent", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, assessmentSchema } =
			await arrangeAssessmentSchemaReady(t);

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.createAssessmentComponent,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					name: "IA 1",
					totalAllotedMarks: 20,
					passingMarks: 8,
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("creates a component appended at the end of the schema order", async ({
		t,
	}) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t, { componentCount: 1 });

		const componentId = await asOwner().mutation(
			api.academicTests.mutations.createAssessmentComponent,
			withSlug(institution, {
				assessmentSchemaId: assessmentSchema._id,
				name: "IA 2",
				totalAllotedMarks: 30,
				passingMarks: 12,
			}),
		);

		const rows = await t.run((ctx) =>
			ctx.db
				.query("assessmentComponents")
				.withIndex("by_assessmentSchema_orderIdx", (q) =>
					q.eq("assessmentSchemaId", assessmentSchema._id),
				)
				.collect(),
		);

		expect(rows).toHaveLength(2);
		expect(rows[1]).toMatchObject({
			_id: componentId,
			name: "IA 2",
			totalAllotedMarks: 30,
			passingMarks: 12,
			orderIdx: 1,
		});
	});

	test("rejects when passing marks exceed total allotted marks", async ({
		t,
	}) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.createAssessmentComponent,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					name: "IA 1",
					totalAllotedMarks: 20,
					passingMarks: 25,
				}),
			),
			ERROR_CODES.ASSESSMENT_COMPONENT.INVALID_MARKS,
		);
	});

	test("rejects unknown assessment schema", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		await t.run((ctx) =>
			ctx.db.delete("assessmentSchemas", assessmentSchema._id),
		);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.createAssessmentComponent,
				withSlug(institution, {
					assessmentSchemaId: assessmentSchema._id,
					name: "IA 1",
					totalAllotedMarks: 20,
					passingMarks: 8,
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND,
		);
	});
});

describe("academicTests.updateAssessmentSchema", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, assessmentSchema } =
			await arrangeAssessmentSchemaReady(t);

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.updateAssessmentSchema,
				withSlug(institution, {
					id: assessmentSchema._id,
					body: { name: "Updated" },
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("updates schema name and description", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		await asOwner().mutation(
			api.academicTests.mutations.updateAssessmentSchema,
			withSlug(institution, {
				id: assessmentSchema._id,
				body: {
					name: "SEE Schema",
					description: "Semester end exam",
				},
			}),
		);

		const updated = await t.run((ctx) =>
			ctx.db.get("assessmentSchemas", assessmentSchema._id),
		);

		expect(updated).toMatchObject({
			name: "SEE Schema",
			description: "Semester end exam",
		});
	});

	test("rejects unknown schema", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		await t.run((ctx) =>
			ctx.db.delete("assessmentSchemas", assessmentSchema._id),
		);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.updateAssessmentSchema,
				withSlug(institution, {
					id: assessmentSchema._id,
					body: { name: "Gone" },
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND,
		);
	});
});

describe("academicTests.updateAssessmentComponent", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, component } = await arrangeAssessmentSchemaReady(t, {
			componentCount: 1,
		});

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.updateAssessmentComponent,
				withSlug(institution, {
					id: component._id,
					body: { name: "Updated IA" },
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("updates component fields", async ({ t }) => {
		const { institution, component, asOwner } =
			await arrangeAssessmentSchemaReady(t, { componentCount: 1 });

		await asOwner().mutation(
			api.academicTests.mutations.updateAssessmentComponent,
			withSlug(institution, {
				id: component._id,
				body: {
					name: "Quiz",
					totalAllotedMarks: 25,
					passingMarks: 10,
				},
			}),
		);

		const updated = await t.run((ctx) =>
			ctx.db.get("assessmentComponents", component._id),
		);

		expect(updated).toMatchObject({
			name: "Quiz",
			totalAllotedMarks: 25,
			passingMarks: 10,
		});
	});

	test("rejects when passing marks exceed total allotted marks", async ({
		t,
	}) => {
		const { institution, component, asOwner } =
			await arrangeAssessmentSchemaReady(t, { componentCount: 1 });

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.updateAssessmentComponent,
				withSlug(institution, {
					id: component._id,
					body: { passingMarks: 100 },
				}),
			),
			ERROR_CODES.ASSESSMENT_COMPONENT.INVALID_MARKS,
		);
	});
});

describe("academicTests.reposAssessmentComponent", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, component } = await arrangeAssessmentSchemaReady(t, {
			componentCount: 2,
		});

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.reposAssessmentComponent,
				withSlug(institution, {
					id: component._id,
					orderIdx: 1,
				}),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("repositions a component and shifts siblings", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		const first = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "A",
			orderIdx: 0,
		});
		const second = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "B",
			orderIdx: 1,
		});
		const third = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "C",
			orderIdx: 2,
		});

		await asOwner().mutation(
			api.academicTests.mutations.reposAssessmentComponent,
			withSlug(institution, {
				id: third._id,
				orderIdx: 0,
			}),
		);

		const rows = await t.run((ctx) =>
			ctx.db
				.query("assessmentComponents")
				.withIndex("by_assessmentSchema_orderIdx", (q) =>
					q.eq("assessmentSchemaId", assessmentSchema._id),
				)
				.collect(),
		);

		expect(
			rows.map((row) => ({ name: row.name, orderIdx: row.orderIdx })),
		).toEqual([
			{ name: "C", orderIdx: 0 },
			{ name: "A", orderIdx: 1 },
			{ name: "B", orderIdx: 2 },
		]);
		expect(rows.map((row) => row._id)).toEqual([
			third._id,
			first._id,
			second._id,
		]);
	});

	test("rejects out-of-range order index", async ({ t }) => {
		const { institution, component, asOwner } =
			await arrangeAssessmentSchemaReady(t, { componentCount: 2 });

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.reposAssessmentComponent,
				withSlug(institution, {
					id: component._id,
					orderIdx: 5,
				}),
			),
			ERROR_CODES.ASSESSMENT_COMPONENT.INVALID_ORDER,
		);
	});
});

describe("academicTests.removeAssessmentSchema", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, assessmentSchema } =
			await arrangeAssessmentSchemaReady(t);

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.removeAssessmentSchema,
				withSlug(institution, { id: assessmentSchema._id }),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("removes schema and cascades components", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t, { componentCount: 2 });

		await asOwner().mutation(
			api.academicTests.mutations.removeAssessmentSchema,
			withSlug(institution, { id: assessmentSchema._id }),
		);

		const schema = await t.run((ctx) =>
			ctx.db.get("assessmentSchemas", assessmentSchema._id),
		);
		const components = await t.run((ctx) =>
			ctx.db
				.query("assessmentComponents")
				.withIndex("by_assessmentSchema_orderIdx", (q) =>
					q.eq("assessmentSchemaId", assessmentSchema._id),
				)
				.collect(),
		);

		expect(schema).toBeNull();
		expect(components).toHaveLength(0);
	});

	test("rejects unknown schema", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		await t.run((ctx) =>
			ctx.db.delete("assessmentSchemas", assessmentSchema._id),
		);

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.removeAssessmentSchema,
				withSlug(institution, { id: assessmentSchema._id }),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND,
		);
	});
});

describe("academicTests.removeAssessmentComponent", () => {
	const test = baseTest();

	test("rejects unauthenticated user", async ({ t }) => {
		const { institution, component } = await arrangeAssessmentSchemaReady(t, {
			componentCount: 1,
		});

		await expectAppError(
			t.mutation(
				api.academicTests.mutations.removeAssessmentComponent,
				withSlug(institution, { id: component._id }),
			),
			ERROR_CODES.BASE.UNAUTHORIZED,
		);
	});

	test("removes a component and reindexes remaining order", async ({ t }) => {
		const { institution, assessmentSchema, asOwner } =
			await arrangeAssessmentSchemaReady(t);

		const first = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "A",
			orderIdx: 0,
		});
		const second = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "B",
			orderIdx: 1,
		});
		const third = await createAssessmentComponent(t, {
			assessmentSchemaId: assessmentSchema._id,
			name: "C",
			orderIdx: 2,
		});

		await asOwner().mutation(
			api.academicTests.mutations.removeAssessmentComponent,
			withSlug(institution, { id: second._id }),
		);

		const removed = await t.run((ctx) =>
			ctx.db.get("assessmentComponents", second._id),
		);
		const rows = await t.run((ctx) =>
			ctx.db
				.query("assessmentComponents")
				.withIndex("by_assessmentSchema_orderIdx", (q) =>
					q.eq("assessmentSchemaId", assessmentSchema._id),
				)
				.collect(),
		);

		expect(removed).toBeNull();
		expect(
			rows.map((row) => ({ name: row.name, orderIdx: row.orderIdx })),
		).toEqual([
			{ name: "A", orderIdx: 0 },
			{ name: "C", orderIdx: 1 },
		]);
		expect(rows.map((row) => row._id)).toEqual([first._id, third._id]);
	});

	test("rejects unknown component", async ({ t }) => {
		const { institution, component, asOwner } =
			await arrangeAssessmentSchemaReady(t, { componentCount: 1 });

		await t.run((ctx) => ctx.db.delete("assessmentComponents", component._id));

		await expectAppError(
			asOwner().mutation(
				api.academicTests.mutations.removeAssessmentComponent,
				withSlug(institution, { id: component._id }),
			),
			ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND,
		);
	});
});

describe("academicTests mutations — cross-institution isolation", () => {
	const test = baseTest();

	test("cannot mutate assessment schema belonging to another institution", async ({
		t,
	}) => {
		const owned = await arrangeAssessmentSchemaReady(t, { componentCount: 1 });
		const other = await arrangeOwnerInstitution(t);

		await expectAppError(
			other.asOwner().mutation(
				api.academicTests.mutations.updateAssessmentSchema,
				withSlug(other.institution, {
					id: owned.assessmentSchema._id,
					body: { name: "Hijacked" },
				}),
			),
			ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND,
		);

		await expectAppError(
			other.asOwner().mutation(
				api.academicTests.mutations.updateAssessmentComponent,
				withSlug(other.institution, {
					id: owned.component._id,
					body: { name: "Hijacked" },
				}),
			),
			ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND,
		);
	});

	test("can create schemas for multiple program subjects in the same institution", async ({
		t,
	}) => {
		const scenario = await arrangeProgramSubjectReady(t);
		const secondSubject = await createSubject(t, {
			institutionId: scenario.institution._id,
			name: "Physics",
			code: "PHY01T",
			alias: "physics",
		});
		const secondProgramSubject = await createProgramSubject(t, {
			programId: scenario.program._id,
			subjectId: secondSubject._id,
			academicStageId: scenario.firstStage._id,
		});

		const firstId = await scenario.asOwner().mutation(
			api.academicTests.mutations.createAssessmentSchema,
			withSlug(scenario.institution, {
				programSubjectId: scenario.programSubject._id,
				name: "Math CIE",
			}),
		);
		const secondId = await scenario.asOwner().mutation(
			api.academicTests.mutations.createAssessmentSchema,
			withSlug(scenario.institution, {
				programSubjectId: secondProgramSubject._id,
				name: "Physics CIE",
			}),
		);

		expect(firstId).not.toEqual(secondId);

		const rows = await t.run((ctx) =>
			ctx.db.query("assessmentSchemas").collect(),
		);
		expect(rows).toHaveLength(2);
		expect(rows.map((row) => row.programSubjectId).sort()).toEqual(
			[scenario.programSubject._id, secondProgramSubject._id].sort(),
		);
	});
});
